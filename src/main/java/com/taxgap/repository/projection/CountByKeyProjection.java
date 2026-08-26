package com.taxgap.repository.projection;

/**
 * Generic "key -> count" projection used by exception summary aggregates.
 */
public interface CountByKeyProjection {
    String getKey();
    long getCount();
}
