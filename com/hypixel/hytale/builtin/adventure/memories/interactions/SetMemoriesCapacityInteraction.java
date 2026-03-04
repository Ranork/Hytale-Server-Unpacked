package com.hypixel.hytale.builtin.adventure.memories.interactions;

import com.hypixel.hytale.builtin.adventure.memories.component.PlayerMemories;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.protocol.packets.player.UpdateMemoriesFeatureStatus;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import javax.annotation.Nonnull;

public class SetMemoriesCapacityInteraction extends SimpleInstantInteraction {
   @Nonnull
   private static final String NOTIFICATION_ICON_MEMORIES = "NotificationIcons/MemoriesIcon.png";
   @Nonnull
   private static final Message MESSAGE_SERVER_MEMORIES_GENERAL_FEATURE_UNLOCKED_NOTIFICATION = Message.translation(
      "server.memories.general.featureUnlockedNotification"
   );
   @Nonnull
   private static final Message MESSAGE_SERVER_MEMORIES_GENERAL_FEATURE_UNLOCKED_MESSAGE = Message.translation("server.memories.general.featureUnlockedMessage");
   @Nonnull
   public static final BuilderCodec<SetMemoriesCapacityInteraction> CODEC = BuilderCodec.builder(
         SetMemoriesCapacityInteraction.class, SetMemoriesCapacityInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Sets how many memories a player can store.")
      .<Integer>appendInherited(
         new KeyedCodec<>("Capacity", Codec.INTEGER), (i, s) -> i.capacity = s, i -> i.capacity, (i, parent) -> i.capacity = parent.capacity
      )
      .documentation("Defines the amount of memories that a player can store.")
      .add()
      .build();
   private int capacity;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> ref = context.getEntity();
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      PlayerMemories memoriesComponent = commandBuffer.ensureAndGetComponent(ref, PlayerMemories.getComponentType());
      if (this.capacity <= memoriesComponent.getMemoriesCapacity()) {
         context.getState().state = InteractionState.Failed;
      } else {
         int previousCapacity = memoriesComponent.getMemoriesCapacity();
         memoriesComponent.setMemoriesCapacity(this.capacity);
         if (previousCapacity <= 0) {
            PlayerRef playerRefComponent = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
            if (playerRefComponent != null) {
               PacketHandler playerConnection = playerRefComponent.getPacketHandler();
               playerConnection.writeNoCache(new UpdateMemoriesFeatureStatus(true));
               NotificationUtil.sendNotification(
                  playerConnection, MESSAGE_SERVER_MEMORIES_GENERAL_FEATURE_UNLOCKED_NOTIFICATION, null, "NotificationIcons/MemoriesIcon.png"
               );
               playerRefComponent.sendMessage(MESSAGE_SERVER_MEMORIES_GENERAL_FEATURE_UNLOCKED_MESSAGE);
            }
         }

         context.getState().state = InteractionState.Finished;
      }
   }

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
   }

   @Override
   public String toString() {
      return super.toString();
   }
}
