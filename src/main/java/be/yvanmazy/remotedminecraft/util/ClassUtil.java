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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public final class ClassUtil {

    private ClassUtil() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    public static @NotNull Set<Class<?>> getClasses(final @NotNull String packageName, final int maxDepth) throws IOException {
        return getClasses(ClassLoader.getSystemClassLoader(), packageName, maxDepth);
    }

    public static @NotNull Set<Class<?>> getClasses(final @NotNull ClassLoader classLoader,
                                                    final @NotNull String packageName,
                                                    final int maxDepth) throws IOException {
        final InputStream stream = classLoader.getResourceAsStream(packageName.replace('.', '/'));
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            final Set<Class<?>> classes = new HashSet<>();
            for (final String line : reader.lines().toList()) {
                if (line.endsWith(".class")) {
                    try {
                        classes.add(Class.forName(packageName + "." + line.substring(0, line.lastIndexOf('.'))));
                    } catch (final ClassNotFoundException ignored) {
                    }
                } else if (maxDepth >= 1) {
                    classes.addAll(getClasses(classLoader, packageName + "." + line, maxDepth - 1));
                }
            }
            return classes;
        }
    }

}