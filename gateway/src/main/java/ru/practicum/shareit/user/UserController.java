package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public UserDto add(@Valid @RequestBody UserDto userDto) {
        return userClient.add(userDto);
    }

    @GetMapping
    public List<UserDto> getAll() {  //    получение списка всех пользователей.
        return userClient.getAll();
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        return userClient.getById(id);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @NotNull @RequestBody UserDto userDto) {
        return userClient.update(id, userDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userClient.delete(id);
    }


}