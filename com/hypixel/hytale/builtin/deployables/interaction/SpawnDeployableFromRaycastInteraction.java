package com.hypixel.hytale.builtin.deployables.interaction;

import com.hypixel.hytale.builtin.deployables.DeployablesUtils;
import com.hypixel.hytale.builtin.deployables.config.DeployableConfig;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.Object2FloatMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpawnDeployableFromRaycastInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<SpawnDeployableFromRaycastInteraction> CODEC = BuilderCodec.builder(
         SpawnDeployableFromRaycastInteraction.class, SpawnDeployableFromRaycastInteraction::new, SimpleInstantInteraction.CODEC
      )
      .append(new KeyedCodec<>("Config", DeployableConfig.CODEC), (i, s) -> i.config = s, i -> i.config)
      .addValidator(Validators.nonNull())
      .add()
      .<Object2FloatMap<String>>append(
         new KeyedCodec<>("PreviewStatConditions", new Object2FloatMapCodec<>(Codec.STRING, Object2FloatOpenHashMap::new)),
         (changeStatInteraction, stringObject2DoubleMap) -> changeStatInteraction.unknownEntityStats = stringObject2DoubleMap,
         changeStatInteraction -> changeStatInteraction.unknownEntityStats
      )
      .addValidator(EntityStatType.VALIDATOR_CACHE.getMapKeyValidator())
      .documentation("Modifiers to apply to EntityStats.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("MaxPlacementDistance", Codec.FLOAT),
         (o, i) -> o.maxPlacementDistance = i,
         o -> o.maxPlacementDistance,
         (i, o) -> i.maxPlacementDistance = o.maxPlacementDistance
      )
      .documentation("The max distance at which the player can deploy the deployable.")
      .add()
      .afterDecode(SpawnDeployableFromRaycastInteraction::processConfig)
      .build();
   protected Object2FloatMap<String> unknownEntityStats;
   @Nullable
   protected Int2FloatMap entityStats;
   protected float maxPlacementDistance;
   private DeployableConfig config;

   private void processConfig() {
      if (this.unknownEntityStats != null) {
         this.entityStats = EntityStatsModule.resolveEntityStats(this.unknownEntityStats);
      }
   }

   private static boolean isSurface(@Nonnull Vector3f normal) {
      return normal.x == 0.0F && normal.y - 1.0F < 0.01 && normal.z == 0.0F;
   }

   @Override
   public boolean needsRemoteSync() {
      return true;
   }

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Client;
   }

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> entityRef = context.getOwningEntity();
      Store<EntityStore> store = entityRef.getStore();
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      InteractionSyncData clientState = context.getClientState();

      assert clientState != null;

      if (!this.canAfford(context.getEntity(), commandBuffer)) {
         context.getState().state = InteractionState.Failed;
      } else {
         Position raycastHit = clientState.raycastHit;
         if (raycastHit == null) {
            TransformComponent transformComponent = store.getComponent(entityRef, TransformComponent.getComponentType());

            assert transformComponent != null;

            Vector3d position = transformComponent.getPosition();
            raycastHit = new Position((float)position.x, (float)position.y, (float)position.z);
         }

         com.hypixel.hytale.protocol.Vector3f raycastNormal = clientState.raycastNormal;
         float correctedRaycastDistance = clientState.raycastDistance;
         com.hypixel.hytale.protocol.Vector3f spawnPosition = new com.hypixel.hytale.protocol.Vector3f(
            (float)raycastHit.x, (float)raycastHit.y, (float)raycastHit.z
         );
         Vector3f norm = new Vector3f(raycastNormal.x, raycastNormal.y, raycastNormal.z);
         if (correctedRaycastDistance > 0.0F
            && correctedRaycastDistance <= this.maxPlacementDistance
            && (this.config.getAllowPlaceOnWalls() || isSurface(norm))) {
            Direction attackerRot = clientState.attackerRot;
            Vector3f rot = new Vector3f(0.0F, attackerRot.yaw, 0.0F);
            DeployablesUtils.spawnDeployable(
               commandBuffer, store, this.config, entityRef, new Vector3f(spawnPosition.x, spawnPosition.y, spawnPosition.z), rot, "UP"
            );
         }
      }
   }

   protected boolean canAfford(@Nonnull Ref<EntityStore> entityRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.entityStats != null && !this.entityStats.isEmpty()) {
         EntityStatMap entityStatMapComponent = componentAccessor.getComponent(entityRef, EntityStatMap.getComponentType());
         if (entityStatMapComponent == null) {
            return false;
         } else {
            ObjectIterator var4 = this.entityStats.int2FloatEntrySet().iterator();

            while (var4.hasNext()) {
               Entry cost = (Entry)var4.next();
               EntityStatValue stat = entityStatMapComponent.get(cost.getIntKey());
               if (stat == null || stat.get() < cost.getFloatValue()) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return true;
      }
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.SpawnDeployableFromRaycastInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.SpawnDeployableFromRaycastInteraction p = (com.hypixel.hytale.protocol.SpawnDeployableFromRaycastInteraction)packet;
      p.deployableConfig = this.config.toPacket();
      p.maxDistance = this.maxPlacementDistance;
      p.costs = this.entityStats;
   }
}
