package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.gameplay.BrokenPenalties;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StatModifiersManager {
   @Nonnull
   private final AtomicBoolean recalculate = new AtomicBoolean();
   @Nonnull
   private final IntSet statsToClear = new IntOpenHashSet();

   public void setRecalculate(boolean value) {
      this.recalculate.set(value);
   }

   public void queueEntityStatsToClear(@Nonnull int[] entityStatsToClear) {
      for (int i = 0; i < entityStatsToClear.length; i++) {
         this.statsToClear.add(entityStatsToClear[i]);
      }
   }

   public void recalculateEntityStatModifiers(
      @Nonnull Ref<EntityStore> ref, @Nonnull EntityStatMap statMap, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.recalculate.getAndSet(false)) {
         if (!this.statsToClear.isEmpty()) {
            IntIterator iterator = this.statsToClear.iterator();

            while (iterator.hasNext()) {
               statMap.minimizeStatValue(EntityStatMap.Predictable.SELF, iterator.nextInt());
            }

            this.statsToClear.clear();
         }

         World world = componentAccessor.getExternalData().getWorld();
         if (EntityUtils.getEntity(ref, componentAccessor) instanceof LivingEntity livingEntity) {
            Inventory var12 = livingEntity.getInventory();
            Int2ObjectOpenHashMap effectModifiers = calculateEffectStatModifiers(ref, componentAccessor);
            applyEffectModifiers(statMap, effectModifiers);
            BrokenPenalties brokenPenalties = world.getGameplayConfig().getItemDurabilityConfig().getBrokenPenalties();
            Int2ObjectMap statModifiers = computeStatModifiers(brokenPenalties, var12);
            applyStatModifiers(statMap, statModifiers);
            ItemStack itemInHand = var12.getItemInHand();
            addItemStatModifiers(itemInHand, statMap, "*Weapon_", v -> v.getWeapon() != null ? v.getWeapon().getStatModifiers() : null);
            if (itemInHand == null || itemInHand.getItem().getUtility().isCompatible()) {
               addItemStatModifiers(var12.getUtilityItem(), statMap, "*Utility_", v -> v.getUtility().getStatModifiers());
            }
         }
      }
   }

   @Nonnull
   private static Int2ObjectOpenHashMap<Object2FloatMap<StaticModifier.CalculationType>> calculateEffectStatModifiers(
      @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Int2ObjectOpenHashMap<Object2FloatMap<StaticModifier.CalculationType>> statModifiers = new Int2ObjectOpenHashMap();
      EffectControllerComponent effectControllerComponent = componentAccessor.getComponent(ref, EffectControllerComponent.getComponentType());
      if (effectControllerComponent == null) {
         return statModifiers;
      } else {
         effectControllerComponent.getActiveEffects()
            .forEach(
               (k, v) -> {
                  if (v.isInfinite() || !(v.getRemainingDuration() <= 0.0F)) {
                     int index = v.getEntityEffectIndex();
                     EntityEffect effect = EntityEffect.getAssetMap().getAsset(index);
                     if (effect != null && effect.getStatModifiers() != null) {
                        Iterator i$ = effect.getStatModifiers().int2ObjectEntrySet().iterator();

                        while (i$.hasNext()) {
                           it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<StaticModifier[]> entry = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<StaticModifier[]>)i$.next();
                           int entityStatType = entry.getIntKey();

                           for (StaticModifier modifier : (StaticModifier[])entry.getValue()) {
                              float value = modifier.getAmount();
                              Object2FloatMap<StaticModifier.CalculationType> statModifierToApply = (Object2FloatMap<StaticModifier.CalculationType>)statModifiers.computeIfAbsent(
                                 entityStatType, x -> new Object2FloatOpenHashMap()
                              );
                              statModifierToApply.mergeFloat(modifier.getCalculationType(), value, Float::sum);
                           }
                        }
                     }
                  }
               }
            );
         return statModifiers;
      }
   }

   private static void applyEffectModifiers(
      @Nonnull EntityStatMap statMap, @Nonnull Int2ObjectMap<Object2FloatMap<StaticModifier.CalculationType>> statModifiers
   ) {
      for (int i = 0; i < statMap.size(); i++) {
         Object2FloatMap<StaticModifier.CalculationType> statModifiersForEntityStat = (Object2FloatMap<StaticModifier.CalculationType>)statModifiers.get(i);
         if (statModifiersForEntityStat == null) {
            for (StaticModifier.CalculationType calculationType : StaticModifier.CalculationType.values()) {
               statMap.removeModifier(i, calculationType.createKey("Effect"));
            }
         } else {
            for (StaticModifier.CalculationType calculationType : StaticModifier.CalculationType.values()) {
               if (!statModifiersForEntityStat.containsKey(calculationType)) {
                  statMap.removeModifier(i, calculationType.createKey("Effect"));
               }
            }

            ObjectIterator var8 = statModifiersForEntityStat.object2FloatEntrySet().iterator();

            while (var8.hasNext()) {
               Entry<StaticModifier.CalculationType> entry = (Entry<StaticModifier.CalculationType>)var8.next();
               StaticModifier.CalculationType calculationTypex = (StaticModifier.CalculationType)entry.getKey();
               StaticModifier modifier = new StaticModifier(Modifier.ModifierTarget.MAX, calculationTypex, entry.getFloatValue());
               statMap.putModifier(i, calculationTypex.createKey("Effect"), modifier);
            }
         }
      }
   }

   private static void computeStatModifiers(
      double brokenPenalty,
      @Nonnull Int2ObjectMap<Object2FloatMap<StaticModifier.CalculationType>> statModifiers,
      @Nonnull ItemStack itemInHand,
      @Nonnull Int2ObjectMap<StaticModifier[]> itemStatModifiers
   ) {
      boolean broken = itemInHand.isBroken();
      ObjectIterator var6 = itemStatModifiers.int2ObjectEntrySet().iterator();

      while (var6.hasNext()) {
         it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<StaticModifier[]> entry = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<StaticModifier[]>)var6.next();
         int entityStatType = entry.getIntKey();

         for (StaticModifier modifier : (StaticModifier[])entry.getValue()) {
            float value = modifier.getAmount();
            if (broken) {
               value = (float)(value * brokenPenalty);
            }

            Object2FloatMap<StaticModifier.CalculationType> statModifierToApply = (Object2FloatMap<StaticModifier.CalculationType>)statModifiers.computeIfAbsent(
               entityStatType, x -> new Object2FloatOpenHashMap()
            );
            statModifierToApply.mergeFloat(modifier.getCalculationType(), value, Float::sum);
         }
      }
   }

   @Nonnull
   private static Int2ObjectMap<Object2FloatMap<StaticModifier.CalculationType>> computeStatModifiers(
      @Nonnull BrokenPenalties brokenPenalties, @Nonnull Inventory inventory
   ) {
      Int2ObjectOpenHashMap<Object2FloatMap<StaticModifier.CalculationType>> statModifiers = new Int2ObjectOpenHashMap();
      double armorBrokenPenalty = brokenPenalties.getArmor(0.0);
      ItemContainer armorContainer = inventory.getArmor();

      for (short i = 0; i < armorContainer.getCapacity(); i++) {
         ItemStack armorItemStack = armorContainer.getItemStack(i);
         if (armorItemStack != null) {
            addArmorStatModifiers(armorItemStack, armorBrokenPenalty, statModifiers);
         }
      }

      return statModifiers;
   }

   private static void addArmorStatModifiers(
      @Nonnull ItemStack itemStack, double brokenPenalties, @Nonnull Int2ObjectOpenHashMap<Object2FloatMap<StaticModifier.CalculationType>> statModifiers
   ) {
      if (!ItemStack.isEmpty(itemStack)) {
         ItemArmor armorItem = itemStack.getItem().getArmor();
         if (armorItem != null) {
            Int2ObjectMap<StaticModifier[]> itemStatModifiers = armorItem.getStatModifiers();
            if (itemStatModifiers != null) {
               computeStatModifiers(brokenPenalties, statModifiers, itemStack, itemStatModifiers);
            }
         }
      }
   }

   private static void addItemStatModifiers(
      @Nullable ItemStack itemStack,
      @Nonnull EntityStatMap entityStatMap,
      @Nonnull String prefix,
      @Nonnull Function<Item, Int2ObjectMap<StaticModifier[]>> toStatModifiers
   ) {
      if (ItemStack.isEmpty(itemStack)) {
         clearAllStatModifiers(EntityStatMap.Predictable.SELF, entityStatMap, prefix, null);
      } else {
         Int2ObjectMap<StaticModifier[]> itemStatModifiers = toStatModifiers.apply(itemStack.getItem());
         if (itemStatModifiers == null) {
            clearAllStatModifiers(EntityStatMap.Predictable.SELF, entityStatMap, prefix, null);
         } else {
            ObjectIterator var5 = itemStatModifiers.int2ObjectEntrySet().iterator();

            while (var5.hasNext()) {
               it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<StaticModifier[]> entry = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<StaticModifier[]>)var5.next();
               int offset = 0;
               int statIndex = entry.getIntKey();

               for (StaticModifier modifier : (StaticModifier[])entry.getValue()) {
                  String key = prefix + offset;
                  offset++;
                  if (!(entityStatMap.getModifier(statIndex, key) instanceof StaticModifier existingStatic && existingStatic.equals(modifier))) {
                     entityStatMap.putModifier(EntityStatMap.Predictable.SELF, statIndex, key, modifier);
                  }
               }

               clearStatModifiers(EntityStatMap.Predictable.SELF, entityStatMap, statIndex, prefix, offset);
            }

            clearAllStatModifiers(EntityStatMap.Predictable.SELF, entityStatMap, prefix, itemStatModifiers);
         }
      }
   }

   private static void clearAllStatModifiers(
      @Nonnull EntityStatMap.Predictable predictable,
      @Nonnull EntityStatMap entityStatMap,
      @Nonnull String prefix,
      @Nullable Int2ObjectMap<StaticModifier[]> excluding
   ) {
      for (int i = 0; i < entityStatMap.size(); i++) {
         if (excluding == null || !excluding.containsKey(i)) {
            clearStatModifiers(predictable, entityStatMap, i, prefix, 0);
         }
      }
   }

   private static void clearStatModifiers(
      @Nonnull EntityStatMap.Predictable predictable, @Nonnull EntityStatMap entityStatMap, int statIndex, @Nonnull String prefix, int offset
   ) {
      String key;
      do {
         key = prefix + offset;
         offset++;
      } while (entityStatMap.removeModifier(predictable, statIndex, key) != null);
   }

   private static void applyStatModifiers(@Nonnull EntityStatMap statMap, @Nonnull Int2ObjectMap<Object2FloatMap<StaticModifier.CalculationType>> statModifiers) {
      for (int i = 0; i < statMap.size(); i++) {
         Object2FloatMap<StaticModifier.CalculationType> statModifiersForEntityStat = (Object2FloatMap<StaticModifier.CalculationType>)statModifiers.get(i);
         if (statModifiersForEntityStat == null) {
            for (StaticModifier.CalculationType calculationType : StaticModifier.CalculationType.values()) {
               statMap.removeModifier(i, calculationType.createKey("Armor"));
            }
         } else {
            for (StaticModifier.CalculationType calculationType : StaticModifier.CalculationType.values()) {
               if (!statModifiersForEntityStat.containsKey(calculationType)) {
                  statMap.removeModifier(i, calculationType.createKey("Armor"));
               }
            }

            ObjectIterator var8 = statModifiersForEntityStat.object2FloatEntrySet().iterator();

            while (var8.hasNext()) {
               Entry<StaticModifier.CalculationType> entry = (Entry<StaticModifier.CalculationType>)var8.next();
               StaticModifier.CalculationType calculationTypex = (StaticModifier.CalculationType)entry.getKey();
               StaticModifier modifier = new StaticModifier(Modifier.ModifierTarget.MAX, calculationTypex, entry.getFloatValue());
               statMap.putModifier(i, calculationTypex.createKey("Armor"), modifier);
            }
         }
      }
   }
}
