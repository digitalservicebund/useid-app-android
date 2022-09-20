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

### android bumpVersion

```sh
[bundle exec] fastlane android bumpVersion
```

Bump version

### android release

```sh
[bundle exec] fastlane android release
```

Create new Playstore build and submit to Google Play Store

### android previewRelease

```sh
[bundle exec] fastlane android previewRelease
```

Create new Playstore preview build and submit to Google Play Store

### android productionRelease

```sh
[bundle exec] fastlane android productionRelease
```

Create new Playstore production build and submit to Google Play Store

### android buildAndSubmit

```sh
[bundle exec] fastlane android buildAndSubmit
```

Create new Playstore build and submit to Google Play Store

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
