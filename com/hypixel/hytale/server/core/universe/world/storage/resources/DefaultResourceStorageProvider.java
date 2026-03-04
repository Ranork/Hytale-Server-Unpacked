package com.hypixel.hytale.server.core.universe.world.storage.resources;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.IResourceStorage;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import javax.annotation.Nonnull;

public class DefaultResourceStorageProvider implements IResourceStorageProvider {
   public static final DefaultResourceStorageProvider INSTANCE = new DefaultResourceStorageProvider();
   public static final String ID = "Hytale";
   public static final BuilderCodec<DefaultResourceStorageProvider> CODEC = BuilderCodec.builder(DefaultResourceStorageProvider.class, () -> INSTANCE).build();
   public static final DiskResourceStorageProvider DEFAULT = new DiskResourceStorageProvider();

   @Nonnull
   @Override
   public <T extends WorldProvider> IResourceStorage getResourceStorage(@Nonnull World world) {
      return DEFAULT.getResourceStorage(world);
   }

   @Nonnull
   @Override
   public String toString() {
      return "DefaultResourceStorageProvider{}";
   }
}
