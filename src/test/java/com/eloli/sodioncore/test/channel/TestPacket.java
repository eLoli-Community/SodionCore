package com.eloli.sodioncore.test.channel;

import com.eloli.sodioncore.channel.ClientPacket;
import com.eloli.sodioncore.channel.util.FieldWrapper;
import com.eloli.sodioncore.channel.util.Priority;

import java.util.List;

public class TestPacket extends ClientPacket {
    @Priority(0)
    public int sss;

    @Priority(1)
    public InnerPacket pp;

    @Override
    public List<FieldWrapper> getFieldWrapperList() {
        return resolveFieldWrapperList(this.getClass());
    }
}
