package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockFace;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.BlockRotation;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SimpleBlockInteraction extends SimpleInteraction {
   @Nonnull
   public static final BuilderCodec<SimpleBlockInteraction> CODEC = BuilderCodec.abstractBuilder(SimpleBlockInteraction.class, SimpleInteraction.CODEC)
      .appendInherited(
         new KeyedCodec<>("UseLatestTarget", Codec.BOOLEAN),
         (interaction, s) -> interaction.useLatestTarget = s,
         interaction -> interaction.useLatestTarget,
         (interaction, parent) -> interaction.useLatestTarget = parent.useLatestTarget
      )
      .documentation("Determines whether to use the clients latest target block position for this interaction.")
      .add()
      .build();
   private boolean useLatestTarget = false;

   public SimpleBlockInteraction(@Nonnull String id) {
      super(id);
   }

   protected SimpleBlockInteraction() {
   }

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Client;
   }

   @Override
   protected void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      if (firstRun) {
         Ref<EntityStore> ref = context.getEntity();
         CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

         assert commandBuffer != null;

         World world = commandBuffer.getExternalData().getWorld();
         if (this.useLatestTarget) {
            InteractionSyncData clientState = context.getClientState();
            if (clientState == null || clientState.blockPosition == null) {
               context.getState().state = InteractionState.Failed;
               super.tick0(firstRun, time, type, context, cooldownHandler);
               return;
            }

            BlockPosition latestBlockPos = clientState.blockPosition;
            TransformComponent transformComponent = commandBuffer.getComponent(ref, TransformComponent.getComponentType());

            assert transformComponent != null;

            double distanceSquared = transformComponent.getPosition().distanceSquaredTo(latestBlockPos.x + 0.5, latestBlockPos.y + 0.5, latestBlockPos.z + 0.5);
            BlockPosition baseBlock = world.getBaseBlock(latestBlockPos);
            context.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK, baseBlock);
            context.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK_RAW, latestBlockPos);
         }

         BlockPosition targetBlockPos = context.getTargetBlock();
         if (targetBlockPos == null) {
            context.getState().state = InteractionState.Failed;
            super.tick0(firstRun, time, type, context, cooldownHandler);
         } else if (EntityUtils.getEntity(ref, commandBuffer) instanceof LivingEntity livingEntity) {
            Inventory inventory = livingEntity.getInventory();
            ItemStack itemInHand = inventory.getItemInHand();
            Vector3i var21 = new Vector3i(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
            WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(var21.x, var21.z));
            if (chunk == null) {
               context.getState().state = InteractionState.Failed;
               super.tick0(firstRun, time, type, context, cooldownHandler);
            } else {
               int blockId = chunk.getBlock(var21);
               if (blockId != 1 && blockId != 0) {
                  this.interactWithBlock(world, commandBuffer, type, context, itemInHand, var21, cooldownHandler);
                  super.tick0(firstRun, time, type, context, cooldownHandler);
               } else {
                  context.getState().state = InteractionState.Failed;
                  super.tick0(firstRun, time, type, context, cooldownHandler);
               }
            }
         }
      }
   }

   protected abstract void interactWithBlock(
      @Nonnull World var1,
      @Nonnull CommandBuffer<EntityStore> var2,
      @Nonnull InteractionType var3,
      @Nonnull InteractionContext var4,
      @Nullable ItemStack var5,
      @Nonnull Vector3i var6,
      @Nonnull CooldownHandler var7
   );

   @Override
   protected void simulateTick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      if (firstRun) {
         Ref<EntityStore> ref = context.getEntity();
         CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

         assert commandBuffer != null;

         World world = commandBuffer.getExternalData().getWorld();
         if (EntityUtils.getEntity(ref, commandBuffer) instanceof LivingEntity livingEntity) {
            Inventory inventory = livingEntity.getInventory();
            ItemStack itemInHand = inventory.getItemInHand();
            context.getState().blockFace = BlockFace.Up;
            BlockPosition contextTargetBlock = context.getTargetBlock();
            Vector3i targetBlock;
            if (contextTargetBlock == null) {
               targetBlock = TargetUtil.getTargetBlock(ref, 8.0, commandBuffer);
               if (targetBlock == null) {
                  context.getState().state = InteractionState.Failed;
                  super.tick0(firstRun, time, type, context, cooldownHandler);
                  return;
               }

               context.getState().blockPosition = new BlockPosition(targetBlock.x, targetBlock.y, targetBlock.z);
            } else {
               context.getState().blockPosition = contextTargetBlock;
               targetBlock = new Vector3i(contextTargetBlock.x, contextTargetBlock.y, contextTargetBlock.z);
            }

            WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
            if (chunk == null) {
               context.getState().state = InteractionState.Failed;
               super.tick0(firstRun, time, type, context, cooldownHandler);
            } else {
               int blockId = chunk.getBlock(targetBlock);
               if (blockId != 1 && blockId != 0) {
                  this.simulateInteractWithBlock(type, context, itemInHand, world, targetBlock);
                  super.tick0(firstRun, time, type, context, cooldownHandler);
               } else {
                  context.getState().state = InteractionState.Failed;
                  super.tick0(firstRun, time, type, context, cooldownHandler);
               }
            }
         }
      }
   }

   protected abstract void simulateInteractWithBlock(
      @Nonnull InteractionType var1, @Nonnull InteractionContext var2, @Nullable ItemStack var3, @Nonnull World var4, @Nonnull Vector3i var5
   );

   protected void computeCurrentBlockSyncData(@Nonnull InteractionContext context) {
      BlockPosition targetBlockPos = context.getTargetBlock();
      if (targetBlockPos != null) {
         CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

         assert commandBuffer != null;

         World world = commandBuffer.getStore().getExternalData().getWorld();
         ChunkStore chunkStore = world.getChunkStore();
         long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlockPos.x, targetBlockPos.z);
         Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
         if (chunkReference != null && chunkReference.isValid()) {
            BlockChunk blockChunk = chunkStore.getStore().getComponent(chunkReference, BlockChunk.getComponentType());
            if (targetBlockPos.y >= 0 && targetBlockPos.y < 320) {
               BlockSection section = blockChunk.getSectionAtBlockY(targetBlockPos.y);
               context.getState().blockPosition = new BlockPosition(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
               context.getState().placedBlockId = section.get(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
               RotationTuple resultRotation = section.getRotation(targetBlockPos.x, targetBlockPos.y, targetBlockPos.z);
               context.getState().blockRotation = new BlockRotation(
                  resultRotation.yaw().toPacket(), resultRotation.pitch().toPacket(), resultRotation.roll().toPacket()
               );
            }
         }
      }
   }

   @Nonnull
   @Override
   protected com.hypixel.hytale.protocol.Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.SimpleBlockInteraction();
   }

   @Override
   protected void configurePacket(com.hypixel.hytale.protocol.Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.SimpleBlockInteraction p = (com.hypixel.hytale.protocol.SimpleBlockInteraction)packet;
      p.useLatestTarget = this.useLatestTarget;
   }

   @Override
   public boolean needsRemoteSync() {
      return true;
   }

   @Nonnull
   @Override
   public String toString() {
      return "SimpleBlockInteraction{} " + super.toString();
   }
}
