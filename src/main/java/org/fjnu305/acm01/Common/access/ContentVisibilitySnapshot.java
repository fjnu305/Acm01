package org.fjnu305.acm01.Common.access;

/**
 * Module-owned snapshot so {@link ContentAccessPolicy} never talks to feature mappers.
 */
public record ContentVisibilitySnapshot(Long ownerId, Integer status, Integer deleted) {
}
