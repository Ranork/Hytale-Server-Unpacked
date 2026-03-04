package com.hypixel.hytale.builtin.adventure.objectives.historydata;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import javax.annotation.Nonnull;

public final class ItemObjectiveRewardHistoryData extends ObjectiveRewardHistoryData {
   @Nonnull
   public static final BuilderCodec<ItemObjectiveRewardHistoryData> CODEC = BuilderCodec.builder(
         ItemObjectiveRewardHistoryData.class, ItemObjectiveRewardHistoryData::new, BASE_CODEC
      )
      .append(
         new KeyedCodec<>("ItemId", Codec.STRING),
         (itemObjectiveRewardDetails, blockTypeKey) -> itemObjectiveRewardDetails.itemId = blockTypeKey,
         itemObjectiveRewardDetails -> itemObjectiveRewardDetails.itemId
      )
      .addValidator(Item.VALIDATOR_CACHE.getValidator())
      .add()
      .append(
         new KeyedCodec<>("Quantity", Codec.INTEGER),
         (itemObjectiveRewardDetails, integer) -> itemObjectiveRewardDetails.quantity = integer,
         itemObjectiveRewardDetails -> itemObjectiveRewardDetails.quantity
      )
      .add()
      .build();
   protected String itemId;
   protected int quantity;

   public ItemObjectiveRewardHistoryData(String itemId, int quantity) {
      this.itemId = itemId;
      this.quantity = quantity;
   }

   protected ItemObjectiveRewardHistoryData() {
   }

   public String getItemId() {
      return this.itemId;
   }

   public int getQuantity() {
      return this.quantity;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemObjectiveRewardHistoryData{itemId=" + this.itemId + ", quantity=" + this.quantity + "} " + super.toString();
   }
}
