# Meso Enrollment Android App [![CircleCI](https://circleci.com/gh/Watsi/meso-enrollment/tree/master.svg?style=svg&circle-token=2ec1ea50f676e519d26ee6d04422bac57c79ecac)](https://circleci.com/gh/Watsi/meso-enrollment/tree/master)

### Architecture

This app is structured using the [clean architecture design principles](http://five.agency/android-architecture-part-1-every-new-beginning-is-hard/) and is separated into three modules with cascading dependencies:
- `domain` is a pure Java library that contains entities, repositories and use-cases. It represents the core business logic.
- `device` is an Android library that contains components used by the application but are not tied to any specific UI implementations. These components can depend on the Android SDK and domain module and include implementation-specific code (e.g. implementing repositories). Components in this module could be shared amongst multiple Android applications.
- `app` is an Android application and contains all UI code as well as any classes that apply specifically to the enrollment application. Classes in the `app` module can depend on both `device` and `domain` classes.

We use the following libraries as part of the application architecture:
- Android [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel.html) and [LiveData](https://developer.android.com/topic/libraries/architecture/livedata.html) for our MVVM UI Architecture
- Android [Room](https://developer.android.com/topic/libraries/architecture/room.html) for persisting data on the device
- [Dagger](https://google.github.io/dagger/android.html) for dependency injection
- [Retrofit](http://square.github.io/retrofit/) for API calls
- [RxJava](https://github.com/ReactiveX/RxJava) for reactive style programming
- [ThreetenABP](https://github.com/JakeWharton/ThreeTenABP) for date/time (see [here](https://github.com/meso-health/meso-enrollment/pull/54) for a detailed writeup of how we made this decision)
- Android [JobScheduler](https://developer.android.com/reference/android/app/job/JobScheduler.html) for scheduling jobs (see [here](https://github.com/meso-health/meso-enrollment/pull/80) for a detailed writeup of how we made this decision)

### Build Types and Flavors

[Build types and flavors](https://developer.android.com/studio/build/build-variants.html) are the Android way of defining different build variants.

We use build types to correspond to the type of environment the application is targeting such as
development, staging or production. By default Android provides `debug` and `release` build
types, so we have co-opted them as our development and production environments respectively to
avoid creating unnecessary build types. We additionally created a `sandbox` build type that targets
our sandbox environment, and `staging` build that targets staging environment.

For generating a signed APK, please see [Running or Building Release types](https://github.com/meso-health/meso-clinic#running-or-building-release-types)

### Styleguide

The codebase follows the [Android Kotlin Styleguide](https://android.github.io/kotlin-guides/style.html) - please add any deviations or additional style rules to this section

### Dev environment

We use [Android Studio](https://developer.android.com/studio/index.html) as our IDE which leverages [Gradle](https://gradle.org/) as the build tool.

In order to run the application, you also need a `variables.gradle` file in your root directory which stores environment variables.
The defaults should work if you have a local rails server running.
When preparing for sandbox or production, please set the relevant variables in order to get the app to build successfully.

#### Connecting to your device

To access localhost from your device, we use the command-line tool that comes pre-installed with Android called `adb` (Android Debug Bridge).

After connecting your device via USB, check that your device is connected:

```
$ adb devices
```

And forward all HTTP requests to your local machine with:

```
$ adb reverse tcp:5000 tcp:5000
```

Note that you'll need to rerun this command every time you disconnect and reconnect the USB.

### Testing

Due to a bug with Android Studio on Mac OSX, we need to update the working directory that Android Studio uses when running JUnit tests - use the following [instructions](http://robolectric.org/other-environments/#updating-junit-run-configurations) to update the setting
