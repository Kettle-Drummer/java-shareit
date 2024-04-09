package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.EntityNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemDto add(ItemDto itemDto, Long id) {
        if (id != null) {
            User user = userRepository.getById(id);
            Item item = itemRepository.create(ItemMapper.toItem(itemDto, user));
            log.info("Добавлен новый лот: {} пользователем id:{}", itemDto, id);
            return ItemMapper.toItemDto(item);
        } else {
            throw new EntityNotFoundException("не передан id");
        }
    }

    public ItemDto update(ItemDto itemDto, Long id, Long itemId) {
        Item item = itemRepository.getById(itemId);
        if (Objects.equals(item.getOwner().getId(), id)) {
            if (itemDto.getName() != null) {
                item.setName(itemDto.getName());
            }
            if (itemDto.getDescription() != null) {
                item.setDescription(itemDto.getDescription());
            }
            if (itemDto.getAvailable() != null) {
                item.setAvailable(itemDto.getAvailable());
            }
            log.info("Обновлен лот: {} пользователем id:{}", itemDto, id);
            return ItemMapper.toItemDto(itemRepository.update(item));
        } else {
            throw new EntityNotFoundException("Не совпадает id владельца вещи");
        }
    }

    public ItemDto getById(Long itemId) {
        return ItemMapper.toItemDto(itemRepository.getById(itemId));
    }


    public Collection<ItemDto> getByUser(Long id) {
        return itemRepository.getByUser(id);
    }


    public List<ItemDto> getBySearch(String textQuery) {
        if (textQuery == null || textQuery.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.getBySearch(textQuery.toLowerCase());
    }
}
