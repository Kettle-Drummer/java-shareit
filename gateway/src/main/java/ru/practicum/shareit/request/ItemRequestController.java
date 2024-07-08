package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.error.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
public class ItemRequestController {

    public static final String USER_ID = "X-Sharer-User-Id";

    private final ItemRequestClient requestClient;

    @PostMapping
    public ItemRequestDto add(@RequestHeader(USER_ID) Long requesterId,
                              @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return requestClient.add(requesterId, itemRequestDto);
    }

    @GetMapping("/{id}")
    public ItemRequestDto getByRequestId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long id) {
        return requestClient.getByRequestId(userId, id);
    }

    @GetMapping
    public List<ItemRequestDto> getByUserId(@RequestHeader(USER_ID) Long userId) {
        return requestClient.getByUserId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllPaginated(@RequestHeader(USER_ID) Long userId,
                                        @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                        @Positive @RequestParam(defaultValue = "10") int size) {
        checkPageableParameters(from, size);
        return requestClient.getAllPaginated(userId, from, size);
    }

    private void checkPageableParameters(int from, int size) {  //это перенести
        if (from < 0) {
            throw new ValidationException("Не верно указано значение первого элемента страницы. " +
                    "Переданное значение: " + from);
        }
        if (size <= 0) {
            throw new ValidationException("Не верно указано значение размера страницы. Переданное значение: " + size);
        }
    }
}
