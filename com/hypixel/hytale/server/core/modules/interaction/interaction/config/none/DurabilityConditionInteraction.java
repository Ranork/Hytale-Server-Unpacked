package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.protocol.DurabilityOperator;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ValueType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import javax.annotation.Nonnull;

public class DurabilityConditionInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<DurabilityConditionInteraction> CODEC = BuilderCodec.builder(
         DurabilityConditionInteraction.class, DurabilityConditionInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Gates the interaction chain on the held item's durability.")
      .<Float>appendInherited(
         new KeyedCodec<>("Threshold", Codec.FLOAT), (i, v) -> i.threshold = v, i -> i.threshold, (i, parent) -> i.threshold = parent.threshold
      )
      .documentation("Durability value to compare against. Interpreted as Absolute (raw durability) or Percent (durability/maxDurability * 100) per ValueType.")
      .add()
      .<ValueType>appendInherited(
         new KeyedCodec<>("ValueType", new EnumCodec<>(ValueType.class)),
         (i, v) -> i.valueType = v,
         i -> i.valueType,
         (i, parent) -> i.valueType = parent.valueType
      )
      .documentation("Whether Threshold is interpreted as an Absolute durability value or a Percent of MaxDurability. Default: Absolute.")
      .add()
      .<DurabilityOperator>appendInherited(
         new KeyedCodec<>("Operator", new EnumCodec<>(DurabilityOperator.class)),
         (i, v) -> i.operator = v,
         i -> i.operator,
         (i, parent) -> i.operator = parent.operator
      )
      .documentation(
         "How to compare the item's durability to Threshold. Default: LessOrEqual (succeeds when durability <= Threshold). Choices: LessThan, LessOrEqual, GreaterThan, GreaterOrEqual, Equal, NotEqual."
      )
      .add()
      .build();
   protected float threshold;
   @Nonnull
   protected ValueType valueType = ValueType.Absolute;
   @Nonnull
   protected DurabilityOperator operator = DurabilityOperator.LessOrEqual;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      if (!this.matches(context)) {
         context.getState().state = InteractionState.Failed;
      }
   }

   protected boolean matches(@Nonnull InteractionContext context) {
      ItemStack item = context.getHeldItem();
      if (item != null && !item.isUnbreakable()) {
         double maxDurability = item.getMaxDurability();
         double rawDurability = item.getDurability();
         double value = this.valueType == ValueType.Absolute ? rawDurability : (maxDurability > 0.0 ? rawDurability / maxDurability * 100.0 : 0.0);

         return switch (this.operator) {
            case LessThan -> value < this.threshold;
            case LessOrEqual -> value <= this.threshold;
            case GreaterThan -> value > this.threshold;
            case GreaterOrEqual -> value >= this.threshold;
            case Equal -> value == this.threshold;
            case NotEqual -> value != this.threshold;
         };
      } else {
         return false;
      }
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.DurabilityConditionInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.DurabilityConditionInteraction p = (com.hypixel.hytale.protocol.DurabilityConditionInteraction)packet;
      p.threshold = this.threshold;
      p.valueType = this.valueType;
      p.operator = this.operator;
   }

   @Nonnull
   @Override
   public String toString() {
      return "DurabilityConditionInteraction{threshold="
         + this.threshold
         + ", valueType="
         + this.valueType
         + ", operator="
         + this.operator
         + "} "
         + super.toString();
   }
}
