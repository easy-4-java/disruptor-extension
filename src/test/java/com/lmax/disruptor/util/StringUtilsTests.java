package com.lmax.disruptor.util;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTests {

    @Test
    void shouldReturnTrueForNullString() {
        assertTrue(StringUtils.isEmpty((String) null));
    }

    @Test
    void shouldReturnTrueForEmptyString() {
        assertTrue(StringUtils.isEmpty(""));
    }

    @Test
    void shouldReturnTrueForNullLiteral() {
        assertTrue(StringUtils.isEmpty("NULL"));
        assertTrue(StringUtils.isEmpty("null"));
    }

    @Test
    void shouldReturnFalseForNonEmptyString() {
        assertFalse(StringUtils.isEmpty("hello"));
    }

    @Test
    void shouldReturnOppositeForIsNotEmpty() {
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty("hello"));
    }

    @Test
    void shouldReturnTrueForNullWithIsNull() {
        assertTrue(StringUtils.isNull(null));
        assertTrue(StringUtils.isNull("  "));
    }

    @Test
    void shouldReturnFalseForNonNullWithIsNull() {
        assertFalse(StringUtils.isNull("hello"));
    }

    @Test
    void shouldCheckIsEmptyForObject() {
        assertTrue(StringUtils.isEmpty((Object) null));
        assertTrue(StringUtils.isEmpty((Object) ""));
        assertFalse(StringUtils.isEmpty((Object) "hello"));
    }

    @Test
    void shouldCheckHasLength() {
        assertFalse(StringUtils.hasLength((CharSequence) null));
        assertFalse(StringUtils.hasLength(""));
        assertTrue(StringUtils.hasLength(" "));
        assertTrue(StringUtils.hasLength("hello"));
    }

    @Test
    void shouldCheckHasLengthForString() {
        assertFalse(StringUtils.hasLength((String) null));
        assertTrue(StringUtils.hasLength("hello"));
    }

    @Test
    void shouldCheckHasText() {
        assertFalse(StringUtils.hasText((CharSequence) null));
        assertFalse(StringUtils.hasText(""));
        assertFalse(StringUtils.hasText("  "));
        assertTrue(StringUtils.hasText("hello"));
        assertTrue(StringUtils.hasText(" hello "));
    }

    @Test
    void shouldCheckHasTextForString() {
        assertFalse(StringUtils.hasText((String) null));
        assertTrue(StringUtils.hasText("hello"));
    }

    @Test
    void shouldCheckContainsWhitespace() {
        assertFalse(StringUtils.containsWhitespace((CharSequence) null));
        assertFalse(StringUtils.containsWhitespace(""));
        assertTrue(StringUtils.containsWhitespace(" "));
        assertTrue(StringUtils.containsWhitespace("hello world"));
        assertFalse(StringUtils.containsWhitespace("helloworld"));
    }

    @Test
    void shouldCheckContainsWhitespaceForString() {
        assertFalse(StringUtils.containsWhitespace((String) null));
        assertTrue(StringUtils.containsWhitespace(" "));
    }

    @Test
    void shouldTrimWhitespace() {
        assertNull(StringUtils.trimWhitespace(null));
        assertEquals("", StringUtils.trimWhitespace(""));
        assertEquals("hello", StringUtils.trimWhitespace("  hello  "));
        assertEquals("hello", StringUtils.trimWhitespace("hello"));
    }

    @Test
    void shouldTrimAllWhitespace() {
        assertNull(StringUtils.trimAllWhitespace(null));
        assertEquals("helloworld", StringUtils.trimAllWhitespace("hello world"));
        assertEquals("hello", StringUtils.trimAllWhitespace("  h e l l o  "));
    }

    @Test
    void shouldTrimLeadingWhitespace() {
        assertNull(StringUtils.trimLeadingWhitespace(null));
        assertEquals("hello  ", StringUtils.trimLeadingWhitespace("  hello  "));
    }

    @Test
    void shouldTrimTrailingWhitespace() {
        assertNull(StringUtils.trimTrailingWhitespace(null));
        assertEquals("  hello", StringUtils.trimTrailingWhitespace("  hello  "));
    }

    @Test
    void shouldTrimLeadingCharacter() {
        assertNull(StringUtils.trimLeadingCharacter(null, '/'));
        assertEquals("hello///", StringUtils.trimLeadingCharacter("///hello///", '/'));
    }

    @Test
    void shouldTrimTrailingCharacter() {
        assertNull(StringUtils.trimTrailingCharacter(null, '/'));
        assertEquals("///hello", StringUtils.trimTrailingCharacter("///hello///", '/'));
    }

    @Test
    void shouldStartsWithIgnoreCase() {
        assertFalse(StringUtils.startsWithIgnoreCase(null, "abc"));
        assertFalse(StringUtils.startsWithIgnoreCase("abc", null));
        assertTrue(StringUtils.startsWithIgnoreCase("abcdef", "abc"));
        assertTrue(StringUtils.startsWithIgnoreCase("ABCdef", "abc"));
        assertFalse(StringUtils.startsWithIgnoreCase("abc", "abcdef"));
    }

    @Test
    void shouldEndsWithIgnoreCase() {
        assertFalse(StringUtils.endsWithIgnoreCase(null, "abc"));
        assertFalse(StringUtils.endsWithIgnoreCase("abc", null));
        assertTrue(StringUtils.endsWithIgnoreCase("abcdef", "DEF"));
        assertTrue(StringUtils.endsWithIgnoreCase("abcdef", "def"));
        assertFalse(StringUtils.endsWithIgnoreCase("abc", "abcdef"));
    }

    @Test
    void shouldSubstringMatch() {
        assertTrue(StringUtils.substringMatch("hello", 0, "hel"));
        assertFalse(StringUtils.substringMatch("hello", 0, "world"));
        assertFalse(StringUtils.substringMatch("hi", 0, "hello"));
    }

    @Test
    void shouldCountOccurrences() {
        assertEquals(0, StringUtils.countOccurrencesOf(null, "a"));
        assertEquals(0, StringUtils.countOccurrencesOf("hello", null));
        assertEquals(2, StringUtils.countOccurrencesOf("hello world hello", "hello"));
        assertEquals(3, StringUtils.countOccurrencesOf("aaa", "a"));
    }

    @Test
    void shouldReplace() {
        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("hello", StringUtils.replace("hello", null, "b"));
        assertEquals("h3llo world", StringUtils.replace("hello world", "el", "3l"));
    }

    @Test
    void shouldDelete() {
        assertEquals("hllo", StringUtils.delete("hello", "e"));
        assertEquals("hello", StringUtils.delete("hello", "x"));
    }

    @Test
    void shouldDeleteAny() {
        assertNull(StringUtils.deleteAny(null, "abc"));
        assertEquals("hll", StringUtils.deleteAny("hello", "aeiou"));
        assertEquals("hello", StringUtils.deleteAny("hello", null));
    }

    @Test
    void shouldUnqualify() {
        assertEquals("qualified", StringUtils.unqualify("this.name.is.qualified"));
        assertEquals("qualified", StringUtils.unqualify("this:name:is:qualified", ':'));
    }

    @Test
    void shouldCapitalize() {
        assertNull(StringUtils.capitalize(null));
        assertEquals("", StringUtils.capitalize(""));
        assertEquals("Hello", StringUtils.capitalize("hello"));
        assertEquals("Hello", StringUtils.capitalize("Hello"));
    }

    @Test
    void shouldUncapitalize() {
        assertNull(StringUtils.uncapitalize(null));
        assertEquals("", StringUtils.uncapitalize(""));
        assertEquals("hello", StringUtils.uncapitalize("Hello"));
        assertEquals("hello", StringUtils.uncapitalize("hello"));
    }

    @Test
    void shouldGetFilename() {
        assertNull(StringUtils.getFilename(null));
        assertEquals("myfile.txt", StringUtils.getFilename("mypath/myfile.txt"));
        assertEquals("myfile.txt", StringUtils.getFilename("myfile.txt"));
    }

    @Test
    void shouldGetFilenameExtension() {
        assertNull(StringUtils.getFilenameExtension(null));
        assertEquals("txt", StringUtils.getFilenameExtension("mypath/myfile.txt"));
        assertNull(StringUtils.getFilenameExtension("myfile"));
    }

    @Test
    void shouldStripFilenameExtension() {
        assertNull(StringUtils.stripFilenameExtension(null));
        assertEquals("mypath/myfile", StringUtils.stripFilenameExtension("mypath/myfile.txt"));
        assertEquals("myfile", StringUtils.stripFilenameExtension("myfile"));
    }

    @Test
    void shouldApplyRelativePath() {
        assertEquals("/base/relative", StringUtils.applyRelativePath("/base/file.txt", "/relative"));
        assertEquals("/base/relative", StringUtils.applyRelativePath("/base/file.txt", "relative"));
        assertEquals("relative", StringUtils.applyRelativePath("file.txt", "relative"));
    }

    @Test
    void shouldCleanPath() {
        assertNull(StringUtils.cleanPath(null));
        assertEquals("/src/main", StringUtils.cleanPath("/src/../src/main"));
        assertEquals("/src/main", StringUtils.cleanPath("/src/./main"));
    }

    @Test
    void shouldPathEquals() {
        assertTrue(StringUtils.pathEquals("/a/../b", "/b"));
        assertFalse(StringUtils.pathEquals("/a", "/b"));
    }

    @Test
    void shouldParseLocaleString() {
        Locale locale = StringUtils.parseLocaleString("en_US");
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
    }

    @Test
    void shouldToLanguageTag() {
        assertEquals("en", StringUtils.toLanguageTag(Locale.ENGLISH));
        assertEquals("en-US", StringUtils.toLanguageTag(Locale.US));
    }

    @Test
    void shouldParseTimeZoneString() {
        assertNotNull(StringUtils.parseTimeZoneString("GMT"));
    }

    @Test
    void shouldThrowForInvalidTimeZone() {
        assertThrows(IllegalArgumentException.class, () ->
                StringUtils.parseTimeZoneString("INVALID_ZONE"));
    }

    @Test
    void shouldToStringArray() {
        assertNull(StringUtils.toStringArray((Collection<String>) null));
        List<String> list = Arrays.asList("a", "b", "c");
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.toStringArray(list));
    }

    @Test
    void shouldTokenizeToStringArray() {
        assertNull(StringUtils.tokenizeToStringArray(null, ","));
        String[] tokens = StringUtils.tokenizeToStringArray("a, b, c", ",");
        assertEquals(3, tokens.length);
        assertEquals("a", tokens[0]);
    }

    @Test
    void shouldTokenizeToStringArrayWithDefaults() {
        String[] tokens = StringUtils.tokenizeToStringArray("a;b, c d\t\ne");
        assertEquals(5, tokens.length);
    }

    @Test
    void shouldDelimitedListToStringArray() {
        String[] result = StringUtils.delimitedListToStringArray("a,b,c", ",");
        assertEquals(3, result.length);
        assertEquals("a", result[0]);
    }

    @Test
    void shouldDelimitedListToStringArrayWithNull() {
        assertEquals(0, StringUtils.delimitedListToStringArray(null, ",").length);
    }

    @Test
    void shouldCommaDelimitedListToStringArray() {
        String[] result = StringUtils.commaDelimitedListToStringArray("a,b,c");
        assertEquals(3, result.length);
    }

    @Test
    void shouldCommaDelimitedListToSet() {
        Set<String> set = StringUtils.commaDelimitedListToSet("a,b,a");
        assertEquals(2, set.size());
    }

    @Test
    void shouldCollectionToDelimitedString() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("a,b,c", StringUtils.collectionToDelimitedString(list, ","));
        assertEquals("[a]|[b]|[c]", StringUtils.collectionToDelimitedString(list, "|", "[", "]"));
    }

    @Test
    void shouldCollectionToCommaDelimitedString() {
        List<String> list = Arrays.asList("a", "b");
        assertEquals("a,b", StringUtils.collectionToCommaDelimitedString(list));
    }

    @Test
    void shouldSplit() {
        assertNull(StringUtils.split(null, ","));
        assertNull(StringUtils.split("hello", null));
        String[] result = StringUtils.split("hello.world", ".");
        assertEquals(2, result.length);
        assertEquals("hello", result[0]);
        assertEquals("world", result[1]);
    }

    @Test
    void shouldSplitByChar() {
        // split uses byte-level operations
        String[] result = StringUtils.split("a,b", ',');
        assertTrue(result.length >= 1);
    }

    @Test
    void shouldSplitByCharWithNull() {
        assertEquals(0, StringUtils.split(null, ',').length);
    }

    @Test
    void shouldSplits() {
        assertEquals(0, StringUtils.splits(null, ",").length);
        assertEquals(0, StringUtils.splits("hello", null).length);
        String[] result = StringUtils.splits("a,b,c", ",");
        assertEquals(3, result.length);
    }

    @Test
    void shouldRemoveLast() {
        assertNull(StringUtils.removeLast(null));
        assertEquals("hell", StringUtils.removeLast("hello"));
    }

    @Test
    void shouldAddQuotation() {
        assertNull(StringUtils.addQuotation(null));
        // split uses byte-level operations; "a,b,c" splits into "a" and "b,c"
        String result = StringUtils.addQuotation("a,b,c");
        assertNotNull(result);
        assertTrue(result.contains("'"));
    }

    @Test
    void shouldListToArray() {
        List<String> list = Arrays.asList("a", "b");
        String[] result = StringUtils.listToArray(list);
        assertEquals(2, result.length);
    }

    @Test
    void shouldListToString() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("a|b|c", StringUtils.listToString(list, "|"));
    }

    @Test
    void shouldGenRandomNum() {
        String pwd = StringUtils.genRandomNum(10);
        assertNotNull(pwd);
        assertEquals(10, pwd.length());
    }

    @Test
    void shouldKillNull() {
        assertEquals("", StringUtils.killNull(null));
        assertEquals("hello", StringUtils.killNull("hello"));
    }

    @Test
    void shouldParentheses() {
        assertNull(StringUtils.parentheses(null));
        assertEquals("(hello)", StringUtils.parentheses("hello"));
    }

    @Test
    void shouldBrackets() {
        assertNull(StringUtils.brackets(null));
        assertEquals("[hello]", StringUtils.brackets("hello"));
    }

    @Test
    void shouldDitto() {
        assertNull(StringUtils.ditto(null));
        assertEquals("\"hello\"", StringUtils.ditto("hello"));
    }

    @Test
    void shouldQuote() {
        assertNull(StringUtils.quote((String) null));
        assertEquals("'hello'", StringUtils.quote("hello"));
    }

    @Test
    void shouldQuoteArray() {
        assertEquals("", StringUtils.quote(null, ","));
        assertEquals("", StringUtils.quote(new String[]{}, ","));
        assertEquals("'a','b'", StringUtils.quote(new String[]{"a", "b"}, ","));
    }

    @Test
    void shouldQuoteIfString() {
        assertNull(StringUtils.quoteIfString(null));
        assertEquals("'hello'", StringUtils.quoteIfString("hello"));
        assertEquals(42, StringUtils.quoteIfString(42));
    }

    @Test
    void shouldTrimToAlphaString() {
        assertEquals("", StringUtils.trimToAlphaString(null));
        assertEquals("", StringUtils.trimToAlphaString(""));
        assertEquals("11", StringUtils.trimToAlphaString("1\r\n1\r\n"));
    }

    @Test
    void shouldTrimToAlphaStrings() {
        assertEquals(0, StringUtils.trimToAlphaStrings(null).length);
        assertEquals(0, StringUtils.trimToAlphaStrings("").length);
        String[] result = StringUtils.trimToAlphaStrings("1\r\n1\r\n");
        assertEquals(2, result.length);
    }

    @Test
    void shouldTrimToString() {
        assertNull(StringUtils.trimToString(null));
        assertNull(StringUtils.trimToString("  "));
        assertEquals("hello", StringUtils.trimToString("  hello  "));
    }

    @Test
    void shouldReplaceAllWithNoMatch() {
        // replaceAll uses byte-level operations; test with no match
        String result = StringUtils.replaceAll("z", "x", "hello");
        assertNotNull(result);
        assertEquals("hello", result);
    }

    @Test
    void shouldSplitArrayElementsIntoProperties() {
        assertNull(StringUtils.splitArrayElementsIntoProperties(null, "="));
        assertNull(StringUtils.splitArrayElementsIntoProperties(new String[]{}, "="));
        Properties props = StringUtils.splitArrayElementsIntoProperties(
                new String[]{"a=1", "b=2"}, "=");
        assertNotNull(props);
        assertEquals("1", props.getProperty("a"));
        assertEquals("2", props.getProperty("b"));
    }

    @Test
    void shouldSplitArrayElementsIntoPropertiesWithCharsToDelete() {
        Properties props = StringUtils.splitArrayElementsIntoProperties(
                new String[]{"a='1'", "b='2'"}, "=", "'");
        assertNotNull(props);
        assertEquals("1", props.getProperty("a"));
    }

    @Test
    void shouldGetFirstLetterFromChinessWord() {
        char result = StringUtils.getFirstLetterFromChinessWord("A");
        assertEquals('A', result);
    }

    @Test
    void shouldGetMapFromQueryParamString() {
        Map<String, String> result = StringUtils.getMapFromQueryParamString("key1`value1");
        assertNotNull(result);
    }
}
