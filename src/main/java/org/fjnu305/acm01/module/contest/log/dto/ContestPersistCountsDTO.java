package org.fjnu305.acm01.module.contest.log.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContestPersistCountsDTO {

    private int fetchedCount;
    private int insertedCount;
    private int updatedCount;
    private int skippedCount;
    private int ignoredCount;

    public static ContestPersistCountsDTO empty() {
        return ContestPersistCountsDTO.builder().build();
    }
}
