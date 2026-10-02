package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("select m from Message m where m.chat.id = :chatId order by m.sentAt asc")
    List<Message> findAllByChatIdOrderBySentAtAsc(@Param("chatId") Long chatId);

    @Query("select m from Message m where m.chat.id = :chatId order by m.sentAt desc")
    List<Message> findLatestByChatId(@Param("chatId") Long chatId);

    default Optional<Message> findTopByChatIdOrderBySentAtDesc(Long chatId) {
        return findLatestByChatId(chatId).stream().findFirst();
    }
}
