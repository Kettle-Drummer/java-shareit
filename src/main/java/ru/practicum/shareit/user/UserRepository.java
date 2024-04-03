package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.error.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
public class UserRepository {
    private final Map<Long, User> users = new HashMap<>();

    public User add(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public List<UserDto> getAll() {
        ArrayList<UserDto> list = new ArrayList<>();
        if (users.isEmpty()) {
            return list;
        }
        for (User user : users.values()) {
            list.add(UserMapper.toUserDto(user));
        }
        return list;
    }

    public User getById(Long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        }
        throw new EntityNotFoundException("Нет пользователя с таким id");
    }

    public void update(User user) {
        users.put(user.getId(), user);
    }

    public Long delete(Long id) {
        if (users.containsKey(id)) {
            users.remove(id);
            return id;
        }
        throw new EntityNotFoundException(String.format("Удаление невозможно %s не сущесвует", id));
    }

    public boolean matchingEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> email.equals(user.getEmail()));
    }
}


