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
import edu.xtec.jclic.skins.AboutWindow;
import edu.xtec.util.Messages;
import edu.xtec.util.ResourceManager;
import edu.xtec.util.StrUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.celtric.nogaos.app.jclic.player.EmbeddedPlayer;
import org.celtric.nogaos.app.jclic.player.EmbeddedPlayerFactory;
import org.celtric.nogaos.app.jclic.player.EmbeddedPlayerHost;

import java.awt.Component;

/** What the app finds through {@code ServiceLoader} once the bundle is loaded. */
public final class UpstreamPlayerFactory implements EmbeddedPlayerFactory {

    private static final System.Logger log = System.getLogger(UpstreamPlayerFactory.class.getName());
    /** A picture named relative to the page, no scheme: {@code SRC="gencat.png"}. */
    private static final Pattern RELATIVE_SRC = Pattern.compile("SRC=\"(?![a-zA-Z][a-zA-Z0-9+.-]*:)([^\"]+)\"",
            Pattern.CASE_INSENSITIVE);

    @Override
    public EmbeddedPlayer create(final Component window, final EmbeddedPlayerHost host) {
        return new UpstreamPlayer(window, host);
    }

    /**
     * Upstream's About tab, assembled the way {@link AboutWindow#buildAboutTab} does but without the dialog: the
     * same documents, captions and version, with every picture made an absolute URL into the bundle so the page
     * renders wherever the app puts it.
     */
    @Override
    public String about(final String language) {
        try {
            final Messages messages = new Messages(Constants.DEFAULT_BUNDLE, language, null, null);
            messages.addBundle(Constants.COMMON_SETTINGS);
            final URL base = AboutWindow.getAboutUrlBase();
            String doc = ResourceManager.getResourceText("about/about.html", false);
            doc = StrUtils.replace(doc, "%LOGO", ResourceManager.getResource("about/logo_small.png").toExternalForm());
            doc = StrUtils.replace(doc, "%APPVERSION",
                    "JClic " + messages.get("VERSION") + " " + messages.get("JCLIC_VERSION"));
            doc = StrUtils.replace(doc, "%CREDITS", ResourceManager.getResourceText("about/credits.html", false));
            doc = StrUtils.replace(doc, "%TRANSLATION", messages.get("html_translation_credits"));
            doc = StrUtils.replace(doc, "%OTHERCREDITS", messages.get("about_otherCreditsCaption") + "<BR>&nbsp;<BR>"
                    + ResourceManager.getResourceText(messages.get("about_otherCreditsDoc"), false));
            doc = StrUtils.replace(doc, "%LICENSE", messages.get("html_gpl_license"));
            final String sponsors = ResourceManager.getResourceText(messages.get("about_sponsorsDoc"), false);
            doc = StrUtils.replace(doc, "%SPONSORS", sponsors == null || sponsors.isEmpty() ? ""
                    : messages.get("about_sponsorsCaption") + "<BR>" + sponsors);
            return absolutePictures(doc, base);
        } catch (final Exception e) {
            log.log(System.Logger.Level.WARNING, "Could not assemble JClic's About page: " + e);
            return null;
        }
    }

    /** Every relative {@code SRC} resolved against {@code base}: the page is shown far from its folder. */
    static String absolutePictures(final String html, final URL base) {
        final Matcher relative = RELATIVE_SRC.matcher(html);
        final StringBuilder out = new StringBuilder(html.length() + 200);
        while (relative.find()) {
            String resolved = relative.group(0);
            try {
                resolved = "SRC=\"" + new URL(base, relative.group(1)).toExternalForm() + "\"";
            } catch (final MalformedURLException ignored) {
                // Left as written: a picture that cannot be resolved is simply not shown.
            }
            relative.appendReplacement(out, Matcher.quoteReplacement(resolved));
        }
        relative.appendTail(out);
        return out.toString();
    }
}
