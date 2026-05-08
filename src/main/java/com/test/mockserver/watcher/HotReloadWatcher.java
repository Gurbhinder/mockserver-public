package com.test.mockserver.watcher;

import com.test.mockserver.initializers.YamlInitializer;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.Expectation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class HotReloadWatcher {

    private static final Logger log = LoggerFactory.getLogger(HotReloadWatcher.class);

    private final ClientAndServer mockServer;
    private final List<String> folders;
    private volatile boolean running = false;
    private Thread watchThread;

    public HotReloadWatcher(ClientAndServer mockServer, String foldersCsv) {
        this.mockServer = mockServer;
        this.folders = Arrays.stream(foldersCsv.split(","))
                .map(String::trim)
                .filter(f -> !f.isEmpty())
                .toList();
    }

    public void start() {
        running = true;
        watchThread = Thread.ofVirtual().name("hot-reload-watcher").start(this::watch);
    }

    public void stop() {
        running = false;
        if (watchThread != null) {
            watchThread.interrupt();
        }
    }

    private void watch() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            for (String folder : folders) {
                Path path = resolveResourcePath(folder);
                if (path != null && Files.isDirectory(path)) {
                    path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                    log.info("Watching: {}", path);
                } else {
                    log.warn("Could not watch folder (not found on filesystem): {}", folder);
                }
            }

            while (running) {
                WatchKey key = watchService.take();
                boolean changed = key.pollEvents().stream()
                        .anyMatch(e -> e.kind() != OVERFLOW);
                key.reset();

                if (changed) {
                    log.info("Change detected — reloading mock expectations...");
                    reload();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error("Watcher error", e);
        }
    }

    private void reload() {
        try {
            Expectation[] all = folders.stream()
                    .map(YamlInitializer::new)
                    .flatMap(i -> Arrays.stream(i.initializeExpectations()))
                    .toArray(Expectation[]::new);

            mockServer.reset();
            mockServer.upsert(all);

            log.info("Reloaded {} mock expectation(s) from {} folder(s): {}",
                    all.length, folders.size(), String.join(", ", folders));
        } catch (Exception e) {
            log.error("Reload failed", e);
        }
    }

    private Path resolveResourcePath(String folder) {
        try {
            URL url = getClass().getClassLoader().getResource(folder);
            if (url != null) {
                return Paths.get(url.toURI());
            }
        } catch (URISyntaxException e) {
            log.error("Could not resolve path for folder: {}", folder, e);
        }
        return null;
    }
}
