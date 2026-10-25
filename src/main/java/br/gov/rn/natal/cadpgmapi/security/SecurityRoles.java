package br.gov.rn.natal.cadpgmapi.security;

public final class SecurityRoles {

    private SecurityRoles() {
        // Classe utilitária - não instanciável
    }
    /** Permite a abertura de chamados no módulo de manutenção */
    public static final String MANUTENCAO = "manutencao";
    /** Identifica servidores que fazem parte da equipe operacional do almoxarifado */
    public static final String ALMOXARIFADO = "almoxarifado";
    /** Identifica usuários com competência para tramitar, iniciar, concluir e cancelar chamados */
    public static final String TRAMITADOR = "tramitador";
    /** Administrador geral do sistema */
    public static final String ADMIN = "admin";
}
