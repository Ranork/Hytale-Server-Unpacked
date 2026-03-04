package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.state.LaunchPad;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LaunchPadInteraction extends SimpleBlockInteraction {
   public static final BuilderCodec<LaunchPadInteraction> CODEC = BuilderCodec.builder(
         LaunchPadInteraction.class, LaunchPadInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Applies the launchpad forces.")
      .build();

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
   }

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
      WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
      if (chunk != null) {
         BlockPosition baseTargetBlock = world.getBaseBlock(new BlockPosition(targetBlock.x, targetBlock.y, targetBlock.z));
         Ref<ChunkStore> blockEntityRef = chunk.getBlockComponentEntity(baseTargetBlock.x, baseTargetBlock.y, baseTargetBlock.z);
         if (blockEntityRef != null) {
            LaunchPad launchPadState = blockEntityRef.getStore().getComponent(blockEntityRef, LaunchPad.getComponentType());
            if (launchPadState != null) {
               Ref<EntityStore> ref = context.getEntity();
               Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
               if (!launchPadState.isPlayersOnly() || playerComponent != null) {
                  Velocity velocityComponent = commandBuffer.getComponent(ref, Velocity.getComponentType());

                  assert velocityComponent != null;

                  velocityComponent.addInstruction(
                     new Vector3d(launchPadState.getVelocityX(), launchPadState.getVelocityY(), launchPadState.getVelocityZ()), null, ChangeVelocityType.Set
                  );
                  Vector3d particlePos = targetBlock.toVector3d().add(0.5, 0.5, 0.5);
                  SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = commandBuffer.getResource(
                     EntityModule.get().getPlayerSpatialResourceType()
                  );
                  ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
                  playerSpatialResource.getSpatialStructure().collect(particlePos, 75.0, results);
                  ParticleUtil.spawnParticleEffect("Splash", particlePos, results, commandBuffer);
               }
            }
         }
      }
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }
}
