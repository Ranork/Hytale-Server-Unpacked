package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerSkin {
   public static final int NULLABLE_BIT_FIELD_SIZE = 3;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 20;
   public static final int VARIABLE_BLOCK_START = 83;
   public static final int MAX_SIZE = 327680183;
   @Nullable
   public String bodyCharacteristic;
   @Nullable
   public String underwear;
   @Nullable
   public String face;
   @Nullable
   public String eyes;
   @Nullable
   public String ears;
   @Nullable
   public String mouth;
   @Nullable
   public String facialHair;
   @Nullable
   public String haircut;
   @Nullable
   public String eyebrows;
   @Nullable
   public String pants;
   @Nullable
   public String overpants;
   @Nullable
   public String undertop;
   @Nullable
   public String overtop;
   @Nullable
   public String shoes;
   @Nullable
   public String headAccessory;
   @Nullable
   public String faceAccessory;
   @Nullable
   public String earAccessory;
   @Nullable
   public String skinFeature;
   @Nullable
   public String gloves;
   @Nullable
   public String cape;

   public PlayerSkin() {
   }

   public PlayerSkin(
      @Nullable String bodyCharacteristic,
      @Nullable String underwear,
      @Nullable String face,
      @Nullable String eyes,
      @Nullable String ears,
      @Nullable String mouth,
      @Nullable String facialHair,
      @Nullable String haircut,
      @Nullable String eyebrows,
      @Nullable String pants,
      @Nullable String overpants,
      @Nullable String undertop,
      @Nullable String overtop,
      @Nullable String shoes,
      @Nullable String headAccessory,
      @Nullable String faceAccessory,
      @Nullable String earAccessory,
      @Nullable String skinFeature,
      @Nullable String gloves,
      @Nullable String cape
   ) {
      this.bodyCharacteristic = bodyCharacteristic;
      this.underwear = underwear;
      this.face = face;
      this.eyes = eyes;
      this.ears = ears;
      this.mouth = mouth;
      this.facialHair = facialHair;
      this.haircut = haircut;
      this.eyebrows = eyebrows;
      this.pants = pants;
      this.overpants = overpants;
      this.undertop = undertop;
      this.overtop = overtop;
      this.shoes = shoes;
      this.headAccessory = headAccessory;
      this.faceAccessory = faceAccessory;
      this.earAccessory = earAccessory;
      this.skinFeature = skinFeature;
      this.gloves = gloves;
      this.cape = cape;
   }

   public PlayerSkin(@Nonnull PlayerSkin other) {
      this.bodyCharacteristic = other.bodyCharacteristic;
      this.underwear = other.underwear;
      this.face = other.face;
      this.eyes = other.eyes;
      this.ears = other.ears;
      this.mouth = other.mouth;
      this.facialHair = other.facialHair;
      this.haircut = other.haircut;
      this.eyebrows = other.eyebrows;
      this.pants = other.pants;
      this.overpants = other.overpants;
      this.undertop = other.undertop;
      this.overtop = other.overtop;
      this.shoes = other.shoes;
      this.headAccessory = other.headAccessory;
      this.faceAccessory = other.faceAccessory;
      this.earAccessory = other.earAccessory;
      this.skinFeature = other.skinFeature;
      this.gloves = other.gloves;
      this.cape = other.cape;
   }

   @Nonnull
   public static PlayerSkin deserialize(@Nonnull ByteBuf buf, int offset) {
      PlayerSkin obj = new PlayerSkin();
      byte[] nullBits = PacketIO.readBytes(buf, offset, 3);
      if ((nullBits[0] & 1) != 0) {
         int varPos0 = offset + 83 + buf.getIntLE(offset + 3);
         int bodyCharacteristicLen = VarInt.peek(buf, varPos0);
         if (bodyCharacteristicLen < 0) {
            throw ProtocolException.negativeLength("BodyCharacteristic", bodyCharacteristicLen);
         }

         if (bodyCharacteristicLen > 4096000) {
            throw ProtocolException.stringTooLong("BodyCharacteristic", bodyCharacteristicLen, 4096000);
         }

         obj.bodyCharacteristic = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits[0] & 2) != 0) {
         int varPos1 = offset + 83 + buf.getIntLE(offset + 7);
         int underwearLen = VarInt.peek(buf, varPos1);
         if (underwearLen < 0) {
            throw ProtocolException.negativeLength("Underwear", underwearLen);
         }

         if (underwearLen > 4096000) {
            throw ProtocolException.stringTooLong("Underwear", underwearLen, 4096000);
         }

         obj.underwear = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits[0] & 4) != 0) {
         int varPos2 = offset + 83 + buf.getIntLE(offset + 11);
         int faceLen = VarInt.peek(buf, varPos2);
         if (faceLen < 0) {
            throw ProtocolException.negativeLength("Face", faceLen);
         }

         if (faceLen > 4096000) {
            throw ProtocolException.stringTooLong("Face", faceLen, 4096000);
         }

         obj.face = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      if ((nullBits[0] & 8) != 0) {
         int varPos3 = offset + 83 + buf.getIntLE(offset + 15);
         int eyesLen = VarInt.peek(buf, varPos3);
         if (eyesLen < 0) {
            throw ProtocolException.negativeLength("Eyes", eyesLen);
         }

         if (eyesLen > 4096000) {
            throw ProtocolException.stringTooLong("Eyes", eyesLen, 4096000);
         }

         obj.eyes = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
      }

      if ((nullBits[0] & 16) != 0) {
         int varPos4 = offset + 83 + buf.getIntLE(offset + 19);
         int earsLen = VarInt.peek(buf, varPos4);
         if (earsLen < 0) {
            throw ProtocolException.negativeLength("Ears", earsLen);
         }

         if (earsLen > 4096000) {
            throw ProtocolException.stringTooLong("Ears", earsLen, 4096000);
         }

         obj.ears = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
      }

      if ((nullBits[0] & 32) != 0) {
         int varPos5 = offset + 83 + buf.getIntLE(offset + 23);
         int mouthLen = VarInt.peek(buf, varPos5);
         if (mouthLen < 0) {
            throw ProtocolException.negativeLength("Mouth", mouthLen);
         }

         if (mouthLen > 4096000) {
            throw ProtocolException.stringTooLong("Mouth", mouthLen, 4096000);
         }

         obj.mouth = PacketIO.readVarString(buf, varPos5, PacketIO.UTF8);
      }

      if ((nullBits[0] & 64) != 0) {
         int varPos6 = offset + 83 + buf.getIntLE(offset + 27);
         int facialHairLen = VarInt.peek(buf, varPos6);
         if (facialHairLen < 0) {
            throw ProtocolException.negativeLength("FacialHair", facialHairLen);
         }

         if (facialHairLen > 4096000) {
            throw ProtocolException.stringTooLong("FacialHair", facialHairLen, 4096000);
         }

         obj.facialHair = PacketIO.readVarString(buf, varPos6, PacketIO.UTF8);
      }

      if ((nullBits[0] & 128) != 0) {
         int varPos7 = offset + 83 + buf.getIntLE(offset + 31);
         int haircutLen = VarInt.peek(buf, varPos7);
         if (haircutLen < 0) {
            throw ProtocolException.negativeLength("Haircut", haircutLen);
         }

         if (haircutLen > 4096000) {
            throw ProtocolException.stringTooLong("Haircut", haircutLen, 4096000);
         }

         obj.haircut = PacketIO.readVarString(buf, varPos7, PacketIO.UTF8);
      }

      if ((nullBits[1] & 1) != 0) {
         int varPos8 = offset + 83 + buf.getIntLE(offset + 35);
         int eyebrowsLen = VarInt.peek(buf, varPos8);
         if (eyebrowsLen < 0) {
            throw ProtocolException.negativeLength("Eyebrows", eyebrowsLen);
         }

         if (eyebrowsLen > 4096000) {
            throw ProtocolException.stringTooLong("Eyebrows", eyebrowsLen, 4096000);
         }

         obj.eyebrows = PacketIO.readVarString(buf, varPos8, PacketIO.UTF8);
      }

      if ((nullBits[1] & 2) != 0) {
         int varPos9 = offset + 83 + buf.getIntLE(offset + 39);
         int pantsLen = VarInt.peek(buf, varPos9);
         if (pantsLen < 0) {
            throw ProtocolException.negativeLength("Pants", pantsLen);
         }

         if (pantsLen > 4096000) {
            throw ProtocolException.stringTooLong("Pants", pantsLen, 4096000);
         }

         obj.pants = PacketIO.readVarString(buf, varPos9, PacketIO.UTF8);
      }

      if ((nullBits[1] & 4) != 0) {
         int varPos10 = offset + 83 + buf.getIntLE(offset + 43);
         int overpantsLen = VarInt.peek(buf, varPos10);
         if (overpantsLen < 0) {
            throw ProtocolException.negativeLength("Overpants", overpantsLen);
         }

         if (overpantsLen > 4096000) {
            throw ProtocolException.stringTooLong("Overpants", overpantsLen, 4096000);
         }

         obj.overpants = PacketIO.readVarString(buf, varPos10, PacketIO.UTF8);
      }

      if ((nullBits[1] & 8) != 0) {
         int varPos11 = offset + 83 + buf.getIntLE(offset + 47);
         int undertopLen = VarInt.peek(buf, varPos11);
         if (undertopLen < 0) {
            throw ProtocolException.negativeLength("Undertop", undertopLen);
         }

         if (undertopLen > 4096000) {
            throw ProtocolException.stringTooLong("Undertop", undertopLen, 4096000);
         }

         obj.undertop = PacketIO.readVarString(buf, varPos11, PacketIO.UTF8);
      }

      if ((nullBits[1] & 16) != 0) {
         int varPos12 = offset + 83 + buf.getIntLE(offset + 51);
         int overtopLen = VarInt.peek(buf, varPos12);
         if (overtopLen < 0) {
            throw ProtocolException.negativeLength("Overtop", overtopLen);
         }

         if (overtopLen > 4096000) {
            throw ProtocolException.stringTooLong("Overtop", overtopLen, 4096000);
         }

         obj.overtop = PacketIO.readVarString(buf, varPos12, PacketIO.UTF8);
      }

      if ((nullBits[1] & 32) != 0) {
         int varPos13 = offset + 83 + buf.getIntLE(offset + 55);
         int shoesLen = VarInt.peek(buf, varPos13);
         if (shoesLen < 0) {
            throw ProtocolException.negativeLength("Shoes", shoesLen);
         }

         if (shoesLen > 4096000) {
            throw ProtocolException.stringTooLong("Shoes", shoesLen, 4096000);
         }

         obj.shoes = PacketIO.readVarString(buf, varPos13, PacketIO.UTF8);
      }

      if ((nullBits[1] & 64) != 0) {
         int varPos14 = offset + 83 + buf.getIntLE(offset + 59);
         int headAccessoryLen = VarInt.peek(buf, varPos14);
         if (headAccessoryLen < 0) {
            throw ProtocolException.negativeLength("HeadAccessory", headAccessoryLen);
         }

         if (headAccessoryLen > 4096000) {
            throw ProtocolException.stringTooLong("HeadAccessory", headAccessoryLen, 4096000);
         }

         obj.headAccessory = PacketIO.readVarString(buf, varPos14, PacketIO.UTF8);
      }

      if ((nullBits[1] & 128) != 0) {
         int varPos15 = offset + 83 + buf.getIntLE(offset + 63);
         int faceAccessoryLen = VarInt.peek(buf, varPos15);
         if (faceAccessoryLen < 0) {
            throw ProtocolException.negativeLength("FaceAccessory", faceAccessoryLen);
         }

         if (faceAccessoryLen > 4096000) {
            throw ProtocolException.stringTooLong("FaceAccessory", faceAccessoryLen, 4096000);
         }

         obj.faceAccessory = PacketIO.readVarString(buf, varPos15, PacketIO.UTF8);
      }

      if ((nullBits[2] & 1) != 0) {
         int varPos16 = offset + 83 + buf.getIntLE(offset + 67);
         int earAccessoryLen = VarInt.peek(buf, varPos16);
         if (earAccessoryLen < 0) {
            throw ProtocolException.negativeLength("EarAccessory", earAccessoryLen);
         }

         if (earAccessoryLen > 4096000) {
            throw ProtocolException.stringTooLong("EarAccessory", earAccessoryLen, 4096000);
         }

         obj.earAccessory = PacketIO.readVarString(buf, varPos16, PacketIO.UTF8);
      }

      if ((nullBits[2] & 2) != 0) {
         int varPos17 = offset + 83 + buf.getIntLE(offset + 71);
         int skinFeatureLen = VarInt.peek(buf, varPos17);
         if (skinFeatureLen < 0) {
            throw ProtocolException.negativeLength("SkinFeature", skinFeatureLen);
         }

         if (skinFeatureLen > 4096000) {
            throw ProtocolException.stringTooLong("SkinFeature", skinFeatureLen, 4096000);
         }

         obj.skinFeature = PacketIO.readVarString(buf, varPos17, PacketIO.UTF8);
      }

      if ((nullBits[2] & 4) != 0) {
         int varPos18 = offset + 83 + buf.getIntLE(offset + 75);
         int glovesLen = VarInt.peek(buf, varPos18);
         if (glovesLen < 0) {
            throw ProtocolException.negativeLength("Gloves", glovesLen);
         }

         if (glovesLen > 4096000) {
            throw ProtocolException.stringTooLong("Gloves", glovesLen, 4096000);
         }

         obj.gloves = PacketIO.readVarString(buf, varPos18, PacketIO.UTF8);
      }

      if ((nullBits[2] & 8) != 0) {
         int varPos19 = offset + 83 + buf.getIntLE(offset + 79);
         int capeLen = VarInt.peek(buf, varPos19);
         if (capeLen < 0) {
            throw ProtocolException.negativeLength("Cape", capeLen);
         }

         if (capeLen > 4096000) {
            throw ProtocolException.stringTooLong("Cape", capeLen, 4096000);
         }

         obj.cape = PacketIO.readVarString(buf, varPos19, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 3);
      int maxEnd = 83;
      if ((nullBits[0] & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 3);
         int pos0 = offset + 83 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[0] & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 7);
         int pos1 = offset + 83 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[0] & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 11);
         int pos2 = offset + 83 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[0] & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 15);
         int pos3 = offset + 83 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[0] & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 19);
         int pos4 = offset + 83 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.length(buf, pos4) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[0] & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 23);
         int pos5 = offset + 83 + fieldOffset5;
         int sl = VarInt.peek(buf, pos5);
         pos5 += VarInt.length(buf, pos5) + sl;
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[0] & 64) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 27);
         int pos6 = offset + 83 + fieldOffset6;
         int sl = VarInt.peek(buf, pos6);
         pos6 += VarInt.length(buf, pos6) + sl;
         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 31);
         int pos7 = offset + 83 + fieldOffset7;
         int sl = VarInt.peek(buf, pos7);
         pos7 += VarInt.length(buf, pos7) + sl;
         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset8 = buf.getIntLE(offset + 35);
         int pos8 = offset + 83 + fieldOffset8;
         int sl = VarInt.peek(buf, pos8);
         pos8 += VarInt.length(buf, pos8) + sl;
         if (pos8 - offset > maxEnd) {
            maxEnd = pos8 - offset;
         }
      }

      if ((nullBits[1] & 2) != 0) {
         int fieldOffset9 = buf.getIntLE(offset + 39);
         int pos9 = offset + 83 + fieldOffset9;
         int sl = VarInt.peek(buf, pos9);
         pos9 += VarInt.length(buf, pos9) + sl;
         if (pos9 - offset > maxEnd) {
            maxEnd = pos9 - offset;
         }
      }

      if ((nullBits[1] & 4) != 0) {
         int fieldOffset10 = buf.getIntLE(offset + 43);
         int pos10 = offset + 83 + fieldOffset10;
         int sl = VarInt.peek(buf, pos10);
         pos10 += VarInt.length(buf, pos10) + sl;
         if (pos10 - offset > maxEnd) {
            maxEnd = pos10 - offset;
         }
      }

      if ((nullBits[1] & 8) != 0) {
         int fieldOffset11 = buf.getIntLE(offset + 47);
         int pos11 = offset + 83 + fieldOffset11;
         int sl = VarInt.peek(buf, pos11);
         pos11 += VarInt.length(buf, pos11) + sl;
         if (pos11 - offset > maxEnd) {
            maxEnd = pos11 - offset;
         }
      }

      if ((nullBits[1] & 16) != 0) {
         int fieldOffset12 = buf.getIntLE(offset + 51);
         int pos12 = offset + 83 + fieldOffset12;
         int sl = VarInt.peek(buf, pos12);
         pos12 += VarInt.length(buf, pos12) + sl;
         if (pos12 - offset > maxEnd) {
            maxEnd = pos12 - offset;
         }
      }

      if ((nullBits[1] & 32) != 0) {
         int fieldOffset13 = buf.getIntLE(offset + 55);
         int pos13 = offset + 83 + fieldOffset13;
         int sl = VarInt.peek(buf, pos13);
         pos13 += VarInt.length(buf, pos13) + sl;
         if (pos13 - offset > maxEnd) {
            maxEnd = pos13 - offset;
         }
      }

      if ((nullBits[1] & 64) != 0) {
         int fieldOffset14 = buf.getIntLE(offset + 59);
         int pos14 = offset + 83 + fieldOffset14;
         int sl = VarInt.peek(buf, pos14);
         pos14 += VarInt.length(buf, pos14) + sl;
         if (pos14 - offset > maxEnd) {
            maxEnd = pos14 - offset;
         }
      }

      if ((nullBits[1] & 128) != 0) {
         int fieldOffset15 = buf.getIntLE(offset + 63);
         int pos15 = offset + 83 + fieldOffset15;
         int sl = VarInt.peek(buf, pos15);
         pos15 += VarInt.length(buf, pos15) + sl;
         if (pos15 - offset > maxEnd) {
            maxEnd = pos15 - offset;
         }
      }

      if ((nullBits[2] & 1) != 0) {
         int fieldOffset16 = buf.getIntLE(offset + 67);
         int pos16 = offset + 83 + fieldOffset16;
         int sl = VarInt.peek(buf, pos16);
         pos16 += VarInt.length(buf, pos16) + sl;
         if (pos16 - offset > maxEnd) {
            maxEnd = pos16 - offset;
         }
      }

      if ((nullBits[2] & 2) != 0) {
         int fieldOffset17 = buf.getIntLE(offset + 71);
         int pos17 = offset + 83 + fieldOffset17;
         int sl = VarInt.peek(buf, pos17);
         pos17 += VarInt.length(buf, pos17) + sl;
         if (pos17 - offset > maxEnd) {
            maxEnd = pos17 - offset;
         }
      }

      if ((nullBits[2] & 4) != 0) {
         int fieldOffset18 = buf.getIntLE(offset + 75);
         int pos18 = offset + 83 + fieldOffset18;
         int sl = VarInt.peek(buf, pos18);
         pos18 += VarInt.length(buf, pos18) + sl;
         if (pos18 - offset > maxEnd) {
            maxEnd = pos18 - offset;
         }
      }

      if ((nullBits[2] & 8) != 0) {
         int fieldOffset19 = buf.getIntLE(offset + 79);
         int pos19 = offset + 83 + fieldOffset19;
         int sl = VarInt.peek(buf, pos19);
         pos19 += VarInt.length(buf, pos19) + sl;
         if (pos19 - offset > maxEnd) {
            maxEnd = pos19 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[3];
      if (this.bodyCharacteristic != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.underwear != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.face != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.eyes != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.ears != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.mouth != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.facialHair != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.haircut != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.eyebrows != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      if (this.pants != null) {
         nullBits[1] = (byte)(nullBits[1] | 2);
      }

      if (this.overpants != null) {
         nullBits[1] = (byte)(nullBits[1] | 4);
      }

      if (this.undertop != null) {
         nullBits[1] = (byte)(nullBits[1] | 8);
      }

      if (this.overtop != null) {
         nullBits[1] = (byte)(nullBits[1] | 16);
      }

      if (this.shoes != null) {
         nullBits[1] = (byte)(nullBits[1] | 32);
      }

      if (this.headAccessory != null) {
         nullBits[1] = (byte)(nullBits[1] | 64);
      }

      if (this.faceAccessory != null) {
         nullBits[1] = (byte)(nullBits[1] | 128);
      }

      if (this.earAccessory != null) {
         nullBits[2] = (byte)(nullBits[2] | 1);
      }

      if (this.skinFeature != null) {
         nullBits[2] = (byte)(nullBits[2] | 2);
      }

      if (this.gloves != null) {
         nullBits[2] = (byte)(nullBits[2] | 4);
      }

      if (this.cape != null) {
         nullBits[2] = (byte)(nullBits[2] | 8);
      }

      buf.writeBytes(nullBits);
      int bodyCharacteristicOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int underwearOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int faceOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int eyesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int earsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int mouthOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int facialHairOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int haircutOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int eyebrowsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int pantsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int overpantsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int undertopOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int overtopOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int shoesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int headAccessoryOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int faceAccessoryOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int earAccessoryOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int skinFeatureOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int glovesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int capeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.bodyCharacteristic != null) {
         buf.setIntLE(bodyCharacteristicOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.bodyCharacteristic, 4096000);
      } else {
         buf.setIntLE(bodyCharacteristicOffsetSlot, -1);
      }

      if (this.underwear != null) {
         buf.setIntLE(underwearOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.underwear, 4096000);
      } else {
         buf.setIntLE(underwearOffsetSlot, -1);
      }

      if (this.face != null) {
         buf.setIntLE(faceOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.face, 4096000);
      } else {
         buf.setIntLE(faceOffsetSlot, -1);
      }

      if (this.eyes != null) {
         buf.setIntLE(eyesOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.eyes, 4096000);
      } else {
         buf.setIntLE(eyesOffsetSlot, -1);
      }

      if (this.ears != null) {
         buf.setIntLE(earsOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.ears, 4096000);
      } else {
         buf.setIntLE(earsOffsetSlot, -1);
      }

      if (this.mouth != null) {
         buf.setIntLE(mouthOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.mouth, 4096000);
      } else {
         buf.setIntLE(mouthOffsetSlot, -1);
      }

      if (this.facialHair != null) {
         buf.setIntLE(facialHairOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.facialHair, 4096000);
      } else {
         buf.setIntLE(facialHairOffsetSlot, -1);
      }

      if (this.haircut != null) {
         buf.setIntLE(haircutOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.haircut, 4096000);
      } else {
         buf.setIntLE(haircutOffsetSlot, -1);
      }

      if (this.eyebrows != null) {
         buf.setIntLE(eyebrowsOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.eyebrows, 4096000);
      } else {
         buf.setIntLE(eyebrowsOffsetSlot, -1);
      }

      if (this.pants != null) {
         buf.setIntLE(pantsOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.pants, 4096000);
      } else {
         buf.setIntLE(pantsOffsetSlot, -1);
      }

      if (this.overpants != null) {
         buf.setIntLE(overpantsOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.overpants, 4096000);
      } else {
         buf.setIntLE(overpantsOffsetSlot, -1);
      }

      if (this.undertop != null) {
         buf.setIntLE(undertopOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.undertop, 4096000);
      } else {
         buf.setIntLE(undertopOffsetSlot, -1);
      }

      if (this.overtop != null) {
         buf.setIntLE(overtopOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.overtop, 4096000);
      } else {
         buf.setIntLE(overtopOffsetSlot, -1);
      }

      if (this.shoes != null) {
         buf.setIntLE(shoesOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.shoes, 4096000);
      } else {
         buf.setIntLE(shoesOffsetSlot, -1);
      }

      if (this.headAccessory != null) {
         buf.setIntLE(headAccessoryOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.headAccessory, 4096000);
      } else {
         buf.setIntLE(headAccessoryOffsetSlot, -1);
      }

      if (this.faceAccessory != null) {
         buf.setIntLE(faceAccessoryOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.faceAccessory, 4096000);
      } else {
         buf.setIntLE(faceAccessoryOffsetSlot, -1);
      }

      if (this.earAccessory != null) {
         buf.setIntLE(earAccessoryOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.earAccessory, 4096000);
      } else {
         buf.setIntLE(earAccessoryOffsetSlot, -1);
      }

      if (this.skinFeature != null) {
         buf.setIntLE(skinFeatureOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.skinFeature, 4096000);
      } else {
         buf.setIntLE(skinFeatureOffsetSlot, -1);
      }

      if (this.gloves != null) {
         buf.setIntLE(glovesOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.gloves, 4096000);
      } else {
         buf.setIntLE(glovesOffsetSlot, -1);
      }

      if (this.cape != null) {
         buf.setIntLE(capeOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.cape, 4096000);
      } else {
         buf.setIntLE(capeOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 83;
      if (this.bodyCharacteristic != null) {
         size += PacketIO.stringSize(this.bodyCharacteristic);
      }

      if (this.underwear != null) {
         size += PacketIO.stringSize(this.underwear);
      }

      if (this.face != null) {
         size += PacketIO.stringSize(this.face);
      }

      if (this.eyes != null) {
         size += PacketIO.stringSize(this.eyes);
      }

      if (this.ears != null) {
         size += PacketIO.stringSize(this.ears);
      }

      if (this.mouth != null) {
         size += PacketIO.stringSize(this.mouth);
      }

      if (this.facialHair != null) {
         size += PacketIO.stringSize(this.facialHair);
      }

      if (this.haircut != null) {
         size += PacketIO.stringSize(this.haircut);
      }

      if (this.eyebrows != null) {
         size += PacketIO.stringSize(this.eyebrows);
      }

      if (this.pants != null) {
         size += PacketIO.stringSize(this.pants);
      }

      if (this.overpants != null) {
         size += PacketIO.stringSize(this.overpants);
      }

      if (this.undertop != null) {
         size += PacketIO.stringSize(this.undertop);
      }

      if (this.overtop != null) {
         size += PacketIO.stringSize(this.overtop);
      }

      if (this.shoes != null) {
         size += PacketIO.stringSize(this.shoes);
      }

      if (this.headAccessory != null) {
         size += PacketIO.stringSize(this.headAccessory);
      }

      if (this.faceAccessory != null) {
         size += PacketIO.stringSize(this.faceAccessory);
      }

      if (this.earAccessory != null) {
         size += PacketIO.stringSize(this.earAccessory);
      }

      if (this.skinFeature != null) {
         size += PacketIO.stringSize(this.skinFeature);
      }

      if (this.gloves != null) {
         size += PacketIO.stringSize(this.gloves);
      }

      if (this.cape != null) {
         size += PacketIO.stringSize(this.cape);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 83) {
         return ValidationResult.error("Buffer too small: expected at least 83 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 3);
         if ((nullBits[0] & 1) != 0) {
            int bodyCharacteristicOffset = buffer.getIntLE(offset + 3);
            if (bodyCharacteristicOffset < 0) {
               return ValidationResult.error("Invalid offset for BodyCharacteristic");
            }

            int pos = offset + 83 + bodyCharacteristicOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for BodyCharacteristic");
            }

            int bodyCharacteristicLen = VarInt.peek(buffer, pos);
            if (bodyCharacteristicLen < 0) {
               return ValidationResult.error("Invalid string length for BodyCharacteristic");
            }

            if (bodyCharacteristicLen > 4096000) {
               return ValidationResult.error("BodyCharacteristic exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += bodyCharacteristicLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading BodyCharacteristic");
            }
         }

         if ((nullBits[0] & 2) != 0) {
            int underwearOffset = buffer.getIntLE(offset + 7);
            if (underwearOffset < 0) {
               return ValidationResult.error("Invalid offset for Underwear");
            }

            int posx = offset + 83 + underwearOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Underwear");
            }

            int underwearLen = VarInt.peek(buffer, posx);
            if (underwearLen < 0) {
               return ValidationResult.error("Invalid string length for Underwear");
            }

            if (underwearLen > 4096000) {
               return ValidationResult.error("Underwear exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += underwearLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Underwear");
            }
         }

         if ((nullBits[0] & 4) != 0) {
            int faceOffset = buffer.getIntLE(offset + 11);
            if (faceOffset < 0) {
               return ValidationResult.error("Invalid offset for Face");
            }

            int posxx = offset + 83 + faceOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Face");
            }

            int faceLen = VarInt.peek(buffer, posxx);
            if (faceLen < 0) {
               return ValidationResult.error("Invalid string length for Face");
            }

            if (faceLen > 4096000) {
               return ValidationResult.error("Face exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += faceLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Face");
            }
         }

         if ((nullBits[0] & 8) != 0) {
            int eyesOffset = buffer.getIntLE(offset + 15);
            if (eyesOffset < 0) {
               return ValidationResult.error("Invalid offset for Eyes");
            }

            int posxxx = offset + 83 + eyesOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Eyes");
            }

            int eyesLen = VarInt.peek(buffer, posxxx);
            if (eyesLen < 0) {
               return ValidationResult.error("Invalid string length for Eyes");
            }

            if (eyesLen > 4096000) {
               return ValidationResult.error("Eyes exceeds max length 4096000");
            }

            posxxx += VarInt.length(buffer, posxxx);
            posxxx += eyesLen;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Eyes");
            }
         }

         if ((nullBits[0] & 16) != 0) {
            int earsOffset = buffer.getIntLE(offset + 19);
            if (earsOffset < 0) {
               return ValidationResult.error("Invalid offset for Ears");
            }

            int posxxxx = offset + 83 + earsOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Ears");
            }

            int earsLen = VarInt.peek(buffer, posxxxx);
            if (earsLen < 0) {
               return ValidationResult.error("Invalid string length for Ears");
            }

            if (earsLen > 4096000) {
               return ValidationResult.error("Ears exceeds max length 4096000");
            }

            posxxxx += VarInt.length(buffer, posxxxx);
            posxxxx += earsLen;
            if (posxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Ears");
            }
         }

         if ((nullBits[0] & 32) != 0) {
            int mouthOffset = buffer.getIntLE(offset + 23);
            if (mouthOffset < 0) {
               return ValidationResult.error("Invalid offset for Mouth");
            }

            int posxxxxx = offset + 83 + mouthOffset;
            if (posxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Mouth");
            }

            int mouthLen = VarInt.peek(buffer, posxxxxx);
            if (mouthLen < 0) {
               return ValidationResult.error("Invalid string length for Mouth");
            }

            if (mouthLen > 4096000) {
               return ValidationResult.error("Mouth exceeds max length 4096000");
            }

            posxxxxx += VarInt.length(buffer, posxxxxx);
            posxxxxx += mouthLen;
            if (posxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Mouth");
            }
         }

         if ((nullBits[0] & 64) != 0) {
            int facialHairOffset = buffer.getIntLE(offset + 27);
            if (facialHairOffset < 0) {
               return ValidationResult.error("Invalid offset for FacialHair");
            }

            int posxxxxxx = offset + 83 + facialHairOffset;
            if (posxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FacialHair");
            }

            int facialHairLen = VarInt.peek(buffer, posxxxxxx);
            if (facialHairLen < 0) {
               return ValidationResult.error("Invalid string length for FacialHair");
            }

            if (facialHairLen > 4096000) {
               return ValidationResult.error("FacialHair exceeds max length 4096000");
            }

            posxxxxxx += VarInt.length(buffer, posxxxxxx);
            posxxxxxx += facialHairLen;
            if (posxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FacialHair");
            }
         }

         if ((nullBits[0] & 128) != 0) {
            int haircutOffset = buffer.getIntLE(offset + 31);
            if (haircutOffset < 0) {
               return ValidationResult.error("Invalid offset for Haircut");
            }

            int posxxxxxxx = offset + 83 + haircutOffset;
            if (posxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Haircut");
            }

            int haircutLen = VarInt.peek(buffer, posxxxxxxx);
            if (haircutLen < 0) {
               return ValidationResult.error("Invalid string length for Haircut");
            }

            if (haircutLen > 4096000) {
               return ValidationResult.error("Haircut exceeds max length 4096000");
            }

            posxxxxxxx += VarInt.length(buffer, posxxxxxxx);
            posxxxxxxx += haircutLen;
            if (posxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Haircut");
            }
         }

         if ((nullBits[1] & 1) != 0) {
            int eyebrowsOffset = buffer.getIntLE(offset + 35);
            if (eyebrowsOffset < 0) {
               return ValidationResult.error("Invalid offset for Eyebrows");
            }

            int posxxxxxxxx = offset + 83 + eyebrowsOffset;
            if (posxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Eyebrows");
            }

            int eyebrowsLen = VarInt.peek(buffer, posxxxxxxxx);
            if (eyebrowsLen < 0) {
               return ValidationResult.error("Invalid string length for Eyebrows");
            }

            if (eyebrowsLen > 4096000) {
               return ValidationResult.error("Eyebrows exceeds max length 4096000");
            }

            posxxxxxxxx += VarInt.length(buffer, posxxxxxxxx);
            posxxxxxxxx += eyebrowsLen;
            if (posxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Eyebrows");
            }
         }

         if ((nullBits[1] & 2) != 0) {
            int pantsOffset = buffer.getIntLE(offset + 39);
            if (pantsOffset < 0) {
               return ValidationResult.error("Invalid offset for Pants");
            }

            int posxxxxxxxxx = offset + 83 + pantsOffset;
            if (posxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Pants");
            }

            int pantsLen = VarInt.peek(buffer, posxxxxxxxxx);
            if (pantsLen < 0) {
               return ValidationResult.error("Invalid string length for Pants");
            }

            if (pantsLen > 4096000) {
               return ValidationResult.error("Pants exceeds max length 4096000");
            }

            posxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxx);
            posxxxxxxxxx += pantsLen;
            if (posxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Pants");
            }
         }

         if ((nullBits[1] & 4) != 0) {
            int overpantsOffset = buffer.getIntLE(offset + 43);
            if (overpantsOffset < 0) {
               return ValidationResult.error("Invalid offset for Overpants");
            }

            int posxxxxxxxxxx = offset + 83 + overpantsOffset;
            if (posxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Overpants");
            }

            int overpantsLen = VarInt.peek(buffer, posxxxxxxxxxx);
            if (overpantsLen < 0) {
               return ValidationResult.error("Invalid string length for Overpants");
            }

            if (overpantsLen > 4096000) {
               return ValidationResult.error("Overpants exceeds max length 4096000");
            }

            posxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxx);
            posxxxxxxxxxx += overpantsLen;
            if (posxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Overpants");
            }
         }

         if ((nullBits[1] & 8) != 0) {
            int undertopOffset = buffer.getIntLE(offset + 47);
            if (undertopOffset < 0) {
               return ValidationResult.error("Invalid offset for Undertop");
            }

            int posxxxxxxxxxxx = offset + 83 + undertopOffset;
            if (posxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Undertop");
            }

            int undertopLen = VarInt.peek(buffer, posxxxxxxxxxxx);
            if (undertopLen < 0) {
               return ValidationResult.error("Invalid string length for Undertop");
            }

            if (undertopLen > 4096000) {
               return ValidationResult.error("Undertop exceeds max length 4096000");
            }

            posxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxx);
            posxxxxxxxxxxx += undertopLen;
            if (posxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Undertop");
            }
         }

         if ((nullBits[1] & 16) != 0) {
            int overtopOffset = buffer.getIntLE(offset + 51);
            if (overtopOffset < 0) {
               return ValidationResult.error("Invalid offset for Overtop");
            }

            int posxxxxxxxxxxxx = offset + 83 + overtopOffset;
            if (posxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Overtop");
            }

            int overtopLen = VarInt.peek(buffer, posxxxxxxxxxxxx);
            if (overtopLen < 0) {
               return ValidationResult.error("Invalid string length for Overtop");
            }

            if (overtopLen > 4096000) {
               return ValidationResult.error("Overtop exceeds max length 4096000");
            }

            posxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxx);
            posxxxxxxxxxxxx += overtopLen;
            if (posxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Overtop");
            }
         }

         if ((nullBits[1] & 32) != 0) {
            int shoesOffset = buffer.getIntLE(offset + 55);
            if (shoesOffset < 0) {
               return ValidationResult.error("Invalid offset for Shoes");
            }

            int posxxxxxxxxxxxxx = offset + 83 + shoesOffset;
            if (posxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Shoes");
            }

            int shoesLen = VarInt.peek(buffer, posxxxxxxxxxxxxx);
            if (shoesLen < 0) {
               return ValidationResult.error("Invalid string length for Shoes");
            }

            if (shoesLen > 4096000) {
               return ValidationResult.error("Shoes exceeds max length 4096000");
            }

            posxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxx);
            posxxxxxxxxxxxxx += shoesLen;
            if (posxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Shoes");
            }
         }

         if ((nullBits[1] & 64) != 0) {
            int headAccessoryOffset = buffer.getIntLE(offset + 59);
            if (headAccessoryOffset < 0) {
               return ValidationResult.error("Invalid offset for HeadAccessory");
            }

            int posxxxxxxxxxxxxxx = offset + 83 + headAccessoryOffset;
            if (posxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for HeadAccessory");
            }

            int headAccessoryLen = VarInt.peek(buffer, posxxxxxxxxxxxxxx);
            if (headAccessoryLen < 0) {
               return ValidationResult.error("Invalid string length for HeadAccessory");
            }

            if (headAccessoryLen > 4096000) {
               return ValidationResult.error("HeadAccessory exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxx);
            posxxxxxxxxxxxxxx += headAccessoryLen;
            if (posxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading HeadAccessory");
            }
         }

         if ((nullBits[1] & 128) != 0) {
            int faceAccessoryOffset = buffer.getIntLE(offset + 63);
            if (faceAccessoryOffset < 0) {
               return ValidationResult.error("Invalid offset for FaceAccessory");
            }

            int posxxxxxxxxxxxxxxx = offset + 83 + faceAccessoryOffset;
            if (posxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FaceAccessory");
            }

            int faceAccessoryLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxx);
            if (faceAccessoryLen < 0) {
               return ValidationResult.error("Invalid string length for FaceAccessory");
            }

            if (faceAccessoryLen > 4096000) {
               return ValidationResult.error("FaceAccessory exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxx);
            posxxxxxxxxxxxxxxx += faceAccessoryLen;
            if (posxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FaceAccessory");
            }
         }

         if ((nullBits[2] & 1) != 0) {
            int earAccessoryOffset = buffer.getIntLE(offset + 67);
            if (earAccessoryOffset < 0) {
               return ValidationResult.error("Invalid offset for EarAccessory");
            }

            int posxxxxxxxxxxxxxxxx = offset + 83 + earAccessoryOffset;
            if (posxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for EarAccessory");
            }

            int earAccessoryLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxxx);
            if (earAccessoryLen < 0) {
               return ValidationResult.error("Invalid string length for EarAccessory");
            }

            if (earAccessoryLen > 4096000) {
               return ValidationResult.error("EarAccessory exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxx);
            posxxxxxxxxxxxxxxxx += earAccessoryLen;
            if (posxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading EarAccessory");
            }
         }

         if ((nullBits[2] & 2) != 0) {
            int skinFeatureOffset = buffer.getIntLE(offset + 71);
            if (skinFeatureOffset < 0) {
               return ValidationResult.error("Invalid offset for SkinFeature");
            }

            int posxxxxxxxxxxxxxxxxx = offset + 83 + skinFeatureOffset;
            if (posxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SkinFeature");
            }

            int skinFeatureLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxx);
            if (skinFeatureLen < 0) {
               return ValidationResult.error("Invalid string length for SkinFeature");
            }

            if (skinFeatureLen > 4096000) {
               return ValidationResult.error("SkinFeature exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxx);
            posxxxxxxxxxxxxxxxxx += skinFeatureLen;
            if (posxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading SkinFeature");
            }
         }

         if ((nullBits[2] & 4) != 0) {
            int glovesOffset = buffer.getIntLE(offset + 75);
            if (glovesOffset < 0) {
               return ValidationResult.error("Invalid offset for Gloves");
            }

            int posxxxxxxxxxxxxxxxxxx = offset + 83 + glovesOffset;
            if (posxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Gloves");
            }

            int glovesLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxx);
            if (glovesLen < 0) {
               return ValidationResult.error("Invalid string length for Gloves");
            }

            if (glovesLen > 4096000) {
               return ValidationResult.error("Gloves exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxx);
            posxxxxxxxxxxxxxxxxxx += glovesLen;
            if (posxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Gloves");
            }
         }

         if ((nullBits[2] & 8) != 0) {
            int capeOffset = buffer.getIntLE(offset + 79);
            if (capeOffset < 0) {
               return ValidationResult.error("Invalid offset for Cape");
            }

            int posxxxxxxxxxxxxxxxxxxx = offset + 83 + capeOffset;
            if (posxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Cape");
            }

            int capeLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxx);
            if (capeLen < 0) {
               return ValidationResult.error("Invalid string length for Cape");
            }

            if (capeLen > 4096000) {
               return ValidationResult.error("Cape exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxx);
            posxxxxxxxxxxxxxxxxxxx += capeLen;
            if (posxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Cape");
            }
         }

         return ValidationResult.OK;
      }
   }

   public PlayerSkin clone() {
      PlayerSkin copy = new PlayerSkin();
      copy.bodyCharacteristic = this.bodyCharacteristic;
      copy.underwear = this.underwear;
      copy.face = this.face;
      copy.eyes = this.eyes;
      copy.ears = this.ears;
      copy.mouth = this.mouth;
      copy.facialHair = this.facialHair;
      copy.haircut = this.haircut;
      copy.eyebrows = this.eyebrows;
      copy.pants = this.pants;
      copy.overpants = this.overpants;
      copy.undertop = this.undertop;
      copy.overtop = this.overtop;
      copy.shoes = this.shoes;
      copy.headAccessory = this.headAccessory;
      copy.faceAccessory = this.faceAccessory;
      copy.earAccessory = this.earAccessory;
      copy.skinFeature = this.skinFeature;
      copy.gloves = this.gloves;
      copy.cape = this.cape;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof PlayerSkin other)
            ? false
            : Objects.equals(this.bodyCharacteristic, other.bodyCharacteristic)
               && Objects.equals(this.underwear, other.underwear)
               && Objects.equals(this.face, other.face)
               && Objects.equals(this.eyes, other.eyes)
               && Objects.equals(this.ears, other.ears)
               && Objects.equals(this.mouth, other.mouth)
               && Objects.equals(this.facialHair, other.facialHair)
               && Objects.equals(this.haircut, other.haircut)
               && Objects.equals(this.eyebrows, other.eyebrows)
               && Objects.equals(this.pants, other.pants)
               && Objects.equals(this.overpants, other.overpants)
               && Objects.equals(this.undertop, other.undertop)
               && Objects.equals(this.overtop, other.overtop)
               && Objects.equals(this.shoes, other.shoes)
               && Objects.equals(this.headAccessory, other.headAccessory)
               && Objects.equals(this.faceAccessory, other.faceAccessory)
               && Objects.equals(this.earAccessory, other.earAccessory)
               && Objects.equals(this.skinFeature, other.skinFeature)
               && Objects.equals(this.gloves, other.gloves)
               && Objects.equals(this.cape, other.cape);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.bodyCharacteristic,
         this.underwear,
         this.face,
         this.eyes,
         this.ears,
         this.mouth,
         this.facialHair,
         this.haircut,
         this.eyebrows,
         this.pants,
         this.overpants,
         this.undertop,
         this.overtop,
         this.shoes,
         this.headAccessory,
         this.faceAccessory,
         this.earAccessory,
         this.skinFeature,
         this.gloves,
         this.cape
      );
   }
}
