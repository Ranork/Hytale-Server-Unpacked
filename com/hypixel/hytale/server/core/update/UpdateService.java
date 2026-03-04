package com.hypixel.hytale.server.core.update;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.auth.AuthConfig;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.config.UpdateConfig;
import com.hypixel.hytale.server.core.util.ServiceHttpClientFactory;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateService {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30L);
   private static final Duration DOWNLOAD_TIMEOUT = Duration.ofMinutes(30L);
   private static final Path STAGING_DIR = Path.of("..").resolve("updater").resolve("staging");
   private static final Path BACKUP_DIR = Path.of("..").resolve("updater").resolve("backup");
   private final HttpClient httpClient;
   private final String accountDataUrl = "https://account-data.hytale.com";

   public UpdateService() {
      this.httpClient = ServiceHttpClientFactory.newBuilder(REQUEST_TIMEOUT).followRedirects(Redirect.NORMAL).build();
   }

   @Nullable
   public CompletableFuture<UpdateService.VersionManifest> checkForUpdate(@Nonnull String patchline) {
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               ServerAuthManager authManager = ServerAuthManager.getInstance();
               String accessToken = authManager.getOAuthAccessToken();
               if (accessToken == null) {
                  LOGGER.at(Level.WARNING).log("Cannot check for updates - not authenticated");
                  return null;
               } else {
                  String manifestPath = String.format("version/%s.json", patchline);
                  String signedUrl = this.getSignedUrl(accessToken, manifestPath);
                  if (signedUrl == null) {
                     LOGGER.at(Level.WARNING).log("Failed to get signed URL for version manifest");
                     return null;
                  } else {
                     HttpRequest manifestRequest = HttpRequest.newBuilder()
                        .uri(URI.create(signedUrl))
                        .header("Accept", "application/json")
                        .timeout(REQUEST_TIMEOUT)
                        .GET()
                        .build();
                     HttpResponse<String> response = this.httpClient.send(manifestRequest, BodyHandlers.ofString());
                     if (response.statusCode() != 200) {
                        LOGGER.at(Level.WARNING).log("Failed to fetch version manifest: HTTP %d", response.statusCode());
                        return null;
                     } else {
                        UpdateService.VersionManifest manifest = UpdateService.VersionManifest.CODEC
                           .decodeJson(new RawJsonReader(response.body().toCharArray()), EmptyExtraInfo.EMPTY);
                        if (manifest != null && manifest.version != null) {
                           LOGGER.at(Level.INFO).log("Found version: %s", manifest.version);
                           return manifest;
                        } else {
                           LOGGER.at(Level.WARNING).log("Invalid version manifest response");
                           return null;
                        }
                     }
                  }
               }
            } catch (IOException var9) {
               LOGGER.at(Level.WARNING).log("IO error checking for updates: %s", var9.getMessage());
               return null;
            } catch (InterruptedException var10) {
               Thread.currentThread().interrupt();
               LOGGER.at(Level.WARNING).log("Update check interrupted");
               return null;
            } catch (Exception var11) {
               ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var11)).log("Error checking for updates");
               return null;
            }
         }
      );
   }

   public UpdateService.DownloadTask downloadUpdate(
      @Nonnull UpdateService.VersionManifest manifest, @Nonnull Path stagingDir, @Nullable UpdateService.ProgressCallback progressCallback
   ) {
      CompletableFuture<Boolean> future = new CompletableFuture<>();
      Thread thread = new Thread(() -> {
         try {
            boolean result = this.performDownload(manifest, stagingDir, progressCallback);
            future.complete(result);
         } catch (CancellationException var6) {
            future.completeExceptionally(var6);
         } catch (InterruptedException var7) {
            Thread.currentThread().interrupt();
            future.completeExceptionally(new CancellationException("Update download interrupted"));
         } catch (Exception var8) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var8)).log("Error downloading update");
            future.complete(false);
         }
      }, "UpdateDownload");
      thread.setDaemon(true);
      thread.start();
      return new UpdateService.DownloadTask(future, thread);
   }

   private boolean performDownload(
      @Nonnull UpdateService.VersionManifest manifest, @Nonnull Path stagingDir, @Nullable UpdateService.ProgressCallback progressCallback
   ) throws IOException, InterruptedException {
      ServerAuthManager authManager = ServerAuthManager.getInstance();
      String accessToken = authManager.getOAuthAccessToken();
      if (accessToken == null) {
         LOGGER.at(Level.WARNING).log("Cannot download update - not authenticated");
         return false;
      } else {
         String signedUrl = this.getSignedUrl(accessToken, manifest.downloadUrl);
         if (signedUrl == null) {
            LOGGER.at(Level.WARNING).log("Failed to get signed URL for download");
            return false;
         } else {
            HttpRequest downloadRequest = HttpRequest.newBuilder().uri(URI.create(signedUrl)).timeout(DOWNLOAD_TIMEOUT).GET().build();
            Path tempFile = Files.createTempFile("hytale-update-", ".zip");

            boolean var36;
            try {
               HttpResponse<InputStream> response = this.httpClient.send(downloadRequest, BodyHandlers.ofInputStream());
               if (response.statusCode() != 200) {
                  LOGGER.at(Level.WARNING).log("Failed to download update: HTTP %d", response.statusCode());
                  return false;
               }

               long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L);

               MessageDigest digest;
               try {
                  digest = MessageDigest.getInstance("SHA-256");
               } catch (NoSuchAlgorithmException var31) {
                  LOGGER.at(Level.SEVERE).log("SHA-256 not available - this should never happen");
                  return false;
               }

               try (
                  InputStream inputStream = response.body();
                  OutputStream outputStream = Files.newOutputStream(tempFile);
               ) {
                  byte[] buffer = new byte[8192];
                  long downloaded = 0L;

                  int read;
                  while ((read = inputStream.read(buffer)) != -1) {
                     if (Thread.currentThread().isInterrupted()) {
                        throw new CancellationException("Update download cancelled");
                     }

                     outputStream.write(buffer, 0, read);
                     digest.update(buffer, 0, read);
                     downloaded += read;
                     if (progressCallback != null && contentLength > 0L) {
                        int percent = (int)(downloaded * 100L / contentLength);
                        progressCallback.onProgress(percent, downloaded, contentLength);
                     }
                  }
               }

               String actualHash = HexFormat.of().formatHex(digest.digest());
               if (manifest.sha256 == null || manifest.sha256.equalsIgnoreCase(actualHash)) {
                  if (clearStagingDir(stagingDir)) {
                     Files.createDirectories(stagingDir);
                     if (Thread.currentThread().isInterrupted()) {
                        throw new CancellationException("Update download cancelled");
                     }

                     FileUtil.extractZip(tempFile, stagingDir);
                     LOGGER.at(Level.INFO).log("Update %s downloaded and extracted to staging", manifest.version);
                     return true;
                  }

                  LOGGER.at(Level.WARNING).log("Failed to clear staging directory before extraction");
                  return false;
               }

               LOGGER.at(Level.WARNING).log("Checksum mismatch! Expected: %s, Got: %s", manifest.sha256, actualHash);
               var36 = false;
            } finally {
               Files.deleteIfExists(tempFile);
            }

            return var36;
         }
      }
   }

   @Nullable
   private String getSignedUrl(String accessToken, String path) throws IOException, InterruptedException {
      String url = this.accountDataUrl + "/game-assets/" + path;
      HttpRequest request = HttpRequest.newBuilder()
         .uri(URI.create(url))
         .header("Accept", "application/json")
         .header("Authorization", "Bearer " + accessToken)
         .header("User-Agent", AuthConfig.USER_AGENT)
         .timeout(REQUEST_TIMEOUT)
         .GET()
         .build();
      HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());
      if (response.statusCode() != 200) {
         LOGGER.at(Level.WARNING).log("Failed to get signed URL: HTTP %d - %s", response.statusCode(), response.body());
         return null;
      } else {
         UpdateService.SignedUrlResponse signedResponse = UpdateService.SignedUrlResponse.CODEC
            .decodeJson(new RawJsonReader(response.body().toCharArray()), EmptyExtraInfo.EMPTY);
         return signedResponse != null ? signedResponse.url : null;
      }
   }

   @Nonnull
   public static String getEffectivePatchline() {
      UpdateConfig config = HytaleServer.get().getConfig().getUpdateConfig();
      String patchline = config.getPatchline();
      if (patchline != null && !patchline.isEmpty()) {
         return patchline;
      } else {
         patchline = ManifestUtil.getPatchline();
         return patchline != null ? patchline : "release";
      }
   }

   public static boolean isValidUpdateLayout() {
      Path parent = Path.of("..").toAbsolutePath();
      return Files.exists(parent.resolve("Assets.zip")) && (Files.exists(parent.resolve("start.sh")) || Files.exists(parent.resolve("start.bat")));
   }

   @Nonnull
   public static Path getStagingDir() {
      return STAGING_DIR;
   }

   @Nonnull
   public static Path getBackupDir() {
      return BACKUP_DIR;
   }

   @Nullable
   public static String getStagedVersion() {
      Path stagedJar = STAGING_DIR.resolve("Server").resolve("HytaleServer.jar");
      return !Files.exists(stagedJar) ? null : readVersionFromJar(stagedJar);
   }

   public static boolean deleteStagedUpdate() {
      return safeDeleteUpdaterDir(STAGING_DIR, "staging");
   }

   public static boolean deleteBackupDir() {
      return safeDeleteUpdaterDir(BACKUP_DIR, "backup");
   }

   private static boolean clearStagingDir(@Nonnull Path stagingDir) {
      if (!Files.exists(stagingDir)) {
         return true;
      } else if (stagingDir.toAbsolutePath().normalize().equals(STAGING_DIR.toAbsolutePath().normalize())) {
         return deleteStagedUpdate();
      } else {
         try {
            FileUtil.deleteDirectory(stagingDir);
            return true;
         } catch (IOException var2) {
            LOGGER.at(Level.WARNING).log("Failed to delete staging dir %s: %s", stagingDir, var2.getMessage());
            return false;
         }
      }
   }

   private static boolean safeDeleteUpdaterDir(Path dir, String expectedName) {
      try {
         if (!Files.exists(dir)) {
            return true;
         } else {
            Path absolute = dir.toAbsolutePath().normalize();
            Path parent = absolute.getParent();
            if (parent == null || !parent.getFileName().toString().equals("updater")) {
               LOGGER.at(Level.SEVERE).log("Refusing to delete %s - not within updater/ directory", absolute);
               return false;
            } else if (!absolute.getFileName().toString().equals(expectedName)) {
               LOGGER.at(Level.SEVERE).log("Refusing to delete %s - unexpected directory name", absolute);
               return false;
            } else {
               FileUtil.deleteDirectory(dir);
               return true;
            }
         }
      } catch (IOException var4) {
         LOGGER.at(Level.WARNING).log("Failed to delete %s: %s", dir, var4.getMessage());
         return false;
      }
   }

   @Nullable
   public static String readVersionFromJar(@Nonnull Path jarPath) {
      try {
         String var5;
         try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
               return null;
            }

            Attributes attrs = manifest.getMainAttributes();
            String vendorId = attrs.getValue("Implementation-Vendor-Id");
            if (!"com.hypixel.hytale".equals(vendorId)) {
               return null;
            }

            var5 = attrs.getValue("Implementation-Version");
         }

         return var5;
      } catch (IOException var8) {
         LOGGER.at(Level.WARNING).log("Failed to read version from JAR: %s", var8.getMessage());
         return null;
      }
   }

   private static <T> KeyedCodec<T> externalKey(String key, Codec<T> codec) {
      return new KeyedCodec<>(key, codec, false, true);
   }

   public record DownloadTask(CompletableFuture<Boolean> future, Thread thread) {
   }

   @FunctionalInterface
   public interface ProgressCallback {
      void onProgress(int var1, long var2, long var4);
   }

   private static class SignedUrlResponse {
      public String url;
      public static final BuilderCodec<UpdateService.SignedUrlResponse> CODEC = BuilderCodec.builder(
            UpdateService.SignedUrlResponse.class, UpdateService.SignedUrlResponse::new
         )
         .append(UpdateService.externalKey("url", Codec.STRING), (r, v) -> r.url = v, r -> r.url)
         .add()
         .build();
   }

   public static class VersionManifest {
      public String version;
      public String downloadUrl;
      public String sha256;
      public static final BuilderCodec<UpdateService.VersionManifest> CODEC = BuilderCodec.builder(
            UpdateService.VersionManifest.class, UpdateService.VersionManifest::new
         )
         .append(UpdateService.externalKey("version", Codec.STRING), (m, v) -> m.version = v, m -> m.version)
         .add()
         .append(UpdateService.externalKey("download_url", Codec.STRING), (m, v) -> m.downloadUrl = v, m -> m.downloadUrl)
         .add()
         .append(UpdateService.externalKey("sha256", Codec.STRING), (m, v) -> m.sha256 = v, m -> m.sha256)
         .add()
         .build();
   }
}
