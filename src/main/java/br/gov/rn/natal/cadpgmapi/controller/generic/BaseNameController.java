package br.gov.rn.natal.cadpgmapi.controller.generic;

import br.gov.rn.natal.cadpgmapi.service.generic.BaseNameGenericService;
import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller genérico para entidades que possuem o atributo "nome" e precisam de filtro de busca.
 */
public abstract class BaseNameController<E, Req, Res, ID> extends BaseController<E, Req, Res, ID> {
    // Guardamos uma referência específica do serviço de nomes para acessar o findByFilterName
    protected final BaseNameGenericService<E, Req, Res, ID> nameService;

    public BaseNameController(BaseNameGenericService<E, Req, Res, ID> service) {
        super(service); // Passa o serviço para o pai montar os CRUDs básicos
        this.nameService = service;
    }

    @GetMapping("/searchFilter")
    @Operation(summary = "Buscar registros filtrando por nome de forma paginada")
    public Page<Res> findByNome(
            @RequestParam(required = false) String nome,
            @ParameterObject Pageable pageable) {

        // Retorna direto a página limpa (sem ResponseEntity, como padronizamos!)
        return nameService.findByFilterName(nome, pageable);
    }
}
