# Apprenda - Jenkins integration

## Overview
The Apprenda integration into Jenkins CI is a plugin designed with the standards set to extend functionality within the Jenkins CI environment. This plugin has the capability of performing:

- SMART VERSION DETECTION – To prevent downtime for production applications, the plugin communicates with the Apprenda Platform to determine whether the application is already published, and it will create a new version at the target lifecycle stage.

- TARGET STAGE DEPLOYMENTS – For software engineers with short development cycles that demand rapid changes, the Apprenda plugin deploys the version of the application into Definition, Sandbox (Dev & Test), or Published (Production).

- CUSTOM VERSIONING – Developers can provide a custom prefix for the application version, allowing for branched development and testing.

## Release Notes
- v2.0.0.
  - Bypass SSL: On upgrade to this release, you may get some warnings on the configuration variable BypassSSL. This used to be a global configuration option under Jenkins\Manage Jenkins\Configure System\Apprenda. It is now a configuration option tied to each set of Credentials. You can now configure BypassSSL under Credentials\your Apprenda-specific credentials. Make sure to update your Apprenda credentials appropriately

## Releases
- v2.0.0, released on Nov 03 2017
- v1.3.1, released on Jan 19 2017

### Jenkins Support Matrix
- 2.73.2 - Certified using the latest Jenkins-2.73.2 release
- 1.6x - Supported up to 1.656

### Apprenda Support Matrix
- Works with Apprenda version 6.x and later releases

## Build From Source
- Run `git clone` to download all the relevant binaries
- Run `mvn clean install` in the same folder as pom.xml. Tested with apache-maven-3.5.0

This will generate the Jenkins Apprenda.hpi plugin in the `/target` folder, which you can then install into your Jenkins environment.

## Installation
1. Install Jenkins and enable the default plugins
2. Optionally install the msbuild plugin (Helpful blog at http://justinramel.com/2013/01/15/5-minute-setup/) if you want to use Jenkins to build your Visual Studio solution. Install any other appropriate plugins for other programming frameworks.
  * If you install the msbuild plugin, configure it to ensure Jenkins knows where to find the msbuild.exe location (Configure it under Jenkins\Manage Jenkins\Global Tool Configuration)
3. Use the Apprenda.hpi and install it in Jenkins (Download from here or follow the "Build From Source" section).
4. Upload the file via the Jenkins\Manage Jenkins\Manage Plugins\Advanced\Upload Plugin
5. Restart Jenkins after a successful upload

## Creating and Running a Project
This tutorial provides a quick-start means of deploying your application to Apprenda via a simple Jenkins Freestyle project.

1. Create a new Freestyle project
2. Under Build configuration, click on "Add Build Step" and then select "Deploy to Apprenda"
3. Enter your Apprenda credentials, tenant alias, and the URL of the Apprenda installation. Optionally, you can configure the BypassSSL flag.
4. Click Add. Jenkins will encrypt & store your credentials on the Jenkins server for use by other projects as well
5. Now you are ready to start entering specific information about this project. Fill in the fields for the application alias and name, version prefix (default is 'v'), and the target stage for your deployment.
  * From there, fill out the rest of your project with the necessary steps to execute your workflow (for ex. clone from git, build with MSBuild/ant, package using Apprenda's acs.exe etc.)
  * If you use msbuild.exe to build your solution, the acs.exe command to create an Apprenda Application Package is "acs.exe newpackage -sln jenkinswebapp.sln -o jenkinswebapp.zip"
6. You have a choice to either specify the local path to an Apprenda Application Package using the Artifact Name and Package Directory options, or you can provide the Apprenda Application Package URL
  * If entering the local path, Artifact Name should be the file name, for example myapplication.zip. Package Directory should be the full path to the folder containing myapplication.zip
  * If entering the URL path, it should be a fully formatted URL to the Application Package, for example http://docs.apprenda.com/sites/default/files/TimeCard_Archive.zip
6. There are a few advanced options that offer flexibility for developers concerning version management. For example, there exists an option that forces every build can create a new version in Apprenda. This is especially useful if you have an application in Published mode, and you plan to test in parallel multiple vNext instances of your application. Another advanced option is the ability to force the deployment to a specific Apprenda Version, for example v23.
6. Click Save.
7. Click "Build Now".

If everything is configured correctly, your application will deploy to Apprenda! You can view the "Console Output" to help diagnose and troubleshoot any issues with the Apprenda deployment.

## Video
- View a demo video of using Jenkins with Apprenda at https://apprenda.com/partners/integrations/jenkins-ci/
