package com.programistich.twitterx.core.twitter

open class TwitterException(override val message: String) : Exception(message)

class PrivateTwitterException : TwitterException("This tweet is private")

class NotFoundTwitterException : TwitterException("Tweet not found")

class ApiFailTwitterException : TwitterException("API failure")
