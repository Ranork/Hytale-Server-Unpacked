package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 99) {
         throw ProtocolException.bufferTooSmall("Model", 99, buf.readableBytes() - offset);
      } else {
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
            int varPosBase0 = buf.getIntLE(offset + 51);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("AssetId", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 99 + varPosBase0;
            int assetIdLen = VarInt.peek(buf, varPos0);
            if (assetIdLen < 0) {
               throw ProtocolException.invalidVarInt("AssetId");
            }

            int assetIdVarIntLen = VarInt.size(assetIdLen);
            if (assetIdLen > 4096000) {
               throw ProtocolException.stringTooLong("AssetId", assetIdLen, 4096000);
            }

            if (varPos0 + assetIdVarIntLen + assetIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("AssetId", varPos0 + assetIdVarIntLen + assetIdLen, buf.readableBytes());
            }

            obj.assetId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits[0] & 8) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 55);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("Path", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 99 + varPosBase1;
            int pathLen = VarInt.peek(buf, varPos1);
            if (pathLen < 0) {
               throw ProtocolException.invalidVarInt("Path");
            }

            int pathVarIntLen = VarInt.size(pathLen);
            if (pathLen > 4096000) {
               throw ProtocolException.stringTooLong("Path", pathLen, 4096000);
            }

            if (varPos1 + pathVarIntLen + pathLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Path", varPos1 + pathVarIntLen + pathLen, buf.readableBytes());
            }

            obj.path = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits[0] & 16) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 59);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("Texture", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 99 + varPosBase2;
            int textureLen = VarInt.peek(buf, varPos2);
            if (textureLen < 0) {
               throw ProtocolException.invalidVarInt("Texture");
            }

            int textureVarIntLen = VarInt.size(textureLen);
            if (textureLen > 4096000) {
               throw ProtocolException.stringTooLong("Texture", textureLen, 4096000);
            }

            if (varPos2 + textureVarIntLen + textureLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Texture", varPos2 + textureVarIntLen + textureLen, buf.readableBytes());
            }

            obj.texture = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits[0] & 32) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 63);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("GradientSet", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 99 + varPosBase3;
            int gradientSetLen = VarInt.peek(buf, varPos3);
            if (gradientSetLen < 0) {
               throw ProtocolException.invalidVarInt("GradientSet");
            }

            int gradientSetVarIntLen = VarInt.size(gradientSetLen);
            if (gradientSetLen > 4096000) {
               throw ProtocolException.stringTooLong("GradientSet", gradientSetLen, 4096000);
            }

            if (varPos3 + gradientSetVarIntLen + gradientSetLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("GradientSet", varPos3 + gradientSetVarIntLen + gradientSetLen, buf.readableBytes());
            }

            obj.gradientSet = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
         }

         if ((nullBits[0] & 64) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 67);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("GradientId", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 99 + varPosBase4;
            int gradientIdLen = VarInt.peek(buf, varPos4);
            if (gradientIdLen < 0) {
               throw ProtocolException.invalidVarInt("GradientId");
            }

            int gradientIdVarIntLen = VarInt.size(gradientIdLen);
            if (gradientIdLen > 4096000) {
               throw ProtocolException.stringTooLong("GradientId", gradientIdLen, 4096000);
            }

            if (varPos4 + gradientIdVarIntLen + gradientIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("GradientId", varPos4 + gradientIdVarIntLen + gradientIdLen, buf.readableBytes());
            }

            obj.gradientId = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
         }

         if ((nullBits[0] & 128) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 71);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("Camera", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 99 + varPosBase5;
            obj.camera = CameraSettings.deserialize(buf, varPos5);
         }

         if ((nullBits[1] & 1) != 0) {
            int varPosBase6 = buf.getIntLE(offset + 75);
            if (varPosBase6 < 0 || varPosBase6 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("AnimationSets", varPosBase6, buf.readableBytes());
            }

            int varPos6 = offset + 99 + varPosBase6;
            int animationSetsCount = VarInt.peek(buf, varPos6);
            if (animationSetsCount < 0) {
               throw ProtocolException.invalidVarInt("AnimationSets");
            }

            int varIntLen = VarInt.size(animationSetsCount);
            if (animationSetsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("AnimationSets", animationSetsCount, 4096000);
            }

            obj.animationSets = new HashMap<>(animationSetsCount);
            int dictPos = varPos6 + varIntLen;

            for (int i = 0; i < animationSetsCount; i++) {
               int keyLen = VarInt.peek(buf, dictPos);
               if (keyLen < 0) {
                  throw ProtocolException.invalidVarInt("key");
               }

               int keyVarLen = VarInt.size(keyLen);
               if (keyLen > 4096000) {
                  throw ProtocolException.stringTooLong("key", keyLen, 4096000);
               }

               if (dictPos + keyVarLen + keyLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("key", dictPos + keyVarLen + keyLen, buf.readableBytes());
               }

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
            int varPosBase7 = buf.getIntLE(offset + 79);
            if (varPosBase7 < 0 || varPosBase7 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("Attachments", varPosBase7, buf.readableBytes());
            }

            int varPos7 = offset + 99 + varPosBase7;
            int attachmentsCount = VarInt.peek(buf, varPos7);
            if (attachmentsCount < 0) {
               throw ProtocolException.invalidVarInt("Attachments");
            }

            int varIntLen = VarInt.size(attachmentsCount);
            if (attachmentsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Attachments", attachmentsCount, 4096000);
            }

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
            int varPosBase8 = buf.getIntLE(offset + 83);
            if (varPosBase8 < 0 || varPosBase8 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("Particles", varPosBase8, buf.readableBytes());
            }

            int varPos8 = offset + 99 + varPosBase8;
            int particlesCount = VarInt.peek(buf, varPos8);
            if (particlesCount < 0) {
               throw ProtocolException.invalidVarInt("Particles");
            }

            int varIntLenx = VarInt.size(particlesCount);
            if (particlesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Particles", particlesCount, 4096000);
            }

            if (varPos8 + varIntLenx + particlesCount * 34L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Particles", varPos8 + varIntLenx + particlesCount * 34, buf.readableBytes());
            }

            obj.particles = new ModelParticle[particlesCount];
            int elemPos = varPos8 + varIntLenx;

            for (int i = 0; i < particlesCount; i++) {
               obj.particles[i] = ModelParticle.deserialize(buf, elemPos);
               elemPos += ModelParticle.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits[1] & 8) != 0) {
            int varPosBase9 = buf.getIntLE(offset + 87);
            if (varPosBase9 < 0 || varPosBase9 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("Trails", varPosBase9, buf.readableBytes());
            }

            int varPos9 = offset + 99 + varPosBase9;
            int trailsCount = VarInt.peek(buf, varPos9);
            if (trailsCount < 0) {
               throw ProtocolException.invalidVarInt("Trails");
            }

            int varIntLenxx = VarInt.size(trailsCount);
            if (trailsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Trails", trailsCount, 4096000);
            }

            if (varPos9 + varIntLenxx + trailsCount * 27L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Trails", varPos9 + varIntLenxx + trailsCount * 27, buf.readableBytes());
            }

            obj.trails = new ModelTrail[trailsCount];
            int elemPos = varPos9 + varIntLenxx;

            for (int i = 0; i < trailsCount; i++) {
               obj.trails[i] = ModelTrail.deserialize(buf, elemPos);
               elemPos += ModelTrail.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits[1] & 16) != 0) {
            int varPosBase10 = buf.getIntLE(offset + 91);
            if (varPosBase10 < 0 || varPosBase10 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("DetailBoxes", varPosBase10, buf.readableBytes());
            }

            int varPos10 = offset + 99 + varPosBase10;
            int detailBoxesCount = VarInt.peek(buf, varPos10);
            if (detailBoxesCount < 0) {
               throw ProtocolException.invalidVarInt("DetailBoxes");
            }

            int varIntLenxxx = VarInt.size(detailBoxesCount);
            if (detailBoxesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("DetailBoxes", detailBoxesCount, 4096000);
            }

            obj.detailBoxes = new HashMap<>(detailBoxesCount);
            int dictPos = varPos10 + varIntLenxxx;

            for (int i = 0; i < detailBoxesCount; i++) {
               int keyLenx = VarInt.peek(buf, dictPos);
               if (keyLenx < 0) {
                  throw ProtocolException.invalidVarInt("key");
               }

               int keyVarLenx = VarInt.size(keyLenx);
               if (keyLenx > 4096000) {
                  throw ProtocolException.stringTooLong("key", keyLenx, 4096000);
               }

               if (dictPos + keyVarLenx + keyLenx > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("key", dictPos + keyVarLenx + keyLenx, buf.readableBytes());
               }

               String key = PacketIO.readVarString(buf, dictPos);
               dictPos += keyVarLenx + keyLenx;
               int valLen = VarInt.peek(buf, dictPos);
               if (valLen < 0) {
                  throw ProtocolException.invalidVarInt("val");
               }

               int valVarLen = VarInt.size(valLen);
               if (valLen > 64) {
                  throw ProtocolException.arrayTooLong("val", valLen, 64);
               }

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
            int varPosBase11 = buf.getIntLE(offset + 95);
            if (varPosBase11 < 0 || varPosBase11 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("PhobiaModel", varPosBase11, buf.readableBytes());
            }

            int varPos11 = offset + 99 + varPosBase11;
            obj.phobiaModel = deserialize(buf, varPos11);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      int maxEnd = 99;
      if ((nullBits[0] & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 51);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("AssetId", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 99 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[0] & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 55);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("Path", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 99 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[0] & 16) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 59);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("Texture", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 99 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[0] & 32) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 63);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("GradientSet", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 99 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(sl) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[0] & 64) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 67);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("GradientId", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 99 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(sl) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 71);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("Camera", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 99 + fieldOffset5;
         pos5 += CameraSettings.computeBytesConsumed(buf, pos5);
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 75);
         if (fieldOffset6 < 0 || fieldOffset6 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("AnimationSets", fieldOffset6, maxEnd);
         }

         int pos6 = offset + 99 + fieldOffset6;
         int dictLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos6);
            pos6 += VarInt.size(sl) + sl;
            pos6 += AnimationSet.computeBytesConsumed(buf, pos6);
         }

         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[1] & 2) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 79);
         if (fieldOffset7 < 0 || fieldOffset7 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("Attachments", fieldOffset7, maxEnd);
         }

         int pos7 = offset + 99 + fieldOffset7;
         int arrLen = VarInt.peek(buf, pos7);
         pos7 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos7 += ModelAttachment.computeBytesConsumed(buf, pos7);
         }

         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      if ((nullBits[1] & 4) != 0) {
         int fieldOffset8 = buf.getIntLE(offset + 83);
         if (fieldOffset8 < 0 || fieldOffset8 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("Particles", fieldOffset8, maxEnd);
         }

         int pos8 = offset + 99 + fieldOffset8;
         int arrLen = VarInt.peek(buf, pos8);
         pos8 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos8 += ModelParticle.computeBytesConsumed(buf, pos8);
         }

         if (pos8 - offset > maxEnd) {
            maxEnd = pos8 - offset;
         }
      }

      if ((nullBits[1] & 8) != 0) {
         int fieldOffset9 = buf.getIntLE(offset + 87);
         if (fieldOffset9 < 0 || fieldOffset9 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("Trails", fieldOffset9, maxEnd);
         }

         int pos9 = offset + 99 + fieldOffset9;
         int arrLen = VarInt.peek(buf, pos9);
         pos9 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos9 += ModelTrail.computeBytesConsumed(buf, pos9);
         }

         if (pos9 - offset > maxEnd) {
            maxEnd = pos9 - offset;
         }
      }

      if ((nullBits[1] & 16) != 0) {
         int fieldOffset10 = buf.getIntLE(offset + 91);
         if (fieldOffset10 < 0 || fieldOffset10 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("DetailBoxes", fieldOffset10, maxEnd);
         }

         int pos10 = offset + 99 + fieldOffset10;
         int dictLen = VarInt.peek(buf, pos10);
         pos10 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos10);
            pos10 += VarInt.size(sl) + sl;
            sl = VarInt.peek(buf, pos10);
            pos10 += VarInt.size(sl);

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
         if (fieldOffset11 < 0 || fieldOffset11 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("PhobiaModel", fieldOffset11, maxEnd);
         }

         int pos11 = offset + 99 + fieldOffset11;
         pos11 += computeBytesConsumed(buf, pos11);
         if (pos11 - offset > maxEnd) {
            maxEnd = pos11 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 99L;
   }

   @Nullable
   public static String getAssetId(MemorySegment mem) {
      return getAssetId(mem, 0);
   }

   @Nullable
   public static String getAssetId(MemorySegment mem, int offset) {
      return hasAssetId(mem, offset)
         ? PacketIO.readVarString("AssetId", mem, offset + getValidatedOffset(mem, offset, 51, 99, "AssetId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getPath(MemorySegment mem) {
      return getPath(mem, 0);
   }

   @Nullable
   public static String getPath(MemorySegment mem, int offset) {
      return hasPath(mem, offset)
         ? PacketIO.readVarString("Path", mem, offset + getValidatedOffset(mem, offset, 55, 99, "Path"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getTexture(MemorySegment mem) {
      return getTexture(mem, 0);
   }

   @Nullable
   public static String getTexture(MemorySegment mem, int offset) {
      return hasTexture(mem, offset)
         ? PacketIO.readVarString("Texture", mem, offset + getValidatedOffset(mem, offset, 59, 99, "Texture"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getGradientSet(MemorySegment mem) {
      return getGradientSet(mem, 0);
   }

   @Nullable
   public static String getGradientSet(MemorySegment mem, int offset) {
      return hasGradientSet(mem, offset)
         ? PacketIO.readVarString("GradientSet", mem, offset + getValidatedOffset(mem, offset, 63, 99, "GradientSet"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getGradientId(MemorySegment mem) {
      return getGradientId(mem, 0);
   }

   @Nullable
   public static String getGradientId(MemorySegment mem, int offset) {
      return hasGradientId(mem, offset)
         ? PacketIO.readVarString("GradientId", mem, offset + getValidatedOffset(mem, offset, 67, 99, "GradientId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static CameraSettings getCamera(MemorySegment mem) {
      return getCamera(mem, 0);
   }

   @Nullable
   public static CameraSettings getCamera(MemorySegment mem, int offset) {
      return hasCamera(mem, offset) ? CameraSettings.toObject(mem, offset + getValidatedOffset(mem, offset, 71, 99, "Camera")) : null;
   }

   public static float getScale(MemorySegment mem) {
      return getScale(mem, 0);
   }

   public static float getScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 2));
   }

   public static float getEyeHeight(MemorySegment mem) {
      return getEyeHeight(mem, 0);
   }

   public static float getEyeHeight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 6));
   }

   public static float getCrouchOffset(MemorySegment mem) {
      return getCrouchOffset(mem, 0);
   }

   public static float getCrouchOffset(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 10));
   }

   public static float getSittingOffset(MemorySegment mem) {
      return getSittingOffset(mem, 0);
   }

   public static float getSittingOffset(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 14));
   }

   public static float getSleepingOffset(MemorySegment mem) {
      return getSleepingOffset(mem, 0);
   }

   public static float getSleepingOffset(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 18));
   }

   @Nullable
   public static Map<String, AnimationSet> getAnimationSets(MemorySegment mem) {
      return getAnimationSets(mem, 0);
   }

   @Nullable
   public static Map<String, AnimationSet> getAnimationSets(MemorySegment mem, int offset) {
      if (!hasAnimationSets(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 75, 99, "AnimationSets");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("AnimationSets", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("AnimationSets", len, 4096000);
         } else {
            Map<String, AnimationSet> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               AnimationSet value = AnimationSet.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("AnimationSets", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static ModelAttachment[] getAttachments(MemorySegment mem) {
      return getAttachments(mem, 0);
   }

   @Nullable
   public static ModelAttachment[] getAttachments(MemorySegment mem, int offset) {
      if (!hasAttachments(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 79, 99, "Attachments");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Attachments", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Attachments", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Attachments", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ModelAttachment[] data = new ModelAttachment[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ModelAttachment.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static Hitbox getHitbox(MemorySegment mem) {
      return getHitbox(mem, 0);
   }

   @Nullable
   public static Hitbox getHitbox(MemorySegment mem, int offset) {
      return hasHitbox(mem, offset) ? Hitbox.toObject(mem, offset + 22) : null;
   }

   @Nullable
   public static ModelParticle[] getParticles(MemorySegment mem) {
      return getParticles(mem, 0);
   }

   @Nullable
   public static ModelParticle[] getParticles(MemorySegment mem, int offset) {
      if (!hasParticles(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 83, 99, "Particles");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Particles", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Particles", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Particles", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ModelParticle[] data = new ModelParticle[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ModelParticle.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static ModelTrail[] getTrails(MemorySegment mem) {
      return getTrails(mem, 0);
   }

   @Nullable
   public static ModelTrail[] getTrails(MemorySegment mem, int offset) {
      if (!hasTrails(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 87, 99, "Trails");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Trails", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Trails", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Trails", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ModelTrail[] data = new ModelTrail[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ModelTrail.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static ColorLight getLight(MemorySegment mem) {
      return getLight(mem, 0);
   }

   @Nullable
   public static ColorLight getLight(MemorySegment mem, int offset) {
      return hasLight(mem, offset) ? ColorLight.toObject(mem, offset + 46) : null;
   }

   @Nullable
   public static Map<String, DetailBox[]> getDetailBoxes(MemorySegment mem) {
      return getDetailBoxes(mem, 0);
   }

   @Nullable
   public static Map<String, DetailBox[]> getDetailBoxes(MemorySegment mem, int offset) {
      if (!hasDetailBoxes(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 91, 99, "DetailBoxes");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("DetailBoxes", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DetailBoxes", len, 4096000);
         } else {
            Map<String, DetailBox[]> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               long valuePacked = VarInt.getWithLength(mem, off);
               int valueLen = (int)valuePacked;
               int valueVarLen = (int)(valuePacked >>> 32);
               if (valueLen < 0) {
                  throw ProtocolException.negativeLength("value", valueLen);
               }

               if (valueLen > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLen, 64);
               }

               if (off + valueVarLen + valueLen * 37L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", off + valueVarLen + valueLen * 37, (int)mem.byteSize());
               }

               off += valueVarLen;
               DetailBox[] value = new DetailBox[valueLen];

               for (int valueIdx = 0; valueIdx < valueLen; valueIdx++) {
                  value[valueIdx] = DetailBox.toObject(mem, off);
                  off += value[valueIdx].computeSize();
               }

               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("DetailBoxes", key);
               }
            }

            return data;
         }
      }
   }

   public static Phobia getPhobia(MemorySegment mem) {
      return getPhobia(mem, 0);
   }

   public static Phobia getPhobia(MemorySegment mem, int offset) {
      return Phobia.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 50)));
   }

   @Nullable
   public static Model getPhobiaModel(MemorySegment mem) {
      return getPhobiaModel(mem, 0);
   }

   @Nullable
   public static Model getPhobiaModel(MemorySegment mem, int offset) {
      return hasPhobiaModel(mem, offset) ? toObject(mem, offset + getValidatedOffset(mem, offset, 95, 99, "PhobiaModel")) : null;
   }

   public static boolean hasHitbox(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasLight(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasAssetId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasPath(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasTexture(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasGradientSet(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasGradientId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasCamera(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static boolean hasAnimationSets(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 1) != 0;
   }

   public static boolean hasAttachments(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 2) != 0;
   }

   public static boolean hasParticles(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 4) != 0;
   }

   public static boolean hasTrails(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 8) != 0;
   }

   public static boolean hasDetailBoxes(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 16) != 0;
   }

   public static boolean hasPhobiaModel(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 32) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static Model toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Model toObject(MemorySegment mem, int offset) {
      if (offset + 99 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Model", offset + 99, (int)mem.byteSize());
      } else {
         Map<String, AnimationSet> animationSets = null;
         if (hasAnimationSets(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 75, 99, "AnimationSets");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("AnimationSets", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("AnimationSets", len, 4096000);
            }

            animationSets = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               AnimationSet value = AnimationSet.toObject(mem, off);
               off += value.computeSize();
               if (animationSets.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("AnimationSets", key);
               }
            }
         }

         ModelAttachment[] attachments = null;
         if (hasAttachments(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 79, 99, "Attachments");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Attachments", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("Attachments", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Attachments", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            attachments = new ModelAttachment[lenx];

            for (int ix = 0; ix < lenx; ix++) {
               attachments[ix] = ModelAttachment.toObject(mem, offx);
               offx += attachments[ix].computeSize();
            }
         }

         ModelParticle[] particles = null;
         if (hasParticles(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 83, 99, "Particles");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("Particles", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("Particles", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Particles", offxx + lenOffset + lenxx, (int)mem.byteSize());
            }

            offxx += lenOffset;
            particles = new ModelParticle[lenxx];

            for (int ix = 0; ix < lenxx; ix++) {
               particles[ix] = ModelParticle.toObject(mem, offxx);
               offxx += particles[ix].computeSize();
            }
         }

         ModelTrail[] trails = null;
         if (hasTrails(mem, offset)) {
            int offxxx = offset + getValidatedOffset(mem, offset, 87, 99, "Trails");
            long packedxxx = VarInt.getWithLength(mem, offxxx);
            int lenxxx = (int)packedxxx;
            if (lenxxx < 0) {
               throw ProtocolException.negativeLength("Trails", lenxxx);
            }

            if (lenxxx > 4096000) {
               throw ProtocolException.arrayTooLong("Trails", lenxxx, 4096000);
            }

            int lenOffset = (int)(packedxxx >>> 32);
            if (offxxx + lenOffset + lenxxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Trails", offxxx + lenOffset + lenxxx, (int)mem.byteSize());
            }

            offxxx += lenOffset;
            trails = new ModelTrail[lenxxx];

            for (int ix = 0; ix < lenxxx; ix++) {
               trails[ix] = ModelTrail.toObject(mem, offxxx);
               offxxx += trails[ix].computeSize();
            }
         }

         Map<String, DetailBox[]> detailBoxes = null;
         if (hasDetailBoxes(mem, offset)) {
            int offxxxx = offset + getValidatedOffset(mem, offset, 91, 99, "DetailBoxes");
            long packedxxxx = VarInt.getWithLength(mem, offxxxx);
            int lenxxxx = (int)packedxxxx;
            if (lenxxxx < 0) {
               throw ProtocolException.negativeLength("DetailBoxes", lenxxxx);
            }

            if (lenxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("DetailBoxes", lenxxxx, 4096000);
            }

            detailBoxes = new HashMap<>(lenxxxx);
            offxxxx += (int)(packedxxxx >>> 32);

            for (int ix = 0; ix < lenxxxx; ix++) {
               long keyPacked = VarInt.getWithLength(mem, offxxxx);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, offxxxx, 16384000, PacketIO.UTF8);
               offxxxx += nkey;
               long valuePacked = VarInt.getWithLength(mem, offxxxx);
               int valueLen = (int)valuePacked;
               int valueVarLen = (int)(valuePacked >>> 32);
               if (valueLen < 0) {
                  throw ProtocolException.negativeLength("value", valueLen);
               }

               if (valueLen > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLen, 64);
               }

               if (offxxxx + valueVarLen + valueLen * 37L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", offxxxx + valueVarLen + valueLen * 37, (int)mem.byteSize());
               }

               offxxxx += valueVarLen;
               DetailBox[] value = new DetailBox[valueLen];

               for (int valueIdx = 0; valueIdx < valueLen; valueIdx++) {
                  value[valueIdx] = DetailBox.toObject(mem, offxxxx);
                  offxxxx += value[valueIdx].computeSize();
               }

               if (detailBoxes.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("DetailBoxes", key);
               }
            }
         }

         return new Model(
            hasAssetId(mem, offset)
               ? PacketIO.readVarString("AssetId", mem, offset + getValidatedOffset(mem, offset, 51, 99, "AssetId"), 4096000, PacketIO.UTF8)
               : null,
            hasPath(mem, offset) ? PacketIO.readVarString("Path", mem, offset + getValidatedOffset(mem, offset, 55, 99, "Path"), 4096000, PacketIO.UTF8) : null,
            hasTexture(mem, offset)
               ? PacketIO.readVarString("Texture", mem, offset + getValidatedOffset(mem, offset, 59, 99, "Texture"), 4096000, PacketIO.UTF8)
               : null,
            hasGradientSet(mem, offset)
               ? PacketIO.readVarString("GradientSet", mem, offset + getValidatedOffset(mem, offset, 63, 99, "GradientSet"), 4096000, PacketIO.UTF8)
               : null,
            hasGradientId(mem, offset)
               ? PacketIO.readVarString("GradientId", mem, offset + getValidatedOffset(mem, offset, 67, 99, "GradientId"), 4096000, PacketIO.UTF8)
               : null,
            hasCamera(mem, offset) ? CameraSettings.toObject(mem, offset + getValidatedOffset(mem, offset, 71, 99, "Camera")) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 6)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 10)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 14)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 18)),
            animationSets,
            attachments,
            hasHitbox(mem, offset) ? Hitbox.toObject(mem, offset + 22) : null,
            particles,
            trails,
            hasLight(mem, offset) ? ColorLight.toObject(mem, offset + 46) : null,
            detailBoxes,
            Phobia.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 50))),
            hasPhobiaModel(mem, offset) ? toObject(mem, offset + getValidatedOffset(mem, offset, 95, 99, "PhobiaModel")) : null
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.hitbox != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.light != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.assetId != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.path != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.texture != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.gradientSet != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.gradientId != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.camera != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      nullBits = 0;
      if (this.animationSets != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.attachments != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.particles != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.trails != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.detailBoxes != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.phobiaModel != null) {
         nullBits = (byte)(nullBits | 32);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 2), this.scale);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 6), this.eyeHeight);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 10), this.crouchOffset);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 14), this.sittingOffset);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 18), this.sleepingOffset);
      if (this.hitbox != null) {
         this.hitbox.serialize(mem, offset + 22);
      } else {
         mem.asSlice(offset + 22, 24L).fill((byte)0);
      }

      if (this.light != null) {
         this.light.serialize(mem, offset + 46);
      } else {
         mem.asSlice(offset + 46, 4L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 50), (byte)this.phobia.getValue());
      int varOffset = offset + 99;
      if (this.assetId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 51), varOffset - offset - 99);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.assetId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 51), -1);
      }

      if (this.path != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 55), varOffset - offset - 99);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.path, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 55), -1);
      }

      if (this.texture != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 59), varOffset - offset - 99);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.texture, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 59), -1);
      }

      if (this.gradientSet != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 63), varOffset - offset - 99);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.gradientSet, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 63), -1);
      }

      if (this.gradientId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 67), varOffset - offset - 99);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.gradientId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 67), -1);
      }

      if (this.camera != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 71), varOffset - offset - 99);
         varOffset += this.camera.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 71), -1);
      }

      if (this.animationSets != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 75), varOffset - offset - 99);
         if (this.animationSets.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("AnimationSets", this.animationSets.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.animationSets.size());

         for (Entry<String, AnimationSet> e : this.animationSets.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 75), -1);
      }

      if (this.attachments != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 79), varOffset - offset - 99);
         if (this.attachments.length > 4096000) {
            throw ProtocolException.arrayTooLong("Attachments", this.attachments.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.attachments.length);
         int attachmentsValueOffset = 0;

         for (int i = 0; i < this.attachments.length; i++) {
            attachmentsValueOffset += this.attachments[i].serialize(mem, varOffset + attachmentsValueOffset);
         }

         varOffset += attachmentsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 79), -1);
      }

      if (this.particles != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 83), varOffset - offset - 99);
         if (this.particles.length > 4096000) {
            throw ProtocolException.arrayTooLong("Particles", this.particles.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.particles.length);
         int particlesValueOffset = 0;

         for (int i = 0; i < this.particles.length; i++) {
            particlesValueOffset += this.particles[i].serialize(mem, varOffset + particlesValueOffset);
         }

         varOffset += particlesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 83), -1);
      }

      if (this.trails != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 87), varOffset - offset - 99);
         if (this.trails.length > 4096000) {
            throw ProtocolException.arrayTooLong("Trails", this.trails.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.trails.length);
         int trailsValueOffset = 0;

         for (int i = 0; i < this.trails.length; i++) {
            trailsValueOffset += this.trails[i].serialize(mem, varOffset + trailsValueOffset);
         }

         varOffset += trailsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 87), -1);
      }

      if (this.detailBoxes != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 91), varOffset - offset - 99);
         if (this.detailBoxes.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DetailBoxes", this.detailBoxes.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.detailBoxes.size());

         for (Entry<String, DetailBox[]> e : this.detailBoxes.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += VarInt.set(mem, varOffset, e.getValue().length);

            for (DetailBox arrItem : e.getValue()) {
               varOffset += arrItem.serialize(mem, varOffset);
            }
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 91), -1);
      }

      if (this.phobiaModel != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 95), varOffset - offset - 99);
         varOffset += this.phobiaModel.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 95), -1);
      }

      return varOffset - offset;
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
         int v = buffer.getByte(offset + 50) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid Phobia value for Phobia");
         } else {
            if ((nullBits[0] & 4) != 0) {
               v = buffer.getIntLE(offset + 51);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for AssetId");
               }

               int pos = offset + 99 + v;
               int assetIdLen = VarInt.peek(buffer, pos);
               if (assetIdLen < 0) {
                  return ValidationResult.error("Invalid string length for AssetId");
               }

               if (assetIdLen > 4096000) {
                  return ValidationResult.error("AssetId exceeds max length 4096000");
               }

               pos += VarInt.size(assetIdLen);
               pos += assetIdLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading AssetId");
               }
            }

            if ((nullBits[0] & 8) != 0) {
               v = buffer.getIntLE(offset + 55);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for Path");
               }

               int posx = offset + 99 + v;
               int pathLen = VarInt.peek(buffer, posx);
               if (pathLen < 0) {
                  return ValidationResult.error("Invalid string length for Path");
               }

               if (pathLen > 4096000) {
                  return ValidationResult.error("Path exceeds max length 4096000");
               }

               posx += VarInt.size(pathLen);
               posx += pathLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Path");
               }
            }

            if ((nullBits[0] & 16) != 0) {
               v = buffer.getIntLE(offset + 59);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for Texture");
               }

               int posxx = offset + 99 + v;
               int textureLen = VarInt.peek(buffer, posxx);
               if (textureLen < 0) {
                  return ValidationResult.error("Invalid string length for Texture");
               }

               if (textureLen > 4096000) {
                  return ValidationResult.error("Texture exceeds max length 4096000");
               }

               posxx += VarInt.size(textureLen);
               posxx += textureLen;
               if (posxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Texture");
               }
            }

            if ((nullBits[0] & 32) != 0) {
               v = buffer.getIntLE(offset + 63);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for GradientSet");
               }

               int posxxx = offset + 99 + v;
               int gradientSetLen = VarInt.peek(buffer, posxxx);
               if (gradientSetLen < 0) {
                  return ValidationResult.error("Invalid string length for GradientSet");
               }

               if (gradientSetLen > 4096000) {
                  return ValidationResult.error("GradientSet exceeds max length 4096000");
               }

               posxxx += VarInt.size(gradientSetLen);
               posxxx += gradientSetLen;
               if (posxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading GradientSet");
               }
            }

            if ((nullBits[0] & 64) != 0) {
               v = buffer.getIntLE(offset + 67);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for GradientId");
               }

               int posxxxx = offset + 99 + v;
               int gradientIdLen = VarInt.peek(buffer, posxxxx);
               if (gradientIdLen < 0) {
                  return ValidationResult.error("Invalid string length for GradientId");
               }

               if (gradientIdLen > 4096000) {
                  return ValidationResult.error("GradientId exceeds max length 4096000");
               }

               posxxxx += VarInt.size(gradientIdLen);
               posxxxx += gradientIdLen;
               if (posxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading GradientId");
               }
            }

            if ((nullBits[0] & 128) != 0) {
               v = buffer.getIntLE(offset + 71);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for Camera");
               }

               int posxxxxx = offset + 99 + v;
               ValidationResult cameraResult = CameraSettings.validateStructure(buffer, posxxxxx);
               if (!cameraResult.isValid()) {
                  return ValidationResult.error("Invalid Camera: " + cameraResult.error());
               }

               posxxxxx += CameraSettings.computeBytesConsumed(buffer, posxxxxx);
            }

            if ((nullBits[1] & 1) != 0) {
               v = buffer.getIntLE(offset + 75);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for AnimationSets");
               }

               int posxxxxx = offset + 99 + v;
               int animationSetsCount = VarInt.peek(buffer, posxxxxx);
               if (animationSetsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for AnimationSets");
               }

               if (animationSetsCount > 4096000) {
                  return ValidationResult.error("AnimationSets exceeds max length 4096000");
               }

               posxxxxx += VarInt.size(animationSetsCount);

               for (int i = 0; i < animationSetsCount; i++) {
                  int keyLen = VarInt.peek(buffer, posxxxxx);
                  if (keyLen < 0) {
                     return ValidationResult.error("Invalid string length for key");
                  }

                  if (keyLen > 4096000) {
                     return ValidationResult.error("key exceeds max length 4096000");
                  }

                  posxxxxx += VarInt.size(keyLen);
                  posxxxxx += keyLen;
                  if (posxxxxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  posxxxxx += AnimationSet.computeBytesConsumed(buffer, posxxxxx);
               }
            }

            if ((nullBits[1] & 2) != 0) {
               v = buffer.getIntLE(offset + 79);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for Attachments");
               }

               int posxxxxxx = offset + 99 + v;
               int attachmentsCount = VarInt.peek(buffer, posxxxxxx);
               if (attachmentsCount < 0) {
                  return ValidationResult.error("Invalid array count for Attachments");
               }

               if (attachmentsCount > 4096000) {
                  return ValidationResult.error("Attachments exceeds max length 4096000");
               }

               posxxxxxx += VarInt.size(attachmentsCount);

               for (int i = 0; i < attachmentsCount; i++) {
                  ValidationResult structResult = ModelAttachment.validateStructure(buffer, posxxxxxx);
                  if (!structResult.isValid()) {
                     return ValidationResult.error("Invalid ModelAttachment in Attachments[" + i + "]: " + structResult.error());
                  }

                  posxxxxxx += ModelAttachment.computeBytesConsumed(buffer, posxxxxxx);
               }
            }

            if ((nullBits[1] & 4) != 0) {
               v = buffer.getIntLE(offset + 83);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for Particles");
               }

               int posxxxxxxx = offset + 99 + v;
               int particlesCount = VarInt.peek(buffer, posxxxxxxx);
               if (particlesCount < 0) {
                  return ValidationResult.error("Invalid array count for Particles");
               }

               if (particlesCount > 4096000) {
                  return ValidationResult.error("Particles exceeds max length 4096000");
               }

               posxxxxxxx += VarInt.size(particlesCount);

               for (int i = 0; i < particlesCount; i++) {
                  ValidationResult structResult = ModelParticle.validateStructure(buffer, posxxxxxxx);
                  if (!structResult.isValid()) {
                     return ValidationResult.error("Invalid ModelParticle in Particles[" + i + "]: " + structResult.error());
                  }

                  posxxxxxxx += ModelParticle.computeBytesConsumed(buffer, posxxxxxxx);
               }
            }

            if ((nullBits[1] & 8) != 0) {
               v = buffer.getIntLE(offset + 87);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for Trails");
               }

               int posxxxxxxxx = offset + 99 + v;
               int trailsCount = VarInt.peek(buffer, posxxxxxxxx);
               if (trailsCount < 0) {
                  return ValidationResult.error("Invalid array count for Trails");
               }

               if (trailsCount > 4096000) {
                  return ValidationResult.error("Trails exceeds max length 4096000");
               }

               posxxxxxxxx += VarInt.size(trailsCount);

               for (int i = 0; i < trailsCount; i++) {
                  ValidationResult structResult = ModelTrail.validateStructure(buffer, posxxxxxxxx);
                  if (!structResult.isValid()) {
                     return ValidationResult.error("Invalid ModelTrail in Trails[" + i + "]: " + structResult.error());
                  }

                  posxxxxxxxx += ModelTrail.computeBytesConsumed(buffer, posxxxxxxxx);
               }
            }

            if ((nullBits[1] & 16) != 0) {
               v = buffer.getIntLE(offset + 91);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for DetailBoxes");
               }

               int posxxxxxxxxx = offset + 99 + v;
               int detailBoxesCount = VarInt.peek(buffer, posxxxxxxxxx);
               if (detailBoxesCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for DetailBoxes");
               }

               if (detailBoxesCount > 4096000) {
                  return ValidationResult.error("DetailBoxes exceeds max length 4096000");
               }

               posxxxxxxxxx += VarInt.size(detailBoxesCount);

               for (int i = 0; i < detailBoxesCount; i++) {
                  int keyLenx = VarInt.peek(buffer, posxxxxxxxxx);
                  if (keyLenx < 0) {
                     return ValidationResult.error("Invalid string length for key");
                  }

                  if (keyLenx > 4096000) {
                     return ValidationResult.error("key exceeds max length 4096000");
                  }

                  posxxxxxxxxx += VarInt.size(keyLenx);
                  posxxxxxxxxx += keyLenx;
                  if (posxxxxxxxxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  int valueArrCount = VarInt.peek(buffer, posxxxxxxxxx);
                  if (valueArrCount < 0) {
                     return ValidationResult.error("Invalid array count for value");
                  }

                  posxxxxxxxxx += VarInt.size(valueArrCount);

                  for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                     posxxxxxxxxx += 37;
                  }
               }
            }

            if ((nullBits[1] & 32) != 0) {
               v = buffer.getIntLE(offset + 95);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for PhobiaModel");
               }

               int posxxxxxxxxxx = offset + 99 + v;
               ValidationResult phobiaModelResult = validateStructure(buffer, posxxxxxxxxxx);
               if (!phobiaModelResult.isValid()) {
                  return ValidationResult.error("Invalid PhobiaModel: " + phobiaModelResult.error());
               }

               posxxxxxxxxxx += computeBytesConsumed(buffer, posxxxxxxxxxx);
            }

            return ValidationResult.OK;
         }
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
