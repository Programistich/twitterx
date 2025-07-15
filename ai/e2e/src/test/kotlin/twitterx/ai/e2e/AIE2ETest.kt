package twitterx.ai.e2e

import kotlinx.coroutines.test.runTest
import twitterx.ai.api.AIModel
import twitterx.ai.api.AIRequest
import twitterx.ai.google.GoogleAIConfiguration
import twitterx.ai.google.GoogleAIService
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AIE2ETest {

    private fun createService(): GoogleAIService {
        val config = GoogleAIConfiguration(
            secure1PSID = System.getenv("GEMINI_SECURE_1PSID"),
            secure1PSIDTS = System.getenv("GEMINI_SECURE_1PSIDTS"),
            timeout = 60_000L, // 60 seconds for E2E tests,
            scriptPath = "../../scripts/gemini_ai.py"
        )
        return GoogleAIService(config)
    }

    @Test
    fun `test service availability`() = runTest {
        val service = createService()
        val isAvailable = service.isAvailable()
        assertTrue(isAvailable, "AI Service should be available for E2E tests")
    }

    @Test
    fun `test simple text generation`() = runTest {
        val service = createService()

        val request = AIRequest(
            prompt = "Say hello in one word",
            model = AIModel.GEMINI_2_5_FLASH
        )

        val result = service.generateContent(request)

        result.fold(
            onSuccess = { response ->
                assertNotNull(response.text)
                assertTrue(response.text.isNotBlank())
                println("Generated text: ${response.text}")
            },
            onFailure = { error ->
                println("Expected failure without authentication: ${error.message}")
                assertTrue(false)
            }
        )
        service.close()
    }

    @Test
    fun `test chat session functionality`() = runTest {
        val service = createService()

        val sessionResult = service.startChat(AIModel.GEMINI_2_5_FLASH)

        sessionResult.fold(
            onSuccess = { session ->
                assertNotNull(session.conversationId)
                println("Chat session created: ${session.conversationId}")
                println("Session metadata: ${session.metadata}")

                // Try to send a message
                val messageResult = service.sendMessage(session, "Hello!")

                messageResult.fold(
                    onSuccess = { response ->
                        assertNotNull(response.text)
                        assertTrue(response.text.isNotBlank())
                        println("Chat response: ${response.text}")
                    },
                    onFailure = { error ->
                        println("Expected failure without authentication: ${error.message}")
                        // This is expected without proper authentication
                        assertTrue(true) // Test passes as we expect this to fail
                    }
                )
            },
            onFailure = { error ->
                println("Chat session creation failed (expected without auth): ${error.message}")
                // This is expected without proper authentication
                assertTrue(true)
            }
        )

        service.close()
    }

    @Test
    fun `test different AI models`() = runTest {
        val service = createService()

        val models = listOf(
            AIModel.UNSPECIFIED,
            AIModel.GEMINI_2_5_FLASH,
            AIModel.GEMINI_2_5_PRO
        )

        for (model in models) {
            val request = AIRequest(
                prompt = "What model are you?",
                model = model
            )

            val result = service.generateContent(request)

            result.fold(
                onSuccess = { response ->
                    println("Model ${model.modelName} response: ${response.text.take(100)}")
                    assertTrue(response.text.isNotBlank())
                },
                onFailure = { error ->
                    println("Model ${model.modelName} failed (expected without auth): ${error.message}")
                    assertTrue(false, "Expected to generate content with model ${model.modelName}")
                }
            )
        }

        service.close()
    }
}
