package br.gov.rn.natal.cadpgmapi.service;

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
    public UsuarioRegisterResponseDTO registerNewUserPublic(UsuarioRegisterRequestDTO dto) {
        // Valida se há duplicidade de E-mail
        if (usuarioRepository.existsByEmail(dto.email().trim())) {
            throw new BusinessException("Já existe cadastro com este e-mail " +
                    "(<strong>" + dto.email() + "</strong>).");
        }
        // UserName (Login) único
        if (usuarioRepository.existsByUserName(dto.userName().trim())) {
            throw new BusinessException("Já existe cadastro com este Login " +
                    "(<strong>" + dto.userName() + "</strong>) já está em uso.");
        }

        // Instancia o usuário apenas com os dados seguros
        Usuario newUser = registerUserMapper.toEntity(dto);

        // Chama o método beforeSave para criptografar a senha e
        // inserr a permissão padrão Guest ao usuário
        beforeSave(newUser);

        // Salva no banco
        return registerUserMapper.toDto(usuarioRepository.save(newUser));
    }

    @Transactional
    public UsuarioResponseDTO updateProfile(Integer id, UsuarioUpdateDTO dto) {

        // Busca o usuário atual no banco
        Usuario existingUsuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        // Valida duplicidade de E-mail (se ele estiver tentando mudar)
        if (!existingUsuario.getEmail().equalsIgnoreCase(dto.email().trim())) {
            if (usuarioRepository.existsByEmail(dto.email().trim())) {
                throw new BusinessException("Este E-mail (<strong>" + dto.email() + "</strong>) já está em uso.");
            }
        }

        // Valida duplicidade de UserName (se ele estiver tentando mudar)
        if (!existingUsuario.getUsername().equalsIgnoreCase(dto.userName().trim())) {
            if (usuarioRepository.existsByUserName(dto.userName().trim())) {
                throw new BusinessException("Este login (<strong>" + dto.userName() + "<strong>) já está em uso.");
            }
        }

        // Chama o método utilitário para checar se é o Adminsitrador Geral
        // que está logado. Se sim, deixa altetar os dados. Se não, bloqueia.
        onlyAdminMakeChange(existingUsuario);

        // Aplica os novos valores
        usuarioUpdateMapper.updateEntityFromDTO(existingUsuario, dto);

        // Salva e retorna o DTO atualizado
        return mapper.toDto(usuarioRepository.save(existingUsuario));
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

    public AdminResetPasswordResponseDTO resetPasswordByAdmin(Integer id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        String temporaryPassword = generateRandomPassword(8);

        usuario.setPassword(passwordEncoder.encode(temporaryPassword));
        usuario.setForcePasswordChange(true);
        repository.save(usuario);

        return new AdminResetPasswordResponseDTO(temporaryPassword);
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        return random.ints(length, 0, chars.length())
                .mapToObj(chars::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }


    // SÓ REGRA DE NEGÓCIO, ZERO CÓDIGO DE INFRAESTRUTURA
    @Override
    protected void beforeCreate(UsuarioRequestDTO dto) {
        // E-mail único
        if (usuarioRepository.existsByEmail(dto.email().trim())) {
            throw new BusinessException("Já existe cadastro com este e-mail " +
                    "(<strong>" + dto.email() + "</strong>).");
        }
        // UserName (Login) único
        if (usuarioRepository.existsByUserName(dto.userName().trim())) {
            throw new BusinessException("Já existe cadastro com este Login " +
                    "(<strong>" + dto.userName() + "</strong>) já está em uso.");
        }

    }

    // Método com assinatura original declarada na classe pai (BasicGenericService)
    @Override
    protected void beforeUpdate(UsuarioRequestDTO dto, Usuario existingUsuario) {

        // Só valida duplicidade se o usuário estiver de fato tentando MUDAR o e-mail
        if (!existingUsuario.getEmail().equalsIgnoreCase(dto.email().trim())) {
            if (usuarioRepository.existsByEmail(dto.email())) {
                throw new BusinessException("Este E-mail (<strong>" + dto.email() + "</strong>) já está em uso.");
            }
        }

        // Só valida duplicidade se tentar MUDAR o userName
        if (!existingUsuario.getUsername().equalsIgnoreCase(dto.userName().trim())) {
            if (usuarioRepository.existsByUserName(dto.userName().trim())) {
                throw new BusinessException("Este login (<strong>" + dto.userName() + "<strong>) já está em uso.");
            }
        }
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
        if ("procurador.geral".trim().equalsIgnoreCase(targetUser)) {
            throw new BusinessException("O (<strong> Adminsitrador Geral </strong>) " +
                    "não pode ser removido."
            );
        }

        if (targetUser.equalsIgnoreCase(loggedUser)) {
            throw new BusinessException("Você não pode excluir o seu <strong>próprio perfil</strong>.");
        }
    }

    // MÉTODOS UTILITÁRIOS PRIVADOS
    // Método utilitário para pegar o login de quem fez a requisição
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

    // Método utilitário que impede outro usuário
    // Adminsitrador alterar os dados do Administrador Geral
    private void onlyAdminMakeChange(Usuario existingUsuario) {
        String loggedUser = getLoggedUser();
        String targetUser = existingUsuario.getUsername().trim();

        if ("procurador.geral".equalsIgnoreCase(targetUser)) {
            if (!"procurador.geral".equalsIgnoreCase(loggedUser)) {
                throw new BusinessException("<strong>ACESSO NEGADO</strong>: Apenas o próprio " +
                        "Administrador Geral pode alterar os seus dados.");
            }
        }
    }
}
