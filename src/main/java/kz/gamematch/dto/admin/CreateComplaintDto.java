package kz.gamematch.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateComplaintDto {

    @NotNull
    private Long reporterId;

    @NotNull
    private Long reportedUserId;

    @NotBlank
    @Size(max = 2000)
    private String reason;
}
