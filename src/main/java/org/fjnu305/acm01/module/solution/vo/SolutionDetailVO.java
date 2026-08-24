package org.fjnu305.acm01.module.solution.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SolutionDetailVO extends SolutionVO {

    private String content;
    private Integer status;
}
