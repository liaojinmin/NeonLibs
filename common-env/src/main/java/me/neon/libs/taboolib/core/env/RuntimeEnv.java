package me.neon.libs.taboolib.core.env;

import me.neon.libs.taboolib.core.ClassAppender;
import me.neon.libs.taboolib.core.PrimitiveIO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipFile;

import static me.neon.libs.taboolib.core.PrimitiveIO.getAssetsFile;
import static me.neon.libs.taboolib.core.PrimitiveIO.getLibraryFile;


/**
 * TabooLib
 * me.neon.libs.taboolib.core.env.RuntimeEnv
 *
 * @author sky
 * @since 2021/6/15 6:23 下午
 */
public class RuntimeEnv {

    public static final RuntimeEnv ENV = new RuntimeEnv();

    static void init() throws Throwable {
        // 检查 Kotlin 环境
        if (PrimitiveIO.isKotlinEnvironment()) {
            PrimitiveIO.println("已存在 Kotlin 环境集成...");
            return;
        }
        // 如果包内的 Kotlin 未被加入类加载器，则下载。
        PrimitiveIO.println("正在尝试下载 Kotlin 环境集成...");
        List<JarRelocation> rel = new ArrayList<>();
        // 启用 Kotlin 重定向
        rel.add(new JarRelocation("!kotlin.","kotlin."));

        // 加载 Kotlin 环境
        ENV.loadDependency("!org.jetbrains.kotlin:kotlin-stdlib:1.7.20", rel);
    }

    public void inject(@NotNull Class<?> clazz) throws Throwable {
        loadAssets(clazz);
        loadDependency(clazz);
    }

    private void loadAssets(@NotNull Class<?> clazz) throws IOException {
        RuntimeResource[] resources = null;
        if (clazz.isAnnotationPresent(RuntimeResource.class)) {
            resources = clazz.getAnnotationsByType(RuntimeResource.class);
        } else {
            RuntimeResources annotation = clazz.getAnnotation(RuntimeResources.class);
            if (annotation != null) {
                resources = annotation.value();
            }
        }
        if (resources == null) {
            return;
        }
        for (RuntimeResource resource : resources) {
            loadAssets(resource.name(), resource.hash(), resource.value(), resource.zip());
        }
    }

    private void loadAssets(String name, String hash, String url, boolean zip) throws IOException {
        File file;
        if (name.isEmpty()) {
            file = new File(getAssetsFile(), hash.substring(0, 2) + "/" + hash);
        } else {
            file = new File(getAssetsFile(), name);
        }
        if (file.exists() && PrimitiveIO.getHash(file).equals(hash)) {
            return;
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        PrimitiveIO.println("正在下载 assets " + url.substring(url.lastIndexOf('/') + 1));
        if (zip) {
            File cacheFile = new File(file.getParentFile(), file.getName() + ".zip");
            PrimitiveIO.downloadFile(new URL(url + ".zip"), cacheFile);
            try (ZipFile zipFile = new ZipFile(cacheFile)) {
                InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(url.substring(url.lastIndexOf('/') + 1)));
                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    fileOutputStream.write(PrimitiveIO.readFully(inputStream));
                }
            } finally {
                cacheFile.delete();
            }
        } else {
            PrimitiveIO.downloadFile(new URL(url), file);
        }
    }

    private boolean test(String path) {
        String test = path.startsWith("!") ? path.substring(1) : path;
        return !test.isEmpty() && ClassAppender.isExists(test);
    }

    private void loadDependency(@NotNull Class<?> clazz) throws Throwable {
        RuntimeDependency[] dependencies = null;
        if (clazz.isAnnotationPresent(RuntimeDependency.class)) {
            dependencies = clazz.getAnnotationsByType(RuntimeDependency.class);
        } else {
            RuntimeDependencies annotation = clazz.getAnnotation(RuntimeDependencies.class);
            if (annotation != null) {
                dependencies = annotation.value();
            }
        }
        if (dependencies != null) {
            for (RuntimeDependency dep : dependencies) {
                String allTest = dep.test();
                List<String> tests = new ArrayList<>();
                if (allTest.contains(",")) {
                    tests.addAll(Arrays.asList(allTest.split(",")));
                } else {
                    tests.add(allTest);
                }
                if (tests.stream().allMatch(this::test)) {
                    PrimitiveIO.println("运行依赖库已存在 %s", dep.value().replace("!", ""));
                    continue;
                }
                List<JarRelocation> relocation = new ArrayList<>();
                String[] relocate = dep.relocate();
                if (relocate.length % 2 != 0) {
                    throw new IllegalStateException("invalid relocate format");
                }
                for (int i = 0; i + 1 < relocate.length; i += 2) {
                    String pattern = relocate[i].startsWith("!") ? relocate[i].substring(1) : relocate[i];
                    String relocatePattern = relocate[i + 1].startsWith("!") ? relocate[i + 1].substring(1) : relocate[i + 1];
                    relocation.add(new JarRelocation(pattern, relocatePattern));
                }
                String url = dep.value().startsWith("!") ? dep.value().substring(1) : dep.value();
                loadDependency(url, getLibraryFile(), relocation, dep.repository(), dep.ignoreOptional(), dep.ignoreException(), dep.transitive(), dep.scopes());
            }
        }
    }

    private void loadDependency(@NotNull String url, @NotNull List<JarRelocation> relocation) throws Throwable {
        loadDependency(url, getLibraryFile(), relocation, PrimitiveIO.REPO_CENTRAL, true, false, true, new DependencyScope[]{DependencyScope.RUNTIME, DependencyScope.COMPILE});
    }

    private void loadDependency(
            @NotNull String url,
            @NotNull File baseDir,
            @NotNull List<JarRelocation> relocation,
            @NotNull String repository,
            boolean ignoreOptional,
            boolean ignoreException,
            boolean transitive,
            @NotNull DependencyScope[] scope
    ) throws Throwable {
        String[] args = url.split(":");
        DependencyDownloader downloader = new DependencyDownloader(baseDir, relocation);
        // 支持用户对源进行替换
        if (repository.isEmpty()) {
            repository = PrimitiveIO.REPO_CENTRAL;
        }
        downloader.addRepository(new Repository(repository));
        downloader.setIgnoreOptional(ignoreOptional);
        downloader.setIgnoreException(ignoreException);
        downloader.setDependencyScopes(scope);
        downloader.setTransitive(transitive);
        // 解析依赖
        File pomFile = new File(baseDir, String.format("%s/%s/%s/%s-%s.pom", args[0].replace('.', '/'), args[1], args[2], args[1], args[2]));
        File pomFile1 = new File(pomFile.getPath() + ".sha1");
        // 验证文件完整性
        if (PrimitiveIO.validation(pomFile, pomFile1)) {
            downloader.loadDependencyFromInputStream(pomFile.toPath().toUri().toURL().openStream());
        } else {
            String pom = String.format("%s/%s/%s/%s/%s-%s.pom", repository, args[0].replace('.', '/'), args[1], args[2], args[1], args[2]);
            PrimitiveIO.println("正在下载 library %s:%s:%s %s", args[0], args[1], args[2], transitive ? "(transitive)" : "");
            downloader.loadDependencyFromInputStream(new URL(pom).openStream());
        }
        // 加载自身
        Dependency dep = new Dependency(args[0], args[1], args[2], DependencyScope.RUNTIME);
        if (transitive) {
            downloader.injectClasspath(downloader.loadDependency(downloader.getRepositories(), dep));
        } else {
            downloader.injectClasspath(Collections.singleton(dep));
        }
    }
}
