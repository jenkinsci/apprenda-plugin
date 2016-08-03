# Apprenda - Jenkins integration

## Overview
The Apprenda integration into Jenkins CI is a plugin designed with the standards set to extend functionality within the Jenkins CI environment. This plugin has the capability of performing:

- SMART VERSION DETECTION – To prevent downtime for production applications, the plugin communicates with the Apprenda Platform whether the application is already published, and the new version is simply a new version.

- TARGET STAGE DEPLOYMENTS – For software engineers with short development cycles that demand rapid changes, the Apprenda plugin deploys the version of the application into Definition, Sandbox (Test), or Published (Production).

- CUSTOM VERSIONING – Developers can provide a custom prefix for the application version, allowing for branched development and testing.

## Release Notes

### Jenkins Support Matrix
- 2.x - Certification is underway. Please file issues should you find an issue with the plugin.
- 1.6x - Supported up to 1.656

### Apprenda Support Matrix
- Works with Apprenda v6.0.x and up.

## Build From Source

- `git clone`
- `mvn install`

This will generate the Jenkins .hpi plugin in the `/target` folder, which you can then install into your Jenkins environment.
