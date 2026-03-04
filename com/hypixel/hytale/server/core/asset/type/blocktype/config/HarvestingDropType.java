package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.Harvesting;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HarvestingDropType implements NetworkSerializable<Harvesting> {
   public static final BuilderCodec<HarvestingDropType> CODEC = BuilderCodec.builder(HarvestingDropType.class, HarvestingDropType::new)
      .append(new KeyedCodec<>("ItemId", Codec.STRING), (harvesting, o) -> harvesting.itemId = o, harvesting -> harvesting.itemId)
      .addValidatorLate(() -> Item.VALIDATOR_CACHE.getValidator().late())
      .add()
      .<String>append(
         new KeyedCodec<>("DropList", new ContainedAssetCodec<>(ItemDropList.class, ItemDropList.CODEC)),
         (harvesting, o) -> harvesting.dropListId = o,
         harvesting -> harvesting.dropListId
      )
      .addValidatorLate(() -> ItemDropList.VALIDATOR_CACHE.getValidator().late())
      .add()
      .build();
   protected String itemId;
   protected String dropListId;

   public HarvestingDropType(String itemId, String dropListId) {
      this.itemId = itemId;
      this.dropListId = dropListId;
   }

   protected HarvestingDropType() {
   }

   @Nonnull
   public Harvesting toPacket() {
      Harvesting packet = new Harvesting();
      if (this.itemId != null) {
         packet.itemId = this.itemId.toString();
      }

      packet.dropListId = this.dropListId;
      return packet;
   }

   public String getItemId() {
      return this.itemId;
   }

   public String getDropListId() {
      return this.dropListId;
   }

   @Nullable
   public HarvestingDropType withoutDrops() {
      return null;
   }

   @Nonnull
   @Override
   public String toString() {
      return "HarvestingDropType{itemId=" + this.itemId + ", dropListId='" + this.dropListId + "'}";
   }
}
