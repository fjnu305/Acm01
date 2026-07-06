package org.fjnu305.acm01.Common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContestSource {

    CODEFORCES("codeforces"),
    ATCODER("atcoder"),
    NOWCODER("nowcoder"),
    LUOGU("luogu"),
    CCPC("ccpc"),
    ICPC("icpc"),
    LANQIAO("lanqiao");

    private final String value;
}
