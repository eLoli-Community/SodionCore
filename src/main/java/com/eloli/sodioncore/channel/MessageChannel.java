package com.eloli.sodioncore.channel;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class MessageChannel {
    public final String name;

    public final byte[] serverKey;
    public final byte[] clientKey;
    private Map<Class<? extends ClientPacket>, PacketFactory<? extends ClientPacket>> clientPacketsByClass = new HashMap<>();
    private Map<Class<? extends ServerPacket>, PacketFactory<? extends ServerPacket>> serverPacketsByClass = new HashMap<>();

    private Map<Integer, PacketFactory<? extends ClientPacket>> clientPacketsById = new HashMap<>();
    private Map<Integer, PacketFactory<? extends ServerPacket>> serverPacketsById = new HashMap<>();

    public MessageChannel(String name, byte[] serverKey, byte[] clientKey) {
        this.name = name;
        this.serverKey = serverKey;
        this.clientKey = clientKey;
    }

    public <P extends ClientPacket> MessageChannel registerClientPacket(Class<P> packet) {
        int id = clientPacketsById.size();
        PacketFactory<P> factory = new PacketFactory<>(this, packet, id);
        clientPacketsByClass.put(packet, factory);
        clientPacketsById.put(id, factory);
        return this;
    }

    public <P extends ServerPacket> MessageChannel registerServerPacket(Class<P> packet) {
        int id = serverPacketsById.size();
        PacketFactory<P> factory = new PacketFactory<>(this, packet, id);
        serverPacketsByClass.put(packet, factory);
        serverPacketsById.put(id, factory);
        return this;
    }

    public PacketFactory<? extends ClientPacket> getClientFactory(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        return getClientFactory(buffer.getInt());
    }

    public PacketFactory<? extends ClientPacket> getClientFactory(int id) {
        return clientPacketsById.get(id);
    }

    @SuppressWarnings("unchecked")
    public <T extends ClientPacket> PacketFactory<T> getClientFactory(Class<T> clazz) {
        return (PacketFactory<T>) clientPacketsByClass.get(clazz);
    }

    public PacketFactory<? extends ServerPacket> getServerFactory(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        return getServerFactory(buffer.getInt());
    }

    public PacketFactory<? extends ServerPacket> getServerFactory(int id) {
        return serverPacketsById.get(id);
    }

    @SuppressWarnings("unchecked")
    public <T extends ServerPacket> PacketFactory<T> getServerFactory(Class<T> clazz) {
        return (PacketFactory<T>) serverPacketsByClass.get(clazz);
    }
}
