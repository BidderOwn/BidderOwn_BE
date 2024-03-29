package site.bidderown.server.boundedcontext.bid.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import site.bidderown.server.base.exception.custom_exception.BidEndItemException;
import site.bidderown.server.base.exception.custom_exception.ForbiddenException;
import site.bidderown.server.base.exception.custom_exception.WrongBidPriceException;
import site.bidderown.server.boundedcontext.bid.controller.dto.BidRequest;
import site.bidderown.server.boundedcontext.bid.controller.dto.BidResponse;
import site.bidderown.server.boundedcontext.bid.entity.Bid;
import site.bidderown.server.boundedcontext.bid.repository.BidRepository;
import site.bidderown.server.boundedcontext.image.service.ImageService;
import site.bidderown.server.boundedcontext.item.controller.dto.ItemRequest;
import site.bidderown.server.boundedcontext.item.entity.Item;
import site.bidderown.server.boundedcontext.item.repository.ItemRepository;
import site.bidderown.server.boundedcontext.item.service.ItemRedisService;
import site.bidderown.server.boundedcontext.item.service.ItemService;
import site.bidderown.server.boundedcontext.member.entity.Member;
import site.bidderown.server.boundedcontext.member.service.MemberService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BidServiceTest {
    @Autowired
    BidService bidService;

    @Autowired
    BidRepository bidRepository;

    @Autowired
    ItemService itemService;

    @Autowired
    MemberService memberService;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ItemRedisService itemRedisService;

    @Autowired
    ImageService imageService;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    Member seller;
    Member bidder;
    Item testItem;

    @BeforeEach
    void initData() {
        seller = createUser("seller");
        bidder = createUser("bidder");
        testItem = createItem(seller, "test1", "test1", 10000);
    }

    @Test
    @DisplayName("입찰 생성 1")
    void t01() {
        //given
        Item item = createItem(seller, "test1", "test1", 10000);
        int bidPrice = 11000;

        //when
        Bid bid = handleBid(bidder, item, bidPrice);

        //then
        Bid persistBid = bidService.getBid(bid.getId());
        assertThat(persistBid.getBidder().getName()).isEqualTo(bidder.getName());
    }

    @Test
    @DisplayName("입찰 생성 2")
    void t02() {
        //given
        Item item = createItem(seller, "test1", "test1", 10000);
        int bidPrice = 11000;

        //when
        Bid bid = handleBid(bidder, item, bidPrice);

        //then
        Bid persistBid = bidService.getBid(bid.getId());
        assertThat(bid.getId()).isEqualTo(persistBid.getId());
    }

    /**
     * 자신의 상품에는 입찰을 생성할 수 없습니다.
     * item.member 의 이름과 입찰자의 이름이 같으면 입찰을 할 수 없습니다.
     */
    @Test
    @DisplayName("입찰 생성 예외_자기 상품 입찰 금지")
    void t03() {
        //given
        int price = 10000;
        int bidPrice = 11000;

        Item item = createItem(seller, "test1", "test1", price);

        //when
        Throwable exception = Assertions.assertThrows(
                ForbiddenException.class,
                () -> handleBid(seller, item, bidPrice)
        );

        //then
        assertThat(exception.getMessage().contains("자신의 상품에는 입찰을 할 수 없습니다.")).isTrue();

    }

    /**
     * handleBid()로 전반적인 입찰 과정을 진행합니다.
     */
    @Test
    @DisplayName("입찰 하기")
    void t04() {
        //given
        Item item = createItem(seller, "test1", "test1", 10000);
        int bidPrice = 11000;

        //when
        Bid bid = handleBid(bidder, item, bidPrice);

        //then
        Long bidId = bidRepository.findByItemAndBidder(item, bidder).get().getId();
        assertThat(bid.getId()).isEqualTo(bidId);

    }

    /**
     * 입찰이 존재하는 경우 입찰가격만 업데이트합니다.
     * 원래 bidId와 새로 입찰한 bidId가 동일한지 확인합니다.
     * 동일하면 입찰가격이 새로 업데이트됐는지 확인합니다.
     */
    @Test
    @DisplayName("입찰이 존재하는 경우 입찰가격만 업데이트한다.")
    void t05() {
        //given
        int itemPrice = 10000;
        int originBidPrice = 11000;

        Item item = createItem(seller, "test1", "test1", itemPrice);

        handleBid(bidder, item, originBidPrice);

        //when
        int updatedBidPrice = originBidPrice + (itemPrice / 10);
        Bid bid = handleBid(bidder, item, updatedBidPrice);

        //then
        assertThat(bid.getPrice()).isNotEqualTo(originBidPrice).isEqualTo(updatedBidPrice);
    }

    /**
     * itemStatus가 BID_END 또는 SOLDOUT이면 입찰을 할 수 없습니다.
     * BidEndItemException이 발생합니다.
     */
    @Test
    @DisplayName("상품이 경매종료 or 판매완료 상태일 경우 입찰 불가 예외 처리")
    void t06() {
        //given
        Item item = createItem(seller, "test1", "test1", 10000);

        itemService.soldOut(item.getId(), seller.getName());
        BidRequest bidRequest = BidRequest.of(item.getId(), 10000);

        //when
        Throwable exception = Assertions.assertThrows(
                BidEndItemException.class,
                () -> bidService.handleBid(bidRequest, bidder.getName())
        );

        //then
        assertThat(exception.getMessage().contains("입찰이 종료된 아이템입니다.")).isTrue();
    }

    /**
     * bidId로 입찰을 조회합니다.
     */
    @Test
    @DisplayName("입찰ID로 입찰 조회")
    void t07() {
        //given
        int price = 10000;
        Item item = createItem(seller, "test1", "test1", price);

        int bidPrice = 11000;
        Bid bid = handleBid(bidder, item, bidPrice);

        //when
        Bid bidTest = bidService.getBid(bid.getId());

        //then
        assertThat(bidTest.getId()).isEqualTo(bid.getId());

    }

    /**
     * itemId을 사용하여 item에 있는 모든 입찰을 조회합니다.
     */
    @Test
    @DisplayName("상품ID로 입찰 조회")
    void t08() {
        //given
        Member seller = memberService.getMember("seller");
        Member bidder1 = createUser("bidder1");
        Member bidder2 = createUser("bidder2");

        int price = 10000;
        Item item = createItem(seller, "test1", "test1", price);

        int bidPrice1 = 11000;
        int bidPrice2 = 12000;
        handleBid(bidder1, item, bidPrice1);
        handleBid(bidder2, item, bidPrice2);

        //when
        List<BidResponse> bids = bidService.getBids(item);

        //then
        assertThat(bids.size()).isEqualTo(2);

    }

    /**
     * bidId와 bidderName을 사용하여 입찰을 삭제합니다.
     */
    @Test
    @DisplayName("입찰 삭제")
    void t09() {
        //given
        Member seller = memberService.getMember("seller");
        Member bidder = memberService.getMember("bidder");

        int price = 10000;
        Item item = createItem(seller, "test1", "test1", price);

        int bidPrice = 11000;
        Bid bid = handleBid(bidder, item, bidPrice);

        //when
        bidService.delete(bid.getId(), bidder.getName());

        //then
        Optional<Bid> bidTest = bidRepository.findById(bid.getId());
        assertThat(bidTest).isEmpty();
    }

    /**
     * 권한이 없는 경우 입찰을 삭제할 수 없습니다.
     * bidderName 과 userName이 일치하지 않는 경우
     * ForBiddenException이 발생합니다.
     */
    @Test
    @DisplayName("입찰 삭제 시 권한이 없는 경우 예외 처리")
    void t011() {
        //given
        Member seller = memberService.getMember("seller");
        Member bidder = memberService.getMember("bidder");

        int price = 10000;
        Item item = createItem(seller, "test1", "test1", price);

        int bidPrice = 11000;
        Bid bid = handleBid(bidder, item, bidPrice);

        //when
        Throwable exception = Assertions.assertThrows(
                ForbiddenException.class,
                () -> bidService.delete(bid.getId(), "fakeUser")
        );

        //then
        assertThat(exception.getMessage().contains("입찰 삭제 권한이 없습니다.")).isTrue();

    }

    /**
     * bidderName을 사용하여 입찰자가 진행한 모든 입찰들을 조회합니다.
     */
    @Test
    @DisplayName("유저가 입찰한 상품ID 리스트 조회")
    void t012() {
        //given
        Member seller = memberService.getMember("seller");
        Member bidder = memberService.getMember("bidder");

        int price = 10000;
        Item item1 = createItem(seller, "test1", "test1", price);
        Item item2 = createItem(seller, "test2", "test2", price);
        Item item3 = createItem(seller, "test3", "test3", price);

        int bidPrice = 11000;
        handleBid(bidder, item1, bidPrice);
        handleBid(bidder, item2, bidPrice);
        handleBid(bidder, item3, bidPrice);

        //when
        List<Long> bidItemIds = bidService.getBidItemIds(bidder.getName());

        //then
        assertThat(bidItemIds.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("입찰 생성 시 제시한 가격이 낮은 가격 혹은 같은 가격 400 오류")
    void t013() {
        //given
        Item item = createItem(seller, "test1", "test1", 10000);
        int price = 9000; // 같은 가격

        //when
        Throwable exception = assertThrows(
                WrongBidPriceException.class,
                () -> bidService.handleBid(BidRequest.of(item.getId(), price), bidder.getName())
        );

        assertThat(exception).isInstanceOf(WrongBidPriceException.class);
    }


    Member createUser(String username) {
        return memberService.join(username, "1234");
    }

    Item createItem(Member member, String itemTitle, String itemDescription, Integer minimumPrice) {
        Item item = itemRepository.save(
                Item.of(ItemRequest.builder()
                        .title(itemTitle)
                        .description(itemDescription)
                        .period(3)
                        .minimumPrice(minimumPrice)
                        .build(), member));
        itemRedisService.createWithExpire(item, 3);
        return item;
    }

    Bid handleBid(Member bidder, Item item, int bidPrice) {
        return bidService.handleBid(BidRequest.of(item.getId(), bidPrice), bidder.getName());
    }
}