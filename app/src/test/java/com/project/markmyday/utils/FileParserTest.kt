package com.project.markmyday.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.regex.Pattern

class FileParserTest {

    private fun extractYoutubeId(url: String): String? {
        val pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*"
        val compiledPattern = Pattern.compile(pattern)
        val matcher = compiledPattern.matcher(url)
        return if (matcher.find()) {
            val id = matcher.group()
            if (id.length == 11) id else null
        } else {
            null
        }
    }

    @Test
    fun testExtractYoutubeId() {
        assertEquals("dQw4w9WgXcQ", extractYoutubeId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", extractYoutubeId("https://youtu.be/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", extractYoutubeId("https://www.youtube.com/embed/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", extractYoutubeId("https://www.youtube.com/v/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", extractYoutubeId("https://youtube.com/watch?v=dQw4w9WgXcQ&feature=related"))
        assertEquals(null, extractYoutubeId("https://www.google.com"))
        assertEquals(null, extractYoutubeId("https://www.youtube.com/watch?v=too_short"))
    }
}
