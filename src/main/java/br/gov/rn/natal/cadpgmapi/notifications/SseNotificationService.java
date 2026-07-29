package br.gov.rn.natal.cadpgmapi.notifications;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseNotificationService {
    // Lista thread-safe para guardar todos os navegadores conectados
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // Método que o Angular vai chamar para "sintonizar" na rádio
    public SseEmitter subscribe() {
        // 0L significa que a conexão não tem timeout no lado do servidor
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        // Se o usuário fechar a aba do Angular, removemos ele da lista
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }

    // Métodos que envia o "Bip" para todos os conectados
    public void notifyPendentesUpdate() {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                // Envia um evento com o nome "pendentes-update" e qualquer payload
                emitter.send(SseEmitter.event()
                        .name("pendentes-update")
                        .data("UPDATE"));
            } catch (IOException e) {
                deadEmitters.add(emitter); // Se falhar (ex: rede caiu), marca para remover
            }
        });

        emitters.removeAll(deadEmitters);
    }

    public void notifyServidoresChanged(String loggedUser) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                // Mandamos um evento com nome diferente!
                emitter.send(SseEmitter.event()
                        .name("servidores-changed")
                        .data(loggedUser));
            } catch (IOException e) {
                deadEmitters.add(emitter); // Se falhar (ex: rede caiu), marca para remover
            }
        });

        emitters.removeAll(deadEmitters);
    }


}
