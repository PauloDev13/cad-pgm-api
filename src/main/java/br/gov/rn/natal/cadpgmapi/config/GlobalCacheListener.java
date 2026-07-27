package br.gov.rn.natal.cadpgmapi.config;

import br.gov.rn.natal.cadpgmapi.entity.*;
import br.gov.rn.natal.cadpgmapi.notifications.SseNotificationService;
import br.gov.rn.natal.cadpgmapi.utils.EntityChangeEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class GlobalCacheListener {
    private final CacheManager cacheManager;
    private final SseNotificationService sseService;

    // Construtor
    public GlobalCacheListener(CacheManager cacheManager, SseNotificationService sseService) {
        this.cacheManager = cacheManager;
        this.sseService = sseService;
    }

    // Fica escutando os eventos silenciosamente
    @EventListener
    public void onEntityChange(EntityChangeEvent event) {
        Object entity = event.getEntity();
        // Se a entidade que mudou foi um Servidor, limpamos o cache do Dashboard!
        if (entity instanceof Servidor) {
            clearCache("dashboardResumoCache");
            clearCache("servidoresCache");
        }
        else if (entity instanceof Alias) {
            clearCache("aliasesCache");
        }
        else if (entity instanceof Cargo) {
            clearCache("cargosCache");
        }
        else if (entity instanceof Setor) {
            clearCache("setoresCache");
        }
        else if (entity instanceof Sistema) {
            clearCache("sistemasCache");
        }
        else if (entity instanceof Status) {
            clearCache("statusCache");
        }
        else if (entity instanceof Vinculo) {
            clearCache("vinculosCache");
        }
    }

    @EventListener
    public void handleEntityChange(EntityChangeEvent event) {
        // Se a entidade que disparou o evento for um Servidor
        if (event.getEntity() instanceof Servidor servidor) {

            // Verifica se o status do servidor que acabou de ser salvo é "Pendente"
            if (servidor.getStatus() != null) {
                // Dispara o alerta para todo mundo que estiver com o sistema aberto!
                sseService.notifyPendentesUpdate();
            }
        }
    }

    private void clearCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
