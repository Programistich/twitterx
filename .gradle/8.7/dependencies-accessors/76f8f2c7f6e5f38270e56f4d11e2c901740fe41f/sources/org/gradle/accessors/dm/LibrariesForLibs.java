package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the {@code libs} extension.
 */
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final KotlinLibraryAccessors laccForKotlinLibraryAccessors = new KotlinLibraryAccessors(owner);
    private final KotlinxLibraryAccessors laccForKotlinxLibraryAccessors = new KotlinxLibraryAccessors(owner);
    private final KtorLibraryAccessors laccForKtorLibraryAccessors = new KtorLibraryAccessors(owner);
    private final LogbackLibraryAccessors laccForLogbackLibraryAccessors = new LogbackLibraryAccessors(owner);
    private final Slf4jLibraryAccessors laccForSlf4jLibraryAccessors = new Slf4jLibraryAccessors(owner);
    private final SpringLibraryAccessors laccForSpringLibraryAccessors = new SpringLibraryAccessors(owner);
    private final TelegramLibraryAccessors laccForTelegramLibraryAccessors = new TelegramLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

    /**
     * Group of libraries at <b>kotlin</b>
     */
    public KotlinLibraryAccessors getKotlin() {
        return laccForKotlinLibraryAccessors;
    }

    /**
     * Group of libraries at <b>kotlinx</b>
     */
    public KotlinxLibraryAccessors getKotlinx() {
        return laccForKotlinxLibraryAccessors;
    }

    /**
     * Group of libraries at <b>ktor</b>
     */
    public KtorLibraryAccessors getKtor() {
        return laccForKtorLibraryAccessors;
    }

    /**
     * Group of libraries at <b>logback</b>
     */
    public LogbackLibraryAccessors getLogback() {
        return laccForLogbackLibraryAccessors;
    }

    /**
     * Group of libraries at <b>slf4j</b>
     */
    public Slf4jLibraryAccessors getSlf4j() {
        return laccForSlf4jLibraryAccessors;
    }

    /**
     * Group of libraries at <b>spring</b>
     */
    public SpringLibraryAccessors getSpring() {
        return laccForSpringLibraryAccessors;
    }

    /**
     * Group of libraries at <b>telegram</b>
     */
    public TelegramLibraryAccessors getTelegram() {
        return laccForTelegramLibraryAccessors;
    }

    /**
     * Group of versions at <b>versions</b>
     */
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    /**
     * Group of bundles at <b>bundles</b>
     */
    public BundleAccessors getBundles() {
        return baccForBundleAccessors;
    }

    /**
     * Group of plugins at <b>plugins</b>
     */
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    public static class KotlinLibraryAccessors extends SubDependencyFactory {

        public KotlinLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>reflect</b> with <b>org.jetbrains.kotlin:kotlin-reflect</b> coordinates and
         * with version reference <b>kotlin</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getReflect() {
            return create("kotlin.reflect");
        }

        /**
         * Dependency provider for <b>test</b> with <b>org.jetbrains.kotlin:kotlin-test</b> coordinates and
         * with version reference <b>kotlin</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getTest() {
            return create("kotlin.test");
        }

    }

    public static class KotlinxLibraryAccessors extends SubDependencyFactory {
        private final KotlinxCoroutinesLibraryAccessors laccForKotlinxCoroutinesLibraryAccessors = new KotlinxCoroutinesLibraryAccessors(owner);
        private final KotlinxSerializationLibraryAccessors laccForKotlinxSerializationLibraryAccessors = new KotlinxSerializationLibraryAccessors(owner);

        public KotlinxLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>kotlinx.coroutines</b>
         */
        public KotlinxCoroutinesLibraryAccessors getCoroutines() {
            return laccForKotlinxCoroutinesLibraryAccessors;
        }

        /**
         * Group of libraries at <b>kotlinx.serialization</b>
         */
        public KotlinxSerializationLibraryAccessors getSerialization() {
            return laccForKotlinxSerializationLibraryAccessors;
        }

    }

    public static class KotlinxCoroutinesLibraryAccessors extends SubDependencyFactory {

        public KotlinxCoroutinesLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>core</b> with <b>org.jetbrains.kotlinx:kotlinx-coroutines-core</b> coordinates and
         * with version reference <b>kotlinx.coroutines</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCore() {
            return create("kotlinx.coroutines.core");
        }

        /**
         * Dependency provider for <b>test</b> with <b>org.jetbrains.kotlinx:kotlinx-coroutines-test</b> coordinates and
         * with version reference <b>kotlinx.coroutines</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getTest() {
            return create("kotlinx.coroutines.test");
        }

    }

    public static class KotlinxSerializationLibraryAccessors extends SubDependencyFactory {

        public KotlinxSerializationLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>json</b> with <b>org.jetbrains.kotlinx:kotlinx-serialization-json</b> coordinates and
         * with version reference <b>kotlinx.serialization.json</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJson() {
            return create("kotlinx.serialization.json");
        }

    }

    public static class KtorLibraryAccessors extends SubDependencyFactory {
        private final KtorClientLibraryAccessors laccForKtorClientLibraryAccessors = new KtorClientLibraryAccessors(owner);

        public KtorLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Group of libraries at <b>ktor.client</b>
         */
        public KtorClientLibraryAccessors getClient() {
            return laccForKtorClientLibraryAccessors;
        }

    }

    public static class KtorClientLibraryAccessors extends SubDependencyFactory {
        private final KtorClientContentLibraryAccessors laccForKtorClientContentLibraryAccessors = new KtorClientContentLibraryAccessors(owner);
        private final KtorClientSerializationLibraryAccessors laccForKtorClientSerializationLibraryAccessors = new KtorClientSerializationLibraryAccessors(owner);

        public KtorClientLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>cio</b> with <b>io.ktor:ktor-client-cio</b> coordinates and
         * with version reference <b>ktor</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCio() {
            return create("ktor.client.cio");
        }

        /**
         * Dependency provider for <b>core</b> with <b>io.ktor:ktor-client-core</b> coordinates and
         * with version reference <b>ktor</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getCore() {
            return create("ktor.client.core");
        }

        /**
         * Dependency provider for <b>logging</b> with <b>io.ktor:ktor-client-logging</b> coordinates and
         * with version reference <b>ktor</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getLogging() {
            return create("ktor.client.logging");
        }

        /**
         * Dependency provider for <b>mock</b> with <b>io.ktor:ktor-client-mock</b> coordinates and
         * with version reference <b>ktor</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getMock() {
            return create("ktor.client.mock");
        }

        /**
         * Group of libraries at <b>ktor.client.content</b>
         */
        public KtorClientContentLibraryAccessors getContent() {
            return laccForKtorClientContentLibraryAccessors;
        }

        /**
         * Group of libraries at <b>ktor.client.serialization</b>
         */
        public KtorClientSerializationLibraryAccessors getSerialization() {
            return laccForKtorClientSerializationLibraryAccessors;
        }

    }

    public static class KtorClientContentLibraryAccessors extends SubDependencyFactory {

        public KtorClientContentLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>negotiation</b> with <b>io.ktor:ktor-client-content-negotiation</b> coordinates and
         * with version reference <b>ktor</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getNegotiation() {
            return create("ktor.client.content.negotiation");
        }

    }

    public static class KtorClientSerializationLibraryAccessors extends SubDependencyFactory implements DependencyNotationSupplier {

        public KtorClientSerializationLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>serialization</b> with <b>io.ktor:ktor-client-serialization</b> coordinates and
         * with version reference <b>ktor</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> asProvider() {
            return create("ktor.client.serialization");
        }

        /**
         * Dependency provider for <b>json</b> with <b>io.ktor:ktor-serialization-kotlinx-json</b> coordinates and
         * with version reference <b>ktor</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJson() {
            return create("ktor.client.serialization.json");
        }

    }

    public static class LogbackLibraryAccessors extends SubDependencyFactory {

        public LogbackLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>classic</b> with <b>ch.qos.logback:logback-classic</b> coordinates and
         * with version reference <b>logback</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getClassic() {
            return create("logback.classic");
        }

    }

    public static class Slf4jLibraryAccessors extends SubDependencyFactory {

        public Slf4jLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>api</b> with <b>org.slf4j:slf4j-api</b> coordinates and
         * with version reference <b>slf4j</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getApi() {
            return create("slf4j.api");
        }

    }

    public static class SpringLibraryAccessors extends SubDependencyFactory {
        private final SpringDataLibraryAccessors laccForSpringDataLibraryAccessors = new SpringDataLibraryAccessors(owner);

        public SpringLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>web</b> with <b>org.springframework.boot:spring-boot-starter-web</b> coordinates and
         * with <b>no version specified</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getWeb() {
            return create("spring.web");
        }

        /**
         * Group of libraries at <b>spring.data</b>
         */
        public SpringDataLibraryAccessors getData() {
            return laccForSpringDataLibraryAccessors;
        }

    }

    public static class SpringDataLibraryAccessors extends SubDependencyFactory {

        public SpringDataLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>jpa</b> with <b>org.springframework.boot:spring-boot-starter-data-jpa</b> coordinates and
         * with <b>no version specified</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJpa() {
            return create("spring.data.jpa");
        }

    }

    public static class TelegramLibraryAccessors extends SubDependencyFactory {

        public TelegramLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Dependency provider for <b>client</b> with <b>org.telegram:telegrambots-client</b> coordinates and
         * with version reference <b>telegram</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getClient() {
            return create("telegram.client");
        }

        /**
         * Dependency provider for <b>spring</b> with <b>org.telegram:telegrambots-springboot-longpolling-starter</b> coordinates and
         * with version reference <b>telegram</b>
         * <p>
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getSpring() {
            return create("telegram.spring");
        }

    }

    public static class VersionAccessors extends VersionFactory  {

        private final KotlinxVersionAccessors vaccForKotlinxVersionAccessors = new KotlinxVersionAccessors(providers, config);
        private final SpingVersionAccessors vaccForSpingVersionAccessors = new SpingVersionAccessors(providers, config);
        private final SpringVersionAccessors vaccForSpringVersionAccessors = new SpringVersionAccessors(providers, config);
        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>detekt</b> with value <b>1.23.8</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getDetekt() { return getVersion("detekt"); }

        /**
         * Version alias <b>kotlin</b> with value <b>2.2.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getKotlin() { return getVersion("kotlin"); }

        /**
         * Version alias <b>ktor</b> with value <b>3.2.1</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getKtor() { return getVersion("ktor"); }

        /**
         * Version alias <b>logback</b> with value <b>1.5.15</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getLogback() { return getVersion("logback"); }

        /**
         * Version alias <b>slf4j</b> with value <b>2.0.16</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getSlf4j() { return getVersion("slf4j"); }

        /**
         * Version alias <b>telegram</b> with value <b>9.0.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getTelegram() { return getVersion("telegram"); }

        /**
         * Group of versions at <b>versions.kotlinx</b>
         */
        public KotlinxVersionAccessors getKotlinx() {
            return vaccForKotlinxVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.sping</b>
         */
        public SpingVersionAccessors getSping() {
            return vaccForSpingVersionAccessors;
        }

        /**
         * Group of versions at <b>versions.spring</b>
         */
        public SpringVersionAccessors getSpring() {
            return vaccForSpringVersionAccessors;
        }

    }

    public static class KotlinxVersionAccessors extends VersionFactory  {

        private final KotlinxSerializationVersionAccessors vaccForKotlinxSerializationVersionAccessors = new KotlinxSerializationVersionAccessors(providers, config);
        public KotlinxVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>kotlinx.coroutines</b> with value <b>1.10.2</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getCoroutines() { return getVersion("kotlinx.coroutines"); }

        /**
         * Group of versions at <b>versions.kotlinx.serialization</b>
         */
        public KotlinxSerializationVersionAccessors getSerialization() {
            return vaccForKotlinxSerializationVersionAccessors;
        }

    }

    public static class KotlinxSerializationVersionAccessors extends VersionFactory  {

        public KotlinxSerializationVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>kotlinx.serialization.json</b> with value <b>1.9.0</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getJson() { return getVersion("kotlinx.serialization.json"); }

    }

    public static class SpingVersionAccessors extends VersionFactory  {

        private final SpingDependencyVersionAccessors vaccForSpingDependencyVersionAccessors = new SpingDependencyVersionAccessors(providers, config);
        public SpingVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Group of versions at <b>versions.sping.dependency</b>
         */
        public SpingDependencyVersionAccessors getDependency() {
            return vaccForSpingDependencyVersionAccessors;
        }

    }

    public static class SpingDependencyVersionAccessors extends VersionFactory  {

        public SpingDependencyVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>sping.dependency.management</b> with value <b>1.1.7</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getManagement() { return getVersion("sping.dependency.management"); }

    }

    public static class SpringVersionAccessors extends VersionFactory  {

        public SpringVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Version alias <b>spring.boot</b> with value <b>3.5.3</b>
         * <p>
         * If the version is a rich version and cannot be represented as a
         * single version string, an empty string is returned.
         * <p>
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> getBoot() { return getVersion("spring.boot"); }

    }

    public static class BundleAccessors extends BundleFactory {
        private final KtorBundleAccessors baccForKtorBundleAccessors = new KtorBundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
        private final LoggingBundleAccessors baccForLoggingBundleAccessors = new LoggingBundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

        /**
         * Group of bundles at <b>bundles.ktor</b>
         */
        public KtorBundleAccessors getKtor() {
            return baccForKtorBundleAccessors;
        }

        /**
         * Group of bundles at <b>bundles.logging</b>
         */
        public LoggingBundleAccessors getLogging() {
            return baccForLoggingBundleAccessors;
        }

    }

    public static class KtorBundleAccessors extends BundleFactory {

        public KtorBundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

        /**
         * Dependency bundle provider for <b>ktor.client</b> which contains the following dependencies:
         * <ul>
         *    <li>io.ktor:ktor-client-core</li>
         *    <li>io.ktor:ktor-client-logging</li>
         *    <li>io.ktor:ktor-client-cio</li>
         *    <li>io.ktor:ktor-client-content-negotiation</li>
         *    <li>io.ktor:ktor-client-serialization</li>
         *    <li>io.ktor:ktor-serialization-kotlinx-json</li>
         * </ul>
         * <p>
         * This bundle was declared in catalog libs.versions.toml
         */
        public Provider<ExternalModuleDependencyBundle> getClient() {
            return createBundle("ktor.client");
        }

    }

    public static class LoggingBundleAccessors extends BundleFactory {

        public LoggingBundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

        /**
         * Dependency bundle provider for <b>logging.api</b> which contains the following dependencies:
         * <ul>
         *    <li>org.slf4j:slf4j-api</li>
         * </ul>
         * <p>
         * This bundle was declared in catalog libs.versions.toml
         */
        public Provider<ExternalModuleDependencyBundle> getApi() {
            return createBundle("logging.api");
        }

        /**
         * Dependency bundle provider for <b>logging.impl</b> which contains the following dependencies:
         * <ul>
         *    <li>ch.qos.logback:logback-classic</li>
         * </ul>
         * <p>
         * This bundle was declared in catalog libs.versions.toml
         */
        public Provider<ExternalModuleDependencyBundle> getImpl() {
            return createBundle("logging.impl");
        }

    }

    public static class PluginAccessors extends PluginFactory {
        private final KotlinPluginAccessors paccForKotlinPluginAccessors = new KotlinPluginAccessors(providers, config);
        private final SpringPluginAccessors paccForSpringPluginAccessors = new SpringPluginAccessors(providers, config);

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Plugin provider for <b>detekt</b> with plugin id <b>io.gitlab.arturbosch.detekt</b> and
         * with version reference <b>detekt</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getDetekt() { return createPlugin("detekt"); }

        /**
         * Group of plugins at <b>plugins.kotlin</b>
         */
        public KotlinPluginAccessors getKotlin() {
            return paccForKotlinPluginAccessors;
        }

        /**
         * Group of plugins at <b>plugins.spring</b>
         */
        public SpringPluginAccessors getSpring() {
            return paccForSpringPluginAccessors;
        }

    }

    public static class KotlinPluginAccessors extends PluginFactory {

        public KotlinPluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Plugin provider for <b>kotlin.jvm</b> with plugin id <b>org.jetbrains.kotlin.jvm</b> and
         * with version reference <b>kotlin</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getJvm() { return createPlugin("kotlin.jvm"); }

        /**
         * Plugin provider for <b>kotlin.serialization</b> with plugin id <b>org.jetbrains.kotlin.plugin.serialization</b> and
         * with version reference <b>kotlin</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getSerialization() { return createPlugin("kotlin.serialization"); }

    }

    public static class SpringPluginAccessors extends PluginFactory {
        private final SpringDependencyPluginAccessors paccForSpringDependencyPluginAccessors = new SpringDependencyPluginAccessors(providers, config);

        public SpringPluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Plugin provider for <b>spring.boot</b> with plugin id <b>org.springframework.boot</b> and
         * with version reference <b>spring.boot</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getBoot() { return createPlugin("spring.boot"); }

        /**
         * Group of plugins at <b>plugins.spring.dependency</b>
         */
        public SpringDependencyPluginAccessors getDependency() {
            return paccForSpringDependencyPluginAccessors;
        }

    }

    public static class SpringDependencyPluginAccessors extends PluginFactory {

        public SpringDependencyPluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Plugin provider for <b>spring.dependency.management</b> with plugin id <b>io.spring.dependency-management</b> and
         * with version reference <b>sping.dependency.management</b>
         * <p>
         * This plugin was declared in catalog libs.versions.toml
         */
        public Provider<PluginDependency> getManagement() { return createPlugin("spring.dependency.management"); }

    }

}
