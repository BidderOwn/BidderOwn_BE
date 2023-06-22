package site.bidderown.server.bounded_context.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import site.bidderown.server.bounded_context.comment.controller.dto.CommentDetailResponse;
import site.bidderown.server.bounded_context.comment.controller.dto.CommentRequest;
import site.bidderown.server.bounded_context.comment.controller.dto.CommentResponse;
import site.bidderown.server.bounded_context.comment.service.CommentService;
import site.bidderown.server.bounded_context.member.service.MemberService;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/item/")
public class CommentApiController {
    private final CommentService commentService;

    @PostMapping("/{itemId}/comment")
    @PreAuthorize("isAuthenticated()")
    public CommentResponse createComment(
            @RequestBody CommentRequest request,
            @PathVariable Long itemId,
            @AuthenticationPrincipal User user
    ) {
        return CommentResponse.of(commentService.create(request, itemId, user.getUsername()));
    }

    @GetMapping("/{id}/comments")
    public List<CommentDetailResponse> getCommentList(@PathVariable Long id) {
        return commentService.getComments(id);
    }

    @DeleteMapping("/{id}/comment")
    @PreAuthorize("isAuthenticated()")
    public void deleteComment(@PathVariable("id") Long commentId) {
        commentService.delete(commentId);
    }

    @PutMapping("/{id}/comment")
    @PreAuthorize("isAuthenticated()")
    public CommentResponse updateComment(
            @PathVariable Long id,
            @RequestBody @Valid CommentRequest commentRequest
    ){
        return commentService.update(id, commentRequest);
    }
}
