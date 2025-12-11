package ru.practicum.ewm.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EventDateValidator.class)
@Documented
public @interface EventDateConstraint {
    String message() default "Event date must be at least 2 hours from now";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}