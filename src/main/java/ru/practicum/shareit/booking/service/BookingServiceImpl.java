package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.EntityNotFoundException;
import ru.practicum.shareit.error.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponseDto save(Long bookerId, BookingRequestDto requestDto) {
        bookingTimeValidation(requestDto);
        User booker = userRepository.findById(bookerId)
                .orElseThrow((() -> new EntityNotFoundException("Нет пользователя с id: " + bookerId)));
        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow((() -> new EntityNotFoundException("Нет лота с id: " + requestDto.getItemId())));
        if (!item.getAvailable()) {
            throw new ValidationException("Лот недоступен");
        }
        if (Objects.equals(item.getOwner().getId(), bookerId)) {
            throw new EntityNotFoundException("Владелец не может бронировать свои лоты");
        }
        Booking bookingAfterMap = BookingMapper.toBooking(requestDto, item, booker);
        Booking savedBooking = bookingRepository.save(bookingAfterMap);
        return BookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    public BookingResponseDto update(Long ownerId, Long bookingId, Boolean approveStatus) {
        Booking booking = bookingRepository.findBookingByIdWithItemAndBookerEagerly(bookingId);
        if (!Objects.equals(booking.getItem().getOwner().getId(), ownerId)) {
            throw new EntityNotFoundException("Не совпадает id ладельца");
        }
        if (booking.getStatus().equals(BookingStatus.WAITING)) {
            BookingStatus newStatus = approveStatus ? BookingStatus.APPROVED : BookingStatus.REJECTED;
            booking.setStatus(newStatus);
        } else {
            throw new ValidationException("Состояние меняется только из WAITING");
        }
        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    public BookingResponseDto getByBookingId(Long id, Long bookingId) {
        Booking booking = bookingRepository.findBookingByIdWithItemAndBookerEagerly(bookingId);
        if (booking == null) {
            throw new EntityNotFoundException("Нет брони с id: " + bookingId);
        }
        boolean isBooker = Objects.equals(booking.getBooker().getId(), id);
        boolean isOwner = Objects.equals(booking.getItem().getOwner().getId(), id);
        if (isBooker) {
            return BookingMapper.toBookingResponseDto(booking);
        } else if (isOwner) {
            return BookingMapper.toBookingResponseDto(booking);
        } else {
            throw new EntityNotFoundException("Entity not found!");
        }
    }

    @Override
    public List<BookingResponseDto> getByBookerId(Long bookerId, String state) {
        userRepository.findById(bookerId).orElseThrow(() -> new EntityNotFoundException("Нет пользователя с id: " + bookerId));
        BookingState fromState;
        try {
            fromState = BookingState.valueOf(state);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Unknown state: " + state);
        }
        switch (fromState) {
            case ALL:
                return BookingMapper.toBookingResponseDtoList(bookingRepository.findAllByGivenUserId(bookerId));
            case CURRENT:
                return BookingMapper.toBookingResponseDtoList(bookingRepository.findCurrentBookingsByBookerId(bookerId));
            case PAST:
                return BookingMapper.toBookingResponseDtoList(bookingRepository.findPastBookingsByBookerId(bookerId));
            case FUTURE:
                return BookingMapper.toBookingResponseDtoList(bookingRepository.findFutureBookingsByBookerId(bookerId));
            case WAITING:
                return BookingMapper.toBookingResponseDtoList(bookingRepository.findWaitingBookingsByBookerId(bookerId));
            case REJECTED:
                return BookingMapper.toBookingResponseDtoList(bookingRepository.findRejectedBookingsByBookerId(bookerId));
            default:
                throw new IllegalStateException("Unknown state: " + state);
        }
    }

    @Override
    public List<BookingResponseDto> getByOwnerId(Long ownerId, String state) {
        userRepository.findById(ownerId).orElseThrow(() -> new EntityNotFoundException("Нет пользователя с id: " + ownerId));
        BookingState fromState;
        try {
            fromState = BookingState.valueOf(state);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Unknown state: " + state);
        }
        switch (fromState) {
            case ALL:
                return BookingMapper.toBookingResponseDtoList(bookingRepository.findAllBookingsByOwnerId(ownerId));
            case CURRENT:
                return BookingMapper.toBookingResponseDtoList(bookingRepository.findCurrentBookingsByOwnerId(ownerId));
            case PAST:
                return BookingMapper.toBookingResponseDtoList(bookingRepository.findPastBookingsByOwnerId(ownerId));
            case FUTURE:
                return BookingMapper.toBookingResponseDtoList(bookingRepository.findFutureBookingsByOwnerId(ownerId));
            case WAITING:
                return BookingMapper.toBookingResponseDtoList(bookingRepository.findWaitingBookingsByOwnerId(ownerId));
            case REJECTED:
                return BookingMapper.toBookingResponseDtoList(bookingRepository.findRejectedBookingsByOwnerId(ownerId));
            default:
                throw new IllegalStateException("Unknown state: " + state);
        }
    }

    private void bookingTimeValidation(BookingRequestDto requestDto) {
        if (requestDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Бронь начинается в будущем");
        }
        if (requestDto.getStart().equals(requestDto.getEnd())) {
            throw new ValidationException("Бронь начинается раньше, чем заканчивается");
        }
        if (requestDto.getStart().isAfter(requestDto.getEnd())) {
            throw new ValidationException("Бронь начинается раньше, чем заканчивается");
        }
    }
}