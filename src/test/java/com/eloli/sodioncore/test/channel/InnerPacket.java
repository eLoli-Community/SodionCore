package com.eloli.sodioncore.test.channel;

import com.eloli.sodioncore.channel.ClientPacket;
import com.eloli.sodioncore.channel.util.FieldWrapper;
import com.eloli.sodioncore.channel.util.Priority;

import java.util.List;

public class InnerPacket extends ClientPacket {
    @Priority(0)
    public String sssub;

    @Priority(1)
    public int[] inss;

    @Override
    public List<FieldWrapper> getFieldWrapperList() {
        return resolveFieldWrapperList(this.getClass());
    }
}
