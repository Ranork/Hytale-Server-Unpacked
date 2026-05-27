package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerSkin {
   public static final int NULLABLE_BIT_FIELD_SIZE = 3;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 20;
   public static final int VARIABLE_BLOCK_START = 83;
   public static final int MAX_SIZE = 2103;
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
      if (buf.readableBytes() - offset < 83) {
         throw ProtocolException.bufferTooSmall("PlayerSkin", 83, buf.readableBytes() - offset);
      } else {
         PlayerSkin obj = new PlayerSkin();
         byte[] nullBits = PacketIO.readBytes(buf, offset, 3);
         if ((nullBits[0] & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 3);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("BodyCharacteristic", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 83 + varPosBase0;
            int bodyCharacteristicLen = VarInt.peek(buf, varPos0);
            if (bodyCharacteristicLen < 0) {
               throw ProtocolException.invalidVarInt("BodyCharacteristic");
            }

            int bodyCharacteristicVarIntLen = VarInt.size(bodyCharacteristicLen);
            if (bodyCharacteristicLen > 96) {
               throw ProtocolException.stringTooLong("BodyCharacteristic", bodyCharacteristicLen, 96);
            }

            if (varPos0 + bodyCharacteristicVarIntLen + bodyCharacteristicLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("BodyCharacteristic", varPos0 + bodyCharacteristicVarIntLen + bodyCharacteristicLen, buf.readableBytes());
            }

            obj.bodyCharacteristic = PacketIO.readValidatedAsciiString(buf, varPos0 + bodyCharacteristicVarIntLen, bodyCharacteristicLen, "BodyCharacteristic");
         }

         if ((nullBits[0] & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 7);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("Underwear", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 83 + varPosBase1;
            int underwearLen = VarInt.peek(buf, varPos1);
            if (underwearLen < 0) {
               throw ProtocolException.invalidVarInt("Underwear");
            }

            int underwearVarIntLen = VarInt.size(underwearLen);
            if (underwearLen > 96) {
               throw ProtocolException.stringTooLong("Underwear", underwearLen, 96);
            }

            if (varPos1 + underwearVarIntLen + underwearLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Underwear", varPos1 + underwearVarIntLen + underwearLen, buf.readableBytes());
            }

            obj.underwear = PacketIO.readValidatedAsciiString(buf, varPos1 + underwearVarIntLen, underwearLen, "Underwear");
         }

         if ((nullBits[0] & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 11);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("Face", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 83 + varPosBase2;
            int faceLen = VarInt.peek(buf, varPos2);
            if (faceLen < 0) {
               throw ProtocolException.invalidVarInt("Face");
            }

            int faceVarIntLen = VarInt.size(faceLen);
            if (faceLen > 96) {
               throw ProtocolException.stringTooLong("Face", faceLen, 96);
            }

            if (varPos2 + faceVarIntLen + faceLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Face", varPos2 + faceVarIntLen + faceLen, buf.readableBytes());
            }

            obj.face = PacketIO.readValidatedAsciiString(buf, varPos2 + faceVarIntLen, faceLen, "Face");
         }

         if ((nullBits[0] & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 15);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("Eyes", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 83 + varPosBase3;
            int eyesLen = VarInt.peek(buf, varPos3);
            if (eyesLen < 0) {
               throw ProtocolException.invalidVarInt("Eyes");
            }

            int eyesVarIntLen = VarInt.size(eyesLen);
            if (eyesLen > 96) {
               throw ProtocolException.stringTooLong("Eyes", eyesLen, 96);
            }

            if (varPos3 + eyesVarIntLen + eyesLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Eyes", varPos3 + eyesVarIntLen + eyesLen, buf.readableBytes());
            }

            obj.eyes = PacketIO.readValidatedAsciiString(buf, varPos3 + eyesVarIntLen, eyesLen, "Eyes");
         }

         if ((nullBits[0] & 16) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 19);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("Ears", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 83 + varPosBase4;
            int earsLen = VarInt.peek(buf, varPos4);
            if (earsLen < 0) {
               throw ProtocolException.invalidVarInt("Ears");
            }

            int earsVarIntLen = VarInt.size(earsLen);
            if (earsLen > 96) {
               throw ProtocolException.stringTooLong("Ears", earsLen, 96);
            }

            if (varPos4 + earsVarIntLen + earsLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Ears", varPos4 + earsVarIntLen + earsLen, buf.readableBytes());
            }

            obj.ears = PacketIO.readValidatedAsciiString(buf, varPos4 + earsVarIntLen, earsLen, "Ears");
         }

         if ((nullBits[0] & 32) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 23);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("Mouth", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 83 + varPosBase5;
            int mouthLen = VarInt.peek(buf, varPos5);
            if (mouthLen < 0) {
               throw ProtocolException.invalidVarInt("Mouth");
            }

            int mouthVarIntLen = VarInt.size(mouthLen);
            if (mouthLen > 96) {
               throw ProtocolException.stringTooLong("Mouth", mouthLen, 96);
            }

            if (varPos5 + mouthVarIntLen + mouthLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Mouth", varPos5 + mouthVarIntLen + mouthLen, buf.readableBytes());
            }

            obj.mouth = PacketIO.readValidatedAsciiString(buf, varPos5 + mouthVarIntLen, mouthLen, "Mouth");
         }

         if ((nullBits[0] & 64) != 0) {
            int varPosBase6 = buf.getIntLE(offset + 27);
            if (varPosBase6 < 0 || varPosBase6 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("FacialHair", varPosBase6, buf.readableBytes());
            }

            int varPos6 = offset + 83 + varPosBase6;
            int facialHairLen = VarInt.peek(buf, varPos6);
            if (facialHairLen < 0) {
               throw ProtocolException.invalidVarInt("FacialHair");
            }

            int facialHairVarIntLen = VarInt.size(facialHairLen);
            if (facialHairLen > 96) {
               throw ProtocolException.stringTooLong("FacialHair", facialHairLen, 96);
            }

            if (varPos6 + facialHairVarIntLen + facialHairLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("FacialHair", varPos6 + facialHairVarIntLen + facialHairLen, buf.readableBytes());
            }

            obj.facialHair = PacketIO.readValidatedAsciiString(buf, varPos6 + facialHairVarIntLen, facialHairLen, "FacialHair");
         }

         if ((nullBits[0] & 128) != 0) {
            int varPosBase7 = buf.getIntLE(offset + 31);
            if (varPosBase7 < 0 || varPosBase7 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("Haircut", varPosBase7, buf.readableBytes());
            }

            int varPos7 = offset + 83 + varPosBase7;
            int haircutLen = VarInt.peek(buf, varPos7);
            if (haircutLen < 0) {
               throw ProtocolException.invalidVarInt("Haircut");
            }

            int haircutVarIntLen = VarInt.size(haircutLen);
            if (haircutLen > 96) {
               throw ProtocolException.stringTooLong("Haircut", haircutLen, 96);
            }

            if (varPos7 + haircutVarIntLen + haircutLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Haircut", varPos7 + haircutVarIntLen + haircutLen, buf.readableBytes());
            }

            obj.haircut = PacketIO.readValidatedAsciiString(buf, varPos7 + haircutVarIntLen, haircutLen, "Haircut");
         }

         if ((nullBits[1] & 1) != 0) {
            int varPosBase8 = buf.getIntLE(offset + 35);
            if (varPosBase8 < 0 || varPosBase8 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("Eyebrows", varPosBase8, buf.readableBytes());
            }

            int varPos8 = offset + 83 + varPosBase8;
            int eyebrowsLen = VarInt.peek(buf, varPos8);
            if (eyebrowsLen < 0) {
               throw ProtocolException.invalidVarInt("Eyebrows");
            }

            int eyebrowsVarIntLen = VarInt.size(eyebrowsLen);
            if (eyebrowsLen > 96) {
               throw ProtocolException.stringTooLong("Eyebrows", eyebrowsLen, 96);
            }

            if (varPos8 + eyebrowsVarIntLen + eyebrowsLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Eyebrows", varPos8 + eyebrowsVarIntLen + eyebrowsLen, buf.readableBytes());
            }

            obj.eyebrows = PacketIO.readValidatedAsciiString(buf, varPos8 + eyebrowsVarIntLen, eyebrowsLen, "Eyebrows");
         }

         if ((nullBits[1] & 2) != 0) {
            int varPosBase9 = buf.getIntLE(offset + 39);
            if (varPosBase9 < 0 || varPosBase9 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("Pants", varPosBase9, buf.readableBytes());
            }

            int varPos9 = offset + 83 + varPosBase9;
            int pantsLen = VarInt.peek(buf, varPos9);
            if (pantsLen < 0) {
               throw ProtocolException.invalidVarInt("Pants");
            }

            int pantsVarIntLen = VarInt.size(pantsLen);
            if (pantsLen > 96) {
               throw ProtocolException.stringTooLong("Pants", pantsLen, 96);
            }

            if (varPos9 + pantsVarIntLen + pantsLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Pants", varPos9 + pantsVarIntLen + pantsLen, buf.readableBytes());
            }

            obj.pants = PacketIO.readValidatedAsciiString(buf, varPos9 + pantsVarIntLen, pantsLen, "Pants");
         }

         if ((nullBits[1] & 4) != 0) {
            int varPosBase10 = buf.getIntLE(offset + 43);
            if (varPosBase10 < 0 || varPosBase10 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("Overpants", varPosBase10, buf.readableBytes());
            }

            int varPos10 = offset + 83 + varPosBase10;
            int overpantsLen = VarInt.peek(buf, varPos10);
            if (overpantsLen < 0) {
               throw ProtocolException.invalidVarInt("Overpants");
            }

            int overpantsVarIntLen = VarInt.size(overpantsLen);
            if (overpantsLen > 96) {
               throw ProtocolException.stringTooLong("Overpants", overpantsLen, 96);
            }

            if (varPos10 + overpantsVarIntLen + overpantsLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Overpants", varPos10 + overpantsVarIntLen + overpantsLen, buf.readableBytes());
            }

            obj.overpants = PacketIO.readValidatedAsciiString(buf, varPos10 + overpantsVarIntLen, overpantsLen, "Overpants");
         }

         if ((nullBits[1] & 8) != 0) {
            int varPosBase11 = buf.getIntLE(offset + 47);
            if (varPosBase11 < 0 || varPosBase11 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("Undertop", varPosBase11, buf.readableBytes());
            }

            int varPos11 = offset + 83 + varPosBase11;
            int undertopLen = VarInt.peek(buf, varPos11);
            if (undertopLen < 0) {
               throw ProtocolException.invalidVarInt("Undertop");
            }

            int undertopVarIntLen = VarInt.size(undertopLen);
            if (undertopLen > 96) {
               throw ProtocolException.stringTooLong("Undertop", undertopLen, 96);
            }

            if (varPos11 + undertopVarIntLen + undertopLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Undertop", varPos11 + undertopVarIntLen + undertopLen, buf.readableBytes());
            }

            obj.undertop = PacketIO.readValidatedAsciiString(buf, varPos11 + undertopVarIntLen, undertopLen, "Undertop");
         }

         if ((nullBits[1] & 16) != 0) {
            int varPosBase12 = buf.getIntLE(offset + 51);
            if (varPosBase12 < 0 || varPosBase12 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("Overtop", varPosBase12, buf.readableBytes());
            }

            int varPos12 = offset + 83 + varPosBase12;
            int overtopLen = VarInt.peek(buf, varPos12);
            if (overtopLen < 0) {
               throw ProtocolException.invalidVarInt("Overtop");
            }

            int overtopVarIntLen = VarInt.size(overtopLen);
            if (overtopLen > 96) {
               throw ProtocolException.stringTooLong("Overtop", overtopLen, 96);
            }

            if (varPos12 + overtopVarIntLen + overtopLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Overtop", varPos12 + overtopVarIntLen + overtopLen, buf.readableBytes());
            }

            obj.overtop = PacketIO.readValidatedAsciiString(buf, varPos12 + overtopVarIntLen, overtopLen, "Overtop");
         }

         if ((nullBits[1] & 32) != 0) {
            int varPosBase13 = buf.getIntLE(offset + 55);
            if (varPosBase13 < 0 || varPosBase13 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("Shoes", varPosBase13, buf.readableBytes());
            }

            int varPos13 = offset + 83 + varPosBase13;
            int shoesLen = VarInt.peek(buf, varPos13);
            if (shoesLen < 0) {
               throw ProtocolException.invalidVarInt("Shoes");
            }

            int shoesVarIntLen = VarInt.size(shoesLen);
            if (shoesLen > 96) {
               throw ProtocolException.stringTooLong("Shoes", shoesLen, 96);
            }

            if (varPos13 + shoesVarIntLen + shoesLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Shoes", varPos13 + shoesVarIntLen + shoesLen, buf.readableBytes());
            }

            obj.shoes = PacketIO.readValidatedAsciiString(buf, varPos13 + shoesVarIntLen, shoesLen, "Shoes");
         }

         if ((nullBits[1] & 64) != 0) {
            int varPosBase14 = buf.getIntLE(offset + 59);
            if (varPosBase14 < 0 || varPosBase14 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("HeadAccessory", varPosBase14, buf.readableBytes());
            }

            int varPos14 = offset + 83 + varPosBase14;
            int headAccessoryLen = VarInt.peek(buf, varPos14);
            if (headAccessoryLen < 0) {
               throw ProtocolException.invalidVarInt("HeadAccessory");
            }

            int headAccessoryVarIntLen = VarInt.size(headAccessoryLen);
            if (headAccessoryLen > 96) {
               throw ProtocolException.stringTooLong("HeadAccessory", headAccessoryLen, 96);
            }

            if (varPos14 + headAccessoryVarIntLen + headAccessoryLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("HeadAccessory", varPos14 + headAccessoryVarIntLen + headAccessoryLen, buf.readableBytes());
            }

            obj.headAccessory = PacketIO.readValidatedAsciiString(buf, varPos14 + headAccessoryVarIntLen, headAccessoryLen, "HeadAccessory");
         }

         if ((nullBits[1] & 128) != 0) {
            int varPosBase15 = buf.getIntLE(offset + 63);
            if (varPosBase15 < 0 || varPosBase15 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("FaceAccessory", varPosBase15, buf.readableBytes());
            }

            int varPos15 = offset + 83 + varPosBase15;
            int faceAccessoryLen = VarInt.peek(buf, varPos15);
            if (faceAccessoryLen < 0) {
               throw ProtocolException.invalidVarInt("FaceAccessory");
            }

            int faceAccessoryVarIntLen = VarInt.size(faceAccessoryLen);
            if (faceAccessoryLen > 96) {
               throw ProtocolException.stringTooLong("FaceAccessory", faceAccessoryLen, 96);
            }

            if (varPos15 + faceAccessoryVarIntLen + faceAccessoryLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("FaceAccessory", varPos15 + faceAccessoryVarIntLen + faceAccessoryLen, buf.readableBytes());
            }

            obj.faceAccessory = PacketIO.readValidatedAsciiString(buf, varPos15 + faceAccessoryVarIntLen, faceAccessoryLen, "FaceAccessory");
         }

         if ((nullBits[2] & 1) != 0) {
            int varPosBase16 = buf.getIntLE(offset + 67);
            if (varPosBase16 < 0 || varPosBase16 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("EarAccessory", varPosBase16, buf.readableBytes());
            }

            int varPos16 = offset + 83 + varPosBase16;
            int earAccessoryLen = VarInt.peek(buf, varPos16);
            if (earAccessoryLen < 0) {
               throw ProtocolException.invalidVarInt("EarAccessory");
            }

            int earAccessoryVarIntLen = VarInt.size(earAccessoryLen);
            if (earAccessoryLen > 96) {
               throw ProtocolException.stringTooLong("EarAccessory", earAccessoryLen, 96);
            }

            if (varPos16 + earAccessoryVarIntLen + earAccessoryLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("EarAccessory", varPos16 + earAccessoryVarIntLen + earAccessoryLen, buf.readableBytes());
            }

            obj.earAccessory = PacketIO.readValidatedAsciiString(buf, varPos16 + earAccessoryVarIntLen, earAccessoryLen, "EarAccessory");
         }

         if ((nullBits[2] & 2) != 0) {
            int varPosBase17 = buf.getIntLE(offset + 71);
            if (varPosBase17 < 0 || varPosBase17 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("SkinFeature", varPosBase17, buf.readableBytes());
            }

            int varPos17 = offset + 83 + varPosBase17;
            int skinFeatureLen = VarInt.peek(buf, varPos17);
            if (skinFeatureLen < 0) {
               throw ProtocolException.invalidVarInt("SkinFeature");
            }

            int skinFeatureVarIntLen = VarInt.size(skinFeatureLen);
            if (skinFeatureLen > 96) {
               throw ProtocolException.stringTooLong("SkinFeature", skinFeatureLen, 96);
            }

            if (varPos17 + skinFeatureVarIntLen + skinFeatureLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("SkinFeature", varPos17 + skinFeatureVarIntLen + skinFeatureLen, buf.readableBytes());
            }

            obj.skinFeature = PacketIO.readValidatedAsciiString(buf, varPos17 + skinFeatureVarIntLen, skinFeatureLen, "SkinFeature");
         }

         if ((nullBits[2] & 4) != 0) {
            int varPosBase18 = buf.getIntLE(offset + 75);
            if (varPosBase18 < 0 || varPosBase18 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("Gloves", varPosBase18, buf.readableBytes());
            }

            int varPos18 = offset + 83 + varPosBase18;
            int glovesLen = VarInt.peek(buf, varPos18);
            if (glovesLen < 0) {
               throw ProtocolException.invalidVarInt("Gloves");
            }

            int glovesVarIntLen = VarInt.size(glovesLen);
            if (glovesLen > 96) {
               throw ProtocolException.stringTooLong("Gloves", glovesLen, 96);
            }

            if (varPos18 + glovesVarIntLen + glovesLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Gloves", varPos18 + glovesVarIntLen + glovesLen, buf.readableBytes());
            }

            obj.gloves = PacketIO.readValidatedAsciiString(buf, varPos18 + glovesVarIntLen, glovesLen, "Gloves");
         }

         if ((nullBits[2] & 8) != 0) {
            int varPosBase19 = buf.getIntLE(offset + 79);
            if (varPosBase19 < 0 || varPosBase19 > buf.writerIndex() - offset - 83) {
               throw ProtocolException.invalidOffset("Cape", varPosBase19, buf.readableBytes());
            }

            int varPos19 = offset + 83 + varPosBase19;
            int capeLen = VarInt.peek(buf, varPos19);
            if (capeLen < 0) {
               throw ProtocolException.invalidVarInt("Cape");
            }

            int capeVarIntLen = VarInt.size(capeLen);
            if (capeLen > 96) {
               throw ProtocolException.stringTooLong("Cape", capeLen, 96);
            }

            if (varPos19 + capeVarIntLen + capeLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Cape", varPos19 + capeVarIntLen + capeLen, buf.readableBytes());
            }

            obj.cape = PacketIO.readValidatedAsciiString(buf, varPos19 + capeVarIntLen, capeLen, "Cape");
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 3);
      int maxEnd = 83;
      if ((nullBits[0] & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 3);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("BodyCharacteristic", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 83 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[0] & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 7);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("Underwear", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 83 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[0] & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 11);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("Face", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 83 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[0] & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 15);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("Eyes", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 83 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(sl) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[0] & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 19);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("Ears", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 83 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(sl) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[0] & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 23);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("Mouth", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 83 + fieldOffset5;
         int sl = VarInt.peek(buf, pos5);
         pos5 += VarInt.size(sl) + sl;
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[0] & 64) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 27);
         if (fieldOffset6 < 0 || fieldOffset6 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("FacialHair", fieldOffset6, maxEnd);
         }

         int pos6 = offset + 83 + fieldOffset6;
         int sl = VarInt.peek(buf, pos6);
         pos6 += VarInt.size(sl) + sl;
         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 31);
         if (fieldOffset7 < 0 || fieldOffset7 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("Haircut", fieldOffset7, maxEnd);
         }

         int pos7 = offset + 83 + fieldOffset7;
         int sl = VarInt.peek(buf, pos7);
         pos7 += VarInt.size(sl) + sl;
         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset8 = buf.getIntLE(offset + 35);
         if (fieldOffset8 < 0 || fieldOffset8 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("Eyebrows", fieldOffset8, maxEnd);
         }

         int pos8 = offset + 83 + fieldOffset8;
         int sl = VarInt.peek(buf, pos8);
         pos8 += VarInt.size(sl) + sl;
         if (pos8 - offset > maxEnd) {
            maxEnd = pos8 - offset;
         }
      }

      if ((nullBits[1] & 2) != 0) {
         int fieldOffset9 = buf.getIntLE(offset + 39);
         if (fieldOffset9 < 0 || fieldOffset9 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("Pants", fieldOffset9, maxEnd);
         }

         int pos9 = offset + 83 + fieldOffset9;
         int sl = VarInt.peek(buf, pos9);
         pos9 += VarInt.size(sl) + sl;
         if (pos9 - offset > maxEnd) {
            maxEnd = pos9 - offset;
         }
      }

      if ((nullBits[1] & 4) != 0) {
         int fieldOffset10 = buf.getIntLE(offset + 43);
         if (fieldOffset10 < 0 || fieldOffset10 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("Overpants", fieldOffset10, maxEnd);
         }

         int pos10 = offset + 83 + fieldOffset10;
         int sl = VarInt.peek(buf, pos10);
         pos10 += VarInt.size(sl) + sl;
         if (pos10 - offset > maxEnd) {
            maxEnd = pos10 - offset;
         }
      }

      if ((nullBits[1] & 8) != 0) {
         int fieldOffset11 = buf.getIntLE(offset + 47);
         if (fieldOffset11 < 0 || fieldOffset11 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("Undertop", fieldOffset11, maxEnd);
         }

         int pos11 = offset + 83 + fieldOffset11;
         int sl = VarInt.peek(buf, pos11);
         pos11 += VarInt.size(sl) + sl;
         if (pos11 - offset > maxEnd) {
            maxEnd = pos11 - offset;
         }
      }

      if ((nullBits[1] & 16) != 0) {
         int fieldOffset12 = buf.getIntLE(offset + 51);
         if (fieldOffset12 < 0 || fieldOffset12 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("Overtop", fieldOffset12, maxEnd);
         }

         int pos12 = offset + 83 + fieldOffset12;
         int sl = VarInt.peek(buf, pos12);
         pos12 += VarInt.size(sl) + sl;
         if (pos12 - offset > maxEnd) {
            maxEnd = pos12 - offset;
         }
      }

      if ((nullBits[1] & 32) != 0) {
         int fieldOffset13 = buf.getIntLE(offset + 55);
         if (fieldOffset13 < 0 || fieldOffset13 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("Shoes", fieldOffset13, maxEnd);
         }

         int pos13 = offset + 83 + fieldOffset13;
         int sl = VarInt.peek(buf, pos13);
         pos13 += VarInt.size(sl) + sl;
         if (pos13 - offset > maxEnd) {
            maxEnd = pos13 - offset;
         }
      }

      if ((nullBits[1] & 64) != 0) {
         int fieldOffset14 = buf.getIntLE(offset + 59);
         if (fieldOffset14 < 0 || fieldOffset14 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("HeadAccessory", fieldOffset14, maxEnd);
         }

         int pos14 = offset + 83 + fieldOffset14;
         int sl = VarInt.peek(buf, pos14);
         pos14 += VarInt.size(sl) + sl;
         if (pos14 - offset > maxEnd) {
            maxEnd = pos14 - offset;
         }
      }

      if ((nullBits[1] & 128) != 0) {
         int fieldOffset15 = buf.getIntLE(offset + 63);
         if (fieldOffset15 < 0 || fieldOffset15 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("FaceAccessory", fieldOffset15, maxEnd);
         }

         int pos15 = offset + 83 + fieldOffset15;
         int sl = VarInt.peek(buf, pos15);
         pos15 += VarInt.size(sl) + sl;
         if (pos15 - offset > maxEnd) {
            maxEnd = pos15 - offset;
         }
      }

      if ((nullBits[2] & 1) != 0) {
         int fieldOffset16 = buf.getIntLE(offset + 67);
         if (fieldOffset16 < 0 || fieldOffset16 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("EarAccessory", fieldOffset16, maxEnd);
         }

         int pos16 = offset + 83 + fieldOffset16;
         int sl = VarInt.peek(buf, pos16);
         pos16 += VarInt.size(sl) + sl;
         if (pos16 - offset > maxEnd) {
            maxEnd = pos16 - offset;
         }
      }

      if ((nullBits[2] & 2) != 0) {
         int fieldOffset17 = buf.getIntLE(offset + 71);
         if (fieldOffset17 < 0 || fieldOffset17 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("SkinFeature", fieldOffset17, maxEnd);
         }

         int pos17 = offset + 83 + fieldOffset17;
         int sl = VarInt.peek(buf, pos17);
         pos17 += VarInt.size(sl) + sl;
         if (pos17 - offset > maxEnd) {
            maxEnd = pos17 - offset;
         }
      }

      if ((nullBits[2] & 4) != 0) {
         int fieldOffset18 = buf.getIntLE(offset + 75);
         if (fieldOffset18 < 0 || fieldOffset18 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("Gloves", fieldOffset18, maxEnd);
         }

         int pos18 = offset + 83 + fieldOffset18;
         int sl = VarInt.peek(buf, pos18);
         pos18 += VarInt.size(sl) + sl;
         if (pos18 - offset > maxEnd) {
            maxEnd = pos18 - offset;
         }
      }

      if ((nullBits[2] & 8) != 0) {
         int fieldOffset19 = buf.getIntLE(offset + 79);
         if (fieldOffset19 < 0 || fieldOffset19 > buf.writerIndex() - offset - 83) {
            throw ProtocolException.invalidOffset("Cape", fieldOffset19, maxEnd);
         }

         int pos19 = offset + 83 + fieldOffset19;
         int sl = VarInt.peek(buf, pos19);
         pos19 += VarInt.size(sl) + sl;
         if (pos19 - offset > maxEnd) {
            maxEnd = pos19 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 83L;
   }

   @Nullable
   public static String getBodyCharacteristic(MemorySegment mem) {
      return getBodyCharacteristic(mem, 0);
   }

   @Nullable
   public static String getBodyCharacteristic(MemorySegment mem, int offset) {
      return hasBodyCharacteristic(mem, offset)
         ? PacketIO.readValidatedAsciiString("BodyCharacteristic", mem, offset + getValidatedOffset(mem, offset, 3, 83, "BodyCharacteristic"), 96)
         : null;
   }

   @Nullable
   public static String getUnderwear(MemorySegment mem) {
      return getUnderwear(mem, 0);
   }

   @Nullable
   public static String getUnderwear(MemorySegment mem, int offset) {
      return hasUnderwear(mem, offset)
         ? PacketIO.readValidatedAsciiString("Underwear", mem, offset + getValidatedOffset(mem, offset, 7, 83, "Underwear"), 96)
         : null;
   }

   @Nullable
   public static String getFace(MemorySegment mem) {
      return getFace(mem, 0);
   }

   @Nullable
   public static String getFace(MemorySegment mem, int offset) {
      return hasFace(mem, offset) ? PacketIO.readValidatedAsciiString("Face", mem, offset + getValidatedOffset(mem, offset, 11, 83, "Face"), 96) : null;
   }

   @Nullable
   public static String getEyes(MemorySegment mem) {
      return getEyes(mem, 0);
   }

   @Nullable
   public static String getEyes(MemorySegment mem, int offset) {
      return hasEyes(mem, offset) ? PacketIO.readValidatedAsciiString("Eyes", mem, offset + getValidatedOffset(mem, offset, 15, 83, "Eyes"), 96) : null;
   }

   @Nullable
   public static String getEars(MemorySegment mem) {
      return getEars(mem, 0);
   }

   @Nullable
   public static String getEars(MemorySegment mem, int offset) {
      return hasEars(mem, offset) ? PacketIO.readValidatedAsciiString("Ears", mem, offset + getValidatedOffset(mem, offset, 19, 83, "Ears"), 96) : null;
   }

   @Nullable
   public static String getMouth(MemorySegment mem) {
      return getMouth(mem, 0);
   }

   @Nullable
   public static String getMouth(MemorySegment mem, int offset) {
      return hasMouth(mem, offset) ? PacketIO.readValidatedAsciiString("Mouth", mem, offset + getValidatedOffset(mem, offset, 23, 83, "Mouth"), 96) : null;
   }

   @Nullable
   public static String getFacialHair(MemorySegment mem) {
      return getFacialHair(mem, 0);
   }

   @Nullable
   public static String getFacialHair(MemorySegment mem, int offset) {
      return hasFacialHair(mem, offset)
         ? PacketIO.readValidatedAsciiString("FacialHair", mem, offset + getValidatedOffset(mem, offset, 27, 83, "FacialHair"), 96)
         : null;
   }

   @Nullable
   public static String getHaircut(MemorySegment mem) {
      return getHaircut(mem, 0);
   }

   @Nullable
   public static String getHaircut(MemorySegment mem, int offset) {
      return hasHaircut(mem, offset)
         ? PacketIO.readValidatedAsciiString("Haircut", mem, offset + getValidatedOffset(mem, offset, 31, 83, "Haircut"), 96)
         : null;
   }

   @Nullable
   public static String getEyebrows(MemorySegment mem) {
      return getEyebrows(mem, 0);
   }

   @Nullable
   public static String getEyebrows(MemorySegment mem, int offset) {
      return hasEyebrows(mem, offset)
         ? PacketIO.readValidatedAsciiString("Eyebrows", mem, offset + getValidatedOffset(mem, offset, 35, 83, "Eyebrows"), 96)
         : null;
   }

   @Nullable
   public static String getPants(MemorySegment mem) {
      return getPants(mem, 0);
   }

   @Nullable
   public static String getPants(MemorySegment mem, int offset) {
      return hasPants(mem, offset) ? PacketIO.readValidatedAsciiString("Pants", mem, offset + getValidatedOffset(mem, offset, 39, 83, "Pants"), 96) : null;
   }

   @Nullable
   public static String getOverpants(MemorySegment mem) {
      return getOverpants(mem, 0);
   }

   @Nullable
   public static String getOverpants(MemorySegment mem, int offset) {
      return hasOverpants(mem, offset)
         ? PacketIO.readValidatedAsciiString("Overpants", mem, offset + getValidatedOffset(mem, offset, 43, 83, "Overpants"), 96)
         : null;
   }

   @Nullable
   public static String getUndertop(MemorySegment mem) {
      return getUndertop(mem, 0);
   }

   @Nullable
   public static String getUndertop(MemorySegment mem, int offset) {
      return hasUndertop(mem, offset)
         ? PacketIO.readValidatedAsciiString("Undertop", mem, offset + getValidatedOffset(mem, offset, 47, 83, "Undertop"), 96)
         : null;
   }

   @Nullable
   public static String getOvertop(MemorySegment mem) {
      return getOvertop(mem, 0);
   }

   @Nullable
   public static String getOvertop(MemorySegment mem, int offset) {
      return hasOvertop(mem, offset)
         ? PacketIO.readValidatedAsciiString("Overtop", mem, offset + getValidatedOffset(mem, offset, 51, 83, "Overtop"), 96)
         : null;
   }

   @Nullable
   public static String getShoes(MemorySegment mem) {
      return getShoes(mem, 0);
   }

   @Nullable
   public static String getShoes(MemorySegment mem, int offset) {
      return hasShoes(mem, offset) ? PacketIO.readValidatedAsciiString("Shoes", mem, offset + getValidatedOffset(mem, offset, 55, 83, "Shoes"), 96) : null;
   }

   @Nullable
   public static String getHeadAccessory(MemorySegment mem) {
      return getHeadAccessory(mem, 0);
   }

   @Nullable
   public static String getHeadAccessory(MemorySegment mem, int offset) {
      return hasHeadAccessory(mem, offset)
         ? PacketIO.readValidatedAsciiString("HeadAccessory", mem, offset + getValidatedOffset(mem, offset, 59, 83, "HeadAccessory"), 96)
         : null;
   }

   @Nullable
   public static String getFaceAccessory(MemorySegment mem) {
      return getFaceAccessory(mem, 0);
   }

   @Nullable
   public static String getFaceAccessory(MemorySegment mem, int offset) {
      return hasFaceAccessory(mem, offset)
         ? PacketIO.readValidatedAsciiString("FaceAccessory", mem, offset + getValidatedOffset(mem, offset, 63, 83, "FaceAccessory"), 96)
         : null;
   }

   @Nullable
   public static String getEarAccessory(MemorySegment mem) {
      return getEarAccessory(mem, 0);
   }

   @Nullable
   public static String getEarAccessory(MemorySegment mem, int offset) {
      return hasEarAccessory(mem, offset)
         ? PacketIO.readValidatedAsciiString("EarAccessory", mem, offset + getValidatedOffset(mem, offset, 67, 83, "EarAccessory"), 96)
         : null;
   }

   @Nullable
   public static String getSkinFeature(MemorySegment mem) {
      return getSkinFeature(mem, 0);
   }

   @Nullable
   public static String getSkinFeature(MemorySegment mem, int offset) {
      return hasSkinFeature(mem, offset)
         ? PacketIO.readValidatedAsciiString("SkinFeature", mem, offset + getValidatedOffset(mem, offset, 71, 83, "SkinFeature"), 96)
         : null;
   }

   @Nullable
   public static String getGloves(MemorySegment mem) {
      return getGloves(mem, 0);
   }

   @Nullable
   public static String getGloves(MemorySegment mem, int offset) {
      return hasGloves(mem, offset) ? PacketIO.readValidatedAsciiString("Gloves", mem, offset + getValidatedOffset(mem, offset, 75, 83, "Gloves"), 96) : null;
   }

   @Nullable
   public static String getCape(MemorySegment mem) {
      return getCape(mem, 0);
   }

   @Nullable
   public static String getCape(MemorySegment mem, int offset) {
      return hasCape(mem, offset) ? PacketIO.readValidatedAsciiString("Cape", mem, offset + getValidatedOffset(mem, offset, 79, 83, "Cape"), 96) : null;
   }

   public static boolean hasBodyCharacteristic(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasUnderwear(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasFace(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasEyes(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasEars(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasMouth(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasFacialHair(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasHaircut(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static boolean hasEyebrows(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 1) != 0;
   }

   public static boolean hasPants(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 2) != 0;
   }

   public static boolean hasOverpants(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 4) != 0;
   }

   public static boolean hasUndertop(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 8) != 0;
   }

   public static boolean hasOvertop(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 16) != 0;
   }

   public static boolean hasShoes(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 32) != 0;
   }

   public static boolean hasHeadAccessory(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 64) != 0;
   }

   public static boolean hasFaceAccessory(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 128) != 0;
   }

   public static boolean hasEarAccessory(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 1) != 0;
   }

   public static boolean hasSkinFeature(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 2) != 0;
   }

   public static boolean hasGloves(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 4) != 0;
   }

   public static boolean hasCape(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 8) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static PlayerSkin toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static PlayerSkin toObject(MemorySegment mem, int offset) {
      if (offset + 83 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("PlayerSkin", offset + 83, (int)mem.byteSize());
      } else {
         return new PlayerSkin(
            hasBodyCharacteristic(mem, offset)
               ? PacketIO.readValidatedAsciiString("BodyCharacteristic", mem, offset + getValidatedOffset(mem, offset, 3, 83, "BodyCharacteristic"), 96)
               : null,
            hasUnderwear(mem, offset)
               ? PacketIO.readValidatedAsciiString("Underwear", mem, offset + getValidatedOffset(mem, offset, 7, 83, "Underwear"), 96)
               : null,
            hasFace(mem, offset) ? PacketIO.readValidatedAsciiString("Face", mem, offset + getValidatedOffset(mem, offset, 11, 83, "Face"), 96) : null,
            hasEyes(mem, offset) ? PacketIO.readValidatedAsciiString("Eyes", mem, offset + getValidatedOffset(mem, offset, 15, 83, "Eyes"), 96) : null,
            hasEars(mem, offset) ? PacketIO.readValidatedAsciiString("Ears", mem, offset + getValidatedOffset(mem, offset, 19, 83, "Ears"), 96) : null,
            hasMouth(mem, offset) ? PacketIO.readValidatedAsciiString("Mouth", mem, offset + getValidatedOffset(mem, offset, 23, 83, "Mouth"), 96) : null,
            hasFacialHair(mem, offset)
               ? PacketIO.readValidatedAsciiString("FacialHair", mem, offset + getValidatedOffset(mem, offset, 27, 83, "FacialHair"), 96)
               : null,
            hasHaircut(mem, offset) ? PacketIO.readValidatedAsciiString("Haircut", mem, offset + getValidatedOffset(mem, offset, 31, 83, "Haircut"), 96) : null,
            hasEyebrows(mem, offset)
               ? PacketIO.readValidatedAsciiString("Eyebrows", mem, offset + getValidatedOffset(mem, offset, 35, 83, "Eyebrows"), 96)
               : null,
            hasPants(mem, offset) ? PacketIO.readValidatedAsciiString("Pants", mem, offset + getValidatedOffset(mem, offset, 39, 83, "Pants"), 96) : null,
            hasOverpants(mem, offset)
               ? PacketIO.readValidatedAsciiString("Overpants", mem, offset + getValidatedOffset(mem, offset, 43, 83, "Overpants"), 96)
               : null,
            hasUndertop(mem, offset)
               ? PacketIO.readValidatedAsciiString("Undertop", mem, offset + getValidatedOffset(mem, offset, 47, 83, "Undertop"), 96)
               : null,
            hasOvertop(mem, offset) ? PacketIO.readValidatedAsciiString("Overtop", mem, offset + getValidatedOffset(mem, offset, 51, 83, "Overtop"), 96) : null,
            hasShoes(mem, offset) ? PacketIO.readValidatedAsciiString("Shoes", mem, offset + getValidatedOffset(mem, offset, 55, 83, "Shoes"), 96) : null,
            hasHeadAccessory(mem, offset)
               ? PacketIO.readValidatedAsciiString("HeadAccessory", mem, offset + getValidatedOffset(mem, offset, 59, 83, "HeadAccessory"), 96)
               : null,
            hasFaceAccessory(mem, offset)
               ? PacketIO.readValidatedAsciiString("FaceAccessory", mem, offset + getValidatedOffset(mem, offset, 63, 83, "FaceAccessory"), 96)
               : null,
            hasEarAccessory(mem, offset)
               ? PacketIO.readValidatedAsciiString("EarAccessory", mem, offset + getValidatedOffset(mem, offset, 67, 83, "EarAccessory"), 96)
               : null,
            hasSkinFeature(mem, offset)
               ? PacketIO.readValidatedAsciiString("SkinFeature", mem, offset + getValidatedOffset(mem, offset, 71, 83, "SkinFeature"), 96)
               : null,
            hasGloves(mem, offset) ? PacketIO.readValidatedAsciiString("Gloves", mem, offset + getValidatedOffset(mem, offset, 75, 83, "Gloves"), 96) : null,
            hasCape(mem, offset) ? PacketIO.readValidatedAsciiString("Cape", mem, offset + getValidatedOffset(mem, offset, 79, 83, "Cape"), 96) : null
         );
      }
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
         PacketIO.writeVarAsciiString(buf, this.bodyCharacteristic, 96);
      } else {
         buf.setIntLE(bodyCharacteristicOffsetSlot, -1);
      }

      if (this.underwear != null) {
         buf.setIntLE(underwearOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.underwear, 96);
      } else {
         buf.setIntLE(underwearOffsetSlot, -1);
      }

      if (this.face != null) {
         buf.setIntLE(faceOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.face, 96);
      } else {
         buf.setIntLE(faceOffsetSlot, -1);
      }

      if (this.eyes != null) {
         buf.setIntLE(eyesOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.eyes, 96);
      } else {
         buf.setIntLE(eyesOffsetSlot, -1);
      }

      if (this.ears != null) {
         buf.setIntLE(earsOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.ears, 96);
      } else {
         buf.setIntLE(earsOffsetSlot, -1);
      }

      if (this.mouth != null) {
         buf.setIntLE(mouthOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.mouth, 96);
      } else {
         buf.setIntLE(mouthOffsetSlot, -1);
      }

      if (this.facialHair != null) {
         buf.setIntLE(facialHairOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.facialHair, 96);
      } else {
         buf.setIntLE(facialHairOffsetSlot, -1);
      }

      if (this.haircut != null) {
         buf.setIntLE(haircutOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.haircut, 96);
      } else {
         buf.setIntLE(haircutOffsetSlot, -1);
      }

      if (this.eyebrows != null) {
         buf.setIntLE(eyebrowsOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.eyebrows, 96);
      } else {
         buf.setIntLE(eyebrowsOffsetSlot, -1);
      }

      if (this.pants != null) {
         buf.setIntLE(pantsOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.pants, 96);
      } else {
         buf.setIntLE(pantsOffsetSlot, -1);
      }

      if (this.overpants != null) {
         buf.setIntLE(overpantsOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.overpants, 96);
      } else {
         buf.setIntLE(overpantsOffsetSlot, -1);
      }

      if (this.undertop != null) {
         buf.setIntLE(undertopOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.undertop, 96);
      } else {
         buf.setIntLE(undertopOffsetSlot, -1);
      }

      if (this.overtop != null) {
         buf.setIntLE(overtopOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.overtop, 96);
      } else {
         buf.setIntLE(overtopOffsetSlot, -1);
      }

      if (this.shoes != null) {
         buf.setIntLE(shoesOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.shoes, 96);
      } else {
         buf.setIntLE(shoesOffsetSlot, -1);
      }

      if (this.headAccessory != null) {
         buf.setIntLE(headAccessoryOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.headAccessory, 96);
      } else {
         buf.setIntLE(headAccessoryOffsetSlot, -1);
      }

      if (this.faceAccessory != null) {
         buf.setIntLE(faceAccessoryOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.faceAccessory, 96);
      } else {
         buf.setIntLE(faceAccessoryOffsetSlot, -1);
      }

      if (this.earAccessory != null) {
         buf.setIntLE(earAccessoryOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.earAccessory, 96);
      } else {
         buf.setIntLE(earAccessoryOffsetSlot, -1);
      }

      if (this.skinFeature != null) {
         buf.setIntLE(skinFeatureOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.skinFeature, 96);
      } else {
         buf.setIntLE(skinFeatureOffsetSlot, -1);
      }

      if (this.gloves != null) {
         buf.setIntLE(glovesOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.gloves, 96);
      } else {
         buf.setIntLE(glovesOffsetSlot, -1);
      }

      if (this.cape != null) {
         buf.setIntLE(capeOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarAsciiString(buf, this.cape, 96);
      } else {
         buf.setIntLE(capeOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.bodyCharacteristic != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.underwear != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.face != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.eyes != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.ears != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.mouth != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.facialHair != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.haircut != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      nullBits = 0;
      if (this.eyebrows != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.pants != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.overpants != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.undertop != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.overtop != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.shoes != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.headAccessory != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.faceAccessory != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), nullBits);
      nullBits = 0;
      if (this.earAccessory != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.skinFeature != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.gloves != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.cape != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), nullBits);
      int varOffset = offset + 83;
      if (this.bodyCharacteristic != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 3), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.bodyCharacteristic, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 3), -1);
      }

      if (this.underwear != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 7), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.underwear, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 7), -1);
      }

      if (this.face != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 11), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.face, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 11), -1);
      }

      if (this.eyes != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 15), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.eyes, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 15), -1);
      }

      if (this.ears != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 19), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.ears, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 19), -1);
      }

      if (this.mouth != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 23), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.mouth, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 23), -1);
      }

      if (this.facialHair != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.facialHair, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), -1);
      }

      if (this.haircut != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 31), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.haircut, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 31), -1);
      }

      if (this.eyebrows != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 35), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.eyebrows, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 35), -1);
      }

      if (this.pants != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 39), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.pants, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 39), -1);
      }

      if (this.overpants != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 43), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.overpants, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 43), -1);
      }

      if (this.undertop != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 47), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.undertop, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 47), -1);
      }

      if (this.overtop != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 51), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.overtop, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 51), -1);
      }

      if (this.shoes != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 55), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.shoes, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 55), -1);
      }

      if (this.headAccessory != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 59), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.headAccessory, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 59), -1);
      }

      if (this.faceAccessory != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 63), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.faceAccessory, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 63), -1);
      }

      if (this.earAccessory != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 67), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.earAccessory, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 67), -1);
      }

      if (this.skinFeature != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 71), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.skinFeature, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 71), -1);
      }

      if (this.gloves != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 75), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.gloves, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 75), -1);
      }

      if (this.cape != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 79), varOffset - offset - 83);
         varOffset += PacketIO.writeVarAsciiString(mem, varOffset, this.cape, 96);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 79), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 83;
      if (this.bodyCharacteristic != null) {
         size += VarInt.size(this.bodyCharacteristic.length()) + this.bodyCharacteristic.length();
      }

      if (this.underwear != null) {
         size += VarInt.size(this.underwear.length()) + this.underwear.length();
      }

      if (this.face != null) {
         size += VarInt.size(this.face.length()) + this.face.length();
      }

      if (this.eyes != null) {
         size += VarInt.size(this.eyes.length()) + this.eyes.length();
      }

      if (this.ears != null) {
         size += VarInt.size(this.ears.length()) + this.ears.length();
      }

      if (this.mouth != null) {
         size += VarInt.size(this.mouth.length()) + this.mouth.length();
      }

      if (this.facialHair != null) {
         size += VarInt.size(this.facialHair.length()) + this.facialHair.length();
      }

      if (this.haircut != null) {
         size += VarInt.size(this.haircut.length()) + this.haircut.length();
      }

      if (this.eyebrows != null) {
         size += VarInt.size(this.eyebrows.length()) + this.eyebrows.length();
      }

      if (this.pants != null) {
         size += VarInt.size(this.pants.length()) + this.pants.length();
      }

      if (this.overpants != null) {
         size += VarInt.size(this.overpants.length()) + this.overpants.length();
      }

      if (this.undertop != null) {
         size += VarInt.size(this.undertop.length()) + this.undertop.length();
      }

      if (this.overtop != null) {
         size += VarInt.size(this.overtop.length()) + this.overtop.length();
      }

      if (this.shoes != null) {
         size += VarInt.size(this.shoes.length()) + this.shoes.length();
      }

      if (this.headAccessory != null) {
         size += VarInt.size(this.headAccessory.length()) + this.headAccessory.length();
      }

      if (this.faceAccessory != null) {
         size += VarInt.size(this.faceAccessory.length()) + this.faceAccessory.length();
      }

      if (this.earAccessory != null) {
         size += VarInt.size(this.earAccessory.length()) + this.earAccessory.length();
      }

      if (this.skinFeature != null) {
         size += VarInt.size(this.skinFeature.length()) + this.skinFeature.length();
      }

      if (this.gloves != null) {
         size += VarInt.size(this.gloves.length()) + this.gloves.length();
      }

      if (this.cape != null) {
         size += VarInt.size(this.cape.length()) + this.cape.length();
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
            if (bodyCharacteristicOffset < 0 || bodyCharacteristicOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for BodyCharacteristic");
            }

            int pos = offset + 83 + bodyCharacteristicOffset;
            int bodyCharacteristicLen = VarInt.peek(buffer, pos);
            if (bodyCharacteristicLen < 0) {
               return ValidationResult.error("Invalid string length for BodyCharacteristic");
            }

            if (bodyCharacteristicLen > 96) {
               return ValidationResult.error("BodyCharacteristic exceeds max length 96");
            }

            pos += VarInt.size(bodyCharacteristicLen);
            pos += bodyCharacteristicLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading BodyCharacteristic");
            }

            if (!PacketIO.isValidAscii(buffer, pos - bodyCharacteristicLen, bodyCharacteristicLen)) {
               return ValidationResult.error("BodyCharacteristic contains non-ASCII bytes");
            }
         }

         if ((nullBits[0] & 2) != 0) {
            int underwearOffset = buffer.getIntLE(offset + 7);
            if (underwearOffset < 0 || underwearOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for Underwear");
            }

            int posx = offset + 83 + underwearOffset;
            int underwearLen = VarInt.peek(buffer, posx);
            if (underwearLen < 0) {
               return ValidationResult.error("Invalid string length for Underwear");
            }

            if (underwearLen > 96) {
               return ValidationResult.error("Underwear exceeds max length 96");
            }

            posx += VarInt.size(underwearLen);
            posx += underwearLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Underwear");
            }

            if (!PacketIO.isValidAscii(buffer, posx - underwearLen, underwearLen)) {
               return ValidationResult.error("Underwear contains non-ASCII bytes");
            }
         }

         if ((nullBits[0] & 4) != 0) {
            int faceOffset = buffer.getIntLE(offset + 11);
            if (faceOffset < 0 || faceOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for Face");
            }

            int posxx = offset + 83 + faceOffset;
            int faceLen = VarInt.peek(buffer, posxx);
            if (faceLen < 0) {
               return ValidationResult.error("Invalid string length for Face");
            }

            if (faceLen > 96) {
               return ValidationResult.error("Face exceeds max length 96");
            }

            posxx += VarInt.size(faceLen);
            posxx += faceLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Face");
            }

            if (!PacketIO.isValidAscii(buffer, posxx - faceLen, faceLen)) {
               return ValidationResult.error("Face contains non-ASCII bytes");
            }
         }

         if ((nullBits[0] & 8) != 0) {
            int eyesOffset = buffer.getIntLE(offset + 15);
            if (eyesOffset < 0 || eyesOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for Eyes");
            }

            int posxxx = offset + 83 + eyesOffset;
            int eyesLen = VarInt.peek(buffer, posxxx);
            if (eyesLen < 0) {
               return ValidationResult.error("Invalid string length for Eyes");
            }

            if (eyesLen > 96) {
               return ValidationResult.error("Eyes exceeds max length 96");
            }

            posxxx += VarInt.size(eyesLen);
            posxxx += eyesLen;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Eyes");
            }

            if (!PacketIO.isValidAscii(buffer, posxxx - eyesLen, eyesLen)) {
               return ValidationResult.error("Eyes contains non-ASCII bytes");
            }
         }

         if ((nullBits[0] & 16) != 0) {
            int earsOffset = buffer.getIntLE(offset + 19);
            if (earsOffset < 0 || earsOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for Ears");
            }

            int posxxxx = offset + 83 + earsOffset;
            int earsLen = VarInt.peek(buffer, posxxxx);
            if (earsLen < 0) {
               return ValidationResult.error("Invalid string length for Ears");
            }

            if (earsLen > 96) {
               return ValidationResult.error("Ears exceeds max length 96");
            }

            posxxxx += VarInt.size(earsLen);
            posxxxx += earsLen;
            if (posxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Ears");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxx - earsLen, earsLen)) {
               return ValidationResult.error("Ears contains non-ASCII bytes");
            }
         }

         if ((nullBits[0] & 32) != 0) {
            int mouthOffset = buffer.getIntLE(offset + 23);
            if (mouthOffset < 0 || mouthOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for Mouth");
            }

            int posxxxxx = offset + 83 + mouthOffset;
            int mouthLen = VarInt.peek(buffer, posxxxxx);
            if (mouthLen < 0) {
               return ValidationResult.error("Invalid string length for Mouth");
            }

            if (mouthLen > 96) {
               return ValidationResult.error("Mouth exceeds max length 96");
            }

            posxxxxx += VarInt.size(mouthLen);
            posxxxxx += mouthLen;
            if (posxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Mouth");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxx - mouthLen, mouthLen)) {
               return ValidationResult.error("Mouth contains non-ASCII bytes");
            }
         }

         if ((nullBits[0] & 64) != 0) {
            int facialHairOffset = buffer.getIntLE(offset + 27);
            if (facialHairOffset < 0 || facialHairOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for FacialHair");
            }

            int posxxxxxx = offset + 83 + facialHairOffset;
            int facialHairLen = VarInt.peek(buffer, posxxxxxx);
            if (facialHairLen < 0) {
               return ValidationResult.error("Invalid string length for FacialHair");
            }

            if (facialHairLen > 96) {
               return ValidationResult.error("FacialHair exceeds max length 96");
            }

            posxxxxxx += VarInt.size(facialHairLen);
            posxxxxxx += facialHairLen;
            if (posxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FacialHair");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxxx - facialHairLen, facialHairLen)) {
               return ValidationResult.error("FacialHair contains non-ASCII bytes");
            }
         }

         if ((nullBits[0] & 128) != 0) {
            int haircutOffset = buffer.getIntLE(offset + 31);
            if (haircutOffset < 0 || haircutOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for Haircut");
            }

            int posxxxxxxx = offset + 83 + haircutOffset;
            int haircutLen = VarInt.peek(buffer, posxxxxxxx);
            if (haircutLen < 0) {
               return ValidationResult.error("Invalid string length for Haircut");
            }

            if (haircutLen > 96) {
               return ValidationResult.error("Haircut exceeds max length 96");
            }

            posxxxxxxx += VarInt.size(haircutLen);
            posxxxxxxx += haircutLen;
            if (posxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Haircut");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxxxx - haircutLen, haircutLen)) {
               return ValidationResult.error("Haircut contains non-ASCII bytes");
            }
         }

         if ((nullBits[1] & 1) != 0) {
            int eyebrowsOffset = buffer.getIntLE(offset + 35);
            if (eyebrowsOffset < 0 || eyebrowsOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for Eyebrows");
            }

            int posxxxxxxxx = offset + 83 + eyebrowsOffset;
            int eyebrowsLen = VarInt.peek(buffer, posxxxxxxxx);
            if (eyebrowsLen < 0) {
               return ValidationResult.error("Invalid string length for Eyebrows");
            }

            if (eyebrowsLen > 96) {
               return ValidationResult.error("Eyebrows exceeds max length 96");
            }

            posxxxxxxxx += VarInt.size(eyebrowsLen);
            posxxxxxxxx += eyebrowsLen;
            if (posxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Eyebrows");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxxxxx - eyebrowsLen, eyebrowsLen)) {
               return ValidationResult.error("Eyebrows contains non-ASCII bytes");
            }
         }

         if ((nullBits[1] & 2) != 0) {
            int pantsOffset = buffer.getIntLE(offset + 39);
            if (pantsOffset < 0 || pantsOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for Pants");
            }

            int posxxxxxxxxx = offset + 83 + pantsOffset;
            int pantsLen = VarInt.peek(buffer, posxxxxxxxxx);
            if (pantsLen < 0) {
               return ValidationResult.error("Invalid string length for Pants");
            }

            if (pantsLen > 96) {
               return ValidationResult.error("Pants exceeds max length 96");
            }

            posxxxxxxxxx += VarInt.size(pantsLen);
            posxxxxxxxxx += pantsLen;
            if (posxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Pants");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxxxxxx - pantsLen, pantsLen)) {
               return ValidationResult.error("Pants contains non-ASCII bytes");
            }
         }

         if ((nullBits[1] & 4) != 0) {
            int overpantsOffset = buffer.getIntLE(offset + 43);
            if (overpantsOffset < 0 || overpantsOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for Overpants");
            }

            int posxxxxxxxxxx = offset + 83 + overpantsOffset;
            int overpantsLen = VarInt.peek(buffer, posxxxxxxxxxx);
            if (overpantsLen < 0) {
               return ValidationResult.error("Invalid string length for Overpants");
            }

            if (overpantsLen > 96) {
               return ValidationResult.error("Overpants exceeds max length 96");
            }

            posxxxxxxxxxx += VarInt.size(overpantsLen);
            posxxxxxxxxxx += overpantsLen;
            if (posxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Overpants");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxxxxxxx - overpantsLen, overpantsLen)) {
               return ValidationResult.error("Overpants contains non-ASCII bytes");
            }
         }

         if ((nullBits[1] & 8) != 0) {
            int undertopOffset = buffer.getIntLE(offset + 47);
            if (undertopOffset < 0 || undertopOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for Undertop");
            }

            int posxxxxxxxxxxx = offset + 83 + undertopOffset;
            int undertopLen = VarInt.peek(buffer, posxxxxxxxxxxx);
            if (undertopLen < 0) {
               return ValidationResult.error("Invalid string length for Undertop");
            }

            if (undertopLen > 96) {
               return ValidationResult.error("Undertop exceeds max length 96");
            }

            posxxxxxxxxxxx += VarInt.size(undertopLen);
            posxxxxxxxxxxx += undertopLen;
            if (posxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Undertop");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxxxxxxxx - undertopLen, undertopLen)) {
               return ValidationResult.error("Undertop contains non-ASCII bytes");
            }
         }

         if ((nullBits[1] & 16) != 0) {
            int overtopOffset = buffer.getIntLE(offset + 51);
            if (overtopOffset < 0 || overtopOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for Overtop");
            }

            int posxxxxxxxxxxxx = offset + 83 + overtopOffset;
            int overtopLen = VarInt.peek(buffer, posxxxxxxxxxxxx);
            if (overtopLen < 0) {
               return ValidationResult.error("Invalid string length for Overtop");
            }

            if (overtopLen > 96) {
               return ValidationResult.error("Overtop exceeds max length 96");
            }

            posxxxxxxxxxxxx += VarInt.size(overtopLen);
            posxxxxxxxxxxxx += overtopLen;
            if (posxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Overtop");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxxxxxxxxx - overtopLen, overtopLen)) {
               return ValidationResult.error("Overtop contains non-ASCII bytes");
            }
         }

         if ((nullBits[1] & 32) != 0) {
            int shoesOffset = buffer.getIntLE(offset + 55);
            if (shoesOffset < 0 || shoesOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for Shoes");
            }

            int posxxxxxxxxxxxxx = offset + 83 + shoesOffset;
            int shoesLen = VarInt.peek(buffer, posxxxxxxxxxxxxx);
            if (shoesLen < 0) {
               return ValidationResult.error("Invalid string length for Shoes");
            }

            if (shoesLen > 96) {
               return ValidationResult.error("Shoes exceeds max length 96");
            }

            posxxxxxxxxxxxxx += VarInt.size(shoesLen);
            posxxxxxxxxxxxxx += shoesLen;
            if (posxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Shoes");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxxxxxxxxxx - shoesLen, shoesLen)) {
               return ValidationResult.error("Shoes contains non-ASCII bytes");
            }
         }

         if ((nullBits[1] & 64) != 0) {
            int headAccessoryOffset = buffer.getIntLE(offset + 59);
            if (headAccessoryOffset < 0 || headAccessoryOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for HeadAccessory");
            }

            int posxxxxxxxxxxxxxx = offset + 83 + headAccessoryOffset;
            int headAccessoryLen = VarInt.peek(buffer, posxxxxxxxxxxxxxx);
            if (headAccessoryLen < 0) {
               return ValidationResult.error("Invalid string length for HeadAccessory");
            }

            if (headAccessoryLen > 96) {
               return ValidationResult.error("HeadAccessory exceeds max length 96");
            }

            posxxxxxxxxxxxxxx += VarInt.size(headAccessoryLen);
            posxxxxxxxxxxxxxx += headAccessoryLen;
            if (posxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading HeadAccessory");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxxxxxxxxxxx - headAccessoryLen, headAccessoryLen)) {
               return ValidationResult.error("HeadAccessory contains non-ASCII bytes");
            }
         }

         if ((nullBits[1] & 128) != 0) {
            int faceAccessoryOffset = buffer.getIntLE(offset + 63);
            if (faceAccessoryOffset < 0 || faceAccessoryOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for FaceAccessory");
            }

            int posxxxxxxxxxxxxxxx = offset + 83 + faceAccessoryOffset;
            int faceAccessoryLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxx);
            if (faceAccessoryLen < 0) {
               return ValidationResult.error("Invalid string length for FaceAccessory");
            }

            if (faceAccessoryLen > 96) {
               return ValidationResult.error("FaceAccessory exceeds max length 96");
            }

            posxxxxxxxxxxxxxxx += VarInt.size(faceAccessoryLen);
            posxxxxxxxxxxxxxxx += faceAccessoryLen;
            if (posxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FaceAccessory");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxxxxxxxxxxxx - faceAccessoryLen, faceAccessoryLen)) {
               return ValidationResult.error("FaceAccessory contains non-ASCII bytes");
            }
         }

         if ((nullBits[2] & 1) != 0) {
            int earAccessoryOffset = buffer.getIntLE(offset + 67);
            if (earAccessoryOffset < 0 || earAccessoryOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for EarAccessory");
            }

            int posxxxxxxxxxxxxxxxx = offset + 83 + earAccessoryOffset;
            int earAccessoryLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxxx);
            if (earAccessoryLen < 0) {
               return ValidationResult.error("Invalid string length for EarAccessory");
            }

            if (earAccessoryLen > 96) {
               return ValidationResult.error("EarAccessory exceeds max length 96");
            }

            posxxxxxxxxxxxxxxxx += VarInt.size(earAccessoryLen);
            posxxxxxxxxxxxxxxxx += earAccessoryLen;
            if (posxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading EarAccessory");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxxxxxxxxxxxxx - earAccessoryLen, earAccessoryLen)) {
               return ValidationResult.error("EarAccessory contains non-ASCII bytes");
            }
         }

         if ((nullBits[2] & 2) != 0) {
            int skinFeatureOffset = buffer.getIntLE(offset + 71);
            if (skinFeatureOffset < 0 || skinFeatureOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for SkinFeature");
            }

            int posxxxxxxxxxxxxxxxxx = offset + 83 + skinFeatureOffset;
            int skinFeatureLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxx);
            if (skinFeatureLen < 0) {
               return ValidationResult.error("Invalid string length for SkinFeature");
            }

            if (skinFeatureLen > 96) {
               return ValidationResult.error("SkinFeature exceeds max length 96");
            }

            posxxxxxxxxxxxxxxxxx += VarInt.size(skinFeatureLen);
            posxxxxxxxxxxxxxxxxx += skinFeatureLen;
            if (posxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading SkinFeature");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxxxxxxxxxxxxxx - skinFeatureLen, skinFeatureLen)) {
               return ValidationResult.error("SkinFeature contains non-ASCII bytes");
            }
         }

         if ((nullBits[2] & 4) != 0) {
            int glovesOffset = buffer.getIntLE(offset + 75);
            if (glovesOffset < 0 || glovesOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for Gloves");
            }

            int posxxxxxxxxxxxxxxxxxx = offset + 83 + glovesOffset;
            int glovesLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxx);
            if (glovesLen < 0) {
               return ValidationResult.error("Invalid string length for Gloves");
            }

            if (glovesLen > 96) {
               return ValidationResult.error("Gloves exceeds max length 96");
            }

            posxxxxxxxxxxxxxxxxxx += VarInt.size(glovesLen);
            posxxxxxxxxxxxxxxxxxx += glovesLen;
            if (posxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Gloves");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxxxxxxxxxxxxxxx - glovesLen, glovesLen)) {
               return ValidationResult.error("Gloves contains non-ASCII bytes");
            }
         }

         if ((nullBits[2] & 8) != 0) {
            int capeOffset = buffer.getIntLE(offset + 79);
            if (capeOffset < 0 || capeOffset > buffer.writerIndex() - offset - 83) {
               return ValidationResult.error("Invalid offset for Cape");
            }

            int posxxxxxxxxxxxxxxxxxxx = offset + 83 + capeOffset;
            int capeLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxx);
            if (capeLen < 0) {
               return ValidationResult.error("Invalid string length for Cape");
            }

            if (capeLen > 96) {
               return ValidationResult.error("Cape exceeds max length 96");
            }

            posxxxxxxxxxxxxxxxxxxx += VarInt.size(capeLen);
            posxxxxxxxxxxxxxxxxxxx += capeLen;
            if (posxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Cape");
            }

            if (!PacketIO.isValidAscii(buffer, posxxxxxxxxxxxxxxxxxxx - capeLen, capeLen)) {
               return ValidationResult.error("Cape contains non-ASCII bytes");
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
