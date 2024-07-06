package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
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
        return bookingClient.getByBookerId(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getByOwnerId(@RequestHeader(USER_ID) Long ownerId,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(defaultValue = "ALL") String state) {
        return bookingClient.getByOwnerId(ownerId, state, from, size);
    }
}

