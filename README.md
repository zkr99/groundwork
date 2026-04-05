# Groundwork

Groundwork is a Java CLI that creates a clean new project folder from one of your recurring project patterns.

In practice, that means:

- copy one or more local reference repos into `_reference/`
- strip junk like `.git`, `node_modules`, caches, and build outputs
- generate fresh project files like `CLAUDE.md`, `README.md`, and `.gitignore`
- create starter directories for the new project

If AI helps you decide what kind of project you want to build, Groundwork helps you start that kind of project the same way every time.

## Release Status

Groundwork is currently an open-source alpha.

That means:

- the core CLI works and is tested
- the repo is ready for public source use
- installation from source is solid today
- tagged GitHub releases can publish JVM distribution assets
- Homebrew and richer polish are still future work

## The Simple Idea

Groundwork is not the tool that invents your product idea.

Groundwork is the tool that says:

> "Now that we know this is another `camera-ai-app` or `web-only` project, create the clean starting folder for me in one command."

The simplest mental model is:

1. You or an AI model decide the project archetype.
2. Groundwork captures that archetype as a reusable template.
3. Groundwork scaffolds fresh projects from it quickly and consistently.

## Why It Exists

If you reuse the same local repos and setup conventions over and over, the setup work gets repetitive:

- copy reference repos
- remove `.git`
- remove `node_modules`
- remove caches and build folders
- create `_reference/`
- create project docs
- create starter directories

That work is easy, but annoying. Groundwork turns it into a repeatable command.

## What Groundwork Is Good At

Groundwork is strongest when you already have useful local references:

- existing codebases
- a workspace containing several local git repos
- known project structures
- known cleanup rules
- known documentation patterns

It is best thought of as a deterministic scaffolding engine for recurring project archetypes.

## What Groundwork Is Not

Groundwork does not:

- invent new product ideas
- reason about your blueprint the way an AI model does
- choose relevant repos by itself
- understand which exact source files matter semantically

Today, the decision-making still comes from you or AI.
Groundwork executes that decision reliably.

## Groundwork vs AI

The cleanest way to think about the split is:

- AI thinks.
- Groundwork repeats.

AI is better for:

- brainstorming
- writing blueprints
- choosing architecture
- deciding which old repos are relevant the first time

Groundwork is better for:

- repeating a known setup
- reducing prompt repetition
- scaffolding consistently
- doing safe copy/clean/generate steps quickly

## Current Workflow

The current happy path looks like this:

1. Brainstorm the new idea with AI or by hand.
2. Decide which archetype it fits.
3. Create a Groundwork template once with `groundwork template create` or `groundwork template discover`.
4. Use `groundwork new ...` whenever you want another project of that type.

The new convenience improvement in this repo is that you no longer need to hand-author template YAML for common cases.

You can now create a template from CLI flags:

```bash
groundwork template create camera-ai-app \
  --description "Camera to AI archetype" \
  --reference "/path/to/core-agent::Vision patterns" \
  --reference "/path/to/mobile-app::Mobile patterns" \
  --claude-template python-fastapi \
  --gitignore-profile python-nextjs \
  --dir backend/ \
  --dir mobile/
```

That command writes the YAML for you.

## What Groundwork Does Under The Hood

When you run:

```bash
groundwork new PlantSnap --template camera-ai-app
```

Groundwork does this:

1. Loads the template YAML.
2. Validates that the template and repo paths are valid.
3. Creates the target project folder.
4. Copies the listed reference repos into `_reference/`.
5. Strips junk from those copied repos.
6. Creates any requested starter directories.
7. Generates root files like `CLAUDE.md`, `README.md`, and `.gitignore`.
8. Optionally initializes git.

## Commands

### Core Commands

- `groundwork init`
- `groundwork list`
- `groundwork validate <template>`
- `groundwork new <name> --template <template>`
- `groundwork clean <project>`

### Template Convenience Commands

- `groundwork template create <name> ...`
- `groundwork template discover <name> --from <workspace> ...`

This is the first step toward hiding YAML from normal day-to-day usage.

## Installation

### Option 1: Run From Source

Requirements:

- Java 21+

Clone the repo and run:

```bash
./gradlew test
./gradlew run --args='--help'
```

### Option 2: Install A Local JVM CLI Distribution

This is the best current way to use Groundwork as a normal JVM CLI on your machine.

Requirements:

- Java 21+

Build and install locally:

```bash
./gradlew installDist
./build/install/groundwork/bin/groundwork --help
```

You can also build release-style archives:

```bash
./gradlew distZip distTar
```

### Option 3: Build A Native Binary Locally

Requirements:

- GraalVM with `native-image`

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-25.jdk/Contents/Home \
PATH="/Library/Java/JavaVirtualMachines/graalvm-25.jdk/Contents/Home/bin:$PATH" \
./gradlew clean nativeCompile --no-daemon
./build/native/nativeCompile/groundwork --help
```

### GitHub Releases

Tagged releases now build and publish JVM distribution assets through GitHub Actions.

Today, the smoothest install story is:

- source checkout for contributors
- `installDist` for local CLI use
- GitHub release archives for wider alpha testing

Homebrew packaging is not in place yet.

## Example End-To-End Usage

### 1. Initialize Groundwork Home

```bash
groundwork init
```

This creates:

- `~/.groundwork/templates/`
- `~/.groundwork/claude-templates/`
- a starter example template
- a starter project-guide CLAUDE template

### 2. Create a Template Without Writing YAML Manually

```bash
groundwork template create camera-ai-app \
  --description "Camera to AI archetype" \
  --reference "/path/to/core-agent::Vision patterns" \
  --reference "/path/to/mobile-app::Mobile patterns" \
  --claude-template python-fastapi \
  --gitignore-profile python-nextjs \
  --dir backend/ \
  --dir mobile/
```

### 2A. Create a Template By Discovering a Local Workspace

If you already keep a folder with several local git repos that make up a recurring stack, Groundwork can turn that workspace into an archetype automatically:

```bash
groundwork template discover camera-ai-app \
  --from /path/to/camera-stack \
  --description "Camera to AI archetype" \
  --dir backend/ \
  --dir mobile/
```

Notes:

- Repeat `--reference` for multiple repos.
- Repeat `--dir` for multiple starter folders.
- Repeat `--strip` for extra cleanup rules beyond Groundwork's built-in defaults.
- Groundwork strips obvious junk like `.git`, `node_modules`, `build`, caches, and `.env*`, but it no longer strips source directories like `ios/` and `android/` by default.
- If your reference purpose contains spaces, quote the whole `--reference` value.
- `template discover` scans a workspace for local git repos and drafts the template for you.

### 3. Validate the Template

```bash
groundwork validate camera-ai-app
```

### 4. Scaffold a New Project

```bash
groundwork new PlantSnap \
  --template camera-ai-app \
  --description "AI plant species identifier"
```

### 5. Remove References Later If You Want

```bash
groundwork clean /path/to/PlantSnap
```

## Running Groundwork During Development

From the repo root:

```bash
./gradlew test
./gradlew run --args='init'
./gradlew run --args='list'
```

If you are in a constrained environment and Gradle has native lock issues, this fallback is reliable:

```bash
GRADLE_USER_HOME=/tmp/groundwork-gradle-home ./gradlew test --no-daemon
```

## How To Test It

### Quick Reality Check

This is the fastest real test:

1. Create a tiny fake reference repo in `/tmp`.
2. Use `groundwork template create` to generate a template.
3. Run `groundwork new ...` with that generated template.
4. Confirm:
   - `_reference/` exists
   - source files were copied
   - `.git` and `node_modules` are gone
   - `CLAUDE.md`, `README.md`, and `.gitignore` were generated

### Full Local Test Suite

```bash
GRADLE_USER_HOME=/tmp/groundwork-gradle-home ./gradlew test --no-daemon
```

### Native Binary Build

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-25.jdk/Contents/Home \
PATH="/Library/Java/JavaVirtualMachines/graalvm-25.jdk/Contents/Home/bin:$PATH" \
GRADLE_USER_HOME=/tmp/groundwork-gradle-home \
./gradlew clean nativeCompile --no-daemon
```

Then run the binary:

```bash
./build/native/nativeCompile/groundwork --help
```

## Development Status

Groundwork is now usable as an early local tool.

### What Is Working

- Java 21 Gradle CLI project
- Picocli command surface
- template loading and validation
- reference repo copy
- strip policy and cleanup
- generated `CLAUDE.md`, `README.md`, and `.gitignore`
- starter directory creation
- `groundwork init`
- `groundwork validate`
- `groundwork new`
- `groundwork clean`
- `groundwork template create`
- `groundwork template discover`
- JUnit coverage for core paths
- successful GraalVM native build

### What Has Been Verified

- full JVM test suite passes
- CLI smoke tests passed for:
  - `init`
  - `validate`
  - `new`
  - `clean`
  - `template create`
- native binary builds and launches successfully
- native binary validates templates that use built-in `CLAUDE` and `.gitignore` resources

### What Is Still Missing

- broader release automation, including native release assets
- Homebrew tap packaging
- richer git initialization flow
- blueprint-to-template automation
- AI-assisted template drafting
- more command-level integration coverage

Release automation has now started in the repo for JVM distribution assets, but the public distribution story is still early-stage rather than polished.

## Progress Estimate

Two different estimates matter here:

- Roughly **80% done** toward "useful for personal local use"
- Roughly **60-65% done** toward "polished public v1"

Why the difference:

- The core product works now.
- The tooling, automation, packaging, and UX polish are not finished yet.
- Alpha installation is now real, but public packaging convenience is still catching up.

So the honest answer is:

- **Ready to use now:** yes, for a local early-adopter workflow
- **Ready to publish as a polished tool:** not yet

## Where It Sits In The Blueprint

### Phase 1: Core Scaffolding

This is effectively done.

### Phase 2: Generators

This is mostly done.

### Phase 3: Polish and Distribution

This has started, but is not complete.

The biggest remaining gaps are around packaging, release automation, and better ergonomics beyond raw templates.

## Design Direction

The biggest UX problem with the original concept was:

> "If I still have to hand-write YAML, why not just ask AI again?"

The new `groundwork template create` command is the first answer to that problem.

The likely next improvements are:

1. interactive template creation
2. template capture from an existing scaffolded project
3. blueprint-to-template drafting
4. AI-assisted repo selection and template generation

That direction would let Groundwork keep its deterministic execution model while becoming much easier to adopt.

## Repo Structure

```text
src/main/java/dev/groundwork/
├── Groundwork.java
├── commands/
├── config/
├── generate/
├── scaffold/
├── support/
└── template/
```

## License

MIT. See [LICENSE](/Users/johnny/Forge/LICENSE).

## Contributing

See [CONTRIBUTING.md](/Users/johnny/Forge/CONTRIBUTING.md).
