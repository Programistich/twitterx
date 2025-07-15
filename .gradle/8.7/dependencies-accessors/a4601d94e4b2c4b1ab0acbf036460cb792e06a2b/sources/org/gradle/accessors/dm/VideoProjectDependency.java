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
public class VideoProjectDependency extends DelegatingProjectDependency {

    @Inject
    public VideoProjectDependency(TypeSafeProjectDependencyFactory factory, ProjectDependencyInternal delegate) {
        super(factory, delegate);
    }

    /**
     * Creates a project dependency on the project at path ":video:api"
     */
    public Video_ApiProjectDependency getApi() { return new Video_ApiProjectDependency(getFactory(), create(":video:api")); }

    /**
     * Creates a project dependency on the project at path ":video:e2e"
     */
    public Video_E2eProjectDependency getE2e() { return new Video_E2eProjectDependency(getFactory(), create(":video:e2e")); }

    /**
     * Creates a project dependency on the project at path ":video:ytdlp"
     */
    public Video_YtdlpProjectDependency getYtdlp() { return new Video_YtdlpProjectDependency(getFactory(), create(":video:ytdlp")); }

}
