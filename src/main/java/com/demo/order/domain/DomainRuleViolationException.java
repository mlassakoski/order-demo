package com.demo.order.domain;

import java.util.Map;

/**
 * An operation the aggregate's current state does not allow. Its message is safe to show to the caller, and
 * {@link #problemProperties()} adds any rule-specific details.
 */
public abstract class DomainRuleViolationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected DomainRuleViolationException(String message) {
        super(message);
    }

    /**
     * Extra properties, safe to show to the caller, that describe this violation. Empty unless a rule adds some.
     * Plain strings only, so no domain object can reach the response.
     */
    public Map<String, String> problemProperties() {
        return Map.of();
    }
}
