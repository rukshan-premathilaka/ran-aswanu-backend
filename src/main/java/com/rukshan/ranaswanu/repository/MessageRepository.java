package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}