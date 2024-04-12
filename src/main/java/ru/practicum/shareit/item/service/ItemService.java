package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto add(ItemDto itemDto, Long id);

    ItemDto update(ItemDto itemDto, Long id, Long itemId);

    ItemDto getById(Long id, Long itemId);

    List<ItemDto> getByUser(Long id);

    List<ItemDto> getBySearch(String textQuery);

    CommentDto saveComment(Long itemId, Long userId, CommentDto commentDto);
}
