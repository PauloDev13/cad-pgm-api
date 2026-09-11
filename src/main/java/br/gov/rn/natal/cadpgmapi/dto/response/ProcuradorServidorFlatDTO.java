package br.gov.rn.natal.cadpgmapi.dto.response;

/**
 * Registro plano para projeção de alto desempenho via JPQL Constructor Expression.
 */
public record ProcuradorServidorFlatDTO(
        String nomeProcurador,
        String nomeServidor,
        String nomeSetor
) {
}
