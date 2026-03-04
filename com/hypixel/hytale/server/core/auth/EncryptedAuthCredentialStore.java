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
import java.security.SecureRandom;
import java.time.Instant;
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
   private static final String ALGORITHM = "AES/GCM/NoPadding";
   private static final int GCM_IV_LENGTH = 12;
   private static final int GCM_TAG_LENGTH = 128;
   private static final int KEY_LENGTH = 256;
   private static final int PBKDF2_ITERATIONS = 100000;
   private static final byte[] SALT = "HytaleAuthCredentialStore".getBytes(StandardCharsets.UTF_8);
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
   private IAuthCredentialStore.OAuthTokens tokens = new IAuthCredentialStore.OAuthTokens(null, null, null);
   @Nullable
   private UUID profile;

   public EncryptedAuthCredentialStore(@Nonnull Path path) {
      this.path = path;
      this.encryptionKey = deriveKey();
      if (this.encryptionKey == null) {
         LOGGER.at(Level.WARNING).log("Cannot derive encryption key - encrypted storage will not persist credentials");
      } else {
         this.load();
      }
   }

   @Nullable
   private static SecretKey deriveKey() {
      UUID hardwareId = HardwareUtil.getUUID();
      if (hardwareId == null) {
         return null;
      } else {
         try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(hardwareId.toString().toCharArray(), SALT, 100000, 256);
            SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), "AES");
         } catch (Exception var4) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var4)).log("Failed to derive encryption key");
            return null;
         }
      }
   }

   private void load() {
      if (this.encryptionKey != null && Files.exists(this.path)) {
         try {
            byte[] encrypted = Files.readAllBytes(this.path);
            byte[] decrypted = this.decrypt(encrypted);
            if (decrypted == null) {
               LOGGER.at(Level.WARNING).log("Failed to decrypt credentials from %s - file may be corrupted or from different hardware", this.path);
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
         } catch (Exception var5) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var5)).log("Failed to load encrypted credentials from %s", this.path);
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
   private byte[] decrypt(@Nonnull byte[] encrypted) {
      if (this.encryptionKey != null && encrypted.length >= 12) {
         try {
            ByteBuffer buffer = ByteBuffer.wrap(encrypted);
            byte[] iv = new byte[12];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(2, this.encryptionKey, new GCMParameterSpec(128, iv));
            return cipher.doFinal(ciphertext);
         } catch (Exception var6) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var6)).log("Decryption failed");
            return null;
         }
      } else {
         return null;
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
