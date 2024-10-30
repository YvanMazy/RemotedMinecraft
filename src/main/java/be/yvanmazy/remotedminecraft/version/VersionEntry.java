package be.yvanmazy.remotedminecraft.version;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record VersionEntry(@NotNull String id, @NotNull VersionType type, @NotNull String url) {

    public VersionEntry {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(url, "url must not be null");
    }

}