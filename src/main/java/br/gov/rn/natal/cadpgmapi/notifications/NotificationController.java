package br.gov.rn.natal.cadpgmapi.notifications;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notificações", description = "API de gestão de notificações para o Status igual a 'Pendente'")
public class NotificationController {
    private final SseNotificationService sseService;

    public NotificationController(SseNotificationService sseService) {
        this.sseService = sseService;
    }

    // O MediaType.TEXT_EVENT_STREAM_VALUE é a chave que diz ao
    // navegador para não fechar a conexão!
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return sseService.subscribe();
    }
}
