package utils;

import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;

import net.sf.json.JSONObject;

public class ApprendaRestUtility {
	private String ApprendaSessionToken;
	private Logger logger;

	public ApprendaRestUtility() {
		ApprendaSessionToken = "";
		logger = LogManager.getLogManager().getLogger("jenkins.plugins.apprenda");
	}

	public ApprendaRestUtility(String token) {
		ApprendaSessionToken = token;
		logger = LogManager.getLogManager().getLogger("jenkins.plugins.apprenda");
	}

	// there's an instance in which this method gets called without a session
	// token (authentication). so we test for both.
	public Response PostResponseRequest(boolean bypassSSL, String url, String path, JsonObject json) throws Exception {
		try {
			if (ApprendaSessionToken.length() > 0) {
				Response response = getClient(bypassSSL).target(url).path(path).request(MediaType.APPLICATION_JSON)
						.header("ApprendaSessionToken", ApprendaSessionToken).post(Entity.json(json));
				logger.log(Level.FINE, "Response: " + response);
				return response;
			} else {
				Response response = getClient(bypassSSL).target(url).path(path).request(MediaType.APPLICATION_JSON)
						.post(Entity.json(json));
				logger.log(Level.FINE, "Response: " + response);
				return response;
			}
		} catch (Exception e) {
			logger.severe("Error: " + e.getMessage());
			throw new Exception(e);
		}
	}

	public Response GetResponseRequest(boolean bypassSSL, String url, String path,
			List<Pair<String, String>> queryParams) throws Exception {
		logger.log(Level.INFO, "Starting get response for " + path);
		if (ApprendaSessionToken.length() == 0) {
			// no session token available, need to re-authenticate
			logger.log(Level.SEVERE, "apprenda session token missing, require reauthentication");
			return null;
		}
		logger.log(Level.INFO, "debug - bypassSSL: " + bypassSSL);
		logger.log(Level.INFO, "debug - url: " + url);
		logger.log(Level.INFO, "debug - path: " + path);
		WebTarget target = getClient(bypassSSL).target(url).path(path);
		if (queryParams != null) {
			for (Iterator<Pair<String, String>> i = queryParams.iterator(); i.hasNext();) {
				Pair<String, String> pair = i.next();
				target.queryParam(pair.getLeft(), pair.getRight());
			}
		}
		logger.log(Level.INFO, "Executing request.");
		Response response = target.request(MediaType.APPLICATION_JSON)
				.header("ApprendaSessionToken", ApprendaSessionToken).get();
		return response;
	}

	// Atypical Methods that do not fit the above flow.
	public Response PatchApplication(boolean bypassSSL, String url, String path, String stage, InputStream fileInStream)
			throws Exception {
		try {
			return getClient(bypassSSL).target(url).path(path).queryParam("action", "patch").queryParam("stage", stage)
					.request(MediaType.APPLICATION_JSON).header("ApprendaSessionToken", ApprendaSessionToken)
					.post(Entity.entity(fileInStream, MediaType.APPLICATION_OCTET_STREAM));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new Exception(e);
		}
	}

	public Response PatchApplication(boolean bypassSSL, String url, String path, String stage, String applicationPackageURL)
			throws Exception {
		try {
			return getClient(bypassSSL).target(url).path(path).queryParam("action", "patch").queryParam("stage", stage).queryParam("archiveUri", applicationPackageURL)
					.request(MediaType.APPLICATION_JSON).header("ApprendaSessionToken", ApprendaSessionToken)
					.post(Entity.json(""));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new Exception(e);
		}
	}

	public Response CreateApplication(boolean bypassSSL, String url, String path, String alias, String name) throws Exception {
		try {
			JSONObject json = new JSONObject();
			json.put("Name", name);
			json.put("Alias", alias);

			Client client = getClient(bypassSSL);
			WebTarget target = client.target(url).path(path);
			Builder builder = target.request(MediaType.APPLICATION_JSON)
					.header("ApprendaSessionToken", ApprendaSessionToken);
			logger.log(Level.INFO, "Request: " + json);
			return builder.post(Entity.json(json));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new Exception(e);
		}
	}

	// Begin private methods
	private Client getClient(boolean bypassSSL) {
		Client client;
		try {
			if (bypassSSL) {
				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
					@Override
					public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
							throws CertificateException {
					}

					@Override
					public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
							throws CertificateException {
					}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[0];
					}
				} };
				SSLContext context = SSLContext.getInstance("TLS");
				context.init(null, trustAllCerts, new SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
				HostnameVerifier verifier = new HostnameVerifier() {
					@Override
					public boolean verify(String s, SSLSession sslSession) {
						return true;
					}
				};
				client = ClientBuilder.newBuilder().sslContext(context).hostnameVerifier(verifier).build();
			} else {
				client = ClientBuilder.newClient();
			}
			return client;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
