package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.BlockBreaking;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class BlockBreakingDropType implements NetworkSerializable<BlockBreaking> {
   public static final BuilderCodec<BlockBreakingDropType> CODEC = BuilderCodec.builder(BlockBreakingDropType.class, BlockBreakingDropType::new)
      .append(new KeyedCodec<>("GatherType", Codec.STRING), (blockBreaking, s) -> blockBreaking.gatherType = s, blockBreaking -> blockBreaking.gatherType)
      .add()
      .append(new KeyedCodec<>("Quality", Codec.INTEGER), (blockBreaking, s) -> blockBreaking.quality = s, blockBreaking -> blockBreaking.quality)
      .add()
      .<String>append(new KeyedCodec<>("ItemId", Codec.STRING), (blockBreaking, s) -> blockBreaking.itemId = s, blockBreaking -> blockBreaking.itemId)
      .addValidatorLate(() -> Item.VALIDATOR_CACHE.getValidator().late())
      .add()
      .append(new KeyedCodec<>("Quantity", Codec.INTEGER), (blockBreaking, s) -> blockBreaking.quantity = s, blockBreaking -> blockBreaking.quantity)
      .add()
      .<String>append(
         new KeyedCodec<>("DropList", new ContainedAssetCodec<>(ItemDropList.class, ItemDropList.CODEC)),
         (blockBreaking, s) -> blockBreaking.dropListId = s,
         blockBreaking -> blockBreaking.dropListId
      )
      .addValidatorLate(() -> ItemDropList.VALIDATOR_CACHE.getValidator().late())
      .add()
      .build();
   protected String gatherType;
   protected int quality;
   protected String itemId;
   protected String dropListId;
   protected int quantity = 1;

   public BlockBreakingDropType(String gatherType, int quality, int quantity, String itemId, String dropListId) {
      this.gatherType = gatherType;
      this.quality = quality;
      this.quantity = quantity;
      this.itemId = itemId;
      this.dropListId = dropListId;
   }

   protected BlockBreakingDropType() {
   }

   @Nonnull
   public BlockBreaking toPacket() {
      BlockBreaking packet = new BlockBreaking();
      packet.gatherType = this.gatherType;
      packet.quality = this.quality;
      packet.quantity = this.quantity;
      if (this.itemId != null) {
         packet.itemId = this.itemId.toString();
      }

      packet.dropListId = this.dropListId;
      return packet;
   }

   public String getGatherType() {
      return this.gatherType;
   }

   public int getQuality() {
      return this.quality;
   }

   public int getQuantity() {
      return this.quantity;
   }

   public String getItemId() {
      return this.itemId;
   }

   public String getDropListId() {
      return this.dropListId;
   }

   @Nonnull
   public BlockBreakingDropType withoutDrops() {
      return new BlockBreakingDropType(this.gatherType, 0, 0, null, null);
   }

   @Nonnull
   @Override
   public String toString() {
      return "BlockBreakingDropType{gatherType='"
         + this.gatherType
         + "', quality="
         + this.quality
         + ", quantity="
         + this.quantity
         + ", itemId="
         + this.itemId
         + ", dropListId='"
         + this.dropListId
         + "'}";
   }
}
