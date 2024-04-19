package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

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

    private final BookingService service;

    @PostMapping
    public BookingResponseDto save(@RequestHeader(USER_ID) Long bookerId,
                                          @Valid @RequestBody BookingRequestDto requestDto) {
        log.info("add booking request {}", requestDto);
        return service.save(bookerId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto update(@RequestHeader(USER_ID) Long ownerId,
                                            @PathVariable Long bookingId,
                                            @RequestParam(name = "approved") Boolean bookingStatus) {
        log.info("update booking request id={}", bookingId);
        return service.update(ownerId, bookingId, bookingStatus);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getByBookingId(@RequestHeader(USER_ID) Long id,
                                             @PathVariable Long bookingId) {
        log.info("get booking by id={}", id);
        return service.getByBookingId(id, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getByBookerId(@RequestHeader(USER_ID) Long bookerId,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(defaultValue = "ALL") String state) {
        log.info("get booking by booker id={}", bookerId);
        return service.getByBookerId(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getByOwnerId(@RequestHeader(USER_ID) Long ownerId,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(defaultValue = "ALL") String state) {
        log.info("get booking by owner id={}", ownerId);
        return service.getByOwnerId(ownerId, state, from, size);
    }
}
