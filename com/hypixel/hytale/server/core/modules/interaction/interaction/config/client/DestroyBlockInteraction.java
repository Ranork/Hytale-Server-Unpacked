package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionValidation;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public class DestroyBlockInteraction extends SimpleInstantInteraction {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final BuilderCodec<DestroyBlockInteraction> CODEC = BuilderCodec.builder(
         DestroyBlockInteraction.class, DestroyBlockInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Destroys the target block.")
      .build();

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      BlockPosition blockPosition = context.getTargetBlock();
      if (blockPosition != null) {
         Ref<EntityStore> ref = context.getEntity();
         CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

         assert commandBuffer != null;

         if (!InteractionValidation.canPlayerInteractWithBlock(ref, commandBuffer, context.getHeldItem(), blockPosition)) {
            LOGGER.at(Level.WARNING)
               .log(
                  "Entity %d failed destroy block interaction distance check at [%d, %d, %d]",
                  ref.getIndex(),
                  blockPosition.x,
                  blockPosition.y,
                  blockPosition.z
               );
            context.getState().state = InteractionState.Failed;
         } else {
            World world = commandBuffer.getExternalData().getWorld();
            ChunkStore chunkStore = world.getChunkStore();
            long chunkIndex = ChunkUtil.indexChunkFromBlock(blockPosition.x, blockPosition.z);
            Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
            if (chunkReference != null) {
               Vector3i position = new Vector3i(blockPosition.x, blockPosition.y, blockPosition.z);
               Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
               BlockHarvestUtils.performBlockBreak(ref, null, position, chunkReference, context.getCommandBuffer(), chunkStoreStore);
            }
         }
      }
   }
}
