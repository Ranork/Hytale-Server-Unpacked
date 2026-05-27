package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageEntityEffect extends TriggerEffect {
   @Nonnull
   public static final BuilderCodec<DamageEntityEffect> CODEC = BuilderCodec.builder(DamageEntityEffect.class, DamageEntityEffect::new, BASE_CODEC)
      .append(new KeyedCodec<>("Mode", new EnumCodec<>(DamageEntityEffect.DamageMode.class)), (e, v) -> e.mode = v, e -> e.mode)
      .add()
      .append(new KeyedCodec<>("Amount", Codec.FLOAT, false), (e, v) -> e.amount = v, e -> e.amount)
      .add()
      .build();
   @Nonnull
   private DamageEntityEffect.DamageMode mode = DamageEntityEffect.DamageMode.FLAT;
   private float amount;

   @Override
   public void execute(@Nonnull TriggerContext context) {
      if (!(this.amount <= 0.0F)) {
         DamageCause cause = resolveCause();
         if (cause != null) {
            Ref<EntityStore> entityRef = context.getEntityRef();
            Store<EntityStore> store = context.getStore();

            float damageAmount = switch (this.mode) {
               case FLAT -> this.amount;
               case PERCENT_MAX, PERCENT_CURRENT -> {
                  EntityStatMap statMap = store.getComponent(entityRef, EntityStatMap.getComponentType());
                  if (statMap == null) {
                     yield 0.0F;
                  } else {
                     EntityStatValue health = statMap.get(DefaultEntityStatTypes.getHealth());
                     if (health == null) {
                        yield 0.0F;
                     } else {
                        float reference = this.mode == DamageEntityEffect.DamageMode.PERCENT_MAX ? health.getMax() : health.get();
                        yield reference * (this.amount / 100.0F);
                     }
                  }
               }
            };
            if (!(damageAmount <= 0.0F)) {
               DamageSystems.executeDamage(entityRef, store, new Damage(Damage.NULL_SOURCE, cause, damageAmount));
            }
         }
      }
   }

   @Nullable
   private static DamageCause resolveCause() {
      IndexedLookupTableAssetMap<String, DamageCause> map = DamageCause.getAssetMap();
      DamageCause env = map.getAsset("Environment");
      return env != null ? env : DamageCause.OUT_OF_WORLD;
   }

   public static enum DamageMode {
      FLAT,
      PERCENT_MAX,
      PERCENT_CURRENT;
   }
}
