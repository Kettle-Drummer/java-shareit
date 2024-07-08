package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.EntityNotFoundException;
import ru.practicum.shareit.error.ValidationException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        User user = findUserById(requesterId);
        ItemRequest itemRequest = ItemRequestMapper.INSTANCE.toItemRequest(dto);
        itemRequest.setRequester(user);
        ItemRequest savedRequest = requestRepository.save(itemRequest);
        return ItemRequestMapper.INSTANCE.toItemRequestDto(savedRequest);
    }

    @Override
    public ItemRequestDto getByRequestId(Long requesterId, Long requestId) {
        findUserById(requesterId);
        ItemRequest itemRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Нет запроса с id: " + requestId));
        return convertToItemRequestDtoWithItems(List.of(itemRequest)).get(0);
    }

    @Override
    public List<ItemRequestDto> getByUserId(Long userId) {
        log.debug("getRequests method was called in ItemRequestServiceIml ");
        findUserById(userId);
        List<ItemRequest> itemRequestList = requestRepository.findItemRequestsByRequesterId(userId);
        return convertToItemRequestDtoWithItems(itemRequestList);
    }

    @Override
    public List<ItemRequestDto> getAllPaginated(Long requesterId, Integer from, Integer size) {
        log.debug("getRequestByPagination method was called in ItemRequestServiceIml");
        findUserById(requesterId);
        Pageable pageable = PageRequest.of(from / size, size);

        List<ItemRequest> itemRequests = requestRepository.findAllWithoutRequesterId(requesterId, pageable).getContent();
        return convertToItemRequestDtoWithItems(itemRequests);
    }

    private List<ItemRequestDto> convertToItemRequestDtoWithItems(List<ItemRequest> itemRequests) {

        List<Long> requestIds = new ArrayList<>();
        for (ItemRequest itemRequest : itemRequests) {
            requestIds.add(itemRequest.getId());
        }

        Map<Long, List<Item>> itemsForRequests = getItemsForRequests(requestIds);

        List<ItemRequestDto> itemRequestsDto = ItemRequestMapper.INSTANCE.toItemRequestDtoList(itemRequests);

        List<ItemRequestDto> result = new ArrayList<>();

        for (ItemRequestDto itemRequestDto : itemRequestsDto) {
            List<ItemDto> itemDtoList = itemsForRequests.get(itemRequestDto.getId()).stream()
                    .map(ItemMapper.INSTANCE::toItemDto)
                    .collect(Collectors.toList());
            itemRequestDto.setItems(itemDtoList);
            result.add(itemRequestDto);
        }

        return result;
    }

    private Map<Long, List<Item>> getItemsForRequests(List<Long> requests) {
        List<Item> items = itemRepository.findByRequestIdIn(requests);
        Map<Long, List<Item>> result = items.stream().collect(Collectors.toMap(Item::getRequestId, List::of));
        for (Long request : requests)
            if (!result.containsKey(request)) {
                result.put(request, new ArrayList<>());
            }
        return result;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow((() -> new EntityNotFoundException("Нет пользователя с id: " + userId)));
    }
}
