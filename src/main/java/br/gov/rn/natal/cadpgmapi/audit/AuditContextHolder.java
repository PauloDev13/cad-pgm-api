package br.gov.rn.natal.cadpgmapi.audit;

public class AuditContextHolder {

    // Cria um "cofre" seguro na memória exclusivo para a requisição atual do usuário
    private static final ThreadLocal<String> logDetalhesHolder = new ThreadLocal<>();

    public static void setLogDetalhes(String detalhes) {
        logDetalhesHolder.set(detalhes);
    }

    public static String getLogDetalhes() {
        return logDetalhesHolder.get();
    }

    // Limpa o cofre para não vazar dados para a próxima requisição
    public static void clear() {
        logDetalhesHolder.remove();
    }
}
