package com.hypixel.hytale.server.core.auth;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.ServiceHttpClientFactory;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SessionServiceClient {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final ExecutorService HTTP_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
   private final HttpClient httpClient;
   private final String sessionServiceUrl;

   public SessionServiceClient(@Nonnull String sessionServiceUrl) {
      if (sessionServiceUrl != null && !sessionServiceUrl.isEmpty()) {
         this.sessionServiceUrl = sessionServiceUrl.endsWith("/") ? sessionServiceUrl.substring(0, sessionServiceUrl.length() - 1) : sessionServiceUrl;
         this.httpClient = ServiceHttpClientFactory.create(AuthConfig.HTTP_TIMEOUT);
         LOGGER.at(Level.INFO).log("Session Service client initialized for: %s", this.sessionServiceUrl);
      } else {
         throw new IllegalArgumentException("Session Service URL cannot be null or empty");
      }
   }

   public CompletableFuture<String> requestAuthorizationGrantAsync(@Nonnull String identityToken, @Nonnull String serverAudience, @Nonnull String bearerToken) {
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               String jsonBody = String.format("{\"identityToken\":\"%s\",\"aud\":\"%s\"}", escapeJsonString(identityToken), escapeJsonString(serverAudience));
               HttpRequest request = HttpRequest.newBuilder()
                  .uri(URI.create(this.sessionServiceUrl + "/server-join/auth-grant"))
                  .header("Content-Type", "application/json")
                  .header("Accept", "application/json")
                  .header("Authorization", "Bearer " + bearerToken)
                  .header("User-Agent", AuthConfig.USER_AGENT)
                  .timeout(AuthConfig.HTTP_TIMEOUT)
                  .POST(BodyPublishers.ofString(jsonBody))
                  .build();
               LOGGER.at(Level.INFO).log("Requesting authorization grant with identity token, aud='%s'", serverAudience);
               HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());
               if (response.statusCode() != 200) {
                  LOGGER.at(Level.WARNING).log("Failed to request authorization grant: HTTP %d - %s", response.statusCode(), response.body());
                  return null;
               } else {
                  SessionServiceClient.AuthGrantResponse authGrantResponse = SessionServiceClient.AuthGrantResponse.CODEC
                     .decodeJson(new RawJsonReader(response.body().toCharArray()), EmptyExtraInfo.EMPTY);
                  if (authGrantResponse != null && authGrantResponse.authorizationGrant != null) {
                     LOGGER.at(Level.INFO).log("Successfully obtained authorization grant");
                     return authGrantResponse.authorizationGrant;
                  } else {
                     LOGGER.at(Level.WARNING).log("Session Service response missing authorizationGrant field");
                     return null;
                  }
               }
            } catch (IOException var8) {
               LOGGER.at(Level.WARNING).log("IO error while requesting authorization grant: %s", var8.getMessage());
               return null;
            } catch (InterruptedException var9) {
               LOGGER.at(Level.WARNING).log("Request interrupted while obtaining authorization grant");
               Thread.currentThread().interrupt();
               return null;
            } catch (Exception var10) {
               LOGGER.at(Level.WARNING).log("Unexpected error requesting authorization grant: %s", var10.getMessage());
               return null;
            }
         },
         HTTP_EXECUTOR
      );
   }

   public CompletableFuture<String> exchangeAuthGrantForTokenAsync(
      @Nonnull String authorizationGrant, @Nonnull String x509Fingerprint, @Nonnull String bearerToken
   ) {
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               String jsonBody = String.format(
                  "{\"authorizationGrant\":\"%s\",\"x509Fingerprint\":\"%s\"}", escapeJsonString(authorizationGrant), escapeJsonString(x509Fingerprint)
               );
               HttpRequest request = HttpRequest.newBuilder()
                  .uri(URI.create(this.sessionServiceUrl + "/server-join/auth-token"))
                  .header("Content-Type", "application/json")
                  .header("Accept", "application/json")
                  .header("Authorization", "Bearer " + bearerToken)
                  .header("User-Agent", AuthConfig.USER_AGENT)
                  .timeout(AuthConfig.HTTP_TIMEOUT)
                  .POST(BodyPublishers.ofString(jsonBody))
                  .build();
               LOGGER.at(Level.INFO).log("Exchanging authorization grant for access token");
               LOGGER.at(Level.FINE).log("Using bearer token (first 20 chars): %s...", bearerToken.length() > 20 ? bearerToken.substring(0, 20) : bearerToken);
               LOGGER.at(Level.FINE).log("Request body: %s", jsonBody);
               HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());
               if (response.statusCode() != 200) {
                  LOGGER.at(Level.WARNING).log("Failed to exchange auth grant: HTTP %d - %s", response.statusCode(), response.body());
                  return null;
               } else {
                  SessionServiceClient.AccessTokenResponse tokenResponse = SessionServiceClient.AccessTokenResponse.CODEC
                     .decodeJson(new RawJsonReader(response.body().toCharArray()), EmptyExtraInfo.EMPTY);
                  if (tokenResponse != null && tokenResponse.accessToken != null) {
                     LOGGER.at(Level.INFO).log("Successfully obtained access token");
                     return tokenResponse.accessToken;
                  } else {
                     LOGGER.at(Level.WARNING).log("Session Service response missing accessToken field");
                     return null;
                  }
               }
            } catch (IOException var8) {
               LOGGER.at(Level.WARNING).log("IO error while exchanging auth grant: %s", var8.getMessage());
               return null;
            } catch (InterruptedException var9) {
               LOGGER.at(Level.WARNING).log("Request interrupted while exchanging auth grant");
               Thread.currentThread().interrupt();
               return null;
            } catch (Exception var10) {
               LOGGER.at(Level.WARNING).log("Unexpected error exchanging auth grant: %s", var10.getMessage());
               return null;
            }
         },
         HTTP_EXECUTOR
      );
   }

   @Nullable
   public SessionServiceClient.JwksResponse getJwks() {
      try {
         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(this.sessionServiceUrl + "/.well-known/jwks.json"))
            .header("Accept", "application/json")
            .header("User-Agent", AuthConfig.USER_AGENT)
            .timeout(AuthConfig.HTTP_TIMEOUT)
            .GET()
            .build();
         LOGGER.at(Level.FINE).log("Fetching JWKS from Session Service");
         HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());
         if (response.statusCode() != 200) {
            LOGGER.at(Level.WARNING).log("Failed to fetch JWKS: HTTP %d - %s", response.statusCode(), response.body());
            return null;
         } else {
            SessionServiceClient.JwksResponse jwks = SessionServiceClient.JwksResponse.CODEC
               .decodeJson(new RawJsonReader(response.body().toCharArray()), EmptyExtraInfo.EMPTY);
            if (jwks != null && jwks.keys != null && jwks.keys.length != 0) {
               LOGGER.at(Level.INFO).log("Successfully fetched JWKS with %d keys", jwks.keys.length);
               return jwks;
            } else {
               LOGGER.at(Level.WARNING).log("Session Service returned invalid JWKS (no keys)");
               return null;
            }
         }
      } catch (IOException var4) {
         LOGGER.at(Level.WARNING).log("IO error while fetching JWKS: %s", var4.getMessage());
         return null;
      } catch (InterruptedException var5) {
         LOGGER.at(Level.WARNING).log("Request interrupted while fetching JWKS");
         Thread.currentThread().interrupt();
         return null;
      } catch (Exception var6) {
         LOGGER.at(Level.WARNING).log("Unexpected error fetching JWKS: %s", var6.getMessage());
         return null;
      }
   }

   @Nullable
   public SessionServiceClient.GameProfile[] getGameProfiles(@Nonnull String oauthAccessToken) {
      try {
         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://account-data.hytale.com/my-account/get-profiles"))
            .header("Accept", "application/json")
            .header("Authorization", "Bearer " + oauthAccessToken)
            .header("User-Agent", AuthConfig.USER_AGENT)
            .timeout(AuthConfig.HTTP_TIMEOUT)
            .GET()
            .build();
         LOGGER.at(Level.INFO).log("Fetching game profiles...");
         HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());
         if (response.statusCode() != 200) {
            LOGGER.at(Level.WARNING).log("Failed to fetch profiles: HTTP %d - %s", response.statusCode(), response.body());
            return null;
         } else {
            SessionServiceClient.LauncherDataResponse data = SessionServiceClient.LauncherDataResponse.CODEC
               .decodeJson(new RawJsonReader(response.body().toCharArray()), EmptyExtraInfo.EMPTY);
            if (data != null && data.profiles != null) {
               LOGGER.at(Level.INFO).log("Found %d game profile(s)", data.profiles.length);
               return data.profiles;
            } else {
               LOGGER.at(Level.WARNING).log("Account Data returned invalid response");
               return null;
            }
         }
      } catch (IOException var5) {
         LOGGER.at(Level.WARNING).log("IO error while fetching profiles: %s", var5.getMessage());
         return null;
      } catch (InterruptedException var6) {
         LOGGER.at(Level.WARNING).log("Request interrupted while fetching profiles");
         Thread.currentThread().interrupt();
         return null;
      } catch (Exception var7) {
         LOGGER.at(Level.WARNING).log("Unexpected error fetching profiles: %s", var7.getMessage());
         return null;
      }
   }

   public SessionServiceClient.GameSessionResponse createGameSession(@Nonnull String oauthAccessToken, @Nonnull UUID profileUuid) {
      try {
         String body = String.format("{\"uuid\":\"%s\"}", profileUuid.toString());
         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(this.sessionServiceUrl + "/game-session/new"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + oauthAccessToken)
            .header("User-Agent", AuthConfig.USER_AGENT)
            .timeout(AuthConfig.HTTP_TIMEOUT)
            .POST(BodyPublishers.ofString(body))
            .build();
         LOGGER.at(Level.INFO).log("Creating game session...");
         HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());
         if (response.statusCode() != 200 && response.statusCode() != 201) {
            LOGGER.at(Level.WARNING).log("Failed to create game session: HTTP %d - %s", response.statusCode(), response.body());
            return null;
         } else {
            SessionServiceClient.GameSessionResponse sessionResponse = SessionServiceClient.GameSessionResponse.CODEC
               .decodeJson(new RawJsonReader(response.body().toCharArray()), EmptyExtraInfo.EMPTY);
            if (sessionResponse != null && sessionResponse.identityToken != null) {
               LOGGER.at(Level.INFO).log("Successfully created game session");
               return sessionResponse;
            } else {
               LOGGER.at(Level.WARNING).log("Session Service returned invalid response");
               return null;
            }
         }
      } catch (IOException var7) {
         LOGGER.at(Level.WARNING).log("IO error while creating session: %s", var7.getMessage());
         return null;
      } catch (InterruptedException var8) {
         LOGGER.at(Level.WARNING).log("Request interrupted while creating session");
         Thread.currentThread().interrupt();
         return null;
      } catch (Exception var9) {
         LOGGER.at(Level.WARNING).log("Unexpected error creating session: %s", var9.getMessage());
         return null;
      }
   }

   public CompletableFuture<SessionServiceClient.GameSessionResponse> refreshSessionAsync(@Nonnull String sessionToken) {
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               HttpRequest request = HttpRequest.newBuilder()
                  .uri(URI.create(this.sessionServiceUrl + "/game-session/refresh"))
                  .header("Accept", "application/json")
                  .header("Authorization", "Bearer " + sessionToken)
                  .header("User-Agent", AuthConfig.USER_AGENT)
                  .timeout(AuthConfig.HTTP_TIMEOUT)
                  .POST(BodyPublishers.noBody())
                  .build();
               LOGGER.at(Level.INFO).log("Refreshing game session...");
               HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());
               if (response.statusCode() != 200) {
                  LOGGER.at(Level.WARNING).log("Failed to refresh session: HTTP %d - %s", response.statusCode(), response.body());
                  return null;
               } else {
                  SessionServiceClient.GameSessionResponse sessionResponse = SessionServiceClient.GameSessionResponse.CODEC
                     .decodeJson(new RawJsonReader(response.body().toCharArray()), EmptyExtraInfo.EMPTY);
                  if (sessionResponse != null && sessionResponse.identityToken != null) {
                     LOGGER.at(Level.INFO).log("Successfully refreshed game session");
                     return sessionResponse;
                  } else {
                     LOGGER.at(Level.WARNING).log("Session Service returned invalid response (missing identity token)");
                     return null;
                  }
               }
            } catch (IOException var5) {
               LOGGER.at(Level.WARNING).log("IO error while refreshing session: %s", var5.getMessage());
               return null;
            } catch (InterruptedException var6) {
               LOGGER.at(Level.WARNING).log("Request interrupted while refreshing session");
               Thread.currentThread().interrupt();
               return null;
            } catch (Exception var7) {
               LOGGER.at(Level.WARNING).log("Unexpected error refreshing session: %s", var7.getMessage());
               return null;
            }
         },
         HTTP_EXECUTOR
      );
   }

   public void terminateSession(@Nonnull String sessionToken) {
      if (sessionToken != null && !sessionToken.isEmpty()) {
         try {
            HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create(this.sessionServiceUrl + "/game-session"))
               .header("Authorization", "Bearer " + sessionToken)
               .header("User-Agent", AuthConfig.USER_AGENT)
               .timeout(AuthConfig.HTTP_TIMEOUT)
               .DELETE()
               .build();
            LOGGER.at(Level.INFO).log("Terminating game session...");
            HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200 && response.statusCode() != 204) {
               LOGGER.at(Level.WARNING).log("Failed to terminate session: HTTP %d - %s", response.statusCode(), response.body());
            } else {
               LOGGER.at(Level.INFO).log("Game session terminated");
            }
         } catch (IOException var4) {
            LOGGER.at(Level.WARNING).log("IO error while terminating session: %s", var4.getMessage());
         } catch (InterruptedException var5) {
            LOGGER.at(Level.WARNING).log("Request interrupted while terminating session");
            Thread.currentThread().interrupt();
         } catch (Exception var6) {
            LOGGER.at(Level.WARNING).log("Error terminating session: %s", var6.getMessage());
         }
      }
   }

   private static String escapeJsonString(String value) {
      return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
   }

   private static <T> KeyedCodec<T> externalKey(String key, Codec<T> codec) {
      return new KeyedCodec<>(key, codec, false, true);
   }

   public static class AccessTokenResponse {
      public String accessToken;
      public static final BuilderCodec<SessionServiceClient.AccessTokenResponse> CODEC = BuilderCodec.builder(
            SessionServiceClient.AccessTokenResponse.class, SessionServiceClient.AccessTokenResponse::new
         )
         .append(SessionServiceClient.externalKey("accessToken", Codec.STRING), (r, v) -> r.accessToken = v, r -> r.accessToken)
         .add()
         .build();
   }

   public static class AuthGrantResponse {
      public String authorizationGrant;
      public static final BuilderCodec<SessionServiceClient.AuthGrantResponse> CODEC = BuilderCodec.builder(
            SessionServiceClient.AuthGrantResponse.class, SessionServiceClient.AuthGrantResponse::new
         )
         .append(SessionServiceClient.externalKey("authorizationGrant", Codec.STRING), (r, v) -> r.authorizationGrant = v, r -> r.authorizationGrant)
         .add()
         .build();
   }

   public static class GameProfile {
      public UUID uuid;
      public String username;
      public static final BuilderCodec<SessionServiceClient.GameProfile> CODEC = BuilderCodec.builder(
            SessionServiceClient.GameProfile.class, SessionServiceClient.GameProfile::new
         )
         .append(SessionServiceClient.externalKey("uuid", Codec.UUID_STRING), (p, v) -> p.uuid = v, p -> p.uuid)
         .add()
         .append(SessionServiceClient.externalKey("username", Codec.STRING), (p, v) -> p.username = v, p -> p.username)
         .add()
         .build();
   }

   public static class GameSessionResponse {
      public String sessionToken;
      public String identityToken;
      public String expiresAt;
      public static final BuilderCodec<SessionServiceClient.GameSessionResponse> CODEC = BuilderCodec.builder(
            SessionServiceClient.GameSessionResponse.class, SessionServiceClient.GameSessionResponse::new
         )
         .append(SessionServiceClient.externalKey("sessionToken", Codec.STRING), (r, v) -> r.sessionToken = v, r -> r.sessionToken)
         .add()
         .append(SessionServiceClient.externalKey("identityToken", Codec.STRING), (r, v) -> r.identityToken = v, r -> r.identityToken)
         .add()
         .append(SessionServiceClient.externalKey("expiresAt", Codec.STRING), (r, v) -> r.expiresAt = v, r -> r.expiresAt)
         .add()
         .build();

      public Instant getExpiresAtInstant() {
         if (this.expiresAt == null) {
            return null;
         } else {
            try {
               return Instant.parse(this.expiresAt);
            } catch (Exception var2) {
               return null;
            }
         }
      }
   }

   public static class JwkKey {
      public String kty;
      public String alg;
      public String use;
      public String kid;
      public String crv;
      public String x;
      public String y;
      public String n;
      public String e;
      public static final BuilderCodec<SessionServiceClient.JwkKey> CODEC = BuilderCodec.builder(
            SessionServiceClient.JwkKey.class, SessionServiceClient.JwkKey::new
         )
         .append(SessionServiceClient.externalKey("kty", Codec.STRING), (k, v) -> k.kty = v, k -> k.kty)
         .add()
         .append(SessionServiceClient.externalKey("alg", Codec.STRING), (k, v) -> k.alg = v, k -> k.alg)
         .add()
         .append(SessionServiceClient.externalKey("use", Codec.STRING), (k, v) -> k.use = v, k -> k.use)
         .add()
         .append(SessionServiceClient.externalKey("kid", Codec.STRING), (k, v) -> k.kid = v, k -> k.kid)
         .add()
         .append(SessionServiceClient.externalKey("crv", Codec.STRING), (k, v) -> k.crv = v, k -> k.crv)
         .add()
         .append(SessionServiceClient.externalKey("x", Codec.STRING), (k, v) -> k.x = v, k -> k.x)
         .add()
         .append(SessionServiceClient.externalKey("y", Codec.STRING), (k, v) -> k.y = v, k -> k.y)
         .add()
         .append(SessionServiceClient.externalKey("n", Codec.STRING), (k, v) -> k.n = v, k -> k.n)
         .add()
         .append(SessionServiceClient.externalKey("e", Codec.STRING), (k, v) -> k.e = v, k -> k.e)
         .add()
         .build();
   }

   public static class JwksResponse {
      public SessionServiceClient.JwkKey[] keys;
      public static final BuilderCodec<SessionServiceClient.JwksResponse> CODEC = BuilderCodec.builder(
            SessionServiceClient.JwksResponse.class, SessionServiceClient.JwksResponse::new
         )
         .append(
            SessionServiceClient.externalKey("keys", new ArrayCodec<>(SessionServiceClient.JwkKey.CODEC, SessionServiceClient.JwkKey[]::new)),
            (r, v) -> r.keys = v,
            r -> r.keys
         )
         .add()
         .build();
   }

   public static class LauncherDataResponse {
      public UUID owner;
      public SessionServiceClient.GameProfile[] profiles;
      public static final BuilderCodec<SessionServiceClient.LauncherDataResponse> CODEC = BuilderCodec.builder(
            SessionServiceClient.LauncherDataResponse.class, SessionServiceClient.LauncherDataResponse::new
         )
         .append(SessionServiceClient.externalKey("owner", Codec.UUID_STRING), (r, v) -> r.owner = v, r -> r.owner)
         .add()
         .append(
            SessionServiceClient.externalKey("profiles", new ArrayCodec<>(SessionServiceClient.GameProfile.CODEC, SessionServiceClient.GameProfile[]::new)),
            (r, v) -> r.profiles = v,
            r -> r.profiles
         )
         .add()
         .build();
   }
}
