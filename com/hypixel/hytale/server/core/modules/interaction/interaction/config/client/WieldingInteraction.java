package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.map.Object2DoubleMapCodec;
import com.hypixel.hytale.codec.codecs.map.Object2FloatMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.gameplay.CombatConfig;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.damage.DamageDataComponent;
import com.hypixel.hytale.server.core.entity.effect.ActiveEntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.DamageEffects;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMaps;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMaps;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap.Entry;
import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WieldingInteraction extends ChargingInteraction {
   public static final float WIELDING_INDEX = 0.0F;
   @Nonnull
   public static final BuilderCodec<WieldingInteraction> CODEC = BuilderCodec.builder(
         WieldingInteraction.class, WieldingInteraction::new, ChargingInteraction.ABSTRACT_CODEC
      )
      .documentation("Interaction that blocks while the key is held and applies various modifiers while active.")
      .<Object2DoubleMap<String>>appendInherited(
         new KeyedCodec<>("KnockbackModifiers", new Object2DoubleMapCodec<>(Codec.STRING, Object2DoubleOpenHashMap::new)),
         (damageCalculator, map) -> damageCalculator.knockbackModifiersRaw = map,
         damageCalculator -> damageCalculator.knockbackModifiersRaw,
         (damageCalculator, parent) -> damageCalculator.knockbackModifiersRaw = parent.knockbackModifiersRaw
      )
      .addValidator(DamageCause.VALIDATOR_CACHE.getMapKeyValidator())
      .add()
      .<Object2FloatMap<String>>appendInherited(
         new KeyedCodec<>("DamageModifiers", new Object2FloatMapCodec<>(Codec.STRING, Object2FloatOpenHashMap::new)),
         (damageCalculator, map) -> damageCalculator.damageModifiersRaw = map,
         damageCalculator -> damageCalculator.damageModifiersRaw,
         (damageCalculator, parent) -> damageCalculator.damageModifiersRaw = parent.damageModifiersRaw
      )
      .addValidator(DamageCause.VALIDATOR_CACHE.getMapKeyValidator())
      .add()
      .appendInherited(
         new KeyedCodec<>("AngledWielding", WieldingInteraction.AngledWielding.CODEC),
         (i, o) -> i.angledWielding = o,
         i -> i.angledWielding,
         (i, parent) -> i.angledWielding = parent.angledWielding
      )
      .add()
      .<String>appendInherited(new KeyedCodec<>("Next", Interaction.CHILD_ASSET_CODEC), (interaction, s) -> {
         interaction.next = new Float2ObjectOpenHashMap();
         interaction.next.put(0.0F, s);
      }, interaction -> interaction.next != null ? (String)interaction.next.get(0.0F) : null, (interaction, parent) -> interaction.next = parent.next)
      .addValidatorLate(() -> VALIDATOR_CACHE.getValidator().late())
      .add()
      .<WieldingInteraction.StaminaCost>appendInherited(
         new KeyedCodec<>("StaminaCost", WieldingInteraction.StaminaCost.CODEC),
         (wieldingInteraction, staminaCost) -> wieldingInteraction.staminaCost = staminaCost,
         wieldingInteraction -> wieldingInteraction.staminaCost,
         (wieldingInteraction, parent) -> wieldingInteraction.staminaCost = parent.staminaCost
      )
      .documentation("Configuration to define how stamina loss is computed.")
      .add()
      .appendInherited(
         new KeyedCodec<>("BlockedEffects", DamageEffects.CODEC),
         (wieldingInteraction, interactionEffects) -> wieldingInteraction.blockedEffects = interactionEffects,
         wieldingInteraction -> wieldingInteraction.blockedEffects,
         (wieldingInteraction, parent) -> wieldingInteraction.blockedEffects = parent.blockedEffects
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("BlockedInteractions", RootInteraction.CHILD_ASSET_CODEC),
         (wieldingInteraction, s) -> wieldingInteraction.blockedInteractions = s,
         wieldingInteraction -> wieldingInteraction.blockedInteractions,
         (wieldingInteraction, parent) -> wieldingInteraction.blockedInteractions = parent.blockedInteractions
      )
      .addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getValidator().late())
      .add()
      .afterDecode(i -> {
         i.allowIndefiniteHold = true;
         if (i.next != null && i.runTime > 0.0F) {
            i.next.put(i.runTime, (String)i.next.get(0.0F));
         }

         if (i.knockbackModifiersRaw != null) {
            i.knockbackModifiers = new Int2DoubleOpenHashMap();
            Iterator i$ = i.knockbackModifiersRaw.object2DoubleEntrySet().iterator();

            while (i$.hasNext()) {
               Entry<String> entry = (Entry<String>)i$.next();
               int index = DamageCause.getAssetMap().getIndex((String)entry.getKey());
               i.knockbackModifiers.put(index, entry.getDoubleValue());
            }
         }

         if (i.damageModifiersRaw != null) {
            i.damageModifiers = new Int2FloatOpenHashMap();
            Iterator i$ = i.damageModifiersRaw.object2FloatEntrySet().iterator();

            while (i$.hasNext()) {
               it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry<String> entry = (it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry<String>)i$.next();
               int index = DamageCause.getAssetMap().getIndex((String)entry.getKey());
               i.damageModifiers.put(index, entry.getFloatValue());
            }
         }
      })
      .build();
   @Nullable
   protected Object2DoubleMap<String> knockbackModifiersRaw;
   @Nullable
   protected Object2FloatMap<String> damageModifiersRaw;
   protected WieldingInteraction.AngledWielding angledWielding;
   protected WieldingInteraction.StaminaCost staminaCost;
   protected DamageEffects blockedEffects;
   protected String blockedInteractions;
   @Nonnull
   protected transient Int2DoubleMap knockbackModifiers = Int2DoubleMaps.EMPTY_MAP;
   @Nonnull
   protected transient Int2FloatMap damageModifiers = Int2FloatMaps.EMPTY_MAP;

   @Nonnull
   public Int2DoubleMap getKnockbackModifiers() {
      return this.knockbackModifiers;
   }

   @Nonnull
   public Int2FloatMap getDamageModifiers() {
      return this.damageModifiers;
   }

   public WieldingInteraction.AngledWielding getAngledWielding() {
      return this.angledWielding;
   }

   public DamageEffects getBlockedEffects() {
      return this.blockedEffects;
   }

   public WieldingInteraction.StaminaCost getStaminaCost() {
      return this.staminaCost;
   }

   public String getBlockedInteractions() {
      return this.blockedInteractions;
   }

   @Override
   protected void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Ref<EntityStore> ref = context.getEntity();
      World world = commandBuffer.getExternalData().getWorld();
      DamageDataComponent damageDataComponent = commandBuffer.getComponent(ref, DamageDataComponent.getComponentType());

      assert damageDataComponent != null;

      if (Interaction.failed(context.getState().state)) {
         damageDataComponent.setCurrentWielding(null);
      } else {
         CombatConfig combatConfig = world.getGameplayConfig().getCombatConfig();
         EffectControllerComponent effectControllerComponent = commandBuffer.getComponent(ref, EffectControllerComponent.getComponentType());
         if (effectControllerComponent != null) {
            Int2ObjectMap<ActiveEntityEffect> activeEffects = effectControllerComponent.getActiveEffects();
            if (!firstRun && activeEffects.containsKey(combatConfig.getStaminaBrokenEffectIndex())) {
               damageDataComponent.setCurrentWielding(null);
               context.getState().state = InteractionState.Failed;
               if (context.hasLabels()) {
                  context.jump(context.getLabel(this.next != null ? this.next.size() : 0));
               }

               return;
            }
         }

         super.tick0(firstRun, time, type, context, cooldownHandler);
         if (firstRun && context.getState().state == InteractionState.NotFinished) {
            damageDataComponent.setCurrentWielding(this);
         } else {
            if (context.getState().state == InteractionState.Finished) {
               damageDataComponent.setCurrentWielding(null);
            }
         }
      }
   }

   @Override
   protected void simulateTick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Ref<EntityStore> ref = context.getEntity();
      World world = commandBuffer.getExternalData().getWorld();
      CombatConfig combatConfig = world.getGameplayConfig().getCombatConfig();
      EffectControllerComponent effectControllerComponent = commandBuffer.getComponent(ref, EffectControllerComponent.getComponentType());
      if (effectControllerComponent != null) {
         Int2ObjectMap<ActiveEntityEffect> activeEffects = effectControllerComponent.getActiveEffects();
         if (!firstRun && activeEffects.containsKey(combatConfig.getStaminaBrokenEffectIndex())) {
            context.getState().state = InteractionState.Failed;
            if (context.hasLabels()) {
               context.jump(context.getLabel(this.next != null ? this.next.size() : 0));
            }

            return;
         }
      }

      super.simulateTick0(firstRun, time, type, context, cooldownHandler);
   }

   @Override
   public void handle(@Nonnull Ref<EntityStore> ref, boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context) {
      super.handle(ref, firstRun, time, type, context);
      if (context.getState().state != InteractionState.NotFinished) {
         CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

         assert commandBuffer != null;

         DamageDataComponent damageDataComponent = commandBuffer.getComponent(ref, DamageDataComponent.getComponentType());

         assert damageDataComponent != null;

         damageDataComponent.setCurrentWielding(null);
      }
   }

   @Nonnull
   @Override
   protected com.hypixel.hytale.protocol.Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.WieldingInteraction();
   }

   @Override
   protected void configurePacket(com.hypixel.hytale.protocol.Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.WieldingInteraction p = (com.hypixel.hytale.protocol.WieldingInteraction)packet;
      if (this.blockedEffects != null) {
         p.blockedEffects = this.blockedEffects.toPacket();
      }

      if (this.angledWielding != null) {
         p.angledWielding = this.angledWielding.toPacket();
      }

      p.hasModifiers = this.damageModifiersRaw != null || this.knockbackModifiersRaw != null;
   }

   @Nonnull
   @Override
   public String toString() {
      return "WieldingInteraction{knockbackModifiers="
         + this.knockbackModifiersRaw
         + ", damageModifiers="
         + this.damageModifiersRaw
         + ", angledWielding="
         + this.angledWielding
         + ", failed='"
         + this.failed
         + "', staminaCost="
         + this.staminaCost
         + ", blockedEffects="
         + this.blockedEffects
         + "} "
         + super.toString();
   }

   public static class AngledWielding implements NetworkSerializable<com.hypixel.hytale.protocol.AngledWielding> {
      public static final BuilderCodec<WieldingInteraction.AngledWielding> CODEC = BuilderCodec.builder(
            WieldingInteraction.AngledWielding.class, WieldingInteraction.AngledWielding::new
         )
         .appendInherited(
            new KeyedCodec<>("Angle", Codec.FLOAT),
            (o, i) -> o.angleRad = i * (float) (Math.PI / 180.0),
            o -> o.angleRad * (180.0F / (float)Math.PI),
            (o, p) -> o.angleRad = p.angleRad
         )
         .add()
         .appendInherited(
            new KeyedCodec<>("AngleDistance", Codec.FLOAT),
            (o, i) -> o.angleDistanceRad = i * (float) (Math.PI / 180.0),
            o -> o.angleDistanceRad * (180.0F / (float)Math.PI),
            (o, p) -> o.angleDistanceRad = p.angleDistanceRad
         )
         .add()
         .<Object2DoubleMap<String>>appendInherited(
            new KeyedCodec<>("KnockbackModifiers", new Object2DoubleMapCodec<>(Codec.STRING, Object2DoubleOpenHashMap::new)),
            (o, m) -> o.knockbackModifiersRaw = m,
            o -> o.knockbackModifiersRaw,
            (o, p) -> o.knockbackModifiersRaw = p.knockbackModifiersRaw
         )
         .addValidator(DamageCause.VALIDATOR_CACHE.getMapKeyValidator())
         .add()
         .<Object2FloatMap<String>>appendInherited(
            new KeyedCodec<>("DamageModifiers", new Object2FloatMapCodec<>(Codec.STRING, Object2FloatOpenHashMap::new)),
            (o, m) -> o.damageModifiersRaw = m,
            o -> o.damageModifiersRaw,
            (o, p) -> o.damageModifiersRaw = p.damageModifiersRaw
         )
         .addValidator(DamageCause.VALIDATOR_CACHE.getMapKeyValidator())
         .add()
         .afterDecode(o -> {
            if (o.knockbackModifiersRaw != null) {
               o.knockbackModifiers = new Int2DoubleOpenHashMap();
               Iterator i$ = o.knockbackModifiersRaw.object2DoubleEntrySet().iterator();

               while (i$.hasNext()) {
                  Entry<String> entry = (Entry<String>)i$.next();
                  int index = DamageCause.getAssetMap().getIndex((String)entry.getKey());
                  o.knockbackModifiers.put(index, entry.getDoubleValue());
               }
            }

            if (o.damageModifiersRaw != null) {
               o.damageModifiers = new Int2FloatOpenHashMap();
               Iterator i$ = o.damageModifiersRaw.object2FloatEntrySet().iterator();

               while (i$.hasNext()) {
                  it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry<String> entry = (it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry<String>)i$.next();
                  int index = DamageCause.getAssetMap().getIndex((String)entry.getKey());
                  o.damageModifiers.put(index, entry.getFloatValue());
               }
            }
         })
         .build();
      protected float angleRad;
      protected float angleDistanceRad;
      @Nullable
      protected Object2DoubleMap<String> knockbackModifiersRaw;
      @Nullable
      protected Object2FloatMap<String> damageModifiersRaw;
      @Nonnull
      protected transient Int2DoubleMap knockbackModifiers = Int2DoubleMaps.EMPTY_MAP;
      @Nonnull
      protected transient Int2FloatMap damageModifiers = Int2FloatMaps.EMPTY_MAP;

      public double getAngleRad() {
         return this.angleRad;
      }

      public double getAngleDistanceRad() {
         return this.angleDistanceRad;
      }

      @Nonnull
      public Int2DoubleMap getKnockbackModifiers() {
         return this.knockbackModifiers;
      }

      @Nonnull
      public Int2FloatMap getDamageModifiers() {
         return this.damageModifiers;
      }

      @Nonnull
      public com.hypixel.hytale.protocol.AngledWielding toPacket() {
         com.hypixel.hytale.protocol.AngledWielding packet = new com.hypixel.hytale.protocol.AngledWielding();
         packet.angleRad = this.angleRad;
         packet.angleDistanceRad = this.angleDistanceRad;
         packet.hasModifiers = this.damageModifiersRaw != null || this.knockbackModifiersRaw != null;
         return packet;
      }
   }

   public static class StaminaCost {
      public static final BuilderCodec<WieldingInteraction.StaminaCost> CODEC = BuilderCodec.builder(
            WieldingInteraction.StaminaCost.class, WieldingInteraction.StaminaCost::new
         )
         .append(
            new KeyedCodec<>("CostType", new EnumCodec<>(WieldingInteraction.StaminaCost.CostType.class)),
            (staminaCost, costType) -> staminaCost.costType = costType,
            staminaCost -> staminaCost.costType
         )
         .documentation(
            "Define how the stamina loss is computed. Use MAX_HEALTH_PERCENTAGE to define how many % of the player's max health 1 stamina point is worth. Use DAMAGE define how much damage 1 stamina point is worth. Default value is MAX_HEALTH_PERCENTAGE."
         )
         .add()
         .<Float>append(new KeyedCodec<>("Value", Codec.FLOAT), (staminaCost, aFloat) -> staminaCost.value = aFloat, staminaCost -> staminaCost.value)
         .addValidator(Validators.greaterThanOrEqual(0.0F))
         .documentation(
            "The value to define how much a stamina point is worth. When CostType.MAX_HEALTH_PERCENTAGE, a ratio is expected, so for 4% of max health, the value expected here is 0.04. Default value is 0.04f"
         )
         .add()
         .build();
      private WieldingInteraction.StaminaCost.CostType costType = WieldingInteraction.StaminaCost.CostType.MAX_HEALTH_PERCENTAGE;
      private float value = 0.04F;

      public float computeStaminaAmountToConsume(float damageRaw, @Nonnull EntityStatMap entityStatMap) {
         return switch (this.costType) {
            case MAX_HEALTH_PERCENTAGE -> damageRaw / (this.value * entityStatMap.get(DefaultEntityStatTypes.getHealth()).getMax());
            case DAMAGE -> damageRaw / this.value;
         };
      }

      @Nonnull
      @Override
      public String toString() {
         return "StaminaCost{costType=" + this.costType + ", value=" + this.value + "}";
      }

      static enum CostType {
         MAX_HEALTH_PERCENTAGE,
         DAMAGE;
      }
   }
}
