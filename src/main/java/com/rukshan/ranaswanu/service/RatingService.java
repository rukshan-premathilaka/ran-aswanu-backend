package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.dto.request.rating.RatingRequestDto;
import com.rukshan.ranaswanu.dto.response.rating.RatingResponseDto;
import com.rukshan.ranaswanu.dto.response.rating.ReviewItemDto;
import com.rukshan.ranaswanu.dto.response.rating.UserReviewsResponseDto;
import com.rukshan.ranaswanu.entities.CustomerReview;
import com.rukshan.ranaswanu.entities.Order;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.exception.ConflictException;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.CustomerReviewRepository;
import com.rukshan.ranaswanu.repository.OrderRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

@Service
public class RatingService {

    @Autowired private CustomerReviewRepository reviewRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;

    // ---------------- SUBMIT (BUYER) ----------------

    @Transactional
    public RatingResponseDto submit(String email, Long orderId, RatingRequestDto request) {
        User me = requireUser(email);


        Order order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));


        if (!order.getUser().getId().equals(me.getId())) {
            throw new AccessDeniedException("Only the buyer of this order can rate it");
        }

        if (!"COMPLETED".equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("You can only rate a completed order");
        }

        if (reviewRepository.existsByOrderId(orderId)) {
            throw new ConflictException("You already rated this order");
        }

        User farmer = farmerOf(order);
        Instant now = Instant.now();

        CustomerReview review = new CustomerReview();
        review.setUser(farmer);
        review.setReviewer(me);
        review.setOrder(order);
        review.setRating(request.getScore());
        review.setDescription(request.getComment() == null ? "" : request.getComment().trim());
        review.setReviewDate(now);
        review.setCreatedAt(now);
        review.setUpdatedAt(now);

        try {

            reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("You already rated this order");
        }

        notificationService.create(farmer.getId(), "New rating",
                me.getName() + " rated order #" + order.getId() + ": " + request.getScore() + "/5");

        return RatingResponseDto.builder()
                .ratingId(review.getId())
                .orderId(order.getId())
                .revieweeId(farmer.getId())
                .score(review.getRating())
                .comment(review.getDescription())
                .submittedAt(now)
                .build();
    }

    // ---------------- LIST FOR A USER ----------------

    @Transactional(readOnly = true)
    public UserReviewsResponseDto listForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        List<CustomerReview> reviews = reviewRepository.findForUser(userId);

        double average = reviews.stream().mapToInt(CustomerReview::getRating).average().orElse(0.0);
        double rounded = BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP).doubleValue();

        List<ReviewItemDto> items = reviews.stream()
                .map(r -> ReviewItemDto.builder()
                        .ratingId(r.getId())
                        .orderId(r.getOrder() == null ? null : r.getOrder().getId())
                        .reviewerName(r.getReviewer() == null ? "Anonymous" : r.getReviewer().getName())
                        .score(r.getRating())
                        .comment(r.getDescription())
                        .date(r.getReviewDate() == null ? null
                                : r.getReviewDate().atZone(ZoneOffset.UTC).toLocalDate().toString())
                        .build())
                .toList();

        return UserReviewsResponseDto.builder()
                .userId(userId)
                .averageRating(rounded)
                .totalReviews(items.size())
                .reviews(items)
                .build();
    }

    // ---------------- HELPERS ----------------

    // All items of one order belong to the same farmer (checkout makes one order per farmer)
    private User farmerOf(Order order) {
        return order.getOrderItems().stream()
                .min(Comparator.comparing(i -> i.getId()))
                .orElseThrow(() -> new IllegalStateException("Order " + order.getId() + " has no items"))
                .getList().getUser();
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
