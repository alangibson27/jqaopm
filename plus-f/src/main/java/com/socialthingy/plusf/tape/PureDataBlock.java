package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.tape.SignalState.Adjustment;
import com.socialthingy.plusf.util.Try;
import com.socialthingy.replist.RepList;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

public class PureDataBlock extends TapeBlock {

    public static final int[] FINAL_OPPOSITE_EDGE_PULSE = new int[]{3500};

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

    private final Duration pauseLength;
    private final int[] data;
    private final int zeroPulseLength;
    private final int onePulseLength;
    private final int finalByteBitsUsed;

    public PureDataBlock(
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

    public int[] getData() {
        return data;
    }

    @Override
    public RepList<Boolean> getBitList(SignalState signalState) {
        final RepList<Boolean> bits = new RepList<>();
        for (int byteIdx = 0; byteIdx < data.length - 1; byteIdx++) {
            for (int bitIdx = 7; bitIdx >= 0; bitIdx--) {
                bits.append(new PulseSequenceBlock(Adjustment.NO_CHANGE, bitPulses(data[byteIdx], bitIdx)).getBitList(signalState));
            }
        }

        final int finalByte = data[data.length - 1];
        for (int bitIdx = 7; bitIdx >= 8 - finalByteBitsUsed; bitIdx--) {
            bits.append(new PulseSequenceBlock(Adjustment.NO_CHANGE, bitPulses(finalByte, bitIdx)).getBitList(signalState));
        }

        if (!pauseLength.isZero()) {
            bits.append(new PulseSequenceBlock(Adjustment.NO_CHANGE, FINAL_OPPOSITE_EDGE_PULSE).getBitList(signalState));
            bits.append(new PauseBlock(pauseLength).getBitList(signalState));
        }

        return bits;
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

    private int[] bitPulses(final int dataByte, final int bit) {
        final boolean high = (dataByte & (1 << bit)) != 0;
        if (high) {
            return new int[] {onePulseLength, onePulseLength};
        } else {
            return new int[] {zeroPulseLength, zeroPulseLength};
        }
    }
}