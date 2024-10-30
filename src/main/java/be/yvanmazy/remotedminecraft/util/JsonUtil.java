package be.yvanmazy.remotedminecraft.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;

public final class JsonUtil {

    private static final Gson GSON = new Gson();
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    private JsonUtil() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate utility class.");
    }

    public static String toJson(final Object object) {
        return GSON.toJson(object);
    }

    public static String toPrettyJson(final Object object) {
        return PRETTY_GSON.toJson(object);
    }

    public static JsonObject fromJson(final InputStream jsonContent) throws IOException {
        return fromJson(jsonContent.readAllBytes());
    }

    public static JsonObject fromJson(final byte[] jsonContent) {
        return fromJson(new String(jsonContent));
    }

    public static JsonObject fromJson(final String jsonContent) {
        return GSON.fromJson(jsonContent, JsonObject.class);
    }

    public static <T> T fromJson(final JsonElement json, final Class<T> typeClass) {
        return GSON.fromJson(json, typeClass);
    }

    public static <T> T fromJson(final String json, final Class<T> typeClass) {
        return GSON.fromJson(json, typeClass);
    }

}