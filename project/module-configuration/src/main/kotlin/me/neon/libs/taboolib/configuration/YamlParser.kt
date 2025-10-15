package me.neon.libs.taboolib.configuration

import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.ConfigFormat
import com.electronwill.nightconfig.core.io.ConfigParser
import com.electronwill.nightconfig.core.io.ParsingException
import com.electronwill.nightconfig.core.io.ParsingMode
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.MappingNode
import org.yaml.snakeyaml.reader.UnicodeReader
import java.io.ByteArrayInputStream
import java.io.Reader
import java.nio.charset.StandardCharsets

/**
 * YAML 解析器类，实现了 ConfigParser<CommentedConfig> 接口
 *
 * @property configFormat 配置格式
 */
class YamlParser(val configFormat: ConfigFormat<CommentedConfig>) : ConfigParser<CommentedConfig> {

    private val dumperOptions = DumperOptions()
    private val loaderOptions = LoaderOptions()
    private val representer: YamlRepresenter
    private val constructor: YamlConstructor
    private val yaml: Yaml
    private val yamlCommentLoader: YamlCommentLoader

    init {
        dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        loaderOptions.maxAliasesForCollections = Integer.MAX_VALUE
        representer = YamlRepresenter(dumperOptions)
        representer.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        constructor = YamlConstructor(loaderOptions)
        yaml = BukkitYaml(
            constructor,
            representer,
            dumperOptions,
            loaderOptions
        )
        yamlCommentLoader = YamlCommentLoader(
            dumperOptions,
            loaderOptions,
            constructor,
            representer,
            yaml
        )
    }

    override fun getFormat(): ConfigFormat<CommentedConfig> {
        return configFormat
    }

    override fun parse(reader: Reader): CommentedConfig {
        val config = configFormat.createConfig { LinkedHashMap() }
        parse(reader, config, ParsingMode.MERGE)
        return config
    }

    override fun parse(reader: Reader, destination: Config, parsingMode: ParsingMode) {
        try {
            loadFromString(reader.readText(), ConfigSection(destination))
        } catch (e: Exception) {
            throw ParsingException("YAML parsing failed", e)
        }
    }

    /**
     * 从字符串加载 YAML 内容
     *
     * @param contents YAML 内容字符串
     * @param section 目标配置部分
     */
    fun loadFromString(contents: String, section: ConfigurationSection) {
        if (contents.isEmpty()) {
            return
        }
        loaderOptions.isProcessComments = true
        var node: MappingNode
        UnicodeReader(ByteArrayInputStream(contents.toByteArray(StandardCharsets.UTF_8))).use { reader ->
            node = yaml.compose(reader) as? MappingNode ?: return
        }
        yamlCommentLoader.adjustNodeComments(node)
        yamlCommentLoader.fromNodeTree(node, section)
    }
}