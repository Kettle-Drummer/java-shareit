package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto add(Long requesterId, ItemRequestDto dto) {
        log.debug("saveRequest method was called in ItemRequestServiceImpl");
        User user = userRepository.findById(requesterId).orElseThrow(() -> new EntityNotFoundException("User not exist"));
        ItemRequest itemRequest = ItemRequestMapper.INSTANCE.toItemRequest(dto);
        itemRequest.setRequester(user);
        ItemRequest savedRequest = requestRepository.save(itemRequest);
        return ItemRequestMapper.INSTANCE.toItemRequestDto(savedRequest);
    }

    @Override
    public ItemRequestDto getById(Long requesterId, Long requestId) {
        userRepository.findById(requesterId).orElseThrow(() -> new EntityNotFoundException("User not exist"));
        ItemRequest itemRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not exist"));
        return convertToItemRequestDtoWithItems(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getByRequesterId(Long requesterId) {
        log.debug("getRequests method was called in ItemRequestServiceIml ");
        userRepository.findById(requesterId).orElseThrow(() -> new EntityNotFoundException("User not exist"));
        List<ItemRequest> itemRequestsByRequesterId = requestRepository.findItemRequestsByRequesterId(requesterId);
        return itemRequestsByRequesterId.stream()
                .map(this::convertToItemRequestDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getPaginated(Long requesterId, Integer from, Integer size) {
        log.debug("getRequestByPagination method was called in ItemRequestServiceIml");
        userRepository.findById(requesterId).orElseThrow(() -> new EntityNotFoundException("User not exist"));
        if (from != null && size != null) {
            Pageable pageable = PageRequest.of(from / size, size);
            List<ItemRequest> itemRequests = requestRepository.findItemRequestsByRequesterId(requesterId, pageable).getContent();
            return itemRequests.stream()
                    .map(this::convertToItemRequestDtoWithItems)
                    .collect(Collectors.toList());
        }
        return getByRequesterId(requesterId);
    }

    private ItemRequestDto convertToItemRequestDtoWithItems(ItemRequest itemRequest) {
        ItemRequestDto dto = ItemRequestMapper.INSTANCE.toItemRequestDto(itemRequest);
        List<Item> items = itemRepository.findByRequestId(itemRequest.getId());
        List<ItemDto> itemDtoList = items.stream()
                .map(ItemMapper.INSTANCE::toItemDto)
                .collect(Collectors.toList());
        dto.setItems(itemDtoList);
        return dto;
    }
}
