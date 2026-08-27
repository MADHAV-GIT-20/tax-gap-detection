package com.taxgap.repository;

import com.taxgap.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Long> {

    List<Rule> findByEnabledTrue();

    boolean existsByRuleName(String ruleName);
}
