package com.programistich.twitterx.twitter

private const val TWEET_REGEX = "https://(?:mobile.)?(?:twitter.com|x.com)/([a-zA-Z0-9_]+)/status/([0-9]+)?(.*)"

fun String.getTweetIds(): List<String> {
    return TWEET_REGEX.toRegex().findAll(this).map { it.groupValues[2] }.toList()
}
