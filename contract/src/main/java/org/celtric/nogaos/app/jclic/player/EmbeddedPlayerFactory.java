package org.celtric.nogaos.app.jclic.player;

import org.jspecify.annotations.Nullable;

import java.awt.Component;

/** Makes {@link EmbeddedPlayer}s; the player bundle names its implementation in {@code META-INF/services}. */
public interface EmbeddedPlayerFactory {

    /** A player for one window, on the EDT; {@code window} parents the player's own dialogs. */
    EmbeddedPlayer create(Component window, EmbeddedPlayerHost host);

    /**
     * JClic's own About page as the player shows it, in {@code language} (a two-letter code): logo, version,
     * authors, translators and licence, as an HTML document whose pictures are absolute URLs. Null from a bundle
     * older than this method. Reads resources, so not for the EDT.
     */
    default @Nullable String about(final String language) {
        return null;
    }
}
