package com.hypixel.hytale.server.core.universe.world.storage.resources;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.EmptyResourceStorage;
import com.hypixel.hytale.component.IResourceStorage;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import javax.annotation.Nonnull;

public class EmptyResourceStorageProvider implements IResourceStorageProvider {
   public static final EmptyResourceStorageProvider INSTANCE = new EmptyResourceStorageProvider();
   public static final String ID = "Empty";
   public static final BuilderCodec<EmptyResourceStorageProvider> CODEC = BuilderCodec.builder(EmptyResourceStorageProvider.class, () -> INSTANCE).build();

   @Nonnull
   @Override
   public <T extends WorldProvider> IResourceStorage getResourceStorage(@Nonnull World world) {
      return EmptyResourceStorage.get();
   }

   @Nonnull
   @Override
   public String toString() {
      return "EmptyResourceStorageProvider{}";
   }
}
