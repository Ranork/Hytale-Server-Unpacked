package com.hypixel.hytale.server.core.auth;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.HardwareUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.BsonUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.bson.BsonDocument;

public class EncryptedAuthCredentialStore implements IAuthCredentialStore {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final String ENV_AUTH_KEY = "HYTALE_AUTH_KEY";
   private static final String ENV_AUTH_KEY_FILE = "HYTALE_AUTH_KEY_FILE";
   private static final String ALGORITHM = "AES/GCM/NoPadding";
   private static final int GCM_IV_LENGTH = 12;
   private static final int GCM_TAG_LENGTH = 128;
   private static final int KEY_LENGTH = 256;
   private static final int PBKDF2_ITERATIONS = 100000;
   private static final byte[] SALT = "HytaleAuthCredentialStore".getBytes(StandardCharsets.UTF_8);
   private static final Set<PosixFilePermission> OWNER_ONLY_PERMISSIONS = Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
   private static final BuilderCodec<EncryptedAuthCredentialStore.StoredCredentials> CREDENTIALS_CODEC = BuilderCodec.builder(
         EncryptedAuthCredentialStore.StoredCredentials.class, EncryptedAuthCredentialStore.StoredCredentials::new
      )
      .append(new KeyedCodec<>("AccessToken", Codec.STRING), (o, v) -> o.accessToken = v, o -> o.accessToken)
      .add()
      .append(new KeyedCodec<>("RefreshToken", Codec.STRING), (o, v) -> o.refreshToken = v, o -> o.refreshToken)
      .add()
      .append(new KeyedCodec<>("ExpiresAt", Codec.INSTANT), (o, v) -> o.expiresAt = v, o -> o.expiresAt)
      .add()
      .append(new KeyedCodec<>("ProfileUuid", Codec.UUID_STRING), (o, v) -> o.profileUuid = v, o -> o.profileUuid)
      .add()
      .build();
   private final Path path;
   @Nullable
   private final SecretKey encryptionKey;
   private final List<SecretKey> decryptionKeys;
   private IAuthCredentialStore.OAuthTokens tokens = new IAuthCredentialStore.OAuthTokens(null, null, null);
   @Nullable
   private UUID profile;

   public EncryptedAuthCredentialStore(@Nonnull Path path) {
      this.path = path;
      EncryptedAuthCredentialStore.ResolvedPassphrases resolved = resolvePassphrases(path);
      this.encryptionKey = resolved.writePassphrase != null ? deriveKey(resolved.writePassphrase) : null;
      this.decryptionKeys = deriveKeys(resolved.readPassphrases);
      if (this.encryptionKey == null) {
         LOGGER.at(Level.WARNING).log("Cannot derive encryption key - encrypted storage will not persist credentials");
      } else {
         this.load();
      }
   }

   @Nullable
   private static SecretKey deriveKey(@Nonnull String passphrase) {
      PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray(), SALT, 100000, 256);

      SecretKey tmp;
      try {
         SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
         tmp = factory.generateSecret(spec);
         return new SecretKeySpec(tmp.getEncoded(), "AES");
      } catch (Exception var8) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var8)).log("Failed to derive encryption key");
         tmp = null;
      } finally {
         spec.clearPassword();
      }

      return tmp;
   }

   @Nonnull
   private static List<SecretKey> deriveKeys(@Nonnull List<String> passphrases) {
      ArrayList<SecretKey> keys = new ArrayList<>();

      for (String passphrase : passphrases) {
         SecretKey key = deriveKey(passphrase);
         if (key != null) {
            keys.add(key);
         }
      }

      return keys;
   }

   @Nonnull
   private static EncryptedAuthCredentialStore.ResolvedPassphrases resolvePassphrases(@Nonnull Path credentialPath) {
      String envFilePassphrase = null;
      String envDirectPassphrase = null;
      String hardwarePassphrase = null;
      String generatedPassphrase = null;
      String keyFilePath = System.getenv("HYTALE_AUTH_KEY_FILE");
      if (keyFilePath != null && !keyFilePath.isBlank()) {
         String content;
         try {
            content = Files.readString(Path.of(keyFilePath), StandardCharsets.UTF_8).trim();
         } catch (IOException var10) {
            throw new IllegalStateException("Failed to read encryption key file specified by HYTALE_AUTH_KEY_FILE: " + keyFilePath, var10);
         }

         if (content.isEmpty()) {
            throw new IllegalStateException("Encryption key file specified by HYTALE_AUTH_KEY_FILE is empty: " + keyFilePath);
         }

         LOGGER.at(Level.INFO).log("Using encryption passphrase from %s", "HYTALE_AUTH_KEY_FILE");
         envFilePassphrase = content;
      }

      String directKey = System.getenv("HYTALE_AUTH_KEY");
      if (directKey != null && !directKey.isBlank()) {
         LOGGER.at(Level.INFO).log("Using encryption passphrase from %s", "HYTALE_AUTH_KEY");
         envDirectPassphrase = directKey;
      }

      UUID hardwareId = HardwareUtil.getUUID();
      if (hardwareId != null) {
         hardwarePassphrase = hardwareId.toString();
      }

      generatedPassphrase = loadGeneratedKeyFile(credentialPath);
      String writePassphrase;
      if (envFilePassphrase != null) {
         writePassphrase = envFilePassphrase;
      } else if (envDirectPassphrase != null) {
         writePassphrase = envDirectPassphrase;
      } else if (hardwarePassphrase != null) {
         writePassphrase = hardwarePassphrase;
      } else if (generatedPassphrase != null) {
         writePassphrase = generatedPassphrase;
      } else {
         writePassphrase = generateKeyFile(credentialPath);
      }

      ArrayList<String> readPassphrases = new ArrayList<>(4);
      if (envFilePassphrase != null) {
         readPassphrases.add(envFilePassphrase);
      }

      if (envDirectPassphrase != null) {
         readPassphrases.add(envDirectPassphrase);
      }

      if (generatedPassphrase != null) {
         readPassphrases.add(generatedPassphrase);
      }

      if (hardwarePassphrase != null) {
         readPassphrases.add(hardwarePassphrase);
      }

      return new EncryptedAuthCredentialStore.ResolvedPassphrases(writePassphrase, readPassphrases);
   }

   @Nonnull
   public static Path keyFilePath(@Nonnull Path credentialPath) {
      String fileName = credentialPath.getFileName().toString();
      String keyFileName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(46)) + ".key" : fileName + ".key";
      return credentialPath.resolveSibling(keyFileName);
   }

   @Nullable
   private static String loadGeneratedKeyFile(@Nonnull Path credentialPath) {
      Path keyPath = keyFilePath(credentialPath);
      if (!Files.exists(keyPath)) {
         return null;
      } else {
         try {
            String content = Files.readString(keyPath, StandardCharsets.UTF_8).trim();
            if (!content.isEmpty()) {
               LOGGER.at(Level.INFO).log("Using auto-generated encryption key from %s", keyPath);
               return content;
            }
         } catch (IOException var3) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var3)).log("Failed to read generated key file %s", keyPath);
         }

         return null;
      }
   }

   @Nullable
   private static String generateKeyFile(@Nonnull Path credentialPath) {
      Path keyPath = keyFilePath(credentialPath);

      try {
         String passphrase = UUID.randomUUID().toString();
         Files.writeString(keyPath, passphrase, StandardCharsets.UTF_8);
         restrictFilePermissions(keyPath);
         LOGGER.at(Level.INFO).log("Generated new encryption key file at %s", keyPath);
         return passphrase;
      } catch (IOException var3) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var3)).log("Failed to generate key file at %s", keyPath);
         return null;
      }
   }

   private static void restrictFilePermissions(@Nonnull Path file) {
      try {
         Files.setPosixFilePermissions(file, OWNER_ONLY_PERMISSIONS);
      } catch (UnsupportedOperationException var2) {
      } catch (IOException var3) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var3)).log("Failed to set file permissions on %s", file);
      }
   }

   private void load() {
      if (this.encryptionKey != null && Files.exists(this.path)) {
         try {
            byte[] encrypted = Files.readAllBytes(this.path);
            byte[] decrypted = null;
            boolean migrated = false;

            for (SecretKey key : this.decryptionKeys) {
               decrypted = decrypt(encrypted, key);
               if (decrypted != null) {
                  if (!key.equals(this.encryptionKey)) {
                     LOGGER.at(Level.INFO).log("Decrypted credentials using fallback key - will re-encrypt with current key");
                     migrated = true;
                  }
                  break;
               }
            }

            if (decrypted == null) {
               LOGGER.at(Level.WARNING).log("Failed to decrypt credentials from %s - file may be corrupted or from different key source", this.path);
               return;
            }

            BsonDocument doc = BsonUtil.readFromBytes(decrypted);
            if (doc == null) {
               LOGGER.at(Level.WARNING).log("Failed to parse credentials from %s", this.path);
               return;
            }

            EncryptedAuthCredentialStore.StoredCredentials stored = CREDENTIALS_CODEC.decode(doc);
            if (stored != null) {
               this.tokens = new IAuthCredentialStore.OAuthTokens(stored.accessToken, stored.refreshToken, stored.expiresAt);
               this.profile = stored.profileUuid;
            }

            LOGGER.at(Level.INFO).log("Loaded encrypted credentials from %s", this.path);
            if (migrated) {
               this.save();
               LOGGER.at(Level.INFO).log("Migrated credentials to current encryption key");
            }
         } catch (Exception var6) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var6)).log("Failed to load encrypted credentials from %s", this.path);
         }
      }
   }

   private void save() {
      if (this.encryptionKey == null) {
         LOGGER.at(Level.WARNING).log("Cannot save credentials - no encryption key available");
      } else {
         try {
            EncryptedAuthCredentialStore.StoredCredentials stored = new EncryptedAuthCredentialStore.StoredCredentials();
            stored.accessToken = this.tokens.accessToken();
            stored.refreshToken = this.tokens.refreshToken();
            stored.expiresAt = this.tokens.accessTokenExpiresAt();
            stored.profileUuid = this.profile;
            BsonDocument doc = (BsonDocument)CREDENTIALS_CODEC.encode(stored);
            byte[] plaintext = BsonUtil.writeToBytes(doc);
            byte[] encrypted = this.encrypt(plaintext);
            if (encrypted == null) {
               LOGGER.at(Level.SEVERE).log("Failed to encrypt credentials");
               return;
            }

            Files.write(this.path, encrypted);
            restrictFilePermissions(this.path);
         } catch (IOException var5) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var5)).log("Failed to save encrypted credentials to %s", this.path);
         }
      }
   }

   @Nullable
   private byte[] encrypt(@Nonnull byte[] plaintext) {
      if (this.encryptionKey == null) {
         return null;
      } else {
         try {
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, this.encryptionKey, new GCMParameterSpec(128, iv));
            byte[] ciphertext = cipher.doFinal(plaintext);
            ByteBuffer result = ByteBuffer.allocate(iv.length + ciphertext.length);
            result.put(iv);
            result.put(ciphertext);
            return result.array();
         } catch (Exception var6) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var6)).log("Encryption failed");
            return null;
         }
      }
   }

   @Nullable
   private static byte[] decrypt(@Nonnull byte[] encrypted, @Nonnull SecretKey key) {
      if (encrypted.length < 12) {
         return null;
      } else {
         try {
            ByteBuffer buffer = ByteBuffer.wrap(encrypted);
            byte[] iv = new byte[12];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(2, key, new GCMParameterSpec(128, iv));
            return cipher.doFinal(ciphertext);
         } catch (Exception var6) {
            return null;
         }
      }
   }

   @Override
   public void setTokens(@Nonnull IAuthCredentialStore.OAuthTokens tokens) {
      this.tokens = tokens;
      this.save();
   }

   @Nonnull
   @Override
   public IAuthCredentialStore.OAuthTokens getTokens() {
      return this.tokens;
   }

   @Override
   public void setProfile(@Nullable UUID uuid) {
      this.profile = uuid;
      this.save();
   }

   @Nullable
   @Override
   public UUID getProfile() {
      return this.profile;
   }

   @Override
   public void clear() {
      this.tokens = new IAuthCredentialStore.OAuthTokens(null, null, null);
      this.profile = null;

      try {
         Files.deleteIfExists(this.path);
      } catch (IOException var2) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var2)).log("Failed to delete encrypted credentials file %s", this.path);
      }
   }

   private record ResolvedPassphrases(@Nullable String writePassphrase, @Nonnull List<String> readPassphrases) {
   }

   private static class StoredCredentials {
      @Nullable
      String accessToken;
      @Nullable
      String refreshToken;
      @Nullable
      Instant expiresAt;
      @Nullable
      UUID profileUuid;
   }
}
