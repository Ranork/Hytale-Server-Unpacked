package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.BlockTextures;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import javax.annotation.Nonnull;

public class BlockTypeTextures {
   public static final BuilderCodec<BlockTypeTextures> CODEC = BuilderCodec.builder(BlockTypeTextures.class, BlockTypeTextures::new)
      .append(new KeyedCodec<>("All", Codec.STRING), (blockType, o) -> {
         blockType.up = o;
         blockType.down = o;
         blockType.north = o;
         blockType.south = o;
         blockType.west = o;
         blockType.east = o;
      }, blockType -> null)
      .addValidator(CommonAssetValidator.TEXTURE_ITEM)
      .add()
      .<String>append(new KeyedCodec<>("Sides", Codec.STRING), (blockType, o) -> {
         blockType.north = o;
         blockType.south = o;
         blockType.west = o;
         blockType.east = o;
      }, blockType -> null)
      .addValidator(CommonAssetValidator.TEXTURE_ITEM)
      .add()
      .<String>append(new KeyedCodec<>("UpDown", Codec.STRING), (blockType, o) -> {
         blockType.up = o;
         blockType.down = o;
      }, blockType -> null)
      .addValidator(CommonAssetValidator.TEXTURE_ITEM)
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("Up", Codec.STRING), (blockType, o) -> blockType.up = o, blockType -> blockType.up, (blockType, parent) -> blockType.up = parent.up
      )
      .addValidator(CommonAssetValidator.TEXTURE_ITEM)
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("Down", Codec.STRING),
         (blockType, o) -> blockType.down = o,
         blockType -> blockType.down,
         (blockType, parent) -> blockType.down = parent.down
      )
      .addValidator(CommonAssetValidator.TEXTURE_ITEM)
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("North", Codec.STRING),
         (blockType, o) -> blockType.north = o,
         blockType -> blockType.north,
         (blockType, parent) -> blockType.north = parent.north
      )
      .addValidator(CommonAssetValidator.TEXTURE_ITEM)
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("South", Codec.STRING),
         (blockType, o) -> blockType.south = o,
         blockType -> blockType.south,
         (blockType, parent) -> blockType.south = parent.south
      )
      .addValidator(CommonAssetValidator.TEXTURE_ITEM)
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("West", Codec.STRING),
         (blockType, o) -> blockType.west = o,
         blockType -> blockType.west,
         (blockType, parent) -> blockType.west = parent.west
      )
      .addValidator(CommonAssetValidator.TEXTURE_ITEM)
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("East", Codec.STRING),
         (blockType, o) -> blockType.east = o,
         blockType -> blockType.east,
         (blockType, parent) -> blockType.east = parent.east
      )
      .addValidator(CommonAssetValidator.TEXTURE_ITEM)
      .add()
      .appendInherited(
         new KeyedCodec<>("Weight", Codec.INTEGER),
         (blockType, o) -> blockType.weight = o,
         blockType -> blockType.weight,
         (blockType, parent) -> blockType.weight = parent.weight
      )
      .add()
      .build();
   protected String up = "BlockTextures/Unknown.png";
   protected String down = "BlockTextures/Unknown.png";
   protected String north = "BlockTextures/Unknown.png";
   protected String south = "BlockTextures/Unknown.png";
   protected String east = "BlockTextures/Unknown.png";
   protected String west = "BlockTextures/Unknown.png";
   protected int weight = 1;

   public BlockTypeTextures() {
   }

   public BlockTypeTextures(String all) {
      this.up = all;
      this.down = all;
      this.north = all;
      this.south = all;
      this.east = all;
      this.west = all;
   }

   public BlockTypeTextures(String up, String down, String north, String south, String east, String west, int weight) {
      this.up = up;
      this.down = down;
      this.north = north;
      this.south = south;
      this.east = east;
      this.west = west;
      this.weight = weight;
   }

   public String getUp() {
      return this.up;
   }

   public String getDown() {
      return this.down;
   }

   public String getNorth() {
      return this.north;
   }

   public String getSouth() {
      return this.south;
   }

   public String getEast() {
      return this.east;
   }

   public String getWest() {
      return this.west;
   }

   public float getWeight() {
      return this.weight;
   }

   @Nonnull
   public BlockTextures toPacket(float totalWeight) {
      BlockTextures packet = new BlockTextures();
      packet.top = this.up;
      packet.bottom = this.down;
      packet.front = this.south;
      packet.back = this.north;
      packet.left = this.west;
      packet.right = this.east;
      packet.weight = this.weight / totalWeight;
      return packet;
   }
}
