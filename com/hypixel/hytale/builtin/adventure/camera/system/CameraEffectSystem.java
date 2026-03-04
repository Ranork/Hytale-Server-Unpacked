package com.hypixel.hytale.builtin.adventure.camera.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.asset.type.camera.CameraEffect;
import com.hypixel.hytale.server.core.asset.type.gameplay.CameraEffectsConfig;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CameraEffectSystem extends DamageEventSystem {
   @Nonnull
   private final ComponentType<EntityStore, PlayerRef> playerRefComponentType;
   @Nonnull
   private final ComponentType<EntityStore, EntityStatMap> entityStatMapComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public CameraEffectSystem(
      @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType, @Nonnull ComponentType<EntityStore, EntityStatMap> entityStatMapComponentType
   ) {
      this.playerRefComponentType = playerRefComponentType;
      this.entityStatMapComponentType = entityStatMapComponentType;
      this.query = Query.and(playerRefComponentType, entityStatMapComponentType);
   }

   @Nullable
   @Override
   public SystemGroup<EntityStore> getGroup() {
      return DamageModule.get().getInspectDamageGroup();
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   public void handle(
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Damage damage
   ) {
      EntityStatMap entityStatMapComponent = archetypeChunk.getComponent(index, this.entityStatMapComponentType);

      assert entityStatMapComponent != null;

      EntityStatValue healthStat = entityStatMapComponent.get(DefaultEntityStatTypes.getHealth());
      if (healthStat != null) {
         float health = healthStat.getMax() - healthStat.getMin();
         if (!(health <= 0.0F)) {
            PlayerRef playerRefComponent = archetypeChunk.getComponent(index, this.playerRefComponentType);

            assert playerRefComponent != null;

            World world = commandBuffer.getExternalData().getWorld();
            CameraEffectsConfig cameraEffectsConfig = world.getGameplayConfig().getCameraEffectsConfig();
            Damage.CameraEffect effect = damage.getIfPresentMetaObject(Damage.CAMERA_EFFECT);
            int effectIndex = effect != null ? effect.getEffectIndex() : cameraEffectsConfig.getCameraEffectIndex(damage.getDamageCauseIndex());
            if (effectIndex != Integer.MIN_VALUE) {
               CameraEffect cameraEffect = CameraEffect.getAssetMap().getAsset(effectIndex);
               if (cameraEffect != null) {
                  float intensity = MathUtil.clamp(damage.getAmount() / health, 0.0F, 1.0F);
                  playerRefComponent.getPacketHandler().writeNoCache(cameraEffect.createCameraShakePacket(intensity));
               }
            }
         }
      }
   }
}
