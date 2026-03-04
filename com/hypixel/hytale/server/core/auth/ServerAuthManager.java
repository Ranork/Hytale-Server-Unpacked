package com.hypixel.hytale.server.core.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.auth.oauth.OAuthBrowserFlow;
import com.hypixel.hytale.server.core.auth.oauth.OAuthClient;
import com.hypixel.hytale.server.core.auth.oauth.OAuthDeviceFlow;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import joptsimple.OptionSet;

public class ServerAuthManager {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final int REFRESH_BUFFER_SECONDS = 300;
   private static volatile ServerAuthManager instance;
   private volatile ServerAuthManager.AuthMode authMode = ServerAuthManager.AuthMode.NONE;
   private volatile Instant tokenExpiry;
   private final AtomicReference<SessionServiceClient.GameSessionResponse> gameSession = new AtomicReference<>();
   private final AtomicReference<IAuthCredentialStore> credentialStore = new AtomicReference<>(new DefaultAuthCredentialStore());
   private final Map<UUID, SessionServiceClient.GameProfile> availableProfiles = new ConcurrentHashMap<>();
   private volatile SessionServiceClient.GameProfile[] pendingProfiles;
   private volatile ServerAuthManager.AuthMode pendingAuthMode;
   private final AtomicReference<X509Certificate> serverCertificate = new AtomicReference<>();
   private final UUID serverSessionId = UUID.randomUUID();
   private volatile boolean isSingleplayer;
   private OAuthClient oauthClient;
   private volatile SessionServiceClient sessionServiceClient;
   private volatile ProfileServiceClient profileServiceClient;
   private final ScheduledExecutorService refreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "TokenRefresh");
      t.setDaemon(true);
      return t;
   });
   private ScheduledFuture<?> refreshTask;
   private Runnable cancelActiveFlow;
   private volatile String pendingFatalError;

   private ServerAuthManager() {
   }

   public static ServerAuthManager getInstance() {
      if (instance == null) {
         synchronized (ServerAuthManager.class) {
            if (instance == null) {
               instance = new ServerAuthManager();
            }
         }
      }

      return instance;
   }

   @Nonnull
   public ProfileServiceClient getProfileServiceClient() {
      if (this.profileServiceClient == null) {
         synchronized (this) {
            if (this.profileServiceClient == null) {
               this.profileServiceClient = new ProfileServiceClient("https://account-data.hytale.com");
            }
         }
      }

      return this.profileServiceClient;
   }

   public void initialize() {
      OptionSet optionSet = Options.getOptionSet();
      if (optionSet == null) {
         LOGGER.at(Level.WARNING).log("Options not parsed, cannot initialize ServerAuthManager");
      } else {
         this.oauthClient = new OAuthClient();
         this.isSingleplayer = optionSet.has(Options.SINGLEPLAYER);
         if (this.isSingleplayer && optionSet.has(Options.OWNER_UUID)) {
            SessionServiceClient.GameProfile ownerProfile = new SessionServiceClient.GameProfile();
            ownerProfile.uuid = (UUID)optionSet.valueOf(Options.OWNER_UUID);
            ownerProfile.username = optionSet.has(Options.OWNER_NAME) ? (String)optionSet.valueOf(Options.OWNER_NAME) : null;
            this.credentialStore.get().setProfile(ownerProfile.uuid);
            LOGGER.at(Level.INFO).log("Singleplayer mode, owner: %s (%s)", ownerProfile.username, ownerProfile.uuid);
         }

         boolean hasCliTokens = false;
         String sessionTokenValue = null;
         String identityTokenValue = null;
         if (optionSet.has(Options.SESSION_TOKEN)) {
            sessionTokenValue = (String)optionSet.valueOf(Options.SESSION_TOKEN);
            LOGGER.at(Level.INFO).log("Session token loaded from CLI");
         } else {
            String envToken = System.getenv("HYTALE_SERVER_SESSION_TOKEN");
            if (envToken != null && !envToken.isEmpty()) {
               sessionTokenValue = envToken;
               LOGGER.at(Level.INFO).log("Session token loaded from environment");
            }
         }

         if (optionSet.has(Options.IDENTITY_TOKEN)) {
            identityTokenValue = (String)optionSet.valueOf(Options.IDENTITY_TOKEN);
            LOGGER.at(Level.INFO).log("Identity token loaded from CLI");
         } else {
            String envTokenx = System.getenv("HYTALE_SERVER_IDENTITY_TOKEN");
            if (envTokenx != null && !envTokenx.isEmpty()) {
               identityTokenValue = envTokenx;
               LOGGER.at(Level.INFO).log("Identity token loaded from environment");
            }
         }

         if (sessionTokenValue != null || identityTokenValue != null) {
            if (this.validateInitialTokens(sessionTokenValue, identityTokenValue)) {
               SessionServiceClient.GameSessionResponse session = new SessionServiceClient.GameSessionResponse();
               session.sessionToken = sessionTokenValue;
               session.identityToken = identityTokenValue;
               this.gameSession.set(session);
               hasCliTokens = true;
            } else {
               this.pendingFatalError = "Token validation failed. Provided tokens may be expired, tampered, or malformed. Remove invalid tokens or provide valid ones.";
               LOGGER.at(Level.SEVERE).log(this.pendingFatalError);
            }
         }

         if (hasCliTokens) {
            if (this.isSingleplayer) {
               this.authMode = ServerAuthManager.AuthMode.SINGLEPLAYER;
               LOGGER.at(Level.INFO).log("Auth mode: SINGLEPLAYER");
            } else {
               this.authMode = ServerAuthManager.AuthMode.EXTERNAL_SESSION;
               LOGGER.at(Level.INFO).log("Auth mode: EXTERNAL_SESSION");
            }

            this.parseAndScheduleRefresh();
         } else {
            LOGGER.at(Level.INFO).log("No server tokens configured. Use /auth login to authenticate, or provide tokens via CLI/environment.");
         }

         LOGGER.at(Level.INFO).log("Server session ID: %s", this.serverSessionId);
         LOGGER.at(Level.FINE)
            .log(
               "ServerAuthManager initialized - session token: %s, identity token: %s, auth mode: %s",
               this.hasSessionToken() ? "present" : "missing",
               this.hasIdentityToken() ? "present" : "missing",
               this.authMode
            );
      }
   }

   public void checkPendingFatalError() {
      if (this.pendingFatalError != null) {
         HytaleServer.get().shutdownServer(ShutdownReason.AUTH_FAILED.withMessage(this.pendingFatalError));
      }
   }

   public void initializeCredentialStore() {
      AuthCredentialStoreProvider provider = HytaleServer.get().getConfig().getAuthCredentialStoreProvider();
      this.credentialStore.set(provider.createStore());
      LOGGER.at(Level.INFO)
         .log("Auth credential store: %s", AuthCredentialStoreProvider.CODEC.getIdFor((Class<? extends AuthCredentialStoreProvider>)provider.getClass()));
      IAuthCredentialStore store = this.credentialStore.get();
      IAuthCredentialStore.OAuthTokens tokens = store.getTokens();
      if (tokens.isValid()) {
         LOGGER.at(Level.INFO).log("Found stored credentials, attempting to restore session...");
         ServerAuthManager.AuthResult result = this.createGameSessionFromOAuth(ServerAuthManager.AuthMode.OAUTH_STORE);
         if (result == ServerAuthManager.AuthResult.SUCCESS) {
            LOGGER.at(Level.INFO).log("Session restored from stored credentials");
         } else if (result == ServerAuthManager.AuthResult.PENDING_PROFILE_SELECTION) {
            LOGGER.at(Level.INFO).log("Session restored but profile selection required - use /auth select");
         } else {
            LOGGER.at(Level.WARNING).log("Failed to restore session from stored credentials");
         }
      }
   }

   public void shutdown() {
      this.cancelActiveFlow();
      if (this.refreshTask != null) {
         this.refreshTask.cancel(false);
      }

      this.refreshScheduler.shutdown();
      if (this.isSingleplayer()) {
         String currentSessionToken = this.getSessionToken();
         if (currentSessionToken != null && !currentSessionToken.isEmpty()) {
            if (this.sessionServiceClient == null) {
               this.sessionServiceClient = new SessionServiceClient("https://sessions.hytale.com");
            }

            this.sessionServiceClient.terminateSession(currentSessionToken);
         }
      }
   }

   public void logout() {
      this.cancelActiveFlow();
      if (this.refreshTask != null) {
         this.refreshTask.cancel(false);
         this.refreshTask = null;
      }

      this.gameSession.set(null);
      this.credentialStore.get().clear();
      this.availableProfiles.clear();
      this.pendingProfiles = null;
      this.pendingAuthMode = null;
      this.tokenExpiry = null;
      this.authMode = ServerAuthManager.AuthMode.NONE;
      LOGGER.at(Level.INFO).log("Server logged out");
   }

   @Nullable
   public SessionServiceClient.GameSessionResponse getGameSession() {
      return this.gameSession.get();
   }

   public void setGameSession(@Nonnull SessionServiceClient.GameSessionResponse session) {
      this.gameSession.set(session);
      LOGGER.at(Level.FINE).log("Game session updated");
   }

   @Nullable
   public String getIdentityToken() {
      SessionServiceClient.GameSessionResponse session = this.gameSession.get();
      return session != null ? session.identityToken : null;
   }

   @Nullable
   public String getSessionToken() {
      SessionServiceClient.GameSessionResponse session = this.gameSession.get();
      return session != null ? session.sessionToken : null;
   }

   public boolean hasIdentityToken() {
      SessionServiceClient.GameSessionResponse session = this.gameSession.get();
      return session != null && session.identityToken != null;
   }

   public boolean hasSessionToken() {
      SessionServiceClient.GameSessionResponse session = this.gameSession.get();
      return session != null && session.sessionToken != null;
   }

   @Nullable
   public String getOAuthAccessToken() {
      if (!this.refreshOAuthTokens()) {
         return null;
      } else {
         IAuthCredentialStore store = this.credentialStore.get();
         return store.getTokens().accessToken();
      }
   }

   public void setServerCertificate(@Nonnull X509Certificate certificate) {
      this.serverCertificate.set(certificate);
      LOGGER.at(Level.INFO).log("Server certificate set: %s", certificate.getSubjectX500Principal());
   }

   @Nullable
   public X509Certificate getServerCertificate() {
      return this.serverCertificate.get();
   }

   @Nullable
   public String getServerCertificateFingerprint() {
      X509Certificate cert = this.serverCertificate.get();
      return cert == null ? null : CertificateUtil.computeCertificateFingerprint(cert);
   }

   @Nonnull
   public UUID getServerSessionId() {
      return this.serverSessionId;
   }

   public ServerAuthManager.AuthMode getAuthMode() {
      return this.authMode;
   }

   public boolean isSingleplayer() {
      return this.isSingleplayer;
   }

   public boolean isOwner(@Nullable UUID playerUuid) {
      UUID profileUuid = this.credentialStore.get().getProfile();
      return profileUuid != null && profileUuid.equals(playerUuid);
   }

   @Nullable
   public SessionServiceClient.GameProfile getSelectedProfile() {
      UUID profileUuid = this.credentialStore.get().getProfile();
      return profileUuid == null ? null : this.availableProfiles.get(profileUuid);
   }

   @Nullable
   public Instant getTokenExpiry() {
      return this.tokenExpiry;
   }

   public String getAuthStatus() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.authMode.name());
      if (this.hasSessionToken() && this.hasIdentityToken()) {
         sb.append(" (authenticated)");
      } else if (!this.hasSessionToken() && !this.hasIdentityToken()) {
         sb.append(" (no tokens)");
      } else {
         sb.append(" (partial)");
      }

      if (this.tokenExpiry != null) {
         long secondsRemaining = this.tokenExpiry.getEpochSecond() - Instant.now().getEpochSecond();
         if (secondsRemaining > 0L) {
            sb.append(String.format(" [expires in %dm %ds]", secondsRemaining / 60L, secondsRemaining % 60L));
         } else {
            sb.append(" [EXPIRED]");
         }
      }

      return sb.toString();
   }

   public CompletableFuture<ServerAuthManager.AuthResult> startFlowAsync(@Nonnull OAuthBrowserFlow flow) {
      if (this.isSingleplayer) {
         return CompletableFuture.completedFuture(ServerAuthManager.AuthResult.FAILED);
      } else {
         this.cancelActiveFlow();
         this.cancelActiveFlow = this.oauthClient.startFlow(flow);
         return flow.getFuture()
            .thenApply(
               res -> {
                  switch (res) {
                     case SUCCESS:
                        IAuthCredentialStore store = this.credentialStore.get();
                        OAuthClient.TokenResponse tokens = flow.getTokenResponse();
                        store.setTokens(
                           new IAuthCredentialStore.OAuthTokens(tokens.accessToken(), tokens.refreshToken(), Instant.now().plusSeconds(tokens.expiresIn()))
                        );
                        return this.createGameSessionFromOAuth(ServerAuthManager.AuthMode.OAUTH_BROWSER);
                     case FAILED:
                        LOGGER.at(Level.WARNING).log("OAuth browser flow failed: %s", flow.getErrorMessage());
                        return ServerAuthManager.AuthResult.FAILED;
                     default:
                        LOGGER.at(Level.WARNING).log("OAuth browser flow completed with unexpected result: %v", res);
                        return ServerAuthManager.AuthResult.FAILED;
                  }
               }
            );
      }
   }

   public CompletableFuture<ServerAuthManager.AuthResult> startFlowAsync(OAuthDeviceFlow flow) {
      if (this.isSingleplayer) {
         return CompletableFuture.completedFuture(ServerAuthManager.AuthResult.FAILED);
      } else {
         this.cancelActiveFlow();
         this.cancelActiveFlow = this.oauthClient.startFlow(flow);
         return flow.getFuture()
            .thenApply(
               res -> {
                  switch (res) {
                     case SUCCESS:
                        IAuthCredentialStore store = this.credentialStore.get();
                        OAuthClient.TokenResponse tokens = flow.getTokenResponse();
                        store.setTokens(
                           new IAuthCredentialStore.OAuthTokens(tokens.accessToken(), tokens.refreshToken(), Instant.now().plusSeconds(tokens.expiresIn()))
                        );
                        return this.createGameSessionFromOAuth(ServerAuthManager.AuthMode.OAUTH_DEVICE);
                     case FAILED:
                        LOGGER.at(Level.WARNING).log("OAuth device flow failed: %s", flow.getErrorMessage());
                        return ServerAuthManager.AuthResult.FAILED;
                     default:
                        LOGGER.at(Level.WARNING).log("OAuth device flow completed with unexpected result: %v", res);
                        return ServerAuthManager.AuthResult.FAILED;
                  }
               }
            );
      }
   }

   public CompletableFuture<ServerAuthManager.AuthResult> registerCredentialStore(IAuthCredentialStore store) {
      if (this.isSingleplayer) {
         return CompletableFuture.completedFuture(ServerAuthManager.AuthResult.FAILED);
      } else if (this.hasSessionToken() && this.hasIdentityToken()) {
         return CompletableFuture.completedFuture(ServerAuthManager.AuthResult.FAILED);
      } else {
         this.credentialStore.set(store);
         return CompletableFuture.completedFuture(this.createGameSessionFromOAuth(ServerAuthManager.AuthMode.OAUTH_STORE));
      }
   }

   public void swapCredentialStoreProvider(@Nonnull AuthCredentialStoreProvider provider) {
      IAuthCredentialStore oldStore = this.credentialStore.get();
      IAuthCredentialStore newStore = provider.createStore();
      IAuthCredentialStore.OAuthTokens tokens = oldStore.getTokens();
      if (tokens.isValid()) {
         newStore.setTokens(tokens);
      }

      UUID profile = oldStore.getProfile();
      if (profile != null) {
         newStore.setProfile(profile);
      }

      this.credentialStore.set(newStore);
      LOGGER.at(Level.INFO).log("Swapped credential store to: %s", provider.getClass().getSimpleName());
   }

   public boolean cancelActiveFlow() {
      if (this.cancelActiveFlow != null) {
         this.cancelActiveFlow.run();
         this.cancelActiveFlow = null;
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public SessionServiceClient.GameProfile[] getPendingProfiles() {
      return this.pendingProfiles;
   }

   public boolean hasPendingProfiles() {
      return this.pendingProfiles != null && this.pendingProfiles.length > 0;
   }

   public boolean selectPendingProfile(int index) {
      SessionServiceClient.GameProfile[] profiles = this.pendingProfiles;
      ServerAuthManager.AuthMode mode = this.pendingAuthMode;
      if (profiles == null || profiles.length == 0) {
         LOGGER.at(Level.WARNING).log("No pending profiles to select");
         return false;
      } else if (index >= 1 && index <= profiles.length) {
         SessionServiceClient.GameProfile selected = profiles[index - 1];
         LOGGER.at(Level.INFO).log("Selected profile: %s (%s)", selected.username, selected.uuid);
         return this.completeAuthWithProfile(selected, mode != null ? mode : ServerAuthManager.AuthMode.OAUTH_BROWSER);
      } else {
         LOGGER.at(Level.WARNING).log("Invalid profile index: %d (valid range: 1-%d)", index, profiles.length);
         return false;
      }
   }

   public boolean selectPendingProfileByUsername(String username) {
      SessionServiceClient.GameProfile[] profiles = this.pendingProfiles;
      ServerAuthManager.AuthMode mode = this.pendingAuthMode;
      if (profiles != null && profiles.length != 0) {
         for (SessionServiceClient.GameProfile profile : profiles) {
            if (profile.username != null && profile.username.equalsIgnoreCase(username)) {
               LOGGER.at(Level.INFO).log("Selected profile: %s (%s)", profile.username, profile.uuid);
               return this.completeAuthWithProfile(profile, mode != null ? mode : ServerAuthManager.AuthMode.OAUTH_BROWSER);
            }
         }

         LOGGER.at(Level.WARNING).log("No profile found with username: %s", username);
         return false;
      } else {
         LOGGER.at(Level.WARNING).log("No pending profiles to select");
         return false;
      }
   }

   public void clearPendingProfiles() {
      this.pendingProfiles = null;
      this.pendingAuthMode = null;
   }

   private boolean validateInitialTokens(@Nullable String sessionToken, @Nullable String identityToken) {
      if (sessionToken == null && identityToken == null) {
         return false;
      } else {
         if (this.sessionServiceClient == null) {
            this.sessionServiceClient = new SessionServiceClient("https://sessions.hytale.com");
         }

         JWTValidator validator = new JWTValidator(this.sessionServiceClient, "https://sessions.hytale.com", "");
         boolean valid = true;
         OptionSet optionSet = Options.getOptionSet();
         UUID expectedOwnerUuid = optionSet != null && optionSet.has(Options.OWNER_UUID) ? (UUID)optionSet.valueOf(Options.OWNER_UUID) : null;
         String expectedOwnerName = optionSet != null && optionSet.has(Options.OWNER_NAME) ? (String)optionSet.valueOf(Options.OWNER_NAME) : null;
         if (identityToken != null) {
            JWTValidator.IdentityTokenClaims claims = validator.validateIdentityToken(identityToken);
            if (claims == null) {
               LOGGER.at(Level.WARNING).log("Identity token validation failed");
               valid = false;
            } else if (!claims.hasScope("hytale:server")) {
               LOGGER.at(Level.WARNING).log("Identity token missing required scope: expected %s, got %s", "hytale:server", claims.scope);
               valid = false;
            } else {
               if (expectedOwnerUuid != null) {
                  UUID tokenUuid = claims.getSubjectAsUUID();
                  if (tokenUuid == null || !tokenUuid.equals(expectedOwnerUuid)) {
                     LOGGER.at(Level.WARNING).log("Identity token UUID mismatch: token has %s, expected %s", claims.subject, expectedOwnerUuid);
                     valid = false;
                  }
               }

               if (expectedOwnerName != null && claims.username != null && !claims.username.equals(expectedOwnerName)) {
                  LOGGER.at(Level.WARNING).log("Identity token username mismatch: token has '%s', expected '%s'", claims.username, expectedOwnerName);
                  valid = false;
               }

               if (valid) {
                  LOGGER.at(Level.INFO).log("Identity token validated for %s (%s)", claims.username, claims.subject);
               }
            }
         }

         if (sessionToken != null) {
            JWTValidator.SessionTokenClaims claims = validator.validateSessionToken(sessionToken);
            if (claims == null) {
               LOGGER.at(Level.WARNING).log("Session token validation failed");
               valid = false;
            } else {
               if (expectedOwnerUuid != null) {
                  UUID tokenUuid = claims.getSubjectAsUUID();
                  if (tokenUuid == null || !tokenUuid.equals(expectedOwnerUuid)) {
                     LOGGER.at(Level.WARNING).log("Session token UUID mismatch: token has %s, expected %s", claims.subject, expectedOwnerUuid);
                     valid = false;
                  }
               }

               if (valid) {
                  LOGGER.at(Level.INFO).log("Session token validated");
               }
            }
         }

         return valid;
      }
   }

   private ServerAuthManager.AuthResult createGameSessionFromOAuth(ServerAuthManager.AuthMode mode) {
      if (!this.refreshOAuthTokens()) {
         LOGGER.at(Level.WARNING).log("No valid OAuth tokens to create game session");
         return ServerAuthManager.AuthResult.FAILED;
      } else {
         IAuthCredentialStore store = this.credentialStore.get();
         String accessToken = store.getTokens().accessToken();
         if (accessToken == null) {
            LOGGER.at(Level.WARNING).log("No access token in credential store");
            return ServerAuthManager.AuthResult.FAILED;
         } else {
            if (this.sessionServiceClient == null) {
               this.sessionServiceClient = new SessionServiceClient("https://sessions.hytale.com");
            }

            SessionServiceClient.GameProfile[] profiles = this.sessionServiceClient.getGameProfiles(accessToken);
            if (profiles != null && profiles.length != 0) {
               this.availableProfiles.clear();

               for (SessionServiceClient.GameProfile profile : profiles) {
                  this.availableProfiles.put(profile.uuid, profile);
               }

               SessionServiceClient.GameProfile profile = this.tryAutoSelectProfile(profiles);
               if (profile != null) {
                  return this.completeAuthWithProfile(profile, mode) ? ServerAuthManager.AuthResult.SUCCESS : ServerAuthManager.AuthResult.FAILED;
               } else {
                  this.pendingProfiles = profiles;
                  this.pendingAuthMode = mode;
                  this.cancelActiveFlow = null;
                  LOGGER.at(Level.INFO).log("Multiple profiles available. Use '/auth select <number>' to choose:");

                  for (int i = 0; i < profiles.length; i++) {
                     LOGGER.at(Level.INFO).log("  [%d] %s (%s)", i + 1, profiles[i].username, profiles[i].uuid);
                  }

                  return ServerAuthManager.AuthResult.PENDING_PROFILE_SELECTION;
               }
            } else {
               LOGGER.at(Level.WARNING).log("No game profiles found for this account");
               return ServerAuthManager.AuthResult.FAILED;
            }
         }
      }
   }

   private boolean refreshOAuthTokens() {
      return this.refreshOAuthTokens(false);
   }

   private boolean refreshOAuthTokens(boolean force) {
      IAuthCredentialStore store = this.credentialStore.get();
      IAuthCredentialStore.OAuthTokens tokens = store.getTokens();
      Instant expiresAt = tokens.accessTokenExpiresAt();
      if (!force && expiresAt != null && !expiresAt.isBefore(Instant.now().plusSeconds(300L))) {
         return true;
      } else {
         String refreshToken = tokens.refreshToken();
         if (refreshToken == null) {
            LOGGER.at(Level.WARNING).log("No refresh token present to refresh OAuth tokens");
            return false;
         } else {
            LOGGER.at(Level.INFO).log("Refreshing OAuth tokens...");
            OAuthClient.TokenResponse newTokens = this.oauthClient.refreshTokens(refreshToken);
            if (newTokens != null && newTokens.isSuccess()) {
               store.setTokens(
                  new IAuthCredentialStore.OAuthTokens(newTokens.accessToken(), newTokens.refreshToken(), Instant.now().plusSeconds(newTokens.expiresIn()))
               );
               return true;
            } else {
               LOGGER.at(Level.WARNING).log("OAuth token refresh failed");
               return false;
            }
         }
      }
   }

   @Nullable
   private SessionServiceClient.GameProfile tryAutoSelectProfile(SessionServiceClient.GameProfile[] profiles) {
      OptionSet optionSet = Options.getOptionSet();
      if (optionSet != null && optionSet.has(Options.OWNER_UUID)) {
         UUID requestedUuid = (UUID)optionSet.valueOf(Options.OWNER_UUID);

         for (SessionServiceClient.GameProfile profile : profiles) {
            if (profile.uuid.equals(requestedUuid)) {
               LOGGER.at(Level.INFO).log("Selected profile from --owner-uuid: %s (%s)", profile.username, profile.uuid);
               return profile;
            }
         }

         LOGGER.at(Level.WARNING).log("Specified --owner-uuid %s not found in available profiles", requestedUuid);
         return null;
      } else if (profiles.length == 1) {
         LOGGER.at(Level.INFO).log("Auto-selected profile: %s (%s)", profiles[0].username, profiles[0].uuid);
         return profiles[0];
      } else {
         UUID profileUuid = this.credentialStore.get().getProfile();
         if (profileUuid != null) {
            for (SessionServiceClient.GameProfile profilex : profiles) {
               if (profilex.uuid.equals(profileUuid)) {
                  LOGGER.at(Level.INFO).log("Auto-selected profile from storage: %s (%s)", profilex.username, profilex.uuid);
                  return profilex;
               }
            }
         }

         return null;
      }
   }

   private boolean completeAuthWithProfile(SessionServiceClient.GameProfile profile, ServerAuthManager.AuthMode mode) {
      SessionServiceClient.GameSessionResponse newSession = this.createGameSession(profile.uuid);
      if (newSession == null) {
         LOGGER.at(Level.WARNING).log("Failed to create game session");
         return false;
      } else {
         this.gameSession.set(newSession);
         this.authMode = mode;
         this.cancelActiveFlow = null;
         this.pendingProfiles = null;
         this.pendingAuthMode = null;
         Instant effectiveExpiry = this.getEffectiveExpiry(newSession);
         if (effectiveExpiry != null) {
            this.setExpiryAndScheduleRefresh(effectiveExpiry);
         }

         LOGGER.at(Level.INFO).log("Authentication successful! Mode: %s", mode);
         return true;
      }
   }

   @Nullable
   private SessionServiceClient.GameSessionResponse createGameSession(UUID profileUuid) {
      if (this.sessionServiceClient == null) {
         this.sessionServiceClient = new SessionServiceClient("https://sessions.hytale.com");
      }

      if (!this.refreshOAuthTokens()) {
         LOGGER.at(Level.WARNING).log("OAuth token refresh for game session creation failed");
         return null;
      } else {
         IAuthCredentialStore store = this.credentialStore.get();
         String accessToken = store.getTokens().accessToken();
         SessionServiceClient.GameSessionResponse result = this.sessionServiceClient.createGameSession(accessToken, profileUuid);
         if (result == null) {
            LOGGER.at(Level.WARNING).log("Trying force refresh of OAuth tokens because game session creation failed");
            if (!this.refreshOAuthTokens(true)) {
               LOGGER.at(Level.WARNING).log("Force refresh failed");
               return null;
            }

            result = this.sessionServiceClient.createGameSession(accessToken, profileUuid);
            if (result == null) {
               LOGGER.at(Level.WARNING).log("Game session creation with force refreshed tokens failed");
               return null;
            }
         }

         store.setProfile(profileUuid);
         return result;
      }
   }

   private void parseAndScheduleRefresh() {
      SessionServiceClient.GameSessionResponse session = this.gameSession.get();
      Instant effectiveExpiry = this.getEffectiveExpiry(session);
      if (effectiveExpiry != null) {
         this.setExpiryAndScheduleRefresh(effectiveExpiry);
      }
   }

   @Nullable
   private Instant getEffectiveExpiry(@Nullable SessionServiceClient.GameSessionResponse session) {
      Instant sessionExpiry = session != null ? session.getExpiresAtInstant() : null;
      Instant identityExpiry = this.parseIdentityTokenExpiry(session != null ? session.identityToken : this.getIdentityToken());
      if (sessionExpiry != null && identityExpiry != null) {
         return sessionExpiry.isBefore(identityExpiry) ? sessionExpiry : identityExpiry;
      } else {
         return sessionExpiry != null ? sessionExpiry : identityExpiry;
      }
   }

   @Nullable
   private Instant parseIdentityTokenExpiry(@Nullable String idToken) {
      if (idToken == null) {
         return null;
      } else {
         try {
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
               return null;
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
            if (json.has("exp")) {
               return Instant.ofEpochSecond(json.get("exp").getAsLong());
            }
         } catch (Exception var5) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var5)).log("Failed to parse identity token expiry");
         }

         return null;
      }
   }

   private void setExpiryAndScheduleRefresh(@Nonnull Instant expiry) {
      this.tokenExpiry = expiry;
      if (this.refreshTask != null) {
         this.refreshTask.cancel(false);
      }

      long secondsUntilExpiry = expiry.getEpochSecond() - Instant.now().getEpochSecond();
      if (secondsUntilExpiry > 300L) {
         long refreshDelay = Math.max(secondsUntilExpiry - 300L, 60L);
         LOGGER.at(Level.INFO).log("Token refresh scheduled in %d seconds", refreshDelay);
         this.refreshTask = this.refreshScheduler.schedule(this::doRefresh, refreshDelay, TimeUnit.SECONDS);
      }
   }

   private void doRefresh() {
      String currentSessionToken = this.getSessionToken();
      if (currentSessionToken == null || !this.refreshGameSession(currentSessionToken)) {
         LOGGER.at(Level.INFO).log("Game session refresh failed, attempting OAuth refresh...");
         if (!this.refreshGameSessionViaOAuth()) {
            LOGGER.at(Level.WARNING).log("All refresh attempts failed. Server may lose authentication.");
         }
      }
   }

   private boolean refreshGameSession(String currentSessionToken) {
      LOGGER.at(Level.INFO).log("Refreshing game session with Session Service...");
      if (this.sessionServiceClient == null) {
         this.sessionServiceClient = new SessionServiceClient("https://sessions.hytale.com");
      }

      try {
         SessionServiceClient.GameSessionResponse response = this.sessionServiceClient.refreshSessionAsync(currentSessionToken).join();
         if (response != null) {
            this.gameSession.set(response);
            Instant effectiveExpiry = this.getEffectiveExpiry(response);
            if (effectiveExpiry != null) {
               this.setExpiryAndScheduleRefresh(effectiveExpiry);
            }

            LOGGER.at(Level.INFO).log("Game session refresh successful");
            return true;
         }
      } catch (Exception var4) {
         LOGGER.at(Level.WARNING).log("Session Service refresh failed: %s", var4.getMessage());
      }

      return false;
   }

   private boolean refreshGameSessionViaOAuth() {
      boolean supported = switch (this.authMode) {
         case OAUTH_BROWSER, OAUTH_DEVICE, OAUTH_STORE -> true;
         default -> false;
      };
      if (!supported) {
         LOGGER.at(Level.WARNING).log("Refresh via OAuth not supported for current Auth Mode");
         return false;
      } else {
         UUID currentProfile = this.credentialStore.get().getProfile();
         if (currentProfile == null) {
            LOGGER.at(Level.WARNING).log("No current profile, cannot refresh game session");
            return false;
         } else {
            SessionServiceClient.GameSessionResponse newSession = this.createGameSession(currentProfile);
            if (newSession == null) {
               LOGGER.at(Level.WARNING).log("Failed to create new game session");
               return false;
            } else {
               this.gameSession.set(newSession);
               Instant effectiveExpiry = this.getEffectiveExpiry(newSession);
               if (effectiveExpiry != null) {
                  this.setExpiryAndScheduleRefresh(effectiveExpiry);
               }

               LOGGER.at(Level.INFO).log("New game session created via OAuth refresh");
               return true;
            }
         }
      }
   }

   static {
      AuthCredentialStoreProvider.CODEC.register(Priority.DEFAULT, "Memory", MemoryAuthCredentialStoreProvider.class, MemoryAuthCredentialStoreProvider.CODEC);
      AuthCredentialStoreProvider.CODEC.register("Encrypted", EncryptedAuthCredentialStoreProvider.class, EncryptedAuthCredentialStoreProvider.CODEC);
   }

   public static enum AuthMode {
      NONE,
      SINGLEPLAYER,
      EXTERNAL_SESSION,
      OAUTH_BROWSER,
      OAUTH_DEVICE,
      OAUTH_STORE;
   }

   public static enum AuthResult {
      SUCCESS,
      PENDING_PROFILE_SELECTION,
      FAILED;
   }
}
