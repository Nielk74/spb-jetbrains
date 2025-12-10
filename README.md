# SPB Build - JetBrains Rider Plugin

A Rider plugin that provides Superbuild functionality with VSTEL_CurrentSolutionBuildID environment variable support.

## Features

- **Superbuild**: Trigger builds with a random `VSTEL_CurrentSolutionBuildID` environment variable set (new UUID generated for each build)
- **Toolbar Integration**: Hammer icon in the toolbar for quick Superbuild access
- **Context Menu**: Right-click on projects or solutions to Superbuild
- **Automatic CLI Update**: Checks for CLI tool updates via HTTP Last-Modified header (once per day)

## Building

### Prerequisites

- JDK 21 or higher

### Build Commands

```bash
# Build the plugin
./gradlew buildPlugin

# Run the plugin in a sandbox IDE
./gradlew runIde

# Create distributable zip
./gradlew buildPlugin
# Output: build/distributions/spb-jetbrains-1.4.0.zip
```

## Installation

1. Build the plugin using `./gradlew buildPlugin`
2. In Rider, go to **Settings** → **Plugins** → **⚙️** → **Install Plugin from Disk...**
3. Select `build/distributions/spb-jetbrains-1.4.0.zip`
4. Restart Rider

## Usage

### Superbuilding

**Option 1 - Toolbar**: Click the hammer icon in the toolbar

**Option 2 - Context Menu**: Right-click on a project/solution and select **Superbuild Solution**

Each Superbuild generates a new random UUID for `VSTEL_CurrentSolutionBuildID` and triggers the native build action.

### Settings

Go to **Settings** → **Tools** → **SPB Build** to configure:

- **CLI update URL**: URL to check for CLI tool updates (uses HTTP HEAD Last-Modified)
- **CLI download path**: Where the CLI tool will be downloaded (default: `D:\superbuilder\tracker.exe`)
- **Enable automatic update checks**: Toggle daily update checks

## Project Structure

```
src/main/kotlin/com/spb/
├── actions/
│   ├── SuperbuildAction.kt            # Toolbar superbuild action
│   └── SuperbuildSolutionAction.kt    # Context menu superbuild action
├── services/
│   ├── SpbBuildService.kt             # Build execution with env variable
│   ├── CliUpdateService.kt            # CLI update checking via Last-Modified
│   └── SpbStartupActivity.kt          # Startup hook for update checks
└── settings/
    ├── SpbSettings.kt                 # Persistent settings storage
    └── SpbSettingsConfigurable.kt     # Settings UI
```

## License

Proprietary
