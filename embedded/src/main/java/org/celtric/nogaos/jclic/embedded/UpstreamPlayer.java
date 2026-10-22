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

import edu.xtec.jclic.Constants;
import edu.xtec.jclic.Player;
import edu.xtec.jclic.bags.ActivityBagElement;
import edu.xtec.jclic.fileSystem.FileSystem;
import edu.xtec.jclic.fileSystem.MemoryZip;
import edu.xtec.jclic.project.JClicProject;
import edu.xtec.jclic.skins.AboutWindow;
import edu.xtec.jclic.skins.Skin;
import edu.xtec.util.Html;
import edu.xtec.util.LFUtil;
import edu.xtec.util.Messages;
import edu.xtec.util.Options;
import org.celtric.nogaos.app.jclic.player.EmbeddedPlayer;
import org.celtric.nogaos.app.jclic.player.EmbeddedPlayerHost;
import org.jdom.Document;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The upstream JClic player living inside a NoGaOS window instead of owning a top-level frame: projects come in
 * as bytes the app read, jumps into other projects are resolved by the app, and everything upstream would do on
 * the host (open a browser, run a program, close the frame or the JVM) is turned into something the app decides.
 */
final class UpstreamPlayer extends Player implements EmbeddedPlayer {

    private static final System.Logger log = System.getLogger(UpstreamPlayer.class.getName());

    private final EmbeddedPlayerHost host;
    private final JPanel container = new JPanel(new BorderLayout());
    /** Bumped by every opening and by leaving, so a parse that finishes late shows nothing. */
    private final AtomicInteger openings = new AtomicInteger();
    private String current;
    private Skin chosenSkin;

    UpstreamPlayer(final Component window, final EmbeddedPlayerHost host) {
        super(options(window, host));
        this.host = host;
        this.chosenSkin = defaultSkin;
        final SkinHost skinHost = new SkinHost();
        container.add(skinHost, BorderLayout.CENTER);
        addTo(skinHost, BorderLayout.CENTER);
    }

    /**
     * Without a language upstream asks for one in a dialog, and the look-and-feel key (set on macOS) would swap
     * the whole desktop's look; with no JMF or QuickTime at runtime, the media-system warning would offer a
     * download page for every video.
     */
    private static Options options(final Component window, final EmbeddedPlayerHost host) {
        final Options options = new Options(window);
        options.remove(LFUtil.LOOK_AND_FEEL);
        options.put(Messages.LANGUAGE, host.language());
        options.putBoolean(Constants.NO_MEDIASYSTEM_WARN, true);
        options.put(Constants.SKIN, host.skin());
        return options;
    }

    @Override
    public JComponent component() {
        return container;
    }

    @Override
    public void open(final byte[] bytes, final String key, final String sequence, final Runnable opened,
                     final Runnable failed) {
        show(bytes, key, sequence, true, opened, failed);
    }

    @Override
    public void useSkin(final String skin) {
        final Skin loaded;
        try {
            loaded = Skin.getSkin(skin, null, this);
        } catch (final Exception e) {
            log.log(System.Logger.Level.WARNING, "Could not load the JClic skin " + skin + ": " + e);
            return;
        }
        chosenSkin = loaded;
        defaultSkin = loaded;
        final boolean projectSkin = project != null && project.skin != null;
        final boolean activitySkin = actPanel != null && actPanel.skin != null;
        if (!projectSkin && !activitySkin) {
            setSkin(loaded);
        }
    }

    @Override
    public void close() {
        openings.incrementAndGet();
        removeActivity();
        stopMedia();
        getHistory().clearHistory();
        current = null;
        setProject(null);
    }

    /** The current project's page, assembled the way {@link edu.xtec.jclic.skins.AboutWindow#buildStandardTab} does. */
    @Override
    public String projectInfo() {
        if (project == null) {
            return null;
        }
        final StringBuilder content = new StringBuilder(4096);
        content.append(messages.get("about_window_html_header"));
        content.append("<br><b>").append(messages.get("about_window_projectInfo")).append("</b><hr>");
        content.append(project.settings.toHtmlString(messages));
        if (actPanel != null) {
            content.append(Html.BR).append(actPanel.getActivity().toHtmlString(this));
        }
        final String table = Html.table(content.substring(0), "100%", 0, 5, -1,
                messages.get("about_window_html_style"), true);
        return UpstreamPlayerFactory.absolutePictures(Html.getHtmlDoc(table, messages.get("about_window_html_bgcolor")),
                AboutWindow.getAboutUrlBase());
    }

    @Override
    public void dispose() {
        openings.incrementAndGet();
        removeActivity();
        end();
        final Skin shown = getSkin();
        if (shown != null) {
            // Upstream caches every skin it builds in a static set and never lets go of them, nor of their player.
            shown.emptySkinStack();
        }
    }

    @Override
    protected void setProject(final JClicProject newProject) {
        super.setProject(newProject);
        if (chosenSkin != null) {
            // Upstream keeps the last project's own skin as the default for every project after it.
            defaultSkin = chosenSkin;
        }
    }

    @Override
    public void load(final String fullPath, final String sequence, final String activity,
                     final ActivityBagElement element) {
        final String here = current;
        if (fullPath == null || here == null) {
            super.load(null, sequence, activity, element);
            return;
        }
        final int mine = openings.get();
        host.other(here, fullPath, new EmbeddedPlayerHost.OtherProject() {
            @Override
            public void same() {
                if (mine == openings.get()) {
                    UpstreamPlayer.super.load(null, sequence, activity, element);
                }
            }

            @Override
            public void found(final byte[] bytes, final String key) {
                if (mine == openings.get()) {
                    show(bytes, key, sequence, false, () -> { }, () -> host.cannotOpen(fullPath));
                }
            }

            @Override
            public void missing() {
            }
        });
    }

    @Override
    public void activityFinished(final boolean completedOk) {
        super.activityFinished(completedOk);
        final String here = current;
        if (completedOk && here != null && project != null) {
            final int index = project.activitySequence.getCurrentActNum();
            if (index >= 0) {
                host.finished(here, index);
            }
        }
    }

    @Override
    public void setWindowTitle(final String docTitle) {
        if (host != null) {
            // Null while upstream's constructor runs, which already sets a title from there.
            host.showTitle(docTitle == null ? "" : docTitle);
        }
    }

    @Override
    public void exit(final String url) {
        if (url != null) {
            log.log(System.Logger.Level.INFO, "A JClic project asked to quit to " + url + "; leaving instead");
        }
        SwingUtilities.invokeLater(host::exit);
    }

    @Override
    public void displayUrl(final String url, final boolean inFrame) {
        log.log(System.Logger.Level.INFO, "A JClic project asked to open " + url + " in a browser; ignored");
    }

    @Override
    public void displayUrl(final URL url, final boolean inFrame) {
        log.log(System.Logger.Level.INFO, "A JClic project asked to open " + url + " in a browser; ignored");
    }

    @Override
    protected void runCmd(final String cmd) {
        log.log(System.Logger.Level.INFO, "A JClic project asked to run the host program " + cmd + "; ignored");
    }

    /** Upstream makes its own language the JVM's default locale, which every other window would then follow. */
    @Override
    protected Messages setMessages() {
        final Locale before = Locale.getDefault();
        try {
            return super.setMessages();
        } finally {
            Locale.setDefault(before);
        }
    }

    /** Parses on the calling thread and shows on the EDT, unless something newer was opened meanwhile. */
    private void show(final byte[] bytes, final String key, final String sequence, final boolean fresh,
                      final Runnable shown, final Runnable failed) {
        final int mine = openings.incrementAndGet();
        final long start = System.nanoTime();
        final JClicProject parsed = parse(bytes, key);
        if (parsed != null) {
            log.log(System.Logger.Level.INFO, "Parsed " + key + " in " + (System.nanoTime() - start) / 1_000_000
                    + " ms");
        }
        SwingUtilities.invokeLater(() -> {
            if (mine != openings.get()) {
                return;
            }
            if (parsed == null) {
                failed.run();
                return;
            }
            if (fresh) {
                getHistory().clearHistory();
            }
            current = key;
            setProject(parsed);
            super.load(null, sequence == null ? "0" : sequence, null, null);
            shown.run();
        });
    }

    /**
     * A zip is what the library serves; a bare project file (unpacked by hand) is read without its media, which
     * upstream then reports missing box by box.
     */
    private JClicProject parse(final byte[] bytes, final String key) {
        final boolean zip = bytes.length >= 2 && bytes[0] == 'P' && bytes[1] == 'K';
        try {
            final MemoryZip files = new MemoryZip(key, zip ? bytes : new byte[0], this);
            final Document document;
            if (zip) {
                final String[] projects = files.getEntries(".jclic");
                if (projects == null || projects.length == 0) {
                    log.log(System.Logger.Level.WARNING, key + " holds no .jclic project");
                    return null;
                }
                document = files.getXMLDocument(projects[0]);
            } else {
                document = FileSystem.getXMLDocument(new ByteArrayInputStream(bytes));
            }
            return JClicProject.getJClicProject(document.getRootElement(), this, files, key);
        } catch (final Exception e) {
            log.log(System.Logger.Level.WARNING, "Could not read " + key + " as a JClic project: " + e);
            return null;
        }
    }
}
