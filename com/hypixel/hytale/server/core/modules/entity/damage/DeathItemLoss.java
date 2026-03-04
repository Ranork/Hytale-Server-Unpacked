package com.hypixel.hytale.server.core.modules.entity.damage;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public class DeathItemLoss {
   public static final BuilderCodec<DeathItemLoss> CODEC = BuilderCodec.builder(DeathItemLoss.class, DeathItemLoss::new)
      .append(new KeyedCodec<>("LossMode", DeathConfig.LOSS_MODE_CODEC), (loss, o) -> loss.lossMode = o, loss -> loss.lossMode)
      .add()
      .append(
         new KeyedCodec<>("ItemsLostOnDeath", new ArrayCodec<>(ItemStack.CODEC, ItemStack[]::new)),
         (loss, items) -> loss.itemsLost = items,
         loss -> loss.itemsLost
      )
      .add()
      .append(
         new KeyedCodec<>("ItemsAmountLossPercentage", Codec.DOUBLE), (loss, percent) -> loss.amountLossPercentage = percent, loss -> loss.amountLossPercentage
      )
      .add()
      .append(
         new KeyedCodec<>("ItemsDurabilityLossPercentage", Codec.DOUBLE),
         (loss, percent) -> loss.durabilityLossPercentage = percent,
         loss -> loss.durabilityLossPercentage
      )
      .add()
      .build();
   private DeathConfig.ItemsLossMode lossMode;
   private ItemStack[] itemsLost;
   private double amountLossPercentage;
   private double durabilityLossPercentage;
   private static final DeathItemLoss NO_LOSS_MODE = new DeathItemLoss(DeathConfig.ItemsLossMode.NONE, ItemStack.EMPTY_ARRAY, 0.0, 0.0);

   private DeathItemLoss() {
   }

   public DeathItemLoss(DeathConfig.ItemsLossMode lossMode, ItemStack[] itemsLost, double amountLossPercentage, double durabilityLossPercentage) {
      this.lossMode = lossMode;
      this.itemsLost = itemsLost;
      this.amountLossPercentage = amountLossPercentage;
      this.durabilityLossPercentage = durabilityLossPercentage;
   }

   public static DeathItemLoss noLossMode() {
      return NO_LOSS_MODE;
   }

   public DeathConfig.ItemsLossMode getLossMode() {
      return this.lossMode;
   }

   public ItemStack[] getItemsLost() {
      return this.itemsLost == null ? ItemStack.EMPTY_ARRAY : this.itemsLost;
   }

   public double getAmountLossPercentage() {
      return this.amountLossPercentage;
   }

   public double getDurabilityLossPercentage() {
      return this.durabilityLossPercentage;
   }
}
