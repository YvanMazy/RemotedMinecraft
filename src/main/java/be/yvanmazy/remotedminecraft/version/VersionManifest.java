package be.yvanmazy.remotedminecraft.version;

import be.yvanmazy.remotedminecraft.platform.OsType;
import be.yvanmazy.remotedminecraft.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record VersionManifest(String id, JavaVersion javaVersion, Arguments arguments, AssetIndex assetIndex, String assets,
                              int complianceLevel, Downloads downloads, List<Library> libraries, Logging logging, String mainClass,
                              int minimumLauncherVersion, String releaseTime, String time, String type) {

    public record JavaVersion(String component, int majorVersion) {

    }

    public record Arguments(List<JsonElement> game, List<JsonElement> jvm) {

        public record Rule(String action, Map<String, Object> features, OS os) {

            public boolean isAllowed() {
                if (this.features != null) {
                    return false;
                }
                return (this.os == null || this.os.isValid()) == this.action.equals("allow");
            }

            public record OS(String name, String version, String arch) {

                public boolean isValid() {
                    final OsType type;
                    if (this.name != null && (type = OsType.fromString(this.name)) != OsType.getCurrentType() && type != OsType.UNKNOWN) {
                        return false;
                    }
                    if (this.version != null && !this.version.equals(System.getProperty("os.version"))) {
                        return false;
                    }
                    return this.arch == null || this.arch.equals(System.getProperty("os.arch"));
                }

            }

        }

        public List<String> getGameLines() {
            return getLines(this.game);
        }

        public List<String> getJvmLines() {
            return getLines(this.jvm);
        }

        private static List<String> getLines(final List<JsonElement> elements) {
            final List<String> list = new ArrayList<>(elements.size());
            for (final JsonElement element : elements) {
                if (element.isJsonPrimitive()) {
                    list.add(element.getAsString());
                } else if (element.isJsonObject()) {
                    final JsonObject block = element.getAsJsonObject();
                    final JsonArray rules = block.getAsJsonArray("rules");
                    if (rules == null) {
                        continue;
                    }
                    final Rule[] array = JsonUtil.fromJson(rules, Rule[].class);
                    if (array == null) {
                        continue;
                    }
                    boolean allowed = true;
                    for (final Arguments.Rule rule : array) {
                        if (!rule.isAllowed()) {
                            allowed = false;
                            break;
                        }
                    }
                    if (allowed) {
                        final String value = block.get("value").getAsString();
                        if (value == null) {
                            continue;
                        }
                        list.add(value);
                    }
                }
            }
            return list;
        }

    }

    public record AssetIndex(String id, String sha1, int size, int totalSize, String url) {

    }

    public record Downloads(Client client, Client client_mappings, Server server, Server server_mappings) {

        public record Client(String sha1, int size, String url) {

        }

        public record Server(String sha1, int size, String url) {

        }

    }

    public record Library(Downloads downloads, String name, List<Arguments.Rule> rules) {

        public boolean isAllowed() {
            if (this.rules != null) {
                for (final Arguments.Rule rule : this.rules) {
                    if (!rule.isAllowed()) {
                        return false;
                    }
                }
            }
            return true;
        }

        public record Downloads(Artifact artifact) {

            public record Artifact(String path, String sha1, int size, String url) {

            }

        }

    }

    public record Logging(Client client) {

        public record Client(String argument, File file, String type) {

            public record File(String id, String sha1, int size, String url) {

            }

        }

    }

}
