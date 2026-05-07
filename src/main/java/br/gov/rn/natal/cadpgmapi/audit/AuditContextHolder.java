package br.gov.rn.natal.cadpgmapi.audit;

public class AuditContextHolder {

    // Cria um "cofre" seguro na memória exclusivo para a requisição atual do usuário
    private static final ThreadLocal<String> logDetalhesHolder = new ThreadLocal<>();

    // NOVO: Gaveta para guardar o estado anterior (snapshot) da entidade
    private static final ThreadLocal<Object> oldSnapshotHolder = new ThreadLocal<>();

    // --- Métodos para os Detalhes do Log ---
    public static void setLogDetalhes(String detalhes) {
        logDetalhesHolder.set(detalhes);
    }

    public static String getLogDetalhes() {
        return logDetalhesHolder.get();
    }

    public static void setOldSnapshot(Object snapshot) {
        oldSnapshotHolder.set(snapshot);
    }

    public static Object getOldSnapshot() {
        return oldSnapshotHolder.get();
    }

    // Limpa o cofre para não vazar dados nem gerar vazamento de memória (Memory Leak)
    public static void clear() {
        // IMPORTANTE: Sempre limpar as gavetas!
        logDetalhesHolder.remove();
        oldSnapshotHolder.remove();
    }
}
