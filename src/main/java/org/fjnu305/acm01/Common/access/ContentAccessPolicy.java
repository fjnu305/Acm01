package org.fjnu305.acm01.Common.access;

/**
 * Single-process content ACL. Search, feed, and detail APIs should ask this
 * instead of inventing per-module visibility rules. Extra predicates (friends,
 * contest-only, wiki vault ACL) plug in here later — still one JVM.
 */
public interface ContentAccessPolicy {

    boolean canRead(String type, Long refId, Viewer viewer);
}
