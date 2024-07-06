package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.EntityNotFoundException;
import ru.practicum.shareit.error.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private Item item;
    private ItemDto itemDto;
    private Comment comment;
    private CommentDto commentDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        user = new User(1L, "User", "user@example.com");
        item = new Item(1L, "Item", "Description", true, user, null);
        itemDto = new ItemDto(1L, "Item", "Description", true, null, null, null, null);
        comment = new Comment(1L, "Comment", item, user, null);
        commentDto = new CommentDto(1L, "Comment", item, user, "User", null);
        booking = new Booking(1L, null, null, item, user, null);
    }

    @Test
    void testSaveItemWhenAllDependenciesAvailableThenSaveItem() {
        Long ownerId = 1L;
        Long requestId = 2L;
        ItemDto itemDto = ItemDto.builder()
                .name("Item Name")
                .description("Item Description")
                .available(true)
                .requestId(requestId)
                .build();
        User owner = new User(ownerId, "Owner Name", "owner@example.com");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item savedItem = invocation.getArgument(0);
            savedItem.setId(1L);
            return savedItem;
        });

        ItemDto savedItemDto = itemService.add(ownerId, itemDto);

        assertNotNull(savedItemDto);
        assertEquals(requestId, savedItemDto.getRequestId());
    }

    @Test
    void testPageableParametersFromShouldThrowException() {
        assertThrows(ValidationException.class, () -> itemService.getBySearch("test", -10, 10));
    }

    @Test
    void testPageableParametersSizeShouldThrowException() {
        assertThrows(ValidationException.class, () -> itemService.getBySearch("test", 0, -20));
    }

    @Test
    void testUpdateItemWhenAllDependenciesAvailableThenItemUpdated() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.update(itemDto, item.getId(), user.getId());

        assertNotNull(result);
        assertEquals(itemDto.getName(), result.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void testWhenUpdateItemWithWrongOwnerIdThenMustThrowException() {
        Long ownerId = 1L;
        Long wrongOwnerId = 2L;
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("Updated Item Name")
                .description("Updated Description")
                .available(true)
                .build();
        User existOwner = new User(ownerId, "Owner Name", "owner@example.com");
        User wrongOwner = new User(wrongOwnerId, "Wrong Owner Name", "wrongowner@example.com");
        Item existItem = new Item(itemId, "Item Name", "Item Description", true, wrongOwner, null);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(existOwner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existItem));

        assertThrows(EntityNotFoundException.class, () -> itemService.update(itemDto, itemId, ownerId));

        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void testGetByIdWhenAllDependenciesAvailableThenItemRetrieved() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(anyLong())).thenReturn(Collections.singletonList(comment));

        ItemDto result = itemService.getById(user.getId(), item.getId());

        assertNotNull(result);
        assertEquals(itemDto.getName(), result.getName());
        verify(commentRepository, times(1)).findByItemId(anyLong());
    }

    @Test
    void getByIdShouldReturnItemDtoWithCommentsWhenUserIsNotOwner() {
        Long userId = 1L;
        Long ownerId = 2L;
        Long itemId = 1L;
        User user = new User(userId, "User Name", "user@example.com");
        User owner = new User(ownerId, "Owner Name", "owner@example.com");
        Item item = new Item(itemId, "Item Name", "Item Description", true, owner, null);
        Comment comment = new Comment(1L, "Great item!", item, user, LocalDateTime.now());
        CommentDto commentDto = CommentMapper.INSTANCE.toCommentDto(comment);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(itemId)).thenReturn(Collections.singletonList(comment));

        ItemDto result = itemService.getById(userId, itemId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Item Name", result.getName());
        assertEquals("Item Description", result.getDescription());
        assertTrue(result.getAvailable());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertNotNull(result.getComments());
        assertFalse(result.getComments().isEmpty());
        assertEquals(1, result.getComments().size());
        assertEquals(commentDto.getText(), result.getComments().get(0).getText());

        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(commentRepository).findByItemId(itemId);
    }

    @Test
    void testGetAllItemsWhenAllDependenciesAvailableThenAllItemsRetrieved() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findItemsByOwnerIdOrderById(anyLong(), any(PageRequest.class))).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findLastBookingsForOwnerItems(anyLong())).thenReturn(Collections.singletonList(booking));
        when(bookingRepository.findNextBookingsForOwnerItems(anyLong())).thenReturn(Collections.singletonList(booking));
        when(commentRepository.findByAuthorId(anyLong())).thenReturn(Collections.singletonList(comment));

        List<ItemDto> result = itemService.getByUser(user.getId(), 0, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(itemRepository, times(1)).findItemsByOwnerIdOrderById(anyLong(), any(PageRequest.class));
    }

    @Test
    void testGetAllItemsWithWrongId() {
        when(itemService.getByUser(100L, 0, 10)).thenReturn(new ArrayList<>());

        List<ItemDto> result = itemService.getByUser(100L, 0, 10);

        assertEquals(result, new ArrayList<>());
    }

    @Test
    void testSearchByTextWhenAllDependenciesAvailableThenItemsSearched() {
        Page<Item> page = new PageImpl<>(List.of(item));
        when(itemRepository.findItemsBySearch(anyString(), any(PageRequest.class))).thenReturn(page);

        List<ItemDto> result = itemService.getBySearch("Item",0, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(itemRepository, times(1)).findItemsBySearch(anyString(), any(PageRequest.class));
    }

    @Test
    void testSaveCommentWhenAllDependenciesAvailableThenCommentSaved() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findFinishedBookingsByItemAndUser(anyLong(), anyLong())).thenReturn(Collections.singletonList(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = itemService.saveComment(item.getId(), user.getId(), commentDto);

        assertNotNull(result);
        assertEquals(commentDto.getText(), result.getText());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void saveCommentShouldThrowCustomBadRequestExceptionWhenNoFinishedBookings() {
        Long itemId = 1L;
        Long userId = 1L;
        CommentDto commentDto = new CommentDto();

        User user = new User(userId, "User Name", "user@example.com");
        Item item = new Item(itemId, "Item Name", "Item Description", true, user, null);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(java.util.Optional.of(item));
        when(bookingRepository.findFinishedBookingsByItemAndUser(itemId, userId)).thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () -> itemService.saveComment(itemId, userId, commentDto),
                "User cant comment this item, cause booking isn't done already");
    }
}