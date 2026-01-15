package app.omniOne.chatting.model;

import app.omniOne.chatting.model.dto.ChatMessageDto;
import app.omniOne.chatting.model.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    @Mapping(target = "messageId", source = "id")
    @Mapping(target = "senderId", source = "sender.id")
    ChatMessageDto map(ChatMessage message);

}
