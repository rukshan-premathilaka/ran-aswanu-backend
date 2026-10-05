package com.rukshan.ranaswanu.service.farmer;

import com.rukshan.ranaswanu.dto.request.farmer.ExpenseRequestDto;
import com.rukshan.ranaswanu.dto.response.farmer.ExpenseResponseDto;
import com.rukshan.ranaswanu.entities.Expense;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.ExpenseRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ExpenseResponseDto> listMine(String farmerEmail) {
        User farmer = requireUser(farmerEmail);
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(farmer.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    public ExpenseResponseDto create(String farmerEmail, ExpenseRequestDto requestData) {
        User farmer = requireUser(farmerEmail);

        Expense expense = new Expense();
        expense.setUser(farmer);
        expense.setTitle(requestData.getTitle());
        expense.setCategory(requestData.getCategory());
        expense.setAmount(requestData.getAmount());
        expense.setExpenseDate(requestData.getExpenseDate() != null ? requestData.getExpenseDate() : Instant.now());
        expense.setCreatedAt(Instant.now());
        expense.setUpdatedAt(Instant.now());

        expenseRepository.save(expense);
        return toResponseDto(expense);
    }

    // Updates only the logged-in farmer's expense so another farmer's record cannot be changed.
    public ExpenseResponseDto update(String farmerEmail, Long expenseId, ExpenseRequestDto requestData) {
        User farmer = requireUser(farmerEmail);
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, farmer.getId())
                .orElseThrow(() -> new com.rukshan.ranaswanu.exception.ResourceNotFoundException("Expense not found"));

        expense.setTitle(requestData.getTitle());
        expense.setCategory(requestData.getCategory());
        expense.setAmount(requestData.getAmount());
        if (requestData.getExpenseDate() != null) {
            expense.setExpenseDate(requestData.getExpenseDate());
        }
        expense.setUpdatedAt(Instant.now());

        expenseRepository.save(expense);
        return toResponseDto(expense);
    }

    public void delete(String farmerEmail, Long expenseId) {
        User farmer = requireUser(farmerEmail);
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, farmer.getId())
                .orElseThrow(() -> new com.rukshan.ranaswanu.exception.ResourceNotFoundException("Expense not found"));
        expenseRepository.delete(expense);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private ExpenseResponseDto toResponseDto(Expense expense) {
        return ExpenseResponseDto.builder()
                .expenseId(expense.getId())
                .title(expense.getTitle())
                .category(expense.getCategory())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .build();
    }
}