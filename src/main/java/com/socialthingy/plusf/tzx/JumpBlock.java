package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;

public class JumpBlock extends TzxBlock {

    private final int blocks;

    public static Try<JumpBlock> read(final InputStream tzxFile) {
        try {
            final int blocks = nextWord(tzxFile);
            return Try.success(new JumpBlock(blocks));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    public JumpBlock(final int blocks) {
        this.blocks = blocks;
    }

    public int getBlocks() {
        return blocks;
    }

    @Override
    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        return initialState;
    }

    @Override
    public String toString() {
        return String.format("Jump block: %d", blocks);
    }
}