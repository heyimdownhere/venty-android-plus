# Release signing

Release APKs (phone and watch) are signed with one release key so every build, local or from
GitHub Actions, installs over the last one and the in-app updater works. The watch app must
share the phone app's key for the Wear Data Layer to connect them.

Debug builds keep using the normal Android debug key.

## Where the key lives

- Keystore: `~/.android-keys/venty-release.jks` (PKCS12, alias `venty`, valid 100 years)
- Passwords: `~/.android-keys/venty-release.properties`

**Back both files up somewhere safe** (password manager, encrypted drive). If the key is lost,
no future build can install over the current app; you'd have to uninstall it and lose its data.

## Local builds

Copy the properties file to the repo root, where it is gitignored:

```sh
cp ~/.android-keys/venty-release.properties keystore.properties
./gradlew assembleRelease
```

Without `keystore.properties`, release builds fall back to the debug key.

## GitHub Actions

The Build workflow reads these repository secrets:

| Secret | Value |
|--------|-------|
| `VENTY_KEYSTORE_BASE64` | `base64 -w0 venty-release.jks` |
| `VENTY_KEYSTORE_PASSWORD` | store password |
| `VENTY_KEY_ALIAS` | `venty` |
| `VENTY_KEY_PASSWORD` | key password (same as the store password for PKCS12) |

To set them again with the GitHub CLI:

```sh
base64 -w0 ~/.android-keys/venty-release.jks | gh secret set VENTY_KEYSTORE_BASE64
gh secret set VENTY_KEYSTORE_PASSWORD   # paste the password
gh secret set VENTY_KEY_PASSWORD        # same password
printf venty | gh secret set VENTY_KEY_ALIAS
```

## Switching an installed copy to the new key

A copy installed from an older build (signed with a debug key) can't be updated with a
release-key build. Uninstall it once, then install the new APK.
