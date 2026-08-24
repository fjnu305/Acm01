package org.fjnu305.acm01.module.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRolesRequest {

    @NotEmpty
    private List<String> roles;
}
