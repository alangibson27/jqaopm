package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.*;
import sun.misc.Unsafe;

public class OpExSpIndirectIndexed implements Operation {

    private final Register spReg;
    private final IndexRegister indexRegister;
    private final int[] memory;
    private final Unsafe unsafe = UnsafeUtil.getUnsafe();

    public OpExSpIndirectIndexed(final Processor processor, final Register indexRegister, final int[] memory) {
        this.spReg = processor.register("sp");
        this.indexRegister = IndexRegister.class.cast(indexRegister);
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int oldIndex = indexRegister.get();

        final int spLow = spReg.get();
        final int spHigh = 0xffff & (spLow + 1);
        indexRegister.set(
            Word.from(
                unsafe.getInt(memory, 16L + (spLow * 4)),
                unsafe.getInt(memory, 16L + (spHigh * 4))
            )
        );

        Memory.set(memory, spLow, oldIndex & 0x00ff);
        Memory.set(memory, spHigh, (oldIndex & 0xff00) >> 8);
        return 23;
    }

    @Override
    public String toString() {
        return "ex (sp), " + indexRegister.name();
    }
}