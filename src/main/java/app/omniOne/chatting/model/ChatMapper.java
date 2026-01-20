package app.omniOne.chatting.model;

import app.omniOne.chatting.model.dto.ChatConversationDto;
import app.omniOne.chatting.model.dto.ChatMessageDto;
import app.omniOne.chatting.model.entity.ChatConversation;
import app.omniOne.chatting.model.entity.ChatMessage;
import app.omniOne.model.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "messageId", source = "id")
    @Mapping(target = "senderId", source = "sender.id")
    ChatMessageDto map(ChatMessage message);

    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "lastReadAt", ignore = true)
    @Mapping(target = "otherUserId", source = "profile.id")
    @Mapping(target = "otherFirstName", source = "profile.firstName")
    @Mapping(target = "otherLastName", source = "profile.lastName")
    ChatConversationDto map(ChatConversation conversation, UserProfile profile);

}
