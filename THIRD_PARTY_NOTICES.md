# Licenses and included components

| Component | Included where | Terms / attribution |
| --- | --- | --- |
| Original FSC generator 1.3.0, tests and public patch key | `upstream/`; key and native logic adaptation in APK | MIT, copyright (c) 2026 Noahbg. `LICENSE`, `upstream/LICENSE`. |
| Gradle 8.9 wrapper scripts and wrapper JAR | Source/build only | Apache License 2.0; original Gradle authors and contributors. `LICENSES/Gradle-8.9.txt`, `LICENSES/Gradle-8.9-NOTICE.txt`. |
| Android framework, Java APIs and system fonts | Provided by the device; not bundled | Platform components; no extra runtime library is redistributed in this APK. |

There are **no third-party Maven runtime dependencies**, Python runtime,
cryptography package, Bouncy Castle, installer scripts, fonts or bitmap assets
bundled in this APK. The icon is an original vector with geometric lettering.

The optional Python comparison tests use the upstream `cryptography` development
dependency, installed separately by the developer. That dependency and its native
libraries are not included in the source ZIP or APK. Android SDK, JDK, Gradle
distribution and Android Gradle Plugin are downloaded/provided separately as
build tools; only the wrapper is redistributed here.

The MIT copyright and full license are bundled as `assets/LICENSE` and displayed
in the app. Original author and visual-reference credits are present in the README
and Settings & credits. This audit covers the files distributed with this port,
not unrelated components in either reference repository.
