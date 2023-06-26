package site.bidderown.server.base.event;

import lombok.Builder;
import site.bidderown.server.bounded_context.item.entity.Item;

public class ItemCommentAlarmEvent {
    private Item item;

    @Builder
    public ItemCommentAlarmEvent(Item item) {
        this.item = item;
    }

    public static ItemCommentAlarmEvent of(Item item) {
        return ItemCommentAlarmEvent.builder()
                .item(item)
                .build();
    }
}