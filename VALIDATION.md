# v0.1.0 validation record

Date: 2026-11-15. This is a port and **is not tested yet on a real vehicle or
MGU head unit**. The following checks are local software checks only.

## Completed

- Built Android debug and signed release APKs with JDK 17.0.21, Gradle 8.9,
  Android Gradle Plugin 8.7.3 and Android SDK 35.
- Android lint completed successfully for debug and release, with no errors.
  Remaining warnings concern localization, backup configuration and platform deprecations.
- All 14 unmodified upstream Python tests passed.
- 10,986 native Java checks passed, including all 34 presets, VIN/ID/identifier/
  timestamp validation, replaced-key rejection, and flipping every single byte
  of every generated preset to ensure verification rejects the change.
- All 34 native outputs matched unmodified upstream Python output **byte-for-byte**,
  including deterministic PKCS#1 v1.5 signatures, using the actual bundled patch key.
- Android API 35 emulator: all 34 presets signed and verified using device crypto.
  UI generation, switching pages and saved-instance-state content checks passed.
- Android document picker: generated a 316-byte application FSC, saved it through
  ACTION_CREATE_DOCUMENT, read it back, reopened it using ACTION_OPEN_DOCUMENT,
  and verified its signature. The exported bytes also verified in upstream Python.
- Updater instrumentation: 22 checks passed. Covered stable-version selection,
  equality/downgrade handling, prerelease rejection, missing digest/wrong asset/
  wrong host/wrong repository rejection, semantic version parsing, HTTPS host
  restrictions, acceptance of a same-signer newer APK fixture, and rejection of
  mismatched release version, checksum, signing certificate and current version.
- Background update job registration passed on API 35.
- Actual GitHub release metadata was checked on the emulator; no newer compatible
  APK was available in the configured repository.
- The final release APK installed and launched on API 35. Light and dark layouts
  were visually inspected; screenshots are in `docs/`.
- APK metadata: application label `MGU FSC Generator by Noahbg`, package
  `dev.mgu.fsc.noahbg`, versionName `0.1.0`, versionCode `1`, minimum API `26`,
  target API `35`. No native architecture restrictions or third-party runtime dependencies.
- `apksigner verify` passed: APK Signature Scheme v2, RSA-3072 Android release key.
  Signing certificate SHA-256:
  `d1129a23368a007bd03c7ef7b26c29b7ffd4ac58ad83f33d861b33c02b1117ad`.

## Not established by these checks

- No real vehicle, head unit, FSC Patch installation, feature activation or physical
  Android phone was used. No FSC was installed in a vehicle.
- Older Android versions and manufacturer-specific document providers are untested.
- No future release exists to exercise a complete live GitHub download-and-update
  installation. Download/installer code is implemented; release selection and APK
  validation were tested with local fixtures. No release was published here.
- Android decides when background jobs run; automatic checks are not a guaranteed
  daily schedule, and are re-registered when the app is opened after a reboot.

## Repeating updater instrumentation

`UpdateTest` uses separately generated fixtures, not production assets. Build a
debug APK of this project with versionName `0.1.1`, versionCode `2` and the same
debug signer as the installed `0.1.0` debug app. Save it as `candidate.apk`.
Save the current debug APK as `current.apk`; re-sign the candidate with a different
test key as `wrong-cert.apk`. Keep those three files outside the source project.

```powershell
.\gradlew.bat assembleDebugAndroidTest -PinstrumentationRunner=dev.mgu.fsc.UpdateTest -PupdateTestAssets=C:/private/test-fixtures
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w dev.mgu.fsc.noahbg.test/dev.mgu.fsc.UpdateTest
```

The installed target must be the debug app for this fixture setup, not the delivered
release APK; Android intentionally refuses instrumentation or updates from a different signer.
