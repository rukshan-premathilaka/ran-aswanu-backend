package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.dto.request.order.CheckoutRequestDto;
import com.rukshan.ranaswanu.dto.request.order.OrderItemRequestDto;
import com.rukshan.ranaswanu.dto.response.order.*;
import com.rukshan.ranaswanu.entities.Order;
import com.rukshan.ranaswanu.entities.OrderItem;
import com.rukshan.ranaswanu.entities.ProductListing;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.exception.ResourceNotFoundException;
import com.rukshan.ranaswanu.repository.OrderItemRepository;
import com.rukshan.ranaswanu.repository.OrderRepository;
import com.rukshan.ranaswanu.repository.ProductListingRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
public class OrderService {

    // Only these changes are allowed: current status -> next statuses
    private static final Map<String, Set<String>> ALLOWED_CHANGES = Map.of(
            "PENDING", Set.of("ACCEPTED", "REJECTED"),
            "ACCEPTED", Set.of("SHIPPED"),
            "SHIPPED", Set.of("COMPLETED")
    );
    private static final List<String> SETTABLE_STATUSES = List.of("ACCEPTED", "REJECTED", "SHIPPED", "COMPLETED");

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ProductListingRepository productListingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;

    // ---------------- CHECKOUT ----------------

    @Transactional
    public CheckoutResponseDto checkout(String buyerEmail, CheckoutRequestDto request) {
        User buyer = requireAnyRole(buyerEmail, Set.of("BUYER", "FARMER", "TRANSPORT"),
                "Only buyers, farmers or transport users can place orders");


        Map<Long, BigDecimal> wanted = new LinkedHashMap<>();
        for (OrderItemRequestDto item : request.getItems()) {
            BigDecimal qty = item.getQuantity().setScale(2, RoundingMode.HALF_UP); // column is DECIMAL(18,2)
            if (qty.signum() <= 0) {
                throw new IllegalArgumentException("Quantity must be above 0");
            }
            wanted.merge(item.getListId(), qty, BigDecimal::add);
        }


        Map<Long, ProductListing> listings = new HashMap<>();
        for (ProductListing p : productListingRepository.findAllForUpdate(wanted.keySet())) {
            listings.put(p.getId(), p);
        }


        for (Map.Entry<Long, BigDecimal> e : wanted.entrySet()) {
            ProductListing p = listings.get(e.getKey());
            if (p == null || !Boolean.TRUE.equals(p.getListingStatus())
                    || p.isAdminDisabled() || !p.getUser().isActive()) {
                throw new ResourceNotFoundException("Product not found");
            }
            if (e.getValue().compareTo(p.getMinimumOrderQuantity()) < 0) {
                throw new IllegalArgumentException("Minimum order for " + p.getProductName()
                        + " is " + plain(p.getMinimumOrderQuantity()) + " " + p.getUnitOfMeasurement());
            }
            if (e.getValue().compareTo(p.getAvailableStock()) > 0) {
                throw new IllegalArgumentException("Not enough stock for " + p.getProductName()
                        + ". Available: " + plain(p.getAvailableStock()) + " " + p.getUnitOfMeasurement());
            }
        }


        Map<Long, List<ProductListing>> byFarmer = new LinkedHashMap<>();
        for (Long listId : wanted.keySet()) {
            ProductListing p = listings.get(listId);
            byFarmer.computeIfAbsent(p.getUser().getId(), k -> new ArrayList<>()).add(p);
        }

        Instant now = Instant.now();
        List<Order> created = new ArrayList<>();

        for (List<ProductListing> farmerProducts : byFarmer.values()) {

            List<OrderItem> items = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;
            for (ProductListing p : farmerProducts) {
                BigDecimal qty = wanted.get(p.getId());
                BigDecimal subtotal = qty.multiply(p.getPricePerUnit()).setScale(2, RoundingMode.HALF_UP);

                OrderItem item = new OrderItem();
                item.setList(p);
                item.setQuantity(qty);
                item.setUnitPrice(p.getPricePerUnit());
                item.setSubtotal(subtotal);
                item.setCreatedAt(now);
                item.setUpdatedAt(now);
                items.add(item);
                total = total.add(subtotal);

                p.setAvailableStock(p.getAvailableStock().subtract(qty));
                p.setUpdatedAt(now);
                productListingRepository.save(p);
            }

            Order order = new Order();
            order.setUser(buyer);
            order.setOrderDateTime(now);
            order.setOrderStatus("PENDING");
            order.setPaymentStatus("UNPAID");
            order.setTotalAmount(total);
            order.setDeliveryAddress(request.getDeliveryAddress().trim());
            order.setContactNumber(request.getContactNumber().trim());
            order.setPaymentMethod(request.getPaymentMethod());
            order.setNotes(request.getNotes() == null || request.getNotes().isBlank() ? null : request.getNotes().trim());
            order.setCreatedAt(now);
            order.setUpdatedAt(now);
            orderRepository.save(order);

            for (OrderItem item : items) {
                item.setOrder(order);
                orderItemRepository.save(item);
                order.getOrderItems().add(item);
            }
            created.add(order);
        }

        for (Order order : created) {
            User farmer = farmerOf(order);
            notificationService.create(farmer.getId(), "New order",
                    "New order #" + order.getId() + " from " + buyer.getName());
        }

        return new CheckoutResponseDto(created.stream().map(this::toOrderDto).toList());
    }

    // ---------------- LISTS ----------------

    @Transactional(readOnly = true)
    public List<BuyerOrderSummaryDto> listForBuyer(String email) {
        User buyer = requireAnyRole(email, Set.of("BUYER", "FARMER", "TRANSPORT"),
                "Only buyers, farmers or transport users can view purchase history");
        return orderRepository.findByBuyerWithItems(buyer.getId()).stream()
                .map(o -> {
                    List<OrderItem> items = sortedItems(o);
                    User farmer = farmerOf(o);
                    return BuyerOrderSummaryDto.builder()
                            .orderId(o.getId())
                            .farmerId(farmer.getId())
                            .farmerName(farmer.getName())
                            .orderStatus(o.getOrderStatus())
                            .paymentStatus(o.getPaymentStatus())
                            .totalAmount(o.getTotalAmount())
                            .orderDate(o.getOrderDateTime())
                            .itemCount(items.size())
                            .firstItemName(items.isEmpty() ? null : items.get(0).getList().getProductName())
                            .deliveryAddress(o.getDeliveryAddress())
                            .deliveryId(o.getDelivery() == null ? null : o.getDelivery().getId())
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FarmerOrderSummaryDto> listForFarmer(String email) {
        User farmer = requireRole(email, "FARMER", "Only farmers can view farmer orders");
        return orderRepository.findByFarmerWithItems(farmer.getId()).stream()
                .map(o -> {
                    List<OrderItem> items = sortedItems(o);
                    return FarmerOrderSummaryDto.builder()
                            .orderId(o.getId())
                            .buyerName(o.getUser().getName())
                            .contactNumber(o.getContactNumber())
                            .orderStatus(o.getOrderStatus())
                            .paymentStatus(o.getPaymentStatus())
                            .paymentMethod(o.getPaymentMethod())
                            .deliveryAddress(o.getDeliveryAddress())
                            .totalAmount(o.getTotalAmount())
                            .orderDate(o.getOrderDateTime())
                            .itemCount(items.size())
                            .firstItemName(items.isEmpty() ? null : items.get(0).getList().getProductName())
                            .deliveryId(o.getDelivery() == null ? null : o.getDelivery().getId())
                            .build();
                })
                .toList();
    }

    // ---------------- ONE ORDER ----------------

    @Transactional(readOnly = true)
    public OrderResponseDto getById(String email, Long orderId) {
        User me = requireUser(email);
        // the buyer or the farmer of the order may see it; anyone else gets 404
        Order order = orderRepository.findDetailById(orderId)
                .filter(o -> o.getUser().getId().equals(me.getId()) || farmerOf(o).getId().equals(me.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toOrderDto(order);
    }

    // ---------------- STATUS CHANGE (FARMER) ----------------

    @Transactional
    public OrderStatusResponseDto updateStatus(String email, Long orderId, String rawStatus) {
        User farmer = requireRole(email, "FARMER", "Only farmers can update orders");

        String newStatus = rawStatus == null ? "" : rawStatus.trim().toUpperCase();
        if (!SETTABLE_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid status. Allowed values: " + SETTABLE_STATUSES);
        }

        Order order = orderRepository.findFarmerOrderById(orderId, farmer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        String current = order.getOrderStatus();
        if (!ALLOWED_CHANGES.getOrDefault(current, Set.of()).contains(newStatus)) {
            throw new IllegalArgumentException("Cannot change status from " + current + " to " + newStatus);
        }

        order.setOrderStatus(newStatus);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        // Rejected: give the stock back
        if ("REJECTED".equals(newStatus)) {
            for (OrderItem item : order.getOrderItems()) {
                productListingRepository.addStock(item.getList().getId(), item.getQuantity(), Instant.now());
            }
        }

        notificationService.create(order.getUser().getId(),
                "Order " + newStatus.toLowerCase(),
                "Your order #" + order.getId() + " was " + newStatus.toLowerCase() + " by " + farmer.getName() + ".");

        return new OrderStatusResponseDto(order.getId(), newStatus);
    }

    // ---------------- HELPERS ----------------

    // All items of one order belong to the same farmer
    private User farmerOf(Order order) {
        return sortedItems(order).get(0).getList().getUser();
    }

    private List<OrderItem> sortedItems(Order order) {
        return order.getOrderItems().stream()
                .sorted(Comparator.comparing(OrderItem::getId))
                .toList();
    }

    private User requireAnyRole(String email, Set<String> roles, String deniedMessage) {
        User user = requireUser(email);
        boolean allowed = roles.stream().anyMatch(user::hasRole);
        if (!allowed) {
            throw new AccessDeniedException(deniedMessage);
        }
        return user;
    }

    private String plain(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private User requireRole(String email, String role, String deniedMessage) {
        User user = requireUser(email);
        if (!user.hasRole(role)) {
            throw new AccessDeniedException(deniedMessage);
        }
        return user;
    }

    private OrderResponseDto toOrderDto(Order order) {
        User farmer = farmerOf(order);
        List<OrderItemResponseDto> items = sortedItems(order).stream()
                .map(i -> OrderItemResponseDto.builder()
                        .itemId(i.getId())
                        .listId(i.getList().getId())
                        .productName(i.getList().getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getSubtotal())
                        .build())
                .toList();

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .farmerId(farmer.getId())
                .farmerName(farmer.getName())
                .buyerName(order.getUser().getName())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDateTime())
                .deliveryAddress(order.getDeliveryAddress())
                .contactNumber(order.getContactNumber())
                .paymentMethod(order.getPaymentMethod())
                .notes(order.getNotes())
                .items(items)
                .build();
    }
}
