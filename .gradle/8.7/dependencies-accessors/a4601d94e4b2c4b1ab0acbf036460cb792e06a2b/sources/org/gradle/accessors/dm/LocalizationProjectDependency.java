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
public class LocalizationProjectDependency extends DelegatingProjectDependency {

    @Inject
    public LocalizationProjectDependency(TypeSafeProjectDependencyFactory factory, ProjectDependencyInternal delegate) {
        super(factory, delegate);
    }

    /**
     * Creates a project dependency on the project at path ":localization:api"
     */
    public Localization_ApiProjectDependency getApi() { return new Localization_ApiProjectDependency(getFactory(), create(":localization:api")); }

    /**
     * Creates a project dependency on the project at path ":localization:impl"
     */
    public Localization_ImplProjectDependency getImpl() { return new Localization_ImplProjectDependency(getFactory(), create(":localization:impl")); }

}
