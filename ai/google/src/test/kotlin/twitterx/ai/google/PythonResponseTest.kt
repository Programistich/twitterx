package twitterx.ai.google

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PythonResponseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Test
    fun `test PythonResponse with null metadata`() {
        val jsonString = """{"success": true, "conversationId": "chat_123", "metadata": null}"""

        val response = json.decodeFromString<PythonResponse>(jsonString)

        assertEquals(true, response.success)
        assertEquals("chat_123", response.conversationId)
        assertNull(response.metadata)
    }

    @Test
    fun `test PythonResponse with empty metadata`() {
        val jsonString = """{"success": true, "conversationId": "chat_123", "metadata": []}"""

        val response = json.decodeFromString<PythonResponse>(jsonString)

        assertEquals(true, response.success)
        assertEquals("chat_123", response.conversationId)
        assertNotNull(response.metadata)
        assertEquals(0, response.metadata!!.size)
    }

    @Test
    fun `test PythonResponse with valid metadata`() {
        val jsonString = """{"success": true, "conversationId": "chat_123", "metadata": ["chat_id", "reply_id", "candidate_id"]}"""

        val response = json.decodeFromString<PythonResponse>(jsonString)

        assertEquals(true, response.success)
        assertEquals("chat_123", response.conversationId)
        assertNotNull(response.metadata)
        assertEquals(3, response.metadata!!.size)
        assertEquals("chat_id", response.metadata!![0])
        assertEquals("reply_id", response.metadata!![1])
        assertEquals("candidate_id", response.metadata!![2])
    }

    @Test
    fun `test PythonResponse with complete data`() {
        val jsonString = """
        {
            "success": true,
            "text": "Hello there!",
            "thoughts": null,
            "conversationId": "chat_456",
            "metadata": ["cid", "rid"],
            "model": "gemini-2.5-flash",
            "images": []
        }
        """.trimIndent()

        val response = json.decodeFromString<PythonResponse>(jsonString)

        assertEquals(true, response.success)
        assertEquals("Hello there!", response.text)
        assertNull(response.thoughts)
        assertEquals("chat_456", response.conversationId)
        assertNotNull(response.metadata)
        assertEquals(2, response.metadata!!.size)
        assertEquals("cid", response.metadata!![0])
        assertEquals("rid", response.metadata!![1])
        assertEquals("gemini-2.5-flash", response.model)
        assertEquals(0, response.images.size)
    }
}
