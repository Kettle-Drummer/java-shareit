package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    public static final String USER_ID = "X-Sharer-User-Id";

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto add(@RequestHeader(USER_ID) Long requesterId,
                              @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Получен запрос на добавление нового запроса на вещь от пользователя с ID: " + requesterId);
        return itemRequestService.add(requesterId, itemRequestDto);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getByRequestId(@RequestHeader(USER_ID) Long requesterId,
                                  @PathVariable Long requestId) {
        log.info("Получен запрос на отправку запроса на вещь с ID " + requestId);
        return itemRequestService.getByRequestId(requesterId, requestId);
    }

    @GetMapping
    public List<ItemRequestDto> getByUserId(@RequestHeader(USER_ID) Long userId) {
        log.info("Получен запрос на формирование списка запросов для пользователя с ID " + userId);
        return itemRequestService.getByUserId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllPaginated(@RequestHeader(USER_ID) Long requesterId,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        log.info("Получен запрос на формирование списка всех запросов на вещи для пользователя с ID: " + requesterId);
        return itemRequestService.getAllPaginated(requesterId, from, size);
    }
}