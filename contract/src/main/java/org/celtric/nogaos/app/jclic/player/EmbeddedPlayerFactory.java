package org.celtric.nogaos.app.jclic.player;

import java.awt.Component;

/** Makes {@link EmbeddedPlayer}s; the player bundle names its implementation in {@code META-INF/services}. */
public interface EmbeddedPlayerFactory {

    /** A player for one window, on the EDT; {@code window} parents the player's own dialogs. */
    EmbeddedPlayer create(Component window, EmbeddedPlayerHost host);
}
