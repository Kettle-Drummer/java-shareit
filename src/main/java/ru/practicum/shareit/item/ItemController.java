package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemServiceImpl itemServiceImpl;
    public static final String OWNER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto add(@Valid @RequestBody ItemDto itemDto, @RequestHeader(OWNER_ID) Long id) {
        ItemDto item = itemServiceImpl.add(itemDto, id);
        log.info("Add Item{}", item);
        return item;
    }

    @PatchMapping("{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto, @RequestHeader(OWNER_ID) Long id, @PathVariable Long itemId) {
        ItemDto item = itemServiceImpl.update(itemDto, id, itemId);
        log.info("Update Item{}", item);
        return item;
    }

    @GetMapping("{itemId}")
    public ItemDto getById(@RequestHeader(OWNER_ID) Long id,
                           @PathVariable Long itemId) {
        log.info("Get item by id{}", itemId);
        return itemServiceImpl.getById(id, itemId);
    }

    @GetMapping
    public Collection<ItemDto> getByUser(@RequestHeader(OWNER_ID) Long id) {
        log.info("Get items by user id{}", id);
        return itemServiceImpl.getByUser(id);
    }

    @GetMapping("/search")
    public List<ItemDto> getBySearch(@RequestParam(required = false) String text) {
        log.info("search {}", text);
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return itemServiceImpl.getBySearch(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto saveComment(@PathVariable Long itemId,
                                  @RequestHeader(OWNER_ID) Long userId,
                                  @Valid @RequestBody CommentDto commentDto) {
        log.info("save comment {}", commentDto);
        return itemServiceImpl.saveComment(itemId, userId, commentDto);
    }
}
