# SPB Build - JetBrains Rider Plugin

A Rider plugin that provides custom MSBuild integration with VSTelemetrySession environment variable support.

## Features

- **Custom MSBuild Build**: Right-click on projects (.csproj, .vbproj, .fsproj, .vcxproj) or solutions (.sln) to build with the VSTelemetrySession environment variable set
- **Configurable Settings**: Configure the VSTelemetrySession value, download path, and update check URL
- **Automatic Update Checking**: Checks for new versions of external tools once per day (mock implementation - TODO: implement actual HTTP fetch)

## Building

### Prerequisites

- JDK 17 or higher
- Gradle 8.5 (wrapper included)

### Build Commands

```bash
# Build the plugin
./gradlew buildPlugin

# Run the plugin in a sandbox IDE
./gradlew runIde

# Create distributable zip
./gradlew buildPlugin
# Output: build/distributions/spb-jetbrains-1.0.0.zip
```

## Installation

1. Build the plugin using `./gradlew buildPlugin`
2. In Rider, go to **Settings** → **Plugins** → **⚙️** → **Install Plugin from Disk...**
3. Select `build/distributions/spb-jetbrains-1.0.0.zip`
4. Restart Rider

## Usage

### Building with VSTelemetrySession

1. Right-click on a project file (.csproj, etc.) or solution file (.sln) in the Project view
2. Select **SPB Build Project** or **SPB Build Solution**
3. The build will execute with the `VSTelemetrySession` environment variable set

### Settings

Go to **Settings** → **Tools** → **SPB Build** to configure:

- **VSTelemetrySession value**: The value to set for the VSTelemetrySession environment variable (default: "SPB")
- **Download path**: Where external tools will be downloaded (default: ~/spb-tools)
- **Update check URL**: URL to check for new versions (JSON format)
- **Enable automatic update checks**: Toggle daily update checks

## Version Check (TODO)

The version check feature is currently a mock implementation. To complete it:

1. Implement `fetchLatestVersion()` in `VersionCheckService.kt` to make HTTP GET requests
2. Implement `downloadUpdate()` to download the executable file
3. Expected JSON response format from the update check URL:

```json
{
  "version": "1.2.0",
  "downloadUrl": "http://example.com/spb/spb-tool-1.2.0.exe",
  "releaseNotes": "Bug fixes and improvements"
}
```

## Project Structure

```
src/main/kotlin/com/spb/
├── actions/
│   ├── SpbBuildProjectAction.kt    # Right-click action for projects
│   └── SpbBuildSolutionAction.kt   # Right-click action for solutions
├── services/
│   ├── SpbBuildService.kt          # Build execution with env variable
│   ├── VersionCheckService.kt      # Update checking (mock)
│   └── SpbStartupActivity.kt       # Startup hook for update checks
└── settings/
    ├── SpbSettings.kt              # Persistent settings storage
    └── SpbSettingsConfigurable.kt  # Settings UI
```

## License

Proprietary
