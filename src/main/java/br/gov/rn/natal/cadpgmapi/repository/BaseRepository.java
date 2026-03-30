package br.gov.rn.natal.cadpgmapi.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean // Fundamental para não dar erro no Spring!
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    // O Spring agora sabe que quem herdar essa interface obrigatoriamente terá esse méthod
    Page<T> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
