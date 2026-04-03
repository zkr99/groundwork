# Contributing

Thanks for contributing to Groundwork.

## Local Setup

Groundwork targets:

- Java 21
- Gradle 9.4.1 via the checked-in wrapper

Recommended first commands:

```bash
./gradlew test
./gradlew run --args='--help'
```

## Project Standards

- Keep the CLI surface small and explicit.
- Treat referenced source repositories as read-only input.
- Prefer `Path`, `Files`, and small focused classes over heavier abstractions.
- Preserve native-image compatibility when adding dependencies or reflection-heavy features.
- Add or update tests for behavior changes, especially around path safety and scaffold cleanup.

## Before Opening A Pull Request

Please make sure:

```bash
./gradlew test
```

passes locally.

If you add user-facing commands or behavior, update the README as part of the same change.
