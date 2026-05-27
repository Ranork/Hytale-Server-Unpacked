package com.hypixel.hytale.server.core.discovery;

import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.auth.AuthConfig;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.util.ServiceHttpClientFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class DiscoveryService {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final HttpClient httpClient = ServiceHttpClientFactory.newBuilder(AuthConfig.HTTP_TIMEOUT).build();

   public boolean sendHeartbeat(String discoveryToken) {
      try {
         ServerAuthManager authManager = ServerAuthManager.getInstance();
         String sessionToken = authManager.getSessionToken();
         String serverPatchline = ManifestUtil.getPatchline();
         if (serverPatchline == null) {
            serverPatchline = "dev";
         }

         String serverVersion = ManifestUtil.getVersion();
         if (serverVersion == null) {
            serverVersion = "dev";
         }

         String body = String.format(
            "{\"discoveryToken\":\"%s\",\"serverPatchline\":\"%s\",\"serverVersion\":\"%s\"}",
            escapeJsonString(discoveryToken),
            escapeJsonString(serverPatchline),
            escapeJsonString(serverVersion)
         );
         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(String.format("%s/servers/heartbeat", "https://server-discovery.hytale.com")))
            .header("User-Agent", AuthConfig.USER_AGENT)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + sessionToken)
            .POST(BodyPublishers.ofString(body))
            .build();
         HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());
         if (response.statusCode() == 204) {
            return false;
         } else {
            String errorBody = response.body();
            LOGGER.at(Level.WARNING).log("Discovery heartbeat failed with status code %d, body: %s", response.statusCode(), errorBody);
            return errorBody.contains("session token needs to be from same profile as server") || errorBody.contains("server not found");
         }
      } catch (Throwable var10) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var10)).log("Failed to send discovery heartbeat");
         return false;
      }
   }

   private static String escapeJsonString(String value) {
      return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
   }
}
