package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(min = 6, max = 254, message = "Email must be between 6 and 254 characters")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 250, message = "Name must be between 2 and 250 characters")
    @JsonProperty("name")
    private String name;
}