package com.taxgap.service.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

/**
 * Small helper for pulling typed values out of a rule's JSON configuration.
 */
final class RuleConfigReader {

    private RuleConfigReader() {
    }

    static BigDecimal readBigDecimal(ObjectMapper mapper, String configJson, String field) {
        JsonNode node = read(mapper, configJson);
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).decimalValue();
    }

    private static JsonNode read(ObjectMapper mapper, String configJson) {
        if (configJson == null || configJson.isBlank()) {
            return null;
        }
        try {
            return mapper.readTree(configJson);
        } catch (Exception e) {
            return null;
        }
    }
}
