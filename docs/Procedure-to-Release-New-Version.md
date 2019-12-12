Do **not** click the checkboxes on this page.  This is the template you will be copying.
-  Click on Edit and copy the numbered steps below.
-  Click on the Issues tab.  Click on New Issue.
-  Title the new issue Bump to 1.0.*x*.  Paste the numbered steps into the Issue.

============ Cut and paste everything below this line ============


## Procedure to Release New Version of BlabberTabber
1. Make sure code is formatted and bug-free
  * [ ] reformat src/
  * [ ] run tests
1. Update screenshots
  * [ ] take screenshots from an emulated Pixel XL 25
  * [ ] shrink a version 16% (Apple Preview can do it)
  * [ ] add 16%-version to this issue, grab URI
  * [ ] update README.md with URI
1. Bump versions:
  * [ ] in `build.gradle`, bump `versionCode`, `versionName`
  * [ ] `git add -p`
  * [ ] `git duet-commit -v`, commit message **Bump versionName to ...**
  * [ ] `git push`
  * [ ] tag with the new versionName: `git tag` 1.0.x
  * [ ] `git push --tags`
1. Build Signed APK
  * [ ] **Build** → **Generate Signed Bundle or APK** → **APK**
        - select **release** build variant
        - check both [V1 and V2](https://stackoverflow.com/questions/42648499/difference-between-signature-versions-v1jar-signature-and-v2full-apk-signat) signature versions
  * [ ] click **locate**
1. Publish to Google Play
  * [ ] browse to [Google Play Developer Console](https://play.google.com/apps/publish/)
  * [ ] click **BlabberTabber**
  * [ ] click **Release management → App releases → Production track → Manage → Create Release**
  * [ ] click **APK**
  * [ ] click **Upload new APK to Production**
  * [ ] drag-and-drop `app-release.apk` from Finder
  * [ ] update description (based on commit messages)
        - if no new features: **No new functionality, but bump the Android-related dependencies.**
  * [ ] click **Publish now to Production**
  * [ ] update screenshots if necessary: **Store presence → Store listing**
1. Publish to GitHub
  * [ ] browse to [Draft a new GitHub releases](https://github.com/blabbertabber/blabbertabber/releases/new)
  * [ ] select the latest tag version
  * [ ] set the **Release title** the same as the Tag version
  * [ ] set the **Describe this release** the same as was done in Google Play
  * [ ] drag-and-drop the APK into the **Attach binaries** section
1. Close this Issue
