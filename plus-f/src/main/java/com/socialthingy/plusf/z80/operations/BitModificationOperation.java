package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.z80.Operation;
import sun.misc.Unsafe;

abstract class BitModificationOperation implements Operation {
    private final int bitPosition;
    protected final Unsafe unsafe = UnsafeUtil.getUnsafe();

    protected BitModificationOperation(final int bitPosition) {
        this.bitPosition = bitPosition;
    }

    protected int reset(final int value) {
        return value & (0xff - (1 << bitPosition));
    }

    protected int set(final int value) {
        return value | (1 << bitPosition);
    }
}