package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto add(UserDto userDto);

    List<UserDto> getAll();

    UserDto getById(Long id);

    UserDto update(UserDto userDto, Long id);

    Long delete(Long id);

}
