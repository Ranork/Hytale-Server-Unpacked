package com.hypixel.hytale.server.core.modules.prefabspawner;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.data.unknown.UnknownComponents;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.prefabspawner.commands.PrefabSpawnerCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabSpawnerModule extends JavaPlugin {
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(PrefabSpawnerModule.class).depends(BlockModule.class).build();
   private static PrefabSpawnerModule INSTANCE;
   private ComponentType<ChunkStore, PrefabSpawnerBlock> prefabSpawnerBlockType;

   public static PrefabSpawnerModule get() {
      return INSTANCE;
   }

   public PrefabSpawnerModule(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      INSTANCE = this;
      this.getCommandRegistry().registerCommand(new PrefabSpawnerCommand());
      this.prefabSpawnerBlockType = this.getChunkStoreRegistry().registerComponent(PrefabSpawnerBlock.class, "PrefabSpawner", PrefabSpawnerBlock.CODEC);
      this.getChunkStoreRegistry().registerSystem(new PrefabSpawnerModule.MigratePrefabSpawn());
   }

   public ComponentType<ChunkStore, PrefabSpawnerBlock> getPrefabSpawnerBlockType() {
      return this.prefabSpawnerBlockType;
   }

   public static class MigratePrefabSpawn extends BlockModule.MigrationSystem {
      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
         UnknownComponents<ChunkStore> unknownComponents = holder.getComponent(ChunkStore.REGISTRY.getUnknownComponentType());

         assert unknownComponents != null;

         PrefabSpawnerBlock prefabSpawnerBlock = unknownComponents.removeComponent("prefabspawner", PrefabSpawnerBlock.CODEC);
         if (prefabSpawnerBlock != null) {
            holder.putComponent(PrefabSpawnerBlock.getComponentType(), prefabSpawnerBlock);
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
      }

      @Nullable
      @Override
      public Query<ChunkStore> getQuery() {
         return ChunkStore.REGISTRY.getUnknownComponentType();
      }
   }
}
