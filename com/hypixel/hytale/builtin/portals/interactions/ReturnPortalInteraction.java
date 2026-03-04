package com.hypixel.hytale.builtin.portals.interactions;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.builtin.portals.utils.CursedItems;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReturnPortalInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final Duration MINIMUM_TIME_IN_WORLD = Duration.ofSeconds(15L);
   @Nonnull
   public static final Duration WARNING_TIME = Duration.ofSeconds(4L);
   @Nonnull
   public static final BuilderCodec<ReturnPortalInteraction> CODEC = BuilderCodec.builder(
         ReturnPortalInteraction.class, ReturnPortalInteraction::new, SimpleBlockInteraction.CODEC
      )
      .build();
   @Nonnull
   private static final Message MESSAGE_PORTALS_ATTUNING_TO_WORLD = Message.translation("server.portals.attuningToWorld");
   @Nonnull
   private static final Message MESSAGE_PORTALS_DEVICE_NOT_IN_PORTAL_WORLD = Message.translation("server.portals.device.notInPortalWorld");

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
      Ref<EntityStore> ref = context.getEntity();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent == null) {
         context.getState().state = InteractionState.Failed;
      } else {
         long elapsedNanosInWorld = playerComponent.getSinceLastSpawnNanos();
         if (elapsedNanosInWorld < MINIMUM_TIME_IN_WORLD.toNanos()) {
            if (elapsedNanosInWorld > WARNING_TIME.toNanos()) {
               playerComponent.sendMessage(MESSAGE_PORTALS_ATTUNING_TO_WORLD);
            }

            context.getState().state = InteractionState.Failed;
         } else {
            PortalWorld portalWorld = commandBuffer.getResource(PortalWorld.getResourceType());
            if (!portalWorld.exists()) {
               playerComponent.sendMessage(MESSAGE_PORTALS_DEVICE_NOT_IN_PORTAL_WORLD);
               context.getState().state = InteractionState.Failed;
            } else {
               CursedItems.uncurseAll(playerComponent.getInventory().getCombinedEverything());
               InstancesPlugin.exitInstance(ref, commandBuffer);
            }
         }
      }
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
      context.getState().state = InteractionState.Failed;
   }

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
   }
}
