package com.programistich.twitterx.core.telegraph

open class TelegraphException(message: String? = null) : Exception(message)

class TelegraphCreateAccountException(message: String) : TelegraphException(message)

class TelegraphCreatePageException(message: String) : TelegraphException(message)
