package twitterx.translation.api

public interface TranslationService {
    public suspend fun translate(
        text: String,
        to: Language,
    ): Result<Translation>
}
