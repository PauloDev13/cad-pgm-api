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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
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
//        Usuario newUser = new Usuario();
//        newUser.setName(dto.name().trim());
//        newUser.setUserName(dto.userName().trim());
//        newUser.setEmail(dto.email().trim());

        // Criptografa a senha e insere a permissão padrão Guest ao usuário
        newUser.setPassword(passwordEncoder.encode(dto.password().trim()));
        newUser.setPermissions(Set.of("guest"));

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
        usuario.setPassword(temporaryPassword);
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
        // Criptografa a senha do novo usuário na hora do cadastro!
        String senhaCriptografada = passwordEncoder.encode(entity.getPassword());
        entity.setPassword(senhaCriptografada);

        // Permissão guest adicionada
        entity.setPermissions(Set.of("guest"));
    }
}
