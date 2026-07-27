package com.rukshan.ranaswanu.service.farmer;

import com.rukshan.ranaswanu.dto.request.farmer.ExpenseRequestDto;
import com.rukshan.ranaswanu.dto.response.farmer.ExpenseResponseDto;
import com.rukshan.ranaswanu.entities.Expens;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.ExpensRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    private ExpensRepository expensRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ExpenseResponseDto> listMine(String farmerEmail) {
        User farmer = requireUser(farmerEmail);
        return expensRepository.findByUserIdOrderByExpenseDateDesc(farmer.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    public ExpenseResponseDto create(String farmerEmail, ExpenseRequestDto requestData) {
        User farmer = requireUser(farmerEmail);

        Expens expense = new Expens();
        expense.setUser(farmer);
        expense.setTitle(requestData.getTitle());
        expense.setCategory(requestData.getCategory());
        expense.setAmount(requestData.getAmount());
        expense.setExpenseDate(requestData.getExpenseDate() != null ? requestData.getExpenseDate() : Instant.now());
        expense.setCreatedAt(Instant.now());
        expense.setUpdatedAt(Instant.now());

        expensRepository.save(expense);
        return toResponseDto(expense);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private ExpenseResponseDto toResponseDto(Expens expense) {
        return ExpenseResponseDto.builder()
                .expenseId(expense.getId())
                .title(expense.getTitle())
                .category(expense.getCategory())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .build();
    }
}