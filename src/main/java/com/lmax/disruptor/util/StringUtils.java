/*
 * Copyright (c) 2017, Loong Wan (https://github.com/loong10k).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.lmax.disruptor.util;

import java.util.*;


/**
 * Extension of {@link org.apache.commons.lang3.StringUtils} that adds
 * convenience utilities for path handling, tokenization, locale parsing,
 * and other common string operations used throughout the disruptor-extension
 * framework.
 *
 * <p>Methods in this class are stateless and thread-safe.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see org.apache.commons.lang3.StringUtils
 */
public abstract class StringUtils extends org.apache.commons.lang3.StringUtils {

	private static final String FOLDER_SEPARATOR = "/";

	private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

	private static final String TOP_PATH = "..";

	private static final String CURRENT_PATH = ".";

	private static final char EXTENSION_SEPARATOR = '.';

	/**
	 * Delimiter characters used to separate multiple context config paths in
	 * a single String value.
	 */
	public static String CONFIG_LOCATION_DELIMITERS = ",; \t\n";

	private static final int[] allChineseScope = { 1601, 1637, 1833, 2078,
			2274, 2302, 2433, 2594, 2787, 3106, 3212, 3472, 3635, 3722, 3730,
			3858, 4027, 4086, 4390, 4558, 4684, 4925, 5249, 5600,
			Integer.MAX_VALUE };
	/** Fallback character returned when a Chinese pinyin initial cannot be determined. */
	public static final char unknowChar = '*';
	private static final char[] allEnglishLetter = { 'A', 'B', 'C', 'D', 'E',
			'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
			'T', 'W', 'X', 'Y', 'Z', unknowChar };

	/**
	 * Checks whether the given String is empty, null, or the literal
	 * {@code "NULL"} (case-insensitive).
	 *
	 * @param str the string to check (may be {@code null})
	 * @return {@code true} if the string is empty, null, or "NULL"
	 */
	public static boolean isEmpty(String str) {
		if (str == null) {
			return true;
		} else if (str.length() == 0) {
			return true;
		} else if ("NULL".equals(str.toUpperCase())) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether the given String is not empty.
	 * This method cannot be removed; removing it may break callers.
	 *
	 * @param str the string to check (may be {@code null})
	 * @return {@code true} if the string is not empty
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}


	/**
	 * Checks whether the given string is null or consists solely of
	 * whitespace characters.
	 *
	 * @param str the string to check (may be {@code null})
	 * @return {@code true} if the string is null or blank
	 */
	public static boolean isNull(String str) {

		return str == null || str.trim().length() == 0;
	}

	//---------------------------------------------------------------------
	// General convenience methods for working with Strings
	//---------------------------------------------------------------------

	/**
	 * Checks whether the given Object is null or equals the empty String.
	 *
	 * @param str the candidate Object (may be {@code null})
	 * @return {@code true} if the object is null or equals ""
	 * @since 3.2.1
	 */
	public static boolean isEmpty(Object str) {
		return (str == null || "".equals(str));
	}

	/**
	 * Checks that the given CharSequence is neither {@code null} nor of
	 * length 0. Returns {@code true} for whitespace-only content.
	 *
	 * @param str the CharSequence to check (may be {@code null})
	 * @return {@code true} if the CharSequence is not null and has length
	 * @see #hasText(String)
	 */
	public static boolean hasLength(CharSequence str) {
		return (str != null && str.length() > 0);
	}

	/**
	 * Checks that the given String is neither {@code null} nor of length 0.
	 * Returns {@code true} for whitespace-only content.
	 *
	 * @param str the String to check (may be {@code null})
	 * @return {@code true} if the String is not null and has length
	 * @see #hasLength(CharSequence)
	 */
	public static boolean hasLength(String str) {
		return hasLength((CharSequence) str);
	}

	/**
	 * Checks whether the given CharSequence has actual text -- i.e. is not
	 * null, has length &gt; 0, and contains at least one non-whitespace
	 * character.
	 *
	 * @param str the CharSequence to check (may be {@code null})
	 * @return {@code true} if the CharSequence contains actual text
	 * @see Character#isWhitespace
	 */
	public static boolean hasText(CharSequence str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the given String has actual text.
	 *
	 * @param str the String to check (may be {@code null})
	 * @return {@code true} if the String contains actual text
	 * @see #hasText(CharSequence)
	 */
	public static boolean hasText(String str) {
		return hasText((CharSequence) str);
	}

	/**
	 * Checks whether the given CharSequence contains any whitespace
	 * characters.
	 *
	 * @param str the CharSequence to check (may be {@code null})
	 * @return {@code true} if the CharSequence contains at least one
	 *         whitespace character
	 * @see Character#isWhitespace
	 */
	public static boolean containsWhitespace(CharSequence str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the given String contains any whitespace characters.
	 *
	 * @param str the String to check (may be {@code null})
	 * @return {@code true} if the String contains at least one whitespace
	 *         character
	 * @see #containsWhitespace(CharSequence)
	 */
	public static boolean containsWhitespace(String str) {
		return containsWhitespace((CharSequence) str);
	}

	/**
	 * Trims leading and trailing whitespace from the given String.
	 *
	 * @param str the String to trim
	 * @return the trimmed String
	 * @see Character#isWhitespace
	 */
	public static String trimWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
			sb.deleteCharAt(0);
		}
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Trims <em>all</em> whitespace from the given String: leading, trailing,
	 * and in between characters.
	 *
	 * @param str the String to trim
	 * @return the trimmed String
	 * @see Character#isWhitespace
	 */
	public static String trimAllWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		int len = str.length();
		StringBuilder sb = new StringBuilder(str.length());
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			if (!Character.isWhitespace(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Trims leading whitespace from the given String.
	 *
	 * @param str the String to trim
	 * @return the trimmed String
	 * @see Character#isWhitespace
	 */
	public static String trimLeadingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}

	/**
	 * Trims trailing whitespace from the given String.
	 *
	 * @param str the String to trim
	 * @return the trimmed String
	 * @see Character#isWhitespace
	 */
	public static String trimTrailingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Trims all occurrences of the supplied leading character from the given
	 * String.
	 *
	 * @param str             the String to trim
	 * @param leadingCharacter the leading character to be trimmed
	 * @return the trimmed String
	 */
	public static String trimLeadingCharacter(String str, char leadingCharacter) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && sb.charAt(0) == leadingCharacter) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}

	/**
	 * Trims all occurrences of the supplied trailing character from the given
	 * String.
	 *
	 * @param str               the String to trim
	 * @param trailingCharacter the trailing character to be trimmed
	 * @return the trimmed String
	 */
	public static String trimTrailingCharacter(String str, char trailingCharacter) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0 && sb.charAt(sb.length() - 1) == trailingCharacter) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}


	/**
	 * Tests if the given String starts with the specified prefix, ignoring
	 * case.
	 *
	 * @param str    the String to check
	 * @param prefix the prefix to look for
	 * @return {@code true} if the String starts with the prefix
	 *         (case-insensitive)
	 */
	public static boolean startsWithIgnoreCase(String str, String prefix) {
		if (str == null || prefix == null) {
			return false;
		}
		if (str.startsWith(prefix)) {
			return true;
		}
		if (str.length() < prefix.length()) {
			return false;
		}
		String lcStr = str.substring(0, prefix.length()).toLowerCase();
		String lcPrefix = prefix.toLowerCase();
		return lcStr.equals(lcPrefix);
	}

	/**
	 * Tests if the given String ends with the specified suffix, ignoring
	 * case.
	 *
	 * @param str    the String to check
	 * @param suffix the suffix to look for
	 * @return {@code true} if the String ends with the suffix
	 *         (case-insensitive)
	 */
	public static boolean endsWithIgnoreCase(String str, String suffix) {
		if (str == null || suffix == null) {
			return false;
		}
		if (str.endsWith(suffix)) {
			return true;
		}
		if (str.length() < suffix.length()) {
			return false;
		}

		String lcStr = str.substring(str.length() - suffix.length()).toLowerCase();
		String lcSuffix = suffix.toLowerCase();
		return lcStr.equals(lcSuffix);
	}

	/**
	 * Tests whether the given string matches the given substring at the
	 * given index.
	 *
	 * @param str       the original string (or StringBuilder)
	 * @param index     the index in the original string to start matching
	 * @param substring the substring to match at the given index
	 * @return {@code true} if the substring matches at the given index
	 */
	public static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
		for (int j = 0; j < substring.length(); j++) {
			int i = index + j;
			if (i >= str.length() || str.charAt(i) != substring.charAt(j)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Counts the occurrences of the substring in the given string.
	 *
	 * @param str string to search in (returns 0 if {@code null})
	 * @param sub string to search for (returns 0 if {@code null})
	 * @return the number of occurrences
	 */
	public static int countOccurrencesOf(String str, String sub) {
		if (str == null || sub == null || str.length() == 0 || sub.length() == 0) {
			return 0;
		}
		int count = 0;
		int pos = 0;
		int idx;
		while ((idx = str.indexOf(sub, pos)) != -1) {
			++count;
			pos = idx + sub.length();
		}
		return count;
	}

	/**
	 * Replaces all occurrences of a substring within a string with another
	 * string.
	 *
	 * @param inString   String to examine
	 * @param oldPattern String to replace
	 * @param newPattern String to insert
	 * @return a String with the replacements applied
	 */
	public static String replace(String inString, String oldPattern, String newPattern) {
		if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
			return inString;
		}
		StringBuilder sb = new StringBuilder();
		int pos = 0; // our position in the old string
		int index = inString.indexOf(oldPattern);
		// the index of an occurrence we've found, or -1
		int patLen = oldPattern.length();
		while (index >= 0) {
			sb.append(inString.substring(pos, index));
			sb.append(newPattern);
			pos = index + patLen;
			index = inString.indexOf(oldPattern, pos);
		}
		sb.append(inString.substring(pos));
		// remember to append any characters to the right of a match
		return sb.toString();
	}

	/**
	 * Deletes all occurrences of the given substring from the input string.
	 *
	 * @param inString the original String
	 * @param pattern  the pattern to delete all occurrences of
	 * @return the resulting String
	 */
	public static String delete(String inString, String pattern) {
		return replace(inString, pattern, "");
	}

	/**
	 * Deletes any character in the given set from the input String.
	 *
	 * @param inString      the original String
	 * @param charsToDelete a set of characters to delete (e.g. "az\n" will
	 *                      delete 'a's, 'z's and new lines)
	 * @return the resulting String
	 */
	public static String deleteAny(String inString, String charsToDelete) {
		if (!hasLength(inString) || !hasLength(charsToDelete)) {
			return inString;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < inString.length(); i++) {
			char c = inString.charAt(i);
			if (charsToDelete.indexOf(c) == -1) {
				sb.append(c);
			}
		}
		return sb.toString();
	}


	//---------------------------------------------------------------------
	// Convenience methods for working with formatted Strings
	//---------------------------------------------------------------------


	/**
	 * Unqualifies a string qualified by a '.' dot character. For example,
	 * "this.name.is.qualified" returns "qualified".
	 *
	 * @param qualifiedName the qualified name
	 * @return the unqualified name
	 */
	public static String unqualify(String qualifiedName) {
		return unqualify(qualifiedName, '.');
	}

	/**
	 * Unqualifies a string qualified by a separator character. For example,
	 * "this:name:is:qualified" returns "qualified" if using a ':' separator.
	 *
	 * @param qualifiedName the qualified name
	 * @param separator     the separator
	 * @return the unqualified name
	 */
	public static String unqualify(String qualifiedName, char separator) {
		return qualifiedName.substring(qualifiedName.lastIndexOf(separator) + 1);
	}

	/**
	 * Capitalizes a {@code String}, changing the first letter to upper case.
	 *
	 * @param str the String to capitalize (may be {@code null})
	 * @return the capitalized String, or {@code null} if the input was null
	 */
	public static String capitalize(String str) {
		return changeFirstCharacterCase(str, true);
	}

	/**
	 * Uncapitalizes a {@code String}, changing the first letter to lower case.
	 *
	 * @param str the String to uncapitalize (may be {@code null})
	 * @return the uncapitalized String, or {@code null} if the input was null
	 */
	public static String uncapitalize(String str) {
		return changeFirstCharacterCase(str, false);
	}

	private static String changeFirstCharacterCase(String str, boolean capitalize) {
		if (str == null || str.length() == 0) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str.length());
		if (capitalize) {
			sb.append(Character.toUpperCase(str.charAt(0)));
		}
		else {
			sb.append(Character.toLowerCase(str.charAt(0)));
		}
		sb.append(str.substring(1));
		return sb.toString();
	}

	/**
	 * Extracts the filename from the given path. For example,
	 * "mypath/myfile.txt" returns "myfile.txt".
	 *
	 * @param path the file path (may be {@code null})
	 * @return the extracted filename, or {@code null} if none
	 */
	public static String getFilename(String path) {
		if (path == null) {
			return null;
		}
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
	}

	/**
	 * Extracts the filename extension from the given path. For example,
	 * "mypath/myfile.txt" returns "txt".
	 *
	 * @param path the file path (may be {@code null})
	 * @return the extracted filename extension, or {@code null} if none
	 */
	public static String getFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
		if (extIndex == -1) {
			return null;
		}
		int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		if (folderIndex > extIndex) {
			return null;
		}
		return path.substring(extIndex + 1);
	}

	/**
	 * Strips the filename extension from the given path. For example,
	 * "mypath/myfile.txt" returns "mypath/myfile".
	 *
	 * @param path the file path (may be {@code null})
	 * @return the path with stripped filename extension, or {@code null}
	 *         if the input was null
	 */
	public static String stripFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
		if (extIndex == -1) {
			return path;
		}
		int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		if (folderIndex > extIndex) {
			return path;
		}
		return path.substring(0, extIndex);
	}

	/**
	 * Applies the given relative path to the given path, assuming standard
	 * Java folder separation (i.e. "/" separators).
	 *
	 * @param path         the path to start from (usually a full file path)
	 * @param relativePath the relative path to apply
	 * @return the full file path that results from applying the relative path
	 */
	public static String applyRelativePath(String path, String relativePath) {
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		if (separatorIndex != -1) {
			String newPath = path.substring(0, separatorIndex);
			if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
				newPath += FOLDER_SEPARATOR;
			}
			return newPath + relativePath;
		}
		else {
			return relativePath;
		}
	}

	/**
	 * Normalizes the path by suppressing sequences like "path/.." and inner
	 * simple dots.
	 *
	 * @param path the original path
	 * @return the normalized path
	 */
	public static String cleanPath(String path) {
		if (path == null) {
			return null;
		}
		String pathToUse = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

		// Strip prefix from path to analyze, to not treat it as part of the
		// first path element. This is necessary to correctly parse paths like
		// "file:core/../core/io/Resource.class", where the ".." should just
		// strip the first "core" directory while keeping the "file:" prefix.
		int prefixIndex = pathToUse.indexOf(":");
		String prefix = "";
		if (prefixIndex != -1) {
			prefix = pathToUse.substring(0, prefixIndex + 1);
			if (prefix.contains("/")) {
				prefix = "";
			}
			else {
				pathToUse = pathToUse.substring(prefixIndex + 1);
			}
		}
		if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
			prefix = prefix + FOLDER_SEPARATOR;
			pathToUse = pathToUse.substring(1);
		}

		String[] pathArray = delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
		List<String> pathElements = new LinkedList<String>();
		int tops = 0;

		for (int i = pathArray.length - 1; i >= 0; i--) {
			String element = pathArray[i];
			if (CURRENT_PATH.equals(element)) {
				// Points to current directory - drop it.
			}
			else if (TOP_PATH.equals(element)) {
				// Registering top path found.
				tops++;
			}
			else {
				if (tops > 0) {
					// Merging path element with element corresponding to top path.
					tops--;
				}
				else {
					// Normal path element found.
					pathElements.add(0, element);
				}
			}
		}

		// Remaining top paths need to be retained.
		for (int i = 0; i < tops; i++) {
			pathElements.add(0, TOP_PATH);
		}

		return prefix + collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
	}

	/**
	 * Compares two paths after normalization of them.
	 *
	 * @param path1 first path for comparison
	 * @param path2 second path for comparison
	 * @return whether the two paths are equivalent after normalization
	 */
	public static boolean pathEquals(String path1, String path2) {
		return cleanPath(path1).equals(cleanPath(path2));
	}

	/**
	 * Parses the given {@code localeString} value into a {@link Locale}.
	 * This is the inverse operation of {@link Locale#toString()}.
	 *
	 * @param localeString the locale String (e.g. "en", "en_UK")
	 * @return a corresponding {@code Locale} instance
	 * @throws IllegalArgumentException in case of an invalid locale
	 *                                  specification
	 */
	public static Locale parseLocaleString(String localeString) {
		String[] parts = tokenizeToStringArray(localeString, "_ ", false, false);
		String language = (parts.length > 0 ? parts[0] : "");
		String country = (parts.length > 1 ? parts[1] : "");
		validateLocalePart(language);
		validateLocalePart(country);
		String variant = "";
		if (parts.length > 2) {
			// There is definitely a variant, and it is everything after the country
			// code sans the separator between the country code and the variant.
			int endIndexOfCountryCode = localeString.indexOf(country, language.length()) + country.length();
			// Strip off any leading '_' and whitespace, what's left is the variant.
			variant = trimLeadingWhitespace(localeString.substring(endIndexOfCountryCode));
			if (variant.startsWith("_")) {
				variant = trimLeadingCharacter(variant, '_');
			}
		}
		return (language.length() > 0 ? new Locale(language, country, variant) : null);
	}

	private static void validateLocalePart(String localePart) {
		for (int i = 0; i < localePart.length(); i++) {
			char ch = localePart.charAt(i);
			if (ch != '_' && ch != ' ' && !Character.isLetterOrDigit(ch)) {
				throw new IllegalArgumentException(
						"Locale part \"" + localePart + "\" contains invalid characters");
			}
		}
	}

	/**
	 * Determines the RFC 3066 compliant language tag, as used for the HTTP
	 * "Accept-Language" header.
	 *
	 * @param locale the Locale to transform to a language tag
	 * @return the RFC 3066 compliant language tag as String
	 */
	public static String toLanguageTag(Locale locale) {
		return locale.getLanguage() + (hasText(locale.getCountry()) ? "-" + locale.getCountry() : "");
	}

	/**
	 * Parses the given {@code timeZoneString} value into a {@link TimeZone}.
	 *
	 * @param timeZoneString the time zone String (see
	 *                       {@link TimeZone#getTimeZone(String)})
	 * @return a corresponding {@link TimeZone} instance
	 * @throws IllegalArgumentException in case of an invalid time zone
	 *                                  specification
	 */
	public static TimeZone parseTimeZoneString(String timeZoneString) {
		TimeZone timeZone = TimeZone.getTimeZone(timeZoneString);
		if ("GMT".equals(timeZone.getID()) && !timeZoneString.startsWith("GMT")) {
			// We don't want that GMT fallback...
			throw new IllegalArgumentException("Invalid time zone specification '" + timeZoneString + "'");
		}
		return timeZone;
	}



	/**
	 * Copies the given Collection into a String array.
	 *
	 * @param collection the Collection to copy (may be {@code null})
	 * @return the String array, or {@code null} if the collection was null
	 */
	public static String[] toStringArray(Collection<String> collection) {
		if (collection == null) {
			return null;
		}
		return collection.toArray(new String[collection.size()]);
	}

	/**
	 * Copies the given Enumeration into a String array.
	 *
	 * @param enumeration the Enumeration to copy (may be {@code null})
	 * @return the String array, or {@code null} if the enumeration was null
	 */
	public static String[] toStringArray(Enumeration<String> enumeration) {
		if (enumeration == null) {
			return null;
		}
		List<String> list = Collections.list(enumeration);
		return list.toArray(new String[list.size()]);
	}



	/**
	 * Splits each element in the given array based on the given delimiter
	 * and generates a {@link Properties} instance with the left part as
	 * the key and the right part as the value.
	 *
	 * @param array     the array to process
	 * @param delimiter to split each element using (typically the equals
	 *                  symbol)
	 * @return a {@code Properties} instance, or {@code null} if the array
	 *         was null or empty
	 */
	public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter) {
		return splitArrayElementsIntoProperties(array, delimiter, null);
	}

	/**
	 * Splits each element in the given array based on the given delimiter
	 * and generates a {@link Properties} instance. Optionally deletes
	 * specified characters from each element before splitting.
	 *
	 * @param array         the array to process
	 * @param delimiter     to split each element using
	 * @param charsToDelete characters to remove from each element prior
	 *                      to splitting, or {@code null}
	 * @return a {@code Properties} instance, or {@code null} if the array
	 *         was null or empty
	 */
	public static Properties splitArrayElementsIntoProperties(
			String[] array, String delimiter, String charsToDelete) {

		if (Objects.isNull(array) || array.length == 0) {
			return null;
		}
		Properties result = new Properties();
		for (String element : array) {
			if (charsToDelete != null) {
				element = deleteAny(element, charsToDelete);
			}
			String[] splittedElement = split(element, delimiter);
			if (splittedElement == null) {
				continue;
			}
			result.setProperty(splittedElement[0].trim(), splittedElement[1].trim());
		}
		return result;
	}

	/**
	 * Tokenizes the given String into a String array using the default
	 * delimiters ({@value #CONFIG_LOCATION_DELIMITERS}).
	 *
	 * @param str the String to tokenize
	 * @return an array of tokens
	 */
	public static String[] tokenizeToStringArray(String str) {
		return tokenizeToStringArray(str, CONFIG_LOCATION_DELIMITERS, true, true);
	}

	/**
	 * Tokenizes the given String into a String array via a
	 * {@link StringTokenizer}. Trims tokens and omits empty tokens.
	 *
	 * @param str        the String to tokenize
	 * @param delimiters the delimiter characters, assembled as String
	 * @return an array of the tokens
	 * @see StringTokenizer
	 * @see String#trim()
	 * @see #delimitedListToStringArray
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters) {
		return tokenizeToStringArray(str, delimiters, true, true);
	}

	/**
	 * Tokenizes the given String into a String array via a
	 * {@link StringTokenizer}.
	 *
	 * @param str               the String to tokenize
	 * @param delimiters        the delimiter characters, assembled as String
	 * @param trimTokens        whether to trim tokens via {@code trim()}
	 * @param ignoreEmptyTokens whether to omit empty tokens from the result
	 * @return an array of the tokens, or {@code null} if the input String
	 *         was {@code null}
	 * @see StringTokenizer
	 * @see String#trim()
	 * @see #delimitedListToStringArray
	 */
	public static String[] tokenizeToStringArray(
			String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

		if (str == null) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || !token.isEmpty()) {
				tokens.add(token);
			}
		}
		return toStringArray(tokens);
	}

	/**
	 * Takes a String which is a delimited list and converts it to a String
	 * array. A single delimiter can consist of more than one character.
	 *
	 * @param str       the input String
	 * @param delimiter the delimiter between elements
	 * @return an array of the tokens in the list
	 * @see #tokenizeToStringArray
	 */
	public static String[] delimitedListToStringArray(String str, String delimiter) {
		return delimitedListToStringArray(str, delimiter, null);
	}

	/**
	 * Takes a String which is a delimited list and converts it to a String
	 * array. Optionally deletes specified characters from each token.
	 *
	 * @param str           the input String
	 * @param delimiter     the delimiter between elements
	 * @param charsToDelete characters to delete from each element
	 * @return an array of the tokens in the list
	 * @see #tokenizeToStringArray
	 */
	public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {
		if (str == null) {
			return new String[0];
		}
		if (delimiter == null) {
			return new String[] {str};
		}
		List<String> result = new ArrayList<String>();
		if ("".equals(delimiter)) {
			for (int i = 0; i < str.length(); i++) {
				result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
			}
		}
		else {
			int pos = 0;
			int delPos;
			while ((delPos = str.indexOf(delimiter, pos)) != -1) {
				result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
				pos = delPos + delimiter.length();
			}
			if (str.length() > 0 && pos <= str.length()) {
				// Add rest of String, but not in case of empty input.
				result.add(deleteAny(str.substring(pos), charsToDelete));
			}
		}
		return toStringArray(result);
	}

	/**
	 * Converts a CSV list into an array of Strings.
	 *
	 * @param str the input String
	 * @return an array of Strings, or the empty array in case of empty input
	 */
	public static String[] commaDelimitedListToStringArray(String str) {
		return delimitedListToStringArray(str, ",");
	}

	/**
	 * Convenience method to convert a CSV string list to a set. Duplicates
	 * are suppressed.
	 *
	 * @param str the input String
	 * @return a Set of String entries in the list
	 */
	public static Set<String> commaDelimitedListToSet(String str) {
		Set<String> set = new TreeSet<String>();
		String[] tokens = commaDelimitedListToStringArray(str);
		for (String token : tokens) {
			set.add(token);
		}
		return set;
	}

	/**
	 * Returns a Collection as a delimited (e.g. CSV) String.
	 *
	 * @param coll   the Collection to display
	 * @param delim  the delimiter to use (e.g. ",")
	 * @param prefix the String to start each element with
	 * @param suffix the String to end each element with
	 * @return the delimited String
	 */
	public static String collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix) {
		if (coll == null || coll.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Iterator<?> it = coll.iterator();
		while (it.hasNext()) {
			sb.append(prefix).append(it.next()).append(suffix);
			if (it.hasNext()) {
				sb.append(delim);
			}
		}
		return sb.toString();
	}

	/**
	 * Returns a Collection as a delimited (e.g. CSV) String.
	 *
	 * @param coll  the Collection to display
	 * @param delim the delimiter to use (e.g. ",")
	 * @return the delimited String
	 */
	public static String collectionToDelimitedString(Collection<?> coll, String delim) {
		return collectionToDelimitedString(coll, delim, "", "");
	}

	/**
	 * Returns a Collection as a CSV String.
	 *
	 * @param coll the Collection to display
	 * @return the CSV String
	 */
	public static String collectionToCommaDelimitedString(Collection<?> coll) {
		return collectionToDelimitedString(coll, ",");
	}


	/**
	 * Generates a query parameter Map from a backtick-delimited string.
	 *
	 * @param str the input string
	 * @return a Map of query parameters
	 */
	public static Map<String, String> getMapFromQueryParamString(String str) {
		Map<String, String> param = new HashMap<String, String>();
		String keyValues[] = str.split("`");
		for (int i = 0; i < keyValues.length; i++) {

		}
		return param;
	}

	/**
	 * Replaces all occurrences of {@code src} with {@code tar} in the given
	 * string.
	 *
	 * @param src the substring to replace
	 * @param tar the replacement string
	 * @param str the main string
	 * @return the resulting string with all replacements applied
	 */
	public static String replaceAll(String src, String tar, String str) {
		StringBuilder sb = new StringBuilder();
		byte bytesSrc[] = src.getBytes();

		byte bytes[] = str.getBytes();
		int point = 0;
		for (int i = 0; i < bytes.length; i++) {

			if (isStartWith(bytes, i, bytesSrc, 0)) {

				sb.append(new String(bytes, point, i));
				sb.append(tar);
				i += bytesSrc.length;
				point = i;
			}

		}
		sb.append(new String(bytes, point, bytes.length));
		return sb.toString();
	}

	/**
	 * Checks if the byte array starts with the given prefix at the specified
	 * offset.
	 *
	 * @param bytesSrc the source byte array
	 * @param startSrc the start offset in the source array
	 * @param bytesTar the target byte array to compare
	 * @param startTar the start offset in the target array
	 * @return {@code true} if the source array starts with the target
	 */
	private static boolean isStartWith(byte bytesSrc[], int startSrc,
			byte bytesTar[], int startTar) {
		for (int j = startTar; j < bytesTar.length; j++) {
			if (bytesSrc[startSrc + j] != bytesTar[j]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the first letter of a Chinese word using GBK encoding for pinyin
	 * initial lookup.
	 *
	 * @param str the Chinese string
	 * @return the pinyin initial letter, or {@code '*'} if not determinable
	 */
	public static char getFirstLetterFromChinessWord(String str) {
		char result = '*';
		String temp = str.toUpperCase();
		try {
			byte[] bytes = temp.getBytes("gbk");
			if (bytes[0] < 128 && bytes[0] > 0) {
				return (char) bytes[0];
			}

			int gbkIndex = 0;

			for (int i = 0; i < bytes.length; i++) {
				bytes[i] -= 160;
			}
			gbkIndex = bytes[0] * 100 + bytes[1];
			for (int i = 0; i < allEnglishLetter.length; i++) {
				if (i == 22) {
					// System.out.println(allEnglishLetter.length
					// +" "+allChineseScope.length);
				}
				if (gbkIndex >= allChineseScope[i]
						&& gbkIndex < allChineseScope[i + 1]) {
					result = allEnglishLetter[i];
					break;
				}
			}

		} catch (Exception e) {

		}
		return result;
	}

	/**
	 * Splits a string by the given character delimiter.
	 *
	 * @param src    the source string
	 * @param letter the delimiter character
	 * @return an array of split strings
	 */
	public static String[] split(String src, char letter) {
		if (src == null) {
			return new String[0];
		}
		List<String> ret = new ArrayList<String>();
		byte bytes[] = src.getBytes();
		int curPoint = 0;
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == letter) {
				String s = new String(bytes, curPoint, i - curPoint);
				ret.add(s);
				curPoint = i + 1;
			}
		}
		if (ret.size() == 0) {
			return new String[] { src };
		}
		// ret.add(new String(bytes, curPoint, src.length() - curPoint));
		String[] retStr = new String[ret.size()];
		for (int i = 0; i < ret.size(); i++) {
			retStr[i] = ret.get(i);
		}
		return retStr;
	}

	/**
	 * Splits a String at the first occurrence of the delimiter. Does not
	 * include the delimiter in the result.
	 *
	 * @param toSplit   the string to split
	 * @param delimiter to split the string up with
	 * @return a two-element array [beforeDelimiter, afterDelimiter], or
	 *         {@code null} if the delimiter was not found
	 */
	public static String[] split(String toSplit, String delimiter) {
		if (!hasLength(toSplit) || !hasLength(delimiter)) {
			return null;
		}
		int offset = toSplit.indexOf(delimiter);
		if (offset < 0) {
			return null;
		}
		String beforeDelimiter = toSplit.substring(0, offset);
		String afterDelimiter = toSplit.substring(offset + delimiter.length());
		return new String[] {beforeDelimiter, afterDelimiter};
	}

	/**
	 * Splits the given string using the given regex.
	 *
	 * @param toSplit the string to split
	 * @param regex   the regex delimiter
	 * @return an array of split strings (empty array if input is blank)
	 */
	public static String[] splits(String toSplit, String regex) {
		if (!hasLength(toSplit) || !hasLength(regex)) {
			return new String[] {};
		}
		return toSplit.split(regex);
	}

	/**
	 * Removes the last character from the given string.
	 *
	 * @param str the input string
	 * @return the string with the last character removed
	 */
	public static String removeLast(String str) {
		if (isNull(str)) {
			return str;
		}
		return str.substring(0, str.length() - 1);

	}

	/**
	 * Wraps each comma-separated element in single quotes for SQL usage.
	 * For example, "123,567" becomes "'123','567'".
	 *
	 * @param str the input string
	 * @return the quoted string
	 */
	public static String addQuotation(String str) {
		if (str == null) {
			return null;
		}
		String newStr = "";
		String[] strs = split(str, ",");
		for (int i = 0; i < strs.length; i++) {
			if (i > 0) {
				newStr += ",";
			}
			newStr += "'" + strs[i] + "'";
		}
		return newStr;

	}

	/**
	 * Converts a List of Strings to a String array.
	 *
	 * @param list the list to convert
	 * @return the String array
	 */
	public static String[] listToArray(List<String> list) {
		String[] strs = new String[list.size()];
		return list.toArray(strs);
	}

	/**
	 * Converts a List of Strings to a single delimited String.
	 *
	 * @param list      the list to convert
	 * @param separator the separator between elements
	 * @return the joined String
	 */
	public static String listToString(List<String> list, String separator) {
		return StringUtils.join(listToArray(list), separator);
	}

	/**
	 * Generates a random alphanumeric password of the specified length.
	 *
	 * @param pwd_len the desired password length
	 * @return the generated password string
	 */
	public static String genRandomNum(int pwd_len) {
		// 36 because array starts from 0, 26 letters + 10 digits + underscore
		final int maxNum = 37;
		int i; // generated random number
		int count = 0; // generated password length
		char[] str = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
				'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
				'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8',
				'9', '_' };
		StringBuilder pwd = new StringBuilder("");
		Random r = new Random();
		while (count < pwd_len) {
			i = Math.abs(r.nextInt(maxNum));
			if (i >= 0 && i < str.length) {
				pwd.append(str[i]);
				count++;
			}
		}
		return pwd.toString();
	}

	/**
	 * Returns an empty string if the given string is null.
	 *
	 * @param str the input string
	 * @return the input string, or "" if it was null
	 */
	public static String killNull(String str) {
		if (str == null) {
			return "";
		}
		return str;
	}

	/**
	 * Wraps the source string in parentheses.
	 *
	 * @param source the input string
	 * @return the string wrapped in "(", or {@code null} if source is null
	 */
	public static String parentheses(String source) {
		return (source != null ? "(" + source + ")" : null);
	}

	/**
	 * Wraps the source string in square brackets.
	 *
	 * @param source the input string
	 * @return the string wrapped in "[", or {@code null} if source is null
	 */
	public static String brackets(String source) {
		return (source != null ? "[" + source + "]" : null);
	}

	/**
	 * Wraps the source string in double quotes.
	 *
	 * @param source the input string
	 * @return the string wrapped in double quotes, or {@code null} if source
	 *         is null
	 */
	public static String ditto(String source) {
		return (source != null ? "\"" + source + "\"" : null);
	}

	/**
	 * Wraps the given String with single quotes.
	 *
	 * @param str the input String (e.g. "myString")
	 * @return the quoted String (e.g. "'myString'"), or {@code null} if the
	 *         input was null
	 */
	public static String quote(String str) {
		return (str != null ? "'" + str + "'" : null);
	}

	/**
	 * Wraps each element in the given String array with single quotes and
	 * joins them with the given separator.
	 *
	 * @param array     the String array
	 * @param separator the separator between quoted elements
	 * @return the quoted and joined string, or "" if the array is null or
	 *         empty
	 */
	public static String quote(String[] array, String separator) {
		if (null != array && array.length != 0) {
			String[] last = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				last[i] = StringUtils.quote(array[i]);
			}
			return StringUtils.join(last, separator);
		} else {
			return "";
		}

	}

	/**
	 * Turns the given Object into a String with single quotes if it is a
	 * String; keeping the Object as-is otherwise.
	 *
	 * @param obj the input Object (e.g. "myString")
	 * @return the quoted String (e.g. "'myString'"), or the input object
	 *         as-is if not a String
	 */
	public static Object quoteIfString(Object obj) {
		return (obj instanceof String ? quote((String) obj) : obj);
	}

	/**
	 * Strips all non-alphanumeric characters from the given string.
	 *
	 * @param string the input string
	 * @return the string with only word characters remaining
	 */
	public static String trimToAlphaString(String string) {
		if (string == null || string.isEmpty()) {
			return "";
		}
		return string.replaceAll("[^\\w]", "");
	}

	/**
	 * Strips all non-alphanumeric characters from the given string and
	 * returns each remaining character as an element in an array.
	 *
	 * @param string the input string
	 * @return an array of single-character strings
	 */
	public static String[] trimToAlphaStrings(String string) {
		if (string == null || string.isEmpty()) {
			return new String[0];
		}
		char[] chars = string.replaceAll("[^\\w]", "").toCharArray();
		String[] strs = new String[chars.length];
		for (int i = 0; i < chars.length; i++) {
			strs[i] = String.valueOf(chars[i]);
		}
		return strs;
	}

	/**
	 * Trims the given string and returns {@code null} if the result is
	 * empty.
	 *
	 * @param str the input string
	 * @return the trimmed string, or {@code null} if it was blank
	 */
	public static String trimToString(String str) {
		if (str == null || str.trim().length() == 0) {
			return null;
		} else {
			return str.trim();
		}
	}

}
