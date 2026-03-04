package com.hypixel.hytale.server.core.auth;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.ServiceHttpClientFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ProfileServiceClient {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final HttpClient httpClient;
   private final String profileServiceUrl;

   public ProfileServiceClient(@Nonnull String profileServiceUrl) {
      if (profileServiceUrl != null && !profileServiceUrl.isEmpty()) {
         this.profileServiceUrl = profileServiceUrl.endsWith("/") ? profileServiceUrl.substring(0, profileServiceUrl.length() - 1) : profileServiceUrl;
         this.httpClient = ServiceHttpClientFactory.create(AuthConfig.HTTP_TIMEOUT);
         LOGGER.at(Level.INFO).log("Profile Service client initialized for: %s", this.profileServiceUrl);
      } else {
         throw new IllegalArgumentException("Profile Service URL cannot be null or empty");
      }
   }

   @Nullable
   public ProfileServiceClient.PublicGameProfile getProfileByUuid(@Nonnull UUID uuid, @Nonnull String bearerToken) {
      try {
         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(this.profileServiceUrl + "/profile/uuid/" + uuid.toString()))
            .header("Accept", "application/json")
            .header("Authorization", "Bearer " + bearerToken)
            .header("User-Agent", AuthConfig.USER_AGENT)
            .timeout(AuthConfig.HTTP_TIMEOUT)
            .GET()
            .build();
         LOGGER.at(Level.FINE).log("Fetching profile by UUID: %s", uuid);
         HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());
         if (response.statusCode() != 200) {
            LOGGER.at(Level.WARNING).log("Failed to fetch profile by UUID: HTTP %d - %s", response.statusCode(), response.body());
            return null;
         } else {
            ProfileServiceClient.PublicGameProfile profile = ProfileServiceClient.PublicGameProfile.CODEC
               .decodeJson(new RawJsonReader(response.body().toCharArray()), EmptyExtraInfo.EMPTY);
            if (profile == null) {
               LOGGER.at(Level.WARNING).log("Profile Service returned invalid response for UUID: %s", uuid);
               return null;
            } else {
               LOGGER.at(Level.FINE).log("Successfully fetched profile: %s (%s)", profile.getUsername(), profile.getUuid());
               return profile;
            }
         }
      } catch (IOException var6) {
         LOGGER.at(Level.WARNING).log("IO error while fetching profile by UUID: %s", var6.getMessage());
         return null;
      } catch (InterruptedException var7) {
         LOGGER.at(Level.WARNING).log("Request interrupted while fetching profile by UUID");
         Thread.currentThread().interrupt();
         return null;
      } catch (Exception var8) {
         LOGGER.at(Level.WARNING).log("Unexpected error fetching profile by UUID: %s", var8.getMessage());
         return null;
      }
   }

   public CompletableFuture<ProfileServiceClient.PublicGameProfile> getProfileByUuidAsync(@Nonnull UUID uuid, @Nonnull String bearerToken) {
      return CompletableFuture.supplyAsync(() -> this.getProfileByUuid(uuid, bearerToken));
   }

   @Nullable
   public ProfileServiceClient.PublicGameProfile getProfileByUsername(@Nonnull String username, @Nonnull String bearerToken) {
      try {
         String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(this.profileServiceUrl + "/profile/username/" + encodedUsername))
            .header("Accept", "application/json")
            .header("Authorization", "Bearer " + bearerToken)
            .header("User-Agent", AuthConfig.USER_AGENT)
            .timeout(AuthConfig.HTTP_TIMEOUT)
            .GET()
            .build();
         LOGGER.at(Level.FINE).log("Fetching profile by username: %s", username);
         HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());
         if (response.statusCode() != 200) {
            LOGGER.at(Level.WARNING).log("Failed to fetch profile by username: HTTP %d - %s", response.statusCode(), response.body());
            return null;
         } else {
            ProfileServiceClient.PublicGameProfile profile = ProfileServiceClient.PublicGameProfile.CODEC
               .decodeJson(new RawJsonReader(response.body().toCharArray()), EmptyExtraInfo.EMPTY);
            if (profile == null) {
               LOGGER.at(Level.WARNING).log("Profile Service returned invalid response for username: %s", username);
               return null;
            } else {
               LOGGER.at(Level.FINE).log("Successfully fetched profile: %s (%s)", profile.getUsername(), profile.getUuid());
               return profile;
            }
         }
      } catch (IOException var7) {
         LOGGER.at(Level.WARNING).log("IO error while fetching profile by username: %s", var7.getMessage());
         return null;
      } catch (InterruptedException var8) {
         LOGGER.at(Level.WARNING).log("Request interrupted while fetching profile by username");
         Thread.currentThread().interrupt();
         return null;
      } catch (Exception var9) {
         LOGGER.at(Level.WARNING).log("Unexpected error fetching profile by username: %s", var9.getMessage());
         return null;
      }
   }

   public CompletableFuture<ProfileServiceClient.PublicGameProfile> getProfileByUsernameAsync(@Nonnull String username, @Nonnull String bearerToken) {
      return CompletableFuture.supplyAsync(() -> this.getProfileByUsername(username, bearerToken));
   }

   private static <T> KeyedCodec<T> externalKey(String key, Codec<T> codec) {
      return new KeyedCodec<>(key, codec, false, true);
   }

   public static class PublicGameProfile {
      public static final BuilderCodec<ProfileServiceClient.PublicGameProfile> CODEC = BuilderCodec.builder(
            ProfileServiceClient.PublicGameProfile.class, ProfileServiceClient.PublicGameProfile::new
         )
         .append(ProfileServiceClient.externalKey("uuid", Codec.UUID_STRING), (p, v) -> p.uuid = v, ProfileServiceClient.PublicGameProfile::getUuid)
         .add()
         .append(ProfileServiceClient.externalKey("username", Codec.STRING), (p, v) -> p.username = v, ProfileServiceClient.PublicGameProfile::getUsername)
         .add()
         .build();
      private UUID uuid;
      private String username;

      public PublicGameProfile() {
      }

      public PublicGameProfile(UUID uuid, String username) {
         this.uuid = uuid;
         this.username = username;
      }

      public UUID getUuid() {
         return this.uuid;
      }

      public String getUsername() {
         return this.username;
      }
   }
}
