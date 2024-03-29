package site.bidderown.server.boundedcontext.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import site.bidderown.server.boundedcontext.chat.controller.dto.ChatMessageRequest;
import site.bidderown.server.boundedcontext.chat.controller.dto.ChatNotificationRequest;
import site.bidderown.server.boundedcontext.chat.controller.dto.ChatResponse;
import site.bidderown.server.boundedcontext.chat.service.ChatService;


@RequiredArgsConstructor
@Controller
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @Value("${custom.socket.path}")
    private String socketPath;

    @Value("${custom.socket.alarm_type_chat}")
    private String ALARM_TYPE;

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageRequest chatMessageRequest) {
        ChatResponse chatResponse = chatService.create(chatMessageRequest);
        messagingTemplate.convertAndSend("/sub/chat-room/" + chatResponse.getRoomId(), chatResponse);
    }

    @MessageMapping("/chat/notification")
    public void chatNotification(ChatNotificationRequest chatNotificationRequest) {
        messagingTemplate.convertAndSend(socketPath + chatNotificationRequest.getToUserId(), ALARM_TYPE);
    }
}
