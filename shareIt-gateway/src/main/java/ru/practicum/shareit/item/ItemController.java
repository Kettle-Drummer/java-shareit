package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    public static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto add(@RequestHeader(USER_ID) Long id,
                       @Validated @RequestBody ItemDto itemDto) {
        return itemClient.add(id, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID) Long id,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        return itemClient.update(id, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader(USER_ID) Long id,
                                      @PathVariable Long itemId) {
        return itemClient.getById(id, itemId);
    }

    @GetMapping
    public List<ItemDto> getByUser(@RequestParam(defaultValue = "0") int from,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestHeader(USER_ID) Long id) {
        return itemClient.getByUser(id, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> getBySearch(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @RequestParam(defaultValue = "") String text,
                                     @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                     @Positive @RequestParam(defaultValue = "10") int size) {
        return itemClient.getBySearch(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto saveComment(@PathVariable Long itemId,
                                          @RequestHeader(USER_ID) Long userId,
                                          @Valid @RequestBody CommentDto commentDto) {
        return itemClient.saveComment(userId, commentDto, itemId);
    }

}
