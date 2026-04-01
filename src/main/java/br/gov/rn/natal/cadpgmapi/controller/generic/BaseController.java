package br.gov.rn.natal.cadpgmapi.controller.generic;

import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

public abstract class BaseController<E, Req, Res, ID> {

    protected final BaseGenericService<E, Req, Res, ID> service;

    // Construtor
    protected BaseController(BaseGenericService<E, Req, Res, ID> service) {
        this.service = service;
    }

    // MÉTHOD ABSTRATO: As classes filhas vão implementar isso com apenas 1 linha!
    protected abstract ID getIdFromDto(Res dto);

    // CRIAMOS O MÉTHOD QUE DEFINE A ORDENAÇÃO PADRÃO
    // Ele retorna "id" por padrão, mas as filhas podem sobrescrever!
    protected String getDefaultSortProperty() {
        return "id";
    }

    @PostMapping
    public ResponseEntity<Res> create(@Valid @RequestBody Req dto) {
        // Cria o recurso usando o Service
        Res createdDto = service.create(dto);

        // Monta a URL dinâmica pegando a requisição atual (ex: http://localhost:8080/api/v1/aliases)
        // e adicionando o /{id} no final. O méthod getIdFromDto extrai o ID correto!
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(getIdFromDto(createdDto))
                .toUri();

        // Retorna 201 Created com o Header 'Location' preenchido e o body com o DTO
        return ResponseEntity.created(location).body(createdDto);
    }

    @GetMapping
    public Page<Res> findAll(@ParameterObject Pageable rawPageable) {
        // 1. Extraímos a página e o tamanho de forma segura (com fallback para 0 e 20)
        int page = rawPageable.isPaged() ? rawPageable.getPageNumber() : 0;
        int size = rawPageable.isPaged() ? rawPageable.getPageSize() : 20;

        // 2. TRAVA DE SEGURANÇA ABSOLUTA
        if (size > 1000 || size < 1) {
            size = 20; // Força 20 se vier um número gigante ou negativo
        }
        if (page < 0) {
            page = 0;
        }

        // 3. Extraímos a ordenação e verificamos se é lixo do Swagger
        Sort sort = rawPageable.getSort();
        boolean hasGarbageSort = sort.isSorted() && sort.stream()
                .anyMatch(order -> order.getProperty().contains("string"));

        // 4. Definimos a ordenação final
        if (sort.isUnsorted() || hasGarbageSort) {
            sort = Sort.by(Sort.Direction.ASC, getDefaultSortProperty());
        }

        // 5. O SEGREDO: SEMPRE criamos um objeto PageRequest novo e blindado!
        // Nenhuma sujeira da requisição original passa daqui.
        Pageable safePageable = PageRequest.of(page, size, sort);

        return service.findAll(safePageable);
    }

    @GetMapping("/select")
    public List<Res> findAllSelect() {
        // Retorna 200 OK com a lista sem paginação para os componentes selects e autocomplete
        return service.findAllSelect();
    }

    @GetMapping("/{id}")
    public Res findById(@PathVariable ID id) {
        // Retorna 200 OK com o recurso encontrado (se não encontrar, o Service lança 404)
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public Res update(@PathVariable ID id, @Valid @RequestBody Req dto) {
        // Retorna 200 OK com o recurso atualizado
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        service.delete(id);
        // Retorna 204 No Content (padrão REST para deleção com sucesso sem corpo de resposta)
        return ResponseEntity.noContent().build();
    }
}
