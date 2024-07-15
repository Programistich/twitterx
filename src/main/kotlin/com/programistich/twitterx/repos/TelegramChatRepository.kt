package com.programistich.twitterx.repos

import com.programistich.twitterx.entities.TelegramChat
import org.springframework.data.jpa.repository.JpaRepository

interface TelegramChatRepository : JpaRepository<TelegramChat, Long>
