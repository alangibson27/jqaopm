package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import static com.socialthingy.plusf.util.Bitwise.binary;

public class PureDataBlock extends TzxBlock {

    public static Try<PureDataBlock> read(final InputStream tzxFile) {
        try {
            final int zeroPulseLength = nextWord(tzxFile);
            final int onePulseLength = nextWord(tzxFile);
            final int finalByteBitsUsed = nextByte(tzxFile);
            final Duration pauseLength = Duration.ofMillis(nextWord(tzxFile));
            final int dataLength = nextTriple(tzxFile);

            final int[] data = new int[dataLength];
            for (int i = 0; i < dataLength; i++) {
                data[i] = nextByte(tzxFile);
            }

            return Try.success(
                new PureDataBlock(pauseLength, data, zeroPulseLength, onePulseLength, finalByteBitsUsed)
            );
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    private static final int[] finalByteMask = new int[] {
        binary("10000000"),
        binary("11000000"),
        binary("11100000"),
        binary("11110000"),
        binary("11111000"),
        binary("11111100"),
        binary("11111110"),
        binary("11111111")
    };

    private final Duration pauseLength;
    private final int[] data;
    private final int zeroPulseLength;
    private final int onePulseLength;
    private final int finalByteBitsUsed;

    private PureDataBlock(
        final Duration pauseLength,
        final int[] data,
        final int zeroPulseLength,
        final int onePulseLength,
        final int finalByteBitsUsed
    ) {
        this.pauseLength = pauseLength;
        this.data = data;
        this.zeroPulseLength = zeroPulseLength;
        this.onePulseLength = onePulseLength;
        this.finalByteBitsUsed = finalByteBitsUsed;
    }

    public Duration getPauseLength() {
        return pauseLength;
    }

    public int[] getData() {
        return data;
    }

    @Override
    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        boolean state = initialState;
        for (int i = 0; i < data.length; i++) {
            final int b;
            final int lastBit;
            if (i == data.length - 1) {
                b = data[i] & finalByteMask[finalByteBitsUsed - 1];
                lastBit = 8 - finalByteBitsUsed;
            } else {
                b = data[i];
                lastBit = 0;
            }

            for (int bit = 7; bit >= lastBit; bit--) {
                final boolean high = (b & (1 << bit)) != 0;
                final int pulseLen = high ? onePulseLength : zeroPulseLength;
                tape.add(new Bit(state, "data"), pulseLen);
                state = !state;
                tape.add(new Bit(state, "data"), pulseLen);
                state = !state;
            }
        }

        // pause
        tape.add(new Bit(false, "pause"), 3500 * (int) pauseLength.toMillis());
        return false;
    }

    @Override
    public String toString() {
        return String.format(
            "Pure data block: %d bytes, hi: %d, lo: %d, fbb: %d, pause: %d ms",
            data.length,
            onePulseLength,
            zeroPulseLength,
            finalByteBitsUsed,
            pauseLength.toMillis()
        );
    }
}
