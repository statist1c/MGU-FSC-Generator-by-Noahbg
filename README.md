# MGU FSC Generator by Noahbg

**Version: v0.1.0** · Android 8.0+ · Android Studio project

> **This app is ported from the original project and is not tested yet.**
> It has not been tested on a real vehicle or MGU head unit. Local compilation,
> format comparison and emulator checks do not establish real-world compatibility.

## Original author and credits

The original project is **[mgu-apps by Noahbg](https://github.com/Noahbg/mgu-apps)**.
The original [FSC generator](https://github.com/Noahbg/mgu-apps/tree/main/scripts/fsc_generator),
its FSC formats, application presets, verification logic and intentionally public
FSC Patch signing key are by **Noahbg and the original contributors**. Their
copyright and MIT license are retained. This is an **Android port**, not a claim
of authorship of the original FSC project.

Upstream source inspected: commit `3853c99c99c1e610ae0dee3547a1429b0c097300`,
generator version `1.3.0`. The unmodified Python generator, its tests and key
are retained in `upstream/` for review and format comparison.

The independently implemented Android UI is visually inspired by
[statist1c/MGU-Store-Installer](https://github.com/statist1c/MGU-Store-Installer),
reference commit `4fea4e5f3c7235477be86dddf08c808017c2c96f`. No installer code,
GPL-covered implementation, bundled image, font or logo is copied into this app.
The selected certificate/check icon with **MGU FSC** lettering is an original
vector asset created for this port. Original authors retain all their rights.

## Features

- Application and extended ID7 Live Maps formats, including component protection variants.
- All 34 upstream application/map presets and custom hexadecimal IDs/indexes.
- Full 17-character VIN or last 7; rejects invalid characters, including I, O and Q.
- RSA-2048 / SHA-256 / PKCS#1 v1.5 signing with the unchanged public FSC Patch key.
- Validates the key's exponent and little-endian modulus fingerprint before use.
- Reconstructs every signed header byte and verifies the signature before accepting a file.
- Optional UTC timestamp, generator ID and order ID.
- Android document picker (SAF) export/import; export reads the saved bytes back and verifies them.
- Light, dark and system appearance with rounded cards.
- Automatic GitHub release checks, manual checks, verified APK downloads and Android installer handoff.
- No VIN or FSC uploads. GitHub access is used only for application updates.

Generating an FSC does not install it or enable a vehicle feature. A compatible
FSC Patch setup is required. The intentionally public key establishes compatibility
with that patch, not BMW authorization. No BMW firmware, SDK or donor FSC is included.

## Build in Android Studio

Open this folder as a project. Use **JDK 17**, Android SDK **35**, Gradle **8.9**
(the wrapper is included) and Android Gradle Plugin **8.7.3**.

```powershell
.\gradlew.bat assembleDebug lintDebug
```

The debug APK is `app/build/outputs/apk/debug/app-debug.apk`. The delivered APK
is a separately signed release build named **MGU FSC Generator by Noahbg.apk**.
No architecture-specific native libraries are included.

## Updates and future releases

Update repository: **[statist1c/MGU-FSC-Generator-by-Noahbg](https://github.com/statist1c/MGU-FSC-Generator-by-Noahbg)**.

Publish a normal (non-draft, non-prerelease) GitHub Release with a stable tag such
as `v0.1.1`, and upload exactly **`MGU FSC Generator by Noahbg.apk`** as an asset.
GitHub must expose its `sha256:` asset digest. Increase both `versionCode` and
`versionName`, and sign with the **same Android release signing key** as v0.1.0.
The app scans the most recent 100 releases and selects the highest newer stable
version with a compatible asset. No release has been published by this task.

Checks run approximately daily when Android permits, and when the app opens after
the interval. Android battery/background restrictions can delay them. The setting
can be disabled. Downloads and installation are initiated by the user; Android
may ask to allow installation from this app. Return and select Install again after
granting that permission. There is no silent installation.

Before installation the app checks the APK size, GitHub SHA-256 digest, package
name, signing certificate, increasing version code and matching version name/tag.
Android's package installer performs the final APK signature/integrity checks.

The **Android APK signing key is separate from the public FSC key**. Keep the
delivered signing backup private and retain it for future updates. Never commit
it to GitHub. To sign later, set `MGU_KEYSTORE_PASSWORD` and run:

```powershell
.\gradlew.bat assembleRelease -PreleaseKeystore=C:/private/mgu-fsc-release.jks
```

The key alias is `mgu-fsc`. For GitHub automation, store the key and password in
repository secrets, not in source. See `SIGNING.md` in the private signing backup.

## Validation

See `VALIDATION.md` for the precise local checks and remaining limitations.
To repeat the dependency-free Java format/tamper checks:

```powershell
javac -encoding UTF-8 -d core-classes app/src/main/java/dev/mgu/fsc/Fsc.java tools/CoreTest.java
java -cp core-classes CoreTest . fixtures
python -m pip install -r upstream/requirements.txt
python tools/cross_check.py fixtures
python -m unittest discover -s upstream/tests -v
```

`app/src/androidTest/` contains a platform-only instrumentation runner. Build and
install both debug APKs, then run:

```powershell
.\gradlew.bat assembleDebug assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w dev.mgu.fsc.noahbg.test/dev.mgu.fsc.SmokeTest
```

## License

MIT; see `LICENSE`, `COPYRIGHT.md` and `THIRD_PARTY_NOTICES.md`. Build-tool notices
are in `LICENSES/`. The original author credits and license are also accessible
inside the app under Settings & credits.
