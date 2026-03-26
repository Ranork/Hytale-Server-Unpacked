package com.hypixel.hytale.builtin.adventure.farming.interactions;

import com.hypixel.hytale.builtin.adventure.farming.states.TilledSoilBlock;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UseWateringCanInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<UseWateringCanInteraction> CODEC = BuilderCodec.builder(
         UseWateringCanInteraction.class, UseWateringCanInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Waters the target farmable block. Supports configurable width and depth for directional area-of-effect watering.")
      .addField(new KeyedCodec<>("Duration", Codec.LONG), (interaction, duration) -> interaction.duration = duration, interaction -> interaction.duration)
      .addField(
         new KeyedCodec<>("RefreshModifiers", Codec.STRING_ARRAY),
         (interaction, refreshModifiers) -> interaction.refreshModifiers = refreshModifiers,
         interaction -> interaction.refreshModifiers
      )
      .addField(new KeyedCodec<>("RadiusX", Codec.INTEGER), (interaction, radiusX) -> interaction.radiusX = radiusX, interaction -> interaction.radiusX)
      .addField(new KeyedCodec<>("RadiusZ", Codec.INTEGER), (interaction, radiusZ) -> interaction.radiusZ = radiusZ, interaction -> interaction.radiusZ)
      .build();
   protected long duration;
   protected String[] refreshModifiers;
   protected int radiusX;
   protected int radiusZ;

   @Override
   protected void interactWithBlock(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nullable ItemStack itemInHand,
      @Nonnull Vector3i targetBlock,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      WorldTimeResource worldTimeResource = commandBuffer.getResource(WorldTimeResource.getResourceType());
      Instant gameTime = worldTimeResource.getGameTime();
      Instant wateredUntil = gameTime.plus(this.duration, ChronoUnit.SECONDS);
      int facingX = 0;
      int facingZ = -1;
      HeadRotation headRotation = commandBuffer.getComponent(context.getEntity(), HeadRotation.getComponentType());
      if (headRotation != null) {
         Vector3i facing = headRotation.getHorizontalAxisDirection();
         facingX = facing.getX();
         facingZ = facing.getZ();
      }

      if (facingX != 0 && facingZ != 0) {
         facingX = 0;
      }

      int lateralX = facingZ != 0 ? 1 : 0;
      int lateralZ = facingX != 0 ? 1 : 0;
      int width = Math.max(this.radiusX, 1);
      int depth = Math.max(this.radiusZ, 1);
      int halfLeft = (width - 1) / 2;
      int halfRight = width - 1 - halfLeft;
      boolean anyWatered = false;

      for (int forward = 0; forward < depth; forward++) {
         for (int lateral = -halfLeft; lateral <= halfRight; lateral++) {
            int bx = targetBlock.getX() + lateral * lateralX + forward * facingX;
            int bz = targetBlock.getZ() + lateral * lateralZ + forward * facingZ;
            if (this.waterBlockAt(world, bx, targetBlock.getY(), bz, wateredUntil)) {
               anyWatered = true;
            }
         }
      }

      if (!anyWatered) {
         context.getState().state = InteractionState.Failed;
      }
   }

   private boolean waterBlockAt(@Nonnull World world, int x, int y, int z, @Nonnull Instant wateredUntil) {
      long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
      WorldChunk worldChunk = world.getChunk(chunkIndex);
      if (worldChunk == null) {
         return false;
      } else {
         Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
         Ref<ChunkStore> blockRef = worldChunk.getBlockComponentEntity(x, y, z);
         if (blockRef == null) {
            blockRef = BlockModule.ensureBlockEntity(worldChunk, x, y, z);
         }

         if (blockRef != null && blockRef.isValid()) {
            TilledSoilBlock tilledSoilBlockComponent = chunkStore.getComponent(blockRef, TilledSoilBlock.getComponentType());
            if (tilledSoilBlockComponent != null) {
               tilledSoilBlockComponent.setWateredUntil(wateredUntil);
               worldChunk.setTicking(x, y, z, true);
               worldChunk.getBlockChunk().getSectionAtBlockY(y).scheduleTick(ChunkUtil.indexBlock(x, y, z), wateredUntil);
               worldChunk.setTicking(x, y + 1, z, true);
               return true;
            }
         }

         Ref<ChunkStore> soilBlockRef = worldChunk.getBlockComponentEntity(x, y - 1, z);
         if (soilBlockRef != null && soilBlockRef.isValid()) {
            TilledSoilBlock tilledSoilBlockComponent = chunkStore.getComponent(soilBlockRef, TilledSoilBlock.getComponentType());
            if (tilledSoilBlockComponent == null) {
               return false;
            } else {
               tilledSoilBlockComponent.setWateredUntil(wateredUntil);
               worldChunk.getBlockChunk().getSectionAtBlockY(y - 1).scheduleTick(ChunkUtil.indexBlock(x, y - 1, z), wateredUntil);
               worldChunk.setTicking(x, y - 1, z, true);
               worldChunk.setTicking(x, y, z, true);
               return true;
            }
         } else {
            return false;
         }
      }
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }
}
