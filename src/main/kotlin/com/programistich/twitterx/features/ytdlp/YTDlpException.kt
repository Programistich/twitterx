package com.programistich.twitterx.features.ytdlp

open class YTDlpException : Exception()

class UnAuthorizedException : YTDlpException()

class RateLimitException : YTDlpException()
