package site.bidderown.server.bounded_context.bid.controller.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.bidderown.server.bounded_context.bid.entity.BidResult;
import site.bidderown.server.bounded_context.item.entity.ItemStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BulkInsertBid {

    private int price;
    private Long bidderId;
    private Long itemId;
    private String bidResult;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @Builder
    public BulkInsertBid(int price, Long bidderId, Long itemId) {
        this.price = price;
        this.bidderId = bidderId;
        this.itemId = itemId;
        this.bidResult = String.valueOf(BidResult.WAIT);
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }


}