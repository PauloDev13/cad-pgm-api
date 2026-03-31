package br.gov.rn.natal.cadpgmapi.repository.generic;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

// Repositório usado ÚNICA E EXCLUSIVAMENTE para as entidades que têem o atributo NOME
@NoRepositoryBean // Fundamental para não dar erro no Spring!
public interface BaseNameRepository<T, ID> extends JpaRepository<T, ID> {

    // O Spring agora sabe que quem herdar essa interface obrigatoriamente terá esse méthod
    Page<T> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    boolean existsByNome(String nome);
}
