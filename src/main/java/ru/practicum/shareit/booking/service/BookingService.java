package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {

    BookingResponseDto save(Long bookerId, BookingRequestDto requestDto);

    BookingResponseDto update(Long bookerId, Long bookingId, Boolean bookingStatus);

    BookingResponseDto getByBookingId(Long id, Long bookingId);

    List<BookingResponseDto> getByBookerId(Long bookerId, String state);

    List<BookingResponseDto> getByOwnerId(Long ownerId, String state);
}
