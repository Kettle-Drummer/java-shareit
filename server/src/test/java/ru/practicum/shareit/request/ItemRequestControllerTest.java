package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        itemRequestDto = new ItemRequestDto(1L, "Description", 2L, LocalDateTime.now(), Collections.emptyList());
    }

    @Test
    void testSaveRequestWhenValidRequestThenReturnItemRequestDto() throws Exception {
        when(itemRequestService.add(any(Long.class), any(ItemRequestDto.class))).thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                .header(ItemRequestController.USER_ID, 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"Description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestDto.getDescription()));
    }

    @Test
    void testGetRequestsWhenValidRequestThenReturnListOfItemRequestDto() throws Exception {
        when(itemRequestService.getByUserId(any(Long.class))).thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests")
                .header(ItemRequestController.USER_ID, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$[0].description").value(itemRequestDto.getDescription()));
    }

    @Test
    void testGetRequestsByPaginationWhenValidRequestThenReturnListOfItemRequestDto() throws Exception {
        when(itemRequestService.getAllPaginated(any(Long.class), any(Integer.class), any(Integer.class)))
                .thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests/all")
                .header(ItemRequestController.USER_ID, 2L)
                .param("from", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$[0].description").value(itemRequestDto.getDescription()));
    }

    @Test
    void testGetRequestByIdWhenValidRequestThenReturnItemRequestDto() throws Exception {
        when(itemRequestService.getByRequestId(any(Long.class), any(Long.class))).thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                .header(ItemRequestController.USER_ID, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestDto.getDescription()));
    }
}