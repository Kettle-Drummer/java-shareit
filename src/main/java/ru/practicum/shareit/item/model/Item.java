package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
@Builder
public class Item {

    private Long id;
    @NotBlank
    private String name;
    @NotNull
    private User owner;
    @NotBlank
    private String description;
    @NotNull
    private Boolean available;

    private Integer request;

    public Item(Long id, String name, User owner, String description, Boolean available, Integer request) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.description = description;
        this.available = available;
        this.request = request;
    }
}
