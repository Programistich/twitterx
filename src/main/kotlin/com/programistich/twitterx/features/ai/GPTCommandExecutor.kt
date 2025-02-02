package com.programistich.twitterx.features.ai

import com.programistich.twitterx.core.executors.CommandExecutor
import com.programistich.twitterx.core.telegram.models.Language
import com.programistich.twitterx.core.telegram.models.TelegramCommand
import com.programistich.twitterx.core.telegram.models.TelegramConfig
import com.programistich.twitterx.core.telegram.models.TelegramContext
import com.programistich.twitterx.core.telegram.updates.TelegramMessageUpdate
import com.programistich.twitterx.core.telegram.updates.getTextWithoutCommand
import com.programistich.twitterx.features.dict.DictionaryCache
import com.programistich.twitterx.features.dict.DictionaryKey
import kotlinx.coroutines.future.await
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.generics.TelegramClient

//@Component Disable
class GPTCommandExecutor(
    private val openAIApi: OpenAIApi,
    private val telegramClient: TelegramClient,
    private val dictionary: DictionaryCache,
    private val botConfig: TelegramConfig
) : CommandExecutor() {
    companion object {
        private val ALLOWED_CHATS = listOf("-1001488807577", "241629528", "-1002127649489")
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

        val textMessage = message.getTextWithoutCommand()
            ?: return Result.failure(IllegalArgumentException("Text is required"))

        if (textMessage.isEmpty() && message.replyToMessage == null) {
            return Result.failure(IllegalArgumentException("Text and reply message are required"))
        }

        val sendAction = SendChatAction("${chat.id}", "typing")
        telegramClient.executeAsync(sendAction).await()

        val chatRoleMessage = toChatRoleMessage(message, chat.language)
        val openAIResult = openAIApi.request(chatRoleMessage)

        val result = openAIResult
            .choices
            .firstOrNull()
            ?.message
            ?.content
            ?.replace(codeMarkupPattern, "$1".code())
            ?: dictionary.getByKey(DictionaryKey.TWEET_HEADER, chat.language)

        val sendMessage = SendMessage("${chat.id}", result)
        sendMessage.replyToMessageId = message.messageId
        sendMessage.parseMode = "HTML"
        telegramClient.executeAsync(sendMessage).await()

        return Result.success(Unit)
    }

    private fun String.code(): String = "<code>$this</code>"

    private fun toChatRoleMessage(message: Message, language: Language): List<ChatRoleMessage> {
        val messages = mutableListOf<ChatRoleMessage>()

        // Main message
        val textMessage = message.getTextWithoutCommand()
        if (!textMessage.isNullOrEmpty()) {
            messages.add(ChatRoleMessage.User(textMessage))
        }

        // Reply messages
        message.getAllReplyMessages().forEach {
            val textReplyMessage = it.getTextWithoutCommand()
            if (textReplyMessage.isNullOrEmpty()) return@forEach

            if (it.from.userName == botConfig.botUsername) {
                messages.add(ChatRoleMessage.Assistant(textReplyMessage))
            } else {
                messages.add(ChatRoleMessage.User(textReplyMessage))
            }
        }

        // System messages
        messages.add(ChatRoleMessage.System("The user is asking to provide a helpful reply to the above message."))
        messages.add(
            ChatRoleMessage.System(
                listOf(
                    "Please respond in the language specified by ISO code: ${language.iso}",
                    "Please limit your answer to four sentences, and refrain from using any Markdown or HTML."
                ).joinToString("\n")
            )
        )

        return messages
    }

    private fun Message.getAllReplyMessages(): List<Message> {
        val replies = mutableListOf<Message>()
        var currentReply = this.replyToMessage
        while (currentReply != null) {
            replies.add(currentReply)
            currentReply = currentReply.replyToMessage
        }
        return replies
    }
}
