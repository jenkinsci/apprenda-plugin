# Apprenda - Jenkins integration

## Overview
The Apprenda integration into Jenkins CI is a plugin designed with the standards set to extend functionality within the Jenkins CI environment. This plugin has the capability of performing:

- SMART VERSION DETECTION – To prevent downtime for production applications, the plugin communicates with the Apprenda Platform to determine whether the application is already published, and it will create a new version at the target lifecycle stage.

- TARGET STAGE DEPLOYMENTS – For software engineers with short development cycles that demand rapid changes, the Apprenda plugin deploys the version of the application into Definition, Sandbox (Dev & Test), or Published (Production).

- CUSTOM VERSIONING – Developers can provide a custom prefix for the application version, allowing for branched development and testing.

## Release Notes
- v2.0.0.
  - Bypass SSL: On upgrade to this release, you may get some warnings on the configuration variable BypassSSL. This used to be a global configuration option under Jenkins\Manage Jenkins\Configure System\Apprenda. It is now a configuration option tied to each set of Credentials. You can now configure BypassSSL under Credentials\your Apprenda-specific credentials. Make sure to update your Apprenda credentials appropriately. **Be aware that by default BypassSSL is false now, where in previous releases it defaulted to true. If your Jenkins plugin fails to connect to Apprenda, try updating your credentials object to set BypassSSL to true. This flag may need to be set to true if you are using a self-signed SSL certificate for the Apprenda frontend certificate.**
  - Because we have changed some of the configuration settings for the Apprenda plugin, it will be necessary to open up existing projects and resave them using the new configuration options.

## Releases
- v2.0.0, released on Nov 02 2017
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

## Using Build with Parameters
If you would like to templatize a project and define parameters for some of the key values in the Apprenda build step, follow these example instructions. It is possible other Jenkins plugins can achieve the same result, but we tested this with the MultiJob plugin. Essentially any plugin that can result in the parameterized values being injected into the build step environment variables would work.

1. Install the Jenkins MultiJob plugin
2. Create a new MultiJob called BuilderJob and indicate this project is parameterized. Notice that in this example implementation we will use all 3 ways of specifying parameters to a project. Those 3 are "Build with Parameters", predefined parameters, parameters from property file. You can pick and chose any of these methods, or other methods that inject environment variables to a build. You can even chose only 1 of them and supply all parameters in a property file.
3. Create a new string parameter for the application name
4. Add a new post build action to "Trigger parameterized build on other projects". Indicate that "ApprendaJob" is the project to build (you may have to go create it first). Now add some predefined parameters for the application alias and also ask to import parameters from a property file. Don't forget to also indicate that you want to include the "Current build parameters"
5. This is what the property file looks like
6. Now go and create or edit the ApprendaJob, that's a MultiJob project
7. Again, indicate that ApprendaJob project is parameterized. Create a string parameter for each of the parameter names you created in step number 4 above. You don't have to enter a default value unless you want to use this project independently as well
8. Add a new build step to "Deploy to Apprenda". This can be an independent step or be part of a MultiJob Phase
9. Instead of entering literal values for all of the Apprenda configuration settings (the 6 variables mentioned below), you can instead use the identical parameter names from steps 4 and 7 above. Make sure to check the box that says "Build with Parameters".
10. You are now ready to start a build. Go to the BuilderJob and click on "Build with Parameters" and enter a name for your Apprenda application
11. When the BuilderJob kicks off the ApprendaJob, you can go to the actual execution environment (using Build History links). View the Parameters to ensure all the proper values were propagated to the Apprenda build step
12. View the Console Output to see how Apprenda used the new parameters. You now have a parameterized build for Apprenda!

The 6 variables from the Apprenda build step that allow for parameterization are:
1. Application Name
2. Application Alias
3. Target Stage *This Apprenda parameter expects the fixed environment variable $ApprendaStage*
4. Artifact Name
5. Package Directory
6. Application Package URL

## Video
- View a demo video of using Jenkins with Apprenda at https://apprenda.com/partners/integrations/jenkins-ci/
