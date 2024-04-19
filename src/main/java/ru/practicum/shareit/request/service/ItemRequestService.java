package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto add(Long requesterId, ItemRequestDto dto);

    ItemRequestDto getByRequestId(Long requesterId, Long requestId);

    List<ItemRequestDto> getByUserId(Long userId);

    List<ItemRequestDto> getAllPaginated(Long requesterId, Integer from, Integer size);
}
