package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;
import java.util.List;

public interface ItemService {
    ItemDto add(ItemDto itemDto, Long id);

    ItemDto update(ItemDto itemDto, Long id, Long itemId);

    ItemDto getById(Long itemId);

    Collection<ItemDto> getByUser(Long id);

    List<ItemDto> getBySearch(String textQuery);
}
