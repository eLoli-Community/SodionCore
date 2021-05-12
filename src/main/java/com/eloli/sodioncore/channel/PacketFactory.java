package com.eloli.sodioncore.channel;

import com.eloli.sodioncore.channel.util.ByteUtil;
import com.eloli.sodioncore.channel.util.PayloadDecoder;
import com.eloli.sodioncore.channel.util.PayloadEncoder;

import java.util.Arrays;

// FIXME: Too much bytes copy, Low performance.
public class PacketFactory<P extends PluginMessagePacket> {
    private static final int SIGN_LENGTH = 32;
    protected MessageChannel proxyChannel;
    protected Class<P> packetClass;
    protected int id;

    public PacketFactory(MessageChannel proxyChannel, Class<P> packetClass, int id) {
        this.proxyChannel = proxyChannel;
        this.packetClass = packetClass;
        this.id = id;
    }

    public P parser(byte[] data) throws BadSignException {
        byte[] packetSign = new byte[SIGN_LENGTH];
        System.arraycopy(data, data.length - SIGN_LENGTH, packetSign, 0, SIGN_LENGTH);
        if (ClientPacket.class.isAssignableFrom(packetClass)) {
            System.arraycopy(proxyChannel.serverKey, 0, data, data.length - SIGN_LENGTH, SIGN_LENGTH);
        } else {
            System.arraycopy(proxyChannel.clientKey, 0, data, data.length - SIGN_LENGTH, SIGN_LENGTH);
        }
        byte[] currentSign = ByteUtil.sha256(data);
        if (!Arrays.equals(packetSign, currentSign)) {
            throw new BadSignException();
        }
        return PayloadDecoder.resolve(data, packetClass);
    }

    public byte[] encode(P packet) {
        byte[] content = PayloadEncoder.getPayload(id, packet);
        byte[] data;
        if (ClientPacket.class.isAssignableFrom(packetClass)) {
            data = ByteUtil.merge(content, proxyChannel.clientKey);
        } else {
            data = ByteUtil.merge(content, proxyChannel.serverKey);
        }
        byte[] sign = ByteUtil.sha256(data);
        System.arraycopy(sign, 0, data, data.length - SIGN_LENGTH, SIGN_LENGTH);
        return data;
    }
}
