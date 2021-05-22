package com.eloli.sodioncore.bukkit.channel;

import com.eloli.sodioncore.channel.*;
import com.eloli.sodioncore.logger.AbstractLogger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitChannel {

    @SuppressWarnings("unchecked")
    public BukkitChannel(
            JavaPlugin plugin, MessageChannel messageChannel,
            AbstractLogger logger, PacketReceiveListener listener) {
        plugin.getServer().getMessenger()
                .registerIncomingPluginChannel(plugin, messageChannel.name,
                        (channel, player, message) -> {
                            try {
                                listener.onPacketReceive(player, messageChannel.getClientFactory(message).parser(message),
                                        packet -> player.sendPluginMessage(plugin, messageChannel.name,
                                                ((PacketFactory<ServerPacket>) messageChannel.getServerFactory(packet.getClass()))
                                                        .encode(packet))
                                );
                            } catch (BadSignException e) {
                                logger.info("Bad sign", e);
                            }
                        });
        plugin.getServer().getMessenger()
                .registerOutgoingPluginChannel(plugin, messageChannel.name);
    }

    public interface PacketReceiveListener {
        void onPacketReceive(
                Player player,
                ClientPacket packet,
                PacketSender sender
        );
    }

    public interface PacketSender {
        void send(ServerPacket packet);
    }
}
