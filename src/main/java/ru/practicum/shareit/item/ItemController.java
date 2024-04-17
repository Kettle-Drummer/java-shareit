package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    public static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto add(@RequestHeader(USER_ID) Long id,
                            @Validated @RequestBody ItemDto itemDto) {
        log.info("POST request received to save item");
        return itemService.add(id, itemDto);
    }

    @PatchMapping("{itemId}")
    public ItemDto update(@RequestHeader(USER_ID) Long id,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        ItemDto item = itemService.update(itemDto, id, itemId);
        log.info("Update Item{}", item);
        return item;
    }

    @GetMapping("{itemId}")
    public ItemDto getById(@RequestHeader(USER_ID) Long id,
                           @PathVariable Long itemId) {
        log.info("Get item by id{}", itemId);
        return itemService.getById(id, itemId);
    }

    @GetMapping
    public Collection<ItemDto> getByUser(@RequestHeader(USER_ID) Long id) {
        log.info("Get items by user id{}", id);
        return itemService.getByUser(id);
    }

    @GetMapping("/search")
    public List<ItemDto> getBySearch(@RequestParam(required = false) String text) {
        log.info("search {}", text);
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return itemService.getBySearch(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto saveComment(@PathVariable Long itemId,
                                  @RequestHeader(USER_ID) Long userId,
                                  @Valid @RequestBody CommentDto commentDto) {
        log.info("save comment {}", commentDto);
        return itemService.saveComment(itemId, userId, commentDto);
    }
}
