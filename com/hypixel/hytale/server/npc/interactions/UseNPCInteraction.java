package com.hypixel.hytale.server.npc.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.interaction.InteractionView;
import com.hypixel.hytale.server.npc.blackboard.view.interaction.ReservationStatus;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class UseNPCInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<UseNPCInteraction> CODEC = BuilderCodec.builder(
         UseNPCInteraction.class, UseNPCInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Interacts with a target NPC.")
      .build();
   public static final String DEFAULT_ID = "*UseNPC";
   public static final RootInteraction DEFAULT_ROOT = new RootInteraction("*UseNPC", "*UseNPC");

   public UseNPCInteraction(String id) {
      super(id);
   }

   protected UseNPCInteraction() {
   }

   @Override
   protected final void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> ref = context.getEntity();
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent == null) {
         HytaleLogger.getLogger().at(Level.INFO).log("UseNPCInteraction requires a Player but was used for: %s", ref);
         context.getState().state = InteractionState.Failed;
      } else {
         Ref<EntityStore> targetRef = context.getTargetEntity();
         if (targetRef == null) {
            context.getState().state = InteractionState.Failed;
         } else {
            NPCEntity npcComponent = commandBuffer.getComponent(targetRef, NPCEntity.getComponentType());
            if (npcComponent == null) {
               HytaleLogger.getLogger().at(Level.INFO).log("UseNPCInteraction requires a target NPCEntity but was used for: %s", targetRef);
               context.getState().state = InteractionState.Failed;
            } else if (!npcComponent.getRole().getStateSupport().willInteractWith(ref)) {
               context.getState().state = InteractionState.Failed;
            } else {
               InteractionView interactionView = commandBuffer.getResource(Blackboard.getResourceType()).getView(InteractionView.class, 0L);
               if (interactionView.getReservationStatus(targetRef, ref, commandBuffer) == ReservationStatus.RESERVED_OTHER) {
                  playerComponent.sendMessage(Message.translation("server.npc.npc.isBusy").param("roleName", npcComponent.getRoleName()));
                  context.getState().state = InteractionState.Failed;
               } else {
                  npcComponent.getRole().getStateSupport().addInteraction(playerComponent);
               }
            }
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "UseNPCInteraction{} " + super.toString();
   }
}
