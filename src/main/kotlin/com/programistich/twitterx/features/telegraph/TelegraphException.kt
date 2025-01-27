package com.programistich.twitterx.features.telegraph

open class TelegraphException(message: String? = null) : Exception(message)

class TelegraphCreateAccountException(message: String) : TelegraphException(message)

class TelegraphCreatePageException(message: String) : TelegraphException(message)
