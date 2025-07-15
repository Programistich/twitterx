package twitterx.ai.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AIModelsTest {

    @Test
    fun `test AIModel fromName with valid name`() {
        assertEquals(AIModel.GEMINI_2_5_FLASH, AIModel.fromName("gemini-2.5-flash"))
        assertEquals(AIModel.GEMINI_2_5_PRO, AIModel.fromName("gemini-2.5-pro"))
        assertEquals(AIModel.UNSPECIFIED, AIModel.fromName("unspecified"))
    }

    @Test
    fun `test AIModel fromName with invalid name`() {
        assertFailsWith<IllegalArgumentException> {
            AIModel.fromName("invalid-model")
        }
    }

    @Test
    fun `test AIRequest creation`() {
        val request = AIRequest(
            prompt = "Hello, world!",
            model = AIModel.GEMINI_2_5_FLASH,
            files = listOf("test.txt")
        )

        assertEquals("Hello, world!", request.prompt)
        assertEquals(AIModel.GEMINI_2_5_FLASH, request.model)
        assertEquals(listOf("test.txt"), request.files)
    }

    @Test
    fun `test AIResponse creation`() {
        val response = AIResponse(
            text = "Response text",
            model = AIModel.GEMINI_2_5_FLASH,
            conversationId = "test-id"
        )

        assertEquals("Response text", response.text)
        assertEquals(AIModel.GEMINI_2_5_FLASH, response.model)
        assertEquals("test-id", response.conversationId)
    }
}
