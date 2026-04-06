package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.UsuarioRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.UsuarioResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.mapper.UsuarioMapper;
import br.gov.rn.natal.cadpgmapi.repository.UsuarioRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> findByFilterUserName(String filter, Pageable pageable) {
        if (filter == null || filter.trim().isEmpty()) {
            return super.findAll(pageable); // Reaproveita o méthod do BaseCrudService!
        }

        return usuarioRepository.findByUserNameContainingIgnoreCase(filter.trim(), pageable)
                .map(mapper::toDto); // Usamos o mapper genérico da classe pai
    }
}
