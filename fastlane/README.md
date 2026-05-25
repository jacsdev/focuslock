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

### android build

```sh
[bundle exec] fastlane android build
```

Build release AAB (incrementa versionCode automáticamente)

### android local

```sh
[bundle exec] fastlane android local
```

Build local + copia el AAB a ~/Downloads (sin subir a Play Store)

### android internal

```sh
[bundle exec] fastlane android internal
```

Build + subir a Internal Testing

### android alpha

```sh
[bundle exec] fastlane android alpha
```

Promover Internal → Alpha

### android beta

```sh
[bundle exec] fastlane android beta
```

Promover Alpha → Beta

### android deploy

```sh
[bundle exec] fastlane android deploy
```

Promover Beta → Production

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
