package me.neon.libs.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * NeonLibs
 * me.neon.libs.util
 *
 * @author 老廖
 * @since 2026/1/7 22:38
 */
public class ResourceLocation implements Comparable<ResourceLocation> {

    private static final char NAMESPACE_SEPARATOR = ':';

    private static final String DEFAULT_NAMESPACE = "minecraft";

    private static final String REALMS_NAMESPACE = "realms";

    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9._-]+");

    private static final Pattern PATH_PATTERN = Pattern.compile("[a-z0-9/._-]+");

    private String namespace = DEFAULT_NAMESPACE;

    private String path = REALMS_NAMESPACE;

    /**
     * 创建一个新的资源定位符。
     *
     * @param namespace 命名空间 (只能包含小写字母、数字、._-)
     * @param path      路径 (只能包含小写字母、数字、/._-)
     * @throws IllegalArgumentException 当 namespace 或 path 含有非法字符时
     * @throws NullPointerException     当参数为 null 时
     */
    public ResourceLocation(String namespace, String path) {
        this.namespace = assertValidNamespace(Objects.requireNonNull(namespace, "namespace"), path);
        this.path = assertValidPath(this.namespace, Objects.requireNonNull(path, "path"));
    }

    /** @return 路径部分 (不含命名空间) */
    public String getPath() {
        return this.path;
    }

    /** @return 命名空间 */
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public String toString() {
        return this.namespace + NAMESPACE_SEPARATOR + this.path;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof ResourceLocation) {
            ResourceLocation that = (ResourceLocation) other;
            return this.namespace.equalsIgnoreCase(that.namespace)
                    && this.path.equalsIgnoreCase(that.path);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, path);
    }

    /**
     * 排序规则：
     * 1. 先比较 namespace
     * 2. 再比较 path
     */
    @Override
    public int compareTo(ResourceLocation other) {
        int cmp = this.namespace.compareTo(other.namespace);
        if (cmp == 0) {
            cmp = this.path.compareTo(other.path);
        }
        return cmp;
    }

    /** 判断字符是否允许出现在 namespace 或 path 中 */
    public static boolean isAllowedInResourceLocation(char c) {
        return (c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'z') ||
                c == '_' || c == '-' || c == '.' ||
                c == '/' || c == NAMESPACE_SEPARATOR;
    }

    public static ResourceLocation withDefaultNamespace(String namespacedId, String defaultNamespace) {
        return of(decompose(namespacedId, defaultNamespace));
    }

    public static ResourceLocation of(String[] id) {
        return new ResourceLocation(id[0], id[1]);
    }

    public static ResourceLocation of(String namespaced) {
        return of(decompose(namespaced, "minecraft"));
    }

    public static ResourceLocation of(String namespaced, String path) {
        return new ResourceLocation(namespaced, path);
    }

    public static boolean isValid(final String resourceLocation) {
        int index = resourceLocation.indexOf(":");
        if (index == -1) {
            return isValidPath(resourceLocation);
        } else {
            return isValidNamespace(resourceLocation.substring(0, index)) && isValidPath(resourceLocation.substring(index + 1));
        }
    }

    /** 校验路径是否合法 */
    public static boolean isValidPath(String path) {
        return PATH_PATTERN.matcher(path).matches();
    }

    /** 校验命名空间是否合法 */
    public static boolean isValidNamespace(String namespace) {
        return NAMESPACE_PATTERN.matcher(namespace).matches();
    }

    /** 确保命名空间合法，否则抛出异常 */
    public static String assertValidNamespace(String namespace, String path) {
        if (!isValidNamespace(namespace)) {
            throw new IllegalArgumentException("Invalid namespace in location: " + namespace + ":" + path);
        }
        return namespace;
    }

    /** 确保路径合法，否则抛出异常 */
    public static String assertValidPath(String namespace, String path) {
        if (!isValidPath(path)) {
            throw new IllegalArgumentException("Invalid path in location: " + namespace + ":" + path);
        }
        return path;
    }

    private static String[] decompose(String id, String namespace) {
        String[] strings = new String[]{namespace, id};
        int i = id.indexOf(':');
        if (i >= 0) {
            strings[1] = id.substring(i + 1);
            if (i >= 1) {
                strings[0] = id.substring(0, i);
            }
        }
        return strings;
    }

    public static ResourceLocation ofEmpty() {
        return new ResourceLocation(DEFAULT_NAMESPACE, REALMS_NAMESPACE);
    }

    public static class ResourceLocationAdapter
            implements JsonSerializer<ResourceLocation>,
            JsonDeserializer<ResourceLocation>
    {

        @Override
        public JsonElement serialize(ResourceLocation src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString()); // 输出 "namespace:path"
        }

        @Override
        public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String s = json.getAsString();
            String[] parts = s.split(":", 2);
            if (parts.length == 2) {
                return new ResourceLocation(parts[0], parts[1]);
            } else {
                return new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, parts[0]);
            }
        }
    }

}
