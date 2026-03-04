package com.hypixel.hytale.server.core.modules.projectile.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.BallisticData;
import com.hypixel.hytale.server.core.modules.projectile.config.BallisticDataProvider;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.PositionUtil;
import com.hypixel.hytale.server.core.util.TargetUtil;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ProjectileInteraction extends SimpleInstantInteraction implements BallisticDataProvider {
   @Nonnull
   public static final BuilderCodec<ProjectileInteraction> CODEC = BuilderCodec.builder(
         ProjectileInteraction.class, ProjectileInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Fires a projectile.")
      .<String>appendInherited(new KeyedCodec<>("Config", Codec.STRING), (o, i) -> o.config = i, o -> o.config, (o, p) -> o.config = p.config)
      .addValidator(ProjectileConfig.VALIDATOR_CACHE.getValidator().late())
      .documentation("The ID of the projectile config asset to use for the projectile.")
      .add()
      .build();
   protected String config;

   @Nullable
   public ProjectileConfig getConfig() {
      return ProjectileConfig.getAssetMap().getAsset(this.config);
   }

   @Nullable
   @Override
   public BallisticData getBallisticData() {
      return this.getConfig();
   }

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Client;
   }

   @Override
   public boolean needsRemoteSync() {
      return true;
   }

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      ProjectileConfig config = this.getConfig();
      if (config != null) {
         InteractionSyncData clientState = context.getClientState();
         Ref<EntityStore> ref = context.getEntity();
         CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

         assert commandBuffer != null;

         boolean hasClientState = clientState != null && clientState.attackerPos != null && clientState.attackerRot != null;
         Vector3d position;
         Vector3d direction;
         UUID generatedUUID;
         if (hasClientState) {
            position = PositionUtil.toVector3d(clientState.attackerPos);
            Vector3f lookVec = PositionUtil.toRotation(clientState.attackerRot);
            direction = new Vector3d(lookVec.getYaw(), lookVec.getPitch());
            generatedUUID = clientState.generatedUUID;
         } else {
            Transform lookVec = TargetUtil.getLook(ref, commandBuffer);
            position = lookVec.getPosition();
            direction = lookVec.getDirection();
            generatedUUID = null;
         }

         ProjectileModule.get().spawnProjectile(generatedUUID, ref, commandBuffer, config, position, direction);
      }
   }

   @Override
   protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Ref<EntityStore> ref = context.getEntity();
      Transform lookVec = TargetUtil.getLook(ref, commandBuffer);
      InteractionSyncData state = context.getState();
      state.attackerPos = PositionUtil.toPositionPacket(lookVec.getPosition());
      Vector3f rotation = lookVec.getRotation();
      state.attackerRot = new Direction(rotation.getYaw(), rotation.getPitch(), rotation.getRoll());
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.ProjectileInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.ProjectileInteraction p = (com.hypixel.hytale.protocol.ProjectileInteraction)packet;
      ProjectileConfig config = this.getConfig();
      if (config == null) {
         throw new IllegalStateException("ProjectileInteraction '" + this.getId() + "' has no valid ProjectileConfig: " + this.config);
      } else {
         p.configId = this.config;
      }
   }
}
