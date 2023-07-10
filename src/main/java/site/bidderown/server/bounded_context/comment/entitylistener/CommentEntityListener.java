package site.bidderown.server.bounded_context.comment.entitylistener;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.bidderown.server.base.util.BeanUtils;
import site.bidderown.server.bounded_context.comment.entity.Comment;
import site.bidderown.server.bounded_context.item.buffer.ItemCountBuffer;

import javax.persistence.PostPersist;

/**
 * Comment Entity 추가 이벤트 리스너
 */

@Slf4j
@NoArgsConstructor
public class CommentEntityListener {

    @PostPersist
    public void postPersist(Comment comment) {
        ItemCountBuffer itemCountBuffer = BeanUtils.getBean(ItemCountBuffer.class);
        itemCountBuffer.push(CommentCountTask.of(comment.getItem().getId()));
    }
}