package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.internal.artifacts.dependencies.ProjectDependencyInternal;
import org.gradle.api.internal.artifacts.DefaultProjectDependencyFactory;
import org.gradle.api.internal.artifacts.dsl.dependencies.ProjectFinder;
import org.gradle.api.internal.catalog.DelegatingProjectDependency;
import org.gradle.api.internal.catalog.TypeSafeProjectDependencyFactory;
import javax.inject.Inject;

@NonNullApi
public class TwitterxProjectDependency extends DelegatingProjectDependency {

    @Inject
    public TwitterxProjectDependency(TypeSafeProjectDependencyFactory factory, ProjectDependencyInternal delegate) {
        super(factory, delegate);
    }

    /**
     * Creates a project dependency on the project at path ":ai"
     */
    public AiProjectDependency getAi() { return new AiProjectDependency(getFactory(), create(":ai")); }

    /**
     * Creates a project dependency on the project at path ":app"
     */
    public AppProjectDependency getApp() { return new AppProjectDependency(getFactory(), create(":app")); }

    /**
     * Creates a project dependency on the project at path ":article"
     */
    public ArticleProjectDependency getArticle() { return new ArticleProjectDependency(getFactory(), create(":article")); }

    /**
     * Creates a project dependency on the project at path ":localization"
     */
    public LocalizationProjectDependency getLocalization() { return new LocalizationProjectDependency(getFactory(), create(":localization")); }

    /**
     * Creates a project dependency on the project at path ":telegram"
     */
    public TelegramProjectDependency getTelegram() { return new TelegramProjectDependency(getFactory(), create(":telegram")); }

    /**
     * Creates a project dependency on the project at path ":translations"
     */
    public TranslationsProjectDependency getTranslations() { return new TranslationsProjectDependency(getFactory(), create(":translations")); }

    /**
     * Creates a project dependency on the project at path ":twitter"
     */
    public TwitterProjectDependency getTwitter() { return new TwitterProjectDependency(getFactory(), create(":twitter")); }

    /**
     * Creates a project dependency on the project at path ":video"
     */
    public VideoProjectDependency getVideo() { return new VideoProjectDependency(getFactory(), create(":video")); }

}
