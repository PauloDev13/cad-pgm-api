package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.UsuarioRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.UsuarioResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.mapper.UsuarioMapper;
import br.gov.rn.natal.cadpgmapi.repository.UsuarioRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService extends BaseGenericService<Usuario, UsuarioRequestDTO, UsuarioResponseDTO, Integer> {
    private final UsuarioRepository usuarioRepository;
//    private final PasswordEncoder passwordEncoder;

    // Construtor
    public UsuarioService(UsuarioRepository repository, UsuarioMapper mapper) {
        super(repository, mapper);
        this.usuarioRepository = repository;
//        this.passwordEncoder = passwordEncoder;
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


    // SÓ REGRA DE NEGÓCIO, ZERO CÓDIGO DE INFRAESTRUTURA
    @Override
    protected void beforeCreate(UsuarioRequestDTO dto) {
        // E-mail único
        if (usuarioRepository.existsByEmail(dto.email().trim())) {
            throw new BusinessException("Já existe um Usuário cadastrado como este e-mail " + dto.email());
        }
        // UserName (Login) único
        if (usuarioRepository.existsByUserName(dto.userName().trim())) {
            throw new BusinessException("O login '" + dto.userName() + "' já está em uso por outro usuário.");
        }

    }

    @Override
    protected void beforeUpdate(UsuarioRequestDTO dto, Usuario existingUsuario) {
        // Só valida duplicidade se o usuário estiver de fato tentando MUDAR o e-mail
        if (!existingUsuario.getEmail().equalsIgnoreCase(dto.email().trim())) {
            if (usuarioRepository.existsByEmail(dto.email())) {
                throw new BusinessException("Este E-mail já está em uso por outro Usuário.");
            }
        }

        // Só valida duplicidade se tentar MUDAR o userName
        if (!existingUsuario.getUserName().equalsIgnoreCase(dto.userName().trim())) {
            if (usuarioRepository.existsByUserName(dto.userName().trim())) {
                throw new BusinessException("Este login já está em uso por outro Usuário.");
            }
        }
    }

    // Criptografa a senha
    @Override
    protected void beforeSave(Usuario entity) {
    // TODO: implementer o código abaixo
        // Pega a senha em texto puro (que o mapper colocou) e transforma no Hash BCrypt
        // String senhaCriptografada = passwordEncoder.encode(entity.getPassword());
        // entity.setPassword(senhaCriptografada);
    }
}
