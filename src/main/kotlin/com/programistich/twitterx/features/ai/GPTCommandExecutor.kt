package com.programistich.twitterx.features.ai

import com.programistich.twitterx.core.executors.CommandExecutor
import com.programistich.twitterx.core.telegram.models.TelegramCommand
import com.programistich.twitterx.core.telegram.models.TelegramContext
import com.programistich.twitterx.core.telegram.updates.TelegramMessageUpdate
import com.programistich.twitterx.core.telegram.updates.getTextWithoutCommand
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class GPTCommandExecutor(
    private val openAIApi: OpenAIApi,
    private val telegramClient: TelegramClient
) : CommandExecutor() {
    companion object {
        private val ALLOWED_CHATS = listOf("-1001488807577", "241629528")
        private val codeMarkupPattern = Regex("`{1,3}([^`]+)`{1,3}")
    }

    override val command: TelegramCommand
        get() = TelegramCommand.GPT

    override suspend fun process(context: TelegramContext<TelegramMessageUpdate>): Result<Unit> {
        val chat = context.chat ?: return Result.failure(IllegalArgumentException("Chat is required"))
        if (!ALLOWED_CHATS.contains(chat.id.toString())) {
            return Result.failure(IllegalArgumentException("Chat is not allowed"))
        }

        val message = context.update.message
        val replyMessages = message.getAllReplyMessages()

        val textMessage = message.getTextWithoutCommand()
            ?: return Result.failure(IllegalArgumentException("Text is required"))

        val textReplyMessages = replyMessages.mapNotNull { it.getTextWithoutCommand() }

        if (textMessage.isEmpty() && textReplyMessages.isEmpty()) {
            return Result.failure(IllegalArgumentException("Text and reply message are required"))
        }

        val sendAction = SendChatAction("${chat.id}", "typing")
        telegramClient.executeAsync(sendAction).await()

        val openAIResult = openAIApi.request(
            textMessage,
            textReplyMessages,
            chat.language
        )

        val result = openAIResult
            .choices
            .firstOrNull()
            ?.message
            ?.content
            ?.replace(codeMarkupPattern, "$1".code())
            ?: "No response from OpenAI"

        val sendMessage = SendMessage("${chat.id}", result)
        sendMessage.replyToMessageId = message.messageId
        telegramClient.executeAsync(sendMessage).await()

        return Result.success(Unit)
    }

    private fun String.code(): String = "<code>$this</code>"
}

fun Message.getAllReplyMessages(): List<Message> {
    val replies = mutableListOf<Message>()
    var currentReply = this.replyToMessage
    while (currentReply != null) {
        replies.add(currentReply)
        currentReply = currentReply.replyToMessage
    }
    return replies
}
