package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

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
        return itemRequestService.add(requesterId, itemRequestDto);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@RequestHeader(USER_ID) Long requesterId,
                                  @PathVariable Long requestId) {
        return itemRequestService.getById(requesterId, requestId);
    }

    @GetMapping
    public List<ItemRequestDto> getByRequesterId(@RequestHeader(USER_ID) Long requesterId) {
        return itemRequestService.getByRequesterId(requesterId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getPaginated(@RequestHeader(USER_ID) Long requesterId,
                                                        @RequestParam(required = false) @PositiveOrZero Integer from,
                                                        @RequestParam(required = false) @Positive Integer size) {
        return itemRequestService.getPaginated(requesterId, from, size);
    }
}