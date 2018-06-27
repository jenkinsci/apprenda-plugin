package com.apprenda.jenkins.plugins.apprenda;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import hudson.util.Secret;
import hudson.model.TaskListener;
import utils.ApprendaRestUtility;;

/*
     A lot of the heavier lifting and communication with Apprenda is done here.
 */

public class ApprendaClient {
	private final String url;
	private final boolean bypassSSL;
	private String token = null;
  //private Logger logger;
	private TaskListener listener;

	// Constructor takes in instance URL and the flag whether to bypass SSL
	// validation.
	public ApprendaClient(String url, boolean bypassSSL, final TaskListener listener) {
		this.url = url;
		this.bypassSSL = bypassSSL;
		this.listener = listener;
		//logger = Logger.getLogger("jenkins.plugins.apprenda");
	}

	public boolean authenticate(ApprendaCredentials credentials) throws Exception {
		try {
			listener.getLogger().println("[APPRENDA] Attempting to authenticate to Apprenda with username " + credentials.getUsername()
					+ " and tenant alias " + credentials.getTenant() + ".");

			JsonObject json = Json.createObjectBuilder().add("username", credentials.getUsername())
					.add("password", Secret.toString(credentials.getPassword()))
					.add("tenantAlias", credentials.getTenant()).build();

			String AUTH_PATH = "authentication/api/v1/sessions/developer";
			Response response = new ApprendaRestUtility().PostResponseRequest(bypassSSL, getUrl(), AUTH_PATH, json);
			if (response.getStatus() != 201) {
				listener.getLogger().println("[APPRENDA] Authentication Failed.");
				return false;
			}
			JsonObject jsonObject = response.readEntity(JsonObject.class);
			this.token = jsonObject.getString("apprendaSessionToken");
			// we are not printing the token for security reasons
			return true;
		} catch (Exception e) {
			listener.getLogger().println("Authentication to Apprenda failed. Ensure that BypassSSL is set to true if you are using self-signed certificates for Apprenda.");

			String fullStackTrace = org.apache.commons.lang.exception.ExceptionUtils.getFullStac‌​kTrace(e);
			listener.getLogger().println(fullStackTrace);
			listener.getLogger().println(e.getLocalizedMessage() + e);
			return false;
		}
	}

	// this method calls the Apprenda REST API to create a new version
	public Boolean newAppVersion(String appAlias, String versionAlias, String appDescriptionEx) {
		try {
			JsonObject newVersion = Json.createObjectBuilder().add("Name", versionAlias).add("Alias", versionAlias)
					.add("Description", appDescriptionEx).build();

			listener.getLogger().println("[APPRENDA] JSON -- " + newVersion);
			listener.getLogger().println("[APPRENDA] Step 1: Create New Version: " + versionAlias);
			Response createVersion = new ApprendaRestUtility(token).PostResponseRequest(bypassSSL, getUrl(),
					"developer/api/v1/versions/" + appAlias, newVersion);
			if (createVersion.getStatus() == 201) {
				listener.getLogger().println("[APPRENDA] New Version Created");
				return true;
			} else {
				listener.getLogger().println("[APPRENDA] Version was not created. Please check to see if the version is taken, or the application exists. Full response: "
								+ createVersion.readEntity(JsonObject.class).toString());
				return false;
			}
		} catch (Exception e) {
			// logger.log(Level.SEVERE, "Exception occurred during creation of
			// new version.");
			return false;
		}
	}

	// this method patches the application to a specific version, with a file
	// that contains the new binaries
	public Boolean patchApp(String appAlias, String versionAlias, File appFile, String stage, String applicationPackageURL) throws java.io.IOException
	{
		InputStream fileInStream = null;
		try {
			listener.getLogger().println("[APPRENDA] Starting promotion of application: " + appAlias + " and version: " + versionAlias);
			String VERSION_PATH = "developer/api/v1/versions/";

			Response response = null;
			if (appFile != null)
			{
				fileInStream = new FileInputStream(appFile);
				listener.getLogger().println("[APPRENDA] Starting patching of application using archive: " + appFile.getAbsolutePath());
				response = new ApprendaRestUtility(token).PatchApplication(bypassSSL, getUrl(),
						VERSION_PATH + appAlias + "/" + versionAlias, stage, fileInStream);
			}
			else
			{
				listener.getLogger().println("[APPRENDA] Starting patching of application using archive in URI: " + applicationPackageURL);
				response = new ApprendaRestUtility(token).PatchApplication(bypassSSL, getUrl(),
						VERSION_PATH + appAlias + "/" + versionAlias, stage, applicationPackageURL);
			}

			if (response.getStatus() == 200) {
				listener.getLogger().println("[APPRENDA] Promotion complete.");
				return true;
			} else {
				listener.getLogger().println("[APPRENDA] An error occurred during the patching of your application. Error details: "
								+ response.toString());
				return false;
			}
		} catch (Exception e)
		{
			listener.getLogger().println(e.getMessage() + e);
			return false;
		}
		finally
		{
			if (fileInStream != null)
			{
				fileInStream.close();
			}
		}
	}

	// this method patches the creates an application to a specific version,
	// with a file
	// that contains the new binaries
	public Boolean createApp(String appAlias, String appName, String appDescription, File appFile, String stage, String applicationPackageURL) {
		try {
			listener.getLogger().println("[APPRENDA] Starting creation of application: " + appAlias);
			String PATH = "developer/api/v1/apps";
			Response response = new ApprendaRestUtility(token).CreateApplication(bypassSSL, getUrl(), PATH, appAlias, appName, appDescription);
			if (response.getStatus() == 201) {
				listener.getLogger().println("[APPRENDA] Creation succesful.");
				return patchApp(appAlias, "v1", appFile, stage, applicationPackageURL);
			} else {
				listener.getLogger().println("[APPRENDA] An error occurred during the patching of your application. Here's what I got: "
								+ response.toString());
				return false;
			}
		} catch (Exception e) {
			listener.getLogger().println(e.getMessage() + e);
			return false;
		}
	}

	public String getUrl() {
		return url;
	}

	// this method get all of the versions
	public JsonArray getAppAliasVersions(String appAlias) throws Exception {
		if (token == null) {
			throw new SecurityException("[APPRENDA] Authentication failed previously, no session token exists.");
		}
		Response getVersions = new ApprendaRestUtility(token).GetResponseRequest(bypassSSL, getUrl(),
				"developer/api/v1/versions/" + appAlias, null);
		if (getVersions.getStatus() == 200) {
			return getVersions.readEntity(JsonArray.class);
		} else {
			return null;
		}
	}

/*
	public ArrayList<String> getAppAliases(String username) throws SecurityException {
		try {
			if (token == null)
				throw new SecurityException("[APPRENDA] Authentication failed previously, no session token exists.");
			Response getAliases = new ApprendaRestUtility(token).GetResponseRequest(bypassSSL, getUrl(),
					"developer/api/v1/apps", null);
			if (getAliases.getStatus() == 200) {
				JsonArray jsonArray = getAliases.readEntity(JsonArray.class);
				listener.getLogger().println(jsonArray.toString());
				ArrayList<String> aliasArray = new ArrayList<String>();
				for (int i = 0; i < jsonArray.size(); i++) {
					JsonObject obj = jsonArray.getJsonObject(i);
					aliasArray.add(obj.getString("alias"));
				}
				return aliasArray;
			} else {
				listener.getLogger().println(
						"[APPRENDA] Failed to get application aliases. Response returned: " + getAliases.getStatus());
				listener.getLogger().println("[APPRENDA] Response Body: " + getAliases.readEntity(String.class));
				return null;
			}
		} catch (Exception e) {
			listener.getLogger().println(e.getMessage() + e);
			return null;
		}
	}*/
}
