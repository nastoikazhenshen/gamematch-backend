package kz.gamematch.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendChatMessageDto {

    @NotNull
    private Long senderId;

    @NotBlank
    @Size(max = 2000)
    private String content;
}
