package site.bidderown.server.bounded_context.item.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import site.bidderown.server.base.exception.custom_exception.NotFoundException;
import site.bidderown.server.bounded_context.item.controller.dto.ItemsResponse;
import site.bidderown.server.bounded_context.item.entity.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static site.bidderown.server.bounded_context.bid.entity.QBid.bid;
import static site.bidderown.server.bounded_context.item.entity.QItem.item;

@RequiredArgsConstructor
@Repository
public class ItemCustomRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 성능 테스트를 위한 메서드입니다.
     */
    public List<Item> findItems__v1(int sortCode, String searchText, Pageable pageable) {
        return queryFactory.selectFrom(item)
                .where(
                        eqToSearchText(searchText),
                        eqNotDeleted()
                )
                .orderBy(orderBySortCode(sortCode))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     * 성능 테스트를 위한 메서드입니다.
     */
    public List<Item> findItems__v2(Long lastItemId, int sortCode, String searchText, Pageable pageable) {
        return queryFactory
                .selectFrom(item)
                .where(
                        eqNotDeleted(),
                        betweenItemId(lastItemId, pageable.getPageSize()),
                        eqToSearchText(searchText)
                )
                .orderBy(orderBySortCode(sortCode))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     * @param sortCode   정렬 기준, 1: 최신순 / 2: 인기순 / 3: 경매 마감순
     * @param searchText 검색어(제목, 내용, 작성자)
     * @param pageable   페이징: 9
     * @return 홈화면에 보여질 아이템 리스트
     */
    public List<ItemsResponse> findItems(Long lastItemId, int sortCode, String searchText, Pageable pageable) {
        return queryFactory
                .select(
                        Projections.constructor(
                                ItemsResponse.class,
                                item.id,
                                item.title,
                                item.minimumPrice,
                                item.thumbnailImageFileName,
                                item.itemStatus,
                                item.expireAt
                        )
                )
                .from(item)
                .where(
                        eqNotDeleted(),
                        betweenItemId(lastItemId, pageable.getPageSize()),
                        eqToSearchText(searchText)
                )
                .orderBy(orderBySortCode(sortCode))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     * @param id 상품 아이디
     * @return Optional로 받아서 404 처리
     * @description 상품 ItemDetailResponse로 받음
     */
    public Item findItemById(Long id) {
        return Optional.ofNullable(queryFactory.select(item)
                .from(item)
                .where(item.id.eq(id), eqNotDeleted())
                .fetchOne())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다.", id + ""));
    }

    public Integer findItemBidMaxPriceByItemId(Long itemId) {
        return queryFactory.select(bid.price)
                .from(bid)
                .where(bid.item.id.eq(itemId))
                .orderBy(bid.price.desc())
                .fetchFirst();
    }

    public Integer findItemBidMinPriceByItemId(Long itemId) {
        return queryFactory.select(bid.price)
                .from(bid)
                .where(bid.item.id.eq(itemId))
                .orderBy(bid.price.asc())
                .fetchFirst();
    }

    public Integer findItemBidMaxPriceByItemId__v1(Long itemId) {
        return queryFactory.select(bid.price.max())
                .from(bid)
                .where(bid.item.id.eq(itemId))
                .fetchOne();
    }

    public Integer findItemBidMinPriceByItemId__v1(Long itemId) {
        return queryFactory.select(bid.price.min())
                .from(bid)
                .where(bid.item.id.eq(itemId))
                .fetchOne();
    }

    private BooleanExpression betweenItemId(Long itemId, int size) {
        if (itemId == null) {
            return null;
        }
        return item.id.lt(itemId).and(item.id.gt(itemId - size - 1));
    }

    private OrderSpecifier<?>[] orderBySortCode(int sortCode) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        switch (sortCode) {
            case 2 -> orderSpecifiers.add(item.bids.size().desc());
            case 3 -> orderSpecifiers.add(item.expireAt.asc());
        }
        orderSpecifiers.add(item.id.desc());
        orderSpecifiers.add(item.itemStatus.desc());
        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

    private BooleanExpression eqToSearchText(String searchText) {
        if (StringUtils.isEmpty(searchText)) return null;
        StringExpression keywordExpression = Expressions.asString("%" + searchText + "%");
        return eqToTitle(keywordExpression)
                .or(eqToDescription(keywordExpression))
                .or(eqToSeller(keywordExpression));
    }

    private BooleanExpression eqNotDeleted() {
        return item.deleted.eq(false);
    }

    private BooleanExpression eqToTitle(StringExpression searchText) {
        return item.title.like(searchText);
    }

    private BooleanExpression eqToDescription(StringExpression searchText) {
        return item.description.like(searchText);
    }

    private BooleanExpression eqToSeller(StringExpression searchText) {
        return item.member.name.like(searchText);
    }
}
