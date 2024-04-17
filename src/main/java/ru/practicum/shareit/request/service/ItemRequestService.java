package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto add(Long requesterId, ItemRequestDto dto);

    ItemRequestDto getById(Long requesterId, Long requestId);

    List<ItemRequestDto> getByRequesterId(Long requesterId);

    List<ItemRequestDto> getPaginated(Long requesterId, Integer from, Integer size);
}
