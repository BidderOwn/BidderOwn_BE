package site.bidderown.server.bounded_context.bid.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import site.bidderown.server.base.exception.custom_exception.ForbiddenException;
import site.bidderown.server.bounded_context.bid.controller.dto.BidDetails;
import site.bidderown.server.bounded_context.bid.controller.dto.BidRequest;
import site.bidderown.server.bounded_context.bid.controller.dto.BidResponse;
import site.bidderown.server.bounded_context.bid.controller.dto.BidResponses;
import site.bidderown.server.bounded_context.bid.entity.Bid;
import site.bidderown.server.bounded_context.bid.service.BidService;
import site.bidderown.server.bounded_context.item.service.ItemService;
import site.bidderown.server.bounded_context.member.controller.dto.MemberDetail;
import site.bidderown.server.bounded_context.member.service.MemberService;

import java.util.List;

@RestController
@Tag(name = "입찰 bid-api-controller", description = "입찰 관련 api 입니다.")
@RequiredArgsConstructor
public class BidApiController {

    private final BidService bidService;
    private final MemberService memberService;

    @Operation(summary = "입찰 등록", description = "원하는 상품에 입찰가를 제시합니다.")
    @PostMapping("/api/v1/bid")
    public Long registerBid(@RequestBody BidRequest bidRequest, @AuthenticationPrincipal User user){
        if(user == null)
            throw new ForbiddenException("로그인 후 접근이 가능합니다.");
        return bidService.handleBid(bidRequest, user.getUsername());
    }

    @Operation(summary = "입찰 목록", description = "상품의 id를 통해 입찰 목록을 보여줍니다.")
    @GetMapping("/api/v1/bid/list")
    public BidResponses bidListApi(@RequestParam Long itemId){
        BidDetails bidDetails = bidService.getBidStatistics(itemId);
        List<BidResponse> bids = bidService.getBids(itemId);
        return BidResponses.of(bidDetails, bids);
    }

    @Operation(summary = "입찰 취소", description = "입찰을 취소하고 싶을 때 입찰 id와 유저이름을 비교해 취소합니다.")
    @DeleteMapping("/api/v1/bid/{bidId}")
    @PreAuthorize("isAuthenticated()")
    public void deleteBid(@PathVariable Long bidId, @AuthenticationPrincipal User user) {
        bidService.delete(bidId, user.getUsername());
    }
}

