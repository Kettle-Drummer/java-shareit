package ru.practicum.shareit.request.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findItemRequestsByRequesterId(Long requesterId);

    @Query("SELECT r FROM ItemRequest r WHERE r.requester.id <> :requesterId")
    Page<ItemRequest> findItemRequestsByRequesterId(@Param("requesterId") Long requesterId, Pageable pageable);
}
