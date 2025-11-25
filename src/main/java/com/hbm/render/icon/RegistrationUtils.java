package com.hbm.render.icon;

import com.hbm.Tags;
import com.hbm.main.MainRegistry;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegistrationUtils {


    public static void registerInFolder(TextureMap map, String path) {
        var textureList = getResourcesFromPath(Tags.MODID, path);
        for (ResourceLocation resourceLocation : textureList) {
            map.registerSprite(resourceLocation);
        }
    }

    private static List<ResourceLocation> getResourcesFromPath(String modid, String dir) {
        var result = new ArrayList<ResourceLocation>();

        URI codeSource;
        try {
            codeSource = MainRegistry.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to resolve mod JAR URI", e);
        }

        if (!"jar".equals(codeSource.getScheme()))
            return result;

        try (FileSystem fs = FileSystems.newFileSystem(codeSource, Map.of())) {
            var root = fs.getPath("assets", modid, dir);

            if (Files.exists(root)) {
                Files.walk(root).forEach(path -> {
                    if (path.toString().endsWith(".png")) {
                        String full = path.toString().replace('\\', '/');

                        int idx = full.indexOf("/textures/");
                        if (idx >= 0) {
                            String rel = full.substring(idx + "/textures/".length())
                                    .replaceFirst("\\.png$", "");

                            result.add(new ResourceLocation(modid, rel));
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
