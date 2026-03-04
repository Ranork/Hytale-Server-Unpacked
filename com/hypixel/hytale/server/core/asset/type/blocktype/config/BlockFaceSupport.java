package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class BlockFaceSupport implements NetworkSerializable<com.hypixel.hytale.protocol.BlockFaceSupport> {
   public static final BuilderCodec<BlockFaceSupport> CODEC = BuilderCodec.builder(BlockFaceSupport.class, BlockFaceSupport::new)
      .append(new KeyedCodec<>("FaceType", Codec.STRING), (blockFaceSupport, s) -> blockFaceSupport.faceType = s, blockFaceSupport -> blockFaceSupport.faceType)
      .add()
      .documentation("Can be any string. Compared with FaceType in \"Support\". A LOT of blocks use 'Full'.")
      .append(
         new KeyedCodec<>("Filler", new ArrayCodec<>(Vector3i.CODEC, Vector3i[]::new)),
         (blockFaceSupport, o) -> blockFaceSupport.filler = o,
         blockFaceSupport -> blockFaceSupport.filler
      )
      .add()
      .build();
   public static final BlockFaceSupport ALL = new BlockFaceSupport();
   public static final String FULL_SUPPORTING_FACE = "Full";
   protected String faceType = "Full";
   protected Vector3i[] filler;

   public BlockFaceSupport() {
   }

   public BlockFaceSupport(String faceType, Vector3i[] filler) {
      this.faceType = faceType;
      this.filler = filler;
   }

   public String getFaceType() {
      return this.faceType;
   }

   public Vector3i[] getFiller() {
      return this.filler;
   }

   public boolean providesSupportFromFiller(Vector3i filler) {
      return this.filler == null || ArrayUtil.contains(this.filler, filler);
   }

   @Nonnull
   @Override
   public String toString() {
      return "BlockFaceSupport{faceType='" + this.faceType + "', filler=" + Arrays.toString((Object[])this.filler) + "}";
   }

   @Nonnull
   public static BlockFaceSupport rotate(
      @Nonnull BlockFaceSupport original, @Nonnull Rotation rotationYaw, @Nonnull Rotation rotationPitch, @Nonnull Rotation roll
   ) {
      if (original == ALL) {
         return ALL;
      } else {
         Vector3i[] rotatedFiller = ArrayUtil.copyAndMutate(
            original.filler, vector -> Rotation.rotate(vector, rotationYaw, rotationPitch, roll), Vector3i[]::new
         );
         return new BlockFaceSupport(original.faceType, rotatedFiller);
      }
   }

   @Nonnull
   public com.hypixel.hytale.protocol.BlockFaceSupport toPacket() {
      com.hypixel.hytale.protocol.BlockFaceSupport protocolBlockFaceSupport = new com.hypixel.hytale.protocol.BlockFaceSupport();
      protocolBlockFaceSupport.faceType = this.faceType;
      if (this.filler != null) {
         com.hypixel.hytale.protocol.Vector3i[] filler = new com.hypixel.hytale.protocol.Vector3i[this.filler.length];

         for (int j = 0; j < this.filler.length; j++) {
            Vector3i fillerVector = this.filler[j];
            filler[j] = new com.hypixel.hytale.protocol.Vector3i(fillerVector.x, fillerVector.y, fillerVector.z);
         }

         protocolBlockFaceSupport.filler = filler;
      }

      return protocolBlockFaceSupport;
   }
}
