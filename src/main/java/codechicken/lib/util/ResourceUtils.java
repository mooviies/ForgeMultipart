package codechicken.lib.util;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by covers1624 on 11/07/2017.
 */
public class ResourceUtils {

    /**
     * Retrieves an InputStream from the standard Vanilla IReloadableResourceManager.
     *
     * @param resource The resource.
     * @return The InputStream.
     * @throws IOException If the file is not found or some other IO error occurred.
     */
    public static InputStream getResourceAsStream(ResourceLocation resource) throws IOException {
        return getResource(resource).getInputStream();
    }

    /**
     * Grabs the resource manager.
     *
     * @return The resource manager.
     */
    public static IReloadableResourceManager getResourceManager() {
        return (IReloadableResourceManager) Minecraft.getInstance().getResourceManager();
    }

    /**
     * Get's an IResource from the resource manager.
     *
     * @param location The resource to get.
     * @return The gotten resource.
     * @throws IOException If the resource doesn't exist, or some other IO error occurred.
     */
    public static IResource getResource(String location) throws IOException {
        return getResource(new ResourceLocation(location));
    }

    /**
     * Get's an IResource from the resource manager.
     *
     * @param location The resource to get.
     * @return The gotten resource.
     * @throws IOException If the resource doesn't exist, or some other IO error occurred.
     */
    public static IResource getResource(ResourceLocation location) throws IOException {
        return getResourceManager().getResource(location);
    }

    /**
     * Registers a IResourceManagerReloadListener to MC's resource manager.
     *
     * @param reloadListener The listener.
     */
    public static void registerReloadListener(ISelectiveResourceReloadListener reloadListener) {
        getResourceManager().addReloadListener(reloadListener);
    }

    /**
     * Attempts to create a file.
     *
     * @param file The file to create.
     */
    @Deprecated//ensureExists.
    public static void tryCreateFile(File file) {
        if (!file.getParentFile().exists()) {
            tryCreateDirectory(file.getParentFile());
        }

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new Exception("createNewFile returned false.");
                }
            } catch (Exception e) {
                throw new RuntimeException("Unable to create file: " + file.getAbsolutePath(), e);
            }
        }
    }

    /**
     * Attempts to create a file.
     *
     * @param file The file to create.
     * @return The same file you passed in.
     */
    public static File ensureExists(File file) {
        if (!file.exists()) {
            try {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Error whilst creating file!", e);
            }
        }

        return file;
    }

    /**
     * Attempts to create a directory.
     *
     * @param dir The dir to create.
     */
    @Deprecated//ensureExists.
    public static void tryCreateDirectory(File dir) {
        if (!dir.exists()) {
            try {
                if (!dir.mkdirs()) {
                    throw new Exception("mkdirs returned false.");
                }
            } catch (Exception e) {
                throw new RuntimeException("Unable to create directory: " + dir.getAbsolutePath(), e);
            }
        }
    }
}
