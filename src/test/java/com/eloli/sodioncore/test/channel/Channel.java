package com.eloli.sodioncore.test.channel;

import com.eloli.sodioncore.channel.BadSignException;
import com.eloli.sodioncore.channel.MessageChannel;
import com.eloli.sodioncore.channel.util.ByteUtil;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Channel {
    @Test
    public void packet() throws BadSignException {
        byte[] key = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
        MessageChannel clientChannel = new MessageChannel("test",
                ByteUtil.sha256(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)),
                ByteUtil.sha256(key))
                .registerClientPacket(TestPacket.class);
        MessageChannel serverChannel = new MessageChannel("test",
                ByteUtil.sha256(key),
                ByteUtil.sha256(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)))
                .registerClientPacket(TestPacket.class);
        TestPacket packet = new TestPacket();
        packet.sss = 233;
        packet.pp = new InnerPacket();
        packet.pp.sssub = "safdjkl";
        packet.pp.inss = new int[]{2, 3, 5, 9};
        byte[] bytes = clientChannel.getClientFactory(TestPacket.class).encode(packet);

        packet = (TestPacket) serverChannel.getClientFactory(bytes).parser(bytes);

        assertEquals(packet.sss, 233);
        assertEquals(packet.pp.sssub, "safdjkl");
        assertArrayEquals(packet.pp.inss, new int[]{2, 3, 5, 9});
    }
}
