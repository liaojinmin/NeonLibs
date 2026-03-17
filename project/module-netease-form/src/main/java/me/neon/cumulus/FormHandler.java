package me.neon.cumulus;

import com.google.common.base.Charsets;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import me.neon.cumulus.form.Form;
import me.neon.cumulus.form.impl.FormDefinition;
import me.neon.cumulus.form.impl.FormDefinitions;
import me.neon.libs.NeonLibsLoader;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NeonLibs
 * me.neon.cumulus
 *
 * @author 老廖
 * @since 2026/3/4 19:01
 */
public class FormHandler implements PluginMessageListener, Listener {

    private static final String identifier = "floodgate:form";

    private static FormHandler formHandler;

    private final FormDefinitions formDefinitions =
            FormDefinitions.instance();

    private final Short2ObjectMap<Form> storedForms =
            Short2ObjectMaps.synchronize(new Short2ObjectOpenHashMap<>());

    // Tracking for players last opened form
    private final Object2ShortMap<UUID> uuidForms =
            Object2ShortMaps.synchronize(new Object2ShortOpenHashMap<>());

    private final AtomicInteger nextFormId = new AtomicInteger(1);

    @NotNull
    @Deprecated
    public static FormHandler getFormChannel() {
        return getFormHandler();
    }

    @NotNull
    public static FormHandler getFormHandler() {
        if (formHandler == null) {
            formHandler = new FormHandler();
        }
        return formHandler;
    }

    private FormHandler() {
        Messenger messenger = Bukkit.getServer().getMessenger();
        // 注册接收通道
        messenger.registerIncomingPluginChannel(NeonLibsLoader.getInstance(), identifier, this);
        messenger.registerOutgoingPluginChannel(NeonLibsLoader.getInstance(), identifier);
        Bukkit.getPluginManager().registerEvents(this, NeonLibsLoader.getInstance());
        Bukkit.getOnlinePlayers().forEach(this::register);
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        register(player);
    }

    private void register(Player player) {
        try {
            Class<? extends CommandSender> senderClass = player.getClass();
            Method addChannel = senderClass.getDeclaredMethod("addChannel", String.class);
            addChannel.setAccessible(true);
            addChannel.invoke(player, identifier);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        try {
            Form storedForm = storedForms.remove(getFormId(message));
            if (storedForm != null) {
                String responseData = new String(message, 2, message.length - 2, Charsets.UTF_8);
                try {
                    formDefinitions.definitionFor(storedForm)
                            .handleFormResponse(storedForm, responseData);
                } catch (Exception e) {
                    Bukkit.getLogger().severe("Error while processing form response!" + e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendForm(Player player, Form form) {
        // Player can only open one form at a time, so we make old ones invalid
        playerRemoved(player.getUniqueId());
        byte[] formData = createFormData(player.getUniqueId(), form);
        player.sendPluginMessage(NeonLibsLoader.getInstance(), identifier, formData);

    }

    public void sendForm(UUID player, Form form) {
        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            sendForm(p, form);
        }
    }

    public byte[] createFormData(UUID player, Form form) {
        short formId = getNextFormId();
        uuidForms.put(player, formId);
        storedForms.put(formId, form);

        FormDefinition<Form, ?, ?> definition = formDefinitions.definitionFor(form);

        byte[] jsonData =
                definition.codec()
                        .jsonData(form)
                        .getBytes(Charsets.UTF_8);

        byte[] data = new byte[jsonData.length + 3];
        data[0] = (byte) definition.formType().ordinal();
        data[1] = (byte) (formId >> 8 & 0xFF);
        data[2] = (byte) (formId & 0xFF);
        System.arraycopy(jsonData, 0, data, 3, jsonData.length);
        return data;
    }

    public void playerRemoved(UUID correctUuid) {
        short key = uuidForms.removeShort(correctUuid);
        if (key != 0) {
            Form storedForm = storedForms.remove(key);
            if (storedForm != null) {
                try {
                    formDefinitions.definitionFor(storedForm)
                            .handleFormResponse(storedForm, "null");
                } catch (Exception e) {
                    Bukkit.getLogger().severe("Error while processing form response!" + e);
                }
            }
        }
    }

    protected short getFormId(byte[] data) {
        return (short) ((data[0] & 0xFF) << 8 | data[1] & 0xFF);
    }

    protected short getNextFormId() {
        // signed bit is used to check if the form is from a proxy or a server
        return (short) nextFormId.getAndUpdate(
                (number) -> number == Short.MAX_VALUE ? 0 : number + 1);
    }

}