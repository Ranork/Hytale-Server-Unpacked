package com.hypixel.hytale.server.core.universe.datastore;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class DiskDataStoreProvider implements DataStoreProvider {
   public static final String ID = "Disk";
   public static final BuilderCodec<DiskDataStoreProvider> CODEC = BuilderCodec.builder(DiskDataStoreProvider.class, DiskDataStoreProvider::new)
      .append(
         new KeyedCodec<>("Path", Codec.STRING),
         (diskDataStoreProvider, s) -> diskDataStoreProvider.path = s,
         diskDataStoreProvider -> diskDataStoreProvider.path
      )
      .addValidator(Validators.nonNull())
      .add()
      .build();
   private String path;

   public DiskDataStoreProvider(String path) {
      this.path = path;
   }

   protected DiskDataStoreProvider() {
   }

   @Nonnull
   @Override
   public <T> DataStore<T> create(BuilderCodec<T> builderCodec) {
      return new DiskDataStore<>(this.path, builderCodec);
   }

   @Nonnull
   @Override
   public String toString() {
      return "DiskDataStoreProvider{path='" + this.path + "'}";
   }
}
