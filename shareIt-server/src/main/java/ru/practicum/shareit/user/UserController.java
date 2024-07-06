package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userServiceImpl;

    @PostMapping
    public UserDto add(@Valid @RequestBody UserDto user) {
        log.info("Add user{}", user);
        return userServiceImpl.add(user);
    }

    @GetMapping
    public List<UserDto> getAll() {  //    получение списка всех пользователей.
        return userServiceImpl.getAll();
    }

    @GetMapping("{id}")
    public UserDto getById(@PathVariable Long id) {
        log.info("Get user id{}", id);
        return userServiceImpl.getById(id);
    }


    @PatchMapping("{id}")
    public UserDto update(@RequestBody UserDto user, @PathVariable Long id) {
        log.info("Update user{}", user);
        return userServiceImpl.update(id, user);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id) {
        log.info("Delete user id{}", id);
        userServiceImpl.delete(id);
    }
}