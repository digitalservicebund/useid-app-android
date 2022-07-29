fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android test

```sh
[bundle exec] fastlane android test
```

Runs all the tests

### android lint

```sh
[bundle exec] fastlane android lint
```

Run Android linter and ktlint

### android updateVersion

```sh
[bundle exec] fastlane android updateVersion
```

Update version name and bump version code

### android buildAndSubmitPreview

```sh
[bundle exec] fastlane android buildAndSubmitPreview
```

Create new Playstore build and submit to Google Play Store

### android buildAndSubmitRelease

```sh
[bundle exec] fastlane android buildAndSubmitRelease
```

Create new Playstore build and submit to Google Play Store

### android buildAndSubmit

```sh
[bundle exec] fastlane android buildAndSubmit
```

Create new Playstore build and submit to Google Play Store

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
