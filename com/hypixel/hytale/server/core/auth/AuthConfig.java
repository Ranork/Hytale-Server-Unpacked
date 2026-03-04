package com.hypixel.hytale.server.core.auth;

import com.hypixel.hytale.common.util.java.ManifestUtil;
import java.time.Duration;
import javax.annotation.Nonnull;

public class AuthConfig {
   public static final String USER_AGENT = "HytaleServer/" + ManifestUtil.getImplementationVersion();
   public static final String OAUTH_AUTH_URL = "https://oauth.accounts.hytale.com/oauth2/auth";
   public static final String OAUTH_TOKEN_URL = "https://oauth.accounts.hytale.com/oauth2/token";
   public static final String DEVICE_AUTH_URL = "https://oauth.accounts.hytale.com/oauth2/device/auth";
   public static final String CONSENT_REDIRECT_URL = "https://accounts.hytale.com/consent/client";
   public static final String SESSION_SERVICE_URL = "https://sessions.hytale.com";
   public static final String ACCOUNT_DATA_URL = "https://account-data.hytale.com";
   public static final String BUILD_ENVIRONMENT = "release";
   public static final String CLIENT_ID = "hytale-server";
   public static final String[] SCOPES = new String[]{"openid", "offline", "auth:server"};
   public static final String SCOPE_CLIENT = "hytale:client";
   public static final String SCOPE_SERVER = "hytale:server";
   public static final String SCOPE_EDITOR = "hytale:editor";
   public static final Duration HTTP_TIMEOUT = Duration.ofSeconds(30L);
   public static final int DEVICE_POLL_INTERVAL_SECONDS = 15;
   public static final String ENV_SERVER_AUDIENCE = "HYTALE_SERVER_AUDIENCE";
   public static final String ENV_SERVER_IDENTITY_TOKEN = "HYTALE_SERVER_IDENTITY_TOKEN";
   public static final String ENV_SERVER_SESSION_TOKEN = "HYTALE_SERVER_SESSION_TOKEN";
   private static final String SERVER_AUDIENCE_OVERRIDE = System.getenv("HYTALE_SERVER_AUDIENCE");

   @Nonnull
   public static String getServerAudience() {
      return SERVER_AUDIENCE_OVERRIDE != null ? SERVER_AUDIENCE_OVERRIDE : ServerAuthManager.getInstance().getServerSessionId().toString();
   }

   private AuthConfig() {
   }
}
