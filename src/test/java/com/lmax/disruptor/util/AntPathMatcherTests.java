package com.lmax.disruptor.util;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AntPathMatcherTests {

    private final AntPathMatcher matcher = new AntPathMatcher();

    @Test
    void shouldMatchSimplePattern() {
        assertTrue(matcher.match("/test", "/test"));
        assertFalse(matcher.match("/test", "/other"));
    }

    @Test
    void shouldMatchSingleWildcard() {
        assertTrue(matcher.match("/t?st", "/test"));
        assertTrue(matcher.match("/t?st", "/tast"));
        assertFalse(matcher.match("/t?st", "/toast"));
    }

    @Test
    void shouldMatchStarWildcard() {
        assertTrue(matcher.match("/*.txt", "/test.txt"));
        assertTrue(matcher.match("/com/*.jsp", "/com/test.jsp"));
        assertFalse(matcher.match("/com/*.jsp", "/org/test.jsp"));
    }

    @Test
    void shouldMatchDoubleStarWildcard() {
        assertTrue(matcher.match("/**/test.jsp", "/com/test.jsp"));
        assertTrue(matcher.match("/**/test.jsp", "/com/foo/bar/test.jsp"));
        assertTrue(matcher.match("/com/**", "/com/foo/bar"));
    }

    @Test
    void shouldMatchStart() {
        assertTrue(matcher.matchStart("/test", "/test"));
        assertTrue(matcher.matchStart("/test/**", "/test/anything"));
        assertFalse(matcher.matchStart("/other", "/test"));
    }

    @Test
    void shouldIdentifyPatterns() {
        assertTrue(matcher.isPattern("/test/*"));
        assertTrue(matcher.isPattern("/test/{id}"));
        assertTrue(matcher.isPattern("/test/?"));
        assertFalse(matcher.isPattern("/test/static"));
        assertFalse(matcher.isPattern(null));
    }

    @Test
    void shouldExtractPathWithinPattern() {
        assertEquals("", matcher.extractPathWithinPattern("/docs/cvs/commit.html", "/docs/cvs/commit.html"));
        assertEquals("cvs/commit", matcher.extractPathWithinPattern("/docs/*", "/docs/cvs/commit"));
        assertEquals("commit.html", matcher.extractPathWithinPattern("/docs/cvs/*.html", "/docs/cvs/commit.html"));
    }

    @Test
    void shouldExtractUriTemplateVariables() {
        Map<String, String> vars = matcher.extractUriTemplateVariables("/hotels/{hotel}", "/hotels/1");
        assertEquals("1", vars.get("hotel"));
    }

    @Test
    void shouldThrowWhenExtractingFromNonMatch() {
        assertThrows(IllegalStateException.class, () ->
                matcher.extractUriTemplateVariables("/hotels/{hotel}", "/flights/1"));
    }

    @Test
    void shouldCombinePatterns() {
        assertEquals("/hotels/bookings", matcher.combine("/hotels", "/bookings"));
        assertEquals("/hotels/bookings", matcher.combine("/hotels", "bookings"));
        assertEquals("/hotels/**/bookings", matcher.combine("/hotels/**", "/bookings"));
    }

    @Test
    void shouldCombineWithNull() {
        assertEquals("", matcher.combine(null, null));
        assertEquals("/hotels", matcher.combine("/hotels", null));
        assertEquals("/hotels", matcher.combine(null, "/hotels"));
    }

    @Test
    void shouldCombineFileExtensions() {
        // The combine method returns "/hotels.html" when the first pattern ends with "/*.html"
        assertEquals("/hotels.html", matcher.combine("/*.html", "/hotels.html"));
        assertEquals("/hotels.html", matcher.combine("/*.html", "/hotels"));
    }

    @Test
    void shouldGetPatternComparator() {
        Comparator<String> comparator = matcher.getPatternComparator("/hotels/2");
        assertNotNull(comparator);
        assertTrue(comparator.compare("/hotels/2", "/hotels/{hotel}") < 0);
    }

    @Test
    void shouldSetPathSeparator() {
        AntPathMatcher customMatcher = new AntPathMatcher(".");
        assertTrue(customMatcher.match("com.test", "com.test"));
        assertFalse(customMatcher.match("com/test", "com.test"));
    }

    @Test
    void shouldSetCaseInsensitive() {
        AntPathMatcher caseInsensitive = new AntPathMatcher();
        caseInsensitive.setCaseSensitive(false);
        assertTrue(caseInsensitive.match("/TEST", "/test"));
    }

    @Test
    void shouldSetTrimTokens() {
        AntPathMatcher trimmingMatcher = new AntPathMatcher();
        trimmingMatcher.setTrimTokens(true);
        assertTrue(trimmingMatcher.match("/test", "/ test "));
    }

    @Test
    void shouldRejectNullPathSeparator() {
        assertThrows(NullPointerException.class, () -> new AntPathMatcher(null));
    }

    @Test
    void shouldMatchComplexDoubleStarPattern() {
        assertTrue(matcher.match("/org/**/servlet/bla.jsp", "/org/springframework/servlet/bla.jsp"));
        assertTrue(matcher.match("/org/**/servlet/bla.jsp", "/org/servlet/bla.jsp"));
    }

    @Test
    void shouldMatchVariablePattern() {
        assertTrue(matcher.match("/{name:\\w+}.jsp", "/test.jsp"));
    }
}
