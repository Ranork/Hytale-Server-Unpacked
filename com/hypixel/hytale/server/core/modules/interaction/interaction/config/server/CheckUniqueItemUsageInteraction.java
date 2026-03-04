package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.data.UniqueItemUsagesComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import javax.annotation.Nonnull;

public class CheckUniqueItemUsageInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<CheckUniqueItemUsageInteraction> CODEC = BuilderCodec.builder(
         CheckUniqueItemUsageInteraction.class, CheckUniqueItemUsageInteraction::new, SimpleInstantInteraction.CODEC
      )
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
      PlayerRef playerRefComponent = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
      if (playerRefComponent == null) {
         context.getState().state = InteractionState.Failed;
      } else {
         UniqueItemUsagesComponent uniqueItemUsagesComponent = commandBuffer.getComponent(ref, UniqueItemUsagesComponent.getComponentType());

         assert uniqueItemUsagesComponent != null;

         if (uniqueItemUsagesComponent.hasUsedUniqueItem(context.getHeldItem().getItemId())) {
            context.getState().state = InteractionState.Failed;
            NotificationUtil.sendNotification(
               playerRefComponent.getPacketHandler(), Message.translation("server.commands.checkUniqueItemUsage.uniqueItemAlreadyUsed")
            );
         } else {
            uniqueItemUsagesComponent.recordUniqueItemUsage(context.getHeldItem().getItemId());
            context.getState().state = InteractionState.Finished;
         }
      }
   }

   @Override
   public String toString() {
      return "CheckUniqueItemUsageInteraction{}" + super.toString();
   }
}
