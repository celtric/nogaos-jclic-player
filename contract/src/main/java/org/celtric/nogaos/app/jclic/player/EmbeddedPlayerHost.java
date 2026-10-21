package org.celtric.nogaos.app.jclic.player;

/** What an {@link EmbeddedPlayer} asks of the window it lives in. Every method is called on the EDT. */
public interface EmbeddedPlayerHost {

    /** The ISO 639 code of the language the player's own texts come in. */
    String language();

    /** The skin to start with, as {@link EmbeddedPlayer#useSkin} takes it. */
    String skin();

    /** The open project's title, for the window; blank when it has none. */
    void showTitle(String title);

    /** Each time an activity is solved: the key the project was opened under and the activity's place. */
    void finished(String key, int activityIndex);

    /** The project asked to quit. */
    void exit();

    /**
     * The project opened under {@code key} leads into {@code reference}, as written in it (a path relative to the
     * project, or a key the player was given before). The host finds that project and answers through
     * {@code answer}; the host tells the kid itself when there is no such project.
     */
    void other(String key, String reference, OtherProject answer);

    /** The project {@code reference} leads into was found but could not be read as a project; told on the EDT. */
    void cannotOpen(String reference);

    /** The host's answer to {@link #other}. */
    interface OtherProject {

        /** The reference names the open project itself: the player moves within it. On the EDT. */
        void same();

        /**
         * The other project's bytes and the key to open it under. May be called from any thread; the player parses
         * on it and shows the project on the EDT.
         */
        void found(byte[] bytes, String key);

        /** No such project here; the kid has been told. On the EDT. */
        void missing();
    }
}
