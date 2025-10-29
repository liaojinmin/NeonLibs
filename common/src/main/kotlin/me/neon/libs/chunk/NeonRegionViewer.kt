package me.neon.libs.chunk

import me.neon.libs.chunk.index.VRegion
import me.neon.libs.chunk.storage.RegionFile
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.nio.file.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

/**
 * NeonEngine
 * me.neon.engine.chunk
 *
 * @author 老廖
 * @since 2025/10/29 07:51
 */
class NeonRegionViewer(file: File? = null) {

    private val stopFlag = AtomicBoolean(false)
    private var watchThread: Thread? = null
    private val frame = JFrame("Neon Region Viewer")

    init {
        SwingUtilities.invokeLater {
            setupUI()
            val targetFile = file ?: chooseFile() ?: return@invokeLater
            loadAndWatchFile(targetFile)
        }
    }

    private fun chooseFile(): File? {
        val chooser = JFileChooser().apply {
            dialogTitle = "选择 .neon 文件"
            fileSelectionMode = JFileChooser.FILES_ONLY
            currentDirectory = File(System.getProperty("user.dir"))
        }
        return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
    }

    private fun setupUI() {
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frame.setSize(800, 600)
        frame.setLocationRelativeTo(null)
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                stopFlag.set(true)
                watchThread?.interrupt()
            }
        })
        frame.isVisible = true
    }

    private fun loadAndWatchFile(file: File) {
        val rootNode = DefaultMutableTreeNode(file.name)
        val tree = JTree(rootNode)
        frame.contentPane.removeAll()
        frame.contentPane.add(JScrollPane(tree), BorderLayout.CENTER)
        frame.revalidate()
        frame.repaint()

        // 从文件名解析 VRegion
        val region = parseRegionFromFile(file)
        if (region == null) {
            rootNode.add(DefaultMutableTreeNode("无法解析文件坐标"))
            (tree.model as DefaultTreeModel).reload()
            return
        }

        // 初次加载
        updateTree(file, region, rootNode, tree)

        // 启动文件监听
        val watchService = FileSystems.getDefault().newWatchService()
        val parentPath = file.toPath().parent ?: return
        parentPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)

        watchThread = Thread {
            try {
                while (!stopFlag.get()) {
                    val key = watchService.take()
                    key.pollEvents().forEach { event ->
                        val changed = event.context() as Path
                        if (changed.endsWith(file.name)) {
                            SwingUtilities.invokeLater {
                                updateTree(file, region, rootNode, tree)
                            }
                        }
                    }
                    key.reset()
                }
            } catch (_: InterruptedException) {
            } finally {
                watchService.close()
            }
        }.apply { isDaemon = true; start() }
    }

    private fun updateTree(file: File, region: VRegion, rootNode: DefaultMutableTreeNode, tree: JTree) {
        rootNode.removeAllChildren()
        try {
            val regionFile = RegionFile(file, region)
            val allChunks = regionFile.readAllChunks()
            for ((coord, chunk) in allChunks) {
                val chunkNode = DefaultMutableTreeNode(coord.toString())
                chunkNode.add(DefaultMutableTreeNode(chunk.toJson()))
                rootNode.add(chunkNode)
            }
        } catch (ex: Exception) {
            rootNode.add(DefaultMutableTreeNode("加载失败: ${ex.message}"))
        }
        (tree.model as DefaultTreeModel).reload()
        tree.expandRow(0)
    }

    private fun parseRegionFromFile(file: File): VRegion? {
        // 文件名格式 r.X.Z.neon
        val name = file.nameWithoutExtension
        val parts = name.split(".")
        if (parts.size != 3) return null
        return try {
            val x = parts[1].toInt()
            val z = parts[2].toInt()
            VRegion(x, z)
        } catch (_: NumberFormatException) {
            null
        }
    }

}
