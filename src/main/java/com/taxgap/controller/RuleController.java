package com.taxgap.controller;

import com.taxgap.dto.RuleToggleRequest;
import com.taxgap.dto.RuleView;
import com.taxgap.service.RuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    /** List all configured rules (enabled and disabled). */
    @GetMapping
    public List<RuleView> list() {
        return ruleService.findAll().stream()
                .map(RuleView::from)
                .toList();
    }

    /** Enable/disable a rule (ADMIN only — enforced in SecurityConfig). */
    @PatchMapping("/{id}")
    public RuleView toggle(@PathVariable Long id, @Valid @RequestBody RuleToggleRequest request) {
        return RuleView.from(ruleService.setEnabled(id, request.enabled()));
    }
}
