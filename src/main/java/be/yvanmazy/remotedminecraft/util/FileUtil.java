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

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtil {

    private FileUtil() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    public static @NotNull String toLauncherString(final @NotNull Path path) {
        return path.toAbsolutePath().toString().replace(File.separatorChar, '/');
    }

    public static @NotNull Path getSelf(final Class<?> clazz) {
        return Path.of(URI.create(clazz.getProtectionDomain().getCodeSource().getLocation().toExternalForm()));
    }

    public static @NotNull Path copyResource(final @NotNull String resource) throws IOException {
        return copyResource(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass(), resource);
    }

    public static @NotNull Path copyResource(final @NotNull Class<?> clazz, final @NotNull String resource) throws IOException {
        final Path path = getSelf(clazz).resolveSibling(resource);
        if (Files.notExists(path)) {
            copyResource(clazz, path, resource);
        }
        return path;
    }

    public static void copyResource(final Class<?> clazz, final @NotNull Path outPath, final @NotNull String resource) throws IOException {
        final InputStream in = clazz.getClassLoader().getResourceAsStream(resource);
        if (in == null) {
            throw new IOException("Resource not found: " + resource);
        }
        Files.copy(in, outPath);
    }

}