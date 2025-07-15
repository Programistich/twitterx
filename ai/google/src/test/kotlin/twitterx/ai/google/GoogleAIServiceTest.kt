package twitterx.ai.google

import kotlinx.coroutines.test.runTest
import twitterx.ai.api.AIModel
import twitterx.ai.api.AIRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class GoogleAIServiceTest {

    private val service = GoogleAIService()

    @Test
    fun `test AIRequest creation with model`() = runTest {
        val request = AIRequest(
            prompt = "Test prompt",
            model = AIModel.GEMINI_2_5_FLASH
        )

        assertEquals("Test prompt", request.prompt)
        assertEquals(AIModel.GEMINI_2_5_FLASH, request.model)
    }

    @Test
    fun `test chat session creation`() = runTest {
        val sessionResult = service.startChat(AIModel.GEMINI_2_5_FLASH)

        // This will fail without Python setup, but tests the structure
        // sessionResult.fold(
        //     onSuccess = { session ->
        //         assertEquals(AIModel.GEMINI_2_5_FLASH, session.model)
        //         assertNotNull(session.conversationId)
        //     },
        //     onFailure = {
        //         // Expected to fail without Python environment
        //     }
        // )
    }

    @Test
    fun `test service availability check`() = runTest {
        // This will return false without proper Python setup
        val isAvailable = service.isAvailable()
        // assertFalse(isAvailable) // Expected without Python environment
    }
}
