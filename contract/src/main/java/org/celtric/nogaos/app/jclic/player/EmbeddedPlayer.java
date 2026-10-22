package org.celtric.nogaos.app.jclic.player;

import org.jspecify.annotations.Nullable;

import javax.swing.JComponent;

/**
 * The JClic player as the app drives it. It is implemented by the player bundle the app downloads at first use
 * and finds through {@link java.util.ServiceLoader} as an {@link EmbeddedPlayerFactory}: the bundle is GPL and
 * never part of a NoGaOS release, and these three interfaces are the whole of what the two sides share, so each
 * stays a program of its own under its own licence. The same three files are kept, verbatim, in the bundle's
 * repository.
 */
public interface EmbeddedPlayer {

    /** The component to show: the player draws its skin and activities in it and swaps skins inside it. */
    JComponent component();

    /**
     * Opens a project the app read, at {@code sequence} (a tag, or a position in the main sequence; the first
     * activity when null), under {@code key}, the name the player gives it back in {@link EmbeddedPlayerHost}
     * calls. Parses on the calling thread, which must not be the EDT, and runs {@code opened} or {@code failed}
     * on the EDT once it is known whether the bytes parse.
     */
    void open(byte[] bytes, String key, @Nullable String sequence, Runnable opened, Runnable failed);

    /**
     * Shows {@code skin}, a name the player knows such as {@code @default.xml}, around projects that bring none of
     * their own, now and for every project opened after. On the EDT.
     */
    void useSkin(String skin);

    /** Leaves the open project, freeing its media, so the player waits empty for the next one. On the EDT. */
    void close();

    /** Frees everything for good, when the window closes. On the EDT. */
    void dispose();

    /**
     * The open project's own page as JClic's About shows it: title, authors, organizations, revisions, languages,
     * description, descriptors and the activity on screen, as an HTML document. Null when no project is open, or
     * from a bundle older than this method. On the EDT.
     */
    default @Nullable String projectInfo() {
        return null;
    }
}
