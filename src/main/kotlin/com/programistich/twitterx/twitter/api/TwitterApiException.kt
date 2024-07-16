package com.programistich.twitterx.twitter.api

open class TweetException(override val message: String) : Exception(message)

class PrivateTweetException : TweetException("This tweet is private")

class NotFoundTweetException : TweetException("Tweet not found")

class ApiFailTweetException : TweetException("API failure")

class LongTweetException : TweetException("Tweet so long")
