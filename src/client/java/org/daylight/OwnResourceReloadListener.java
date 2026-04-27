package org.daylight;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import org.daylight.util.CatSkinManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class OwnResourceReloadListener extends SimplePreparableReloadListener<Void> {
    private static final Identifier LISTENER_ID =
            Identifier.fromNamespaceAndPath(CatifyModClient.MOD_ID, "example_cat_texture_extractor");

    @Override
    protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void ignored, ResourceManager manager, ProfilerFiller profiler) {
        try {
            extractCatTextures(manager);
            extractHandTextures(manager);
        } catch (Throwable t) {
            CatifyModClient.LOGGER.error("Failed to reload texture extractor, report this to the mod developer", t);
        }
    }

    private void extractCatTextures(ResourceManager manager) {
        Path outputDir = createOutputDir("example_cats");

        for (String name : CatSkinManager.STATIC_VARIANTS) {
            Identifier id = Identifier.withDefaultNamespace("textures/entity/cat/" + name + ".png");
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
            var resource = manager.getResource(resourceId).orElse(null);
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
        RegisterClientReloadListenersEvent.BUS.addListener(event ->
                event.registerReloadListener(new OwnResourceReloadListener())
        );
    }
}
