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
public class TwitterProjectDependency extends DelegatingProjectDependency {

    @Inject
    public TwitterProjectDependency(TypeSafeProjectDependencyFactory factory, ProjectDependencyInternal delegate) {
        super(factory, delegate);
    }

    /**
     * Creates a project dependency on the project at path ":twitter:api"
     */
    public Twitter_ApiProjectDependency getApi() { return new Twitter_ApiProjectDependency(getFactory(), create(":twitter:api")); }

    /**
     * Creates a project dependency on the project at path ":twitter:e2e"
     */
    public Twitter_E2eProjectDependency getE2e() { return new Twitter_E2eProjectDependency(getFactory(), create(":twitter:e2e")); }

    /**
     * Creates a project dependency on the project at path ":twitter:fx"
     */
    public Twitter_FxProjectDependency getFx() { return new Twitter_FxProjectDependency(getFactory(), create(":twitter:fx")); }

    /**
     * Creates a project dependency on the project at path ":twitter:impl"
     */
    public Twitter_ImplProjectDependency getImpl() { return new Twitter_ImplProjectDependency(getFactory(), create(":twitter:impl")); }

    /**
     * Creates a project dependency on the project at path ":twitter:nitter"
     */
    public Twitter_NitterProjectDependency getNitter() { return new Twitter_NitterProjectDependency(getFactory(), create(":twitter:nitter")); }

}
