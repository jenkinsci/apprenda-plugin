#Apprenda-Plugin

###Table of Contents
* Overview
* Installation
* Creating and Running a Project
* Customization & Configuration

## Overview


## Installation
1. Download the file here.
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

Another option that is availble 
