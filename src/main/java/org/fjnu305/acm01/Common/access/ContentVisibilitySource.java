package org.fjnu305.acm01.Common.access;

/**
 * Feature module port. Register one bean per content type.
 */
public interface ContentVisibilitySource {

    String type();

    ContentVisibilitySnapshot load(Long refId);
}
