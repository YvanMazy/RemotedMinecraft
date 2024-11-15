/*
 * MIT License
 *
 * Copyright (c) 2024 Darkkraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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