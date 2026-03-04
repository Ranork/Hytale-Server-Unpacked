package com.hypixel.hytale.server.npc.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ContextualUseNPCInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<ContextualUseNPCInteraction> CODEC = BuilderCodec.builder(
         ContextualUseNPCInteraction.class, ContextualUseNPCInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Interacts with the target NPC passing in context for it to use.")
      .<String>appendInherited(
         new KeyedCodec<>("Context", Codec.STRING),
         (interaction, s) -> interaction.context = s,
         interaction -> interaction.context,
         (interaction, parent) -> interaction.context = parent.context
      )
      .documentation("The provided context for the use action.")
      .addValidator(Validators.nonNull())
      .add()
      .build();
   protected String context;

   public ContextualUseNPCInteraction(String id) {
      super(id);
   }

   protected ContextualUseNPCInteraction() {
   }

   @Override
   protected final void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> targetRef = context.getTargetEntity();
      if (targetRef == null) {
         context.getState().state = InteractionState.Failed;
      } else {
         CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
         Ref<EntityStore> ref = context.getEntity();
         Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
         if (playerComponent == null) {
            HytaleLogger.getLogger().at(Level.INFO).log("UseNPCInteraction requires a Player but was used for: %s", ref);
            context.getState().state = InteractionState.Failed;
         } else {
            NPCEntity npcComponent = commandBuffer.getComponent(targetRef, NPCEntity.getComponentType());
            if (npcComponent == null) {
               HytaleLogger.getLogger().at(Level.INFO).log("UseNPCInteraction requires a target NPC");
               context.getState().state = InteractionState.Failed;
            } else if (!npcComponent.getRole().getStateSupport().willInteractWith(ref)) {
               context.getState().state = InteractionState.Failed;
            } else {
               npcComponent.getRole().getStateSupport().addContextualInteraction(ref, this.context);
            }
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ContextualUseNPCInteraction{} " + super.toString();
   }
}
