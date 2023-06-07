package site.bidderown.server.bounded_context.bid.service;


import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import site.bidderown.server.base.event.EventItemNotification;
import site.bidderown.server.bounded_context.bid.controller.dto.BidRequest;
import site.bidderown.server.bounded_context.bid.controller.dto.BidResponse;
import site.bidderown.server.bounded_context.bid.entity.Bid;
import site.bidderown.server.bounded_context.bid.repository.BidRepository;
import site.bidderown.server.bounded_context.item.entity.Item;
import site.bidderown.server.bounded_context.item.service.ItemService;
import site.bidderown.server.bounded_context.member.entity.Member;
import site.bidderown.server.bounded_context.member.service.MemberService;
import site.bidderown.server.bounded_context.notification.entity.NotificationType;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidService {
    private final BidRepository bidRepository;
    private final MemberService memberService;
    private final ItemService itemService;
    private final ApplicationEventPublisher publisher;

    public void create(BidRequest bidRequest, String username) {
        Item item = itemService.getItem(bidRequest.getItemId());
        Member member = memberService.getMember(username);
        Bid bid = Bid.of(bidRequest, member, item);
        bidRepository.save(bid);

        publisher.publishEvent(
                EventItemNotification.of(item, member, NotificationType.BID)
        );
    }

    public List<BidResponse> getBids(Long itemId) {
        Item item = itemService.getItem(itemId);
        return bidRepository.findByItem(item).stream()
                .map(bid -> BidResponse.of(bid, item)).collect(Collectors.toList());
    }

    public void clear() {
        bidRepository.deleteAll();
    }
}
