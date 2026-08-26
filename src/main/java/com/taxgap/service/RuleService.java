package com.taxgap.service;

import com.taxgap.domain.Rule;
import com.taxgap.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;

    @Transactional(readOnly = true)
    public List<Rule> findAll() {
        return ruleRepository.findAll();
    }

    @Transactional
    public Rule setEnabled(Long id, boolean enabled) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rule not found: " + id));
        rule.setEnabled(enabled);
        return ruleRepository.save(rule);
    }
}
