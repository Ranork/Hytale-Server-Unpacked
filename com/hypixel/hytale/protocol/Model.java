package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Model {
   public static final int NULLABLE_BIT_FIELD_SIZE = 2;
   public static final int FIXED_BLOCK_SIZE = 51;
   public static final int VARIABLE_FIELD_COUNT = 12;
   public static final int VARIABLE_BLOCK_START = 99;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String assetId;
   @Nullable
   public String path;
   @Nullable
   public String texture;
   @Nullable
   public String gradientSet;
   @Nullable
   public String gradientId;
   @Nullable
   public CameraSettings camera;
   public float scale;
   public float eyeHeight;
   public float crouchOffset;
   public float sittingOffset;
   public float sleepingOffset;
   @Nullable
   public Map<String, AnimationSet> animationSets;
   @Nullable
   public ModelAttachment[] attachments;
   @Nullable
   public Hitbox hitbox;
   @Nullable
   public ModelParticle[] particles;
   @Nullable
   public ModelTrail[] trails;
   @Nullable
   public ColorLight light;
   @Nullable
   public Map<String, DetailBox[]> detailBoxes;
   @Nonnull
   public Phobia phobia = Phobia.None;
   @Nullable
   public Model phobiaModel;

   public Model() {
   }

   public Model(
      @Nullable String assetId,
      @Nullable String path,
      @Nullable String texture,
      @Nullable String gradientSet,
      @Nullable String gradientId,
      @Nullable CameraSettings camera,
      float scale,
      float eyeHeight,
      float crouchOffset,
      float sittingOffset,
      float sleepingOffset,
      @Nullable Map<String, AnimationSet> animationSets,
      @Nullable ModelAttachment[] attachments,
      @Nullable Hitbox hitbox,
      @Nullable ModelParticle[] particles,
      @Nullable ModelTrail[] trails,
      @Nullable ColorLight light,
      @Nullable Map<String, DetailBox[]> detailBoxes,
      @Nonnull Phobia phobia,
      @Nullable Model phobiaModel
   ) {
      this.assetId = assetId;
      this.path = path;
      this.texture = texture;
      this.gradientSet = gradientSet;
      this.gradientId = gradientId;
      this.camera = camera;
      this.scale = scale;
      this.eyeHeight = eyeHeight;
      this.crouchOffset = crouchOffset;
      this.sittingOffset = sittingOffset;
      this.sleepingOffset = sleepingOffset;
      this.animationSets = animationSets;
      this.attachments = attachments;
      this.hitbox = hitbox;
      this.particles = particles;
      this.trails = trails;
      this.light = light;
      this.detailBoxes = detailBoxes;
      this.phobia = phobia;
      this.phobiaModel = phobiaModel;
   }

   public Model(@Nonnull Model other) {
      this.assetId = other.assetId;
      this.path = other.path;
      this.texture = other.texture;
      this.gradientSet = other.gradientSet;
      this.gradientId = other.gradientId;
      this.camera = other.camera;
      this.scale = other.scale;
      this.eyeHeight = other.eyeHeight;
      this.crouchOffset = other.crouchOffset;
      this.sittingOffset = other.sittingOffset;
      this.sleepingOffset = other.sleepingOffset;
      this.animationSets = other.animationSets;
      this.attachments = other.attachments;
      this.hitbox = other.hitbox;
      this.particles = other.particles;
      this.trails = other.trails;
      this.light = other.light;
      this.detailBoxes = other.detailBoxes;
      this.phobia = other.phobia;
      this.phobiaModel = other.phobiaModel;
   }

   @Nonnull
   public static Model deserialize(@Nonnull ByteBuf buf, int offset) {
      Model obj = new Model();
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      obj.scale = buf.getFloatLE(offset + 2);
      obj.eyeHeight = buf.getFloatLE(offset + 6);
      obj.crouchOffset = buf.getFloatLE(offset + 10);
      obj.sittingOffset = buf.getFloatLE(offset + 14);
      obj.sleepingOffset = buf.getFloatLE(offset + 18);
      if ((nullBits[0] & 1) != 0) {
         obj.hitbox = Hitbox.deserialize(buf, offset + 22);
      }

      if ((nullBits[0] & 2) != 0) {
         obj.light = ColorLight.deserialize(buf, offset + 46);
      }

      obj.phobia = Phobia.fromValue(buf.getByte(offset + 50));
      if ((nullBits[0] & 4) != 0) {
         int varPos0 = offset + 99 + buf.getIntLE(offset + 51);
         int assetIdLen = VarInt.peek(buf, varPos0);
         if (assetIdLen < 0) {
            throw ProtocolException.negativeLength("AssetId", assetIdLen);
         }

         if (assetIdLen > 4096000) {
            throw ProtocolException.stringTooLong("AssetId", assetIdLen, 4096000);
         }

         obj.assetId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits[0] & 8) != 0) {
         int varPos1 = offset + 99 + buf.getIntLE(offset + 55);
         int pathLen = VarInt.peek(buf, varPos1);
         if (pathLen < 0) {
            throw ProtocolException.negativeLength("Path", pathLen);
         }

         if (pathLen > 4096000) {
            throw ProtocolException.stringTooLong("Path", pathLen, 4096000);
         }

         obj.path = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits[0] & 16) != 0) {
         int varPos2 = offset + 99 + buf.getIntLE(offset + 59);
         int textureLen = VarInt.peek(buf, varPos2);
         if (textureLen < 0) {
            throw ProtocolException.negativeLength("Texture", textureLen);
         }

         if (textureLen > 4096000) {
            throw ProtocolException.stringTooLong("Texture", textureLen, 4096000);
         }

         obj.texture = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      if ((nullBits[0] & 32) != 0) {
         int varPos3 = offset + 99 + buf.getIntLE(offset + 63);
         int gradientSetLen = VarInt.peek(buf, varPos3);
         if (gradientSetLen < 0) {
            throw ProtocolException.negativeLength("GradientSet", gradientSetLen);
         }

         if (gradientSetLen > 4096000) {
            throw ProtocolException.stringTooLong("GradientSet", gradientSetLen, 4096000);
         }

         obj.gradientSet = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
      }

      if ((nullBits[0] & 64) != 0) {
         int varPos4 = offset + 99 + buf.getIntLE(offset + 67);
         int gradientIdLen = VarInt.peek(buf, varPos4);
         if (gradientIdLen < 0) {
            throw ProtocolException.negativeLength("GradientId", gradientIdLen);
         }

         if (gradientIdLen > 4096000) {
            throw ProtocolException.stringTooLong("GradientId", gradientIdLen, 4096000);
         }

         obj.gradientId = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
      }

      if ((nullBits[0] & 128) != 0) {
         int varPos5 = offset + 99 + buf.getIntLE(offset + 71);
         obj.camera = CameraSettings.deserialize(buf, varPos5);
      }

      if ((nullBits[1] & 1) != 0) {
         int varPos6 = offset + 99 + buf.getIntLE(offset + 75);
         int animationSetsCount = VarInt.peek(buf, varPos6);
         if (animationSetsCount < 0) {
            throw ProtocolException.negativeLength("AnimationSets", animationSetsCount);
         }

         if (animationSetsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("AnimationSets", animationSetsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos6);
         obj.animationSets = new HashMap<>(animationSetsCount);
         int dictPos = varPos6 + varIntLen;

         for (int i = 0; i < animationSetsCount; i++) {
            int keyLen = VarInt.peek(buf, dictPos);
            if (keyLen < 0) {
               throw ProtocolException.negativeLength("key", keyLen);
            }

            if (keyLen > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLen, 4096000);
            }

            int keyVarLen = VarInt.length(buf, dictPos);
            String key = PacketIO.readVarString(buf, dictPos);
            dictPos += keyVarLen + keyLen;
            AnimationSet val = AnimationSet.deserialize(buf, dictPos);
            dictPos += AnimationSet.computeBytesConsumed(buf, dictPos);
            if (obj.animationSets.put(key, val) != null) {
               throw ProtocolException.duplicateKey("animationSets", key);
            }
         }
      }

      if ((nullBits[1] & 2) != 0) {
         int varPos7 = offset + 99 + buf.getIntLE(offset + 79);
         int attachmentsCount = VarInt.peek(buf, varPos7);
         if (attachmentsCount < 0) {
            throw ProtocolException.negativeLength("Attachments", attachmentsCount);
         }

         if (attachmentsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Attachments", attachmentsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos7);
         if (varPos7 + varIntLen + attachmentsCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Attachments", varPos7 + varIntLen + attachmentsCount * 1, buf.readableBytes());
         }

         obj.attachments = new ModelAttachment[attachmentsCount];
         int elemPos = varPos7 + varIntLen;

         for (int i = 0; i < attachmentsCount; i++) {
            obj.attachments[i] = ModelAttachment.deserialize(buf, elemPos);
            elemPos += ModelAttachment.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits[1] & 4) != 0) {
         int varPos8 = offset + 99 + buf.getIntLE(offset + 83);
         int particlesCount = VarInt.peek(buf, varPos8);
         if (particlesCount < 0) {
            throw ProtocolException.negativeLength("Particles", particlesCount);
         }

         if (particlesCount > 4096000) {
            throw ProtocolException.arrayTooLong("Particles", particlesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos8);
         if (varPos8 + varIntLen + particlesCount * 34L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Particles", varPos8 + varIntLen + particlesCount * 34, buf.readableBytes());
         }

         obj.particles = new ModelParticle[particlesCount];
         int elemPos = varPos8 + varIntLen;

         for (int i = 0; i < particlesCount; i++) {
            obj.particles[i] = ModelParticle.deserialize(buf, elemPos);
            elemPos += ModelParticle.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits[1] & 8) != 0) {
         int varPos9 = offset + 99 + buf.getIntLE(offset + 87);
         int trailsCount = VarInt.peek(buf, varPos9);
         if (trailsCount < 0) {
            throw ProtocolException.negativeLength("Trails", trailsCount);
         }

         if (trailsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Trails", trailsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos9);
         if (varPos9 + varIntLen + trailsCount * 27L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Trails", varPos9 + varIntLen + trailsCount * 27, buf.readableBytes());
         }

         obj.trails = new ModelTrail[trailsCount];
         int elemPos = varPos9 + varIntLen;

         for (int i = 0; i < trailsCount; i++) {
            obj.trails[i] = ModelTrail.deserialize(buf, elemPos);
            elemPos += ModelTrail.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits[1] & 16) != 0) {
         int varPos10 = offset + 99 + buf.getIntLE(offset + 91);
         int detailBoxesCount = VarInt.peek(buf, varPos10);
         if (detailBoxesCount < 0) {
            throw ProtocolException.negativeLength("DetailBoxes", detailBoxesCount);
         }

         if (detailBoxesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DetailBoxes", detailBoxesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos10);
         obj.detailBoxes = new HashMap<>(detailBoxesCount);
         int dictPos = varPos10 + varIntLen;

         for (int i = 0; i < detailBoxesCount; i++) {
            int keyLenx = VarInt.peek(buf, dictPos);
            if (keyLenx < 0) {
               throw ProtocolException.negativeLength("key", keyLenx);
            }

            if (keyLenx > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLenx, 4096000);
            }

            int keyVarLen = VarInt.length(buf, dictPos);
            String key = PacketIO.readVarString(buf, dictPos);
            dictPos += keyVarLen + keyLenx;
            int valLen = VarInt.peek(buf, dictPos);
            if (valLen < 0) {
               throw ProtocolException.negativeLength("val", valLen);
            }

            if (valLen > 64) {
               throw ProtocolException.arrayTooLong("val", valLen, 64);
            }

            int valVarLen = VarInt.length(buf, dictPos);
            if (dictPos + valVarLen + valLen * 37L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("val", dictPos + valVarLen + valLen * 37, buf.readableBytes());
            }

            dictPos += valVarLen;
            DetailBox[] val = new DetailBox[valLen];

            for (int valIdx = 0; valIdx < valLen; valIdx++) {
               val[valIdx] = DetailBox.deserialize(buf, dictPos);
               dictPos += DetailBox.computeBytesConsumed(buf, dictPos);
            }

            if (obj.detailBoxes.put(key, val) != null) {
               throw ProtocolException.duplicateKey("detailBoxes", key);
            }
         }
      }

      if ((nullBits[1] & 32) != 0) {
         int varPos11 = offset + 99 + buf.getIntLE(offset + 95);
         obj.phobiaModel = deserialize(buf, varPos11);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      int maxEnd = 99;
      if ((nullBits[0] & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 51);
         int pos0 = offset + 99 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[0] & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 55);
         int pos1 = offset + 99 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[0] & 16) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 59);
         int pos2 = offset + 99 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[0] & 32) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 63);
         int pos3 = offset + 99 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[0] & 64) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 67);
         int pos4 = offset + 99 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.length(buf, pos4) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 71);
         int pos5 = offset + 99 + fieldOffset5;
         pos5 += CameraSettings.computeBytesConsumed(buf, pos5);
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 75);
         int pos6 = offset + 99 + fieldOffset6;
         int dictLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.length(buf, pos6);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos6);
            pos6 += VarInt.length(buf, pos6) + sl;
            pos6 += AnimationSet.computeBytesConsumed(buf, pos6);
         }

         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[1] & 2) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 79);
         int pos7 = offset + 99 + fieldOffset7;
         int arrLen = VarInt.peek(buf, pos7);
         pos7 += VarInt.length(buf, pos7);

         for (int i = 0; i < arrLen; i++) {
            pos7 += ModelAttachment.computeBytesConsumed(buf, pos7);
         }

         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      if ((nullBits[1] & 4) != 0) {
         int fieldOffset8 = buf.getIntLE(offset + 83);
         int pos8 = offset + 99 + fieldOffset8;
         int arrLen = VarInt.peek(buf, pos8);
         pos8 += VarInt.length(buf, pos8);

         for (int i = 0; i < arrLen; i++) {
            pos8 += ModelParticle.computeBytesConsumed(buf, pos8);
         }

         if (pos8 - offset > maxEnd) {
            maxEnd = pos8 - offset;
         }
      }

      if ((nullBits[1] & 8) != 0) {
         int fieldOffset9 = buf.getIntLE(offset + 87);
         int pos9 = offset + 99 + fieldOffset9;
         int arrLen = VarInt.peek(buf, pos9);
         pos9 += VarInt.length(buf, pos9);

         for (int i = 0; i < arrLen; i++) {
            pos9 += ModelTrail.computeBytesConsumed(buf, pos9);
         }

         if (pos9 - offset > maxEnd) {
            maxEnd = pos9 - offset;
         }
      }

      if ((nullBits[1] & 16) != 0) {
         int fieldOffset10 = buf.getIntLE(offset + 91);
         int pos10 = offset + 99 + fieldOffset10;
         int dictLen = VarInt.peek(buf, pos10);
         pos10 += VarInt.length(buf, pos10);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos10);
            pos10 += VarInt.length(buf, pos10) + sl;
            sl = VarInt.peek(buf, pos10);
            pos10 += VarInt.length(buf, pos10);

            for (int j = 0; j < sl; j++) {
               pos10 += DetailBox.computeBytesConsumed(buf, pos10);
            }
         }

         if (pos10 - offset > maxEnd) {
            maxEnd = pos10 - offset;
         }
      }

      if ((nullBits[1] & 32) != 0) {
         int fieldOffset11 = buf.getIntLE(offset + 95);
         int pos11 = offset + 99 + fieldOffset11;
         pos11 += computeBytesConsumed(buf, pos11);
         if (pos11 - offset > maxEnd) {
            maxEnd = pos11 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[2];
      if (this.hitbox != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.light != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.assetId != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.path != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.texture != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.gradientSet != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.gradientId != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.camera != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.animationSets != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      if (this.attachments != null) {
         nullBits[1] = (byte)(nullBits[1] | 2);
      }

      if (this.particles != null) {
         nullBits[1] = (byte)(nullBits[1] | 4);
      }

      if (this.trails != null) {
         nullBits[1] = (byte)(nullBits[1] | 8);
      }

      if (this.detailBoxes != null) {
         nullBits[1] = (byte)(nullBits[1] | 16);
      }

      if (this.phobiaModel != null) {
         nullBits[1] = (byte)(nullBits[1] | 32);
      }

      buf.writeBytes(nullBits);
      buf.writeFloatLE(this.scale);
      buf.writeFloatLE(this.eyeHeight);
      buf.writeFloatLE(this.crouchOffset);
      buf.writeFloatLE(this.sittingOffset);
      buf.writeFloatLE(this.sleepingOffset);
      if (this.hitbox != null) {
         this.hitbox.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      if (this.light != null) {
         this.light.serialize(buf);
      } else {
         buf.writeZero(4);
      }

      buf.writeByte(this.phobia.getValue());
      int assetIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int pathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int textureOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int gradientSetOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int gradientIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int cameraOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int animationSetsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int attachmentsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int particlesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int trailsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int detailBoxesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int phobiaModelOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.assetId != null) {
         buf.setIntLE(assetIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.assetId, 4096000);
      } else {
         buf.setIntLE(assetIdOffsetSlot, -1);
      }

      if (this.path != null) {
         buf.setIntLE(pathOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.path, 4096000);
      } else {
         buf.setIntLE(pathOffsetSlot, -1);
      }

      if (this.texture != null) {
         buf.setIntLE(textureOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.texture, 4096000);
      } else {
         buf.setIntLE(textureOffsetSlot, -1);
      }

      if (this.gradientSet != null) {
         buf.setIntLE(gradientSetOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.gradientSet, 4096000);
      } else {
         buf.setIntLE(gradientSetOffsetSlot, -1);
      }

      if (this.gradientId != null) {
         buf.setIntLE(gradientIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.gradientId, 4096000);
      } else {
         buf.setIntLE(gradientIdOffsetSlot, -1);
      }

      if (this.camera != null) {
         buf.setIntLE(cameraOffsetSlot, buf.writerIndex() - varBlockStart);
         this.camera.serialize(buf);
      } else {
         buf.setIntLE(cameraOffsetSlot, -1);
      }

      if (this.animationSets != null) {
         buf.setIntLE(animationSetsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.animationSets.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("AnimationSets", this.animationSets.size(), 4096000);
         }

         VarInt.write(buf, this.animationSets.size());

         for (Entry<String, AnimationSet> e : this.animationSets.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(animationSetsOffsetSlot, -1);
      }

      if (this.attachments != null) {
         buf.setIntLE(attachmentsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.attachments.length > 4096000) {
            throw ProtocolException.arrayTooLong("Attachments", this.attachments.length, 4096000);
         }

         VarInt.write(buf, this.attachments.length);

         for (ModelAttachment item : this.attachments) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(attachmentsOffsetSlot, -1);
      }

      if (this.particles != null) {
         buf.setIntLE(particlesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.particles.length > 4096000) {
            throw ProtocolException.arrayTooLong("Particles", this.particles.length, 4096000);
         }

         VarInt.write(buf, this.particles.length);

         for (ModelParticle item : this.particles) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(particlesOffsetSlot, -1);
      }

      if (this.trails != null) {
         buf.setIntLE(trailsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.trails.length > 4096000) {
            throw ProtocolException.arrayTooLong("Trails", this.trails.length, 4096000);
         }

         VarInt.write(buf, this.trails.length);

         for (ModelTrail item : this.trails) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(trailsOffsetSlot, -1);
      }

      if (this.detailBoxes != null) {
         buf.setIntLE(detailBoxesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.detailBoxes.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DetailBoxes", this.detailBoxes.size(), 4096000);
         }

         VarInt.write(buf, this.detailBoxes.size());

         for (Entry<String, DetailBox[]> e : this.detailBoxes.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            VarInt.write(buf, e.getValue().length);

            for (DetailBox arrItem : e.getValue()) {
               arrItem.serialize(buf);
            }
         }
      } else {
         buf.setIntLE(detailBoxesOffsetSlot, -1);
      }

      if (this.phobiaModel != null) {
         buf.setIntLE(phobiaModelOffsetSlot, buf.writerIndex() - varBlockStart);
         this.phobiaModel.serialize(buf);
      } else {
         buf.setIntLE(phobiaModelOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 99;
      if (this.assetId != null) {
         size += PacketIO.stringSize(this.assetId);
      }

      if (this.path != null) {
         size += PacketIO.stringSize(this.path);
      }

      if (this.texture != null) {
         size += PacketIO.stringSize(this.texture);
      }

      if (this.gradientSet != null) {
         size += PacketIO.stringSize(this.gradientSet);
      }

      if (this.gradientId != null) {
         size += PacketIO.stringSize(this.gradientId);
      }

      if (this.camera != null) {
         size += this.camera.computeSize();
      }

      if (this.animationSets != null) {
         int animationSetsSize = 0;

         for (Entry<String, AnimationSet> kvp : this.animationSets.entrySet()) {
            animationSetsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.animationSets.size()) + animationSetsSize;
      }

      if (this.attachments != null) {
         int attachmentsSize = 0;

         for (ModelAttachment elem : this.attachments) {
            attachmentsSize += elem.computeSize();
         }

         size += VarInt.size(this.attachments.length) + attachmentsSize;
      }

      if (this.particles != null) {
         int particlesSize = 0;

         for (ModelParticle elem : this.particles) {
            particlesSize += elem.computeSize();
         }

         size += VarInt.size(this.particles.length) + particlesSize;
      }

      if (this.trails != null) {
         int trailsSize = 0;

         for (ModelTrail elem : this.trails) {
            trailsSize += elem.computeSize();
         }

         size += VarInt.size(this.trails.length) + trailsSize;
      }

      if (this.detailBoxes != null) {
         int detailBoxesSize = 0;

         for (Entry<String, DetailBox[]> kvp : this.detailBoxes.entrySet()) {
            detailBoxesSize += PacketIO.stringSize(kvp.getKey()) + VarInt.size(kvp.getValue().length) + ((DetailBox[])kvp.getValue()).length * 37;
         }

         size += VarInt.size(this.detailBoxes.size()) + detailBoxesSize;
      }

      if (this.phobiaModel != null) {
         size += this.phobiaModel.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 99) {
         return ValidationResult.error("Buffer too small: expected at least 99 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 2);
         if ((nullBits[0] & 4) != 0) {
            int assetIdOffset = buffer.getIntLE(offset + 51);
            if (assetIdOffset < 0) {
               return ValidationResult.error("Invalid offset for AssetId");
            }

            int pos = offset + 99 + assetIdOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for AssetId");
            }

            int assetIdLen = VarInt.peek(buffer, pos);
            if (assetIdLen < 0) {
               return ValidationResult.error("Invalid string length for AssetId");
            }

            if (assetIdLen > 4096000) {
               return ValidationResult.error("AssetId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += assetIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading AssetId");
            }
         }

         if ((nullBits[0] & 8) != 0) {
            int pathOffset = buffer.getIntLE(offset + 55);
            if (pathOffset < 0) {
               return ValidationResult.error("Invalid offset for Path");
            }

            int posx = offset + 99 + pathOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Path");
            }

            int pathLen = VarInt.peek(buffer, posx);
            if (pathLen < 0) {
               return ValidationResult.error("Invalid string length for Path");
            }

            if (pathLen > 4096000) {
               return ValidationResult.error("Path exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += pathLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Path");
            }
         }

         if ((nullBits[0] & 16) != 0) {
            int textureOffset = buffer.getIntLE(offset + 59);
            if (textureOffset < 0) {
               return ValidationResult.error("Invalid offset for Texture");
            }

            int posxx = offset + 99 + textureOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Texture");
            }

            int textureLen = VarInt.peek(buffer, posxx);
            if (textureLen < 0) {
               return ValidationResult.error("Invalid string length for Texture");
            }

            if (textureLen > 4096000) {
               return ValidationResult.error("Texture exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += textureLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Texture");
            }
         }

         if ((nullBits[0] & 32) != 0) {
            int gradientSetOffset = buffer.getIntLE(offset + 63);
            if (gradientSetOffset < 0) {
               return ValidationResult.error("Invalid offset for GradientSet");
            }

            int posxxx = offset + 99 + gradientSetOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for GradientSet");
            }

            int gradientSetLen = VarInt.peek(buffer, posxxx);
            if (gradientSetLen < 0) {
               return ValidationResult.error("Invalid string length for GradientSet");
            }

            if (gradientSetLen > 4096000) {
               return ValidationResult.error("GradientSet exceeds max length 4096000");
            }

            posxxx += VarInt.length(buffer, posxxx);
            posxxx += gradientSetLen;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading GradientSet");
            }
         }

         if ((nullBits[0] & 64) != 0) {
            int gradientIdOffset = buffer.getIntLE(offset + 67);
            if (gradientIdOffset < 0) {
               return ValidationResult.error("Invalid offset for GradientId");
            }

            int posxxxx = offset + 99 + gradientIdOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for GradientId");
            }

            int gradientIdLen = VarInt.peek(buffer, posxxxx);
            if (gradientIdLen < 0) {
               return ValidationResult.error("Invalid string length for GradientId");
            }

            if (gradientIdLen > 4096000) {
               return ValidationResult.error("GradientId exceeds max length 4096000");
            }

            posxxxx += VarInt.length(buffer, posxxxx);
            posxxxx += gradientIdLen;
            if (posxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading GradientId");
            }
         }

         if ((nullBits[0] & 128) != 0) {
            int cameraOffset = buffer.getIntLE(offset + 71);
            if (cameraOffset < 0) {
               return ValidationResult.error("Invalid offset for Camera");
            }

            int posxxxxx = offset + 99 + cameraOffset;
            if (posxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Camera");
            }

            ValidationResult cameraResult = CameraSettings.validateStructure(buffer, posxxxxx);
            if (!cameraResult.isValid()) {
               return ValidationResult.error("Invalid Camera: " + cameraResult.error());
            }

            posxxxxx += CameraSettings.computeBytesConsumed(buffer, posxxxxx);
         }

         if ((nullBits[1] & 1) != 0) {
            int animationSetsOffset = buffer.getIntLE(offset + 75);
            if (animationSetsOffset < 0) {
               return ValidationResult.error("Invalid offset for AnimationSets");
            }

            int posxxxxxx = offset + 99 + animationSetsOffset;
            if (posxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for AnimationSets");
            }

            int animationSetsCount = VarInt.peek(buffer, posxxxxxx);
            if (animationSetsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for AnimationSets");
            }

            if (animationSetsCount > 4096000) {
               return ValidationResult.error("AnimationSets exceeds max length 4096000");
            }

            posxxxxxx += VarInt.length(buffer, posxxxxxx);

            for (int i = 0; i < animationSetsCount; i++) {
               int keyLen = VarInt.peek(buffer, posxxxxxx);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posxxxxxx += VarInt.length(buffer, posxxxxxx);
               posxxxxxx += keyLen;
               if (posxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxx += AnimationSet.computeBytesConsumed(buffer, posxxxxxx);
            }
         }

         if ((nullBits[1] & 2) != 0) {
            int attachmentsOffset = buffer.getIntLE(offset + 79);
            if (attachmentsOffset < 0) {
               return ValidationResult.error("Invalid offset for Attachments");
            }

            int posxxxxxxx = offset + 99 + attachmentsOffset;
            if (posxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Attachments");
            }

            int attachmentsCount = VarInt.peek(buffer, posxxxxxxx);
            if (attachmentsCount < 0) {
               return ValidationResult.error("Invalid array count for Attachments");
            }

            if (attachmentsCount > 4096000) {
               return ValidationResult.error("Attachments exceeds max length 4096000");
            }

            posxxxxxxx += VarInt.length(buffer, posxxxxxxx);

            for (int i = 0; i < attachmentsCount; i++) {
               ValidationResult structResult = ModelAttachment.validateStructure(buffer, posxxxxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ModelAttachment in Attachments[" + i + "]: " + structResult.error());
               }

               posxxxxxxx += ModelAttachment.computeBytesConsumed(buffer, posxxxxxxx);
            }
         }

         if ((nullBits[1] & 4) != 0) {
            int particlesOffset = buffer.getIntLE(offset + 83);
            if (particlesOffset < 0) {
               return ValidationResult.error("Invalid offset for Particles");
            }

            int posxxxxxxxx = offset + 99 + particlesOffset;
            if (posxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Particles");
            }

            int particlesCount = VarInt.peek(buffer, posxxxxxxxx);
            if (particlesCount < 0) {
               return ValidationResult.error("Invalid array count for Particles");
            }

            if (particlesCount > 4096000) {
               return ValidationResult.error("Particles exceeds max length 4096000");
            }

            posxxxxxxxx += VarInt.length(buffer, posxxxxxxxx);

            for (int i = 0; i < particlesCount; i++) {
               ValidationResult structResult = ModelParticle.validateStructure(buffer, posxxxxxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ModelParticle in Particles[" + i + "]: " + structResult.error());
               }

               posxxxxxxxx += ModelParticle.computeBytesConsumed(buffer, posxxxxxxxx);
            }
         }

         if ((nullBits[1] & 8) != 0) {
            int trailsOffset = buffer.getIntLE(offset + 87);
            if (trailsOffset < 0) {
               return ValidationResult.error("Invalid offset for Trails");
            }

            int posxxxxxxxxx = offset + 99 + trailsOffset;
            if (posxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Trails");
            }

            int trailsCount = VarInt.peek(buffer, posxxxxxxxxx);
            if (trailsCount < 0) {
               return ValidationResult.error("Invalid array count for Trails");
            }

            if (trailsCount > 4096000) {
               return ValidationResult.error("Trails exceeds max length 4096000");
            }

            posxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxx);

            for (int i = 0; i < trailsCount; i++) {
               ValidationResult structResult = ModelTrail.validateStructure(buffer, posxxxxxxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ModelTrail in Trails[" + i + "]: " + structResult.error());
               }

               posxxxxxxxxx += ModelTrail.computeBytesConsumed(buffer, posxxxxxxxxx);
            }
         }

         if ((nullBits[1] & 16) != 0) {
            int detailBoxesOffset = buffer.getIntLE(offset + 91);
            if (detailBoxesOffset < 0) {
               return ValidationResult.error("Invalid offset for DetailBoxes");
            }

            int posxxxxxxxxxx = offset + 99 + detailBoxesOffset;
            if (posxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for DetailBoxes");
            }

            int detailBoxesCount = VarInt.peek(buffer, posxxxxxxxxxx);
            if (detailBoxesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for DetailBoxes");
            }

            if (detailBoxesCount > 4096000) {
               return ValidationResult.error("DetailBoxes exceeds max length 4096000");
            }

            posxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxx);

            for (int i = 0; i < detailBoxesCount; i++) {
               int keyLenx = VarInt.peek(buffer, posxxxxxxxxxx);
               if (keyLenx < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLenx > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxx);
               posxxxxxxxxxx += keyLenx;
               if (posxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               int valueArrCount = VarInt.peek(buffer, posxxxxxxxxxx);
               if (valueArrCount < 0) {
                  return ValidationResult.error("Invalid array count for value");
               }

               posxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxx);

               for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                  posxxxxxxxxxx += 37;
               }
            }
         }

         if ((nullBits[1] & 32) != 0) {
            int phobiaModelOffset = buffer.getIntLE(offset + 95);
            if (phobiaModelOffset < 0) {
               return ValidationResult.error("Invalid offset for PhobiaModel");
            }

            int posxxxxxxxxxxx = offset + 99 + phobiaModelOffset;
            if (posxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for PhobiaModel");
            }

            ValidationResult phobiaModelResult = validateStructure(buffer, posxxxxxxxxxxx);
            if (!phobiaModelResult.isValid()) {
               return ValidationResult.error("Invalid PhobiaModel: " + phobiaModelResult.error());
            }

            posxxxxxxxxxxx += computeBytesConsumed(buffer, posxxxxxxxxxxx);
         }

         return ValidationResult.OK;
      }
   }

   public Model clone() {
      Model copy = new Model();
      copy.assetId = this.assetId;
      copy.path = this.path;
      copy.texture = this.texture;
      copy.gradientSet = this.gradientSet;
      copy.gradientId = this.gradientId;
      copy.camera = this.camera != null ? this.camera.clone() : null;
      copy.scale = this.scale;
      copy.eyeHeight = this.eyeHeight;
      copy.crouchOffset = this.crouchOffset;
      copy.sittingOffset = this.sittingOffset;
      copy.sleepingOffset = this.sleepingOffset;
      if (this.animationSets != null) {
         Map<String, AnimationSet> m = new HashMap<>();

         for (Entry<String, AnimationSet> e : this.animationSets.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.animationSets = m;
      }

      copy.attachments = this.attachments != null ? Arrays.stream(this.attachments).map(ex -> ex.clone()).toArray(ModelAttachment[]::new) : null;
      copy.hitbox = this.hitbox != null ? this.hitbox.clone() : null;
      copy.particles = this.particles != null ? Arrays.stream(this.particles).map(ex -> ex.clone()).toArray(ModelParticle[]::new) : null;
      copy.trails = this.trails != null ? Arrays.stream(this.trails).map(ex -> ex.clone()).toArray(ModelTrail[]::new) : null;
      copy.light = this.light != null ? this.light.clone() : null;
      if (this.detailBoxes != null) {
         Map<String, DetailBox[]> m = new HashMap<>();

         for (Entry<String, DetailBox[]> e : this.detailBoxes.entrySet()) {
            m.put(e.getKey(), Arrays.stream(e.getValue()).map(x -> x.clone()).toArray(DetailBox[]::new));
         }

         copy.detailBoxes = m;
      }

      copy.phobia = this.phobia;
      copy.phobiaModel = this.phobiaModel != null ? this.phobiaModel.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Model other)
            ? false
            : Objects.equals(this.assetId, other.assetId)
               && Objects.equals(this.path, other.path)
               && Objects.equals(this.texture, other.texture)
               && Objects.equals(this.gradientSet, other.gradientSet)
               && Objects.equals(this.gradientId, other.gradientId)
               && Objects.equals(this.camera, other.camera)
               && this.scale == other.scale
               && this.eyeHeight == other.eyeHeight
               && this.crouchOffset == other.crouchOffset
               && this.sittingOffset == other.sittingOffset
               && this.sleepingOffset == other.sleepingOffset
               && Objects.equals(this.animationSets, other.animationSets)
               && Arrays.equals((Object[])this.attachments, (Object[])other.attachments)
               && Objects.equals(this.hitbox, other.hitbox)
               && Arrays.equals((Object[])this.particles, (Object[])other.particles)
               && Arrays.equals((Object[])this.trails, (Object[])other.trails)
               && Objects.equals(this.light, other.light)
               && Objects.equals(this.detailBoxes, other.detailBoxes)
               && Objects.equals(this.phobia, other.phobia)
               && Objects.equals(this.phobiaModel, other.phobiaModel);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.assetId);
      result = 31 * result + Objects.hashCode(this.path);
      result = 31 * result + Objects.hashCode(this.texture);
      result = 31 * result + Objects.hashCode(this.gradientSet);
      result = 31 * result + Objects.hashCode(this.gradientId);
      result = 31 * result + Objects.hashCode(this.camera);
      result = 31 * result + Float.hashCode(this.scale);
      result = 31 * result + Float.hashCode(this.eyeHeight);
      result = 31 * result + Float.hashCode(this.crouchOffset);
      result = 31 * result + Float.hashCode(this.sittingOffset);
      result = 31 * result + Float.hashCode(this.sleepingOffset);
      result = 31 * result + Objects.hashCode(this.animationSets);
      result = 31 * result + Arrays.hashCode((Object[])this.attachments);
      result = 31 * result + Objects.hashCode(this.hitbox);
      result = 31 * result + Arrays.hashCode((Object[])this.particles);
      result = 31 * result + Arrays.hashCode((Object[])this.trails);
      result = 31 * result + Objects.hashCode(this.light);
      result = 31 * result + Objects.hashCode(this.detailBoxes);
      result = 31 * result + Objects.hashCode(this.phobia);
      return 31 * result + Objects.hashCode(this.phobiaModel);
   }
}
