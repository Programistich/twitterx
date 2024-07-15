package com.programistich.twitterx.twitter.api

class PrivateTweetException : Exception("This tweet is private")

class NotFoundTweetException : Exception("Tweet not found")

class ApiFailTweetException : Exception("API failure")
