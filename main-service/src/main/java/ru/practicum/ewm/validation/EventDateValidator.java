package ru.practicum.ewm.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class EventDateValidator implements ConstraintValidator<EventDateConstraint, LocalDateTime> {

    @Override
    public boolean isValid(LocalDateTime eventDate, ConstraintValidatorContext context) {
        if (eventDate == null) {
            return false;
        }
        return eventDate.isAfter(LocalDateTime.now().plusHours(2));
    }
}