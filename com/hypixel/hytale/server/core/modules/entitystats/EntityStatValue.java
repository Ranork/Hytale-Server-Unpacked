package com.hypixel.hytale.server.core.modules.entitystats;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityStatValue {
   public static final EntityStatValue[] EMPTY_ARRAY = new EntityStatValue[0];
   public static final BuilderCodec<EntityStatValue> CODEC = BuilderCodec.builder(EntityStatValue.class, EntityStatValue::new)
      .addField(new KeyedCodec<>("Id", Codec.STRING), (regenerating, value) -> regenerating.id = value, regenerating -> regenerating.id)
      .addField(new KeyedCodec<>("Value", Codec.FLOAT), (regenerating, value) -> regenerating.value = value, regenerating -> regenerating.value)
      .addField(
         new KeyedCodec<>("Modifiers", new MapCodec<>(Modifier.CODEC, HashMap::new, false)),
         (regenerating, value) -> regenerating.modifiers = value,
         regenerating -> regenerating.modifiers != null && !regenerating.modifiers.isEmpty() ? regenerating.modifiers : null
      )
      .build();
   private String id;
   private int index = Integer.MIN_VALUE;
   private float value;
   private float min;
   private float max;
   private boolean ignoreInvulnerability;
   @Nullable
   private RegeneratingValue[] regeneratingValues;
   @Nullable
   private Map<String, Modifier> modifiers;

   protected EntityStatValue() {
   }

   public EntityStatValue(int index, @Nonnull EntityStatType asset) {
      this.id = asset.getId();
      this.index = index;
      this.value = asset.getInitialValue();
      this.synchronizeAsset(index, asset);
   }

   public String getId() {
      return this.id;
   }

   public int getIndex() {
      return this.index;
   }

   public float get() {
      return this.value;
   }

   public float asPercentage() {
      return this.min == this.max ? 0.0F : (this.value - this.min) / (this.max - this.min);
   }

   public float getMin() {
      return this.min;
   }

   public float getMax() {
      return this.max;
   }

   protected float set(float newValue) {
      return this.value = MathUtil.clamp(newValue, this.min, this.max);
   }

   @Nullable
   public RegeneratingValue[] getRegeneratingValues() {
      return this.regeneratingValues;
   }

   @Nullable
   public Modifier getModifier(String key) {
      return this.modifiers == null ? null : this.modifiers.get(key);
   }

   public boolean getIgnoreInvulnerability() {
      return this.ignoreInvulnerability;
   }

   @Nullable
   public Map<String, Modifier> getModifiers() {
      return this.modifiers;
   }

   @Nullable
   protected Modifier putModifier(String key, Modifier modifier) {
      if (this.modifiers == null) {
         this.modifiers = new Object2ObjectOpenHashMap();
      }

      Modifier oldModifier = this.modifiers.put(key, modifier);
      this.computeModifiers(EntityStatType.getAssetMap().getAsset(this.index));
      return oldModifier;
   }

   @Nullable
   protected Modifier removeModifier(String key) {
      if (this.modifiers == null) {
         return null;
      } else {
         Modifier modifier = this.modifiers.remove(key);
         if (modifier != null) {
            this.computeModifiers(EntityStatType.getAssetMap().getAsset(this.index));
         }

         return modifier;
      }
   }

   public boolean synchronizeAsset(int index, @Nonnull EntityStatType asset) {
      this.id = asset.getId();
      this.index = index;
      this.initializeRegenerating(asset);
      boolean minMaxChanged = this.min != asset.getMin() || this.max != asset.getMax();
      this.ignoreInvulnerability = asset.getIgnoreInvulnerability();
      float oldValue = this.value;
      this.computeModifiers(asset);
      return minMaxChanged || this.value != oldValue;
   }

   private void initializeRegenerating(@Nonnull EntityStatType entityStatType) {
      EntityStatType.Regenerating[] regeneratingTypes = entityStatType.getRegenerating();
      if (regeneratingTypes != null) {
         this.regeneratingValues = new RegeneratingValue[regeneratingTypes.length];

         for (int i = 0; i < regeneratingTypes.length; i++) {
            this.regeneratingValues[i] = new RegeneratingValue(regeneratingTypes[i]);
         }
      }
   }

   protected void computeModifiers(@Nonnull EntityStatType asset) {
      this.min = asset.getMin();
      this.max = asset.getMax();
      if (this.modifiers != null) {
         for (Modifier.ModifierTarget target : Modifier.ModifierTarget.VALUES) {
            boolean hasAdditive = false;
            float additive = 0.0F;
            boolean hasMultiplicative = false;
            float multiplicative = 0.0F;

            for (Modifier modifier : this.modifiers.values()) {
               if (modifier instanceof StaticModifier staticModifier && staticModifier.getTarget() == target) {
                  switch (staticModifier.getCalculationType()) {
                     case ADDITIVE:
                        hasAdditive = true;
                        additive += staticModifier.getAmount();
                        break;
                     case MULTIPLICATIVE:
                        hasMultiplicative = true;
                        multiplicative += staticModifier.getAmount();
                  }
               }
            }

            switch (target) {
               case MIN:
                  if (hasAdditive) {
                     this.min = StaticModifier.CalculationType.ADDITIVE.compute(this.min, additive);
                  }

                  if (hasMultiplicative) {
                     this.min = StaticModifier.CalculationType.MULTIPLICATIVE.compute(this.min, multiplicative);
                  }
                  break;
               case MAX:
                  if (hasAdditive) {
                     this.max = StaticModifier.CalculationType.ADDITIVE.compute(this.max, additive);
                  }

                  if (hasMultiplicative) {
                     this.max = StaticModifier.CalculationType.MULTIPLICATIVE.compute(this.max, multiplicative);
                  }
            }
         }

         for (Modifier modifierx : this.modifiers.values()) {
            if (!(modifierx instanceof StaticModifier)) {
               this.applyModifier(modifierx);
            }
         }
      }

      this.value = MathUtil.clamp(this.value, this.min, this.max);
   }

   private void applyModifier(@Nonnull Modifier modifier) {
      switch (modifier.getTarget()) {
         case MIN:
            this.min = modifier.apply(this.min);
            break;
         case MAX:
            this.max = modifier.apply(this.max);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "EntityStatValue{id='"
         + this.id
         + "', index="
         + this.index
         + ", value="
         + this.value
         + ", min="
         + this.min
         + ", max="
         + this.max
         + ", regeneratingValues="
         + Arrays.toString((Object[])this.regeneratingValues)
         + ", modifiers="
         + this.modifiers
         + "}";
   }
}
