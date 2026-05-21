package kz.gamematch.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateResponseDto {

    @NotNull
    private Long responderId;

    @NotBlank
    @Size(max = 1000)
    private String message;
}
