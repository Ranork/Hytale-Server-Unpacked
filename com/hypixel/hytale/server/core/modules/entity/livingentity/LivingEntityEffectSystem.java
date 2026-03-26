package com.hypixel.hytale.server.core.modules.entity.livingentity;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.DisableProcessingAssert;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.RemovalBehavior;
import com.hypixel.hytale.server.core.entity.effect.ActiveEntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.condition.Condition;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.time.Instant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LivingEntityEffectSystem extends EntityTickingSystem<EntityStore> implements DisableProcessingAssert {
   @Nonnull
   private static final Query<EntityStore> QUERY = Query.and(EffectControllerComponent.getComponentType(), TransformComponent.getComponentType());

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return QUERY;
   }

   @Override
   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return false;
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      EffectControllerComponent effectControllerComponent = archetypeChunk.getComponent(index, EffectControllerComponent.getComponentType());

      assert effectControllerComponent != null;

      Int2ObjectMap<ActiveEntityEffect> activeEffects = effectControllerComponent.getActiveEffects();
      if (!activeEffects.isEmpty()) {
         IndexedLookupTableAssetMap<String, EntityEffect> entityEffectAssetMap = EntityEffect.getAssetMap();
         Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
         EntityStatMap entityStatMapComponent = commandBuffer.getComponent(entityRef, EntityStatMap.getComponentType());
         if (entityStatMapComponent != null) {
            IntList effectsToRemove = null;
            boolean invulnerable = false;
            ObjectIterator var13 = activeEffects.values().iterator();

            while (var13.hasNext()) {
               ActiveEntityEffect activeEntityEffect = (ActiveEntityEffect)var13.next();
               int entityEffectIndex = activeEntityEffect.getEntityEffectIndex();
               EntityEffect entityEffect = entityEffectAssetMap.getAsset(entityEffectIndex);
               if (entityEffect == null) {
                  if (effectsToRemove == null) {
                     effectsToRemove = new IntArrayList();
                  }

                  effectsToRemove.add(entityEffectIndex);
               } else if (!canApplyEffect(entityRef, entityEffect, commandBuffer)) {
                  if (effectsToRemove == null) {
                     effectsToRemove = new IntArrayList();
                  }

                  effectsToRemove.add(entityEffectIndex);
               } else {
                  float tickDelta = Math.min(activeEntityEffect.getRemainingDuration(), dt);
                  activeEntityEffect.tick(commandBuffer, entityRef, entityEffect, entityStatMapComponent, tickDelta);
                  if (activeEffects.isEmpty()) {
                     return;
                  }

                  if (!activeEntityEffect.isInfinite() && activeEntityEffect.getRemainingDuration() <= 0.0F) {
                     if (effectsToRemove == null) {
                        effectsToRemove = new IntArrayList();
                     }

                     effectsToRemove.add(entityEffectIndex);
                  }

                  if (activeEntityEffect.isInvulnerable()) {
                     invulnerable = true;
                  }
               }
            }

            effectControllerComponent.setInvulnerable(invulnerable);
            if (effectsToRemove != null) {
               IntListIterator var18 = effectsToRemove.iterator();

               while (var18.hasNext()) {
                  int effectIndex = (Integer)var18.next();
                  effectControllerComponent.removeEffect(entityRef, effectIndex, RemovalBehavior.COMPLETE, commandBuffer);
               }
            }
         }
      }
   }

   @Nullable
   @Override
   public SystemGroup<EntityStore> getGroup() {
      return DamageModule.get().getGatherDamageGroup();
   }

   public static boolean canApplyEffect(
      @Nonnull Ref<EntityStore> ownerRef, @Nonnull EntityEffect entityEffect, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Condition[] applyConditions = entityEffect.getApplyConditions();
      return applyConditions != null && applyConditions.length != 0
         ? Condition.allConditionsMet(componentAccessor, ownerRef, Instant.now(), applyConditions)
         : true;
   }
}
