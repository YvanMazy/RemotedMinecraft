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

package be.yvanmazy.remotedminecraft.controller.agent;

import be.yvanmazy.remotedminecraft.util.ClassUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public final class AgentFileBuilder {

    private final Class<?> mainClass;
    private final Set<Class<?>> classes = new HashSet<>();

    public AgentFileBuilder(final @NotNull Class<?> mainClass) {
        this.mainClass = Objects.requireNonNull(mainClass, "mainClass must not be null");
        this.classes.add(mainClass);
    }

    @Contract("_ -> this")
    public @NotNull AgentFileBuilder addClass(final @NotNull Class<?> clazz) {
        this.classes.add(Objects.requireNonNull(clazz, "clazz must not be null"));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull AgentFileBuilder addClasses(final @NotNull Class<?> @NotNull ... classes) {
        return this.addClasses(List.of(classes));
    }

    @Contract("_ -> this")
    public @NotNull AgentFileBuilder addClasses(final @NotNull Collection<Class<?>> classes) {
        this.classes.addAll(classes);
        return this;
    }

    @Contract("_ -> this")
    public @NotNull AgentFileBuilder addPackage(final @NotNull String packageName) {
        return this.addPackage(packageName, -1);
    }

    @Contract("_, _ -> this")
    public @NotNull AgentFileBuilder addPackage(final @NotNull String packageName, final int maxDepth) {
        try {
            return this.addClasses(ClassUtil.getClasses(packageName, maxDepth));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Contract("_, _ -> this")
    public @NotNull AgentFileBuilder addPackage(final @NotNull ClassLoader classLoader, final @NotNull String packageName) {
        return this.addPackage(classLoader, packageName, -1);
    }

    @Contract("_, _, _ -> this")
    public @NotNull AgentFileBuilder addPackage(final @NotNull ClassLoader classLoader,
                                                final @NotNull String packageName,
                                                final int maxDepth) {
        try {
            return this.addClasses(ClassUtil.getClasses(classLoader, packageName, maxDepth));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Contract("-> this")
    public @NotNull AgentFileBuilder addRemotedAgentClasses() {
        return this.addClasses(RemotedAgent.class, RemotedAgents.class, RemotedAgents.RemoteSupplier.class);
    }

    @Contract("-> new")
    public @NotNull Path buildUnchecked() {
        try {
            return this.build();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Contract("-> new")
    public @NotNull Path build() throws IOException {
        return this.build(true);
    }

    @Contract("_ -> new")
    public @NotNull Path build(final boolean deleteOnShutdown) throws IOException {
        final Path path = Files.createTempFile("rmc_agent", ".jar");
        if (deleteOnShutdown) {
            Runtime.getRuntime().addShutdownHook(new AgentFileCleanerThread(path));
        }
        return this.build(path);
    }

    @Contract("_ -> new")
    public @NotNull Path buildUnchecked(final @NotNull Path path) {
        try {
            return this.build(path);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Contract("_ -> new")
    public @NotNull Path build(final @NotNull Path path) throws IOException {
        try (final JarOutputStream out = new JarOutputStream(Files.newOutputStream(path))) {
            for (final Class<?> clazz : this.classes) {
                addClassToJar(out, clazz);
            }
            final Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(new Attributes.Name("Agent-Class"), this.mainClass.getName());
            manifest.getMainAttributes().put(new Attributes.Name("Premain-Class"), this.mainClass.getName());
            out.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
            manifest.write(out);
        }
        return path;
    }

    private static void addClassToJar(final JarOutputStream out, final Class<?> clazz) throws IOException {
        final String formatted = toFormatted(clazz);
        out.putNextEntry(new JarEntry(formatted));
        try (final InputStream is = clazz.getResourceAsStream('/' + formatted)) {
            if (is != null) {
                is.transferTo(out);
            }
        }
    }

    private static String toFormatted(final Class<?> clazz) {
        return clazz.getName().replace('.', '/') + ".class";
    }

}