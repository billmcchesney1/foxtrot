package com.flipkart.foxtrot.server.di;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.foxtrot.common.ActionRequestVisitor;
import com.flipkart.foxtrot.common.TableActionRequestVisitor;
import com.flipkart.foxtrot.core.alerts.AlertingSystemEventConsumer;
import com.flipkart.foxtrot.core.cache.CacheFactory;
import com.flipkart.foxtrot.core.cache.CacheManager;
import com.flipkart.foxtrot.core.cache.impl.DistributedCacheFactory;
import com.flipkart.foxtrot.core.cardinality.CardinalityConfig;
import com.flipkart.foxtrot.core.common.DataDeletionManagerConfig;
import com.flipkart.foxtrot.core.config.ConsoleHistoryConfig;
import com.flipkart.foxtrot.server.config.FoxtrotServerConfiguration;
import com.flipkart.foxtrot.core.config.QueryConfig;
import com.flipkart.foxtrot.core.datastore.DataStore;
import com.flipkart.foxtrot.core.datastore.impl.hbase.HBaseDataStore;
import com.flipkart.foxtrot.core.datastore.impl.hbase.HBaseUtil;
import com.flipkart.foxtrot.core.datastore.impl.hbase.HbaseConfig;
import com.flipkart.foxtrot.core.email.EmailConfig;
import com.flipkart.foxtrot.core.email.messageformatting.EmailBodyBuilder;
import com.flipkart.foxtrot.core.email.messageformatting.EmailSubjectBuilder;
import com.flipkart.foxtrot.core.email.messageformatting.impl.StrSubstitutorEmailBodyBuilder;
import com.flipkart.foxtrot.core.email.messageformatting.impl.StrSubstitutorEmailSubjectBuilder;
import com.flipkart.foxtrot.core.funnel.config.BaseFunnelEventConfig;
import com.flipkart.foxtrot.core.funnel.config.FunnelConfiguration;
import com.flipkart.foxtrot.core.funnel.persistence.ElasticsearchFunnelStore;
import com.flipkart.foxtrot.core.funnel.persistence.FunnelStore;
import com.flipkart.foxtrot.core.funnel.services.FunnelService;
import com.flipkart.foxtrot.core.funnel.services.FunnelServiceImplV1;
import com.flipkart.foxtrot.core.internalevents.InternalEventBus;
import com.flipkart.foxtrot.core.internalevents.InternalEventBusConsumer;
import com.flipkart.foxtrot.core.internalevents.impl.GuavaInternalEventBus;
import com.flipkart.foxtrot.core.jobs.optimization.EsIndexOptimizationConfig;
import com.flipkart.foxtrot.core.lock.DistributedLock;
import com.flipkart.foxtrot.core.lock.HazelcastDistributedLock;
import com.flipkart.foxtrot.core.lock.HazelcastDistributedLockConfig;
import com.flipkart.foxtrot.core.querystore.ActionExecutionObserver;
import com.flipkart.foxtrot.core.querystore.EventPublisherActionExecutionObserver;
import com.flipkart.foxtrot.core.querystore.QueryStore;
import com.flipkart.foxtrot.core.querystore.actions.spi.ElasticsearchTuningConfig;
import com.flipkart.foxtrot.core.querystore.handlers.MetricRecorder;
import com.flipkart.foxtrot.core.querystore.handlers.ResponseCacheUpdater;
import com.flipkart.foxtrot.core.querystore.handlers.SlowQueryReporter;
import com.flipkart.foxtrot.core.querystore.impl.CacheConfig;
import com.flipkart.foxtrot.core.querystore.impl.ClusterConfig;
import com.flipkart.foxtrot.core.querystore.impl.ElasticsearchConfig;
import com.flipkart.foxtrot.core.querystore.impl.ElasticsearchQueryStore;
import com.flipkart.foxtrot.core.querystore.impl.ElasticsearchUtils;
import com.flipkart.foxtrot.core.querystore.mutator.IndexerEventMutator;
import com.flipkart.foxtrot.core.querystore.mutator.LargeTextNodeRemover;
import com.flipkart.foxtrot.core.reroute.ClusterRerouteConfig;
import com.flipkart.foxtrot.core.table.TableManager;
import com.flipkart.foxtrot.core.table.TableMetadataManager;
import com.flipkart.foxtrot.core.table.impl.DistributedTableMetadataManager;
import com.flipkart.foxtrot.core.table.impl.FoxtrotTableManager;
import com.flipkart.foxtrot.server.auth.AuthConfig;
import com.flipkart.foxtrot.server.auth.AuthStore;
import com.flipkart.foxtrot.server.auth.ESAuthStore;
import com.flipkart.foxtrot.server.auth.JwtConfig;
import com.flipkart.foxtrot.server.auth.RoleAuthorizer;
import com.flipkart.foxtrot.server.auth.TokenAuthenticator;
import com.flipkart.foxtrot.server.auth.TokenType;
import com.flipkart.foxtrot.server.auth.UserPrincipal;
import com.flipkart.foxtrot.server.auth.authprovider.AuthProvider;
import com.flipkart.foxtrot.server.auth.authprovider.impl.GoogleAuthProvider;
import com.flipkart.foxtrot.server.auth.authprovider.impl.GoogleAuthProviderConfig;
import com.flipkart.foxtrot.server.auth.sessionstore.DistributedSessionDataStore;
import com.flipkart.foxtrot.server.auth.sessionstore.SessionDataStore;
import com.flipkart.foxtrot.server.console.ConsolePersistence;
import com.flipkart.foxtrot.server.console.ElasticsearchConsolePersistence;
import com.flipkart.foxtrot.server.jobs.sessioncleanup.SessionCleanupConfig;
import com.flipkart.foxtrot.sql.fqlstore.FqlStoreService;
import com.flipkart.foxtrot.sql.fqlstore.FqlStoreServiceImpl;
import com.foxtrot.flipkart.translator.config.SegregationConfiguration;
import com.foxtrot.flipkart.translator.config.TranslatorConfig;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.phonepe.platform.http.OkHttpUtils;
import com.phonepe.platform.http.ServiceEndpointProvider;
import com.phonepe.platform.http.ServiceEndpointProviderFactory;
import io.appform.dropwizard.discovery.bundle.ServiceDiscoveryBundle;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.CachingAuthorizer;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import org.apache.hadoop.conf.Configuration;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.HmacKey;

/**
 *
 */
public class FoxtrotModule extends AbstractModule {

    private final ServiceDiscoveryBundle<FoxtrotServerConfiguration> serviceDiscoveryBundle;

    public FoxtrotModule(final ServiceDiscoveryBundle<FoxtrotServerConfiguration> serviceDiscoveryBundle) {
        this.serviceDiscoveryBundle = serviceDiscoveryBundle;
    }

    @Override
    protected void configure() {
        bind(TableMetadataManager.class).to(DistributedTableMetadataManager.class);
        bind(DataStore.class).to(HBaseDataStore.class);
        bind(QueryStore.class).to(ElasticsearchQueryStore.class);
        bind(FqlStoreService.class).to(FqlStoreServiceImpl.class);
        bind(CacheFactory.class).to(DistributedCacheFactory.class);
        bind(InternalEventBus.class).to(GuavaInternalEventBus.class);
        bind(InternalEventBusConsumer.class).to(AlertingSystemEventConsumer.class);
        bind(ConsolePersistence.class).to(ElasticsearchConsolePersistence.class);
        bind(EmailSubjectBuilder.class).to(StrSubstitutorEmailSubjectBuilder.class);
        bind(EmailBodyBuilder.class).to(StrSubstitutorEmailBodyBuilder.class);
        bind(TableManager.class).to(FoxtrotTableManager.class);
        bind(new TypeLiteral<ActionRequestVisitor<String>>() {
        }).toInstance(new TableActionRequestVisitor());
        bind(FunnelService.class).annotatedWith(Names.named("FunnelServiceImplV1"))
                .to(FunnelServiceImplV1.class);
        bind(FunnelService.class).to(FunnelServiceImplV1.class);
        bind(FunnelStore.class).to(ElasticsearchFunnelStore.class);
        bind(DistributedLock.class).to(HazelcastDistributedLock.class);
        bind(new TypeLiteral<List<HealthCheck>>() {
        }).toProvider(HealthcheckListProvider.class);
        bind(AuthStore.class).to(ESAuthStore.class);
        bind(AuthProvider.class).to(GoogleAuthProvider.class);
        bind(SessionDataStore.class).to(DistributedSessionDataStore.class);

    }

    @Provides
    @Singleton
    public HbaseConfig hbConfig(FoxtrotServerConfiguration configuration) {
        return configuration.getHbase();
    }

    @Provides
    @Singleton
    public ElasticsearchConfig esConfig(FoxtrotServerConfiguration configuration) {
        return configuration.getElasticsearch();
    }

    @Provides
    @Singleton
    public ClusterConfig clusterConfig(FoxtrotServerConfiguration configuration) {
        return configuration.getCluster();
    }

    @Provides
    @Singleton
    public CardinalityConfig cardinalityConfig(FoxtrotServerConfiguration configuration) {
        return null == configuration.getCardinality() ? new CardinalityConfig("false",
                String.valueOf(ElasticsearchUtils.DEFAULT_SUB_LIST_SIZE)) : configuration.getCardinality();
    }

    @Provides
    @Singleton
    public EsIndexOptimizationConfig esIndexOptimizationConfig(FoxtrotServerConfiguration configuration) {
        return null == configuration.getEsIndexOptimizationConfig() ? new EsIndexOptimizationConfig()
                : configuration.getEsIndexOptimizationConfig();
    }

    @Provides
    @Singleton
    public DataDeletionManagerConfig dataDeletionManagerConfig(FoxtrotServerConfiguration configuration) {
        return configuration.getDeletionManagerConfig();
    }

    @Provides
    @Singleton
    public ConsoleHistoryConfig consoleHistoryConfig(FoxtrotServerConfiguration configuration) {
        return null == configuration.getConsoleHistoryConfig() ? new ConsoleHistoryConfig()
                : configuration.getConsoleHistoryConfig();
    }

    @Provides
    @Singleton
    public ClusterRerouteConfig clusterRerouteConfig(FoxtrotServerConfiguration configuration) {
        return null == configuration.getClusterRerouteConfig() ? new ClusterRerouteConfig()
                : configuration.getClusterRerouteConfig();
    }

    public SessionCleanupConfig sessionCleanupConfig(FoxtrotServerConfiguration configuration) {
        return null == configuration.getSessionCleanupConfig() ? new SessionCleanupConfig()
                : configuration.getSessionCleanupConfig();
    }

    @Provides
    @Singleton
    public CacheConfig cacheConfig(FoxtrotServerConfiguration configuration) {
        return configuration.getCacheConfig();
    }

    @Provides
    @Singleton
    public EmailConfig emailConfig(FoxtrotServerConfiguration configuration) {
        return configuration.getEmailConfig();
    }

    @Provides
    @Singleton
    public SegregationConfiguration segregationConfiguration(FoxtrotServerConfiguration configuration) {
        return configuration.getSegregationConfiguration();
    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper(Environment environment) {
        return environment.getObjectMapper();
    }

    @Provides
    @Singleton
    public List<IndexerEventMutator> provideMutators(FoxtrotServerConfiguration configuration,
            ObjectMapper objectMapper) {
        return Collections.singletonList(new LargeTextNodeRemover(objectMapper, configuration.getTextNodeRemover()));
    }

    @Provides
    @Singleton
    public List<ActionExecutionObserver> actionExecutionObservers(CacheManager cacheManager,
            InternalEventBus eventBus) {
        return ImmutableList.<ActionExecutionObserver>builder().add(new MetricRecorder())
                .add(new ResponseCacheUpdater(cacheManager))
                .add(new SlowQueryReporter())
                .add(new EventPublisherActionExecutionObserver(eventBus))
                .build();
    }

    @Provides
    @Singleton
    public ExecutorService provideGlobalExecutorService(Environment environment) {
        return environment.lifecycle()
                .executorService("query-executor-%s")
                .minThreads(20)
                .maxThreads(30)
                .keepAliveTime(Duration.seconds(30))
                .build();
    }

    @Provides
    @Singleton
    public ScheduledExecutorService provideGlobalScheduledExecutorService(Environment environment) {
        return environment.lifecycle()
                .scheduledExecutorService("cardinality-executor")
                .threads(1)
                .build();
    }

    @Provides
    @Singleton
    public FunnelConfiguration funnelConfig(FoxtrotServerConfiguration configuration) {
        return configuration.getFunnelConfiguration() != null ? configuration.getFunnelConfiguration()
                : FunnelConfiguration.builder()
                        .baseFunnelEventConfig(BaseFunnelEventConfig.builder()
                                .eventType("APP_LOADED")
                                .category("APP_LOADED")
                                .build())
                        .querySize(100)
                        .build();
    }

    @Provides
    @Singleton
    public Configuration provideHBaseConfiguration(HbaseConfig hbaseConfig) throws IOException {
        return HBaseUtil.create(hbaseConfig);
    }

    @Provides
    @Singleton
    public HazelcastDistributedLockConfig hazelcastDistributedLockConfig(FoxtrotServerConfiguration configuration) {
        return configuration.getDistributedLockConfig();
    }


    @Provides
    @Singleton
    public AuthConfig authConfig(FoxtrotServerConfiguration serverConfiguration) {
        return serverConfiguration.getAuth();
    }

    @Provides
    @Singleton
    public GoogleAuthProviderConfig googleAuthProviderConfig(FoxtrotServerConfiguration configuration) {
        return (GoogleAuthProviderConfig) configuration.getAuth()
                .getProvider();
    }

    @Provides
    @Singleton
    public JwtConsumer provideJwtConsumer(AuthConfig config) {
        final JwtConfig jwtConfig = config.getJwt();
        final byte[] secretKey = jwtConfig.getPrivateKey()
                .getBytes(StandardCharsets.UTF_8);
        return new JwtConsumerBuilder().setRequireIssuedAt()
                .setRequireSubject()
                .setExpectedIssuer(jwtConfig.getIssuerId())
                .setVerificationKey(new HmacKey(secretKey))
                .setJwsAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                        AlgorithmIdentifiers.HMAC_SHA512))
                .setExpectedAudience(Arrays.stream(TokenType.values())
                        .map(TokenType::name)
                        .toArray(String[]::new))
                .build();
    }

    @Provides
    @Singleton
    public Authenticator<JwtContext, UserPrincipal> authenticator(final Environment environment,
            final TokenAuthenticator authenticator, final AuthConfig authConfig) {
        return new CachingAuthenticator<>(environment.metrics(), authenticator, CacheBuilderSpec.parse(
                authConfig.getJwt()
                        .getAuthCachePolicy()));
    }

    @Provides
    @Singleton
    public Authorizer<UserPrincipal> authorizer(final Environment environment, final RoleAuthorizer authorizer,
            final AuthConfig authConfig) {
        return new CachingAuthorizer<>(environment.metrics(), authorizer, CacheBuilderSpec.parse(authConfig.getJwt()
                .getAuthCachePolicy()));
    }

    @Provides
    @Singleton
    public ElasticsearchTuningConfig provideElasticsearchTuningConfig(FoxtrotServerConfiguration configuration) {
        return configuration.getElasticsearchTuningConfig();
    }

    @Provides
    @Singleton
    @Named("GandalfServiceEndpointProvider")
    ServiceEndpointProvider provideGandalfServiceEndpointProvider(FoxtrotServerConfiguration configuration,
            Environment environment) {
        ServiceEndpointProviderFactory serviceEndpointFactory = new ServiceEndpointProviderFactory(
                this.serviceDiscoveryBundle.getCurator());
        return serviceEndpointFactory.provider(configuration.getGandalfConfig()
                .getHttpConfig(), environment);
    }

    @Provides
    @Singleton
    @Named("GandalfOkHttpClient")
    OkHttpClient provideGandalfOkHttpClient(Environment environment, FoxtrotServerConfiguration configuration)
            throws GeneralSecurityException, IOException {
        return OkHttpUtils.createDefaultClient("foxtrot-gandalf-client", environment.metrics(),
                configuration.getGandalfConfig()
                        .getHttpConfig());
    }

    @Provides
    @Singleton
    public QueryConfig providerQueryConfig(FoxtrotServerConfiguration configuration) {
        return configuration.getQueryConfig();
    }

    @Provides
    @Singleton
    public TranslatorConfig translatorConfig(FoxtrotServerConfiguration configuration) {
        return configuration.getTranslatorConfig();
    }

    @Provides
    @Singleton
    public ServerFactory serverFactory(FoxtrotServerConfiguration configuration) {
        return configuration.getServerFactory();
    }

    @Provides
    @Singleton
    public boolean restrictAccess(FoxtrotServerConfiguration configuration){
        return configuration.isRestrictAccess();
    }

}
