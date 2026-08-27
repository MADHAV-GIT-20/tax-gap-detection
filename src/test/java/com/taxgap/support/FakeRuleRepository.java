package com.taxgap.support;

import com.taxgap.entity.Rule;
import com.taxgap.repository.RuleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Simple in-memory fake of RuleRepository for tests. */
public class FakeRuleRepository extends FakeJpaRepository<Rule, Long> implements RuleRepository {

    private final List<Rule> store = new ArrayList<>();
    private long nextId = 1;

    /** Add a rule to the fake store (returns it for convenience). */
    public Rule add(Rule rule) {
        if (rule.getId() == null) {
            rule.setId(nextId++);
        }
        store.add(rule);
        return rule;
    }

    @Override
    public List<Rule> findByEnabledTrue() {
        List<Rule> result = new ArrayList<>();
        for (Rule r : store) {
            if (r.isEnabled()) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public boolean existsByRuleName(String ruleName) {
        return store.stream().anyMatch(r -> ruleName.equals(r.getRuleName()));
    }

    @Override
    public List<Rule> findAll() {
        return new ArrayList<>(store);
    }

    @Override
    public Optional<Rule> findById(Long id) {
        return store.stream().filter(r -> id.equals(r.getId())).findFirst();
    }

    @Override
    public <S extends Rule> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId(nextId++);
        }
        return entity;
    }
}
