package ru.practicum.shareit.request.service;

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

import ru.practicum.shareit.error.EntityNotFoundException;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "User", "user@example.com");
        itemRequest = new ItemRequest(1L, "Need a drill", user, LocalDateTime.now());
        itemRequestDto = new ItemRequestDto(1L, "Need a drill", 1L, LocalDateTime.now(), Collections.emptyList());

        when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(user));
        when(itemRepository.findByRequestId(any(Long.class))).thenReturn(Collections.emptyList());
    }

    @Test
    void saveRequestShouldReturnSavedRequest() {
        when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto result = itemRequestService.add(1L, itemRequestDto);

        assertNotNull(result);
        assertEquals(itemRequestDto.getDescription(), result.getDescription());
        verify(userRepository, times(1)).findById(any(Long.class));
        verify(requestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void getRequestsShouldReturnListOfRequests() {
        when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(user));
        when(requestRepository.findItemRequestsByRequesterId(any(Long.class))).thenReturn(List.of(itemRequest));

        List<ItemRequestDto> result = itemRequestService.getByUserId(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findById(any(Long.class));
        verify(requestRepository, times(1)).findItemRequestsByRequesterId(any(Long.class));
    }

    @Test
    void getRequestByPaginationShouldReturnListOfRequests() {
        Page<ItemRequest> page = new PageImpl<>(List.of(itemRequest));
        when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(user));
        when(requestRepository.findAllWithoutRequesterId(any(Long.class), any(PageRequest.class))).thenReturn(page);

        List<ItemRequestDto> result = itemRequestService.getAllPaginated(1L, 0, 1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findById(any(Long.class));
        verify(requestRepository, times(1)).findAllWithoutRequesterId(any(Long.class), any(PageRequest.class));
    }

    @Test
    void getRequestByPaginationShouldReturnGetRequestsResult() {
        Page<ItemRequest> page = new PageImpl<>(List.of());
        when(requestRepository.findAllWithoutRequesterId(any(Long.class), any())).thenReturn(page);

        List<ItemRequestDto> result = itemRequestService.getAllPaginated(1L, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRequestByIdShouldReturnRequest() {
        when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(user));
        when(requestRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(itemRequest));

        ItemRequestDto result = itemRequestService.getByRequestId(1L, 1L);

        assertNotNull(result);
        assertEquals(itemRequestDto.getDescription(), result.getDescription());
        verify(userRepository, times(1)).findById(any(Long.class));
        verify(requestRepository, times(1)).findById(any(Long.class));
    }

    @Test
    void getRequestByIdShouldThrowExceptionWhenRequestNotFound() {
        when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(user));
        when(requestRepository.findById(any(Long.class))).thenReturn(java.util.Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemRequestService.getByRequestId(1L, 1L));

        verify(userRepository, times(1)).findById(any(Long.class));
        verify(requestRepository, times(1)).findById(any(Long.class));
    }
}