package com.warrior.tracker.data.local.dao

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `containsQuery` builds the LIKE argument for [ExerciseDao.searchExercises].
 *
 * It is security-relevant rather than cosmetic: the Phase-0 query interpolated raw user input
 * (`LIKE '%' || :query || '%'`), so typing a percent sign or an underscore into the search box
 * silently turned it into a wildcard instead of matching literally.
 */
class ExerciseDaoQueryTest {

    @Test
    fun `plain text becomes a wrapped substring pattern`() {
        assertEquals("%bench%", ExerciseDao.containsQuery("bench"))
        assertEquals("%%", ExerciseDao.containsQuery(""))
        assertEquals("%Barbell Row%", ExerciseDao.containsQuery("Barbell Row"))
    }

    @Test
    fun `LIKE metacharacters are escaped so they match literally`() {
        // "100%"  ->  %100\%%      (the user's percent is escaped; the outer two are the wildcards)
        assertEquals("%100\\%%", ExerciseDao.containsQuery("100%"))
        // "a_b"   ->  %a\_b%
        assertEquals("%a\\_b%", ExerciseDao.containsQuery("a_b"))
        // "a\b"   ->  %a\\b%       (the escape character escapes itself)
        assertEquals("%a\\\\b%", ExerciseDao.containsQuery("a\\b"))
    }

    /**
     * Rather than hand-counting backslashes, assert the property that actually matters: once
     * SQLite's ESCAPE-clause unescaping is undone, the pattern body is exactly the user's text.
     */
    @Test
    fun `escaping round-trips for pathological input`() {
        val cases = listOf(
            "%",
            "_",
            "\\",
            "%_\\",
            "\\\\\\",
            "a%b_c\\d",
            "\\%_",
            "%%__\\\\",
        )
        for (raw in cases) {
            val pattern = ExerciseDao.containsQuery(raw)
            assertTrue("must stay percent-wrapped: <$pattern>", pattern.startsWith("%") && pattern.endsWith("%"))
            assertTrue("must be at least as long as the input", pattern.length >= raw.length + 2)
            val body = pattern.substring(1, pattern.length - 1)
            assertEquals("round-trip failed for <$raw>", raw, unescapeLike(body))
        }
    }

    @Test
    fun `persian text passes through untouched`() {
        assertEquals("%شنا سوئدی%", ExerciseDao.containsQuery("شنا سوئدی"))
        // U+066F ARABIC PERCENT SIGN is a different code point from ASCII '%', so it is not
        // escaped — and SQLite LIKE does not treat it as a wildcard either.
        assertEquals("%۱۰۰٪%", ExerciseDao.containsQuery("۱۰۰٪"))
    }

    @Test
    fun `only the three SQLite LIKE metacharacters are touched`() {
        // None of these are LIKE metacharacters, so none of them may be escaped.
        val raw = "a.b[c]d(e)f{g}h|i*j+k?l^m~n@o#p:q;r't\"u,v<w>x=y+z"
        assertEquals("%" + raw + "%", ExerciseDao.containsQuery(raw))
    }

    /** Undo SQLite's escape-character handling so the test can assert on the user's text. */
    private fun unescapeLike(pattern: String): String = buildString(pattern.length) {
        var i = 0
        while (i < pattern.length) {
            val ch = pattern[i]
            if (ch == '\\' && i + 1 < pattern.length) {
                append(pattern[i + 1])
                i += 2
            } else {
                append(ch)
                i++
            }
        }
    }
}
