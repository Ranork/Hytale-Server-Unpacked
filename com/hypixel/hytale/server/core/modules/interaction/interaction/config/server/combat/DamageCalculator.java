package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.map.Object2FloatMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMaps;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import java.util.Iterator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageCalculator {
   public static final BuilderCodec<DamageCalculator> CODEC = BuilderCodec.builder(DamageCalculator.class, DamageCalculator::new)
      .appendInherited(
         new KeyedCodec<>("Type", DamageCalculator.Type.CODEC),
         (damageCalculator, type) -> damageCalculator.type = type,
         damageCalculator -> damageCalculator.type,
         (damageCalculator, parent) -> damageCalculator.type = parent.type
      )
      .add()
      .<DamageClass>appendInherited(
         new KeyedCodec<>("Class", DamageClass.CODEC), (o, v) -> o.damageClass = v, o -> o.damageClass, (o, p) -> o.damageClass = p.damageClass
      )
      .documentation("The class of the damage being created, used by the damage system to apply modifiers based on equipment of the source.")
      .addValidator(Validators.nonNull())
      .add()
      .<Object2FloatMap<String>>appendInherited(
         new KeyedCodec<>("BaseDamage", new Object2FloatMapCodec<>(Codec.STRING, Object2FloatOpenHashMap::new)),
         (damageCalculator, map) -> damageCalculator.baseDamageRaw = map,
         damageCalculator -> damageCalculator.baseDamageRaw,
         (damageCalculator, parent) -> damageCalculator.baseDamageRaw = parent.baseDamageRaw
      )
      .addValidator(DamageCause.VALIDATOR_CACHE.getMapKeyValidator())
      .add()
      .appendInherited(
         new KeyedCodec<>("SequentialModifierStep", Codec.FLOAT),
         (damageCalculator, sequentialModifierStep) -> damageCalculator.sequentialModifierStep = sequentialModifierStep,
         damageCalculator -> damageCalculator.sequentialModifierStep,
         (damageCalculator, parent) -> damageCalculator.sequentialModifierStep = parent.sequentialModifierStep
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("SequentialModifierMinimum", Codec.FLOAT),
         (damageCalculator, sequentialModifierMinimum) -> damageCalculator.sequentialModifierMinimum = sequentialModifierMinimum,
         damageCalculator -> damageCalculator.sequentialModifierMinimum,
         (damageCalculator, parent) -> damageCalculator.sequentialModifierMinimum = parent.sequentialModifierMinimum
      )
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("RandomPercentageModifier", Codec.FLOAT),
         (damageCalculator, randomPercentageModifier) -> damageCalculator.randomPercentageModifier = randomPercentageModifier,
         damageCalculator -> damageCalculator.randomPercentageModifier,
         (damageCalculator, parent) -> damageCalculator.randomPercentageModifier = parent.randomPercentageModifier
      )
      .addValidator(Validators.greaterThanOrEqual(0.0F))
      .add()
      .afterDecode(asset -> {
         if (asset.baseDamageRaw != null) {
            asset.baseDamage = new Int2FloatOpenHashMap();
            Iterator i$ = asset.baseDamageRaw.object2FloatEntrySet().iterator();

            while (i$.hasNext()) {
               Entry<String> entry = (Entry<String>)i$.next();
               int index = DamageCause.getAssetMap().getIndex((String)entry.getKey());
               asset.baseDamage.put(index, entry.getFloatValue());
            }
         }
      })
      .build();
   protected DamageCalculator.Type type = DamageCalculator.Type.ABSOLUTE;
   @Nonnull
   protected DamageClass damageClass = DamageClass.UNKNOWN;
   protected Object2FloatMap<String> baseDamageRaw;
   protected float sequentialModifierStep;
   protected float sequentialModifierMinimum;
   protected float randomPercentageModifier;
   @Nonnull
   protected transient Int2FloatMap baseDamage = Int2FloatMaps.EMPTY_MAP;

   protected DamageCalculator() {
   }

   @Nullable
   public Object2FloatMap<DamageCause> calculateDamage(double durationSeconds) {
      if (this.baseDamageRaw != null && !this.baseDamageRaw.isEmpty()) {
         Object2FloatMap<DamageCause> outDamage = new Object2FloatOpenHashMap(this.baseDamage.size());
         float randomPercentageModifier = MathUtil.randomFloat(-this.randomPercentageModifier, this.randomPercentageModifier);
         ObjectIterator var5 = this.baseDamage.int2FloatEntrySet().iterator();

         while (var5.hasNext()) {
            it.unimi.dsi.fastutil.ints.Int2FloatMap.Entry entry = (it.unimi.dsi.fastutil.ints.Int2FloatMap.Entry)var5.next();
            DamageCause damageCause = DamageCause.getAssetMap().getAsset(entry.getIntKey());
            float value = entry.getFloatValue();
            float damage = this.scaleDamage(durationSeconds, value);
            damage += damage * randomPercentageModifier;
            outDamage.put(damageCause, damage);
         }

         return outDamage;
      } else {
         return null;
      }
   }

   private float scaleDamage(double durationSeconds, float damage) {
      return switch (this.type) {
         case DPS -> (float)durationSeconds * damage;
         case ABSOLUTE -> damage;
      };
   }

   public DamageCalculator.Type getType() {
      return this.type;
   }

   @Nonnull
   public DamageClass getDamageClass() {
      return this.damageClass;
   }

   public float getSequentialModifierStep() {
      return this.sequentialModifierStep;
   }

   public float getSequentialModifierMinimum() {
      return this.sequentialModifierMinimum;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o instanceof DamageCalculator that) {
         if (Double.compare(that.sequentialModifierStep, this.sequentialModifierStep) != 0) {
            return false;
         } else if (Double.compare(that.sequentialModifierMinimum, this.sequentialModifierMinimum) != 0) {
            return false;
         } else if (Double.compare(that.randomPercentageModifier, this.randomPercentageModifier) != 0) {
            return false;
         } else if (this.type != that.type) {
            return false;
         } else {
            return !Objects.equals(this.baseDamageRaw, that.baseDamageRaw) ? false : false;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.type != null ? this.type.hashCode() : 0;
      result = 31 * result + (this.baseDamageRaw != null ? this.baseDamageRaw.hashCode() : 0);
      long temp = Double.doubleToLongBits(this.sequentialModifierStep);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.sequentialModifierMinimum);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.randomPercentageModifier);
      return 31 * result + (int)(temp ^ temp >>> 32);
   }

   @Nonnull
   @Override
   public String toString() {
      return "DamageCalculator{type="
         + this.type
         + ", baseDamage="
         + this.baseDamageRaw
         + ", sequentialModifierStep="
         + this.sequentialModifierStep
         + ", sequentialModifierMinimum="
         + this.sequentialModifierMinimum
         + ", randomPercentageModifier="
         + this.randomPercentageModifier
         + "}";
   }

   public static enum Type {
      DPS,
      ABSOLUTE;

      public static final EnumCodec<DamageCalculator.Type> CODEC = new EnumCodec<>(DamageCalculator.Type.class);
   }
}
