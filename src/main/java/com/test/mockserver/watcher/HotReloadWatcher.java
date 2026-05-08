package com.test.mockserver.watcher;

import com.test.mockserver.initializers.YamlInitializer;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.Expectation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class HotReloadWatcher {

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
                    System.out.println("[HotReload] Watching: " + path);
                } else {
                    System.err.println("[HotReload] Could not watch folder (not found on filesystem): " + folder);
                }
            }

            while (running) {
                WatchKey key = watchService.take();
                boolean changed = key.pollEvents().stream()
                        .anyMatch(e -> e.kind() != OVERFLOW);

                key.reset();

                if (changed) {
                    System.out.println("[HotReload] Change detected — reloading mock expectations...");
                    reload();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.err.println("[HotReload] Watcher error: " + e.getMessage());
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

            int total = all.length;
            System.out.println("[HotReload] Reloaded " + total + " mock expectation(s) from "
                    + folders.size() + " folder(s): " + String.join(", ", folders));
        } catch (Exception e) {
            System.err.println("[HotReload] Reload failed: " + e.getMessage());
        }
    }

    private Path resolveResourcePath(String folder) {
        try {
            URL url = getClass().getClassLoader().getResource(folder);
            if (url != null) {
                return Paths.get(url.toURI());
            }
        } catch (URISyntaxException e) {
            System.err.println("[HotReload] Could not resolve path for folder: " + folder);
        }
        return null;
    }
}
