## Android Extensions
[![android library](https://img.shields.io/badge/version-v1.0.0-orange)](https://github.com/UdaraWanasinghe/android-extensions)

This is a collection of useful extensions for Android development.



## Building
This project uses Gradle as its build system. To build this project, run `gradlew publishToMavenLocal` command on the root of the project.



## Using

To use this library in your project, add the `mavenLocal` repository to your project's `settings.gradle` file.

```groovy
dependencyResolutionManagement {
    repositories {
        mavenLocal()
    }
}
```

Then add the following dependency to your project's `build.gradle` file.

```groovy
dependencies {
    implementation 'com.aureusapps.android:extensions:1.0.0'
}
```



## Appreciate my work!

If you like my work, please consider buying me a coffee.

<a href="https://www.buymeacoffee.com/udarawanasinghe" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/default-orange.png" alt="Buy Me A Coffee" height="41" width="174"></a>