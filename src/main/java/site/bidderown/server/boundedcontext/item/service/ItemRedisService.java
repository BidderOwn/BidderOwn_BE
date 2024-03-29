package site.bidderown.server.boundedcontext.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import site.bidderown.server.boundedcontext.item.entity.Item;
import site.bidderown.server.boundedcontext.item.repository.ItemRedisRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ItemRedisService {

    private final ItemRedisRepository itemRedisRepository;

    public boolean containsKey(Item item) {
        return itemRedisRepository.contains(item.getId());
    }

    public void createWithExpire(Item item, int expire) {
        itemRedisRepository.save(item.getId(), expire);
    }

    public List<Long> getItemIdsByRanking(Pageable pageable) {
        return itemRedisRepository.getBidRankingRange(pageable);
    }

    public void removeBidRankingKey(Long itemId) {
        itemRedisRepository.removeBidRankingKey(itemId);
    }

    public void increaseBidScore(Long itemId) {
        itemRedisRepository.increaseScore(itemId, 1);
    }

    public void decreaseBidScore(Long itemId) {
        itemRedisRepository.decreaseScore(itemId, -1);
    }

    /**
     * 테스트 코드를 위한 메스드입니다.
     */
    public void flushBidRanking() {
        itemRedisRepository.flushBidRanking();
    }
}
