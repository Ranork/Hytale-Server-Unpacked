package com.hypixel.hytale.builtin.adventure.teleporter.system;

import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.builtin.adventure.teleporter.util.CannedWarpNames;
import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.builtin.teleport.Warp;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.components.PlacedByInteractionComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CreateWarpWhenTeleporterPlacedSystem extends RefChangeSystem<ChunkStore, PlacedByInteractionComponent> {
   @Nonnull
   private final ComponentType<ChunkStore, PlacedByInteractionComponent> placedByInteractionComponentType;
   @Nonnull
   private final ComponentType<ChunkStore, Teleporter> teleporterComponentType;
   @Nonnull
   private final ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType;
   @Nonnull
   private final ComponentType<EntityStore, PlayerRef> playerRefComponentType;
   @Nonnull
   private final Query<ChunkStore> query;

   public CreateWarpWhenTeleporterPlacedSystem(
      @Nonnull ComponentType<ChunkStore, PlacedByInteractionComponent> placedByInteractionComponentType,
      @Nonnull ComponentType<ChunkStore, Teleporter> teleporterComponentType,
      @Nonnull ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType,
      @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType
   ) {
      this.placedByInteractionComponentType = placedByInteractionComponentType;
      this.teleporterComponentType = teleporterComponentType;
      this.blockStateInfoComponentType = blockStateInfoComponentType;
      this.playerRefComponentType = playerRefComponentType;
      this.query = Query.and(placedByInteractionComponentType, teleporterComponentType, blockStateInfoComponentType);
   }

   public void onComponentAdded(
      @Nonnull Ref<ChunkStore> ref,
      @Nonnull PlacedByInteractionComponent placedBy,
      @Nonnull Store<ChunkStore> chunkStore,
      @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
      World world = chunkStore.getExternalData().getWorld();
      EntityStore entityStore = world.getEntityStore();
      UUID whoPlacedUuid = placedBy.getWhoPlacedUuid();
      Ref<EntityStore> whoPlacedRef = entityStore.getRefFromUUID(whoPlacedUuid);
      if (whoPlacedRef != null && whoPlacedRef.isValid()) {
         PlayerRef playerRefComponent = entityStore.getStore().getComponent(whoPlacedRef, this.playerRefComponentType);
         String language = playerRefComponent == null ? null : playerRefComponent.getLanguage();
         if (language != null) {
            BlockModule.BlockStateInfo blockStateInfoComponent = commandBuffer.getComponent(ref, this.blockStateInfoComponentType);

            assert blockStateInfoComponent != null;

            Ref<ChunkStore> chunkRef = blockStateInfoComponent.getChunkRef();
            if (chunkRef.isValid()) {
               WorldChunk worldChunk = chunkStore.getComponent(chunkRef, WorldChunk.getComponentType());
               if (worldChunk != null) {
                  String cannedName = CannedWarpNames.generateCannedWarpName(ref, language);
                  if (cannedName != null) {
                     createWarp(worldChunk, blockStateInfoComponent, cannedName);
                     Teleporter teleporterComponent = commandBuffer.getComponent(ref, this.teleporterComponentType);

                     assert teleporterComponent != null;

                     teleporterComponent.setOwnedWarp(cannedName);
                  }
               }
            }
         }
      }
   }

   public static void createWarp(@Nonnull WorldChunk worldChunk, @Nonnull BlockModule.BlockStateInfo blockStateInfo, @Nonnull String name) {
      int chunkBlockX = worldChunk.getX() << 5;
      int chunkBlockZ = worldChunk.getZ() << 5;
      int index = blockStateInfo.getIndex();
      int x = chunkBlockX + ChunkUtil.xFromBlockInColumn(index);
      int y = ChunkUtil.yFromBlockInColumn(index);
      int z = chunkBlockZ + ChunkUtil.zFromBlockInColumn(index);
      BlockChunk blockChunkComponent = worldChunk.getBlockChunk();
      if (blockChunkComponent != null) {
         BlockSection section = blockChunkComponent.getSectionAtBlockY(y);
         int rotationIndex = section.getRotationIndex(x, y, z);
         RotationTuple rotationTuple = RotationTuple.get(rotationIndex);
         Rotation rotationYaw = rotationTuple.yaw();
         float warpRotationYaw = (float)rotationYaw.getRadians() + (float)Math.toRadians(180.0);
         Vector3d warpPosition = new Vector3d(x, y, z).add(0.5, 0.65, 0.5);
         Transform warpTransform = new Transform(warpPosition, new Vector3f(Float.NaN, warpRotationYaw, Float.NaN));
         String warpId = name.toLowerCase();
         Warp warp = new Warp(warpTransform, name, worldChunk.getWorld(), "*Teleporter", Instant.now());
         TeleportPlugin teleportPlugin = TeleportPlugin.get();
         teleportPlugin.getWarps().put(warpId, warp);
         teleportPlugin.saveWarps();
      }
   }

   public void onComponentSet(
      @Nonnull Ref<ChunkStore> ref,
      @Nullable PlacedByInteractionComponent oldComponent,
      @Nonnull PlacedByInteractionComponent newComponent,
      @Nonnull Store<ChunkStore> store,
      @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
   }

   public void onComponentRemoved(
      @Nonnull Ref<ChunkStore> ref,
      @Nonnull PlacedByInteractionComponent component,
      @Nonnull Store<ChunkStore> store,
      @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
   }

   @Nonnull
   @Override
   public ComponentType<ChunkStore, PlacedByInteractionComponent> componentType() {
      return this.placedByInteractionComponentType;
   }

   @Nonnull
   @Override
   public Query<ChunkStore> getQuery() {
      return this.query;
   }
}
