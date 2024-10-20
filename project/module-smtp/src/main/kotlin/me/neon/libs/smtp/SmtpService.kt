package me.neon.libs.smtp

import me.neon.libs.NeonLibsLoader
import me.neon.libs.util.asyncRunner
import org.bukkit.plugin.Plugin
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


@me.neon.libs.core.env.RuntimeDependencies(
    me.neon.libs.core.env.RuntimeDependency(
        value = "!javax.mail:mail:1.5.0-b01",
        test = "javax.mail.Version",
        relocate = ["!javax.mail", "javax.mail"],
        transitive = false
    ),
    me.neon.libs.core.env.RuntimeDependency(
        value = "!javax.activation:activation:1.1.1",
        test = "javax.activation.URLDataSource",
        relocate = ["!javax.activation", "javax.activation"],
        transitive = false
    ),
    me.neon.libs.core.env.RuntimeDependency(
        value = "!redis.clients:jedis:4.2.2",
        test = "redis.clients.jedis.BuilderFactory",
        relocate = ["!redis.clients", "redis.clients"],
        transitive = false
    )
)
class SmtpService(
    private val plugin: Plugin,
    private val param: MutableMap<String, String>
) {
    private val account = param["account"] ?: error("找不到发件账号")
    private val password = param["password"] ?: error("找不到smt授权码") // "JRKHOKSYAPSOUHOS"
    private val personal = param["personal"] ?: "NeonMail-Premium"
    private val subjects = param["subjects"] ?: "NeonMail-收件提醒"


    private val properties by lazy {
        Properties().apply { putAll(param) }
    }

    private val authenticator: Authenticator by lazy {
        object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(account, password)
            }
        }
    }


    fun sendCustomHtml(html: String, vararg targetMail: String) {
        asyncRunner {
            createConnection { transport, session ->
                val message = createMimeMessage(session)
                message.setRecipients(Message.RecipientType.TO, targetMail.map { InternetAddress(it) }.toTypedArray())
                message.setContent(html, "text/html;charset=gb2312")
                transport.sendMessage(message, message.allRecipients)
            }
            onError {
                NeonLibsLoader.warning("发送自定义html时出现异常")
                it.printStackTrace()
            }
        }
    }

    private fun createMimeMessage(session: Session): MimeMessage {
        return MimeMessage(session).apply {
            setFrom(InternetAddress(account, personal, "UTF-8"))
            subject = subjects
        }
    }

    private fun createConnection(function: (Transport, Session) -> Unit) {
        val session = Session.getInstance(properties, authenticator)
        session.transport.use { function.invoke(this.also { connect() }, session) }
    }

    fun File.toHtmlString(): String {
        // 获取HTML文件流
        val htmlSb = StringBuffer()
        BufferedReader(
            InputStreamReader(
                FileInputStream(this), "UTF-8"
            )
        ).use {
            while (it.ready()) {
                htmlSb.append(it.readLine())
            }
        }
        return htmlSb.toString()
    }

    private fun <T> Transport.use(func: Transport.() -> T): T {
        return try {
            func(this)
        } catch (ex: Exception) {
            throw ex
        } finally {
            close()
        }
    }
}


