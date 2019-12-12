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
  * [ ] **Build** &rarr; **Generate Signed APK**
  * [ ] click **Reveal in Finder**
  * [ ] cut new releasel on GitHub <https://github.com/blabbertabber/blabbertabber/releases> and upload APK
1. Publish to Google Play
  * [ ] browse to [Google Play Developer Console](https://play.google.com/apps/publish/)
  * [ ] click **BlabberTabber**
  * [ ] click **Release management &rarr; App releases &rarr; Manage production &rarr; Create Release**
  * [ ] click **APK**
  * [ ] click **Upload new APK to Production**
  * [ ] drag-and-drop `app-release.apk` from Finder
  * [ ] update description (based on commit messages)
  * [ ] click **Publish now to Production**
  * [ ] update screenshots if necessary: **Store presence &rarr; Store listing**
1. Publish to GitHub
  * [ ] browse to [Draft a new GitHub releases](https://github.com/blabbertabber/blabbertabber/releases/new)
  * [ ] select the latest tag version
  * [ ] set the **Release title** the same as the Tag version
  * [ ] set the **Describe this release** the same as was done in Google Play
  * [ ] drag-and-drop the APK into the **Attach binaries** section
1. Close this Issue
