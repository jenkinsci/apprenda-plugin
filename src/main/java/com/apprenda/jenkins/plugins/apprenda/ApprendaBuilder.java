package com.apprenda.jenkins.plugins.apprenda;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.servlet.ServletException;

import org.jenkinsci.remoting.RoleChecker;
import org.jfree.util.Log;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.XmlFile;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

/*
    ApprendaBuilder.java

    This is one of the main classes in this module, as it acts as the view controller between what's displayed on the screen during Jenkins build configuration.
    It also is the controller that executes the "Deploy to Apprenda" stage of a build.
 */
public class ApprendaBuilder extends Builder implements SimpleBuildStep, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public final String credentialsId;
	public final String appAlias;
	public final String appName;
	public String advVersionAliasToBeForced;
	public final String stage;
	public final String artifactName;
	public final String prefix;
	public final String archiveUploadMethod;
	public final boolean forceNewVersion;
	public final String customPackageDirectory;
	public final boolean advIsForcingSpecificVersion;
	public String applicationPackageURL;

	private static Logger logger = Logger.getLogger("jenkins.plugins.apprenda");
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	@DataBoundConstructor
	public ApprendaBuilder(String appAlias, String appName, String versionAlias, String stage, String artifactName,
			String credentialsId, String prefix, String advVersionAliasToBeForced, String advancedNewVersionOption, String customPackageDirectory, String applicationPackageURL, String archiveUploadMethod) {
		this.appAlias = appAlias;
		this.appName = appName;
		this.advVersionAliasToBeForced = advVersionAliasToBeForced;
		this.advIsForcingSpecificVersion = advancedNewVersionOption.equals("Option_ForceSpecificVersion");
		this.stage = stage;
		this.artifactName = artifactName;
		this.credentialsId = credentialsId;
		this.prefix = prefix;
		this.forceNewVersion = advancedNewVersionOption.equals("Option_AlwaysNewVersion");
		this.customPackageDirectory = customPackageDirectory;
		this.applicationPackageURL = applicationPackageURL;
		this.archiveUploadMethod = archiveUploadMethod;
	}

	// this method is called when a build is kicked off. (SimpleBuildStep)
	@Override
	public void perform(Run<?, ?> run, final FilePath workspace, Launcher launcher, final TaskListener listener)
			throws InterruptedException, IOException {
		// Define what should be run on the slave for this build

		final ApprendaCredentials credentials = CredentialsMatchers.firstOrNull(
				CredentialsProvider.lookupCredentials(ApprendaCredentials.class, Jenkins.getInstance(), ACL.SYSTEM),
				CredentialsMatchers.withId(credentialsId));
		if (credentials == null) {
			throw new AbortException("[APPRENDA] ERROR: Please configure Jenkins credentials for Apprenda.");
		}

		final String url = credentials.getUrl();
		final boolean isBypassSSL = credentials.getbypassSSL();//getDescriptor().isBypassSSL();

		Callable<String, IOException> task = new Callable<String, IOException>() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public String call() throws IOException {
				// This code will run on the build slave

				try {
					listener.getLogger()
							.println("[APPRENDA] Begin build step: Deploying application to Apprenda. Create client against URL " + url + " with bypassSSL set to " + isBypassSSL);
					ApprendaClient ac = new ApprendaClient(url, isBypassSSL, listener);
					listener.getLogger().println("[APPRENDA] Authentication starting for " + credentials.getUsername());
					listener.getLogger().println("[APPRENDA] Tenant Alias: " + credentials.getTenant());
					// Begin by loading the credentials and authenticating
					// against
					// Apprenda
					ac.authenticate(credentials);
					JsonArray versions = ac.GetAppAliasVersions(appAlias);
					// iterate the JsonArray, here's how we are going to apply
					// the rules
					// if we find the right regex of the prefix (whether its v
					// or
					// otherwise), we check the stage that version
					// i/s in. if its in published, then we know we need to
					// create a new
					// version.
					// then after that, all we have to do is patch it to the
					// desired
					// stage. this is the easy part now.
					File app = null;
					if (archiveUploadMethod.equals("localUpload"))
					{
							app = getFile(workspace, artifactName, customPackageDirectory);
							applicationPackageURL = "";
					}

					if (advIsForcingSpecificVersion == true)
					{
						if (advVersionAliasToBeForced == null || advVersionAliasToBeForced.length() < 2)
						{
							throw new AbortException("[APPRENDA] When forcing the deployment to a specific version, the complete version should be filled in. Currently set as: " + advVersionAliasToBeForced);
						}
						listener.getLogger().println("[APPRENDA] Will attempt to force a specific version: " + advVersionAliasToBeForced);
					}
					else
					{
						// if the option to force a version alias is not checked, we are not going
						// to use this variable and set it to null
						advVersionAliasToBeForced = null;
					}

					if (versions == null)
					{
						listener.getLogger().println("[APPRENDA] Creating a brand new v1 application for alias " + appAlias + " at target stage " + stage);
						if (!ac.createApp(appAlias, appName, app, stage, applicationPackageURL))
							throw new AbortException("[APPRENDA] Apprenda application creation failed");
						return null;
					}
					else
					{
						String tempNewVersion = detectVersion(versions, ac);
						listener.getLogger().println("[APPRENDA] Patching application to " + tempNewVersion + " for alias " + appAlias + " at target stage " + stage);
						if (!ac.patchApp(appAlias, tempNewVersion, app, stage, applicationPackageURL))
							throw new AbortException("[APPRENDA] Apprenda application patching failed");
						return null;
					}

				} catch (SecurityException s) {
					listener.getLogger().println("[APPRENDA] Unable to authenticate: " + s.getMessage());
					throw new AbortException("[APPRENDA] Unable to authenticate: " + s.getMessage());
				} catch (IOException e) {
					listener.getLogger().println("[APPRENDA] ERROR: IOException: " + e.getMessage());
					throw new AbortException("[APPRENDA] ERROR: IOException" + e.getMessage());
				} catch (InterruptedException e) {
					listener.getLogger().println("[APPRENDA] ERROR: InterruptedException: " + e.getMessage());
					throw new AbortException("[APPRENDA] Interrupted" + e.getMessage());
				} catch (Exception e) {
					listener.getLogger().println(e.getMessage());
					logger.log(Level.SEVERE, e.getMessage(), e);
					throw new AbortException("Unknown exception: " + e);
				}
			}

			@Override
			public void checkRoles(RoleChecker arg0) throws SecurityException {
				// TODO Auto-generated method stub

			}
		};

		// Get a "channel" to the build machine and run the task there
		try {
			launcher.getChannel().call(task);
		} catch (IOException e) {
			listener.getLogger().println(e.getMessage());
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new AbortException("[APPRENDA] IO Exception: " + e.getMessage());
		} catch (InterruptedException e) {
			listener.getLogger().println(e.getMessage());
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new AbortException("[APPRENDA] Interrupted: " + e.getMessage());
		}

	}

	private String detectVersion(JsonArray versions, ApprendaClient ac) {
		// This is now the critical piece of the build.

		// We are going to detect which version of Apprenda we need to update.
		// Guildelines:
		// - we are operating on a version terminology of <prefix><buldnumber>,
		// ie. v1, v2, v3, etc.
		// - if we find a published version to be the most current, we are
		// creating a new version and deploying to sandbox
		// - if we find a sandbox version to be the most current, we'll patch
		// that version unless explicitly forced

		// Induction - start with the "0" case. If we don't find a more current
		// version for this application,
		// then we'll be patching the application's "v1" version.
		int versionNumber = 1;
		String tempNewVersion;
		boolean forcedVersionExists = false;
		Pattern pattern = Pattern.compile("\\d+");
		boolean highestVersionPublished = false;
		for (int i = 0; i < versions.size(); i++) {
			// get the version object and the alias
			JsonObject version = versions.getJsonObject(i);
			String alias = version.getString("alias");
			if (advVersionAliasToBeForced == null && alias.matches(prefix + "\\d+")) {
				Matcher matcher = pattern.matcher(alias);
				matcher.find();
				int temp = Integer.parseInt(alias.substring(matcher.start()));
				String versionStage = version.getString("stage");
				// if the version we are looking at is in published state or we
				// are forcing a new version regardless,
				// get the greater of the two numeric versions and increment it.
				// so if the current version we found is v4 and its
				// published...if we already have found a v5 in sandbox do
				// nothing.
				if (temp >= versionNumber) {
					// use case v5 published, current is v1 (first run)
					// if published set to v6, else set to v5
					versionNumber = temp;
					//
					if (versionStage.equals("Published")) {
						highestVersionPublished = true;
					} else {
						highestVersionPublished = false;
					}
				}
			} else if (advVersionAliasToBeForced != null && alias.matches(advVersionAliasToBeForced)){ //alias.matches(prefix + "\\d+")) {
				forcedVersionExists = true;
			}
		}
		// now that we've traversed all versions, its time to determine whether
		// or not to create a new app version
		if (advVersionAliasToBeForced != null) {
			if (!forcedVersionExists) {
				ac.newAppVersion(appAlias, advVersionAliasToBeForced);
			}
			tempNewVersion = advVersionAliasToBeForced;
		} else if (forceNewVersion || highestVersionPublished) {
			versionNumber++;
			tempNewVersion = prefix + versionNumber;
			ac.newAppVersion(appAlias, tempNewVersion);
		} else {
			tempNewVersion = prefix + versionNumber;
		}
		return tempNewVersion;
	}

	private File getFile(FilePath workspace, String artifactName, String customWorkingPath)
			throws IOException, InterruptedException {
		try {
			if (customWorkingPath == null) {
				FilePath appPath = new FilePath(workspace, artifactName);
				return new File(appPath.toURI());
			} else {
				File workingPath = new File(customWorkingPath);
				if (workingPath.isAbsolute())
					return new File(workingPath, artifactName);
				else {
					File directory = new File(workspace.toURI());
					File appPath = new File(directory, customWorkingPath);
					return new File(appPath, artifactName);
				}
			}
		} catch (IOException e) {
			throw new IOException("IOException occurred getting package for deployment", e);
		} catch (InterruptedException e) {
			throw new InterruptedException("InterruptedException thrown while getting package for deployment");
		}
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return DESCRIPTOR;
	}

	// This class contains all of the UI validation methods.
	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
		//private boolean bypassSSL;

		public DescriptorImpl(){
	        load();
	    }
		//public boolean isBypassSSL() {
			//return bypassSSL;
		//}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			//bypassSSL = formData.getBoolean("bypassSSL");
			save();
			return super.configure(req, formData);
		}

		public FormValidation doCheckAppAlias(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please specify the Application Alias");
			return FormValidation.ok();
		}

		public FormValidation doCheckAppName(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please specify the Application Name");
			return FormValidation.ok();
		}

		public FormValidation doCheckPrefix(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please specify the Version Prefix");
			return FormValidation.ok();
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		public String getDisplayName() {
			return "Deploy to Apprenda";
		}

		public ListBoxModel doFillStageItems(@QueryParameter String stage) {

			StandardListBoxModel items = new StandardListBoxModel();
			items.withEmptySelection();
			items.add(new ListBoxModel.Option("Published", "published", stage.matches("published")));
			items.add(new ListBoxModel.Option("Sandbox", "sandbox", stage.matches("sandbox")));
			items.add(new ListBoxModel.Option("Definition", "definition", stage.matches("definition")));

			return items;
		}

		public ListBoxModel doFillCredentialsIdItems(@AncestorInPath ItemGroup context) {
			StandardListBoxModel list = new StandardListBoxModel();
			list.withEmptySelection();
			list.withMatching(CredentialsMatchers.instanceOf(ApprendaCredentials.class),
					CredentialsProvider.lookupCredentials(ApprendaCredentials.class, context, ACL.SYSTEM));
			return list;
		}

		// Tabling for Phase II.
		/*
		 * public ListBoxModel doFillAppAliasItems() { ListBoxModel model = new
		 * ListBoxModel(); for(int i=0; i<aliases.size(); i++) { // setting the
		 * name and the value to be the same. logger.log(Level.INFO,
		 * "In for loop: " + aliases.get(i)); model.add(new
		 * ListBoxModel.Option(aliases.get(i), aliases.get(i))); } return model;
		 * }
		 */

		// // BAC build only.
		// // this field validates the credentials supplied in the UI and then
		// stores them in an encrypted fashion.
		// public FormValidation doTestCredentials(@QueryParameter("username")
		// final String username, @QueryParameter("password") final String
		// password,
		// @QueryParameter("tenantAlias") final String tenantAlias)
		// {
		// if(username == null) return FormValidation.error("Username cannot be
		// empty.");
		// if(password == null) return FormValidation.error("Password cannot be
		// empty.");
		// if(tenantAlias == null) return FormValidation.error("Tenant Alias
		// cannot be empty");
		// try
		// {
		// // if form validation works, then we invoke the constructor which
		// encrypts them
		// ApprendaCredentials credentials = new ApprendaCredentials(username,
		// password, tenantAlias, getCredentialsLocation());
		// if(credentials == null) return FormValidation.error("Error creating
		// ApprendaCredentials object.");
		// logger.log(Level.INFO, "getUrl() is returning: " + getUrl());
		// ApprendaClient client = new ApprendaClient(getUrl(), bypassSSL,
		// getCredentialsLocation());
		// client.authenticate(username);
		// return FormValidation.ok("Validation successful.");
		// }
		// catch(Exception e)
		// {
		// return FormValidation.error("Failed: " + e.getMessage());
		// }
		// }

		// Tabled for Phase II.
		/*
		 * // This method reloads the application aliases associated with the
		 * credentials. public void
		 * doReloadAppAliases(@QueryParameter("username") final String username)
		 * { try {
		 *
		 * aliases = new ApprendaClient(getUrl(),
		 * getSSLFlag()).GetAppAliases(username); logger.log(Level.INFO,
		 * "Retrieved aliases, now populating listbox"); doFillAppAliasItems();
		 * logger.log(Level.INFO, "Repopulation complete."); save(); load(); }
		 * catch (Exception e) { logger.log(Level.SEVERE, e.getMessage(), e); }
		 * }
		 */
	}

}
