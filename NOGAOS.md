# The JClic player for NoGaOS

This is a fork of [projectestac/jclic](https://github.com/projectestac/jclic) whose only purpose is to build the JClic **player** for the JClic app of [NoGaOS](https://github.com/celtric/noga-os). Branch `master` is upstream's, untouched; branch `nogaos` (the default) adds the Maven build on top of it and is where upstream is merged into.

NoGaOS is MIT and ships no GPL code: its JClic app downloads the **player bundle** released here at first use, keeps it in the user's cache and loads it in a class loader of its own, so the two stay separate programs, each under its own licence, and the combination happens on the user's machine. The app pins the bundle's version and SHA-256 and refuses any other download. Three Maven modules:

- `player/` — upstream's `jclic.jar` built by Maven, `org.celtric.nogaos:nogaos-jclic-player` (GPL).
- `contract/` — the three interfaces the app and the bundle share, copied verbatim from the app and MIT like it (see its README); the bundle compiles against them and never ships them.
- `embedded/` — the glue that lets the player live inside a NoGaOS window (`UpstreamPlayer`, found by the app through `ServiceLoader`), shaded with the player into the one jar `nogaos-jclic-embedded-<version>.jar` that every GitHub release carries, next to its SHA-256, the sources jars and the licence (GPL as a whole).

## What the player artifact is

`org.celtric.nogaos:nogaos-jclic-player` is upstream's `jclic.jar` built by Maven instead of Ant: the `edu.xtec` player core from `src/core` plus the libraries the `core` and `jclicplayer` targets of `build.xml` compile into that jar (JDOM 1.0 without XPath and JClic's `SpecialXMLOutputter`, the Android-derived `org.json`, `dbconn`, image4j, and the Tritonus, JLayer, MP3SPI and JOrbis sound SPIs with their `META-INF/services` entries). The compile-only JMF and QuickTime stubs under `lib/` are compiled against and, as in upstream's jar, left out of the artifact. The source tree is upstream's layout, read in place by `player/pom.xml`; the version tokens of `commonSettings.properties` are filled from `project.properties` as the Ant build does. A sources jar is published next to the binary.

The authoring tool (`src/author`), the reports server (`src/report`) and the default-package launchers (`JClicPlayer`, `JClicApplet`, `JClicInstaller`) are not in the artifact: it is a library a host embeds through `edu.xtec.jclic.Player`, not a program.

## Deviations from upstream

Everything that is not a verbatim copy of upstream, so a reader knows what to look at when merging:

- `pom.xml`, `player/`, `contract/`, `embedded/`, `mvnw`, `mvnw.cmd`, `.mvn/`, `.github/workflows/build.yml`, this file, and three lines in `.gitignore`. Nothing under `src/`, `extensions/` or `lib/` is touched except the file below.
- `src/core/edu/xtec/jclic/fileSystem/MemoryZip.java`: a `ZipFileSystem` over a zip held in memory, so a host that reads the project itself (NoGaOS reads them out of its virtual drives) can hand the player the bytes; a name missing from the zip is reported missing instead of being looked up on the local disk. A candidate for upstreaming.

The version is upstream's followed by a `-nogaos.<n>` qualifier (`0.3.2.18-nogaos.1`): `<n>` counts the builds published from the same upstream release, and resets when upstream is bumped.

## Building and releasing

```bash
./mvnw verify                         # builds player/target/nogaos-jclic-player-<version>.jar and
                                      # embedded/target/nogaos-jclic-embedded-<version>.jar, with sources jars
```

To publish: set the version in the four POMs, commit, tag the commit `v<version>` and push the tag. The workflow builds, checks the tag against the POM version, deploys the artifacts to this repository's GitHub Packages Maven repository (`https://maven.pkg.github.com/celtric/nogaos-jclic-player`, which needs a token with `read:packages` even for a public artifact) and creates the GitHub release with the bundle, whose SHA-256 the notes state for the NoGaOS app to pin.

To take a new upstream release: `git fetch upstream`, merge `upstream/master` into `nogaos`, set the version to `<upstream>-nogaos.1`, build, tag.

## Licence

JClic is GPL-2.0-or-later (see `LICENSE`) and so are the player and the bundle, `MemoryZip` and `embedded/` included; the licence rides inside both jars as `META-INF/LICENSE`. `contract/` is MIT. The bundled libraries keep their own licences, recorded in their own folders: `extensions/xml/jdom-1.0/LICENSE.txt` (JDOM), `extensions/json/README` (Android's `org.json`, Apache-2.0 per each source header), `extensions/img/image4j/license.txt` (LGPL), `extensions/soundspi/javalayer/LICENSE.txt` and `extensions/soundspi/MpegAudioSPI/LICENSE.txt` (LGPL), `extensions/soundspi/jorbis/COPYING.LIB` (LGPL), `extensions/soundspi/tritonus/README` (LGPL).
