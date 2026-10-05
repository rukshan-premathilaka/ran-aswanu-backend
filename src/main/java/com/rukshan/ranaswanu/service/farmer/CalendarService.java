package com.rukshan.ranaswanu.service.farmer;

import com.rukshan.ranaswanu.dto.request.farmer.CalendarNoteRequestDto;
import com.rukshan.ranaswanu.dto.response.farmer.CalendarNoteResponseDto;
import com.rukshan.ranaswanu.entities.Reminder;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.ReminderRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;


@Service
public class CalendarService {

    @Autowired private ReminderRepository reminderRepository;
    @Autowired private UserRepository userRepository;

    public CalendarNoteResponseDto getNote(String email, LocalDate date) {
        User user = requireUser(email);
        String note = reminderRepository.findByUserIdAndReminderDate(user.getId(), startOfDay(date)).stream()
                .findFirst()
                .map(Reminder::getDescription)
                .orElse("");
        return new CalendarNoteResponseDto(date, note);
    }

    @Transactional
    public CalendarNoteResponseDto saveNote(String email, LocalDate date, CalendarNoteRequestDto requestData) {
        User user = requireUser(email);
        Instant day = startOfDay(date);

        // create if missing, otherwise update
        Reminder reminder = reminderRepository.findByUserIdAndReminderDate(user.getId(), day).stream()
                .findFirst()
                .orElseGet(() -> {
                    Reminder created = new Reminder();
                    created.setUser(user);
                    created.setReminderDate(day);
                    created.setCreatedAt(Instant.now());
                    return created;
                });
        reminder.setDescription(requestData.getNote().trim());
        reminder.setUpdatedAt(Instant.now());
        reminderRepository.save(reminder);

        return new CalendarNoteResponseDto(date, reminder.getDescription());
    }

    @Transactional
    public void deleteNote(String email, LocalDate date) {
        User user = requireUser(email);
        // nothing to delete is fine (still 204)
        reminderRepository.deleteAll(reminderRepository.findByUserIdAndReminderDate(user.getId(), startOfDay(date)));
    }

    public List<CalendarNoteResponseDto> getMonth(String email, int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if (year < 1900 || year > 2200) {
            throw new IllegalArgumentException("Year must be between 1900 and 2200");
        }
        User user = requireUser(email);

        YearMonth ym = YearMonth.of(year, month);
        Instant start = startOfDay(ym.atDay(1));
        Instant end = startOfDay(ym.plusMonths(1).atDay(1));

        return reminderRepository
                .findByUserIdAndReminderDateGreaterThanEqualAndReminderDateLessThanOrderByReminderDateAsc(
                        user.getId(), start, end)
                .stream()
                .map(r -> new CalendarNoteResponseDto(
                        r.getReminderDate().atZone(ZoneOffset.UTC).toLocalDate(),
                        r.getDescription() == null ? "" : r.getDescription()))
                .toList();
    }

    // The day is always stored as 00:00 UTC, so the same date always finds the same row
    private Instant startOfDay(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
