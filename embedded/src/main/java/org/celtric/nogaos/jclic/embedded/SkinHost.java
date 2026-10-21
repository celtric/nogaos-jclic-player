/*
 * NoGaOS JClic player bundle
 *
 * Copyright (C) 2026 Ricard Osorio
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details (see the LICENSE file).
 */

package org.celtric.nogaos.jclic.embedded;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

/**
 * Every activity may bring its own skin, and upstream swaps skins by removing the old one from its parent and
 * adding the new one to the content pane of the first {@link RootPaneContainer} above it: being that container
 * keeps the swap in here, where it would otherwise land in the window's own content pane.
 */
final class SkinHost extends JPanel implements RootPaneContainer {

    SkinHost() {
        super(new BorderLayout());
    }

    /** Swing asks a root pane container for its root pane, so this one names the window's. */
    @Override
    public JRootPane getRootPane() {
        final Container parent = getParent();
        return parent == null ? null : SwingUtilities.getRootPane(parent);
    }

    @Override
    public Container getContentPane() {
        return this;
    }

    @Override
    public void setContentPane(final Container contentPane) {
        throw new UnsupportedOperationException("The skin host is its own content pane");
    }

    @Override
    public JLayeredPane getLayeredPane() {
        return window().getLayeredPane();
    }

    @Override
    public void setLayeredPane(final JLayeredPane layeredPane) {
        throw new UnsupportedOperationException("The skin host has no layered pane of its own");
    }

    @Override
    public Component getGlassPane() {
        return window().getGlassPane();
    }

    @Override
    public void setGlassPane(final Component glassPane) {
        throw new UnsupportedOperationException("The skin host has no glass pane of its own");
    }

    private JRootPane window() {
        final JRootPane rootPane = getRootPane();
        if (rootPane == null) {
            throw new IllegalStateException("The skin host is not in a window yet");
        }
        return rootPane;
    }
}
