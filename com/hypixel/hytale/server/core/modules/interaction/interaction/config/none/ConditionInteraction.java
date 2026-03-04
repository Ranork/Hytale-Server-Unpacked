package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConditionInteraction extends SimpleInteraction {
   @Nonnull
   public static final BuilderCodec<ConditionInteraction> CODEC = BuilderCodec.builder(
         ConditionInteraction.class, ConditionInteraction::new, SimpleInteraction.CODEC
      )
      .documentation("An interaction that is successful if the given conditions are met.")
      .appendInherited(
         new KeyedCodec<>("RequiredGameMode", ProtocolCodecs.GAMEMODE),
         (interaction, s) -> interaction.requiredGameMode = s,
         interaction -> interaction.requiredGameMode,
         (interaction, parent) -> interaction.requiredGameMode = parent.requiredGameMode
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Jumping", Codec.BOOLEAN),
         (interaction, s) -> interaction.jumping = s,
         interaction -> interaction.jumping,
         (interaction, parent) -> interaction.jumping = parent.jumping
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Swimming", Codec.BOOLEAN),
         (interaction, s) -> interaction.swimming = s,
         interaction -> interaction.swimming,
         (interaction, parent) -> interaction.swimming = parent.swimming
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Crouching", Codec.BOOLEAN),
         (interaction, s) -> interaction.crouching = s,
         interaction -> interaction.crouching,
         (interaction, parent) -> interaction.crouching = parent.crouching
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Running", Codec.BOOLEAN),
         (interaction, s) -> interaction.running = s,
         interaction -> interaction.running,
         (interaction, parent) -> interaction.running = parent.running
      )
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("Flying", Codec.BOOLEAN),
         (interaction, s) -> interaction.flying = s,
         interaction -> interaction.flying,
         (interaction, parent) -> interaction.flying = parent.flying
      )
      .documentation("Whether the entity can be flying.")
      .add()
      .build();
   @Nullable
   private GameMode requiredGameMode;
   @Nullable
   private Boolean jumping;
   @Nullable
   private Boolean swimming;
   @Nullable
   private Boolean crouching;
   @Nullable
   private Boolean running;
   @Nullable
   private Boolean flying;

   @Override
   protected void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      boolean success = true;
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Ref<EntityStore> ref = context.getEntity();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (this.requiredGameMode != null && playerComponent != null && this.requiredGameMode != playerComponent.getGameMode()) {
         success = false;
      }

      MovementStatesComponent movementStatesComponent = commandBuffer.getComponent(ref, MovementStatesComponent.getComponentType());

      assert movementStatesComponent != null;

      MovementStates movementStates = movementStatesComponent.getMovementStates();
      if (this.jumping != null && this.jumping != movementStates.jumping) {
         success = false;
      }

      if (this.swimming != null && this.swimming != movementStates.swimming) {
         success = false;
      }

      if (this.crouching != null && this.crouching != movementStates.crouching) {
         success = false;
      }

      if (this.running != null && this.running != movementStates.running) {
         success = false;
      }

      if (this.flying != null && this.flying != movementStates.flying) {
         success = false;
      }

      context.getState().state = success ? InteractionState.Finished : InteractionState.Failed;
      super.tick0(firstRun, time, type, context, cooldownHandler);
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.ConditionInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.ConditionInteraction p = (com.hypixel.hytale.protocol.ConditionInteraction)packet;
      p.requiredGameMode = this.requiredGameMode;
      p.jumping = this.jumping;
      p.swimming = this.swimming;
      p.crouching = this.crouching;
      p.running = this.running;
      p.flying = this.flying;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ConditionInteraction{requiredGameMode="
         + this.requiredGameMode
         + ", jumping="
         + this.jumping
         + ", swimming="
         + this.swimming
         + ", crouching="
         + this.crouching
         + ", running="
         + this.running
         + ", flying="
         + this.flying
         + "} "
         + super.toString();
   }
}
