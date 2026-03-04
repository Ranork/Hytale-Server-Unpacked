package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.simple;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionTarget;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class RemoveEntityInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<RemoveEntityInteraction> CODEC = BuilderCodec.builder(
         RemoveEntityInteraction.class, RemoveEntityInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Despawns the given entity.")
      .<InteractionTarget>appendInherited(
         new KeyedCodec<>("Entity", InteractionTarget.CODEC), (o, i) -> o.entityTarget = i, o -> o.entityTarget, (o, p) -> o.entityTarget = p.entityTarget
      )
      .documentation("The entity to target for this interaction.")
      .addValidator(Validators.nonNull())
      .add()
      .build();
   @Nonnull
   private InteractionTarget entityTarget = InteractionTarget.USER;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Ref<EntityStore> ref = context.getEntity();
      Ref<EntityStore> targetRef = this.entityTarget.getEntity(context, ref);
      if (targetRef != null && targetRef.isValid()) {
         if (!commandBuffer.getArchetype(targetRef).contains(Player.getComponentType())) {
            World world = commandBuffer.getExternalData().getWorld();
            world.execute(() -> {
               if (targetRef.isValid()) {
                  Store<EntityStore> store = world.getEntityStore().getStore();
                  store.removeEntity(targetRef, RemoveReason.REMOVE);
               }
            });
         }
      }
   }

   @Override
   protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.RemoveEntityInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.RemoveEntityInteraction p = (com.hypixel.hytale.protocol.RemoveEntityInteraction)packet;
      p.entityTarget = this.entityTarget.toProtocol();
   }

   @Nonnull
   @Override
   public String toString() {
      return "RemoveEntityInteraction{entityTarget=" + this.entityTarget + "} " + super.toString();
   }
}
