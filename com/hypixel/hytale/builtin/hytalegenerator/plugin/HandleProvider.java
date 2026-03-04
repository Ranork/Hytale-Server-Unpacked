package com.hypixel.hytale.builtin.hytalegenerator.plugin;

import com.hypixel.hytale.builtin.hytalegenerator.chunkgenerator.ChunkRequest;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenLoadException;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HandleProvider implements IWorldGenProvider {
   @Nonnull
   public static final String ID = "HytaleGenerator";
   @Nonnull
   public static final String DEFAULT_WORLD_STRUCTURE_NAME = "Default";
   @Nonnull
   private final HytaleGenerator plugin;
   @Nonnull
   private String worldStructureName = "Default";
   @Nullable
   private String seedOverride;
   private int worldCounter;

   public HandleProvider(@Nonnull HytaleGenerator plugin, int worldCounter) {
      this.plugin = plugin;
      this.worldCounter = worldCounter;
   }

   public void setWorldStructureName(@Nullable String worldStructureName) {
      this.worldStructureName = worldStructureName;
   }

   public void setSeedOverride(@Nullable String seedOverride) {
      this.seedOverride = seedOverride;
   }

   @Nonnull
   public String getWorldStructureName() {
      return this.worldStructureName;
   }

   @Nullable
   public String getSeedOverride() {
      return this.seedOverride;
   }

   @Nonnull
   @Override
   public IWorldGen getGenerator() throws WorldGenLoadException {
      return new Handle(this.plugin, new ChunkRequest.GeneratorProfile(this.worldStructureName, 0, this.worldCounter), this.seedOverride);
   }
}
