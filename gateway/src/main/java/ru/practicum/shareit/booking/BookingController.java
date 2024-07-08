package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.error.ValidationException;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    public static final String USER_ID = "X-Sharer-User-Id";

    private final BookingClient bookingClient;

    @PostMapping
    public BookingResponseDto save(@RequestHeader(USER_ID) Long bookerId,
                                  @Valid @RequestBody BookingRequestDto requestDto) {
        bookingTimeValidation(requestDto);
        return bookingClient.save(bookerId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto update(@RequestHeader(USER_ID) Long ownerId,
                                     @PathVariable Long bookingId,
                                     @RequestParam(name = "approved") Boolean bookingStatus) {
        return bookingClient.update(ownerId, bookingId, bookingStatus);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getByBookingId(@RequestHeader(USER_ID) Long id,
                                             @PathVariable Long bookingId) {
        return bookingClient.getByBookingId(id, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getByBookerId(@RequestHeader(USER_ID) Long bookerId,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(defaultValue = "ALL") String state) {
        checkPageableParameters(from, size);

        try { BookingState.valueOf(state);
        } catch (RuntimeException e) { //это перенести
            throw new ValidationException("Unknown state: " + state);
        }
        return bookingClient.getByBookerId(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getByOwnerId(@RequestHeader(USER_ID) Long ownerId,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(defaultValue = "ALL") String state) {
        checkPageableParameters(from, size);

        try { BookingState.valueOf(state);
        } catch (RuntimeException e) { //это перенести
            throw new ValidationException("Unknown state: " + state);
        }
        return bookingClient.getByOwnerId(ownerId, state, from, size);
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

    private void bookingTimeValidation(BookingRequestDto requestDto) {
        if (requestDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Бронь должна начинаться в будущем");
        }
        if (requestDto.getStart().equals(requestDto.getEnd())) {  //это перенести
            throw new ValidationException("Бронь не должна заканчиваться мгновенно");
        }
        if (requestDto.getStart().isAfter(requestDto.getEnd())) {
            throw new ValidationException("Бронь должна начинаться раньше, чем заканчивается");
        }
    }
}

