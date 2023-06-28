# About the project

Securely identify yourself with our app and your ID card anytime, anywhere without video calls and waiting times.

With this Project we aim to develop the state digital identity into a popular and widespread solution - recognised by service providers and citizens.

Digital identification is one of the basic functions necessary for the successful implementation of digital citizen services. For this reason, it must be accessible and usable for all citizens. In this way, we will contribute to increasing the usage rates of digital citizen services.

In our MVP we develop an eID-interface based on the eID-technology to enable the digital identification with the German ID card. From there it will be evolving based on our users’ needs.

## UseId project
> **Important:  This project has been discontinued**
​
This repository is part of the UseId project, that provided the BundesIdent mobile app.  You can find other repositories related to this project in the following list:
​
- Architecture
	- [Architecture](https://github.com/digitalservicebund/useid-architecture/tree/main): Documentation and overview of the UseId architecture
- Backend
	- [Backend](https://github.com/digitalservicebund/useid-backend-service): Kotlin service that acts as the backend for the mobile apps and eID-Service integration for eServices.
- eService
	- [eService-example](https://github.com/digitalservicebund/useid-eservice-example): An example application for an eService integrating with the UseId identity solution.
	- [eService-SDK](https://github.com/digitalservicebund/useid-eservice-sdk): Javascript SDK to easily integrate with the UseId identity solution.
- eID client (mobile app)
	- [iOS client for BundesIdent](https://github.com/digitalservicebund/useid-app-ios)
	- [Android client for BundesIdent](https://github.com/digitalservicebund/useid-app-android)
	- [AusweisApp2 Wrapper iOS](https://github.com/digitalservicebund/AusweisApp2Wrapper-iOS-SPM): Forked repository of the Governikus AusweisApp2 Wrapper for iOS

## Contributing

Everyone is welcome to contribute the development of this project. You can contribute by opening pull request,
providing documentation or answering questions or giving feedback. Please always follow the guidelines and our
[Code of Conduct](CODE_OF_CONDUCT.md).

### Contributing code

Open a pull request with your changes and it will be reviewed by someone from the team. When you submit a pull request,
you declare that you have the right to license your contribution to the DigitalService and the community.
By submitting the patch, you agree that your contributions are licensed under [MIT](LICENSE).

Please make sure that your changes have been tested before submitting a pull request.

## Updating Dependencies

Run `./gradlew versionCatalogUpdates` to update the dependencies in [gradle/libs.versions.toml](gradle/libs.versions.toml).
After that, run the tests and linter (see below) and commit the changes.

## Testing and Linting

* Lint: `bundle exec fastlane lint` or `./gradlew lint lintKotlin`
* Unit Tests: `./gradlew test`
* Lint + Unit Tests: `./gradlew check`
* Device Tests: `./gradlew pixel6api31DeviceDebugAndroidTest`
* All Tests (get a coffee, this takes time): `bundle exec fastlane lint`

See [app/build.gradle](app/build.gradle) for a list of gradle managed devices for device tests.

## License

 This project is licensed under [MIT](LICENSE).

## Localization

This project uses [Weblate](https://hosted.weblate.org/engage/useid/) for localization.
