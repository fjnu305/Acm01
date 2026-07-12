package org.fjnu305.acm01.module.notify.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifyDeliveryMessage implements Serializable {

    private Long userId;
    private List<Long> taskIds;
}
