package com.example.skillswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProfileCommentDto {

    @NotBlank(message = "{profile.comment.validation.required}")
    @Size(max = 200, message = "{profile.comment.validation.max}")
    private String content;
}
