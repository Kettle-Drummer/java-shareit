package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.ArrayList;
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
        User booker = findUserById(bookerId);
        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow((() -> new EntityNotFoundException("Нет лота с id: " + requestDto.getItemId())));
        if (!item.getAvailable()) {
            throw new ValidationException("Лот недоступен");
        }
        if (Objects.equals(item.getOwner().getId(), bookerId)) {
            throw new EntityNotFoundException("Владелец не может бронировать свои лоты");
        }
        Booking bookingAfterMap = BookingMapper.INSTANCE.toBooking(requestDto, item, booker);
        Booking savedBooking = bookingRepository.save(bookingAfterMap);
        return BookingMapper.INSTANCE.toBookingResponseDto(savedBooking);
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
        return BookingMapper.INSTANCE.toBookingResponseDto(savedBooking);
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
            return BookingMapper.INSTANCE.toBookingResponseDto(booking);
        } else if (isOwner) {
            return BookingMapper.INSTANCE.toBookingResponseDto(booking);
        } else {
            throw new EntityNotFoundException("Запросить может только собственник вещи или автор брони");
        }
    }

    @Override
    public List<BookingResponseDto> getByBookerId(Long bookerId, String state, Integer from, Integer size) {
        findUserById(bookerId);
        checkPageableParameters(from, size);

        BookingState fromState;
        try {
            fromState = BookingState.valueOf(state);
        } catch (RuntimeException e) {
            throw new ValidationException("Unknown state: " + state);
        }

        Pageable pageable = PageRequest.of(from / size, size);
        switch (fromState) {
            case ALL:
                return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                        .findAllByGivenUserId(bookerId, pageable).getContent());
            case CURRENT:
                return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                        .findCurrentBookingsByBookerId(bookerId, pageable).getContent());
            case PAST:
                return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                        .findPastBookingsByBookerId(bookerId, pageable).getContent());
            case FUTURE:
                return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                        .findFutureBookingsByBookerId(bookerId, pageable).getContent());
            case WAITING:
                return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                        .findWaitingBookingsByBookerId(bookerId, pageable).getContent());
            case REJECTED:
                return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                        .findRejectedBookingsByBookerId(bookerId, pageable).getContent());
        }
        throw new IllegalStateException("Unknown state: " + state);
    }

    @Override
    public List<BookingResponseDto> getByOwnerId(Long ownerId, String state, Integer from, Integer size) {
        findUserById(ownerId);
        checkPageableParameters(from, size);

        BookingState fromState;
        try {
            fromState = BookingState.valueOf(state);
        } catch (RuntimeException e) {
            throw new ValidationException("Unknown state: " + state);
        }

        Pageable pageable = PageRequest.of(from / size, size);
        switch (fromState) {
            case ALL:
                return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                        .findAllBookingsByOwnerId(ownerId, pageable).getContent());
            case CURRENT:
                return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                        .findCurrentBookingsByOwnerId(ownerId, pageable).getContent());
            case PAST:
                return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                        .findPastBookingsByOwnerId(ownerId, pageable).getContent());
            case FUTURE:
                return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                        .findFutureBookingsByOwnerId(ownerId, pageable).getContent());
            case WAITING:
                return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                        .findWaitingBookingsByOwnerId(ownerId, pageable).getContent());
            case REJECTED:
                return BookingMapper.INSTANCE.toBookingResponseDtoList(bookingRepository
                        .findRejectedBookingsByOwnerId(ownerId, pageable).getContent());
        }
        throw new IllegalStateException("Unknown state: " + state);
    }

    private void bookingTimeValidation(BookingRequestDto requestDto) {
        if (requestDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Бронь должна начинаться в будущем");
        }
        if (requestDto.getStart().equals(requestDto.getEnd())) {
            throw new ValidationException("Бронь не должна заканчиваться мгновенно");
        }
        if (requestDto.getStart().isAfter(requestDto.getEnd())) {
            throw new ValidationException("Бронь должна начинаться раньше, чем заканчивается");
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow((() -> new EntityNotFoundException("Нет пользователя с id: " + userId)));
    }

    private void checkPageableParameters(int from, int size) {
        if (from < 0) {
            throw new ValidationException("Не верно указано значение первого элемента страницы. " +
                    "Переданное значение: " + from);
        }
        if (size <= 0) {
            throw new ValidationException("Не верно указано значение размера страницы. Переданное значение: " + size);
        }
    }
}