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
public class AiProjectDependency extends DelegatingProjectDependency {

    @Inject
    public AiProjectDependency(TypeSafeProjectDependencyFactory factory, ProjectDependencyInternal delegate) {
        super(factory, delegate);
    }

    /**
     * Creates a project dependency on the project at path ":ai:api"
     */
    public Ai_ApiProjectDependency getApi() { return new Ai_ApiProjectDependency(getFactory(), create(":ai:api")); }

    /**
     * Creates a project dependency on the project at path ":ai:e2e"
     */
    public Ai_E2eProjectDependency getE2e() { return new Ai_E2eProjectDependency(getFactory(), create(":ai:e2e")); }

    /**
     * Creates a project dependency on the project at path ":ai:google"
     */
    public Ai_GoogleProjectDependency getGoogle() { return new Ai_GoogleProjectDependency(getFactory(), create(":ai:google")); }

}
