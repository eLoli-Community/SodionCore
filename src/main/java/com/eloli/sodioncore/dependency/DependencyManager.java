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
import org.eclipse.aether.transfer.MetadataNotFoundException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import java.io.File;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class DependencyManager {

    private final ReflectionClassLoader reflectionClassLoader = new ReflectionClassLoader();
    private final RepositorySystem repositorySystem;
    private final DefaultRepositorySystemSession repositorySystemSession;
    private final List<Relocation> rules;

    private final BaseFileService fileService;
    private final String mavenRepo;
    private final AbstractLogger logger;

    public DependencyManager(BaseFileService fileService, AbstractLogger logger, Map<String, String> relocateMap, String mavenRepo) {
        this.fileService = fileService;
        this.logger = logger;
        this.mavenRepo = mavenRepo;
        rules = new ArrayList<>();
        relocateMap.forEach((key, value) -> {
            rules.add(new Relocation(key, value));
        });

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

    public void checkDependencyMaven(String group, String name, String version) {

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
                String relocatedPath = sourcePath.substring(0, sourcePath.length() - ".jar".length()) + "-relocated.jar";
                File sourceFile = new File(sourcePath);
                File relocatedFile = new File(relocatedPath);
                if (!relocatedFile.exists()) {
                    logger.info("Relocating " +
                            artifactResult.getArtifact().getGroupId()
                            + ":" + artifactResult.getArtifact().getArtifactId()
                            + ":" + artifactResult.getArtifact().getVersion());
                    JarRelocator relocator = new JarRelocator(sourceFile, relocatedFile, rules);
                    try {
                        relocator.run();
                    } catch (Exception e) {
                        relocatedFile.delete();
                        throw new RuntimeException("Unable to relocate dependencies " + sourcePath, e);
                    }
                }
                logger.info("Injecting " +
                        artifactResult.getArtifact().getGroupId()
                        + ":" + artifactResult.getArtifact().getArtifactId()
                        + ":" + artifactResult.getArtifact().getVersion());
                reflectionClassLoader.addJarToClasspath(Paths.get(relocatedPath));
            } else {
                logger.info("Failed " +
                        artifactResult.getArtifact().getGroupId()
                        + ":" + artifactResult.getArtifact().getArtifactId()
                        + ":" + artifactResult.getArtifact().getVersion());
            }
        }
    }

    public static class ConsoleRepositoryListener extends AbstractRepositoryListener {
        private final AbstractLogger logger;

        public ConsoleRepositoryListener(AbstractLogger logger) {
            this.logger = logger;
        }

        public void artifactDeployed(RepositoryEvent event) {
            // logger.info("Deployed " + event.getArtifact() + " to " + event.getRepository());
        }

        public void artifactDeploying(RepositoryEvent event) {
            // logger.info("Deploying " + event.getArtifact() + " to " + event.getRepository());
        }

        public void artifactDescriptorInvalid(RepositoryEvent event) {
            logger.warn("Invalid artifact descriptor for " + event.getArtifact() + ": "
                    + event.getException().getMessage());
        }

        public void artifactDescriptorMissing(RepositoryEvent event) {
            logger.warn("Missing artifact descriptor for " + event.getArtifact());
        }

        public void artifactInstalled(RepositoryEvent event) {
            // logger.info("Installed " + event.getArtifact() + " to " + event.getFile());
        }

        public void artifactInstalling(RepositoryEvent event) {
            // logger.info("Installing " + event.getArtifact() + " to " + event.getFile());
        }

        public void artifactResolved(RepositoryEvent event) {
            // logger.info("Resolved artifact " + event.getArtifact() + " from " + event.getRepository());
        }

        public void artifactDownloading(RepositoryEvent event) {
            logger.info("Downloading artifact " + event.getArtifact() + " from " + event.getRepository());
        }

        public void artifactDownloaded(RepositoryEvent event) {
            logger.info("Downloaded artifact " + event.getArtifact() + " from " + event.getRepository());
        }

        public void artifactResolving(RepositoryEvent event) {
            // logger.info("Resolving artifact " + event.getArtifact());
        }

        public void metadataDeployed(RepositoryEvent event) {
            // logger.info("Deployed " + event.getMetadata() + " to " + event.getRepository());
        }

        public void metadataDeploying(RepositoryEvent event) {
            // logger.info("Deploying " + event.getMetadata() + " to " + event.getRepository());
        }

        public void metadataInstalled(RepositoryEvent event) {
            // logger.info("Installed " + event.getMetadata() + " to " + event.getFile());
        }

        public void metadataInstalling(RepositoryEvent event) {
            // logger.info("Installing " + event.getMetadata() + " to " + event.getFile());
        }

        public void metadataInvalid(RepositoryEvent event) {
            logger.warn("Invalid metadata " + event.getMetadata());
        }

        public void metadataResolved(RepositoryEvent event) {
            logger.warn("Resolved metadata " + event.getMetadata() + " from " + event.getRepository());
        }

        public void metadataResolving(RepositoryEvent event) {
            // logger.info("Resolving metadata " + event.getMetadata() + " from " + event.getRepository());
        }
    }

    public static class ConsoleTransferListener extends AbstractTransferListener {
        private final AbstractLogger logger;

        private int lastLength;

        public ConsoleTransferListener(AbstractLogger logger) {
            this.logger = logger;
        }

        @Override
        public void transferInitiated(TransferEvent event) {
            //
        }

        @Override
        public void transferProgressed(TransferEvent event) {
            //
        }

        private String getStatus(long complete, long total) {
            if (total >= 1024) {
                return toKB(complete) + "/" + toKB(total) + " KB ";
            } else if (total >= 0) {
                return complete + "/" + total + " B ";
            } else if (complete >= 1024) {
                return toKB(complete) + " KB ";
            } else {
                return complete + " B ";
            }
        }

        private void pad(StringBuilder buffer, int spaces) {
            String block = "                                        ";
            while (spaces > 0) {
                int n = Math.min(spaces, block.length());
                buffer.append(block, 0, n);
                spaces -= n;
            }
        }

        @Override
        public void transferSucceeded(TransferEvent event) {
            transferCompleted(event);

            TransferResource resource = event.getResource();
            long contentLength = event.getTransferredBytes();
            if (contentLength >= 0) {
                String type = (event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploaded" : "Downloaded");
                String len = contentLength >= 1024 ? toKB(contentLength) + " KB" : contentLength + " B";

                String throughput = "";
                long duration = System.currentTimeMillis() - resource.getTransferStartTime();
                if (duration > 0) {
                    long bytes = contentLength - resource.getResumeOffset();
                    DecimalFormat format = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));
                    double kbPerSec = (bytes / 1024.0) / (duration / 1000.0);
                    throughput = " at " + format.format(kbPerSec) + " KB/sec";
                }

                logger.info(type + ": " + resource.getRepositoryUrl() + resource.getResourceName() + " (" + len
                        + throughput + ")");
            }
        }

        @Override
        public void transferFailed(TransferEvent event) {
            transferCompleted(event);

            if (!(event.getException() instanceof MetadataNotFoundException)) {
                logger.warn("Transfer failed", event.getException());
            }
        }

        private void transferCompleted(TransferEvent event) {
            StringBuilder buffer = new StringBuilder(64);
            pad(buffer, lastLength);
            buffer.append('\r');
            System.out.print(buffer.toString());
        }

        public void transferCorrupted(TransferEvent event) {
            logger.warn("Corrupted transfer", event.getException());
        }

        @SuppressWarnings("checkstyle:magicnumber")
        protected long toKB(long bytes) {
            return (bytes + 1023) / 1024;
        }
    }

}