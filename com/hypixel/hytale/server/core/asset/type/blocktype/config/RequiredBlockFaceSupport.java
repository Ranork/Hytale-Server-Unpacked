package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SupportMatch;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class RequiredBlockFaceSupport implements NetworkSerializable<com.hypixel.hytale.protocol.RequiredBlockFaceSupport> {
   public static final BuilderCodec<RequiredBlockFaceSupport> CODEC = BuilderCodec.builder(RequiredBlockFaceSupport.class, RequiredBlockFaceSupport::new)
      .append(new KeyedCodec<>("FaceType", Codec.STRING), (blockFaceSupport, o) -> blockFaceSupport.faceType = o, blockFaceSupport -> blockFaceSupport.faceType)
      .documentation("Can be any string. Compared with FaceType in \"Supporting\" of other blocks. A LOT of blocks use 'Full'.")
      .add()
      .addField(
         new KeyedCodec<>("SelfFaceType", Codec.STRING),
         (blockFaceSupport, o) -> blockFaceSupport.selfFaceType = o,
         blockFaceSupport -> blockFaceSupport.selfFaceType
      )
      .addField(
         new KeyedCodec<>("BlockSetId", Codec.STRING),
         (blockFaceSupport, o) -> blockFaceSupport.blockSetId = o,
         blockFaceSupport -> blockFaceSupport.blockSetId
      )
      .addField(
         new KeyedCodec<>("BlockTypeId", Codec.STRING),
         (blockFaceSupport, o) -> blockFaceSupport.blockTypeId = o,
         blockFaceSupport -> blockFaceSupport.blockTypeId
      )
      .addField(new KeyedCodec<>("FluidId", Codec.STRING), (blockFaceSupport, o) -> blockFaceSupport.fluidId = o, blockFaceSupport -> blockFaceSupport.fluidId)
      .addField(
         new KeyedCodec<>("MatchSelf", RequiredBlockFaceSupport.Match.CODEC),
         (requiredBlockFaceSupport, b) -> requiredBlockFaceSupport.matchSelf = b,
         requiredBlockFaceSupport -> requiredBlockFaceSupport.matchSelf
      )
      .addField(
         new KeyedCodec<>("Support", RequiredBlockFaceSupport.Match.CODEC),
         (requiredBlockFaceSupport, b) -> requiredBlockFaceSupport.support = b,
         requiredBlockFaceSupport -> requiredBlockFaceSupport.support
      )
      .addField(
         new KeyedCodec<>("AllowSupportPropagation", Codec.BOOLEAN),
         (requiredBlockFaceSupport, b) -> requiredBlockFaceSupport.allowSupportPropagation = b,
         requiredBlockFaceSupport -> requiredBlockFaceSupport.allowSupportPropagation
      )
      .addField(
         new KeyedCodec<>("Rotate", Codec.BOOLEAN),
         (requiredBlockFaceSupport, b) -> requiredBlockFaceSupport.rotate = b,
         requiredBlockFaceSupport -> requiredBlockFaceSupport.rotate
      )
      .addField(
         new KeyedCodec<>("Filler", new ArrayCodec<>(Vector3i.CODEC, Vector3i[]::new)),
         (blockFaceSupport, o) -> blockFaceSupport.filler = o,
         blockFaceSupport -> blockFaceSupport.filler
      )
      .append(
         new KeyedCodec<>("TagId", Codec.STRING),
         (requiredBlockFaceSupport, s) -> requiredBlockFaceSupport.tagId = s,
         requiredBlockFaceSupport -> requiredBlockFaceSupport.tagId
      )
      .add()
      .afterDecode(blockFaceSupport -> {
         if (blockFaceSupport.blockSetId != null) {
            int index = BlockSet.getAssetMap().getIndex(blockFaceSupport.blockSetId);
            if (index == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + blockFaceSupport.blockSetId);
            }

            blockFaceSupport.blockSetIndex = index;
         }

         if (blockFaceSupport.tagId != null) {
            blockFaceSupport.tagIndex = AssetRegistry.getOrCreateTagIndex(blockFaceSupport.tagId);
         }
      })
      .build();
   protected String faceType;
   protected String selfFaceType;
   protected String blockSetId;
   protected int blockSetIndex = Integer.MIN_VALUE;
   protected String blockTypeId;
   protected String fluidId;
   protected RequiredBlockFaceSupport.Match matchSelf = RequiredBlockFaceSupport.Match.IGNORED;
   protected String tagId;
   protected int tagIndex = Integer.MIN_VALUE;
   protected RequiredBlockFaceSupport.Match support = RequiredBlockFaceSupport.Match.REQUIRED;
   protected boolean allowSupportPropagation = true;
   protected boolean rotate = true;
   protected Vector3i[] filler;

   public RequiredBlockFaceSupport() {
   }

   public RequiredBlockFaceSupport(String faceType) {
      this.faceType = faceType;
   }

   public RequiredBlockFaceSupport(
      String faceType,
      String selfFaceType,
      String blockSetId,
      String blockTypeId,
      String fluidId,
      RequiredBlockFaceSupport.Match matchSelf,
      RequiredBlockFaceSupport.Match support,
      boolean allowSupportPropagation,
      boolean rotate,
      Vector3i[] filler,
      String tagId,
      int tagIndex
   ) {
      this.faceType = faceType;
      this.selfFaceType = selfFaceType;
      this.blockSetId = blockSetId;
      this.blockTypeId = blockTypeId;
      this.fluidId = fluidId;
      this.matchSelf = matchSelf;
      this.support = support;
      this.allowSupportPropagation = allowSupportPropagation;
      this.rotate = rotate;
      this.filler = filler;
      this.tagId = tagId;
      this.tagIndex = tagIndex;
   }

   public String getFaceType() {
      return this.faceType;
   }

   public String getSelfFaceType() {
      return this.selfFaceType;
   }

   public String getBlockSetId() {
      return this.blockSetId;
   }

   public int getBlockSetIndex() {
      return this.blockSetIndex;
   }

   public String getBlockTypeId() {
      return this.blockTypeId;
   }

   public String getFluidId() {
      return this.fluidId;
   }

   public RequiredBlockFaceSupport.Match getMatchSelf() {
      return this.matchSelf;
   }

   public RequiredBlockFaceSupport.Match getSupport() {
      return this.support;
   }

   public boolean allowsSupportPropagation() {
      return this.allowSupportPropagation;
   }

   public boolean isRotated() {
      return this.rotate;
   }

   public Vector3i[] getFiller() {
      return this.filler;
   }

   public boolean isAppliedToFiller(Vector3i filler) {
      return this.filler == null || ArrayUtil.contains(this.filler, filler);
   }

   public String getTagId() {
      return this.tagId;
   }

   public int getTagIndex() {
      return this.tagIndex;
   }

   @Nonnull
   @Override
   public String toString() {
      return "RequiredBlockFaceSupport{faceType='"
         + this.faceType
         + "', selfFaceType='"
         + this.selfFaceType
         + "', blockSetId='"
         + this.blockSetId
         + "', blockTypeId="
         + this.blockTypeId
         + ", fluidId="
         + this.fluidId
         + ", matchSelf="
         + this.matchSelf
         + ", support="
         + this.support
         + ", allowSupportPropagation="
         + this.allowSupportPropagation
         + ", rotate="
         + this.rotate
         + ", filler="
         + Arrays.toString((Object[])this.filler)
         + ", tagId="
         + this.tagId
         + "}";
   }

   @Nonnull
   public static RequiredBlockFaceSupport rotate(
      @Nonnull RequiredBlockFaceSupport original, @Nonnull Rotation rotationYaw, @Nonnull Rotation rotationPitch, @Nonnull Rotation roll
   ) {
      Vector3i[] rotatedFiller = ArrayUtil.copyAndMutate(original.filler, vector -> Rotation.rotate(vector, rotationYaw, rotationPitch, roll), Vector3i[]::new);
      return new RequiredBlockFaceSupport(
         original.faceType,
         original.selfFaceType,
         original.blockSetId,
         original.blockTypeId,
         original.fluidId,
         original.matchSelf,
         original.support,
         original.allowSupportPropagation,
         original.rotate,
         rotatedFiller,
         original.tagId,
         original.tagIndex
      );
   }

   @Nonnull
   public com.hypixel.hytale.protocol.RequiredBlockFaceSupport toPacket() {
      com.hypixel.hytale.protocol.RequiredBlockFaceSupport packet = new com.hypixel.hytale.protocol.RequiredBlockFaceSupport();
      packet.faceType = this.faceType;
      packet.selfFaceType = this.selfFaceType;
      packet.tagIndex = this.tagIndex;
      packet.blockTypeId = this.blockTypeId == null ? -1 : BlockType.getAssetMap().getIndex(this.blockTypeId);
      if (packet.blockTypeId == Integer.MIN_VALUE) {
         throw new IllegalArgumentException("Unknown key! " + this.blockTypeId);
      } else {
         packet.fluidId = this.fluidId == null ? -1 : Fluid.getAssetMap().getIndex(this.fluidId);
         if (packet.fluidId == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + this.fluidId);
         } else {
            packet.allowSupportPropagation = this.allowSupportPropagation;
            packet.rotate = this.rotate;
            packet.blockSetId = this.blockSetId;

            packet.support = switch (this.support) {
               case IGNORED -> SupportMatch.Ignored;
               case REQUIRED -> SupportMatch.Required;
               case DISALLOWED -> SupportMatch.Disallowed;
            };

            packet.matchSelf = switch (this.matchSelf) {
               case IGNORED -> SupportMatch.Ignored;
               case REQUIRED -> SupportMatch.Required;
               case DISALLOWED -> SupportMatch.Disallowed;
            };
            if (this.filler != null) {
               com.hypixel.hytale.protocol.Vector3i[] filler = new com.hypixel.hytale.protocol.Vector3i[this.filler.length];

               for (int j = 0; j < this.filler.length; j++) {
                  Vector3i fillerVector = this.filler[j];
                  filler[j] = new com.hypixel.hytale.protocol.Vector3i(fillerVector.x, fillerVector.y, fillerVector.z);
               }

               packet.filler = filler;
            }

            return packet;
         }
      }
   }

   public static enum Match {
      IGNORED,
      REQUIRED,
      DISALLOWED;

      public static final EnumCodec<RequiredBlockFaceSupport.Match> CODEC = new EnumCodec<>(RequiredBlockFaceSupport.Match.class);
   }
}
