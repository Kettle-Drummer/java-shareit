package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto add(UserDto userDto) {
        log.info("Добавление нового пользователя: {}", userDto);
        User user = UserMapper.INSTANCE.toUser(userDto);
        user = userRepository.save(user);
        return UserMapper.INSTANCE.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper.INSTANCE::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getById(Long id) {
        return UserMapper.INSTANCE.toUserDto(userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Нет пользователя c id:" + id)));
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User oldUser = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Нет пользователя c id:" + id));
        log.info("Обновление пользователя: {}", oldUser);

        User save = UserMapper.INSTANCE.updateUserByGivenDto(userDto, oldUser);
        return UserMapper.INSTANCE.toUserDto(userRepository.save(save));
    }

    @Override
    public void delete(Long id) {
        log.info("Удаление пользователя c id: {}", id);
        userRepository.deleteById(id);
    }
}


