package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import br.gov.rn.natal.cadpgmapi.audit.utils.AuditDiffUtil;
import br.gov.rn.natal.cadpgmapi.auth.dto.response.AdminResetPasswordResponseDTO;
import br.gov.rn.natal.cadpgmapi.auth.mappers.RegisterUserMapper;
import br.gov.rn.natal.cadpgmapi.dto.request.UsuarioRegisterRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.request.UsuarioRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ServidorComboDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.UsuarioRegisterResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.UsuarioResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.update.UsuarioUpdateDTO;
import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.mapper.UsuarioMapper;
import br.gov.rn.natal.cadpgmapi.mapper.UsuarioUpdateMapper;
import br.gov.rn.natal.cadpgmapi.repository.ServidorRepository;
import br.gov.rn.natal.cadpgmapi.repository.UsuarioRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.context.ApplicationEventPublisher;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UsuarioService extends BaseGenericService<Usuario, UsuarioRequestDTO, UsuarioResponseDTO, Integer> {
    // B5.1: o login do Administrador Geral estava repetido DUAS vezes como literal ("pgmnet"),
    // o que abria margem para inconsistência ao ser alterado. Agora vira uma constante única.
    public static final String ADMIN_USERNAME = "pgmnet";

    private final UsuarioRepository usuarioRepository;
    private final ServidorRepository servidorRepository;
    private final PasswordEncoder passwordEncoder;

    // Construtor
    public UsuarioService(
            UsuarioRepository repository,
            UsuarioMapper mapper,
            ServidorRepository servidorRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher
    ) {
        super(repository, mapper, eventPublisher);
        this.usuarioRepository = repository;
        this.servidorRepository = servidorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE, entity = "Usuário")
    public UsuarioResponseDTO updateProfile(Integer id, UsuarioUpdateDTO dto) {
        // Busca o usuário atual no banco
        Usuario existingUsuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        // TRAVA: Impede reativação se o servidor estiver com status 'excluído'
        if (existingUsuario.getServidor() != null && existingUsuario.getServidor().isExcluded() && dto.activated()) {
            throw new BusinessException(
                    "O servidor vinculado encontra-se com cadastro <strong>excluído/desligado</strong>. " +
                            "Não é permitido reativar o seu perfil de acesso."
            );
        }

        if (!existingUsuario.getUsername().equalsIgnoreCase(dto.userName().trim())) {
            usuarioRepository.findByUserName(dto.userName().trim()).ifPresent(u -> {
                if (!u.getId().equals(id)) {
                    throw new BusinessException("Este login (<strong>" + dto.userName() + "</strong>) já está em uso.");
                }
            });
        }

        // Só o próprio Administrador Geral pode alterar os dados dele
        onlyAdminMakeChange(existingUsuario);

        // 1. Tira o SNAPSHOT ANTIGO usando o mapper genérico do pai
        UsuarioResponseDTO oldSnapshot = mapper.toDto(existingUsuario);

        existingUsuario.setUserName(dto.userName().trim());
        existingUsuario.setActivated(dto.activated());
        existingUsuario.setPermissions(dto.permissions());
        existingUsuario.setForcePasswordChange(dto.forcePasswordChange());

        if (existingUsuario.isSystemAdmin()) {
            if (dto.systemName() != null && dto.systemEmail() != null) {
                existingUsuario.setSystemAccountData(dto.systemName().trim(), dto.systemEmail().trim());
            }
        }

        usuarioRepository.saveAndFlush(existingUsuario);
        entityManager.refresh(existingUsuario);

        // 3. Tira o SNAPSHOT NOVO
        UsuarioResponseDTO newSnapshot = mapper.toDto(existingUsuario);

        // 4. Gera o Diff e injeta no contexto
        String diffLog = AuditDiffUtil.generateDiff(oldSnapshot, newSnapshot);

        AuditContextHolder.setLogDetalhes(diffLog != null && !diffLog.isBlank()
                ? "ATUALIZAÇÃO: " + diffLog
                : "ATUALIZAÇÃO: Nenhuma alteração.");

        return newSnapshot;
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE, entity = "Usuário")
    public AdminResetPasswordResponseDTO resetPasswordByAdmin(Integer id) {
        Usuario existingUsuario = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        // TRAVA: Bloqueia geração de senha provisória para usuário cujo servidor foi desligado
        if (existingUsuario.getServidor() != null && existingUsuario.getServidor().isExcluded()) {
            throw new BusinessException(
                    "Não é permitido gerar senha temporária. O servidor vinculado a este usuário está " +
                            "<strong>excluído/desligado</strong>."
            );
        }

        // 1. Snapshot Antigo
        UsuarioResponseDTO oldSnapshot = mapper.toDto(existingUsuario);

        String temporaryPassword = generateRandomPassword(8);

        existingUsuario.setPassword(passwordEncoder.encode(temporaryPassword));
        existingUsuario.setForcePasswordChange(true);

        repository.saveAndFlush(existingUsuario);
        entityManager.refresh(existingUsuario);

        // 2. Snapshot Novo
        UsuarioResponseDTO newSnapshot = mapper.toDto(existingUsuario);

        // 3. Auditoria
        String diffLog = AuditDiffUtil.generateDiff(oldSnapshot, newSnapshot);
        AuditContextHolder.setLogDetalhes("ATUALIZAÇÃO DE SENHA: " + diffLog);

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

            Join<Usuario, Servidor> servidorJoin = root.join("servidor", JoinType.LEFT);

            // Se o Nome for informado, monta o SQL de busca por Nome
            if (name != null && !name.trim().isEmpty()) {
                String pattern = "%" + name.trim().toLowerCase() + "%";

                Predicate namePredicate = cb.or(
                        cb.like(cb.lower(servidorJoin.get("nome")), pattern),
                        cb.like(cb.lower(root.get("name")), pattern)
                );
                predicate = cb.and(predicate, namePredicate);
            }

            // Se o userName for informado, monta o SQL de busca por userName
            if (userName != null && !userName.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.like(
                        cb.lower(root.get("userName")), "%" + userName.trim().toLowerCase() + "%")
                );
            }

            if (email != null && !email.trim().isEmpty()) {
                String pattern = "%" + email.trim().toLowerCase() + "%";

                Predicate emailPredicate = cb.or(
                        cb.like(cb.lower(servidorJoin.get("emailInstitucional")), pattern),
                        cb.like(cb.lower(servidorJoin.get("emailPessoal")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)
                );
                predicate = cb.and(predicate, emailPredicate);
            }

            // Encerra a montagem das regras
            return predicate;
        };
        // BLOCO DE EXECUÇÃO: Vai no banco e converte para DTO
        // O retorno real que vai para o Controller
        return usuarioRepository.findAll(spec, pageable)
                .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<ServidorComboDTO> findServidoresDisponiveis() {
        return usuarioRepository.findServidoresSemUsuario();
    }

    // MÉTODOS EXCLUSIVOS DE REGRA DE NEGÓCIO

    @Override
    protected void beforeCreate(UsuarioRequestDTO dto) {
        if (ADMIN_USERNAME.equalsIgnoreCase(dto.userName())) {

            if (dto.servidorId() != null) {
                throw new BusinessException("A conta de Administrador Geral ('pgmnet') não pode ser vinculada a um servidor.");
            }

            if (dto.systemName() == null || dto.systemEmail() == null) {
                throw new BusinessException("Nome e E-mail institucionais são obrigatórios para a conta de sistema.");
            }
        } else {
            if (dto.servidorId() == null) {
                throw new BusinessException("Todo usuário do sistema deve estar vinculado a um Servidor da instituição.");
            }

            Servidor servidor = servidorRepository.findById(dto.servidorId())
                    .orElseThrow(() -> new BusinessException("Servidor informado não encontrado."));

            if (servidor.isExcluded()) {
                throw new BusinessException("Não é permitido criar usuário para servidor com cadastro excluído/desligado.");
            }

            if (usuarioRepository.existsByServidorId(dto.servidorId())) {
                throw new BusinessException("Este Servidor já possui uma conta de usuário cadastrada.");
            }
        }

        if (usuarioRepository.existsByUserName(dto.userName().trim())) {
            throw new BusinessException("Este login (<strong>" + dto.userName() + "</strong>) já está em uso.");
        }
    }

    @Override
    protected void beforeUpdate(UsuarioRequestDTO dto, Usuario entity) {
        onlyAdminMakeChange(entity);
        // TRAVA: Impede reativação se o servidor estiver com status 'excluído'
        if (entity.getServidor() != null && entity.getServidor().isExcluded() && dto.activated()) {
            throw new BusinessException(
                    "O servidor vinculado encontra-se com cadastro <strong>excluído/desligado</strong>. " +
                            "Não é permitido reativar o seu perfil de acesso."
            );
        }
    }

    // Criptografa a senha e injeta a permissão padrão
    @Override
    protected void beforeSave(Usuario entity) {
        // Pega a senha enviada
        String pwd = entity.getPassword();

        // Verifica se a senha existe
        if (pwd != null) {
            // CORREÇÃO DE BUG: o prefixo correto do BCrypt é '$2y$' (o '$3y$' não existe no padrão).
            // Antes, uma senha já hashada com "$2y$" não era reconhecida como hash e era
            // criptografada NOVAMENTE (dupla criptografia) em qualquer save que passasse por aqui,
            // quebrando o fluxo de login e redefinição de senha desse usuário.
            //
            // Um hash BCrypt válido SEMPRE tem 60 caracteres e começa com $2a$, $2b$ ou $2y$
            boolean isAlreadyHashed = pwd.length() == 60
                    && (pwd.startsWith("$2a$") || pwd.startsWith("$2b$") || pwd.startsWith("$2y$"));

            // Se NÃO for um hash, a senha é criptografada
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

    @Override
    protected void beforeDelete(Usuario entity) {
        if (entity == null) return;

        String loggedUser = getLoggedUser();
        String targetUser = entity.getUsername().trim();

        // Ninguém apaga o Administrador Geral
        if (ADMIN_USERNAME.equalsIgnoreCase(targetUser)) {
            throw new BusinessException("O (<strong>Administrador Geral</strong>) " +
                    "não pode ser removido."
            );
        }

        if (targetUser.equalsIgnoreCase(loggedUser)) {
            throw new BusinessException("Você não pode excluir o seu <strong>próprio perfil</strong>.");
        }

        // CORREÇÃO: Quebra o vínculo bidirecional em memória com o Servidor.
        // Isso impede que o Hibernate veja o Servidor apontando para o Usuário deletado no flush.
        if (entity.getServidor() != null) {
            entity.getServidor().setUsuario(null);
            entity.setServidor(null);
        }
    }


    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

        SecureRandom random = new SecureRandom();

        return random.ints(length, 0, chars.length())
                .mapToObj(chars::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    // Método utilitário para pegar o login de quem fez a requisição
    private String getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            // O getName() retorna o "subject" do token, que no nosso caso é o username (login)
            return authentication.getName();
        }
        return null;
    }

    // Método utilitário que impede outro usuário
    // de alterar os dados do Administrador Geral
    private void onlyAdminMakeChange(Usuario existingUsuario) {
        String loggedUser = getLoggedUser();
        String targetUser = existingUsuario.getUsername().trim();

        if (ADMIN_USERNAME.equalsIgnoreCase(targetUser)) {
            if (!ADMIN_USERNAME.equalsIgnoreCase(loggedUser)) {
                throw new BusinessException("<strong>ACESSO NEGADO</strong>: Apenas o próprio " +
                        "Administrador Geral pode alterar os seus dados.");
            }
        }
    }
}