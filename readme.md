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

## Installation
1. Use the Apprenda.hpi (Download from here or build yourself).
2. Upload the file via the "Advanced Tab in Jenkins"
3. Configure your Apprenda instance in the Manage Jenkins -> System.
  * Enter the cloud URI of your Apprenda instance that Jenkins will use to connect and deploy your applications.
  * Click Save.

## Creating and Running a Project
This tutorial provides a quickstart means of deploying your application to Apprenda via a simple Jenkins freeform project.

1. Create a new freeform project
2. Under build configuration, click on "Add a Build Step" and then select "Deploy to Apprenda"
3. Enter your Apprenda credentials and tenant alias.
4. Then click "Validate Credentials". This tests your connectivity to Apprenda, and then encrypts & stores your credentials on the Jenkins server.
5. Fill in the fields for the application alias, version prefix (default is 'v'), and the target stage for your deployment.
From there, fill out the rest of your project with the necessary steps to execute your workflow (for ex. clone from git, build with MSBuild/ant, etc.)
6. Click Save.
7. Click Build Now.

If everything is configured correctly, your application will deploy to Apprenda!

## Customization & Configuration

There are a few advanced options that offer flexibility for developers concerning version management. For example, there exists an option that forces every build can create a new version.

## Video
- View a demo video of using Jenkins with Apprenda at https://apprenda.com/partners/integrations/jenkins-ci/
