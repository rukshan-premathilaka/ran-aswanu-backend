package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("select c from Chat c where c.userOne.id = :firstId and c.userTwo.id = :secondId")
    Optional<Chat> findByUserPair(@Param("firstId") Long firstId, @Param("secondId") Long secondId);

    @Query("select c from Chat c where c.userOne.id = :userId or c.userTwo.id = :userId order by c.updatedAt desc")
    List<Chat> findAllForUser(@Param("userId") Long userId);


    @Query("select count(c) > 0 from Chat c where c.id = :chatId and (c.userOne.id = :userId or c.userTwo.id = :userId)")
    boolean isMember(@Param("chatId") Long chatId, @Param("userId") Long userId);
}
