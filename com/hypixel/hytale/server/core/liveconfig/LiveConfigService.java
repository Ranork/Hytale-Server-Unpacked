package com.hypixel.hytale.server.core.liveconfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.auth.AuthConfig;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.util.ServiceHttpClientFactory;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LiveConfigService {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final String URL_OVERRIDE_PROPERTY = "liveconfig.url";
   private final HttpClient httpClient = ServiceHttpClientFactory.newBuilder(AuthConfig.HTTP_TIMEOUT).build();
   private final String baseUrl = System.getProperty("liveconfig.url", "https://liveconfig.hytale.com");

   public static boolean hasUrlOverride() {
      return System.getProperty("liveconfig.url") != null;
   }

   @Nullable
   public LiveConfigSnapshot fetchServerConfigs(
      @Nonnull String patchline, @Nonnull String os, @Nonnull String arch, @Nullable String version, @Nullable String currentEtag
   ) {
      ServerAuthManager authManager = ServerAuthManager.getInstance();
      String sessionToken = authManager.getSessionToken();
      boolean urlOverride = hasUrlOverride();
      if (urlOverride || sessionToken != null && !sessionToken.isEmpty()) {
         try {
            StringBuilder queryBuilder = new StringBuilder("?os=").append(encode(os)).append("&arch=").append(encode(arch));
            if (version != null && !version.isEmpty()) {
               queryBuilder.append("&version=").append(encode(version));
            }

            URI uri = URI.create(String.format("%s/server-configs/%s%s", this.baseUrl, patchline, queryBuilder));
            String bearerToken = sessionToken != null && !sessionToken.isEmpty() ? sessionToken : "dev-test-token";
            Builder requestBuilder = HttpRequest.newBuilder()
               .uri(uri)
               .header("Authorization", "Bearer " + bearerToken)
               .header("User-Agent", AuthConfig.USER_AGENT)
               .header("Accept", "application/json")
               .timeout(AuthConfig.HTTP_TIMEOUT)
               .GET();
            if (currentEtag != null) {
               requestBuilder.header("If-None-Match", currentEtag);
            }

            HttpResponse<String> response = this.httpClient.send(requestBuilder.build(), BodyHandlers.ofString());
            return this.handleResponse(response);
         } catch (InterruptedException var14) {
            Thread.currentThread().interrupt();
            LOGGER.at(Level.FINE).log("Live config fetch interrupted");
            return null;
         } catch (Exception var15) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var15)).log("Failed to fetch live config");
            return null;
         }
      } else {
         LOGGER.at(Level.FINE).log("No session token available for live config fetch");
         return null;
      }
   }

   @Nullable
   private LiveConfigSnapshot handleResponse(@Nonnull HttpResponse<String> response) {
      int status = response.statusCode();
      if (status == 304) {
         LOGGER.at(Level.FINE).log("Live config not modified (304)");
         return null;
      } else if (status != 200) {
         LOGGER.at(Level.WARNING).log("Live config fetch failed: HTTP %d", status);
         return null;
      } else {
         return this.parseResponse(response);
      }
   }

   @Nullable
   private LiveConfigSnapshot parseResponse(@Nonnull HttpResponse<String> response) {
      try {
         JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
         String version = root.get("version").getAsString();
         JsonObject flagsObj = root.getAsJsonObject("flags");
         Map<String, LiveConfigSnapshot.ResolvedFlag> flags = new HashMap<>();
         if (flagsObj != null) {
            for (Entry<String, JsonElement> entry : flagsObj.entrySet()) {
               JsonObject flagObj = entry.getValue().getAsJsonObject();
               String type = flagObj.get("type").getAsString();
               Object value = parseValue(type, flagObj);
               flags.put(entry.getKey(), new LiveConfigSnapshot.ResolvedFlag(type, value));
            }
         }

         String etag = response.headers().firstValue("ETag").orElse(version);
         return new LiveConfigSnapshot(version, etag, flags);
      } catch (Exception var11) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var11)).log("Failed to parse live config response");
         return null;
      }
   }

   @Nullable
   private static Object parseValue(@Nonnull String type, @Nonnull JsonObject flagObj) {
      JsonElement valueElement = flagObj.get("value");
      if (valueElement != null && !valueElement.isJsonNull()) {
         return switch (type) {
            case "boolean" -> valueElement.getAsBoolean();
            case "integer" -> valueElement.getAsInt();
            case "string" -> valueElement.getAsString();
            default -> {
               LOGGER.at(Level.WARNING).log("Unknown flag type: %s", type);
               yield null;
            }
         };
      } else {
         return null;
      }
   }

   @Nonnull
   private static String encode(@Nonnull String s) {
      return URLEncoder.encode(s, StandardCharsets.UTF_8);
   }

   @Nonnull
   public static String detectOS() {
      String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
      if (osName.contains("win")) {
         return "windows";
      } else if (osName.contains("mac") || osName.contains("darwin")) {
         return "darwin";
      } else {
         return !osName.contains("linux") && !osName.contains("nux") ? "any" : "linux";
      }
   }

   @Nonnull
   public static String detectArch() {
      String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
      if (arch.equals("amd64") || arch.equals("x86_64")) {
         return "amd64";
      } else {
         return !arch.equals("aarch64") && !arch.equals("arm64") ? "any" : "arm64";
      }
   }
}
