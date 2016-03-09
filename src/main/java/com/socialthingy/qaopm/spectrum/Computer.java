package com.socialthingy.qaopm.spectrum;

import com.socialthingy.qaopm.snapshot.SnapshotLoader;
import com.socialthingy.qaopm.z80.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Computer extends Application implements InterruptingDevice {

    private final Processor processor;
    private final ULA ula;
    private final int[] memory;
    private final Display display;
    public static final int T_STATES_PER_REFRESH = 80000;
    private ImageView imageView;
    private PrintWriter dumpFile;
    private final Map<KeyCode, Character> spectrumKeys = new HashMap<>();

    public static void main(final String[] args) throws IOException {
        Application.launch(args);
    }

    public Computer() throws IOException {
        memory = new int[0x10000];
        ula = new ULA();
        processor = new Processor(memory, ula);
        display = new Display(memory);
        dumpFile = new PrintWriter(new FileWriter("/var/tmp/java_spectrum.dump"));

        spectrumKeys.put(KeyCode.A, 'a');
        spectrumKeys.put(KeyCode.B, 'b');
        spectrumKeys.put(KeyCode.C, 'c');
        spectrumKeys.put(KeyCode.D, 'd');
        spectrumKeys.put(KeyCode.E, 'e');
        spectrumKeys.put(KeyCode.F, 'f');
        spectrumKeys.put(KeyCode.G, 'g');
        spectrumKeys.put(KeyCode.H, 'h');
        spectrumKeys.put(KeyCode.I, 'i');
        spectrumKeys.put(KeyCode.J, 'j');
        spectrumKeys.put(KeyCode.K, 'k');
        spectrumKeys.put(KeyCode.L, 'l');
        spectrumKeys.put(KeyCode.M, 'm');
        spectrumKeys.put(KeyCode.N, 'n');
        spectrumKeys.put(KeyCode.O, 'o');
        spectrumKeys.put(KeyCode.P, 'p');
        spectrumKeys.put(KeyCode.Q, 'q');
        spectrumKeys.put(KeyCode.R, 'r');
        spectrumKeys.put(KeyCode.S, 's');
        spectrumKeys.put(KeyCode.T, 't');
        spectrumKeys.put(KeyCode.U, 'u');
        spectrumKeys.put(KeyCode.V, 'v');
        spectrumKeys.put(KeyCode.W, 'w');
        spectrumKeys.put(KeyCode.X, 'x');
        spectrumKeys.put(KeyCode.Y, 'y');
        spectrumKeys.put(KeyCode.Z, 'z');

        spectrumKeys.put(KeyCode.DIGIT0, '0');
        spectrumKeys.put(KeyCode.DIGIT1, '1');
        spectrumKeys.put(KeyCode.DIGIT2, '2');
        spectrumKeys.put(KeyCode.DIGIT3, '3');
        spectrumKeys.put(KeyCode.DIGIT4, '4');
        spectrumKeys.put(KeyCode.DIGIT5, '5');
        spectrumKeys.put(KeyCode.DIGIT6, '6');
        spectrumKeys.put(KeyCode.DIGIT7, '7');
        spectrumKeys.put(KeyCode.DIGIT8, '8');
        spectrumKeys.put(KeyCode.DIGIT9, '9');

        spectrumKeys.put(KeyCode.SHIFT, '^');
        spectrumKeys.put(KeyCode.CONTROL, '$');
        spectrumKeys.put(KeyCode.SPACE, ' ');
        spectrumKeys.put(KeyCode.ENTER, '_');
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final String romFile = getParameters().getRaw().get(0);

        try (final FileInputStream fis = new FileInputStream(romFile)) {
            int addr = 0;
            for (int next = fis.read(); next != -1; next = fis.read()) {
                memory[addr++] = next;
            }
        }

        if (getParameters().getRaw().size() > 1) {
            final String snapshotFile = getParameters().getRaw().get(1);
            loadSnapshot(snapshotFile);
        }

        Group root = new Group();
        Scene scene = new Scene(root);

        imageView = new ImageView(display.getScreen());
        root.getChildren().add(imageView);
        primaryStage.setScene(scene);
        primaryStage.show();

        final SpectrumKeyHandler keyHandler = new SpectrumKeyHandler();
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, keyHandler);

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::singleRefresh, 0, 40, TimeUnit.MILLISECONDS);
    }

    private class SpectrumKeyHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent event) {
            final Character spectrumKey = spectrumKeys.get(event.getCode());
            if (spectrumKey != null) {
                if (event.getEventType() == KeyEvent.KEY_PRESSED) {
                    ula.keyDown(spectrumKey);
                } else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
                    ula.keyUp(spectrumKey);
                }
            }
        }
    }

    private void loadSnapshot(String snapshotFile) throws IOException {
        try (final FileInputStream fis = new FileInputStream(snapshotFile)) {
            final SnapshotLoader sl = new SnapshotLoader(fis);
            sl.read(processor, memory);
        }
    }

    public void singleRefresh() {
        Platform.runLater(() -> imageView.setImage(display.refresh()));

        int tStates = 0;
        processor.interrupt(new InterruptRequest(this));

        while (tStates < T_STATES_PER_REFRESH) {
            try {
                processor.execute();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            tStates += processor.lastTime();
        }
    }

    private void dumpState() {
        dumpFile.println(String.format("af: %04x bc: %04x de: %04x hl: %04x",
                processor.register("af").get(),
                processor.register("bc").get(),
                processor.register("de").get(),
                processor.register("hl").get()));
        dumpFile.println(String.format("af':%04x bc':%04x de':%04x hl':%04x",
                processor.register("af'").get(),
                processor.register("bc'").get(),
                processor.register("de'").get(),
                processor.register("hl'").get()));
        dumpFile.println(String.format("ix: %04x iy: %04x pc: %04x sp: %04x ir:%02x%02x",
                processor.register("ix").get(),
                processor.register("iy").get(),
                processor.register("pc").get(),
                processor.register("sp").get(),
                processor.register("i").get(),
                processor.register("r").get()));
        dumpFile.println(String.format("iff1: %s  iff2: %s  im: %d",
                processor.getIff(0) ? "True" : "False",
                processor.getIff(1) ? "True" : "False",
                processor.getInterruptMode()));
        dumpFile.println();
        dumpFile.flush();
    }

    @Override
    public void acknowledge() {
    }
}
