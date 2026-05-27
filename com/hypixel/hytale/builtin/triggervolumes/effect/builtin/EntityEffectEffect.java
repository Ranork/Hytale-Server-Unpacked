package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityEffectEffect extends TriggerEffect {
   @Nonnull
   public static final BuilderCodec<EntityEffectEffect> CODEC = BuilderCodec.builder(EntityEffectEffect.class, EntityEffectEffect::new, BASE_CODEC)
      .append(new KeyedCodec<>("Effect", Codec.STRING), (e, v) -> e.effectId = v, e -> e.effectId)
      .add()
      .append(new KeyedCodec<>("Mode", new EnumCodec<>(EntityEffectEffect.Mode.class)), (e, v) -> e.mode = v, e -> e.mode)
      .add()
      .append(new KeyedCodec<>("Duration", Codec.FLOAT, false), (e, v) -> e.duration = v, e -> e.duration)
      .add()
      .build();
   @Nullable
   private String effectId;
   @Nonnull
   private EntityEffectEffect.Mode mode = EntityEffectEffect.Mode.APPLY;
   private float duration = -1.0F;

   @Override
   public void execute(@Nonnull TriggerContext context) {
      if (this.effectId != null) {
         Ref<EntityStore> entityRef = context.getEntityRef();
         Store<EntityStore> store = context.getStore();
         EffectControllerComponent effectController = store.getComponent(entityRef, EffectControllerComponent.getComponentType());
         if (effectController != null) {
            if (this.mode == EntityEffectEffect.Mode.APPLY) {
               EntityEffect entityEffect = EntityEffect.getAssetMap().getAsset(this.effectId);
               if (entityEffect == null) {
                  return;
               }

               if (this.duration > 0.0F) {
                  effectController.addEffect(entityRef, entityEffect, this.duration, OverlapBehavior.OVERWRITE, store);
               } else {
                  effectController.addEffect(entityRef, entityEffect, store);
               }
            } else {
               int effectIndex = EntityEffect.getAssetMap().getIndex(this.effectId);
               if (effectIndex == Integer.MIN_VALUE) {
                  return;
               }

               effectController.removeEffect(entityRef, effectIndex, store);
            }
         }
      }
   }

   public static enum Mode {
      APPLY,
      REMOVE;
   }
}
