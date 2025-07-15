package twitterx.ai.google

import kotlinx.coroutines.test.runTest
import twitterx.ai.api.AIModel
import twitterx.ai.api.ChatSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ChatSessionTest {

    @Test
    fun `test ChatSession with metadata`() = runTest {
        val metadata = listOf("chat_id", "reply_id", "candidate_id")
        val session = ChatSession(
            conversationId = "test_conversation",
            model = AIModel.GEMINI_2_5_FLASH,
            metadata = metadata
        )

        assertEquals("test_conversation", session.conversationId)
        assertEquals(AIModel.GEMINI_2_5_FLASH, session.model)
        assertNotNull(session.metadata)
        assertEquals(3, session.metadata!!.size)
        assertEquals("chat_id", session.metadata!![0])
        assertEquals("reply_id", session.metadata!![1])
        assertEquals("candidate_id", session.metadata!![2])
    }

    @Test
    fun `test ChatSession without metadata`() = runTest {
        val session = ChatSession(
            conversationId = "test_conversation",
            model = AIModel.GEMINI_2_5_PRO
        )

        assertEquals("test_conversation", session.conversationId)
        assertEquals(AIModel.GEMINI_2_5_PRO, session.model)
        assertEquals(null, session.metadata)
    }
}
