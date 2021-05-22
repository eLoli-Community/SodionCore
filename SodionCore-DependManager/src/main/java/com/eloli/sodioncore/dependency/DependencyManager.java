package com.eloli.sodioncore.dependency;

import com.eloli.sodioncore.dependency.classloader.ReflectionClassLoader;
import com.eloli.sodioncore.file.BaseFileService;
import com.eloli.sodioncore.logger.AbstractLogger;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class DependencyManager {

    private final ReflectionClassLoader reflectionClassLoader = new ReflectionClassLoader();
    private final RepositorySystem repositorySystem;
    private final DefaultRepositorySystemSession repositorySystemSession;
    private final List<Relocation> rules;
    private final String mapName;

    private final BaseFileService fileService;
    private final String mavenRepo;
    private final AbstractLogger logger;

    public DependencyManager(BaseFileService fileService, AbstractLogger logger, Map<String, String> relocateMap, String mapName, String mavenRepo) {
        this.fileService = fileService;
        this.logger = logger;
        this.mavenRepo = mavenRepo;
        rules = new ArrayList<>();
        rules.add(new Relocation("org.apache.maven", "com.eloli.sodioncore.libs.maven"));
        rules.add(new Relocation("org.apache.http", "com.eloli.sodioncore.libs.http"));
        rules.add(new Relocation("org.apache.commons", "com.eloli.sodioncore.libs.commons"));
        rules.add(new Relocation("org.objectweb.asm", "com.eloli.sodioncore.libs.asm"));
        rules.add(new Relocation("org.codehaus.plexus", "com.eloli.sodioncore.libs.plexus"));
        relocateMap.forEach((key, value) -> {
            rules.add(new Relocation(key, value));
        });
        this.mapName = mapName;

        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                logger.warn(String.format("Service creation failed for %s with implementation %s", type, impl), new Exception(exception));
            }
        });

        repositorySystem = locator.getService(RepositorySystem.class);

        repositorySystemSession = MavenRepositorySystemUtils.newSession();
        repositorySystemSession.setTransferListener(new ConsoleTransferListener(logger));
        repositorySystemSession.setRepositoryListener(new ConsoleRepositoryListener(logger));
        String librariesPath = fileService.getConfigPath("libraries");
        new File(librariesPath).mkdirs();
        LocalRepository localRepo = new LocalRepository(librariesPath);
        repositorySystemSession.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(repositorySystemSession, localRepo));
        repositorySystemSession.setSystemProperties(System.getProperties());
        repositorySystemSession.setConfigProperties(System.getProperties());
        repositorySystemSession.setSystemProperty("os.detected.name", System.getProperty("os.name", "unknown").toLowerCase());
        repositorySystemSession.setSystemProperty("os.detected.arch", System.getProperty("os.arch", "unknown").replaceAll("amd64", "x86_64"));
    }

    public void checkDependencyMaven(String packageName) {
        String[] splits = packageName.split(":");

        String group = splits[0];
        String name = splits[1];
        String version = splits[2];
        String className = splits[3];

        try {
            Class.forName(className);
            return;
        } catch (ClassNotFoundException ignore) {

        }

        File librariesPath = new File(fileService.getConfigPath("libraries"));
        librariesPath.mkdirs();

        Artifact artifact = new DefaultArtifact(group, name, "jar", version);
        DependencyFilter dependencyFilter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
        collectRequest.setRepositories(Collections.singletonList(new RemoteRepository.Builder("central", "default", mavenRepo).build()));
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, dependencyFilter);

        List<ArtifactResult> artifactResults;
        try {
            artifactResults = new LinkedList<>(repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest).getArtifactResults());
        } catch (DependencyResolutionException e) {
            logger.warn("Error resolving dependencies", e);
            return;
        }

        for (ArtifactResult artifactResult : artifactResults) {
            if (artifactResult.isResolved()) {
                String sourcePath = artifactResult.getArtifact().getFile().toPath().toString();
                String relocatedPath = sourcePath.substring(0, sourcePath.length() - ".jar".length())
                        + "-" + mapName + "-relocated.jar";
                File sourceFile = new File(sourcePath);
                File relocatedFile = new File(relocatedPath);
                if (!relocatedFile.exists()) {
                    JarRelocator relocator = new JarRelocator(sourceFile, relocatedFile, rules);
                    try {
                        relocator.run();
                    } catch (Exception e) {
                        relocatedFile.delete();
                        throw new RuntimeException("Unable to relocate dependencies " + sourcePath, e);
                    }
                }
                reflectionClassLoader.addJarToClasspath(Paths.get(relocatedPath));
            } else {
                logger.info("Failed " +
                        artifactResult.getArtifact().getGroupId()
                        + ":" + artifactResult.getArtifact().getArtifactId()
                        + ":" + artifactResult.getArtifact().getVersion());
            }
        }
    }

    public DependencyManager addMap(Map<String, String> map) {
        map.forEach((key, value) -> {
            rules.add(new Relocation(key, value));
        });
        return this;
    }

    public static class ConsoleRepositoryListener extends AbstractRepositoryListener {
        private final AbstractLogger logger;

        public ConsoleRepositoryListener(AbstractLogger logger) {
            this.logger = logger;
        }

        public void artifactDeployed(RepositoryEvent event) {
            //
        }

        public void artifactDeploying(RepositoryEvent event) {
            //
        }

        public void artifactDescriptorInvalid(RepositoryEvent event) {
            logger.warn("Invalid artifact descriptor for " + event.getArtifact() + ": "
                    + event.getException().getMessage());
        }

        public void artifactDescriptorMissing(RepositoryEvent event) {
            logger.warn("Missing artifact descriptor for " + event.getArtifact());
        }

        public void artifactInstalled(RepositoryEvent event) {
            //
        }

        public void artifactInstalling(RepositoryEvent event) {
            //
        }

        public void artifactResolved(RepositoryEvent event) {
            //;
        }

        public void artifactDownloading(RepositoryEvent event) {
            //
        }

        public void artifactDownloaded(RepositoryEvent event) {
            //
        }

        public void artifactResolving(RepositoryEvent event) {
            //
        }

        public void metadataDeployed(RepositoryEvent event) {
            //
        }

        public void metadataDeploying(RepositoryEvent event) {
            //
        }

        public void metadataInstalled(RepositoryEvent event) {
            //
        }

        public void metadataInstalling(RepositoryEvent event) {
            //
        }

        public void metadataInvalid(RepositoryEvent event) {
            logger.warn("Invalid metadata " + event.getMetadata());
        }

        public void metadataResolved(RepositoryEvent event) {
            //
        }

        public void metadataResolving(RepositoryEvent event) {
            //
        }
    }

    public static class ConsoleTransferListener extends AbstractTransferListener {
        private final AbstractLogger logger;
        private long time = System.currentTimeMillis();

        public ConsoleTransferListener(AbstractLogger logger) {
            this.logger = logger;
        }

        @Override
        public void transferInitiated(TransferEvent event) {
            //
        }

        @Override
        public void transferProgressed(TransferEvent event) {
            long now = System.currentTimeMillis();
            if (time + 1000 < now) {
                time = now;
                logger.info("Downloading: " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName()
                        + "( " + (int) (event.getTransferredBytes() * 100 / event.getResource().getContentLength()) + "%)");
            }
        }

        @Override
        public void transferSucceeded(TransferEvent event) {
            //
        }

        @Override
        public void transferFailed(TransferEvent event) {
            //
        }
    }

}