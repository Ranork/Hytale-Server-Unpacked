package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.SoftBlock;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class SoftBlockDropType implements NetworkSerializable<SoftBlock> {
   public static final BuilderCodec<SoftBlockDropType> CODEC = BuilderCodec.builder(SoftBlockDropType.class, SoftBlockDropType::new)
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
      .addField(
         new KeyedCodec<>("IsWeaponBreakable", Codec.BOOLEAN),
         (blockGathering, o) -> blockGathering.isWeaponBreakable = o,
         blockGathering -> blockGathering.isWeaponBreakable
      )
      .build();
   protected String itemId;
   protected String dropListId;
   protected boolean isWeaponBreakable = true;

   public SoftBlockDropType(String itemId, String dropListId, boolean isWeaponBreakable) {
      this.itemId = itemId;
      this.dropListId = dropListId;
      this.isWeaponBreakable = isWeaponBreakable;
   }

   protected SoftBlockDropType() {
   }

   @Nonnull
   public SoftBlock toPacket() {
      SoftBlock packet = new SoftBlock();
      if (this.itemId != null) {
         packet.itemId = this.itemId.toString();
      }

      packet.dropListId = this.dropListId;
      packet.isWeaponBreakable = this.isWeaponBreakable;
      return packet;
   }

   public String getItemId() {
      return this.itemId;
   }

   public String getDropListId() {
      return this.dropListId;
   }

   public boolean isWeaponBreakable() {
      return this.isWeaponBreakable;
   }

   @Nonnull
   public SoftBlockDropType withoutDrops() {
      return new SoftBlockDropType(null, null, this.isWeaponBreakable);
   }

   @Nonnull
   @Override
   public String toString() {
      return "SoftBlockDropType{itemId=" + this.itemId + ", dropListId='" + this.dropListId + "', isWeaponBreakable='" + this.isWeaponBreakable + "'}";
   }
}
