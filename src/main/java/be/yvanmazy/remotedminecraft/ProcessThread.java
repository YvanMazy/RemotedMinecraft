package be.yvanmazy.remotedminecraft;

import be.yvanmazy.remotedminecraft.auth.Auth;
import be.yvanmazy.remotedminecraft.config.ProcessConfiguration;
import be.yvanmazy.remotedminecraft.platform.OsType;
import be.yvanmazy.remotedminecraft.util.FileUtil;
import be.yvanmazy.remotedminecraft.util.HashUtil;
import be.yvanmazy.remotedminecraft.util.JsonUtil;
import be.yvanmazy.remotedminecraft.version.Assets;
import be.yvanmazy.remotedminecraft.version.VersionEntry;
import be.yvanmazy.remotedminecraft.version.VersionManifest;
import be.yvanmazy.remotedminecraft.version.VersionType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ProcessThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessThread.class);

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(\\w+)}");
    private static final URL VERSIONS_URL;
    private static final String ASSETS_URL = "https://resources.download.minecraft.net/";

    static {
        try {
            VERSIONS_URL = URI.create("https://launchermeta.mojang.com/mc/game/version_manifest.json").toURL();
        } catch (final MalformedURLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final MinecraftHolderImpl holder;
    private final ProcessConfiguration configuration;
    private final Path directory;

    private VersionManifest versionManifest;
    private Path jarPath;
    private List<Path> libraries;
    private Map<String, String> placeholderMap;

    ProcessThread(final @NotNull MinecraftHolderImpl holder) {
        this.holder = Objects.requireNonNull(holder, "holder must not be null");
        this.configuration = holder.getConfiguration();
        this.directory = this.configuration.processDirectory();
    }

    @Override
    public void run() {
        try {
            if (!Files.isDirectory(this.directory)) {
                Files.createDirectories(this.directory);
            }
            this.createLauncherProfiles();
            this.prepareVersionManifest();
            this.prepareVersionJar();
            this.prepareLibraries();
            this.prepareAssets();
            this.preparePlaceholders();

            this.holder.complete(this.launchGame());
        } catch (final Throwable throwable) {
            this.holder.completeExceptionally(throwable);
        }
    }

    private @NotNull Process launchGame() throws IOException {
        LOGGER.debug("Start the game...");

        final List<String> commands = new ArrayList<>();
        commands.add(this.configuration.processJavaPath().toString());
        this.fillLines(commands, this.versionManifest.arguments().getJvmLines());
        commands.add("-XX:+EnableDynamicAgentLoading"); // TODO: Make an option for add this
        commands.addAll(this.configuration.jvmArguments());
        String mainClass = this.configuration.processMainClass();
        if (mainClass.isBlank()) {
            mainClass = this.versionManifest.mainClass();
        }
        commands.add(mainClass);
        this.fillLines(commands, this.versionManifest.arguments().getGameLines());
        commands.addAll(this.configuration.gameArguments());

        return new ProcessBuilder(commands).directory(this.directory.toFile()).inheritIO().start();
    }

    private void fillLines(final List<String> commands, final List<String> lines) {
        for (String line : lines) {
            line = this.replacePlaceholder(line);
            if (line.isBlank() && commands.size() > 1) {
                commands.removeLast();
                continue;
            }
            commands.add(line);
        }
    }

    private String replacePlaceholder(final String line) {
        return VARIABLE_PATTERN.matcher(line).replaceAll(result -> {
            final String group = result.group(1);
            final String s = this.placeholderMap.get(group);
            if (s != null) {
                return s;
            }
            return "";
        });
    }

    private void createLauncherProfiles() {
        LOGGER.debug("Prepare launcher_profiles...");
        final Path path = this.directory.resolve("launcher_profiles.json");

        if (Files.notExists(path)) {
            final JsonObject object = new JsonObject();
            object.add("profiles", new JsonArray());
            object.addProperty("version", 3);
            try {
                Files.writeString(path, JsonUtil.toJson(object));
            } catch (final IOException e) {
                LOGGER.error("Failed to write profiles", e);
            }
        }
        LOGGER.debug("launcher_profiles is ready!");
    }

    private void prepareVersionManifest() {
        LOGGER.debug("Prepare version manifest...");

        final Path path = this.directory.resolve("versions/version_manifest.json");
        if (Files.notExists(path)) {
            try {
                downloadVersionManifest(path);
            } catch (final IOException e) {
                throw new IllegalStateException("Failed to download versions", e);
            }
        }
        final String expectedVersion = this.configuration.version();
        boolean retry = false;
        VersionEntry version;
        try {
            do {
                try (final InputStream in = Files.newInputStream(path)) {
                    version = JsonUtil.fromJson(in)
                            .getAsJsonArray("versions")
                            .asList()
                            .stream()
                            .map(JsonElement::getAsJsonObject)
                            .filter(j -> j.get("id").getAsString().equals(expectedVersion))
                            .findFirst()
                            .map(ProcessThread::parseVersion)
                            .orElse(null);
                }
            } while (version == null && (retry = !retry) && downloadVersionManifest(path));
        } catch (final IOException exception) {
            throw new IllegalStateException("Failed to read versions", exception);
        }
        if (version == null) {
            throw new IllegalStateException("Version not found: " + expectedVersion);
        }

        final Path versionPath = this.directory.resolve("versions/" + expectedVersion + '/' + expectedVersion + ".json");
        if (Files.notExists(versionPath.getParent())) {
            try {
                Files.createDirectories(versionPath.getParent());
            } catch (final IOException e) {
                throw new IllegalStateException("Failed to create version directory", e);
            }
        }
        downloadJarFile(version.url(), versionPath, "Failed to download version");
    }

    private void prepareVersionJar() {
        LOGGER.debug("Reading version file...");

        final String version = this.configuration.version();
        final Path versionDirectory = this.directory.resolve("versions/" + version);
        final VersionManifest.Downloads.Client client;
        try {
            this.versionManifest = JsonUtil.fromJson(Files.readString(versionDirectory.resolve(version + ".json")), VersionManifest.class);
            client = this.versionManifest.downloads().client();
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read download url", e);
        }

        LOGGER.debug("Prepare version jar...");

        this.jarPath = this.directory.resolve("versions/" + version + '/' + version + ".jar");
        if (!prepareFile(this.jarPath, client.url(), client.sha1())) {
            throw new IllegalStateException("Version jar is not valid");
        }

        LOGGER.debug("Version jar is ready!");
    }

    private void prepareLibraries() throws IOException {
        LOGGER.debug("Prepare libraries...");
        final Path libs = this.directory.resolve("libraries");
        this.libraries = new ArrayList<>(this.versionManifest.libraries().size());
        for (final VersionManifest.Library library : this.versionManifest.libraries()) {
            if (library.isAllowed()) {
                final var artifact = library.downloads().artifact();
                final Path path = libs.resolve(artifact.path());
                if (!prepareFile(path, artifact.url(), artifact.sha1())) {
                    throw new IllegalStateException("Invalid library");
                }
                this.libraries.add(path);
            }
        }
        final Path natives = this.directory.resolve("natives");
        if (Files.notExists(natives)) {
            Files.createDirectories(natives);
        }
        // TODO: Download natives
    }

    private void prepareAssets() throws IOException {
        LOGGER.debug("Prepare Assets...");
        final Path directory = this.directory.resolve("assets");
        final VersionManifest.AssetIndex index = this.versionManifest.assetIndex();
        final Path indexPath = directory.resolve("indexes/" + index.id() + ".json");
        if (!prepareFile(indexPath, index.url(), index.sha1())) {
            throw new IllegalStateException("Invalid assets file");
        }
        final Assets assets = JsonUtil.fromJson(Files.readString(indexPath), Assets.class);
        for (final Assets.Data data : assets.objects().values()) {
            final String hash = data.hash();
            final String id = hash.substring(0, 2) + "/" + hash;
            final Path downloadPath = directory.resolve("objects/" + id);
            if (Files.isRegularFile(downloadPath)) {
                continue;
            }
            downloadJarFile(ASSETS_URL + id, downloadPath, "Failed to download asset");
        }
    }

    private void preparePlaceholders() {
        this.placeholderMap = new HashMap<>();
        final Auth auth = this.configuration.authentication();
        this.placeholderMap.put("auth_access_token", Objects.requireNonNullElse(auth.accessToken(), "0"));
        if (auth.username() != null) {
            this.placeholderMap.put("auth_player_name", auth.username());
        }
        this.placeholderMap.put("version_name", this.versionManifest.id());
        this.placeholderMap.put("version_type", this.versionManifest.type());
        this.placeholderMap.put("game_directory", FileUtil.toLauncherString(this.directory));
        this.placeholderMap.put("natives_directory", FileUtil.toLauncherString(this.directory.resolve("natives")));
        this.placeholderMap.put("assets_index_name", this.versionManifest.assets());
        this.placeholderMap.put("classpath",
                Stream.concat(this.libraries.stream(), Stream.of(this.jarPath))
                        .map(FileUtil::toLauncherString)
                        .collect(Collectors.joining(OsType.getCurrentType() == OsType.LINUX ? ":" : ";")));
    }

    private static boolean prepareFile(final @NotNull Path path, final @NotNull String url, final @Nullable String sha1) {
        if (Files.notExists(path)) {
            downloadJarFile(url, path, "Failed to download jar");
        }
        boolean retry = false;
        do {
            try (final InputStream in = Files.newInputStream(path)) {
                if (sha1 == null || HashUtil.hash(in).equals(sha1)) {
                    return true;
                }
            } catch (final IOException e) {
                LOGGER.error("Failed to checksum", e);
            }
        } while ((retry = !retry) && downloadJarFile(url, path, "Failed to download jar"));
        return false;
    }

    private static boolean downloadJarFile(final String jarUrl, final Path path, final String errorMessage) {
        try {
            final Path parent = path.getParent();
            if (!Files.isDirectory(parent)) {
                Files.createDirectories(parent);
            }
            try (final InputStream in = URI.create(jarUrl).toURL().openStream()) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (final IOException e) {
            throw new IllegalStateException(errorMessage, e);
        }
        return true;
    }

    private static boolean downloadVersionManifest(final Path path) throws IOException {
        final Path parent = path.getParent();
        if (!Files.isDirectory(parent)) {
            Files.createDirectories(parent);
        }
        try (final InputStream in = VERSIONS_URL.openStream()) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        }
        return true;
    }

    private static VersionEntry parseVersion(final JsonObject object) {
        final String id = object.get("id").getAsString();
        final String rawType = object.get("type").getAsString();
        final VersionType type = VersionType.fromString(rawType);
        if (type == null) {
            LOGGER.warn("Invalid version type on '{}': {}", id, rawType);
            return null;
        }
        final String url = object.get("url").getAsString();

        return new VersionEntry(id, type, url);
    }

}