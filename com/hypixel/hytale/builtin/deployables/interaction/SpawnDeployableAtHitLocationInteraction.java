package com.hypixel.hytale.builtin.deployables.interaction;

import com.hypixel.hytale.builtin.deployables.DeployablesUtils;
import com.hypixel.hytale.builtin.deployables.config.DeployableConfig;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.InteractionChainData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class SpawnDeployableAtHitLocationInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<SpawnDeployableAtHitLocationInteraction> CODEC = BuilderCodec.builder(
         SpawnDeployableAtHitLocationInteraction.class, SpawnDeployableAtHitLocationInteraction::new, SimpleInstantInteraction.CODEC
      )
      .append(new KeyedCodec<>("Config", DeployableConfig.CODEC), (i, s) -> i.config = s, i -> i.config)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   private DeployableConfig config;

   @Override
   public boolean needsRemoteSync() {
      return false;
   }

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      InteractionChain contextChain = context.getChain();

      assert contextChain != null;

      InteractionChainData chainData = contextChain.getChainData();
      if (chainData.hitLocation != null && chainData.hitNormal != null) {
         Ref<EntityStore> owningEntityRef = context.getOwningEntity();
         Ref<EntityStore> deployerRef = owningEntityRef != null ? owningEntityRef : context.getEntity();
         CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

         assert commandBuffer != null;

         Store<EntityStore> store = commandBuffer.getStore();
         Vector3d hitNormalVec = new Vector3d(chainData.hitNormal.x(), chainData.hitNormal.y(), chainData.hitNormal.z());
         DeployablesUtils.spawnDeployable(
            commandBuffer,
            store,
            this.config,
            deployerRef,
            new Vector3d(chainData.hitLocation.x(), chainData.hitLocation.y(), chainData.hitLocation.z()),
            MathUtil.getRotationForHitNormal(hitNormalVec),
            MathUtil.getNameForHitNormal(hitNormalVec)
         );
      }
   }
}
