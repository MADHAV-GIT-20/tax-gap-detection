package com.taxgap.config;

import com.taxgap.entity.AppUser;
import com.taxgap.entity.Rule;
import com.taxgap.enums.RuleType;
import com.taxgap.enums.Severity;
import com.taxgap.repository.AppUserRepository;
import com.taxgap.repository.RuleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inserts the prefilled users and the three mandatory rules on startup,
 * only if they do not already exist.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final RuleRepository ruleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppUserRepository appUserRepository,
                      RuleRepository ruleRepository,
                      PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.ruleRepository = ruleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUser("admin", "admin123", "ADMIN");
        seedUser("auditor", "auditor123", "AUDITOR");

        seedRule("HIGH_VALUE_TRANSACTION", RuleType.HIGH_VALUE, Severity.HIGH,
                "{\"threshold\": 100000}",
                "Flags transactions whose amount exceeds the threshold.");
        seedRule("REFUND_EXCEEDS_SALES", RuleType.REFUND_VALIDATION, Severity.MEDIUM,
                "{}",
                "Refund amount must not exceed the customer's total sales.");
        seedRule("GST_SLAB_VIOLATION", RuleType.GST_SLAB_VIOLATION, Severity.HIGH,
                "{\"slabThreshold\": 50000, \"requiredRate\": 0.18}",
                "Amount over slab threshold but tax rate below the required slab rate.");
    }

    private void seedUser(String username, String rawPassword, String role) {
        if (!appUserRepository.existsByUsername(username)) {
            AppUser user = new AppUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setEnabled(true);
            appUserRepository.save(user);
        }
    }

    private void seedRule(String name, RuleType type, Severity severity, String configJson, String description) {
        if (!ruleRepository.existsByRuleName(name)) {
            Rule rule = new Rule();
            rule.setRuleName(name);
            rule.setRuleType(type);
            rule.setSeverity(severity);
            rule.setEnabled(true);
            rule.setConfigJson(configJson);
            rule.setDescription(description);
            ruleRepository.save(rule);
        }
    }
}
