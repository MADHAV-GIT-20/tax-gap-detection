package com.taxgap.controller;

import com.taxgap.entity.Rule;
import com.taxgap.service.RuleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    /** List all configured rules. */
    @GetMapping
    public List<Rule> list() {
        return ruleService.findAll();
    }

    /** Enable or disable a rule (ADMIN only). Body: {"enabled": true/false}. */
    @PatchMapping("/{id}")
    public Rule toggle(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean enabled = Boolean.TRUE.equals(body.get("enabled"));
        return ruleService.setEnabled(id, enabled);
    }
}
