package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import br.gov.rn.natal.cadpgmapi.audit.utils.AuditDiffUtil;
import br.gov.rn.natal.cadpgmapi.auth.dto.response.AdminResetPasswordResponseDTO;
import br.gov.rn.natal.cadpgmapi.auth.mappers.RegisterUserMapper;
import br.gov.rn.natal.cadpgmapi.dto.request.UsuarioRegisterRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.request.UsuarioRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.UsuarioRegisterResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.UsuarioResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.update.UsuarioUpdateDTO;
import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.mapper.UsuarioMapper;
import br.gov.rn.natal.cadpgmapi.mapper.UsuarioUpdateMapper;
import br.gov.rn.natal.cadpgmapi.repository.UsuarioRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UsuarioService extends BaseGenericService<Usuario, UsuarioRequestDTO, UsuarioResponseDTO, Integer> {
    private final UsuarioRepository usuarioRepository;
    private final UsuarioUpdateMapper usuarioUpdateMapper;
    private final PasswordEncoder passwordEncoder;
    private final RegisterUserMapper registerUserMapper;

    // Construtor
    public UsuarioService(
            UsuarioRepository repository,
            UsuarioMapper mapper,
            RegisterUserMapper registerUserMapper,
            UsuarioUpdateMapper usuarioUpdateMapper,
            PasswordEncoder passwordEncoder
    ) {
        super(repository, mapper);
        this.usuarioRepository = repository;
        this.usuarioUpdateMapper = usuarioUpdateMapper;
        this.passwordEncoder = passwordEncoder;
        this.registerUserMapper = registerUserMapper;
    }

    @Transactional
    @Auditable(action = AuditAction.INSERT, entity = "Usuário")
    public UsuarioRegisterResponseDTO registerNewUserPublic(UsuarioRegisterRequestDTO dto) {
        // Chama método utilitário privado para validar email e username
       validateEmailAndUsername(dto.email().trim(), dto.userName().trim(), null);

        // Instancia o usuário apenas com os dados seguros
        Usuario newUser = registerUserMapper.toEntity(dto);

        // Se é o próprio usuário que fez o cadastro, desativa o perfil
        newUser.setActivated(false);

        // Chama o méthod beforeSave para criptografar a senha e
        // inserr a permissão padrão Guest ao usuário
        beforeSave(newUser);

        // Salva no banco
        return registerUserMapper.toDto(usuarioRepository.save(newUser));
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE, entity = "Usuário")
    public UsuarioResponseDTO updateProfile(Integer id, UsuarioUpdateDTO dto) {
        // Busca o usuário atual no banco
        Usuario existingUsuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        // Valida duplicidade de E-mail (se ele estiver tentando mudar)
        validateEmailAndUsername(dto.email().trim(), dto.userName().trim(), id);

        // Chama o método utilitário para checar se é o Adminsitrador Geral
        // que está logado. Se sim, deixa altetar os dados. Se não, bloqueia.
        onlyAdminMakeChange(existingUsuario);

        // 1. Tira a FOTO ANTIGA usando o mapper genérico do pai
        UsuarioResponseDTO oldSnapshot = mapper.toDto(existingUsuario);

        // 2. Aplica as alterações e salva
        usuarioUpdateMapper.updateEntityFromDTO(existingUsuario, dto);
        // Garante criptografia se houver troca de senha
        beforeSave(existingUsuario);
        usuarioRepository.saveAndFlush(existingUsuario);
        entityManager.refresh(existingUsuario);

        // 3. Tira a FOTO NOVA
        UsuarioResponseDTO newSnapshot = mapper.toDto(existingUsuario);

        // 4. Gera o Diff e injeta no contexto
        String diffLog = AuditDiffUtil.generateDiff(oldSnapshot, newSnapshot);
        if (diffLog != null && !diffLog.isBlank()) {
            AuditContextHolder.setLogDetalhes("Dados atualizados: " + diffLog);
        } else {
            AuditContextHolder.setLogDetalhes("Atualização de Perfil: Nenhuma alteração detectada.");
        }

        return newSnapshot;
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE, entity = "Usuário")
    public AdminResetPasswordResponseDTO resetPasswordByAdmin(Integer id) {
        Usuario existingUsario = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        // 1. Foto Antiga
        UsuarioResponseDTO oldSnapshot = mapper.toDto(existingUsario);

        String temporaryPassword = generateRandomPassword(8);

        existingUsario.setPassword(passwordEncoder.encode(temporaryPassword));
        existingUsario.setForcePasswordChange(true);

        repository.saveAndFlush(existingUsario);
        entityManager.refresh(existingUsario);

        // 2. Foto Nova
        UsuarioResponseDTO newSnapshot = mapper.toDto(existingUsario);

        // 3. Auditoria
        String diffLog = AuditDiffUtil.generateDiff(oldSnapshot, newSnapshot);
        AuditContextHolder.setLogDetalhes("Reset de Senha pelo Admin: " + diffLog);

        return new AdminResetPasswordResponseDTO(temporaryPassword);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> findByFilters(
            String name, String userName, String email, Pageable pageable
    ) {
        // BLOCO DA SPECIFICATION: Monta as regras (a "receita" do SQL)
        Specification<Usuario> spec = (root, query, cb) -> {
            // Começa neutro (1=1)
            Predicate predicate = cb.conjunction();

            // Se o Nome for informado, monta o SQL de busca por Nome
            if (name != null && !name.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.like(
                        root.get("name"), "%" + name.trim() + "%")
                );
            }

            // Se o userName for informado, monta o SQL de busca por userName
            if (userName != null && !userName.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.like(
                        cb.lower(root.get("userName")), "%" + userName.trim().toLowerCase() + "%")
                );
            }

            if (email!= null && !email.trim().isEmpty()) {
                predicate = cb.and(
                        predicate, cb.like(
                                cb.lower(root.get("email")), "%" + email.trim().toLowerCase() + "%")
                );

            }

            // Encerra a montagem das regras
            return predicate;
        };
        // BLOCO DE EXECUÇÃO: Vai no banco e converte para DTO
        // O retorno real que vai para o Controller
        return usuarioRepository.findAll(spec, pageable)
                .map(mapper::toDto);
    }

    // MÉTODOS EXCLUSIVOS DE REGRA DE NEGÓCIO
    @Override
    protected void beforeCreate(UsuarioRequestDTO dto) {
        // Chama método utilitário privado para validar email e username
       validateEmailAndUsername(dto.email().trim(), dto.userName().trim(), null);

    }

    // Methodo com assinatura original declarada na classe pai (BasicGenericService)
    @Override
    protected void beforeUpdate(UsuarioRequestDTO dto, Usuario entity) {
        // Chama método utilitário privado para validar email e username
        validateEmailAndUsername(dto.email().trim(), dto.userName().trim(), entity.getId());
        onlyAdminMakeChange(entity);
    }

    // Criptografa a senha
    @Override
    protected void beforeSave(Usuario entity) {
        // Pega a senha enviada
        String pwd = entity.getPassword();

        // Verifica se a senha existe
        if (pwd != null) {
            // Um hash BCrypt válido SEMPRE tem 60 caracteres e começa com $2a$, $2b$ ou $2y$
            boolean isAlreadyHashed = pwd.length() == 60
                    && (pwd.startsWith("$2a$") || pwd.startsWith("$2b$") || pwd.startsWith("$3y$"));

            // 3. Se NÃO for um hash, a senha é criptografada
            if (!isAlreadyHashed) {
                entity.setPassword(passwordEncoder.encode(pwd));
            }
        }
        // Só injeta a permissão "guest" se a lista estiver vazia (novo usuário sem permissões)
        if (entity.getPermissions() == null || entity.getPermissions().isEmpty()) {
            // Permissão guest adicionada
            entity.setPermissions(new HashSet<>(Set.of("guest")));
        }
    }

    protected void beforeDelete(Usuario entity) {
        String loggedUser = getLoggedUser();
        String targetUser = entity.getUsername().trim();

        // Buscamos o usuário no banco usando a identidade do Token
        if (entity == null || entity.getPassword() == null || entity.getPassword().isEmpty()) {
            return;
        }

        // Ninguém apaga o Procurador Geral
        if ("pgmnet".trim().equalsIgnoreCase(targetUser)) {
            throw new BusinessException("O (<strong> Adminsitrador Geral </strong>) " +
                    "não pode ser removido."
            );
        }

        if (targetUser.equalsIgnoreCase(loggedUser)) {
            throw new BusinessException("Você não pode excluir o seu <strong>próprio perfil</strong>.");
        }
    }

    // MÉTODOS UTILITÁRIOS PRIVADOS
    private void validateEmailAndUsername(String email, String userName, Integer currentUserId) {
        // Busca se existe outro usuário com este email
        usuarioRepository.findByEmail(email.trim()).ifPresent(user -> {
            if (!user.getId().equals(currentUserId)) {
                throw new BusinessException("Este E-mail (<strong>" + email + "</strong>) já está em uso.");
            }
        });

        // Busca se existe outro usuário com este login
        usuarioRepository.findByUserName(userName.trim()).ifPresent(user -> {
            if (!user.getId().equals(currentUserId)) {
                throw new BusinessException("Este login (<strong>" + userName + "</strong>) já está em uso.");
            }
        });
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        return random.ints(length, 0, chars.length())
                .mapToObj(chars::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    // Méthod utilitário para pegar o login de quem fez a requisição
    private String getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")
        ) {

            // O getName() retorna o "subject" do token, que no nosso caso é o username (login)
            return authentication.getName();
        }
        return null;
    }

    // Methodo utilitário que impede outro usuário
    // Adminsitrador alterar os dados do Administrador Geral
    private void onlyAdminMakeChange(Usuario existingUsuario) {
        String loggedUser = getLoggedUser();
        String targetUser = existingUsuario.getUsername().trim();

        if ("pgmnet".equalsIgnoreCase(targetUser)) {
            if (!"pgmnet".equalsIgnoreCase(loggedUser)) {
                throw new BusinessException("<strong>ACESSO NEGADO</strong>: Apenas o próprio " +
                        "Administrador Geral pode alterar os seus dados.");
            }
        }
    }
}
