package com.taxgap.config;

import com.taxgap.domain.AppUser;
import com.taxgap.domain.Rule;
import com.taxgap.domain.enums.RuleType;
import com.taxgap.domain.enums.Severity;
import com.taxgap.repository.AppUserRepository;
import com.taxgap.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the User table (prefilled auth data) and the three mandatory rules
 * on startup, only if they are not already present.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final RuleRepository ruleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedRules();
    }

    private void seedUsers() {
        createUserIfMissing("admin", "admin123", "ADMIN");
        createUserIfMissing("auditor", "auditor123", "AUDITOR");
    }

    private void createUserIfMissing(String username, String rawPassword, String role) {
        if (!appUserRepository.existsByUsername(username)) {
            appUserRepository.save(AppUser.builder()
                    .username(username)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .enabled(true)
                    .build());
            log.info("Seeded user '{}' with role {}", username, role);
        }
    }

    private void seedRules() {
        createRuleIfMissing(Rule.builder()
                .ruleName("HIGH_VALUE_TRANSACTION")
                .ruleType(RuleType.HIGH_VALUE)
                .severity(Severity.HIGH)
                .enabled(true)
                .configJson("{\"threshold\": 100000}")
                .description("Flags transactions whose amount exceeds the threshold.")
                .build());

        createRuleIfMissing(Rule.builder()
                .ruleName("REFUND_EXCEEDS_SALES")
                .ruleType(RuleType.REFUND_VALIDATION)
                .severity(Severity.MEDIUM)
                .enabled(true)
                .configJson("{}")
                .description("Refund amount must not exceed the customer's total sales.")
                .build());

        createRuleIfMissing(Rule.builder()
                .ruleName("GST_SLAB_VIOLATION")
                .ruleType(RuleType.GST_SLAB_VIOLATION)
                .severity(Severity.HIGH)
                .enabled(true)
                .configJson("{\"slabThreshold\": 50000, \"requiredRate\": 0.18}")
                .description("Amount over slab threshold but taxRate below required slab rate.")
                .build());
    }

    private void createRuleIfMissing(Rule rule) {
        if (!ruleRepository.existsByRuleName(rule.getRuleName())) {
            ruleRepository.save(rule);
            log.info("Seeded rule '{}'", rule.getRuleName());
        }
    }
}
