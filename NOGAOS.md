# The JClic player as a Maven artifact

This is a fork of [projectestac/jclic](https://github.com/projectestac/jclic) whose only purpose is to publish the JClic **player** as a Maven artifact, so that the JClic app of [NoGaOS](https://github.com/celtric/noga-os) can embed it as a dependency instead of carrying a copy of its source. Branch `master` is upstream's, untouched; branch `nogaos` (the default) adds the Maven build on top of it and is where upstream is merged into.

## What the artifact is

`org.celtric.nogaos:nogaos-jclic-player` is upstream's `jclic.jar` built by Maven instead of Ant: the `edu.xtec` player core from `src/core` plus the libraries the `core` and `jclicplayer` targets of `build.xml` compile into that jar (JDOM 1.0 without XPath and JClic's `SpecialXMLOutputter`, the Android-derived `org.json`, `dbconn`, image4j, and the Tritonus, JLayer, MP3SPI and JOrbis sound SPIs with their `META-INF/services` entries). The compile-only JMF and QuickTime stubs under `lib/` are compiled against and, as in upstream's jar, left out of the artifact. The source tree is upstream's layout, read in place by `pom.xml`; the version tokens of `commonSettings.properties` are filled from `project.properties` as the Ant build does. A sources jar is published next to the binary.

The authoring tool (`src/author`), the reports server (`src/report`) and the default-package launchers (`JClicPlayer`, `JClicApplet`, `JClicInstaller`) are not in the artifact: it is a library a host embeds through `edu.xtec.jclic.Player`, not a program.

## Deviations from upstream

Everything that is not a verbatim copy of upstream, so a reader knows what to look at when merging:

- `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`, `.github/workflows/build.yml`, this file, and three lines in `.gitignore`.
- `src/core/edu/xtec/jclic/fileSystem/MemoryZip.java`: a `ZipFileSystem` over a zip held in memory, so a host that reads the project itself (NoGaOS reads them out of its virtual drives) can hand the player the bytes; a name missing from the zip is reported missing instead of being looked up on the local disk. A candidate for upstreaming.

The version is upstream's followed by a `-nogaos.<n>` qualifier (`0.3.2.18-nogaos.1`): `<n>` counts the builds published from the same upstream release, and resets when upstream is bumped.

## Building and releasing

```bash
./mvnw verify                         # builds target/nogaos-jclic-player-<version>.jar and its sources jar
```

To publish: set the version in `pom.xml`, commit, tag the commit `v<version>` and push the tag. The workflow builds, checks the tag against the POM version and deploys to this repository's GitHub Packages Maven repository (`https://maven.pkg.github.com/celtric/nogaos-jclic-player`). GitHub Packages needs a token with `read:packages` to download even a public artifact, so a consumer's Maven settings carry one.

To take a new upstream release: `git fetch upstream`, merge `upstream/master` into `nogaos`, set the version to `<upstream>-nogaos.1`, build, tag.

## Licence

JClic is GPL-2.0-or-later (see `LICENSE`) and so is this fork as a whole, `MemoryZip` included. The bundled libraries keep their own licences, recorded in their own folders: `extensions/xml/jdom-1.0/LICENSE.txt` (JDOM), `extensions/json/README` (Android's `org.json`, Apache-2.0 per each source header), `extensions/img/image4j/license.txt` (LGPL), `extensions/soundspi/javalayer/LICENSE.txt` and `extensions/soundspi/MpegAudioSPI/LICENSE.txt` (LGPL), `extensions/soundspi/jorbis/COPYING.LIB` (LGPL), `extensions/soundspi/tritonus/README` (LGPL).
