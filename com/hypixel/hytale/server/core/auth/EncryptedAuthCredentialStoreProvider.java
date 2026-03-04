package com.hypixel.hytale.server.core.auth;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.PathUtil;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class EncryptedAuthCredentialStoreProvider implements AuthCredentialStoreProvider {
   public static final String ID = "Encrypted";
   public static final String DEFAULT_PATH = "auth.enc";
   public static final BuilderCodec<EncryptedAuthCredentialStoreProvider> CODEC = BuilderCodec.builder(
         EncryptedAuthCredentialStoreProvider.class, EncryptedAuthCredentialStoreProvider::new
      )
      .append(new KeyedCodec<>("Path", Codec.STRING), (o, p) -> o.path = p, o -> o.path)
      .add()
      .build();
   private String path = "auth.enc";

   @Nonnull
   @Override
   public IAuthCredentialStore createStore() {
      Path resolved = Path.of(this.path);
      if (!PathUtil.isInTrustedRoot(resolved)) {
         throw new IllegalStateException("Auth credential store path must be within a trusted directory: " + this.path);
      } else {
         return new EncryptedAuthCredentialStore(resolved);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "EncryptedAuthCredentialStoreProvider{path='" + this.path + "'}";
   }
}
