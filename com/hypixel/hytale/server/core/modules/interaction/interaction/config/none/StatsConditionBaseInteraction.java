package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.map.Object2FloatMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ValueType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class StatsConditionBaseInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<StatsConditionBaseInteraction> CODEC = BuilderCodec.abstractBuilder(
         StatsConditionBaseInteraction.class, SimpleInstantInteraction.CODEC
      )
      .appendInherited(
         new KeyedCodec<>("Costs", new Object2FloatMapCodec<>(Codec.STRING, Object2FloatOpenHashMap::new)),
         (i, s) -> i.rawCosts = s,
         i -> i.rawCosts,
         (i, parent) -> i.rawCosts = parent.rawCosts
      )
      .addValidator(Validators.nonNull())
      .addValidator(EntityStatType.VALIDATOR_CACHE.getMapKeyValidator())
      .add()
      .appendInherited(
         new KeyedCodec<>("LessThan", Codec.BOOLEAN),
         (statsConditionInteraction, aBoolean) -> statsConditionInteraction.lessThan = aBoolean,
         statsConditionInteraction -> statsConditionInteraction.lessThan,
         (statsConditionInteraction, parent) -> statsConditionInteraction.lessThan = parent.lessThan
      )
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("Lenient", Codec.BOOLEAN),
         (statsConditionInteraction, aBoolean) -> statsConditionInteraction.lenient = aBoolean,
         statsConditionInteraction -> statsConditionInteraction.lenient,
         (statsConditionInteraction, parent) -> statsConditionInteraction.lenient = parent.lenient
      )
      .documentation("Specifies that the interaction can run even if the stat cost is not met, providing that the value is greater than zero.")
      .add()
      .<ValueType>appendInherited(
         new KeyedCodec<>("ValueType", new EnumCodec<>(ValueType.class)),
         (statsConditionInteraction, valueType) -> statsConditionInteraction.valueType = valueType,
         statsConditionInteraction -> statsConditionInteraction.valueType,
         (statsConditionInteraction, parent) -> statsConditionInteraction.valueType = parent.valueType
      )
      .documentation(
         "Enum to specify if the Costs must be considered as absolute values or percent. Default value is Absolute. When using ValueType.Absolute, '100' matches the max value."
      )
      .add()
      .afterDecode(c -> c.costs = EntityStatsModule.resolveEntityStats(c.rawCosts))
      .build();
   protected Object2FloatMap<String> rawCosts;
   @Nullable
   protected Int2FloatMap costs;
   protected boolean lessThan;
   protected boolean lenient;
   protected ValueType valueType = ValueType.Absolute;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> ref = context.getEntity();
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
      if (!this.canAfford(ref, commandBuffer)) {
         context.getState().state = InteractionState.Failed;
      }
   }

   protected abstract boolean canAfford(@Nonnull Ref<EntityStore> var1, @Nonnull ComponentAccessor<EntityStore> var2);

   protected boolean canOverdraw(float value, float min) {
      return this.lenient && value > 0.0F && min < 0.0F;
   }

   @Nonnull
   @Override
   public String toString() {
      return "StatsConditionBaseInteraction{rawCosts="
         + this.rawCosts
         + ", costs="
         + this.costs
         + ", lessThan="
         + this.lessThan
         + ", lenient="
         + this.lenient
         + ", valueType="
         + this.valueType
         + "}"
         + super.toString();
   }
}
