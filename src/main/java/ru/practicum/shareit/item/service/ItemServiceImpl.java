package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.EntityNotFoundException;
import ru.practicum.shareit.error.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    public ItemDto add(ItemDto itemDto, Long id) {

        userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Нет пользователя с id: " + id));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userRepository.getReferenceById(id));
        item = itemRepository.save(item);
        log.info("Добавлен новый лот: {} пользователем id:{}", itemDto, id);
        return ItemMapper.toItemDto(item);
    }

    public ItemDto update(ItemDto itemDto, Long id, Long itemId) {
        User existOwner = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Нет пользователя с id: " + id));
        Item existItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Нет лота с id: " + itemId));
        if (!existItem.getOwner().getId().equals(existOwner.getId())) {
            throw new EntityNotFoundException("Не совпадает пользователь");
        }
        Item save = ItemMapper.updateItemByGivenDto(existItem, itemDto);
        Item result = itemRepository.save(save);
        log.info("Обновлен лот: {} пользователем id:{}", itemDto, id);
        return ItemMapper.toItemDto(result);
    }

    public ItemDto getById(Long id, Long itemId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Нет пользователя с id: " + id));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Нет лота с id: " + itemId));
        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        if (Objects.equals(item.getOwner().getId(), user.getId())) {
            ItemDto itemWithBookings = getItemWithBookings(itemId);
            itemWithBookings.setComments(comments);
            return itemWithBookings;
        }
        ItemDto itemDto = ItemMapper.toItemDto(item);
        itemDto.setComments(comments);
        return itemDto;
    }


    public List<ItemDto> getByUser(Long id) {
        List<Item> items = itemRepository.findItemsByOwnerId(id);
        List<Booking> lastBookings = bookingRepository.findLastBookingsForOwnerItems(id);
        List<Booking> nextBookings = bookingRepository.findNextBookingsForOwnerItems(id);
        if (id != null && userRepository.findById(id).isPresent()) {
            Map<Long, BookingItemDto> lastBookingsMap = lastBookings.stream()
                    .collect(Collectors.toMap(
                            booking -> booking.getItem().getId(),
                            this::toBookingItemDto,
                            (existing, replacement) -> existing
                    ));
            Map<Long, BookingItemDto> nextBookingsMap = nextBookings.stream()
                    .collect(Collectors.toMap(
                            booking -> booking.getItem().getId(),
                            this::toBookingItemDto,
                            (existing, replacement) -> existing
                    ));
            List<CommentDto> comments = commentRepository.findByAuthorId(id).stream()
                    .map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList());
            return items.stream()
                    .map(item -> ItemDto.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .description(item.getDescription())
                            .available(item.getAvailable())
                            .lastBooking(lastBookingsMap.get(item.getId()))
                            .nextBooking(nextBookingsMap.get(item.getId()))
                            .comments(comments)
                            .build())
                    .collect(Collectors.toList());
        }
        return itemRepository.findAll().stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }


    public List<ItemDto> getBySearch(String textQuery) {
        return itemRepository.findItemsBySearch(textQuery).stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto saveComment(Long itemId, Long userId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Нет пользователя с id: " + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Нет лота с id: " + itemId));
        List<Booking> finishedBookings = bookingRepository.findFinishedBookingsByItemAndUser(itemId, userId);
        if (finishedBookings.isEmpty()) {
            throw new ValidationException("Бронирование еще не окончено");
        }
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(commentRepository.save(savedComment));
    }

    private ItemDto getItemWithBookings(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("Нет лота с id: " + itemId));
        BookingItemDto lastBookingDto = findPastBookingsByItemId(itemId);
        BookingItemDto nextBookingDto = findFutureBookingsByItemId(itemId);
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBookingDto)
                .nextBooking(nextBookingDto)
                .build();
    }

    private BookingItemDto findFutureBookingsByItemId(Long itemId) {
        return bookingRepository.findFutureBookingsByItemId(itemId)
                .stream()
                .map(this::toBookingItemDto)
                .findFirst()
                .orElse(null);
    }

    private BookingItemDto findPastBookingsByItemId(Long itemId) {
        return bookingRepository.findPastBookingsByItemId(itemId)
                .stream()
                .map(this::toBookingItemDto)
                .findFirst()
                .orElse(null);
    }

    private BookingItemDto toBookingItemDto(Booking booking) {
        return new BookingItemDto(booking.getId(), booking.getBooker().getId());
    }
}
