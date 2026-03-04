package com.hypixel.hytale.builtin.instances.interactions;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.teleport.PendingTeleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ExitInstanceInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<ExitInstanceInteraction> CODEC = BuilderCodec.builder(
         ExitInstanceInteraction.class, ExitInstanceInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Teleports the **Entity** out of the current **Instance** and places them at their set return point.")
      .build();

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
   }

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Ref<EntityStore> ref = context.getEntity();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent == null || !playerComponent.isWaitingForClientReady()) {
         Archetype<EntityStore> archetype = commandBuffer.getArchetype(ref);
         if (!archetype.contains(Teleport.getComponentType()) && !archetype.contains(PendingTeleport.getComponentType())) {
            InstancesPlugin.exitInstance(ref, commandBuffer);
         }
      }
   }
}
