package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import javax.annotation.Nonnull;

public class ChangeStatWithModifierInteraction extends ChangeStatBaseInteraction {
   public static final BuilderCodec<ChangeStatWithModifierInteraction> CODEC = BuilderCodec.builder(
         ChangeStatWithModifierInteraction.class, ChangeStatWithModifierInteraction::new, ChangeStatBaseInteraction.CODEC
      )
      .documentation("Changes the given stats.")
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
      Ref<EntityStore> ref = context.getEntity();
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      EntityStatMap entityStatMapComponent = commandBuffer.getComponent(ref, EntityStatMap.getComponentType());

      assert entityStatMapComponent != null;

      Int2FloatMap adjustedEntityStats = new Int2FloatOpenHashMap(this.entityStats);
      Inventory inventory = null;
      if (EntityUtils.getEntity(ref, commandBuffer) instanceof LivingEntity livingEntity) {
         inventory = livingEntity.getInventory();
      }

      IntIterator var19 = adjustedEntityStats.keySet().iterator();

      while (var19.hasNext()) {
         int index = (Integer)var19.next();
         if (inventory != null) {
            ItemContainer armorContainer = inventory.getArmor();
            if (armorContainer != null) {
               float flatModifier = 0.0F;
               float multiplierModifier = 0.0F;

               for (short i = 0; i < armorContainer.getCapacity(); i++) {
                  ItemStack itemStack = armorContainer.getItemStack(i);
                  if (itemStack != null && !itemStack.isEmpty()) {
                     Item item = itemStack.getItem();
                     if (item != null && item.getArmor() != null) {
                        Int2ObjectMap<StaticModifier> statModifierMap = item.getArmor().getInteractionModifier(this.interactionModifierId.toString());
                        if (statModifierMap != null) {
                           StaticModifier statModifier = (StaticModifier)statModifierMap.get(index);
                           if (statModifier != null) {
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

               float cost = this.entityStats.get(index);
               cost += flatModifier;
               cost *= Math.max(0.0F, 1.0F - multiplierModifier);
               adjustedEntityStats.replace(index, cost);
            }
         }
      }

      entityStatMapComponent.processStatChanges(EntityStatMap.Predictable.NONE, adjustedEntityStats, this.valueType, this.changeStatBehaviour);
   }

   @Nonnull
   @Override
   public String toString() {
      return "ChangeStatWithModifierInteraction{interactionModifierId=" + this.interactionModifierId + "}" + super.toString();
   }
}
