package net.minestom.server.extensions;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public abstract class Extension {
    public enum LoadStatus {
        SUCCESS,
        FAILED
    }

    ExtensionDescriptor descriptor;
    Logger logger;
    EventNode<Event> eventNode;

    protected Extension() { }

    public abstract LoadStatus initialize();

    public abstract void terminate();

    public @NotNull ExtensionDescriptor descriptor() {
        return descriptor;
    }

    public @NotNull Logger logger() {
        return logger;
    }

    public @NotNull EventNode<Event> eventNode() {
        return eventNode;
    }

    public @NotNull Path dataDirectory() {
        return descriptor().dataDirectory();
    }

    /**
     * Gets a resource from the extension directory, or from inside the jar if it does not
     * exist in the extension directory.
     * <p>
     * If it does not exist in the extension directory, it will be copied from inside the jar.
     * <p>
     * The caller is responsible for closing the returned {@link InputStream}.
     *
     * @param fileName The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    public @Nullable InputStream getResource(@NotNull String fileName) {
        return getResource(Paths.get(fileName));
    }

    /**
     * Gets a resource from the extension directory, or from inside the jar if it does not
     * exist in the extension directory.
     * <p>
     * If it does not exist in the extension directory, it will be copied from inside the jar.
     * <p>
     * The caller is responsible for closing the returned {@link InputStream}.
     *
     * @param target The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    public @Nullable InputStream getResource(@NotNull Path target) {
        final Path targetFile = dataDirectory().resolve(target);
        try {
            // Copy from jar if the file does not exist in the extension data directory.
            if (!Files.exists(targetFile)) {
                savePackagedResource(target);
            }

            return Files.newInputStream(targetFile);
        } catch (IOException ex) {
            getLogger().info("Failed to read resource {}.", target, ex);
            return null;
        }
    }

    /**
     * Gets a resource from inside the extension jar.
     * <p>
     * The caller is responsible for closing the returned {@link InputStream}.
     *
     * @param fileName The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    public @Nullable InputStream getPackagedResource(@NotNull String fileName) {
        try {
            final URL url = descriptor().classLoader().getResource(fileName);
            if (url == null) {
                getLogger().debug("Resource not found: {}", fileName);
                return null;
            }

            return url.openConnection().getInputStream();
        } catch (IOException ex) {
            getLogger().debug("Failed to load resource {}.", fileName, ex);
            return null;
        }
    }

    /**
     * Gets a resource from inside the extension jar.
     * <p>
     * The caller is responsible for closing the returned {@link InputStream}.
     *
     * @param target The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    public @Nullable InputStream getPackagedResource(@NotNull Path target) {
        return getPackagedResource(target.toString().replace('\\', '/'));
    }

    /**
     * Copies a resource file to the extension directory, replacing any existing copy.
     *
     * @param fileName The resource to save
     * @return True if the resource was saved successfully, null otherwise
     */
    public boolean savePackagedResource(@NotNull String fileName) {
        return savePackagedResource(Paths.get(fileName));
    }

    /**
     * Copies a resource file to the extension directory, replacing any existing copy.
     *
     * @param target The resource to save
     * @return True if the resource was saved successfully, null otherwise
     */
    public boolean savePackagedResource(@NotNull Path target) {
        final Path targetFile = dataDirectory().resolve(target);
        try (InputStream is = getPackagedResource(target)) {
            if (is == null) {
                return false;
            }

            Files.createDirectories(targetFile.getParent());
            Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException ex) {
            getLogger().debug("Failed to save resource {}.", target, ex);
            return false;
        }
    }

    /**
     * @see #descriptor()
     */
    @Deprecated
    public final @NotNull ExtensionDescriptor getOrigin() {
        return descriptor();
    }

    /**
     * @see #logger()
     */
    @Deprecated
    public final @NotNull Logger getLogger() {
        return logger();
    }

    /**
     * @see #eventNode()
     */
    @Deprecated
    public final @NotNull EventNode<Event> getEventNode() {
        return eventNode();
    }

    /**
     * @see #dataDirectory()
     */
    @Deprecated
    public final @NotNull Path getDataDirectory() {
        return dataDirectory();
    }
}
