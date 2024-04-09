package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemServiceImpl itemServiceImpl;
    public static final String OWNER_ID = "X-Sharer-User-Id";

    @PostMapping //добавление вещи;
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
    public ItemDto getBtId(@PathVariable Long itemId) {
        log.info("Get item id{}", itemId);
        ItemDto item = itemServiceImpl.getById(itemId);
        return item;
    }

    @GetMapping
    public Collection<ItemDto> getByUser(@RequestHeader(OWNER_ID) Long id) {
        log.info("Get items user id{}", id);
        return itemServiceImpl.getByUser(id);
    }

    @GetMapping("/search")
    public List<ItemDto> getBySearch(@RequestParam String text) {
        log.info("search {}, text");
        return itemServiceImpl.getBySearch(text);
    }
}
