package com.pcd.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {

    // Administrator Permissions
    ADMIN_READ("admin:read"),
    ADMIN_CREATE("admin:create"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_DELETE("admin:delete"),

    // Expert Permissions
    EXPERT_UPLOAD("expert:upload"),
    EXPERT_ANALYZE("expert:analyze"),
    EXPERT_REPORT("expert:report"),
    EXPERT_ANNOTATE("expert:annotate"),

    // Investigator Permissions
    INVESTIGATOR_SUBMIT("investigator:submit"),
    INVESTIGATOR_READ("investigator:read"),

    // Lawyer Permissions
    LAWYER_SUBMIT("lawyer:submit"),
    LAWYER_READ("lawyer:read"),
    LAWYER_EXPORT("lawyer:export"),

    // Judge Permissions
    JUDGE_READ("judge:read"),
    JUDGE_HISTORY("judge:history");

    @Getter
    private final String permission;
}
