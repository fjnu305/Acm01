package org.fjnu305.acm01.module.team.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeamPublishRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    @Size(max = 2000)
    private String description;

    @Min(0)
    private Integer ratingMin = 0;

    @Min(0)
    @Max(5000)
    private Integer ratingMax = 4000;

    @Size(max = 100)
    private String region;

    @Size(max = 500)
    private String tags;

    @Min(2)
    @Max(10)
    private Integer memberLimit = 3;
}
