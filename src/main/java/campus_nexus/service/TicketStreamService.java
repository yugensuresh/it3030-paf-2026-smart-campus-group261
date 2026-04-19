package campus_nexus.service;

import campus_nexus.dto.response.TicketActivityNotificationDTO;
import campus_nexus.dto.response.TicketResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-Sent Events hub so ticket owners (and admins) see ticket updates without polling.
 */
@Service
public class TicketStreamService {

    private static final Logger logger = LoggerFactory.getLogger(TicketStreamService.class);

    private final List<SseEmitter> adminEmitters = new CopyOnWriteArrayList<>();
    private final Map<Long, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribeAdmin() {
        SseEmitter emitter = new SseEmitter(0L);
        adminEmitters.add(emitter);
        registerAdminCleanup(emitter);
        return emitter;
    }

    public SseEmitter subscribeUser(Long userId) {
        SseEmitter emitter = new SseEmitter(0L);
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        registerUserCleanup(emitter, userId);
        return emitter;
    }

    public void publishTicketUpdate(TicketResponseDTO dto) {
        if (dto == null || dto.getUserId() == null) {
            return;
        }
        sendTicketUpdateToEmitters(adminEmitters, dto);
        List<SseEmitter> forUser = userEmitters.get(dto.getUserId());
        if (forUser != null) {
            sendTicketUpdateToEmitters(forUser, dto);
        }
    }

    /**
     * Real-time technician activity (status change, notes) for ticket owner and admin viewers.
     */
    public void publishTicketActivity(TicketActivityNotificationDTO activity) {
        if (activity == null || activity.getTicketOwnerUserId() == null) {
            return;
        }
        sendActivityToEmitters(adminEmitters, activity);
        List<SseEmitter> forUser = userEmitters.get(activity.getTicketOwnerUserId());
        if (forUser != null) {
            sendActivityToEmitters(forUser, activity);
        }
    }

    private void sendTicketUpdateToEmitters(List<SseEmitter> emitters, TicketResponseDTO dto) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ticket-update")
                        .data(dto, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                logger.debug("Removing SSE client after send failure: {}", e.getMessage());
                emitter.complete();
            }
        }
    }

    private void sendActivityToEmitters(List<SseEmitter> emitters, TicketActivityNotificationDTO activity) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ticket-activity")
                        .data(activity, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                logger.debug("Removing SSE client after activity send failure: {}", e.getMessage());
                emitter.complete();
            }
        }
    }

    private void registerAdminCleanup(SseEmitter emitter) {
        Runnable remove = () -> adminEmitters.remove(emitter);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(e -> remove.run());
    }

    private void registerUserCleanup(SseEmitter emitter, Long userId) {
        Runnable remove = () -> {
            List<SseEmitter> list = userEmitters.get(userId);
            if (list != null) {
                list.remove(emitter);
                if (list.isEmpty()) {
                    userEmitters.remove(userId, list);
                }
            }
        };
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(e -> remove.run());
    }
}
