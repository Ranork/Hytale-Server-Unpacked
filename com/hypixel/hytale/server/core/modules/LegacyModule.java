package com.hypixel.hytale.server.core.modules;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.EntityChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.environment.EnvironmentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.blockpositions.BlockPositionProvider;
import com.hypixel.hytale.server.core.universe.world.chunk.systems.ChunkSystems;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import javax.annotation.Nonnull;

public class LegacyModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(LegacyModule.class).build();
   private static LegacyModule instance;
   private ComponentType<ChunkStore, WorldChunk> worldChunkComponentType;
   private ComponentType<ChunkStore, BlockChunk> blockChunkComponentType;
   private ComponentType<ChunkStore, EntityChunk> entityChunkComponentType;
   private ComponentType<ChunkStore, BlockComponentChunk> blockComponentChunkComponentType;
   private ComponentType<ChunkStore, EnvironmentChunk> environmentChunkComponentType;
   private ComponentType<ChunkStore, ChunkColumn> chunkColumnComponentType;
   private ComponentType<ChunkStore, ChunkSection> chunkSectionComponentType;
   private ComponentType<ChunkStore, BlockSection> blockSectionComponentType;
   private ComponentType<ChunkStore, FluidSection> fluidSectionComponentType;
   private ComponentType<ChunkStore, BlockPositionProvider> blockPositionProviderComponentType;

   public static LegacyModule get() {
      return instance;
   }

   public LegacyModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      this.worldChunkComponentType = this.getChunkStoreRegistry().registerComponent(WorldChunk.class, "WorldChunk", WorldChunk.CODEC);
      this.blockChunkComponentType = this.getChunkStoreRegistry().registerComponent(BlockChunk.class, "BlockChunk", BlockChunk.CODEC);
      this.entityChunkComponentType = this.getChunkStoreRegistry().registerComponent(EntityChunk.class, "EntityChunk", EntityChunk.CODEC);
      this.blockComponentChunkComponentType = this.getChunkStoreRegistry()
         .registerComponent(BlockComponentChunk.class, "BlockComponentChunk", BlockComponentChunk.CODEC);
      this.environmentChunkComponentType = this.getChunkStoreRegistry().registerComponent(EnvironmentChunk.class, "EnvironmentChunk", EnvironmentChunk.CODEC);
      this.chunkColumnComponentType = this.getChunkStoreRegistry().registerComponent(ChunkColumn.class, "ChunkColumn", ChunkColumn.CODEC);
      this.chunkSectionComponentType = this.getChunkStoreRegistry().registerComponent(ChunkSection.class, "ChunkSection", ChunkSection.CODEC);
      this.blockSectionComponentType = this.getChunkStoreRegistry().registerComponent(BlockSection.class, "Block", BlockSection.CODEC);
      this.fluidSectionComponentType = this.getChunkStoreRegistry().registerComponent(FluidSection.class, "Fluid", FluidSection.CODEC);
      this.blockPositionProviderComponentType = this.getChunkStoreRegistry().registerComponent(BlockPositionProvider.class, () -> {
         throw new UnsupportedOperationException("BlockPositionProvider cannot be constructed");
      });
      this.getChunkStoreRegistry().registerSystem(new ChunkSystems.OnChunkLoad());
      this.getChunkStoreRegistry().registerSystem(new ChunkSystems.OnNonTicking());
      this.getChunkStoreRegistry().registerSystem(new ChunkSystems.EnsureBlockSection());
      this.getChunkStoreRegistry().registerSystem(new ChunkSystems.LoadBlockSection());
      this.getChunkStoreRegistry().registerSystem(new ChunkSystems.ReplicateChanges());
      this.getChunkStoreRegistry().registerSystem(new BlockChunk.LoadBlockChunkPacketSystem(this.blockChunkComponentType));
      this.getChunkStoreRegistry().registerSystem(new EntityChunk.EntityChunkLoadingSystem());
      this.getChunkStoreRegistry().registerSystem(new BlockComponentChunk.BlockComponentChunkLoadingSystem());
      this.getChunkStoreRegistry().registerSystem(new BlockComponentChunk.LoadBlockComponentPacketSystem(this.blockComponentChunkComponentType));
      this.getChunkStoreRegistry().registerSystem(new BlockComponentChunk.UnloadBlockComponentPacketSystem(this.blockComponentChunkComponentType));
      this.getEventRegistry().register(LoadedAssetsEvent.class, GameplayConfig.class, event -> WorldMapManager.sendSettingsToAllWorlds());
   }

   public ComponentType<ChunkStore, WorldChunk> getWorldChunkComponentType() {
      return this.worldChunkComponentType;
   }

   public ComponentType<ChunkStore, BlockChunk> getBlockChunkComponentType() {
      return this.blockChunkComponentType;
   }

   public ComponentType<ChunkStore, EntityChunk> getEntityChunkComponentType() {
      return this.entityChunkComponentType;
   }

   public ComponentType<ChunkStore, BlockComponentChunk> getBlockComponentChunkComponentType() {
      return this.blockComponentChunkComponentType;
   }

   public ComponentType<ChunkStore, EnvironmentChunk> getEnvironmentChunkComponentType() {
      return this.environmentChunkComponentType;
   }

   public ComponentType<ChunkStore, ChunkColumn> getChunkColumnComponentType() {
      return this.chunkColumnComponentType;
   }

   public ComponentType<ChunkStore, ChunkSection> getChunkSectionComponentType() {
      return this.chunkSectionComponentType;
   }

   public ComponentType<ChunkStore, BlockSection> getBlockSectionComponentType() {
      return this.blockSectionComponentType;
   }

   public ComponentType<ChunkStore, FluidSection> getFluidSectionComponentType() {
      return this.fluidSectionComponentType;
   }

   public ComponentType<ChunkStore, BlockPositionProvider> getBlockPositionProviderComponentType() {
      return this.blockPositionProviderComponentType;
   }
}
