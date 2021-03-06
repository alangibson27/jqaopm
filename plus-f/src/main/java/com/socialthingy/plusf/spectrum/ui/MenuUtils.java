package com.socialthingy.plusf.spectrum.ui;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.Optional;

public class MenuUtils {
    public static JMenuItem menuItemFor(final String name, final ActionListener action, final Optional<Integer> accelerator) {
        final JMenuItem loadItem = new JMenuItem(name);
        loadItem.setName(name);
        loadItem.addActionListener(action);
        accelerator.ifPresent(acc ->
                loadItem.setAccelerator(KeyStroke.getKeyStroke(acc, InputEvent.ALT_MASK))
        );
        return loadItem;
    }

    public static JMenuItem joystickItem(final ButtonGroup group, final String type, final boolean selected) {
        final JMenuItem item = new JRadioButtonMenuItem(type, selected);
        item.setName("Joystick" + type);
        group.add(item);
        return item;
    }
}
