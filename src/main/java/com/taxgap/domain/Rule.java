package com.taxgap.domain;

import com.taxgap.domain.enums.RuleType;
import com.taxgap.domain.enums.Severity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Database-driven, configurable compliance rule.
 * The behaviour is chosen by {@link #ruleType}; parameters live in {@link #configJson}.
 */
@Entity
@Table(name = "rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType ruleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private boolean enabled;

    /** JSON configuration, e.g. {"threshold": 100000}. */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String configJson;

    private String description;
}
