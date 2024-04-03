package ru.practicum.shareit.user.model;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String name;

}
