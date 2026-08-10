package br.gov.rn.natal.cadpgmapi.config;

import br.gov.rn.natal.cadpgmapi.entity.Alias;
import br.gov.rn.natal.cadpgmapi.entity.Cargo;
import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.entity.Setor;
import br.gov.rn.natal.cadpgmapi.entity.Sistema;
import br.gov.rn.natal.cadpgmapi.entity.Status;
import br.gov.rn.natal.cadpgmapi.entity.Vinculo;
import br.gov.rn.natal.cadpgmapi.notifications.SseNotificationService;
import br.gov.rn.natal.cadpgmapi.utils.EntityChangeEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        // Guarda o usuário da requisição (ou "sistema" quando não há sessão)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedUser = (auth != null) ? auth.getName() : "sistema";

        // Se a entidade que disparou o evento for um Servidor
        if (event.getEntity() instanceof Servidor servidor) {
            // DEFICIÊNCIA CORRIGIDA (bloqueio de alerta): antes o "pendentes-update" era emitido
            // para QUALQUER servidor salvo (Criação, Atualização) — qualquer movimento gerava o
            // alerta. Agora só emitimos quando o status é o Pendente, que é o gatilho correto.
            if (servidor.getStatus() != null
                    && Status.STATUS_PENDENTE.equalsIgnoreCase(servidor.getStatus().getDescricao())) {
                sseService.notifyPendentesUpdate();
            }
        }

        // Avisa que a tabela principal está desatualizada!
        sseService.notifyServidoresChanged(loggedUser);
    }

    private void clearCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
