package com.hypixel.hytale.builtin.deployables;

import com.hypixel.hytale.builtin.deployables.component.DeployableComponent;
import com.hypixel.hytale.builtin.deployables.component.DeployableOwnerComponent;
import com.hypixel.hytale.builtin.deployables.config.DeployableConfig;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.entities.PlayAnimation;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.AudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.modules.entityui.UIComponentList;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.PlayerUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeployablesUtils {
   @Nonnull
   private static final String DEPLOYABLE_MAX_STAT_MODIFIER = "DEPLOYABLE_MAX";

   @Nonnull
   public static Ref<EntityStore> spawnDeployable(
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Store<EntityStore> store,
      @Nonnull DeployableConfig config,
      @Nonnull Ref<EntityStore> deployerRef,
      @Nonnull Vector3f position,
      @Nonnull Vector3f rotation,
      @Nonnull String spawnFace
   ) {
      Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
      Vector3d spawnPos = new Vector3d(position.x, position.y, position.z);
      Model model = config.getModel();
      AudioComponent audioComponent = new AudioComponent();
      if (config.getAmbientSoundEventIndex() != 0) {
         audioComponent.addSound(config.getAmbientSoundEventIndex());
      }

      holder.addComponent(DeployableComponent.getComponentType(), new DeployableComponent());
      holder.addComponent(TransformComponent.getComponentType(), new TransformComponent());
      holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(Vector3f.FORWARD));
      holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(UUID.randomUUID()));
      holder.addComponent(EntityStatMap.getComponentType(), new EntityStatMap());
      holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
      holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
      holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
      holder.addComponent(AudioComponent.getComponentType(), audioComponent);
      holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
      holder.ensureComponent(DeployableComponent.getComponentType());
      holder.ensureComponent(EntityModule.get().getVisibleComponentType());
      holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());
      UIComponentList uiCompList = holder.ensureAndGetComponent(UIComponentList.getComponentType());
      uiCompList.update();
      if (config.getInvulnerable()) {
         holder.ensureComponent(Invulnerable.getComponentType());
      }

      int hitboxCollisionConfigIndex = config.getHitboxCollisionConfigIndex();
      if (hitboxCollisionConfigIndex != -1) {
         HitboxCollisionConfig hitboxCollisionAsset = HitboxCollisionConfig.getAssetMap().getAsset(hitboxCollisionConfigIndex);
         holder.addComponent(HitboxCollision.getComponentType(), new HitboxCollision(hitboxCollisionAsset));
      }

      long liveDuration = config.getLiveDurationInMillis();
      if (liveDuration > 0L) {
         holder.addComponent(
            DespawnComponent.getComponentType(),
            new DespawnComponent(store.getResource(TimeResource.getResourceType()).getNow().plus(Duration.ofMillis(liveDuration)))
         );
      }

      EntityStatMap entityStatMapComponent = holder.ensureAndGetComponent(EntityStatMap.getComponentType());
      entityStatMapComponent.update();
      populateStats(config, entityStatMapComponent);
      DeployableComponent deployableComponent = holder.ensureAndGetComponent(DeployableComponent.getComponentType());
      deployableComponent.init(deployerRef, store, config, store.getResource(TimeResource.getResourceType()).getNow(), spawnFace);
      TransformComponent transformComponent = holder.ensureAndGetComponent(TransformComponent.getComponentType());
      transformComponent.setRotation(rotation);
      transformComponent.setPosition(new Vector3d(spawnPos));
      HeadRotation headRotationComponent = holder.ensureAndGetComponent(HeadRotation.getComponentType());
      headRotationComponent.setRotation(rotation);
      commandBuffer.ensureComponent(deployerRef, DeployableOwnerComponent.getComponentType());
      return commandBuffer.addEntity(holder, AddReason.SPAWN);
   }

   static void populateStats(@Nonnull DeployableConfig config, @Nonnull EntityStatMap entityStatMapComponent) {
      Map<String, DeployableConfig.StatConfig> stats = config.getStatValues();
      if (stats != null) {
         for (Entry<String, DeployableConfig.StatConfig> statEntry : stats.entrySet()) {
            DeployableConfig.StatConfig statConfig = statEntry.getValue();
            int statIndex = EntityStatType.getAssetMap().getIndex(statEntry.getKey());
            EntityStatValue stat = entityStatMapComponent.get(statIndex);
            if (stat != null) {
               EntityStatType statType = EntityStatType.getAssetMap().getAsset(statIndex);
               if (statType != null) {
                  StaticModifier modifier = new StaticModifier(
                     Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, statConfig.getMax() - statType.getMax()
                  );
                  entityStatMapComponent.putModifier(statIndex, "DEPLOYABLE_MAX", modifier);
                  float initialValue = statConfig.getInitial();
                  if (initialValue == Float.MAX_VALUE) {
                     entityStatMapComponent.maximizeStatValue(statIndex);
                  } else {
                     entityStatMapComponent.setStatValue(statIndex, initialValue);
                  }
               }
            }
         }
      }
   }

   public static void playAnimation(
      @Nonnull Store<EntityStore> store,
      int networkId,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull DeployableConfig config,
      @Nonnull AnimationSlot animationSlot,
      @Nullable String itemAnimationsId,
      @Nonnull String animationId
   ) {
      Model model = config.getModel();
      if (animationSlot == AnimationSlot.Action || model == null || model.getAnimationSetMap().containsKey(animationId)) {
         PlayAnimation animationPacket = new PlayAnimation(networkId, itemAnimationsId, animationId, animationSlot);
         PlayerUtil.forEachPlayerThatCanSeeEntity(
            ref, (playerRef, playerRefComponent, ca) -> playerRefComponent.getPacketHandler().write(animationPacket), store
         );
      }
   }

   public static void stopAnimation(@Nonnull Store<EntityStore> store, int networkId, @Nonnull Ref<EntityStore> ref, @Nonnull AnimationSlot animationSlot) {
      PlayAnimation animationPacket = new PlayAnimation(networkId, null, null, animationSlot);
      PlayerUtil.forEachPlayerThatCanSeeEntity(ref, (playerRef, playerRefComponent, ca) -> playerRefComponent.getPacketHandler().write(animationPacket), store);
   }

   public static void playSoundEventsAtEntity(
      @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, int localIndex, int worldIndex, @Nonnull Vector3d pos
   ) {
      Player targetPlayerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
      if (localIndex != 0 && targetPlayerComponent != null) {
         SoundUtil.playSoundEvent2d(ref, localIndex, SoundCategory.SFX, componentAccessor);
      }

      if (worldIndex != 0) {
         SoundUtil.playSoundEvent3d(worldIndex, SoundCategory.SFX, pos, componentAccessor);
      }
   }
}
