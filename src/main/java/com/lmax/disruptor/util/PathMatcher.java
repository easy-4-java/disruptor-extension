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

import java.util.Comparator;
import java.util.Map;

/**
 * Strategy interface for {@code String}-based path matching.
 *
 * <p>The default implementation is {@link AntPathMatcher}, supporting the
 * Ant-style pattern syntax.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see AntPathMatcher
 */
public interface PathMatcher {

    /**
     * Determines whether the given {@code path} represents a pattern that can
     * be matched by an implementation of this interface.
     *
     * <p>If the return value is {@code false}, then the {@link #match} method
     * does not have to be used because direct equality comparisons on the
     * static path Strings will lead to the same result.</p>
     *
     * @param path the path to check
     * @return {@code true} if the given {@code path} represents a pattern
     */
    boolean isPattern(String path);

    /**
     * Matches the given {@code path} against the given {@code pattern},
     * according to this PathMatcher's matching strategy.
     *
     * @param pattern the pattern to match against
     * @param path    the path to test
     * @return {@code true} if the supplied {@code path} matched,
     *         {@code false} if it did not
     */
    boolean match(String pattern, String path);

    /**
     * Matches the given {@code path} against the corresponding part of the
     * given {@code pattern}, according to this PathMatcher's matching strategy.
     *
     * <p>Determines whether the pattern at least matches as far as the given
     * base path goes, assuming that a full path may then match as well.</p>
     *
     * @param pattern the pattern to match against
     * @param path    the path to test
     * @return {@code true} if the supplied {@code path} matched,
     *         {@code false} if it did not
     */
    boolean matchStart(String pattern, String path);

    /**
     * Given a pattern and a full path, determines the pattern-mapped part.
     *
     * <p>This method strips off a statically defined leading path from the
     * given full path, returning only the actually pattern-matched part of
     * the path.</p>
     *
     * @param pattern the path pattern
     * @param path    the full path to introspect
     * @return the pattern-mapped part of the given {@code path}
     *         (never {@code null})
     */
    String extractPathWithinPattern(String pattern, String path);

    /**
     * Given a pattern and a full path, extracts the URI template variables.
     * URI template variables are expressed through curly brackets ('{' and '}').
     *
     * @param pattern the path pattern, possibly containing URI templates
     * @param path    the full path to extract template variables from
     * @return a map, containing variable names as keys and variable values as
     *         values
     */
    Map<String, String> extractUriTemplateVariables(String pattern, String path);

    /**
     * Given a full path, returns a {@link Comparator} suitable for sorting
     * patterns in order of explicitness for that path.
     *
     * @param path the full path to use for comparison
     * @return a comparator capable of sorting patterns in order of
     *         explicitness
     */
    Comparator<String> getPatternComparator(String path);

    /**
     * Combines two patterns into a new pattern that is returned.
     *
     * @param pattern1 the first pattern
     * @param pattern2 the second pattern
     * @return the combination of the two patterns
     * @throws IllegalArgumentException when the two patterns cannot be combined
     */
    String combine(String pattern1, String pattern2);

}
