package site.bidderown.server.bounded_context.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.bidderown.server.base.exception.NotFoundException;
import site.bidderown.server.base.util.ImageUtils;
import site.bidderown.server.bounded_context.image.service.ImageService;
import site.bidderown.server.bounded_context.item.controller.dto.ItemRequest;
import site.bidderown.server.bounded_context.item.controller.dto.ItemResponse;
import site.bidderown.server.bounded_context.item.entity.Item;
import site.bidderown.server.bounded_context.item.repository.ItemRepository;
import site.bidderown.server.bounded_context.member.entity.Member;
import site.bidderown.server.bounded_context.member.service.MemberService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final MemberService memberService;
    private final ImageService imageService;
    private final ImageUtils imageUtils;

    public Item create(ItemRequest request, Long memberId) {
        Member member = memberService.getMember(memberId);
        Item item = itemRepository.save(Item.of(request, member));
        List<String> fileNames = imageUtils.uploadMulti(request.getImages(), "items");
        imageService.create(item, fileNames);

        return item;
    }

    public List<ItemResponse> getItems(String description) {
        return itemRepository
                .findAllByDescription(description)
                .stream()
                .map(ItemResponse::of)
                .collect(Collectors.toList());
    }

    public List<ItemResponse> getItems(String title, Pageable pageable) {
        return itemRepository
                .findAllByTitleContaining(title, pageable)
                .stream()
                .map(ItemResponse::of)
                .collect(Collectors.toList());
    }

    public List<ItemResponse> getItems(Long memberId) {
        return itemRepository
                .findAllByMemberId(memberId)
                .stream()
                .map(ItemResponse::of)
                .collect(Collectors.toList());
    }

    public Item getItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id));
    }

    public List<ItemResponse> getAll() {
        return itemRepository
                .findAll()
                .stream()
                .map(ItemResponse::of)
                .collect(Collectors.toList());
    }

    public void delete(Long itemId) {
        Item findItem = itemRepository.findById(itemId)
                        .orElseThrow(() -> new NotFoundException(itemId));
        itemRepository.delete(findItem);
    }

}
