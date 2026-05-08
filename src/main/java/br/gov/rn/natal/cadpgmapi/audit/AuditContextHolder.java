package br.gov.rn.natal.cadpgmapi.audit;

public class AuditContextHolder {

    // Cria um "cofre" seguro na memória exclusivo para a requisição atual do usuário
    private static final ThreadLocal<String> logDetalhesHolder = new ThreadLocal<>();
    // Gaveta para guardar o estado anterior (snapshot) da entidade
    private static final ThreadLocal<Object> oldSnapshotHolder = new ThreadLocal<>();
    // Guarda o ID amigável (CPF, E-mail, etc.)
    private static final ThreadLocal<String> friendlyIdHolder = new ThreadLocal<>();
    // Gaveta para o nome da entidade
    private static final ThreadLocal<String> entityNameHolder = new ThreadLocal<>();

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

    public static void setFriendlyId(String friendlyId) { friendlyIdHolder.set(friendlyId); }
    public static String getFriendlyId() { return friendlyIdHolder.get(); }

    public static void setEntityName(String name) { entityNameHolder.set(name); }
    public static String getEntityName() { return entityNameHolder.get(); }

    // Limpa o cofre para não vazar dados nem gerar vazamento de memória (Memory Leak)
    public static void clear() {
        // IMPORTANTE: Sempre limpar as gavetas!
        logDetalhesHolder.remove();
        oldSnapshotHolder.remove();
        friendlyIdHolder.remove();
        entityNameHolder.remove();
    }
}
