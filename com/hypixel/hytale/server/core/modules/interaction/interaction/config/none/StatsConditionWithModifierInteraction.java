package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ValueType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StatsConditionWithModifierInteraction extends StatsConditionBaseInteraction {
   @Nonnull
   public static final BuilderCodec<StatsConditionWithModifierInteraction> CODEC = BuilderCodec.builder(
         StatsConditionWithModifierInteraction.class, StatsConditionWithModifierInteraction::new, StatsConditionBaseInteraction.CODEC
      )
      .documentation("Interaction that is successful if the given stat conditions match.")
      .<ItemArmor.InteractionModifierId>append(
         new KeyedCodec<>("InteractionModifierId", new EnumCodec<>(ItemArmor.InteractionModifierId.class)),
         (changeStatWithModifierInteraction, s) -> changeStatWithModifierInteraction.interactionModifierId = s,
         changeStatWithModifierInteraction -> changeStatWithModifierInteraction.interactionModifierId
      )
      .addValidator(Validators.nonNull())
      .add()
      .build();
   protected ItemArmor.InteractionModifierId interactionModifierId;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Ref<EntityStore> ref = context.getEntity();
      if (!this.canAfford(ref, commandBuffer)) {
         context.getState().state = InteractionState.Failed;
      }
   }

   @Override
   protected boolean canAfford(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      EntityStatMap entityStatMapComponent = componentAccessor.getComponent(ref, EntityStatMap.getComponentType());
      if (entityStatMapComponent == null) {
         return false;
      } else if (this.costs == null) {
         return false;
      } else {
         ObjectIterator var4 = this.costs.int2FloatEntrySet().iterator();

         while (var4.hasNext()) {
            Entry cost = (Entry)var4.next();
            EntityStatValue stat = entityStatMapComponent.get(cost.getIntKey());
            if (stat == null) {
               return false;
            }

            float statValue = this.valueType == ValueType.Absolute ? stat.get() : stat.asPercentage() * 100.0F;
            Inventory inventory = null;
            if (EntityUtils.getEntity(ref, componentAccessor) instanceof LivingEntity livingEntity) {
               inventory = livingEntity.getInventory();
            }

            float modifiedCost = this.calculateDiscount(inventory, cost.getIntKey(), cost.getFloatValue());
            if (this.lessThan) {
               if (statValue >= modifiedCost) {
                  return false;
               }
            } else if (statValue < modifiedCost && !this.canOverdraw(statValue, stat.getMin())) {
               return false;
            }
         }

         return true;
      }
   }

   private float calculateDiscount(@Nullable Inventory inventory, int statIndex, float baseCost) {
      float flatModifier = 0.0F;
      float multiplierModifier = 0.0F;
      ItemContainer armorContainer = null;
      if (inventory != null) {
         armorContainer = inventory.getArmor();
      }

      if (armorContainer != null) {
         for (short i = 0; i < armorContainer.getCapacity(); i++) {
            ItemStack itemStack = armorContainer.getItemStack(i);
            if (itemStack != null && !itemStack.isEmpty()) {
               Item item = itemStack.getItem();
               if (item != null && item.getArmor() != null) {
                  Int2ObjectMap<StaticModifier> statModifierMap = item.getArmor().getInteractionModifier(this.interactionModifierId.toString());
                  if (statModifierMap != null) {
                     StaticModifier statModifier = (StaticModifier)statModifierMap.get(statIndex);
                     if (statModifier.getCalculationType() == StaticModifier.CalculationType.ADDITIVE) {
                        flatModifier += statModifier.getAmount();
                     } else {
                        multiplierModifier = statModifier.getAmount();
                     }
                  }
               }
            }
         }
      }

      float modifiedCost = baseCost + flatModifier;
      return modifiedCost * Math.max(0.0F, 1.0F - multiplierModifier);
   }

   @Nonnull
   @Override
   public String toString() {
      return "StatsConditionWithModifierInteraction{interactionModifierId=" + this.interactionModifierId + "}" + super.toString();
   }
}
