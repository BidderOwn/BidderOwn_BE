package site.bidderown.server.boundedcontext.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import site.bidderown.server.boundedcontext.notification.controller.dto.NewBidNotificationRequest;
import site.bidderown.server.boundedcontext.notification.controller.dto.NewCommentNotificationRequest;
import site.bidderown.server.boundedcontext.notification.controller.dto.SoldOutNotificationRequest;
import site.bidderown.server.boundedcontext.notification.service.NotificationService;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    public String notificationList() {
        return "usr/notification/list";
    }

    @MessageMapping("/notification/new-bid")
    public void noticeNewBid(NewBidNotificationRequest newBidNotificationRequest) {
        notificationService.createNewBidNotification(newBidNotificationRequest);
    }

    @MessageMapping("/notification/new-comment")
    public void noticeNewComment(NewCommentNotificationRequest newCommentNotificationRequest) {
        notificationService.createNewCommentNotification(newCommentNotificationRequest.getItemId(), newCommentNotificationRequest.getWriterName());
    }

    @MessageMapping("/notification/sold-out")
    public void noticeBidEnd(SoldOutNotificationRequest soldOutNotificationRequest) {
        notificationService.createSoldOutNotification(soldOutNotificationRequest.getItemId());
    }
}