package org.fjnu305.acm01.module.contest.crawl.fetch;

import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

@Component
public class ContestFetchSupport {

    public String computeRawHash(ContestDTO dto) {
        String raw = String.join("|",
                Objects.toString(dto.getSource(), ""),
                Objects.toString(dto.getExternalId(), ""),
                Objects.toString(dto.getTitle(), ""),
                Objects.toString(dto.getStartTime(), ""),
                Objects.toString(dto.getEndTime(), ""),
                Objects.toString(dto.getUrl(), ""),
                Objects.toString(dto.getDifficulty(), "")
        );
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public List<ContestDTO> attachRawHash(List<ContestDTO> contests) {
        if (contests == null || contests.isEmpty()) {
            return Collections.emptyList();
        }
        List<ContestDTO> result = new ArrayList<>(contests.size());
        for (ContestDTO dto : contests) {
            dto.setRawHash(computeRawHash(dto));
            result.add(dto);
        }
        return result;
    }
}
