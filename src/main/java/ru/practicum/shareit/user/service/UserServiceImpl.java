package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.ValidationException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private Long idGen = 1L;
    private final UserRepository userRepository;

    private Long idGen() {
        return idGen++;
    }

    @Override
    public UserDto add(UserDto userDto) {
        if (userRepository.matchingEmail(userDto.getEmail())) {
            throw new ValidationException("Этот Email занят");
        } else {
            userDto.setId(idGen());
            log.info("Добавление нового пользователя: {}", userDto);
            return UserMapper.toUserDto(userRepository.add(UserMapper.toUser(userDto)));
        }
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.getAll();
    }

    @Override
    public UserDto getById(Long id) {
        return UserMapper.toUserDto(userRepository.getById(id));
    }

    @Override
    public UserDto update(UserDto userDto, Long id) {
        User oldUser = userRepository.getById(id);
        if (userDto.getName() != null) {
            oldUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (!userDto.getEmail().equals(oldUser.getEmail())) {
                if (userRepository.matchingEmail(userDto.getEmail())) {
                    throw new ValidationException("Этот Email занят");
                } else {
                    oldUser.setEmail(userDto.getEmail());
                }
            }
        }
        userRepository.update(oldUser);
        log.info("Обновление пользователя: {}", oldUser);
        return UserMapper.toUserDto(oldUser);
    }

    @Override
    public Long delete(Long id) {
        User user = userRepository.getById(id);
        userRepository.delete(id);
        log.info("Удаление пользователя c id:" + id);
        return id;
    }
}


