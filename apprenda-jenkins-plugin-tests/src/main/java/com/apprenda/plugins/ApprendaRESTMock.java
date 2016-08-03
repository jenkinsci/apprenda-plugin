package com.apprenda.plugins;
import javax.json.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    ApprendaRESTMock

    This class is primarily used as a testing harness to simulate REST API calls to an apprenda instance.
    Very useful for testing.
 */

public class ApprendaRESTMock {
        public ApprendaRESTMock()
        {
            init();
        }

        // init function here just simply creates a few arrays of data for us to work with.
        protected void init()
        {
            applications = new ArrayList<Application>();
            appHash = new ArrayList<String>();
            // TEST CASE 1
            Application app = new Application();
            app.appAlias = "app";
            app.versionHash = new ArrayList<String>();
            app.versionHash.add("v1");
            Version version = new Version();
            version.stage = "Published";
            version.versionAlias = "v1";
            app.versions = new ArrayList<Version>();
            app.versions.add(version);
            applications.add(app);
            appHash.add("app");
            // TEST CASE 2
            Application bac = new Application();
            bac.appAlias = "bac";
            bac.versions = new ArrayList<Version>();
            bac.versionHash = new ArrayList<String>();
            bac.versionHash.add("v1");
            bac.versionHash.add("v2");
            Version bacv1 = new Version();
            bacv1.stage = "Published";
            bacv1.versionAlias = "v1";
            Version bacv2 = new Version();
            bacv2.stage = "Sandbox";
            bacv2.versionAlias = "v2";
            bac.versions.add(bacv1);
            bac.versions.add(bacv2);
            applications.add(bac);
            appHash.add("bac");
        }
        // Model Classes
        protected class Version
        {
            protected String versionAlias;
            protected String stage;
        }

        protected class Application
        {
            protected String appAlias;
            protected ArrayList<String> versionHash;
            protected ArrayList<Version> versions;

        }
        protected List<String> appHash;
        protected List<Application> applications;

        // REST API: /developer/api/v1/versions/<appAlias> GET
        protected String GetAppAliasVersions(String appAlias)
        {
            int ref = appHash.indexOf(appAlias);
            if(ref >= 0)
            {
                Application app = applications.get(ref);
                return app.toString();
            }
            else return null;
        }

        // REST API: /developer/api/v1/versions/<appAlias> POST
        protected void newAppVersion(String appAlias, String versionAlias )
        {
            System.out.println("Creating app " + appAlias + " with version " + versionAlias);
        }

        // this is a test method i put in here due to static references. basically this is functionally testing that our version detection engine is working
        public String testVersionDetection(String appAlias, String prefix, boolean forceNewVersion, boolean advForceVersion, String advForceVersionAlias)
        {
            String tempNewVersion;
            // rules from AI-166
            // versions appear
            String gson = GetAppAliasVersions(appAlias);
            System.out.println(gson);
            Boolean forcedVersionExists = false;
            JsonReader reader = Json.createReader(new StringReader(gson));
            JsonObject apps = reader.readObject();
            JsonArray versions = apps.getJsonArray("versions");
            // iterate the JsonArray, here's how we are going to apply the rules
            // if we find the right regex of the prefix (whether its v or otherwise), we check the stage that version
            // is in. if its in published, then we know we need to create a new version.
            int targetVersionNumber = 1;
            Pattern pattern = Pattern.compile("\\d+");
            boolean HighestVersionPublished = false;
            for (int i = 0; i < versions.size(); i++) {
                // get the version object and the alias
                JsonObject version = versions.getJsonObject(i);
                String alias = version.getString("versionAlias");
                // test1
                if (!advForceVersion && alias.matches(prefix + "\\d+")) {

                    Matcher matcher = pattern.matcher(alias);
                    matcher.find();
                    int temp = Integer.parseInt(alias.substring(matcher.start()));
                    String versionStage = version.getString("stage");
                    // if the version we are looking at is in published state or we are forcing a new version regardless,
                    // get the greater of the two numeric versions and increment it.
                    // so if the current version we found is v4 and its published...if we already have found a v5 in sandbox do nothing.
                    if(temp >= targetVersionNumber)
                    {
                        // use case v5 published, current is v1 (first run)
                        // if published set to v6, else set to v5
                        targetVersionNumber = temp;
                        //
                        if (versionStage.equals("Published")) {
                            HighestVersionPublished = true;
                        }
                        else
                        {
                            HighestVersionPublished = false;
                        }
                    }
                } else if(advForceVersion && alias.matches(prefix + "\\d+"))
                {
                    forcedVersionExists = true;
                }
            }
            if(advForceVersion) {
                if (!forcedVersionExists) {
                    newAppVersion(appAlias, advForceVersionAlias);
                }
                return advForceVersionAlias;
            }
            if(forceNewVersion || HighestVersionPublished)
            {
                targetVersionNumber++;
                tempNewVersion = prefix + targetVersionNumber;
                newAppVersion(appAlias, tempNewVersion);
            }
            else
            {
                tempNewVersion = prefix + targetVersionNumber;
            }
            // if we literally can't find anything that would require us to bump up the version we patch v1
            // and continue on with the patch element
            return tempNewVersion;
        }
}

