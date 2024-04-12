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
        User user = UserMapper.toUser(userDto);
        user = userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getById(Long id) {
        return UserMapper.toUserDto(userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Нет пользователя c id:" + id)));
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User oldUser = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Нет пользователя c id:" + id));
        if (userDto.getName() != null) {
            oldUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            oldUser.setEmail(userDto.getEmail());
        }
        log.info("Обновление пользователя: {}", oldUser);
        User save = userRepository.save(oldUser);
        return UserMapper.toUserDto(save);
    }

    @Override
    public Long delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Нет пользователя c id:" + id));
        userRepository.deleteById(id);
        log.info("Удаление пользователя c id: {}", id);
        return id;
    }
}


