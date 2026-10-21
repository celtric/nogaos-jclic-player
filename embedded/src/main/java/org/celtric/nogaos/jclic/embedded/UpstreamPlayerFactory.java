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

import org.celtric.nogaos.app.jclic.player.EmbeddedPlayer;
import org.celtric.nogaos.app.jclic.player.EmbeddedPlayerFactory;
import org.celtric.nogaos.app.jclic.player.EmbeddedPlayerHost;

import java.awt.Component;

/** What the app finds through {@code ServiceLoader} once the bundle is loaded. */
public final class UpstreamPlayerFactory implements EmbeddedPlayerFactory {

    @Override
    public EmbeddedPlayer create(final Component window, final EmbeddedPlayerHost host) {
        return new UpstreamPlayer(window, host);
    }
}
