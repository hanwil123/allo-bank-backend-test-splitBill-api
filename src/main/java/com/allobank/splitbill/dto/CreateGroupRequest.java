package com.allobank.splitbill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateGroupRequest(
        @NotBlank(message = "name must not be empty") String name,
        @NotEmpty(message = "participants must not be empty") List<@NotBlank String> participants
) {
}
