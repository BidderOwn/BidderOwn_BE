package site.bidderown.server.bounded_context.chat.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.bidderown.server.bounded_context.chat.controller.dto.ChatRequest;
import site.bidderown.server.bounded_context.chat.controller.dto.ChatResponse;
import site.bidderown.server.bounded_context.chat.entity.Chat;
import site.bidderown.server.bounded_context.chat.repository.ChatRepository;
import site.bidderown.server.bounded_context.chat_room.entity.ChatRoom;
import site.bidderown.server.bounded_context.chat_room.service.ChatRoomService;
import site.bidderown.server.bounded_context.member.entity.Member;
import site.bidderown.server.bounded_context.member.repository.MemberRepository;
import site.bidderown.server.bounded_context.member.service.MemberService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatRoomService chatRoomService;
    private final MemberService memberService;
    public ChatResponse save(ChatRequest chatRequest, User user) {
        ChatRoom chatRoom = chatRoomService.findById(chatRequest.getRoomId());
        Member member = memberService.findByName(user.getUsername());

        Chat chat = chatRepository.save(Chat.of(chatRequest.getMessage(), member, chatRoom));
        return ChatResponse.of(chat);
    }
}
