package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository requestRepository;

    @PersistenceContext
    private EntityManager em;

    private User user;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("Test User")
                .email("test@test.com")
                .build();
        em.persist(user);

        itemRequest = ItemRequest.builder()
                .description("Description")
                .requester(user)
                .created(LocalDateTime.now())
                .build();
        em.persist(itemRequest);
    }

    @Test
    public void testFindItemRequestsByRequesterIdWhenRecordsExistThenReturnList() {
        List<ItemRequest> result = requestRepository.findItemRequestsByRequesterId(user.getId());
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo(itemRequest.getDescription());
        assertThat(result.get(0).getRequester()).isEqualTo(user);
    }

    @Test
    public void testFindItemRequestsByRequesterIdWhenNoRecordsThenReturnEmptyList() {
        List<ItemRequest> result = requestRepository.findItemRequestsByRequesterId(-1L);
        assertThat(result).isEmpty();
    }
}
