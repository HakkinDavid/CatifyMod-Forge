package org.daylight;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.Identifier;
import org.daylight.util.CatSkinManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class OwnResourceReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath(CatifyModClient.MOD_ID, "example_cat_texture_extractor");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        try {
            extractCatTextures(manager);
            extractHandTextures(manager);
        } catch (Throwable t) {
            CatifyModClient.LOGGER.error("Failed to reload texture extractor, report this to the mod developer", t);
        }
    }

    private void extractCatTextures(ResourceManager manager) {
        Path outputDir = createOutputDir("example_cats");

        for (String name : org.daylight.util.CatSkinManager.STATIC_VARIANTS) {
            Identifier id = Identifier.fromNamespaceAndPath("minecraft", "textures/entity/cat/" + name + ".png");
            copyResource(manager, id, outputDir, name);
        }
    }

    private void extractHandTextures(ResourceManager manager) {
        Path outputDir = createOutputDir("example_hands");

        for (Map.Entry<?, Identifier> entry : ModResources.CAT_HAND_BY_VARIANT.entrySet()) {
            Identifier textureId = entry.getValue();
            String name = extractBaseName(textureId);
            copyResource(manager, textureId, outputDir, name);
        }
    }

    private Path createOutputDir(String folderName) {
        Path outputDir = Minecraft.getInstance().gameDirectory.toPath()
                .resolve("data/catify/example_skins/" + folderName);
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create output folder: " + outputDir, e);
        }
        return outputDir;
    }

    private void copyResource(ResourceManager manager, Identifier resourceId, Path outputDir, String fileName) {
        try {
            Resource resource = manager.getResource(resourceId).orElse(null);
            if (resource == null) {
                CatifyModClient.LOGGER.error("Not found: {}", resourceId);
                return;
            }

            Path outFile = outputDir.resolve(fileName + ".png");
            try (InputStream input = resource.open()) {
                Files.copy(input, outFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            CatifyModClient.LOGGER.error("Error extracting {}: {} ", resourceId, e);
        }
    }

    private String extractBaseName(Identifier id) {
        String path = id.getPath(); // textures/entity/tabby_hand.png
        int lastSlash = path.lastIndexOf('/');
        String file = (lastSlash >= 0 ? path.substring(lastSlash + 1) : path);
        return file.replace("_hand.png", "");
    }

    public static void register() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new OwnResourceReloadListener());
    }
}