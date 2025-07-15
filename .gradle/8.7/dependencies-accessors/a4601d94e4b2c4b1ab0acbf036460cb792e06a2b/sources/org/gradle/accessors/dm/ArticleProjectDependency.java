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
public class ArticleProjectDependency extends DelegatingProjectDependency {

    @Inject
    public ArticleProjectDependency(TypeSafeProjectDependencyFactory factory, ProjectDependencyInternal delegate) {
        super(factory, delegate);
    }

    /**
     * Creates a project dependency on the project at path ":article:api"
     */
    public Article_ApiProjectDependency getApi() { return new Article_ApiProjectDependency(getFactory(), create(":article:api")); }

    /**
     * Creates a project dependency on the project at path ":article:e2e"
     */
    public Article_E2eProjectDependency getE2e() { return new Article_E2eProjectDependency(getFactory(), create(":article:e2e")); }

    /**
     * Creates a project dependency on the project at path ":article:telegraph"
     */
    public Article_TelegraphProjectDependency getTelegraph() { return new Article_TelegraphProjectDependency(getFactory(), create(":article:telegraph")); }

}
