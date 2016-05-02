package com.socialthingy.qaopm.spectrum;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.socialthingy.qaopm.spectrum.dialog.ContactInfoFinder;
import com.socialthingy.qaopm.spectrum.display.JavaFXBorder;
import com.socialthingy.qaopm.spectrum.display.JavaFXDisplay;
import com.socialthingy.qaopm.spectrum.io.IOMultiplexer;
import com.socialthingy.qaopm.spectrum.io.SinglePortIO;
import com.socialthingy.qaopm.spectrum.input.SpectrumKeyboard;
import com.socialthingy.qaopm.spectrum.io.ULA;
import com.socialthingy.qaopm.spectrum.remote.GuestState;
import com.socialthingy.qaopm.spectrum.remote.NetworkPeer;
import com.socialthingy.qaopm.spectrum.remote.SpectrumState;
import com.socialthingy.qaopm.z80.Processor;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Optional;
import java.util.TimerTask;

import static com.socialthingy.qaopm.spectrum.UIBuilder.BORDER;
import static com.socialthingy.qaopm.spectrum.UIBuilder.registerMenuItem;
import static com.socialthingy.qaopm.spectrum.dialog.ConnectionDetailsDialog.getConnectionDetails;
import static javafx.scene.input.KeyCode.*;

public class JavaFXComputer extends Application {

    private static final String DISPLAY_REFRESH_TIMER_NAME = "display.refresh";
    private static final int LOCAL_PORT = 7000;

    private final Timer displayRefreshTimer;
    private final ComputerLoop computerLoop;
    private final MetricRegistry metricRegistry;
    private final JavaFXDisplay display;
    private final JavaFXBorder border;
    private final Label statusLabel;

    private ULA ula;
    private Computer computer;
    private SpectrumKeyboard keyboard;
    private int[] memory;
    private MenuItem connectItem;
    private MenuItem disconnectItem;
    private Optional<NetworkPeer<GuestState>> hostRelay = Optional.empty();
    private SinglePortIO guestKempstonJoystick;

    private Stage primaryStage;
    private IOMultiplexer ioMux;

    public static void main(final String ... args) {
        Application.launch(args);
    }

    public JavaFXComputer() {
        metricRegistry = new MetricRegistry();
        displayRefreshTimer = metricRegistry.timer(DISPLAY_REFRESH_TIMER_NAME);
        computerLoop = new ComputerLoop();
        display = new JavaFXDisplay();
        border = new JavaFXBorder();
        statusLabel = new Label("No guest connected");
    }

    private void newComputer() throws IOException {
        ioMux = new IOMultiplexer();
        memory = new int[0x10000];
        computer = new Computer(new Processor(memory, ioMux), memory, new Timings(50, 60, 3500000), metricRegistry);
        ula = new ULA(computer, BORDER, BORDER);
        guestKempstonJoystick = new SinglePortIO(0x1f);
        keyboard = new SpectrumKeyboard(ula);

        ioMux.register(0xfe, ula);
        ioMux.register(0x1f, guestKempstonJoystick);

        final String romFile = getParameters().getRaw().get(0);
        computer.loadRom(romFile);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        newComputer();

        final Iterator<String> params = getParameters().getRaw().iterator();
        params.next();
        if (params.hasNext()) {
            computer.loadSnapshot(new File(params.next()));
        }

        final java.util.Timer statusBarTimer = new java.util.Timer();
        statusBarTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (!hostRelay.isPresent()) {
                        statusLabel.setText("Not connected to guest");
                    }
                    hostRelay.ifPresent(h -> {
                        if (h.awaitingCommunication()) {
                            statusLabel.setText("Awaiting communication from guest");
                        } else {
                            final String text = String.format(
                                    "Connected - average delay: %f ms, out-of-sequence: %d",
                                    h.getAverageLatency(),
                                    h.getOutOfOrderPacketCount()
                            );
                            statusLabel.setText(text);
                        }
                    });
                });
            }
        }, 0L, 5000L);

        UIBuilder.buildUI(primaryStage, display, border, statusLabel, getMenuBar());
        primaryStage.setOnCloseRequest(we -> {
            statusBarTimer.cancel();
            hostRelay.ifPresent(h -> h.disconnect());
        });
        primaryStage.setTitle("QAOPM Spectrum Emulator");
        primaryStage.show();

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                dump(System.out);
            }
        });

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> keyboard.handle(e));
        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, e -> keyboard.handle(e));
        computerLoop.start();
    }

    private MenuBar getMenuBar() {
        final MenuBar menuBar = new MenuBar();

        final Menu fileMenu = new Menu("File");
        registerMenuItem(fileMenu, "Load ...", Optional.of(L), this::loadSnapshot);
        registerMenuItem(fileMenu, "Quit", Optional.of(Q), ae -> System.exit(0));

        final Menu networkMenu = new Menu("Network");
        registerMenuItem(networkMenu, "Get contact info ...", Optional.of(I), ae -> ContactInfoFinder.getContactInfo(LOCAL_PORT));
        connectItem = registerMenuItem(networkMenu, "Connect to guest ...", Optional.of(C), this::connectToGuest);
        disconnectItem = registerMenuItem(networkMenu, "Disconnect from guest", Optional.of(D), this::disconnectFromGuest);
        disconnectItem.setDisable(true);

        final Menu computerMenu = new Menu("Computer");
        registerMenuItem(computerMenu, "Reset", Optional.of(R), this::resetComputer);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(computerMenu);
        menuBar.getMenus().add(networkMenu);
        return menuBar;
    }

    private void connectToGuest(final ActionEvent ae) {
        computerLoop.stop();
        try {
            final Optional<Pair<String, Integer>> result = getConnectionDetails("guest");

            if (result.isPresent()) {
                try {
                    hostRelay = Optional.of(
                            new NetworkPeer<>(
                                    this::receiveGuestInput,
                                    System::currentTimeMillis,
                                    LOCAL_PORT,
                                    new InetSocketAddress(result.get().getKey(), result.get().getValue())
                            )
                    );

                    hostRelay.ifPresent(h -> {
                        connectItem.setDisable(true);
                        disconnectItem.setDisable(false);
                    });
                } catch (SocketException e) {
                    e.printStackTrace();
                    final Alert errorDialog = new Alert(
                            Alert.AlertType.ERROR,
                            "Unable to connect to guest.",
                            ButtonType.OK
                    );

                    errorDialog.showAndWait();
                }
            }
        } finally {
            computerLoop.start();
        }
    }

    private void receiveGuestInput(final GuestState guestState) {
        if (guestState.getPort() == 0x1f) {
            guestKempstonJoystick.setValue(guestState.getValue());
        }
    }

    private void disconnectFromGuest(final ActionEvent ae) {
        hostRelay.ifPresent(r -> {
            r.disconnect();
            hostRelay = Optional.empty();
            connectItem.setDisable(false);
            disconnectItem.setDisable(true);
        });
    }

    private void resetComputer(final ActionEvent actionEvent) {
        computerLoop.stop();
        try {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Reset Computer?");
            alert.setHeaderText("Are you sure you want to reset the computer?");
            alert.getDialogPane().getChildren().stream()
                    .filter(node -> node instanceof Label)
                    .forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
            final Optional<ButtonType> clicked = alert.showAndWait();
            clicked.ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    try {
                        newComputer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } finally {
            computerLoop.start();
        }
    }

    private void loadSnapshot(final ActionEvent ae) {
        computerLoop.stop();
        try {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Snapshot File");
            final File chosen = fileChooser.showOpenDialog(primaryStage);

            if (chosen != null) {
                try {
                    computer.loadSnapshot(chosen);
                } catch (IOException ex) {
                    final Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Loading Error");
                    alert.setHeaderText("Unable to load snapshot");
                    alert.setContentText(
                            String.format("An error occurred while loading the snapshot file:\n%s", ex.getMessage())
                    );
                    alert.getDialogPane().getChildren().stream()
                            .filter(node -> node instanceof Label)
                            .forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
                    alert.showAndWait();
                }
            }
        } finally {
            computerLoop.start();
        }
    }

    private void dump(final PrintStream out) {
        computer.dump(out);
    }

    private class ComputerLoop extends AnimationTimer {
        private boolean flashActive = false;
        private int flashCycleCount = 0x10;
        private final int[] screenBytes = new int[0x1b00];

        @Override
        public void handle(final long now) {
            final Timer.Context timer = displayRefreshTimer.time();
            try {
                final int[] borderLines = ula.getBorderLines();
                hostRelay.ifPresent(h -> {
                    System.arraycopy(memory, 0x4000, screenBytes, 0, 0x1b00);
                    h.sendDataToPartner(new SpectrumState(screenBytes, borderLines, flashActive));
                });
                display.refresh(memory, flashActive);
                border.refresh(borderLines);
                computer.singleCycle();
                flashCycleCount--;
                if (flashCycleCount < 0) {
                    flashCycleCount = 16;
                    flashActive = !flashActive;
                }
            } finally {
                timer.stop();
            }
        }
    }
}

