package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.ArrayList;
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

        @Override
        public ItemDto add(Long id, ItemDto itemDto) {
        log.debug("saveItem method called in Service to save");
        User owner = findUserById(id);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setOwner(owner);

        item = itemRepository.save(item);
        return ItemMapper.INSTANCE.toItemDto(item);
    }

    public ItemDto update(ItemDto itemDto, Long id, Long itemId) {
        User existOwner = findUserById(id);
        Item existItem = findItemById(itemId);
        if (!existItem.getOwner().getId().equals(existOwner.getId())) {
            throw new EntityNotFoundException("Не совпадает пользователь");
        }
        Item save = ItemMapper.INSTANCE.updateItemByGivenDto(itemDto, existItem);
        Item result = itemRepository.save(save);
        log.info("Обновлен лот: {} пользователем id:{}", itemDto, id);
        return ItemMapper.INSTANCE.toItemDto(result);
    }

    public ItemDto getById(Long id, Long itemId) {
        User user = findUserById(id);
        Item item = findItemById(itemId);
        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper.INSTANCE::toCommentDto)
                .collect(Collectors.toList());
        if (Objects.equals(item.getOwner().getId(), user.getId())) {
            ItemDto itemWithBookings = getItemWithBookings(itemId);
            itemWithBookings.setComments(comments);
            return itemWithBookings;
        }
        ItemDto itemDto = ItemMapper.INSTANCE.toItemDto(item);
        itemDto.setComments(comments);
        return itemDto;
    }


    public List<ItemDto> getByUser(Long id, int from, int size) {
        checkPageableParameters(from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findItemsByOwnerId(id, pageable);
        if (items.isEmpty()) {
            return new ArrayList<>();
        }
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
                    .map(CommentMapper.INSTANCE::toCommentDto)
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
        return ItemMapper.INSTANCE.toItemDtoList(itemRepository
                .findAll(pageable).getContent());
    }


    public List<ItemDto> getBySearch(String textQuery, int from, int size) {
        checkPageableParameters(from, size);
        Pageable pageable = PageRequest.of(from / size, size);

        return ItemMapper.INSTANCE.toItemDtoList(itemRepository
                .findItemsBySearch(textQuery, pageable).getContent().stream().filter(Item::getAvailable).collect(Collectors.toList()));
    }

    @Override
    public CommentDto saveComment(Long itemId, Long userId, CommentDto commentDto) {
        User user = findUserById(userId);
        Item item = findItemById(itemId);
        List<Booking> finishedBookings = bookingRepository.findFinishedBookingsByItemAndUser(itemId, userId);
        if (finishedBookings.isEmpty()) {
            throw new ValidationException("Бронирование еще не окончено");
        }
        Comment comment = CommentMapper.INSTANCE.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.INSTANCE.toCommentDto(savedComment);
    }

    private ItemDto getItemWithBookings(Long itemId) {
        Item item = findItemById(itemId);
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

    private void checkPageableParameters(int from, int size) {
        if (from < 0) {
            throw new ValidationException("Не верно указано значение первого элемента страницы. " +
                    "Переданное значение: " + from);
        }
        if (size <= 0) {
            throw new ValidationException("Не верно указано значение размера страницы. Переданное значение: " + size);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow((() -> new EntityNotFoundException("Нет пользователя с id: " + userId)));
    }

    private Item findItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow((() -> new EntityNotFoundException("Нет лота с id: " + itemId)));
    }
}
