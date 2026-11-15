# Publishing v0.1.0 yourself

The source is ready for a manual GitHub Release. No release or tag was created
by the source upload.

1. Open https://github.com/statist1c/MGU-FSC-Generator-by-Noahbg/releases/new .
2. Create the tag **`v0.1.0`**, targeting **main**.
3. Use the release title **MGU FSC Generator by Noahbg v0.1.0**.
4. Copy the description from `RELEASE-NOTES-v0.1.0.md`. Keep the original-author
   attribution and the warning that this port is not tested on a real vehicle.
5. Upload the delivered signed **`MGU FSC Generator by Noahbg.apk`**.
   Keep that exact filename; the built-in updater uses it to identify the APK.
6. Optionally upload `APK-SHA256.txt` and the delivered source ZIP. GitHub also
   supplies source archives automatically from the release tag.
7. Publish as a normal release. Leave **Set as a pre-release** unchecked if you
   want the updater to recognize it.

The installed v0.1.0 app will not offer v0.1.0 as an update. For the next release,
increase `versionCode` and `versionName`, use the corresponding tag, and sign with
the same Android signing key. See `README.md` for the build command.

**Never upload the private signing backup, its keystore, or password.** Keep
those locally for signing future releases. The intentionally public FSC Patch
key in the source is a different key and is required by the generator.
