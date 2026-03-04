package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PhysicsDropType {
   public static final BuilderCodec<PhysicsDropType> CODEC = BuilderCodec.builder(PhysicsDropType.class, PhysicsDropType::new)
      .append(new KeyedCodec<>("ItemId", Codec.STRING), (softBlock, o) -> softBlock.itemId = o, softBlock -> softBlock.itemId)
      .addValidatorLate(() -> Item.VALIDATOR_CACHE.getValidator().late())
      .add()
      .<String>append(
         new KeyedCodec<>("DropList", new ContainedAssetCodec<>(ItemDropList.class, ItemDropList.CODEC)),
         (softBlock, o) -> softBlock.dropListId = o,
         softBlock -> softBlock.dropListId
      )
      .addValidatorLate(() -> ItemDropList.VALIDATOR_CACHE.getValidator().late())
      .add()
      .build();
   protected String itemId;
   protected String dropListId;

   public PhysicsDropType(String itemId, String dropListId) {
      this.itemId = itemId;
      this.dropListId = dropListId;
   }

   protected PhysicsDropType() {
   }

   public String getItemId() {
      return this.itemId;
   }

   public String getDropListId() {
      return this.dropListId;
   }

   @Nullable
   public PhysicsDropType withoutDrops() {
      return null;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PhysicsDropType{itemId=" + this.itemId + ", dropListId='" + this.dropListId + "'}";
   }
}
