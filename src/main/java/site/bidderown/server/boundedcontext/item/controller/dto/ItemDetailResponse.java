package site.bidderown.server.boundedcontext.item.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import site.bidderown.server.boundedcontext.item.entity.ItemStatus;

import java.time.LocalDateTime;

@Schema(description = "상품상세 응답")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemDetailResponse {
    @Schema(description = "상품ID")
    private Long id;
    @Schema(description = "판매자ID")
    private Long sellerId;
    @Schema(description = "제목")
    private String title;
    @Schema(description = "설명")
    private String description;
    @Schema(description = "판매자이름")
    private String memberName;
    @Schema(description = "최소희망가격")
    private int minimumPrice;
    @Schema(description = "최고가격")
    private Integer maxPrice;
    @Schema(description = "최저가격")
    private Integer minPrice;
    @Schema(description = "썸네일사진 이름")
    private String thumbnailImageName;
    @Schema(description = "입찰수")
    private int bidCount;
    @Schema(description = "댓글수")
    private int commentCount;
    @Schema(description = "좋아요수")
    private int heartCount;
    @Schema(description = "상품 상태", allowableValues = {"BIDDING","SOLDOUT","BID_END"})
    private ItemStatus itemStatus;
    @Schema(description = "만료일자")
    private LocalDateTime expireAt;

    @Builder
    public ItemDetailResponse(
            Long id,
            Long sellerId,
            String title,
            String description,
            String memberName,
            int minimumPrice,
            String thumbnailImageName,
            int bidCount,
            int commentCount,
            int heartCount,
            ItemStatus itemStatus,
            LocalDateTime expireAt
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.memberName = memberName;
        this.minimumPrice = minimumPrice;
        this.thumbnailImageName = thumbnailImageName;
        this.bidCount = bidCount;
        this.commentCount = commentCount;
        this.heartCount = heartCount;
        this.itemStatus = itemStatus;
        this.expireAt = expireAt;
    }
}
