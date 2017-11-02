package com.apprenda.jenkins.plugins.apprenda;

import org.kohsuke.stapler.DataBoundConstructor;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;

@NameWith(value = ApprendaCredentials.NameProvider.class, priority = 8)
public class ApprendaCredentials extends BaseStandardCredentials implements StandardUsernamePasswordCredentials {

	private static final long serialVersionUID = 1L;

	protected final String tenant;
	protected final String username;
	protected final Secret password;
	protected final String url;
	protected final boolean bypassSSL;

	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	// @DataBoundConstructor
	// public ApprendaCredentials(String username, String password, String url,
	// String description) {
	// super(username, description);
	// this.username = username;
	// this.password = Secret.fromString(password);
	// this.url = url;
	// }

	@DataBoundConstructor
	public ApprendaCredentials(@CheckForNull CredentialsScope scope, @CheckForNull String id,
			@CheckForNull String tenant, @CheckForNull String username, @CheckForNull String password,
			@CheckForNull String url, @CheckForNull boolean bypassSSL, String description) {
		super(scope, id, description);
		this.tenant = tenant;
		this.username = username;
		this.password = Secret.fromString(password);
		this.url = url;
		this.bypassSSL = bypassSSL;
	}

	public String getTenant() {
		return this.tenant;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public Secret getPassword() {
		return this.password;
	}

	public String getUrl() {
		return this.url;
	}

	public boolean getbypassSSL() {
		return this.bypassSSL;
	}

	@Extension
	public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

		@Override
		public String getDisplayName() {
			return "Apprenda Credentials";
		}

	}

	public static class NameProvider extends CredentialsNameProvider<ApprendaCredentials> {

		/**
		 * {@inheritDoc}
		 */
		@NonNull
		@Override
		public String getName(@NonNull ApprendaCredentials c) {
			return c.getUrl() + " " + c.getUsername() + " (" + c.getTenant() + ")";
		}
	}

}
