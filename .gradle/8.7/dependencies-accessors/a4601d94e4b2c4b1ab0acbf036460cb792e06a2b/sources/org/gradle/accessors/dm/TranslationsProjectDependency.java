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
public class TranslationsProjectDependency extends DelegatingProjectDependency {

    @Inject
    public TranslationsProjectDependency(TypeSafeProjectDependencyFactory factory, ProjectDependencyInternal delegate) {
        super(factory, delegate);
    }

    /**
     * Creates a project dependency on the project at path ":translations:api"
     */
    public Translations_ApiProjectDependency getApi() { return new Translations_ApiProjectDependency(getFactory(), create(":translations:api")); }

    /**
     * Creates a project dependency on the project at path ":translations:e2e"
     */
    public Translations_E2eProjectDependency getE2e() { return new Translations_E2eProjectDependency(getFactory(), create(":translations:e2e")); }

    /**
     * Creates a project dependency on the project at path ":translations:google"
     */
    public Translations_GoogleProjectDependency getGoogle() { return new Translations_GoogleProjectDependency(getFactory(), create(":translations:google")); }

}
