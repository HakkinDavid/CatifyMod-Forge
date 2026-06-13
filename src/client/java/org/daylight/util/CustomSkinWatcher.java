package org.daylight.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class CustomSkinWatcher implements Runnable {
    private final Path skinsDir;
    private final CopyOnWriteArrayList<String> skinList;

    public CustomSkinWatcher(Path skinsDir, CopyOnWriteArrayList<String> skinList) {
        this.skinsDir = skinsDir;
        this.skinList = skinList;
    }

    @Override
    public void run() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            skinsDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

            updateSkinList();

            while (true) {
                WatchKey key = watchService.take(); // ждём изменения
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                    Path changed = skinsDir.resolve((Path) event.context());
                    if (changed.toString().endsWith(".png")) {
//                        System.out.println("[SkinWatcher] Change detected: " + changed);
                        updateSkinList();
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public abstract void updateAllVariants();

    private void updateSkinList() {
        skinList.clear();
        try {
            Files.walk(skinsDir)
                    .filter(p -> p.toString().endsWith(".png"))
                    .forEach(p -> skinList.add(skinsDir.relativize(p).toString().replace("\\", "/").replace(".png", "")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateAllVariants();
//        System.out.println("[SkinWatcher] Updated skins: " + skinList);
    }
}