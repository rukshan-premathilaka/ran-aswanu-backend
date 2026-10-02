package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Buyer: my orders, newest first (items, products and farmers loaded in one query)
    @Query("select o from Order o join fetch o.orderItems i join fetch i.list l join fetch l.user " +
           "where o.user.id = :buyerId order by o.orderDateTime desc")
    List<Order> findByBuyerWithItems(@Param("buyerId") Long buyerId);

    // Farmer: orders that contain at least one of this farmer's products
    @Query("select o from Order o join fetch o.orderItems i join fetch i.list l join fetch l.user " +
           "where o.id in (select o2.id from Order o2 join o2.orderItems i2 where i2.list.user.id = :farmerId) " +
           "order by o.orderDateTime desc")
    List<Order> findByFarmerWithItems(@Param("farmerId") Long farmerId);

    @Query("select o from Order o join fetch o.orderItems i join fetch i.list l join fetch l.user " +
           "where o.id = :orderId")
    Optional<Order> findDetailById(@Param("orderId") Long orderId);

    // Same as above, but only if the order belongs to this farmer
    @Query("select o from Order o join fetch o.orderItems i join fetch i.list l join fetch l.user " +
           "where o.id = :orderId " +
           "and o.id in (select o2.id from Order o2 join o2.orderItems i2 where i2.list.user.id = :farmerId)")
    Optional<Order> findFarmerOrderById(@Param("orderId") Long orderId, @Param("farmerId") Long farmerId);

    // Dashboard: how many orders of this farmer have the given status
    @Query("select count(distinct o.id) from Order o join o.orderItems i " +
           "where i.list.user.id = :farmerId and o.orderStatus = :status")
    long countByFarmerAndStatus(@Param("farmerId") Long farmerId, @Param("status") String status);
}
