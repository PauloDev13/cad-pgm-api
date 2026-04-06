package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Page<Usuario> findByUserNameContainingIgnoreCase(String userName, Pageable pageable);
    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
}
