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

public class Weather {
   public static final int NULLABLE_BIT_FIELD_SIZE = 4;
   public static final int FIXED_BLOCK_SIZE = 30;
   public static final int VARIABLE_FIELD_COUNT = 24;
   public static final int VARIABLE_BLOCK_START = 126;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public int[] tagIndexes;
   @Nullable
   public String stars;
   @Nullable
   public Map<Integer, String> moons;
   @Nullable
   public Cloud[] clouds;
   @Nullable
   public Map<Float, Float> sunlightDampingMultiplier;
   @Nullable
   public Map<Float, Color> sunlightColors;
   @Nullable
   public Map<Float, ColorAlpha> skyTopColors;
   @Nullable
   public Map<Float, ColorAlpha> skyBottomColors;
   @Nullable
   public Map<Float, ColorAlpha> skySunsetColors;
   @Nullable
   public Map<Float, Color> sunColors;
   @Nullable
   public Map<Float, Float> sunScales;
   @Nullable
   public Map<Float, ColorAlpha> sunGlowColors;
   @Nullable
   public Map<Float, ColorAlpha> moonColors;
   @Nullable
   public Map<Float, Float> moonScales;
   @Nullable
   public Map<Float, ColorAlpha> moonGlowColors;
   @Nullable
   public Map<Float, Color> fogColors;
   @Nullable
   public Map<Float, Float> fogHeightFalloffs;
   @Nullable
   public Map<Float, Float> fogDensities;
   @Nullable
   public String screenEffect;
   @Nullable
   public Map<Float, ColorAlpha> screenEffectColors;
   @Nullable
   public Map<Float, Color> colorFilters;
   @Nullable
   public Map<Float, Color> waterTints;
   @Nullable
   public WeatherParticle particle;
   @Nullable
   public NearFar fog;
   @Nullable
   public FogOptions fogOptions;

   public Weather() {
   }

   public Weather(
      @Nullable String id,
      @Nullable int[] tagIndexes,
      @Nullable String stars,
      @Nullable Map<Integer, String> moons,
      @Nullable Cloud[] clouds,
      @Nullable Map<Float, Float> sunlightDampingMultiplier,
      @Nullable Map<Float, Color> sunlightColors,
      @Nullable Map<Float, ColorAlpha> skyTopColors,
      @Nullable Map<Float, ColorAlpha> skyBottomColors,
      @Nullable Map<Float, ColorAlpha> skySunsetColors,
      @Nullable Map<Float, Color> sunColors,
      @Nullable Map<Float, Float> sunScales,
      @Nullable Map<Float, ColorAlpha> sunGlowColors,
      @Nullable Map<Float, ColorAlpha> moonColors,
      @Nullable Map<Float, Float> moonScales,
      @Nullable Map<Float, ColorAlpha> moonGlowColors,
      @Nullable Map<Float, Color> fogColors,
      @Nullable Map<Float, Float> fogHeightFalloffs,
      @Nullable Map<Float, Float> fogDensities,
      @Nullable String screenEffect,
      @Nullable Map<Float, ColorAlpha> screenEffectColors,
      @Nullable Map<Float, Color> colorFilters,
      @Nullable Map<Float, Color> waterTints,
      @Nullable WeatherParticle particle,
      @Nullable NearFar fog,
      @Nullable FogOptions fogOptions
   ) {
      this.id = id;
      this.tagIndexes = tagIndexes;
      this.stars = stars;
      this.moons = moons;
      this.clouds = clouds;
      this.sunlightDampingMultiplier = sunlightDampingMultiplier;
      this.sunlightColors = sunlightColors;
      this.skyTopColors = skyTopColors;
      this.skyBottomColors = skyBottomColors;
      this.skySunsetColors = skySunsetColors;
      this.sunColors = sunColors;
      this.sunScales = sunScales;
      this.sunGlowColors = sunGlowColors;
      this.moonColors = moonColors;
      this.moonScales = moonScales;
      this.moonGlowColors = moonGlowColors;
      this.fogColors = fogColors;
      this.fogHeightFalloffs = fogHeightFalloffs;
      this.fogDensities = fogDensities;
      this.screenEffect = screenEffect;
      this.screenEffectColors = screenEffectColors;
      this.colorFilters = colorFilters;
      this.waterTints = waterTints;
      this.particle = particle;
      this.fog = fog;
      this.fogOptions = fogOptions;
   }

   public Weather(@Nonnull Weather other) {
      this.id = other.id;
      this.tagIndexes = other.tagIndexes;
      this.stars = other.stars;
      this.moons = other.moons;
      this.clouds = other.clouds;
      this.sunlightDampingMultiplier = other.sunlightDampingMultiplier;
      this.sunlightColors = other.sunlightColors;
      this.skyTopColors = other.skyTopColors;
      this.skyBottomColors = other.skyBottomColors;
      this.skySunsetColors = other.skySunsetColors;
      this.sunColors = other.sunColors;
      this.sunScales = other.sunScales;
      this.sunGlowColors = other.sunGlowColors;
      this.moonColors = other.moonColors;
      this.moonScales = other.moonScales;
      this.moonGlowColors = other.moonGlowColors;
      this.fogColors = other.fogColors;
      this.fogHeightFalloffs = other.fogHeightFalloffs;
      this.fogDensities = other.fogDensities;
      this.screenEffect = other.screenEffect;
      this.screenEffectColors = other.screenEffectColors;
      this.colorFilters = other.colorFilters;
      this.waterTints = other.waterTints;
      this.particle = other.particle;
      this.fog = other.fog;
      this.fogOptions = other.fogOptions;
   }

   @Nonnull
   public static Weather deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 126) {
         throw ProtocolException.bufferTooSmall("Weather", 126, buf.readableBytes() - offset);
      } else {
         Weather obj = new Weather();
         byte[] nullBits = PacketIO.readBytes(buf, offset, 4);
         if ((nullBits[0] & 1) != 0) {
            obj.fog = NearFar.deserialize(buf, offset + 4);
         }

         if ((nullBits[0] & 2) != 0) {
            obj.fogOptions = FogOptions.deserialize(buf, offset + 12);
         }

         if ((nullBits[0] & 4) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 30);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 126 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarIntLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits[0] & 8) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 34);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("TagIndexes", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 126 + varPosBase1;
            int tagIndexesCount = VarInt.peek(buf, varPos1);
            if (tagIndexesCount < 0) {
               throw ProtocolException.invalidVarInt("TagIndexes");
            }

            int varIntLen = VarInt.size(tagIndexesCount);
            if (tagIndexesCount > 4096000) {
               throw ProtocolException.arrayTooLong("TagIndexes", tagIndexesCount, 4096000);
            }

            if (varPos1 + varIntLen + tagIndexesCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("TagIndexes", varPos1 + varIntLen + tagIndexesCount * 4, buf.readableBytes());
            }

            obj.tagIndexes = new int[tagIndexesCount];

            for (int i = 0; i < tagIndexesCount; i++) {
               obj.tagIndexes[i] = buf.getIntLE(varPos1 + varIntLen + i * 4);
            }
         }

         if ((nullBits[0] & 16) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 38);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("Stars", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 126 + varPosBase2;
            int starsLen = VarInt.peek(buf, varPos2);
            if (starsLen < 0) {
               throw ProtocolException.invalidVarInt("Stars");
            }

            int starsVarIntLen = VarInt.size(starsLen);
            if (starsLen > 4096000) {
               throw ProtocolException.stringTooLong("Stars", starsLen, 4096000);
            }

            if (varPos2 + starsVarIntLen + starsLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Stars", varPos2 + starsVarIntLen + starsLen, buf.readableBytes());
            }

            obj.stars = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits[0] & 32) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 42);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("Moons", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 126 + varPosBase3;
            int moonsCount = VarInt.peek(buf, varPos3);
            if (moonsCount < 0) {
               throw ProtocolException.invalidVarInt("Moons");
            }

            int varIntLenx = VarInt.size(moonsCount);
            if (moonsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Moons", moonsCount, 4096000);
            }

            obj.moons = new HashMap<>(moonsCount);
            int dictPos = varPos3 + varIntLenx;

            for (int i = 0; i < moonsCount; i++) {
               int key = buf.getIntLE(dictPos);
               dictPos += 4;
               int valLen = VarInt.peek(buf, dictPos);
               if (valLen < 0) {
                  throw ProtocolException.invalidVarInt("val");
               }

               int valVarLen = VarInt.size(valLen);
               if (valLen > 4096000) {
                  throw ProtocolException.stringTooLong("val", valLen, 4096000);
               }

               if (dictPos + valVarLen + valLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("val", dictPos + valVarLen + valLen, buf.readableBytes());
               }

               String val = PacketIO.readVarString(buf, dictPos);
               dictPos += valVarLen + valLen;
               if (obj.moons.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("moons", key);
               }
            }
         }

         if ((nullBits[0] & 64) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 46);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("Clouds", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 126 + varPosBase4;
            int cloudsCount = VarInt.peek(buf, varPos4);
            if (cloudsCount < 0) {
               throw ProtocolException.invalidVarInt("Clouds");
            }

            int varIntLenx = VarInt.size(cloudsCount);
            if (cloudsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Clouds", cloudsCount, 4096000);
            }

            if (varPos4 + varIntLenx + cloudsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Clouds", varPos4 + varIntLenx + cloudsCount * 1, buf.readableBytes());
            }

            obj.clouds = new Cloud[cloudsCount];
            int elemPos = varPos4 + varIntLenx;

            for (int i = 0; i < cloudsCount; i++) {
               obj.clouds[i] = Cloud.deserialize(buf, elemPos);
               elemPos += Cloud.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits[0] & 128) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 50);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("SunlightDampingMultiplier", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 126 + varPosBase5;
            int sunlightDampingMultiplierCount = VarInt.peek(buf, varPos5);
            if (sunlightDampingMultiplierCount < 0) {
               throw ProtocolException.invalidVarInt("SunlightDampingMultiplier");
            }

            int varIntLenxx = VarInt.size(sunlightDampingMultiplierCount);
            if (sunlightDampingMultiplierCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SunlightDampingMultiplier", sunlightDampingMultiplierCount, 4096000);
            }

            obj.sunlightDampingMultiplier = new HashMap<>(sunlightDampingMultiplierCount);
            int dictPos = varPos5 + varIntLenxx;

            for (int i = 0; i < sunlightDampingMultiplierCount; i++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               float val = buf.getFloatLE(dictPos);
               dictPos += 4;
               if (obj.sunlightDampingMultiplier.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("sunlightDampingMultiplier", keyx);
               }
            }
         }

         if ((nullBits[1] & 1) != 0) {
            int varPosBase6 = buf.getIntLE(offset + 54);
            if (varPosBase6 < 0 || varPosBase6 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("SunlightColors", varPosBase6, buf.readableBytes());
            }

            int varPos6 = offset + 126 + varPosBase6;
            int sunlightColorsCount = VarInt.peek(buf, varPos6);
            if (sunlightColorsCount < 0) {
               throw ProtocolException.invalidVarInt("SunlightColors");
            }

            int varIntLenxx = VarInt.size(sunlightColorsCount);
            if (sunlightColorsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SunlightColors", sunlightColorsCount, 4096000);
            }

            obj.sunlightColors = new HashMap<>(sunlightColorsCount);
            int dictPos = varPos6 + varIntLenxx;

            for (int ix = 0; ix < sunlightColorsCount; ix++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               Color val = Color.deserialize(buf, dictPos);
               dictPos += Color.computeBytesConsumed(buf, dictPos);
               if (obj.sunlightColors.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("sunlightColors", keyx);
               }
            }
         }

         if ((nullBits[1] & 2) != 0) {
            int varPosBase7 = buf.getIntLE(offset + 58);
            if (varPosBase7 < 0 || varPosBase7 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("SkyTopColors", varPosBase7, buf.readableBytes());
            }

            int varPos7 = offset + 126 + varPosBase7;
            int skyTopColorsCount = VarInt.peek(buf, varPos7);
            if (skyTopColorsCount < 0) {
               throw ProtocolException.invalidVarInt("SkyTopColors");
            }

            int varIntLenxx = VarInt.size(skyTopColorsCount);
            if (skyTopColorsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SkyTopColors", skyTopColorsCount, 4096000);
            }

            obj.skyTopColors = new HashMap<>(skyTopColorsCount);
            int dictPos = varPos7 + varIntLenxx;

            for (int ixx = 0; ixx < skyTopColorsCount; ixx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               ColorAlpha val = ColorAlpha.deserialize(buf, dictPos);
               dictPos += ColorAlpha.computeBytesConsumed(buf, dictPos);
               if (obj.skyTopColors.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("skyTopColors", keyx);
               }
            }
         }

         if ((nullBits[1] & 4) != 0) {
            int varPosBase8 = buf.getIntLE(offset + 62);
            if (varPosBase8 < 0 || varPosBase8 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("SkyBottomColors", varPosBase8, buf.readableBytes());
            }

            int varPos8 = offset + 126 + varPosBase8;
            int skyBottomColorsCount = VarInt.peek(buf, varPos8);
            if (skyBottomColorsCount < 0) {
               throw ProtocolException.invalidVarInt("SkyBottomColors");
            }

            int varIntLenxx = VarInt.size(skyBottomColorsCount);
            if (skyBottomColorsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SkyBottomColors", skyBottomColorsCount, 4096000);
            }

            obj.skyBottomColors = new HashMap<>(skyBottomColorsCount);
            int dictPos = varPos8 + varIntLenxx;

            for (int ixxx = 0; ixxx < skyBottomColorsCount; ixxx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               ColorAlpha val = ColorAlpha.deserialize(buf, dictPos);
               dictPos += ColorAlpha.computeBytesConsumed(buf, dictPos);
               if (obj.skyBottomColors.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("skyBottomColors", keyx);
               }
            }
         }

         if ((nullBits[1] & 8) != 0) {
            int varPosBase9 = buf.getIntLE(offset + 66);
            if (varPosBase9 < 0 || varPosBase9 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("SkySunsetColors", varPosBase9, buf.readableBytes());
            }

            int varPos9 = offset + 126 + varPosBase9;
            int skySunsetColorsCount = VarInt.peek(buf, varPos9);
            if (skySunsetColorsCount < 0) {
               throw ProtocolException.invalidVarInt("SkySunsetColors");
            }

            int varIntLenxx = VarInt.size(skySunsetColorsCount);
            if (skySunsetColorsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SkySunsetColors", skySunsetColorsCount, 4096000);
            }

            obj.skySunsetColors = new HashMap<>(skySunsetColorsCount);
            int dictPos = varPos9 + varIntLenxx;

            for (int ixxxx = 0; ixxxx < skySunsetColorsCount; ixxxx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               ColorAlpha val = ColorAlpha.deserialize(buf, dictPos);
               dictPos += ColorAlpha.computeBytesConsumed(buf, dictPos);
               if (obj.skySunsetColors.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("skySunsetColors", keyx);
               }
            }
         }

         if ((nullBits[1] & 16) != 0) {
            int varPosBase10 = buf.getIntLE(offset + 70);
            if (varPosBase10 < 0 || varPosBase10 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("SunColors", varPosBase10, buf.readableBytes());
            }

            int varPos10 = offset + 126 + varPosBase10;
            int sunColorsCount = VarInt.peek(buf, varPos10);
            if (sunColorsCount < 0) {
               throw ProtocolException.invalidVarInt("SunColors");
            }

            int varIntLenxx = VarInt.size(sunColorsCount);
            if (sunColorsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SunColors", sunColorsCount, 4096000);
            }

            obj.sunColors = new HashMap<>(sunColorsCount);
            int dictPos = varPos10 + varIntLenxx;

            for (int ixxxxx = 0; ixxxxx < sunColorsCount; ixxxxx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               Color val = Color.deserialize(buf, dictPos);
               dictPos += Color.computeBytesConsumed(buf, dictPos);
               if (obj.sunColors.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("sunColors", keyx);
               }
            }
         }

         if ((nullBits[1] & 32) != 0) {
            int varPosBase11 = buf.getIntLE(offset + 74);
            if (varPosBase11 < 0 || varPosBase11 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("SunScales", varPosBase11, buf.readableBytes());
            }

            int varPos11 = offset + 126 + varPosBase11;
            int sunScalesCount = VarInt.peek(buf, varPos11);
            if (sunScalesCount < 0) {
               throw ProtocolException.invalidVarInt("SunScales");
            }

            int varIntLenxx = VarInt.size(sunScalesCount);
            if (sunScalesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SunScales", sunScalesCount, 4096000);
            }

            obj.sunScales = new HashMap<>(sunScalesCount);
            int dictPos = varPos11 + varIntLenxx;

            for (int ixxxxxx = 0; ixxxxxx < sunScalesCount; ixxxxxx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               float val = buf.getFloatLE(dictPos);
               dictPos += 4;
               if (obj.sunScales.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("sunScales", keyx);
               }
            }
         }

         if ((nullBits[1] & 64) != 0) {
            int varPosBase12 = buf.getIntLE(offset + 78);
            if (varPosBase12 < 0 || varPosBase12 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("SunGlowColors", varPosBase12, buf.readableBytes());
            }

            int varPos12 = offset + 126 + varPosBase12;
            int sunGlowColorsCount = VarInt.peek(buf, varPos12);
            if (sunGlowColorsCount < 0) {
               throw ProtocolException.invalidVarInt("SunGlowColors");
            }

            int varIntLenxx = VarInt.size(sunGlowColorsCount);
            if (sunGlowColorsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SunGlowColors", sunGlowColorsCount, 4096000);
            }

            obj.sunGlowColors = new HashMap<>(sunGlowColorsCount);
            int dictPos = varPos12 + varIntLenxx;

            for (int ixxxxxxx = 0; ixxxxxxx < sunGlowColorsCount; ixxxxxxx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               ColorAlpha val = ColorAlpha.deserialize(buf, dictPos);
               dictPos += ColorAlpha.computeBytesConsumed(buf, dictPos);
               if (obj.sunGlowColors.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("sunGlowColors", keyx);
               }
            }
         }

         if ((nullBits[1] & 128) != 0) {
            int varPosBase13 = buf.getIntLE(offset + 82);
            if (varPosBase13 < 0 || varPosBase13 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("MoonColors", varPosBase13, buf.readableBytes());
            }

            int varPos13 = offset + 126 + varPosBase13;
            int moonColorsCount = VarInt.peek(buf, varPos13);
            if (moonColorsCount < 0) {
               throw ProtocolException.invalidVarInt("MoonColors");
            }

            int varIntLenxx = VarInt.size(moonColorsCount);
            if (moonColorsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("MoonColors", moonColorsCount, 4096000);
            }

            obj.moonColors = new HashMap<>(moonColorsCount);
            int dictPos = varPos13 + varIntLenxx;

            for (int ixxxxxxxx = 0; ixxxxxxxx < moonColorsCount; ixxxxxxxx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               ColorAlpha val = ColorAlpha.deserialize(buf, dictPos);
               dictPos += ColorAlpha.computeBytesConsumed(buf, dictPos);
               if (obj.moonColors.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("moonColors", keyx);
               }
            }
         }

         if ((nullBits[2] & 1) != 0) {
            int varPosBase14 = buf.getIntLE(offset + 86);
            if (varPosBase14 < 0 || varPosBase14 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("MoonScales", varPosBase14, buf.readableBytes());
            }

            int varPos14 = offset + 126 + varPosBase14;
            int moonScalesCount = VarInt.peek(buf, varPos14);
            if (moonScalesCount < 0) {
               throw ProtocolException.invalidVarInt("MoonScales");
            }

            int varIntLenxx = VarInt.size(moonScalesCount);
            if (moonScalesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("MoonScales", moonScalesCount, 4096000);
            }

            obj.moonScales = new HashMap<>(moonScalesCount);
            int dictPos = varPos14 + varIntLenxx;

            for (int ixxxxxxxxx = 0; ixxxxxxxxx < moonScalesCount; ixxxxxxxxx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               float val = buf.getFloatLE(dictPos);
               dictPos += 4;
               if (obj.moonScales.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("moonScales", keyx);
               }
            }
         }

         if ((nullBits[2] & 2) != 0) {
            int varPosBase15 = buf.getIntLE(offset + 90);
            if (varPosBase15 < 0 || varPosBase15 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("MoonGlowColors", varPosBase15, buf.readableBytes());
            }

            int varPos15 = offset + 126 + varPosBase15;
            int moonGlowColorsCount = VarInt.peek(buf, varPos15);
            if (moonGlowColorsCount < 0) {
               throw ProtocolException.invalidVarInt("MoonGlowColors");
            }

            int varIntLenxx = VarInt.size(moonGlowColorsCount);
            if (moonGlowColorsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("MoonGlowColors", moonGlowColorsCount, 4096000);
            }

            obj.moonGlowColors = new HashMap<>(moonGlowColorsCount);
            int dictPos = varPos15 + varIntLenxx;

            for (int ixxxxxxxxxx = 0; ixxxxxxxxxx < moonGlowColorsCount; ixxxxxxxxxx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               ColorAlpha val = ColorAlpha.deserialize(buf, dictPos);
               dictPos += ColorAlpha.computeBytesConsumed(buf, dictPos);
               if (obj.moonGlowColors.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("moonGlowColors", keyx);
               }
            }
         }

         if ((nullBits[2] & 4) != 0) {
            int varPosBase16 = buf.getIntLE(offset + 94);
            if (varPosBase16 < 0 || varPosBase16 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("FogColors", varPosBase16, buf.readableBytes());
            }

            int varPos16 = offset + 126 + varPosBase16;
            int fogColorsCount = VarInt.peek(buf, varPos16);
            if (fogColorsCount < 0) {
               throw ProtocolException.invalidVarInt("FogColors");
            }

            int varIntLenxx = VarInt.size(fogColorsCount);
            if (fogColorsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("FogColors", fogColorsCount, 4096000);
            }

            obj.fogColors = new HashMap<>(fogColorsCount);
            int dictPos = varPos16 + varIntLenxx;

            for (int ixxxxxxxxxxx = 0; ixxxxxxxxxxx < fogColorsCount; ixxxxxxxxxxx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               Color val = Color.deserialize(buf, dictPos);
               dictPos += Color.computeBytesConsumed(buf, dictPos);
               if (obj.fogColors.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("fogColors", keyx);
               }
            }
         }

         if ((nullBits[2] & 8) != 0) {
            int varPosBase17 = buf.getIntLE(offset + 98);
            if (varPosBase17 < 0 || varPosBase17 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("FogHeightFalloffs", varPosBase17, buf.readableBytes());
            }

            int varPos17 = offset + 126 + varPosBase17;
            int fogHeightFalloffsCount = VarInt.peek(buf, varPos17);
            if (fogHeightFalloffsCount < 0) {
               throw ProtocolException.invalidVarInt("FogHeightFalloffs");
            }

            int varIntLenxx = VarInt.size(fogHeightFalloffsCount);
            if (fogHeightFalloffsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("FogHeightFalloffs", fogHeightFalloffsCount, 4096000);
            }

            obj.fogHeightFalloffs = new HashMap<>(fogHeightFalloffsCount);
            int dictPos = varPos17 + varIntLenxx;

            for (int ixxxxxxxxxxxx = 0; ixxxxxxxxxxxx < fogHeightFalloffsCount; ixxxxxxxxxxxx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               float val = buf.getFloatLE(dictPos);
               dictPos += 4;
               if (obj.fogHeightFalloffs.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("fogHeightFalloffs", keyx);
               }
            }
         }

         if ((nullBits[2] & 16) != 0) {
            int varPosBase18 = buf.getIntLE(offset + 102);
            if (varPosBase18 < 0 || varPosBase18 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("FogDensities", varPosBase18, buf.readableBytes());
            }

            int varPos18 = offset + 126 + varPosBase18;
            int fogDensitiesCount = VarInt.peek(buf, varPos18);
            if (fogDensitiesCount < 0) {
               throw ProtocolException.invalidVarInt("FogDensities");
            }

            int varIntLenxx = VarInt.size(fogDensitiesCount);
            if (fogDensitiesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("FogDensities", fogDensitiesCount, 4096000);
            }

            obj.fogDensities = new HashMap<>(fogDensitiesCount);
            int dictPos = varPos18 + varIntLenxx;

            for (int ixxxxxxxxxxxxx = 0; ixxxxxxxxxxxxx < fogDensitiesCount; ixxxxxxxxxxxxx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               float val = buf.getFloatLE(dictPos);
               dictPos += 4;
               if (obj.fogDensities.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("fogDensities", keyx);
               }
            }
         }

         if ((nullBits[2] & 32) != 0) {
            int varPosBase19 = buf.getIntLE(offset + 106);
            if (varPosBase19 < 0 || varPosBase19 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("ScreenEffect", varPosBase19, buf.readableBytes());
            }

            int varPos19 = offset + 126 + varPosBase19;
            int screenEffectLen = VarInt.peek(buf, varPos19);
            if (screenEffectLen < 0) {
               throw ProtocolException.invalidVarInt("ScreenEffect");
            }

            int screenEffectVarIntLen = VarInt.size(screenEffectLen);
            if (screenEffectLen > 4096000) {
               throw ProtocolException.stringTooLong("ScreenEffect", screenEffectLen, 4096000);
            }

            if (varPos19 + screenEffectVarIntLen + screenEffectLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ScreenEffect", varPos19 + screenEffectVarIntLen + screenEffectLen, buf.readableBytes());
            }

            obj.screenEffect = PacketIO.readVarString(buf, varPos19, PacketIO.UTF8);
         }

         if ((nullBits[2] & 64) != 0) {
            int varPosBase20 = buf.getIntLE(offset + 110);
            if (varPosBase20 < 0 || varPosBase20 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("ScreenEffectColors", varPosBase20, buf.readableBytes());
            }

            int varPos20 = offset + 126 + varPosBase20;
            int screenEffectColorsCount = VarInt.peek(buf, varPos20);
            if (screenEffectColorsCount < 0) {
               throw ProtocolException.invalidVarInt("ScreenEffectColors");
            }

            int varIntLenxx = VarInt.size(screenEffectColorsCount);
            if (screenEffectColorsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ScreenEffectColors", screenEffectColorsCount, 4096000);
            }

            obj.screenEffectColors = new HashMap<>(screenEffectColorsCount);
            int dictPos = varPos20 + varIntLenxx;

            for (int ixxxxxxxxxxxxxx = 0; ixxxxxxxxxxxxxx < screenEffectColorsCount; ixxxxxxxxxxxxxx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               ColorAlpha val = ColorAlpha.deserialize(buf, dictPos);
               dictPos += ColorAlpha.computeBytesConsumed(buf, dictPos);
               if (obj.screenEffectColors.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("screenEffectColors", keyx);
               }
            }
         }

         if ((nullBits[2] & 128) != 0) {
            int varPosBase21 = buf.getIntLE(offset + 114);
            if (varPosBase21 < 0 || varPosBase21 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("ColorFilters", varPosBase21, buf.readableBytes());
            }

            int varPos21 = offset + 126 + varPosBase21;
            int colorFiltersCount = VarInt.peek(buf, varPos21);
            if (colorFiltersCount < 0) {
               throw ProtocolException.invalidVarInt("ColorFilters");
            }

            int varIntLenxx = VarInt.size(colorFiltersCount);
            if (colorFiltersCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ColorFilters", colorFiltersCount, 4096000);
            }

            obj.colorFilters = new HashMap<>(colorFiltersCount);
            int dictPos = varPos21 + varIntLenxx;

            for (int ixxxxxxxxxxxxxxx = 0; ixxxxxxxxxxxxxxx < colorFiltersCount; ixxxxxxxxxxxxxxx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               Color val = Color.deserialize(buf, dictPos);
               dictPos += Color.computeBytesConsumed(buf, dictPos);
               if (obj.colorFilters.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("colorFilters", keyx);
               }
            }
         }

         if ((nullBits[3] & 1) != 0) {
            int varPosBase22 = buf.getIntLE(offset + 118);
            if (varPosBase22 < 0 || varPosBase22 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("WaterTints", varPosBase22, buf.readableBytes());
            }

            int varPos22 = offset + 126 + varPosBase22;
            int waterTintsCount = VarInt.peek(buf, varPos22);
            if (waterTintsCount < 0) {
               throw ProtocolException.invalidVarInt("WaterTints");
            }

            int varIntLenxx = VarInt.size(waterTintsCount);
            if (waterTintsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("WaterTints", waterTintsCount, 4096000);
            }

            obj.waterTints = new HashMap<>(waterTintsCount);
            int dictPos = varPos22 + varIntLenxx;

            for (int ixxxxxxxxxxxxxxxx = 0; ixxxxxxxxxxxxxxxx < waterTintsCount; ixxxxxxxxxxxxxxxx++) {
               float keyx = buf.getFloatLE(dictPos);
               dictPos += 4;
               Color val = Color.deserialize(buf, dictPos);
               dictPos += Color.computeBytesConsumed(buf, dictPos);
               if (obj.waterTints.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("waterTints", keyx);
               }
            }
         }

         if ((nullBits[3] & 2) != 0) {
            int varPosBase23 = buf.getIntLE(offset + 122);
            if (varPosBase23 < 0 || varPosBase23 > buf.writerIndex() - offset - 126) {
               throw ProtocolException.invalidOffset("Particle", varPosBase23, buf.readableBytes());
            }

            int varPos23 = offset + 126 + varPosBase23;
            obj.particle = WeatherParticle.deserialize(buf, varPos23);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 4);
      int maxEnd = 126;
      if ((nullBits[0] & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 30);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 126 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[0] & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 34);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("TagIndexes", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 126 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen) + arrLen * 4;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[0] & 16) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 38);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("Stars", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 126 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[0] & 32) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 42);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("Moons", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 126 + fieldOffset3;
         int dictLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos3 += 4;
            int sl = VarInt.peek(buf, pos3);
            pos3 += VarInt.size(sl) + sl;
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[0] & 64) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 46);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("Clouds", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 126 + fieldOffset4;
         int arrLen = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos4 += Cloud.computeBytesConsumed(buf, pos4);
         }

         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 50);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("SunlightDampingMultiplier", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 126 + fieldOffset5;
         int dictLen = VarInt.peek(buf, pos5);
         pos5 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos5 += 4;
            pos5 += 4;
         }

         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 54);
         if (fieldOffset6 < 0 || fieldOffset6 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("SunlightColors", fieldOffset6, maxEnd);
         }

         int pos6 = offset + 126 + fieldOffset6;
         int dictLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos6 += 4;
            pos6 += Color.computeBytesConsumed(buf, pos6);
         }

         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[1] & 2) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 58);
         if (fieldOffset7 < 0 || fieldOffset7 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("SkyTopColors", fieldOffset7, maxEnd);
         }

         int pos7 = offset + 126 + fieldOffset7;
         int dictLen = VarInt.peek(buf, pos7);
         pos7 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos7 += 4;
            pos7 += ColorAlpha.computeBytesConsumed(buf, pos7);
         }

         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      if ((nullBits[1] & 4) != 0) {
         int fieldOffset8 = buf.getIntLE(offset + 62);
         if (fieldOffset8 < 0 || fieldOffset8 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("SkyBottomColors", fieldOffset8, maxEnd);
         }

         int pos8 = offset + 126 + fieldOffset8;
         int dictLen = VarInt.peek(buf, pos8);
         pos8 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos8 += 4;
            pos8 += ColorAlpha.computeBytesConsumed(buf, pos8);
         }

         if (pos8 - offset > maxEnd) {
            maxEnd = pos8 - offset;
         }
      }

      if ((nullBits[1] & 8) != 0) {
         int fieldOffset9 = buf.getIntLE(offset + 66);
         if (fieldOffset9 < 0 || fieldOffset9 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("SkySunsetColors", fieldOffset9, maxEnd);
         }

         int pos9 = offset + 126 + fieldOffset9;
         int dictLen = VarInt.peek(buf, pos9);
         pos9 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos9 += 4;
            pos9 += ColorAlpha.computeBytesConsumed(buf, pos9);
         }

         if (pos9 - offset > maxEnd) {
            maxEnd = pos9 - offset;
         }
      }

      if ((nullBits[1] & 16) != 0) {
         int fieldOffset10 = buf.getIntLE(offset + 70);
         if (fieldOffset10 < 0 || fieldOffset10 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("SunColors", fieldOffset10, maxEnd);
         }

         int pos10 = offset + 126 + fieldOffset10;
         int dictLen = VarInt.peek(buf, pos10);
         pos10 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos10 += 4;
            pos10 += Color.computeBytesConsumed(buf, pos10);
         }

         if (pos10 - offset > maxEnd) {
            maxEnd = pos10 - offset;
         }
      }

      if ((nullBits[1] & 32) != 0) {
         int fieldOffset11 = buf.getIntLE(offset + 74);
         if (fieldOffset11 < 0 || fieldOffset11 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("SunScales", fieldOffset11, maxEnd);
         }

         int pos11 = offset + 126 + fieldOffset11;
         int dictLen = VarInt.peek(buf, pos11);
         pos11 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos11 += 4;
            pos11 += 4;
         }

         if (pos11 - offset > maxEnd) {
            maxEnd = pos11 - offset;
         }
      }

      if ((nullBits[1] & 64) != 0) {
         int fieldOffset12 = buf.getIntLE(offset + 78);
         if (fieldOffset12 < 0 || fieldOffset12 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("SunGlowColors", fieldOffset12, maxEnd);
         }

         int pos12 = offset + 126 + fieldOffset12;
         int dictLen = VarInt.peek(buf, pos12);
         pos12 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos12 += 4;
            pos12 += ColorAlpha.computeBytesConsumed(buf, pos12);
         }

         if (pos12 - offset > maxEnd) {
            maxEnd = pos12 - offset;
         }
      }

      if ((nullBits[1] & 128) != 0) {
         int fieldOffset13 = buf.getIntLE(offset + 82);
         if (fieldOffset13 < 0 || fieldOffset13 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("MoonColors", fieldOffset13, maxEnd);
         }

         int pos13 = offset + 126 + fieldOffset13;
         int dictLen = VarInt.peek(buf, pos13);
         pos13 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos13 += 4;
            pos13 += ColorAlpha.computeBytesConsumed(buf, pos13);
         }

         if (pos13 - offset > maxEnd) {
            maxEnd = pos13 - offset;
         }
      }

      if ((nullBits[2] & 1) != 0) {
         int fieldOffset14 = buf.getIntLE(offset + 86);
         if (fieldOffset14 < 0 || fieldOffset14 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("MoonScales", fieldOffset14, maxEnd);
         }

         int pos14 = offset + 126 + fieldOffset14;
         int dictLen = VarInt.peek(buf, pos14);
         pos14 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos14 += 4;
            pos14 += 4;
         }

         if (pos14 - offset > maxEnd) {
            maxEnd = pos14 - offset;
         }
      }

      if ((nullBits[2] & 2) != 0) {
         int fieldOffset15 = buf.getIntLE(offset + 90);
         if (fieldOffset15 < 0 || fieldOffset15 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("MoonGlowColors", fieldOffset15, maxEnd);
         }

         int pos15 = offset + 126 + fieldOffset15;
         int dictLen = VarInt.peek(buf, pos15);
         pos15 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos15 += 4;
            pos15 += ColorAlpha.computeBytesConsumed(buf, pos15);
         }

         if (pos15 - offset > maxEnd) {
            maxEnd = pos15 - offset;
         }
      }

      if ((nullBits[2] & 4) != 0) {
         int fieldOffset16 = buf.getIntLE(offset + 94);
         if (fieldOffset16 < 0 || fieldOffset16 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("FogColors", fieldOffset16, maxEnd);
         }

         int pos16 = offset + 126 + fieldOffset16;
         int dictLen = VarInt.peek(buf, pos16);
         pos16 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos16 += 4;
            pos16 += Color.computeBytesConsumed(buf, pos16);
         }

         if (pos16 - offset > maxEnd) {
            maxEnd = pos16 - offset;
         }
      }

      if ((nullBits[2] & 8) != 0) {
         int fieldOffset17 = buf.getIntLE(offset + 98);
         if (fieldOffset17 < 0 || fieldOffset17 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("FogHeightFalloffs", fieldOffset17, maxEnd);
         }

         int pos17 = offset + 126 + fieldOffset17;
         int dictLen = VarInt.peek(buf, pos17);
         pos17 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos17 += 4;
            pos17 += 4;
         }

         if (pos17 - offset > maxEnd) {
            maxEnd = pos17 - offset;
         }
      }

      if ((nullBits[2] & 16) != 0) {
         int fieldOffset18 = buf.getIntLE(offset + 102);
         if (fieldOffset18 < 0 || fieldOffset18 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("FogDensities", fieldOffset18, maxEnd);
         }

         int pos18 = offset + 126 + fieldOffset18;
         int dictLen = VarInt.peek(buf, pos18);
         pos18 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos18 += 4;
            pos18 += 4;
         }

         if (pos18 - offset > maxEnd) {
            maxEnd = pos18 - offset;
         }
      }

      if ((nullBits[2] & 32) != 0) {
         int fieldOffset19 = buf.getIntLE(offset + 106);
         if (fieldOffset19 < 0 || fieldOffset19 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("ScreenEffect", fieldOffset19, maxEnd);
         }

         int pos19 = offset + 126 + fieldOffset19;
         int sl = VarInt.peek(buf, pos19);
         pos19 += VarInt.size(sl) + sl;
         if (pos19 - offset > maxEnd) {
            maxEnd = pos19 - offset;
         }
      }

      if ((nullBits[2] & 64) != 0) {
         int fieldOffset20 = buf.getIntLE(offset + 110);
         if (fieldOffset20 < 0 || fieldOffset20 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("ScreenEffectColors", fieldOffset20, maxEnd);
         }

         int pos20 = offset + 126 + fieldOffset20;
         int dictLen = VarInt.peek(buf, pos20);
         pos20 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos20 += 4;
            pos20 += ColorAlpha.computeBytesConsumed(buf, pos20);
         }

         if (pos20 - offset > maxEnd) {
            maxEnd = pos20 - offset;
         }
      }

      if ((nullBits[2] & 128) != 0) {
         int fieldOffset21 = buf.getIntLE(offset + 114);
         if (fieldOffset21 < 0 || fieldOffset21 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("ColorFilters", fieldOffset21, maxEnd);
         }

         int pos21 = offset + 126 + fieldOffset21;
         int dictLen = VarInt.peek(buf, pos21);
         pos21 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos21 += 4;
            pos21 += Color.computeBytesConsumed(buf, pos21);
         }

         if (pos21 - offset > maxEnd) {
            maxEnd = pos21 - offset;
         }
      }

      if ((nullBits[3] & 1) != 0) {
         int fieldOffset22 = buf.getIntLE(offset + 118);
         if (fieldOffset22 < 0 || fieldOffset22 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("WaterTints", fieldOffset22, maxEnd);
         }

         int pos22 = offset + 126 + fieldOffset22;
         int dictLen = VarInt.peek(buf, pos22);
         pos22 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos22 += 4;
            pos22 += Color.computeBytesConsumed(buf, pos22);
         }

         if (pos22 - offset > maxEnd) {
            maxEnd = pos22 - offset;
         }
      }

      if ((nullBits[3] & 2) != 0) {
         int fieldOffset23 = buf.getIntLE(offset + 122);
         if (fieldOffset23 < 0 || fieldOffset23 > buf.writerIndex() - offset - 126) {
            throw ProtocolException.invalidOffset("Particle", fieldOffset23, maxEnd);
         }

         int pos23 = offset + 126 + fieldOffset23;
         pos23 += WeatherParticle.computeBytesConsumed(buf, pos23);
         if (pos23 - offset > maxEnd) {
            maxEnd = pos23 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 126L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 30, 126, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static int[] getTagIndexes(MemorySegment mem) {
      return getTagIndexes(mem, 0);
   }

   @Nullable
   public static int[] getTagIndexes(MemorySegment mem, int offset) {
      if (!hasTagIndexes(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 34, 126, "TagIndexes");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("TagIndexes", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("TagIndexes", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("TagIndexes", off + lenOffset + len * 4, (int)mem.byteSize());
            } else {
               off += lenOffset;
               int[] data = new int[len];
               MemorySegment.copy(mem, PacketIO.PROTO_INT, off, data, 0, len);
               return data;
            }
         }
      }
   }

   @Nullable
   public static String getStars(MemorySegment mem) {
      return getStars(mem, 0);
   }

   @Nullable
   public static String getStars(MemorySegment mem, int offset) {
      return hasStars(mem, offset)
         ? PacketIO.readVarString("Stars", mem, offset + getValidatedOffset(mem, offset, 38, 126, "Stars"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static Map<Integer, String> getMoons(MemorySegment mem) {
      return getMoons(mem, 0);
   }

   @Nullable
   public static Map<Integer, String> getMoons(MemorySegment mem, int offset) {
      if (!hasMoons(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 42, 126, "Moons");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Moons", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Moons", len, 4096000);
         } else {
            Map<Integer, String> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               long valuePacked = VarInt.getWithLength(mem, off);
               int nvalue = (int)valuePacked + (int)(valuePacked >>> 32);
               String value = PacketIO.readVarString("value", mem, off, 16384000, PacketIO.UTF8);
               off += nvalue;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Moons", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Cloud[] getClouds(MemorySegment mem) {
      return getClouds(mem, 0);
   }

   @Nullable
   public static Cloud[] getClouds(MemorySegment mem, int offset) {
      if (!hasClouds(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 46, 126, "Clouds");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Clouds", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Clouds", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Clouds", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               Cloud[] data = new Cloud[len];

               for (int i = 0; i < len; i++) {
                  data[i] = Cloud.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static Map<Float, Float> getSunlightDampingMultiplier(MemorySegment mem) {
      return getSunlightDampingMultiplier(mem, 0);
   }

   @Nullable
   public static Map<Float, Float> getSunlightDampingMultiplier(MemorySegment mem, int offset) {
      if (!hasSunlightDampingMultiplier(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 50, 126, "SunlightDampingMultiplier");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SunlightDampingMultiplier", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunlightDampingMultiplier", len, 4096000);
         } else {
            Map<Float, Float> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SunlightDampingMultiplier", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, Color> getSunlightColors(MemorySegment mem) {
      return getSunlightColors(mem, 0);
   }

   @Nullable
   public static Map<Float, Color> getSunlightColors(MemorySegment mem, int offset) {
      if (!hasSunlightColors(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 54, 126, "SunlightColors");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SunlightColors", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunlightColors", len, 4096000);
         } else {
            Map<Float, Color> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               Color value = Color.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SunlightColors", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, ColorAlpha> getSkyTopColors(MemorySegment mem) {
      return getSkyTopColors(mem, 0);
   }

   @Nullable
   public static Map<Float, ColorAlpha> getSkyTopColors(MemorySegment mem, int offset) {
      if (!hasSkyTopColors(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 58, 126, "SkyTopColors");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SkyTopColors", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SkyTopColors", len, 4096000);
         } else {
            Map<Float, ColorAlpha> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SkyTopColors", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, ColorAlpha> getSkyBottomColors(MemorySegment mem) {
      return getSkyBottomColors(mem, 0);
   }

   @Nullable
   public static Map<Float, ColorAlpha> getSkyBottomColors(MemorySegment mem, int offset) {
      if (!hasSkyBottomColors(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 62, 126, "SkyBottomColors");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SkyBottomColors", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SkyBottomColors", len, 4096000);
         } else {
            Map<Float, ColorAlpha> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SkyBottomColors", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, ColorAlpha> getSkySunsetColors(MemorySegment mem) {
      return getSkySunsetColors(mem, 0);
   }

   @Nullable
   public static Map<Float, ColorAlpha> getSkySunsetColors(MemorySegment mem, int offset) {
      if (!hasSkySunsetColors(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 66, 126, "SkySunsetColors");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SkySunsetColors", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SkySunsetColors", len, 4096000);
         } else {
            Map<Float, ColorAlpha> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SkySunsetColors", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, Color> getSunColors(MemorySegment mem) {
      return getSunColors(mem, 0);
   }

   @Nullable
   public static Map<Float, Color> getSunColors(MemorySegment mem, int offset) {
      if (!hasSunColors(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 70, 126, "SunColors");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SunColors", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunColors", len, 4096000);
         } else {
            Map<Float, Color> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               Color value = Color.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SunColors", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, Float> getSunScales(MemorySegment mem) {
      return getSunScales(mem, 0);
   }

   @Nullable
   public static Map<Float, Float> getSunScales(MemorySegment mem, int offset) {
      if (!hasSunScales(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 74, 126, "SunScales");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SunScales", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunScales", len, 4096000);
         } else {
            Map<Float, Float> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SunScales", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, ColorAlpha> getSunGlowColors(MemorySegment mem) {
      return getSunGlowColors(mem, 0);
   }

   @Nullable
   public static Map<Float, ColorAlpha> getSunGlowColors(MemorySegment mem, int offset) {
      if (!hasSunGlowColors(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 78, 126, "SunGlowColors");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SunGlowColors", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunGlowColors", len, 4096000);
         } else {
            Map<Float, ColorAlpha> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SunGlowColors", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, ColorAlpha> getMoonColors(MemorySegment mem) {
      return getMoonColors(mem, 0);
   }

   @Nullable
   public static Map<Float, ColorAlpha> getMoonColors(MemorySegment mem, int offset) {
      if (!hasMoonColors(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 82, 126, "MoonColors");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("MoonColors", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MoonColors", len, 4096000);
         } else {
            Map<Float, ColorAlpha> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("MoonColors", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, Float> getMoonScales(MemorySegment mem) {
      return getMoonScales(mem, 0);
   }

   @Nullable
   public static Map<Float, Float> getMoonScales(MemorySegment mem, int offset) {
      if (!hasMoonScales(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 86, 126, "MoonScales");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("MoonScales", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MoonScales", len, 4096000);
         } else {
            Map<Float, Float> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("MoonScales", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, ColorAlpha> getMoonGlowColors(MemorySegment mem) {
      return getMoonGlowColors(mem, 0);
   }

   @Nullable
   public static Map<Float, ColorAlpha> getMoonGlowColors(MemorySegment mem, int offset) {
      if (!hasMoonGlowColors(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 90, 126, "MoonGlowColors");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("MoonGlowColors", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MoonGlowColors", len, 4096000);
         } else {
            Map<Float, ColorAlpha> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("MoonGlowColors", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, Color> getFogColors(MemorySegment mem) {
      return getFogColors(mem, 0);
   }

   @Nullable
   public static Map<Float, Color> getFogColors(MemorySegment mem, int offset) {
      if (!hasFogColors(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 94, 126, "FogColors");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("FogColors", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FogColors", len, 4096000);
         } else {
            Map<Float, Color> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               Color value = Color.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("FogColors", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, Float> getFogHeightFalloffs(MemorySegment mem) {
      return getFogHeightFalloffs(mem, 0);
   }

   @Nullable
   public static Map<Float, Float> getFogHeightFalloffs(MemorySegment mem, int offset) {
      if (!hasFogHeightFalloffs(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 98, 126, "FogHeightFalloffs");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("FogHeightFalloffs", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FogHeightFalloffs", len, 4096000);
         } else {
            Map<Float, Float> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("FogHeightFalloffs", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, Float> getFogDensities(MemorySegment mem) {
      return getFogDensities(mem, 0);
   }

   @Nullable
   public static Map<Float, Float> getFogDensities(MemorySegment mem, int offset) {
      if (!hasFogDensities(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 102, 126, "FogDensities");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("FogDensities", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FogDensities", len, 4096000);
         } else {
            Map<Float, Float> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("FogDensities", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static String getScreenEffect(MemorySegment mem) {
      return getScreenEffect(mem, 0);
   }

   @Nullable
   public static String getScreenEffect(MemorySegment mem, int offset) {
      return hasScreenEffect(mem, offset)
         ? PacketIO.readVarString("ScreenEffect", mem, offset + getValidatedOffset(mem, offset, 106, 126, "ScreenEffect"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static Map<Float, ColorAlpha> getScreenEffectColors(MemorySegment mem) {
      return getScreenEffectColors(mem, 0);
   }

   @Nullable
   public static Map<Float, ColorAlpha> getScreenEffectColors(MemorySegment mem, int offset) {
      if (!hasScreenEffectColors(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 110, 126, "ScreenEffectColors");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ScreenEffectColors", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ScreenEffectColors", len, 4096000);
         } else {
            Map<Float, ColorAlpha> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ScreenEffectColors", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, Color> getColorFilters(MemorySegment mem) {
      return getColorFilters(mem, 0);
   }

   @Nullable
   public static Map<Float, Color> getColorFilters(MemorySegment mem, int offset) {
      if (!hasColorFilters(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 114, 126, "ColorFilters");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ColorFilters", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ColorFilters", len, 4096000);
         } else {
            Map<Float, Color> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               Color value = Color.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ColorFilters", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, Color> getWaterTints(MemorySegment mem) {
      return getWaterTints(mem, 0);
   }

   @Nullable
   public static Map<Float, Color> getWaterTints(MemorySegment mem, int offset) {
      if (!hasWaterTints(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 118, 126, "WaterTints");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("WaterTints", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("WaterTints", len, 4096000);
         } else {
            Map<Float, Color> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               Color value = Color.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("WaterTints", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static WeatherParticle getParticle(MemorySegment mem) {
      return getParticle(mem, 0);
   }

   @Nullable
   public static WeatherParticle getParticle(MemorySegment mem, int offset) {
      return hasParticle(mem, offset) ? WeatherParticle.toObject(mem, offset + getValidatedOffset(mem, offset, 122, 126, "Particle")) : null;
   }

   @Nullable
   public static NearFar getFog(MemorySegment mem) {
      return getFog(mem, 0);
   }

   @Nullable
   public static NearFar getFog(MemorySegment mem, int offset) {
      return hasFog(mem, offset) ? NearFar.toObject(mem, offset + 4) : null;
   }

   @Nullable
   public static FogOptions getFogOptions(MemorySegment mem) {
      return getFogOptions(mem, 0);
   }

   @Nullable
   public static FogOptions getFogOptions(MemorySegment mem, int offset) {
      return hasFogOptions(mem, offset) ? FogOptions.toObject(mem, offset + 12) : null;
   }

   public static boolean hasFog(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasFogOptions(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasTagIndexes(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasStars(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasMoons(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasClouds(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasSunlightDampingMultiplier(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static boolean hasSunlightColors(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 1) != 0;
   }

   public static boolean hasSkyTopColors(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 2) != 0;
   }

   public static boolean hasSkyBottomColors(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 4) != 0;
   }

   public static boolean hasSkySunsetColors(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 8) != 0;
   }

   public static boolean hasSunColors(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 16) != 0;
   }

   public static boolean hasSunScales(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 32) != 0;
   }

   public static boolean hasSunGlowColors(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 64) != 0;
   }

   public static boolean hasMoonColors(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 128) != 0;
   }

   public static boolean hasMoonScales(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 1) != 0;
   }

   public static boolean hasMoonGlowColors(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 2) != 0;
   }

   public static boolean hasFogColors(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 4) != 0;
   }

   public static boolean hasFogHeightFalloffs(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 8) != 0;
   }

   public static boolean hasFogDensities(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 16) != 0;
   }

   public static boolean hasScreenEffect(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 32) != 0;
   }

   public static boolean hasScreenEffectColors(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 64) != 0;
   }

   public static boolean hasColorFilters(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 128) != 0;
   }

   public static boolean hasWaterTints(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3));
      return (b & 1) != 0;
   }

   public static boolean hasParticle(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static Weather toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Weather toObject(MemorySegment mem, int offset) {
      if (offset + 126 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Weather", offset + 126, (int)mem.byteSize());
      } else {
         int[] tagIndexes = null;
         if (hasTagIndexes(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 34, 126, "TagIndexes");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("TagIndexes", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("TagIndexes", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("TagIndexes", off + lenOffset + len * 4, (int)mem.byteSize());
            }

            off += lenOffset;
            tagIndexes = new int[len];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, off, tagIndexes, 0, len);
         }

         Map<Integer, String> moons = null;
         if (hasMoons(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 42, 126, "Moons");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Moons", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Moons", lenx, 4096000);
            }

            moons = new HashMap<>(lenx);
            offx += (int)(packedx >>> 32);

            for (int i = 0; i < lenx; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)offx);
               offx += 4;
               long valuePacked = VarInt.getWithLength(mem, offx);
               int nvalue = (int)valuePacked + (int)(valuePacked >>> 32);
               String value = PacketIO.readVarString("value", mem, offx, 16384000, PacketIO.UTF8);
               offx += nvalue;
               if (moons.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Moons", key);
               }
            }
         }

         Cloud[] clouds = null;
         if (hasClouds(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 46, 126, "Clouds");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("Clouds", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("Clouds", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Clouds", offxx + lenOffset + lenxx, (int)mem.byteSize());
            }

            offxx += lenOffset;
            clouds = new Cloud[lenxx];

            for (int ix = 0; ix < lenxx; ix++) {
               clouds[ix] = Cloud.toObject(mem, offxx);
               offxx += clouds[ix].computeSize();
            }
         }

         Map<Float, Float> sunlightDampingMultiplier = null;
         if (hasSunlightDampingMultiplier(mem, offset)) {
            int offxxx = offset + getValidatedOffset(mem, offset, 50, 126, "SunlightDampingMultiplier");
            long packedxxx = VarInt.getWithLength(mem, offxxx);
            int lenxxx = (int)packedxxx;
            if (lenxxx < 0) {
               throw ProtocolException.negativeLength("SunlightDampingMultiplier", lenxxx);
            }

            if (lenxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SunlightDampingMultiplier", lenxxx, 4096000);
            }

            sunlightDampingMultiplier = new HashMap<>(lenxxx);
            offxxx += (int)(packedxxx >>> 32);

            for (int ix = 0; ix < lenxxx; ix++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxx);
               offxxx += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)offxxx);
               offxxx += 4;
               if (sunlightDampingMultiplier.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SunlightDampingMultiplier", key);
               }
            }
         }

         Map<Float, Color> sunlightColors = null;
         if (hasSunlightColors(mem, offset)) {
            int offxxxx = offset + getValidatedOffset(mem, offset, 54, 126, "SunlightColors");
            long packedxxxx = VarInt.getWithLength(mem, offxxxx);
            int lenxxxx = (int)packedxxxx;
            if (lenxxxx < 0) {
               throw ProtocolException.negativeLength("SunlightColors", lenxxxx);
            }

            if (lenxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SunlightColors", lenxxxx, 4096000);
            }

            sunlightColors = new HashMap<>(lenxxxx);
            offxxxx += (int)(packedxxxx >>> 32);

            for (int ixx = 0; ixx < lenxxxx; ixx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxx);
               offxxxx += 4;
               Color value = Color.toObject(mem, offxxxx);
               offxxxx += value.computeSize();
               if (sunlightColors.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SunlightColors", key);
               }
            }
         }

         Map<Float, ColorAlpha> skyTopColors = null;
         if (hasSkyTopColors(mem, offset)) {
            int offxxxxx = offset + getValidatedOffset(mem, offset, 58, 126, "SkyTopColors");
            long packedxxxxx = VarInt.getWithLength(mem, offxxxxx);
            int lenxxxxx = (int)packedxxxxx;
            if (lenxxxxx < 0) {
               throw ProtocolException.negativeLength("SkyTopColors", lenxxxxx);
            }

            if (lenxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SkyTopColors", lenxxxxx, 4096000);
            }

            skyTopColors = new HashMap<>(lenxxxxx);
            offxxxxx += (int)(packedxxxxx >>> 32);

            for (int ixxx = 0; ixxx < lenxxxxx; ixxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxx);
               offxxxxx += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, offxxxxx);
               offxxxxx += value.computeSize();
               if (skyTopColors.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SkyTopColors", key);
               }
            }
         }

         Map<Float, ColorAlpha> skyBottomColors = null;
         if (hasSkyBottomColors(mem, offset)) {
            int offxxxxxx = offset + getValidatedOffset(mem, offset, 62, 126, "SkyBottomColors");
            long packedxxxxxx = VarInt.getWithLength(mem, offxxxxxx);
            int lenxxxxxx = (int)packedxxxxxx;
            if (lenxxxxxx < 0) {
               throw ProtocolException.negativeLength("SkyBottomColors", lenxxxxxx);
            }

            if (lenxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SkyBottomColors", lenxxxxxx, 4096000);
            }

            skyBottomColors = new HashMap<>(lenxxxxxx);
            offxxxxxx += (int)(packedxxxxxx >>> 32);

            for (int ixxxx = 0; ixxxx < lenxxxxxx; ixxxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxx);
               offxxxxxx += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, offxxxxxx);
               offxxxxxx += value.computeSize();
               if (skyBottomColors.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SkyBottomColors", key);
               }
            }
         }

         Map<Float, ColorAlpha> skySunsetColors = null;
         if (hasSkySunsetColors(mem, offset)) {
            int offxxxxxxx = offset + getValidatedOffset(mem, offset, 66, 126, "SkySunsetColors");
            long packedxxxxxxx = VarInt.getWithLength(mem, offxxxxxxx);
            int lenxxxxxxx = (int)packedxxxxxxx;
            if (lenxxxxxxx < 0) {
               throw ProtocolException.negativeLength("SkySunsetColors", lenxxxxxxx);
            }

            if (lenxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SkySunsetColors", lenxxxxxxx, 4096000);
            }

            skySunsetColors = new HashMap<>(lenxxxxxxx);
            offxxxxxxx += (int)(packedxxxxxxx >>> 32);

            for (int ixxxxx = 0; ixxxxx < lenxxxxxxx; ixxxxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxx);
               offxxxxxxx += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, offxxxxxxx);
               offxxxxxxx += value.computeSize();
               if (skySunsetColors.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SkySunsetColors", key);
               }
            }
         }

         Map<Float, Color> sunColors = null;
         if (hasSunColors(mem, offset)) {
            int offxxxxxxxx = offset + getValidatedOffset(mem, offset, 70, 126, "SunColors");
            long packedxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxx);
            int lenxxxxxxxx = (int)packedxxxxxxxx;
            if (lenxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("SunColors", lenxxxxxxxx);
            }

            if (lenxxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SunColors", lenxxxxxxxx, 4096000);
            }

            sunColors = new HashMap<>(lenxxxxxxxx);
            offxxxxxxxx += (int)(packedxxxxxxxx >>> 32);

            for (int ixxxxxx = 0; ixxxxxx < lenxxxxxxxx; ixxxxxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxx);
               offxxxxxxxx += 4;
               Color value = Color.toObject(mem, offxxxxxxxx);
               offxxxxxxxx += value.computeSize();
               if (sunColors.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SunColors", key);
               }
            }
         }

         Map<Float, Float> sunScales = null;
         if (hasSunScales(mem, offset)) {
            int offxxxxxxxxx = offset + getValidatedOffset(mem, offset, 74, 126, "SunScales");
            long packedxxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxxx);
            int lenxxxxxxxxx = (int)packedxxxxxxxxx;
            if (lenxxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("SunScales", lenxxxxxxxxx);
            }

            if (lenxxxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SunScales", lenxxxxxxxxx, 4096000);
            }

            sunScales = new HashMap<>(lenxxxxxxxxx);
            offxxxxxxxxx += (int)(packedxxxxxxxxx >>> 32);

            for (int ixxxxxxx = 0; ixxxxxxx < lenxxxxxxxxx; ixxxxxxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxx);
               offxxxxxxxxx += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxx);
               offxxxxxxxxx += 4;
               if (sunScales.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SunScales", key);
               }
            }
         }

         Map<Float, ColorAlpha> sunGlowColors = null;
         if (hasSunGlowColors(mem, offset)) {
            int offxxxxxxxxxx = offset + getValidatedOffset(mem, offset, 78, 126, "SunGlowColors");
            long packedxxxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxxxx);
            int lenxxxxxxxxxx = (int)packedxxxxxxxxxx;
            if (lenxxxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("SunGlowColors", lenxxxxxxxxxx);
            }

            if (lenxxxxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SunGlowColors", lenxxxxxxxxxx, 4096000);
            }

            sunGlowColors = new HashMap<>(lenxxxxxxxxxx);
            offxxxxxxxxxx += (int)(packedxxxxxxxxxx >>> 32);

            for (int ixxxxxxxx = 0; ixxxxxxxx < lenxxxxxxxxxx; ixxxxxxxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxxx);
               offxxxxxxxxxx += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, offxxxxxxxxxx);
               offxxxxxxxxxx += value.computeSize();
               if (sunGlowColors.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SunGlowColors", key);
               }
            }
         }

         Map<Float, ColorAlpha> moonColors = null;
         if (hasMoonColors(mem, offset)) {
            int offxxxxxxxxxxx = offset + getValidatedOffset(mem, offset, 82, 126, "MoonColors");
            long packedxxxxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxxxxx);
            int lenxxxxxxxxxxx = (int)packedxxxxxxxxxxx;
            if (lenxxxxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("MoonColors", lenxxxxxxxxxxx);
            }

            if (lenxxxxxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("MoonColors", lenxxxxxxxxxxx, 4096000);
            }

            moonColors = new HashMap<>(lenxxxxxxxxxxx);
            offxxxxxxxxxxx += (int)(packedxxxxxxxxxxx >>> 32);

            for (int ixxxxxxxxx = 0; ixxxxxxxxx < lenxxxxxxxxxxx; ixxxxxxxxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxxxx);
               offxxxxxxxxxxx += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, offxxxxxxxxxxx);
               offxxxxxxxxxxx += value.computeSize();
               if (moonColors.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("MoonColors", key);
               }
            }
         }

         Map<Float, Float> moonScales = null;
         if (hasMoonScales(mem, offset)) {
            int offxxxxxxxxxxxx = offset + getValidatedOffset(mem, offset, 86, 126, "MoonScales");
            long packedxxxxxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxxxxxx);
            int lenxxxxxxxxxxxx = (int)packedxxxxxxxxxxxx;
            if (lenxxxxxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("MoonScales", lenxxxxxxxxxxxx);
            }

            if (lenxxxxxxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("MoonScales", lenxxxxxxxxxxxx, 4096000);
            }

            moonScales = new HashMap<>(lenxxxxxxxxxxxx);
            offxxxxxxxxxxxx += (int)(packedxxxxxxxxxxxx >>> 32);

            for (int ixxxxxxxxxx = 0; ixxxxxxxxxx < lenxxxxxxxxxxxx; ixxxxxxxxxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxxxxx);
               offxxxxxxxxxxxx += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxxxxx);
               offxxxxxxxxxxxx += 4;
               if (moonScales.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("MoonScales", key);
               }
            }
         }

         Map<Float, ColorAlpha> moonGlowColors = null;
         if (hasMoonGlowColors(mem, offset)) {
            int offxxxxxxxxxxxxx = offset + getValidatedOffset(mem, offset, 90, 126, "MoonGlowColors");
            long packedxxxxxxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxxxxxxx);
            int lenxxxxxxxxxxxxx = (int)packedxxxxxxxxxxxxx;
            if (lenxxxxxxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("MoonGlowColors", lenxxxxxxxxxxxxx);
            }

            if (lenxxxxxxxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("MoonGlowColors", lenxxxxxxxxxxxxx, 4096000);
            }

            moonGlowColors = new HashMap<>(lenxxxxxxxxxxxxx);
            offxxxxxxxxxxxxx += (int)(packedxxxxxxxxxxxxx >>> 32);

            for (int ixxxxxxxxxxx = 0; ixxxxxxxxxxx < lenxxxxxxxxxxxxx; ixxxxxxxxxxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxxxxxx);
               offxxxxxxxxxxxxx += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, offxxxxxxxxxxxxx);
               offxxxxxxxxxxxxx += value.computeSize();
               if (moonGlowColors.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("MoonGlowColors", key);
               }
            }
         }

         Map<Float, Color> fogColors = null;
         if (hasFogColors(mem, offset)) {
            int offxxxxxxxxxxxxxx = offset + getValidatedOffset(mem, offset, 94, 126, "FogColors");
            long packedxxxxxxxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxxxxxxxx);
            int lenxxxxxxxxxxxxxx = (int)packedxxxxxxxxxxxxxx;
            if (lenxxxxxxxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("FogColors", lenxxxxxxxxxxxxxx);
            }

            if (lenxxxxxxxxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("FogColors", lenxxxxxxxxxxxxxx, 4096000);
            }

            fogColors = new HashMap<>(lenxxxxxxxxxxxxxx);
            offxxxxxxxxxxxxxx += (int)(packedxxxxxxxxxxxxxx >>> 32);

            for (int ixxxxxxxxxxxx = 0; ixxxxxxxxxxxx < lenxxxxxxxxxxxxxx; ixxxxxxxxxxxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxxxxxxx);
               offxxxxxxxxxxxxxx += 4;
               Color value = Color.toObject(mem, offxxxxxxxxxxxxxx);
               offxxxxxxxxxxxxxx += value.computeSize();
               if (fogColors.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("FogColors", key);
               }
            }
         }

         Map<Float, Float> fogHeightFalloffs = null;
         if (hasFogHeightFalloffs(mem, offset)) {
            int offxxxxxxxxxxxxxxx = offset + getValidatedOffset(mem, offset, 98, 126, "FogHeightFalloffs");
            long packedxxxxxxxxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxxxxxxxxx);
            int lenxxxxxxxxxxxxxxx = (int)packedxxxxxxxxxxxxxxx;
            if (lenxxxxxxxxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("FogHeightFalloffs", lenxxxxxxxxxxxxxxx);
            }

            if (lenxxxxxxxxxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("FogHeightFalloffs", lenxxxxxxxxxxxxxxx, 4096000);
            }

            fogHeightFalloffs = new HashMap<>(lenxxxxxxxxxxxxxxx);
            offxxxxxxxxxxxxxxx += (int)(packedxxxxxxxxxxxxxxx >>> 32);

            for (int ixxxxxxxxxxxxx = 0; ixxxxxxxxxxxxx < lenxxxxxxxxxxxxxxx; ixxxxxxxxxxxxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxxxxxxxx);
               offxxxxxxxxxxxxxxx += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxxxxxxxx);
               offxxxxxxxxxxxxxxx += 4;
               if (fogHeightFalloffs.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("FogHeightFalloffs", key);
               }
            }
         }

         Map<Float, Float> fogDensities = null;
         if (hasFogDensities(mem, offset)) {
            int offxxxxxxxxxxxxxxxx = offset + getValidatedOffset(mem, offset, 102, 126, "FogDensities");
            long packedxxxxxxxxxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxxxxxxxxxx);
            int lenxxxxxxxxxxxxxxxx = (int)packedxxxxxxxxxxxxxxxx;
            if (lenxxxxxxxxxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("FogDensities", lenxxxxxxxxxxxxxxxx);
            }

            if (lenxxxxxxxxxxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("FogDensities", lenxxxxxxxxxxxxxxxx, 4096000);
            }

            fogDensities = new HashMap<>(lenxxxxxxxxxxxxxxxx);
            offxxxxxxxxxxxxxxxx += (int)(packedxxxxxxxxxxxxxxxx >>> 32);

            for (int ixxxxxxxxxxxxxx = 0; ixxxxxxxxxxxxxx < lenxxxxxxxxxxxxxxxx; ixxxxxxxxxxxxxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxxxxxxxxx);
               offxxxxxxxxxxxxxxxx += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxxxxxxxxx);
               offxxxxxxxxxxxxxxxx += 4;
               if (fogDensities.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("FogDensities", key);
               }
            }
         }

         Map<Float, ColorAlpha> screenEffectColors = null;
         if (hasScreenEffectColors(mem, offset)) {
            int offxxxxxxxxxxxxxxxxx = offset + getValidatedOffset(mem, offset, 110, 126, "ScreenEffectColors");
            long packedxxxxxxxxxxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxxxxxxxxxxx);
            int lenxxxxxxxxxxxxxxxxx = (int)packedxxxxxxxxxxxxxxxxx;
            if (lenxxxxxxxxxxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("ScreenEffectColors", lenxxxxxxxxxxxxxxxxx);
            }

            if (lenxxxxxxxxxxxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ScreenEffectColors", lenxxxxxxxxxxxxxxxxx, 4096000);
            }

            screenEffectColors = new HashMap<>(lenxxxxxxxxxxxxxxxxx);
            offxxxxxxxxxxxxxxxxx += (int)(packedxxxxxxxxxxxxxxxxx >>> 32);

            for (int ixxxxxxxxxxxxxxx = 0; ixxxxxxxxxxxxxxx < lenxxxxxxxxxxxxxxxxx; ixxxxxxxxxxxxxxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxxxxxxxxxx);
               offxxxxxxxxxxxxxxxxx += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, offxxxxxxxxxxxxxxxxx);
               offxxxxxxxxxxxxxxxxx += value.computeSize();
               if (screenEffectColors.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ScreenEffectColors", key);
               }
            }
         }

         Map<Float, Color> colorFilters = null;
         if (hasColorFilters(mem, offset)) {
            int offxxxxxxxxxxxxxxxxxx = offset + getValidatedOffset(mem, offset, 114, 126, "ColorFilters");
            long packedxxxxxxxxxxxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxxxxxxxxxxxx);
            int lenxxxxxxxxxxxxxxxxxx = (int)packedxxxxxxxxxxxxxxxxxx;
            if (lenxxxxxxxxxxxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("ColorFilters", lenxxxxxxxxxxxxxxxxxx);
            }

            if (lenxxxxxxxxxxxxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ColorFilters", lenxxxxxxxxxxxxxxxxxx, 4096000);
            }

            colorFilters = new HashMap<>(lenxxxxxxxxxxxxxxxxxx);
            offxxxxxxxxxxxxxxxxxx += (int)(packedxxxxxxxxxxxxxxxxxx >>> 32);

            for (int ixxxxxxxxxxxxxxxx = 0; ixxxxxxxxxxxxxxxx < lenxxxxxxxxxxxxxxxxxx; ixxxxxxxxxxxxxxxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxxxxxxxxxxx);
               offxxxxxxxxxxxxxxxxxx += 4;
               Color value = Color.toObject(mem, offxxxxxxxxxxxxxxxxxx);
               offxxxxxxxxxxxxxxxxxx += value.computeSize();
               if (colorFilters.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ColorFilters", key);
               }
            }
         }

         Map<Float, Color> waterTints = null;
         if (hasWaterTints(mem, offset)) {
            int offxxxxxxxxxxxxxxxxxxx = offset + getValidatedOffset(mem, offset, 118, 126, "WaterTints");
            long packedxxxxxxxxxxxxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxxxxxxxxxxxxx);
            int lenxxxxxxxxxxxxxxxxxxx = (int)packedxxxxxxxxxxxxxxxxxxx;
            if (lenxxxxxxxxxxxxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("WaterTints", lenxxxxxxxxxxxxxxxxxxx);
            }

            if (lenxxxxxxxxxxxxxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("WaterTints", lenxxxxxxxxxxxxxxxxxxx, 4096000);
            }

            waterTints = new HashMap<>(lenxxxxxxxxxxxxxxxxxxx);
            offxxxxxxxxxxxxxxxxxxx += (int)(packedxxxxxxxxxxxxxxxxxxx >>> 32);

            for (int ixxxxxxxxxxxxxxxxx = 0; ixxxxxxxxxxxxxxxxx < lenxxxxxxxxxxxxxxxxxxx; ixxxxxxxxxxxxxxxxx++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxxxxxxxxxxxxxxxxxxx);
               offxxxxxxxxxxxxxxxxxxx += 4;
               Color value = Color.toObject(mem, offxxxxxxxxxxxxxxxxxxx);
               offxxxxxxxxxxxxxxxxxxx += value.computeSize();
               if (waterTints.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("WaterTints", key);
               }
            }
         }

         return new Weather(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 30, 126, "Id"), 4096000, PacketIO.UTF8) : null,
            tagIndexes,
            hasStars(mem, offset)
               ? PacketIO.readVarString("Stars", mem, offset + getValidatedOffset(mem, offset, 38, 126, "Stars"), 4096000, PacketIO.UTF8)
               : null,
            moons,
            clouds,
            sunlightDampingMultiplier,
            sunlightColors,
            skyTopColors,
            skyBottomColors,
            skySunsetColors,
            sunColors,
            sunScales,
            sunGlowColors,
            moonColors,
            moonScales,
            moonGlowColors,
            fogColors,
            fogHeightFalloffs,
            fogDensities,
            hasScreenEffect(mem, offset)
               ? PacketIO.readVarString("ScreenEffect", mem, offset + getValidatedOffset(mem, offset, 106, 126, "ScreenEffect"), 4096000, PacketIO.UTF8)
               : null,
            screenEffectColors,
            colorFilters,
            waterTints,
            hasParticle(mem, offset) ? WeatherParticle.toObject(mem, offset + getValidatedOffset(mem, offset, 122, 126, "Particle")) : null,
            hasFog(mem, offset) ? NearFar.toObject(mem, offset + 4) : null,
            hasFogOptions(mem, offset) ? FogOptions.toObject(mem, offset + 12) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[4];
      if (this.fog != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.fogOptions != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.id != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.tagIndexes != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.stars != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.moons != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.clouds != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.sunlightDampingMultiplier != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.sunlightColors != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      if (this.skyTopColors != null) {
         nullBits[1] = (byte)(nullBits[1] | 2);
      }

      if (this.skyBottomColors != null) {
         nullBits[1] = (byte)(nullBits[1] | 4);
      }

      if (this.skySunsetColors != null) {
         nullBits[1] = (byte)(nullBits[1] | 8);
      }

      if (this.sunColors != null) {
         nullBits[1] = (byte)(nullBits[1] | 16);
      }

      if (this.sunScales != null) {
         nullBits[1] = (byte)(nullBits[1] | 32);
      }

      if (this.sunGlowColors != null) {
         nullBits[1] = (byte)(nullBits[1] | 64);
      }

      if (this.moonColors != null) {
         nullBits[1] = (byte)(nullBits[1] | 128);
      }

      if (this.moonScales != null) {
         nullBits[2] = (byte)(nullBits[2] | 1);
      }

      if (this.moonGlowColors != null) {
         nullBits[2] = (byte)(nullBits[2] | 2);
      }

      if (this.fogColors != null) {
         nullBits[2] = (byte)(nullBits[2] | 4);
      }

      if (this.fogHeightFalloffs != null) {
         nullBits[2] = (byte)(nullBits[2] | 8);
      }

      if (this.fogDensities != null) {
         nullBits[2] = (byte)(nullBits[2] | 16);
      }

      if (this.screenEffect != null) {
         nullBits[2] = (byte)(nullBits[2] | 32);
      }

      if (this.screenEffectColors != null) {
         nullBits[2] = (byte)(nullBits[2] | 64);
      }

      if (this.colorFilters != null) {
         nullBits[2] = (byte)(nullBits[2] | 128);
      }

      if (this.waterTints != null) {
         nullBits[3] = (byte)(nullBits[3] | 1);
      }

      if (this.particle != null) {
         nullBits[3] = (byte)(nullBits[3] | 2);
      }

      buf.writeBytes(nullBits);
      if (this.fog != null) {
         this.fog.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.fogOptions != null) {
         this.fogOptions.serialize(buf);
      } else {
         buf.writeZero(18);
      }

      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int tagIndexesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int starsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int moonsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int cloudsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int sunlightDampingMultiplierOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int sunlightColorsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int skyTopColorsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int skyBottomColorsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int skySunsetColorsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int sunColorsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int sunScalesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int sunGlowColorsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int moonColorsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int moonScalesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int moonGlowColorsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int fogColorsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int fogHeightFalloffsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int fogDensitiesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int screenEffectOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int screenEffectColorsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int colorFiltersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int waterTintsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int particleOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.tagIndexes != null) {
         buf.setIntLE(tagIndexesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.tagIndexes.length > 4096000) {
            throw ProtocolException.arrayTooLong("TagIndexes", this.tagIndexes.length, 4096000);
         }

         VarInt.write(buf, this.tagIndexes.length);

         for (int item : this.tagIndexes) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(tagIndexesOffsetSlot, -1);
      }

      if (this.stars != null) {
         buf.setIntLE(starsOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.stars, 4096000);
      } else {
         buf.setIntLE(starsOffsetSlot, -1);
      }

      if (this.moons != null) {
         buf.setIntLE(moonsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.moons.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Moons", this.moons.size(), 4096000);
         }

         VarInt.write(buf, this.moons.size());

         for (Entry<Integer, String> e : this.moons.entrySet()) {
            buf.writeIntLE(e.getKey());
            PacketIO.writeVarString(buf, e.getValue(), 4096000);
         }
      } else {
         buf.setIntLE(moonsOffsetSlot, -1);
      }

      if (this.clouds != null) {
         buf.setIntLE(cloudsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.clouds.length > 4096000) {
            throw ProtocolException.arrayTooLong("Clouds", this.clouds.length, 4096000);
         }

         VarInt.write(buf, this.clouds.length);

         for (Cloud item : this.clouds) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(cloudsOffsetSlot, -1);
      }

      if (this.sunlightDampingMultiplier != null) {
         buf.setIntLE(sunlightDampingMultiplierOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.sunlightDampingMultiplier.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunlightDampingMultiplier", this.sunlightDampingMultiplier.size(), 4096000);
         }

         VarInt.write(buf, this.sunlightDampingMultiplier.size());

         for (Entry<Float, Float> e : this.sunlightDampingMultiplier.entrySet()) {
            buf.writeFloatLE(e.getKey());
            buf.writeFloatLE(e.getValue());
         }
      } else {
         buf.setIntLE(sunlightDampingMultiplierOffsetSlot, -1);
      }

      if (this.sunlightColors != null) {
         buf.setIntLE(sunlightColorsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.sunlightColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunlightColors", this.sunlightColors.size(), 4096000);
         }

         VarInt.write(buf, this.sunlightColors.size());

         for (Entry<Float, Color> e : this.sunlightColors.entrySet()) {
            buf.writeFloatLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(sunlightColorsOffsetSlot, -1);
      }

      if (this.skyTopColors != null) {
         buf.setIntLE(skyTopColorsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.skyTopColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SkyTopColors", this.skyTopColors.size(), 4096000);
         }

         VarInt.write(buf, this.skyTopColors.size());

         for (Entry<Float, ColorAlpha> e : this.skyTopColors.entrySet()) {
            buf.writeFloatLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(skyTopColorsOffsetSlot, -1);
      }

      if (this.skyBottomColors != null) {
         buf.setIntLE(skyBottomColorsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.skyBottomColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SkyBottomColors", this.skyBottomColors.size(), 4096000);
         }

         VarInt.write(buf, this.skyBottomColors.size());

         for (Entry<Float, ColorAlpha> e : this.skyBottomColors.entrySet()) {
            buf.writeFloatLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(skyBottomColorsOffsetSlot, -1);
      }

      if (this.skySunsetColors != null) {
         buf.setIntLE(skySunsetColorsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.skySunsetColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SkySunsetColors", this.skySunsetColors.size(), 4096000);
         }

         VarInt.write(buf, this.skySunsetColors.size());

         for (Entry<Float, ColorAlpha> e : this.skySunsetColors.entrySet()) {
            buf.writeFloatLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(skySunsetColorsOffsetSlot, -1);
      }

      if (this.sunColors != null) {
         buf.setIntLE(sunColorsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.sunColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunColors", this.sunColors.size(), 4096000);
         }

         VarInt.write(buf, this.sunColors.size());

         for (Entry<Float, Color> e : this.sunColors.entrySet()) {
            buf.writeFloatLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(sunColorsOffsetSlot, -1);
      }

      if (this.sunScales != null) {
         buf.setIntLE(sunScalesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.sunScales.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunScales", this.sunScales.size(), 4096000);
         }

         VarInt.write(buf, this.sunScales.size());

         for (Entry<Float, Float> e : this.sunScales.entrySet()) {
            buf.writeFloatLE(e.getKey());
            buf.writeFloatLE(e.getValue());
         }
      } else {
         buf.setIntLE(sunScalesOffsetSlot, -1);
      }

      if (this.sunGlowColors != null) {
         buf.setIntLE(sunGlowColorsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.sunGlowColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunGlowColors", this.sunGlowColors.size(), 4096000);
         }

         VarInt.write(buf, this.sunGlowColors.size());

         for (Entry<Float, ColorAlpha> e : this.sunGlowColors.entrySet()) {
            buf.writeFloatLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(sunGlowColorsOffsetSlot, -1);
      }

      if (this.moonColors != null) {
         buf.setIntLE(moonColorsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.moonColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MoonColors", this.moonColors.size(), 4096000);
         }

         VarInt.write(buf, this.moonColors.size());

         for (Entry<Float, ColorAlpha> e : this.moonColors.entrySet()) {
            buf.writeFloatLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(moonColorsOffsetSlot, -1);
      }

      if (this.moonScales != null) {
         buf.setIntLE(moonScalesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.moonScales.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MoonScales", this.moonScales.size(), 4096000);
         }

         VarInt.write(buf, this.moonScales.size());

         for (Entry<Float, Float> e : this.moonScales.entrySet()) {
            buf.writeFloatLE(e.getKey());
            buf.writeFloatLE(e.getValue());
         }
      } else {
         buf.setIntLE(moonScalesOffsetSlot, -1);
      }

      if (this.moonGlowColors != null) {
         buf.setIntLE(moonGlowColorsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.moonGlowColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MoonGlowColors", this.moonGlowColors.size(), 4096000);
         }

         VarInt.write(buf, this.moonGlowColors.size());

         for (Entry<Float, ColorAlpha> e : this.moonGlowColors.entrySet()) {
            buf.writeFloatLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(moonGlowColorsOffsetSlot, -1);
      }

      if (this.fogColors != null) {
         buf.setIntLE(fogColorsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.fogColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FogColors", this.fogColors.size(), 4096000);
         }

         VarInt.write(buf, this.fogColors.size());

         for (Entry<Float, Color> e : this.fogColors.entrySet()) {
            buf.writeFloatLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(fogColorsOffsetSlot, -1);
      }

      if (this.fogHeightFalloffs != null) {
         buf.setIntLE(fogHeightFalloffsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.fogHeightFalloffs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FogHeightFalloffs", this.fogHeightFalloffs.size(), 4096000);
         }

         VarInt.write(buf, this.fogHeightFalloffs.size());

         for (Entry<Float, Float> e : this.fogHeightFalloffs.entrySet()) {
            buf.writeFloatLE(e.getKey());
            buf.writeFloatLE(e.getValue());
         }
      } else {
         buf.setIntLE(fogHeightFalloffsOffsetSlot, -1);
      }

      if (this.fogDensities != null) {
         buf.setIntLE(fogDensitiesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.fogDensities.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FogDensities", this.fogDensities.size(), 4096000);
         }

         VarInt.write(buf, this.fogDensities.size());

         for (Entry<Float, Float> e : this.fogDensities.entrySet()) {
            buf.writeFloatLE(e.getKey());
            buf.writeFloatLE(e.getValue());
         }
      } else {
         buf.setIntLE(fogDensitiesOffsetSlot, -1);
      }

      if (this.screenEffect != null) {
         buf.setIntLE(screenEffectOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.screenEffect, 4096000);
      } else {
         buf.setIntLE(screenEffectOffsetSlot, -1);
      }

      if (this.screenEffectColors != null) {
         buf.setIntLE(screenEffectColorsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.screenEffectColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ScreenEffectColors", this.screenEffectColors.size(), 4096000);
         }

         VarInt.write(buf, this.screenEffectColors.size());

         for (Entry<Float, ColorAlpha> e : this.screenEffectColors.entrySet()) {
            buf.writeFloatLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(screenEffectColorsOffsetSlot, -1);
      }

      if (this.colorFilters != null) {
         buf.setIntLE(colorFiltersOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.colorFilters.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ColorFilters", this.colorFilters.size(), 4096000);
         }

         VarInt.write(buf, this.colorFilters.size());

         for (Entry<Float, Color> e : this.colorFilters.entrySet()) {
            buf.writeFloatLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(colorFiltersOffsetSlot, -1);
      }

      if (this.waterTints != null) {
         buf.setIntLE(waterTintsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.waterTints.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("WaterTints", this.waterTints.size(), 4096000);
         }

         VarInt.write(buf, this.waterTints.size());

         for (Entry<Float, Color> e : this.waterTints.entrySet()) {
            buf.writeFloatLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(waterTintsOffsetSlot, -1);
      }

      if (this.particle != null) {
         buf.setIntLE(particleOffsetSlot, buf.writerIndex() - varBlockStart);
         this.particle.serialize(buf);
      } else {
         buf.setIntLE(particleOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.fog != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.fogOptions != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.tagIndexes != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.stars != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.moons != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.clouds != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.sunlightDampingMultiplier != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      nullBits = 0;
      if (this.sunlightColors != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.skyTopColors != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.skyBottomColors != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.skySunsetColors != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.sunColors != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.sunScales != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.sunGlowColors != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.moonColors != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), nullBits);
      nullBits = 0;
      if (this.moonScales != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.moonGlowColors != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.fogColors != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.fogHeightFalloffs != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.fogDensities != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.screenEffect != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.screenEffectColors != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.colorFilters != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), nullBits);
      nullBits = 0;
      if (this.waterTints != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.particle != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 3), nullBits);
      if (this.fog != null) {
         this.fog.serialize(mem, offset + 4);
      } else {
         mem.asSlice(offset + 4, 8L).fill((byte)0);
      }

      if (this.fogOptions != null) {
         this.fogOptions.serialize(mem, offset + 12);
      } else {
         mem.asSlice(offset + 12, 18L).fill((byte)0);
      }

      int varOffset = offset + 126;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 30), varOffset - offset - 126);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 30), -1);
      }

      if (this.tagIndexes != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 34), varOffset - offset - 126);
         if (this.tagIndexes.length > 4096000) {
            throw ProtocolException.arrayTooLong("TagIndexes", this.tagIndexes.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.tagIndexes.length);
         MemorySegment.copy(this.tagIndexes, 0, mem, PacketIO.PROTO_INT, varOffset, this.tagIndexes.length);
         varOffset += this.tagIndexes.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 34), -1);
      }

      if (this.stars != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 38), varOffset - offset - 126);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.stars, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 38), -1);
      }

      if (this.moons != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 42), varOffset - offset - 126);
         if (this.moons.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Moons", this.moons.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.moons.size());

         for (Entry<Integer, String> e : this.moons.entrySet()) {
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getValue(), 16384000);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 42), -1);
      }

      if (this.clouds != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 46), varOffset - offset - 126);
         if (this.clouds.length > 4096000) {
            throw ProtocolException.arrayTooLong("Clouds", this.clouds.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.clouds.length);
         int cloudsValueOffset = 0;

         for (int i = 0; i < this.clouds.length; i++) {
            cloudsValueOffset += this.clouds[i].serialize(mem, varOffset + cloudsValueOffset);
         }

         varOffset += cloudsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 46), -1);
      }

      if (this.sunlightDampingMultiplier != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 50), varOffset - offset - 126);
         if (this.sunlightDampingMultiplier.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunlightDampingMultiplier", this.sunlightDampingMultiplier.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.sunlightDampingMultiplier.size());

         for (Entry<Float, Float> e : this.sunlightDampingMultiplier.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 50), -1);
      }

      if (this.sunlightColors != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 54), varOffset - offset - 126);
         if (this.sunlightColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunlightColors", this.sunlightColors.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.sunlightColors.size());

         for (Entry<Float, Color> e : this.sunlightColors.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 54), -1);
      }

      if (this.skyTopColors != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 58), varOffset - offset - 126);
         if (this.skyTopColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SkyTopColors", this.skyTopColors.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.skyTopColors.size());

         for (Entry<Float, ColorAlpha> e : this.skyTopColors.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 58), -1);
      }

      if (this.skyBottomColors != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 62), varOffset - offset - 126);
         if (this.skyBottomColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SkyBottomColors", this.skyBottomColors.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.skyBottomColors.size());

         for (Entry<Float, ColorAlpha> e : this.skyBottomColors.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 62), -1);
      }

      if (this.skySunsetColors != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 66), varOffset - offset - 126);
         if (this.skySunsetColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SkySunsetColors", this.skySunsetColors.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.skySunsetColors.size());

         for (Entry<Float, ColorAlpha> e : this.skySunsetColors.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 66), -1);
      }

      if (this.sunColors != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 70), varOffset - offset - 126);
         if (this.sunColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunColors", this.sunColors.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.sunColors.size());

         for (Entry<Float, Color> e : this.sunColors.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 70), -1);
      }

      if (this.sunScales != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 74), varOffset - offset - 126);
         if (this.sunScales.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunScales", this.sunScales.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.sunScales.size());

         for (Entry<Float, Float> e : this.sunScales.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 74), -1);
      }

      if (this.sunGlowColors != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 78), varOffset - offset - 126);
         if (this.sunGlowColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunGlowColors", this.sunGlowColors.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.sunGlowColors.size());

         for (Entry<Float, ColorAlpha> e : this.sunGlowColors.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 78), -1);
      }

      if (this.moonColors != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 82), varOffset - offset - 126);
         if (this.moonColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MoonColors", this.moonColors.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.moonColors.size());

         for (Entry<Float, ColorAlpha> e : this.moonColors.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 82), -1);
      }

      if (this.moonScales != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 86), varOffset - offset - 126);
         if (this.moonScales.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MoonScales", this.moonScales.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.moonScales.size());

         for (Entry<Float, Float> e : this.moonScales.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 86), -1);
      }

      if (this.moonGlowColors != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 90), varOffset - offset - 126);
         if (this.moonGlowColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MoonGlowColors", this.moonGlowColors.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.moonGlowColors.size());

         for (Entry<Float, ColorAlpha> e : this.moonGlowColors.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 90), -1);
      }

      if (this.fogColors != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 94), varOffset - offset - 126);
         if (this.fogColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FogColors", this.fogColors.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.fogColors.size());

         for (Entry<Float, Color> e : this.fogColors.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 94), -1);
      }

      if (this.fogHeightFalloffs != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 98), varOffset - offset - 126);
         if (this.fogHeightFalloffs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FogHeightFalloffs", this.fogHeightFalloffs.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.fogHeightFalloffs.size());

         for (Entry<Float, Float> e : this.fogHeightFalloffs.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 98), -1);
      }

      if (this.fogDensities != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 102), varOffset - offset - 126);
         if (this.fogDensities.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FogDensities", this.fogDensities.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.fogDensities.size());

         for (Entry<Float, Float> e : this.fogDensities.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 102), -1);
      }

      if (this.screenEffect != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 106), varOffset - offset - 126);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.screenEffect, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 106), -1);
      }

      if (this.screenEffectColors != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 110), varOffset - offset - 126);
         if (this.screenEffectColors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ScreenEffectColors", this.screenEffectColors.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.screenEffectColors.size());

         for (Entry<Float, ColorAlpha> e : this.screenEffectColors.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 110), -1);
      }

      if (this.colorFilters != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 114), varOffset - offset - 126);
         if (this.colorFilters.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ColorFilters", this.colorFilters.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.colorFilters.size());

         for (Entry<Float, Color> e : this.colorFilters.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 114), -1);
      }

      if (this.waterTints != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 118), varOffset - offset - 126);
         if (this.waterTints.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("WaterTints", this.waterTints.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.waterTints.size());

         for (Entry<Float, Color> e : this.waterTints.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 118), -1);
      }

      if (this.particle != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 122), varOffset - offset - 126);
         varOffset += this.particle.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 122), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 126;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.tagIndexes != null) {
         size += VarInt.size(this.tagIndexes.length) + this.tagIndexes.length * 4;
      }

      if (this.stars != null) {
         size += PacketIO.stringSize(this.stars);
      }

      if (this.moons != null) {
         int moonsSize = 0;

         for (Entry<Integer, String> kvp : this.moons.entrySet()) {
            moonsSize += 4 + PacketIO.stringSize(kvp.getValue());
         }

         size += VarInt.size(this.moons.size()) + moonsSize;
      }

      if (this.clouds != null) {
         int cloudsSize = 0;

         for (Cloud elem : this.clouds) {
            cloudsSize += elem.computeSize();
         }

         size += VarInt.size(this.clouds.length) + cloudsSize;
      }

      if (this.sunlightDampingMultiplier != null) {
         size += VarInt.size(this.sunlightDampingMultiplier.size()) + this.sunlightDampingMultiplier.size() * 8;
      }

      if (this.sunlightColors != null) {
         size += VarInt.size(this.sunlightColors.size()) + this.sunlightColors.size() * 7;
      }

      if (this.skyTopColors != null) {
         size += VarInt.size(this.skyTopColors.size()) + this.skyTopColors.size() * 8;
      }

      if (this.skyBottomColors != null) {
         size += VarInt.size(this.skyBottomColors.size()) + this.skyBottomColors.size() * 8;
      }

      if (this.skySunsetColors != null) {
         size += VarInt.size(this.skySunsetColors.size()) + this.skySunsetColors.size() * 8;
      }

      if (this.sunColors != null) {
         size += VarInt.size(this.sunColors.size()) + this.sunColors.size() * 7;
      }

      if (this.sunScales != null) {
         size += VarInt.size(this.sunScales.size()) + this.sunScales.size() * 8;
      }

      if (this.sunGlowColors != null) {
         size += VarInt.size(this.sunGlowColors.size()) + this.sunGlowColors.size() * 8;
      }

      if (this.moonColors != null) {
         size += VarInt.size(this.moonColors.size()) + this.moonColors.size() * 8;
      }

      if (this.moonScales != null) {
         size += VarInt.size(this.moonScales.size()) + this.moonScales.size() * 8;
      }

      if (this.moonGlowColors != null) {
         size += VarInt.size(this.moonGlowColors.size()) + this.moonGlowColors.size() * 8;
      }

      if (this.fogColors != null) {
         size += VarInt.size(this.fogColors.size()) + this.fogColors.size() * 7;
      }

      if (this.fogHeightFalloffs != null) {
         size += VarInt.size(this.fogHeightFalloffs.size()) + this.fogHeightFalloffs.size() * 8;
      }

      if (this.fogDensities != null) {
         size += VarInt.size(this.fogDensities.size()) + this.fogDensities.size() * 8;
      }

      if (this.screenEffect != null) {
         size += PacketIO.stringSize(this.screenEffect);
      }

      if (this.screenEffectColors != null) {
         size += VarInt.size(this.screenEffectColors.size()) + this.screenEffectColors.size() * 8;
      }

      if (this.colorFilters != null) {
         size += VarInt.size(this.colorFilters.size()) + this.colorFilters.size() * 7;
      }

      if (this.waterTints != null) {
         size += VarInt.size(this.waterTints.size()) + this.waterTints.size() * 7;
      }

      if (this.particle != null) {
         size += this.particle.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 126) {
         return ValidationResult.error("Buffer too small: expected at least 126 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 4);
         if ((nullBits[0] & 4) != 0) {
            int idOffset = buffer.getIntLE(offset + 30);
            if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 126 + idOffset;
            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.size(idLen);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits[0] & 8) != 0) {
            int tagIndexesOffset = buffer.getIntLE(offset + 34);
            if (tagIndexesOffset < 0 || tagIndexesOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for TagIndexes");
            }

            int posx = offset + 126 + tagIndexesOffset;
            int tagIndexesCount = VarInt.peek(buffer, posx);
            if (tagIndexesCount < 0) {
               return ValidationResult.error("Invalid array count for TagIndexes");
            }

            if (tagIndexesCount > 4096000) {
               return ValidationResult.error("TagIndexes exceeds max length 4096000");
            }

            posx += VarInt.size(tagIndexesCount);
            posx += tagIndexesCount * 4;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading TagIndexes");
            }
         }

         if ((nullBits[0] & 16) != 0) {
            int starsOffset = buffer.getIntLE(offset + 38);
            if (starsOffset < 0 || starsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for Stars");
            }

            int posxx = offset + 126 + starsOffset;
            int starsLen = VarInt.peek(buffer, posxx);
            if (starsLen < 0) {
               return ValidationResult.error("Invalid string length for Stars");
            }

            if (starsLen > 4096000) {
               return ValidationResult.error("Stars exceeds max length 4096000");
            }

            posxx += VarInt.size(starsLen);
            posxx += starsLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Stars");
            }
         }

         if ((nullBits[0] & 32) != 0) {
            int moonsOffset = buffer.getIntLE(offset + 42);
            if (moonsOffset < 0 || moonsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for Moons");
            }

            int posxxx = offset + 126 + moonsOffset;
            int moonsCount = VarInt.peek(buffer, posxxx);
            if (moonsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Moons");
            }

            if (moonsCount > 4096000) {
               return ValidationResult.error("Moons exceeds max length 4096000");
            }

            posxxx += VarInt.size(moonsCount);

            for (int i = 0; i < moonsCount; i++) {
               posxxx += 4;
               if (posxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               int valueLen = VarInt.peek(buffer, posxxx);
               if (valueLen < 0) {
                  return ValidationResult.error("Invalid string length for value");
               }

               if (valueLen > 4096000) {
                  return ValidationResult.error("value exceeds max length 4096000");
               }

               posxxx += VarInt.size(valueLen);
               posxxx += valueLen;
               if (posxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         if ((nullBits[0] & 64) != 0) {
            int cloudsOffset = buffer.getIntLE(offset + 46);
            if (cloudsOffset < 0 || cloudsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for Clouds");
            }

            int posxxxx = offset + 126 + cloudsOffset;
            int cloudsCount = VarInt.peek(buffer, posxxxx);
            if (cloudsCount < 0) {
               return ValidationResult.error("Invalid array count for Clouds");
            }

            if (cloudsCount > 4096000) {
               return ValidationResult.error("Clouds exceeds max length 4096000");
            }

            posxxxx += VarInt.size(cloudsCount);

            for (int i = 0; i < cloudsCount; i++) {
               ValidationResult structResult = Cloud.validateStructure(buffer, posxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid Cloud in Clouds[" + i + "]: " + structResult.error());
               }

               posxxxx += Cloud.computeBytesConsumed(buffer, posxxxx);
            }
         }

         if ((nullBits[0] & 128) != 0) {
            int sunlightDampingMultiplierOffset = buffer.getIntLE(offset + 50);
            if (sunlightDampingMultiplierOffset < 0 || sunlightDampingMultiplierOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for SunlightDampingMultiplier");
            }

            int posxxxxx = offset + 126 + sunlightDampingMultiplierOffset;
            int sunlightDampingMultiplierCount = VarInt.peek(buffer, posxxxxx);
            if (sunlightDampingMultiplierCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SunlightDampingMultiplier");
            }

            if (sunlightDampingMultiplierCount > 4096000) {
               return ValidationResult.error("SunlightDampingMultiplier exceeds max length 4096000");
            }

            posxxxxx += VarInt.size(sunlightDampingMultiplierCount);

            for (int i = 0; i < sunlightDampingMultiplierCount; i++) {
               posxxxxx += 4;
               if (posxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxx += 4;
               if (posxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         if ((nullBits[1] & 1) != 0) {
            int sunlightColorsOffset = buffer.getIntLE(offset + 54);
            if (sunlightColorsOffset < 0 || sunlightColorsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for SunlightColors");
            }

            int posxxxxxx = offset + 126 + sunlightColorsOffset;
            int sunlightColorsCount = VarInt.peek(buffer, posxxxxxx);
            if (sunlightColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SunlightColors");
            }

            if (sunlightColorsCount > 4096000) {
               return ValidationResult.error("SunlightColors exceeds max length 4096000");
            }

            posxxxxxx += VarInt.size(sunlightColorsCount);

            for (int i = 0; i < sunlightColorsCount; i++) {
               posxxxxxx += 4;
               if (posxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxx += 3;
            }
         }

         if ((nullBits[1] & 2) != 0) {
            int skyTopColorsOffset = buffer.getIntLE(offset + 58);
            if (skyTopColorsOffset < 0 || skyTopColorsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for SkyTopColors");
            }

            int posxxxxxxx = offset + 126 + skyTopColorsOffset;
            int skyTopColorsCount = VarInt.peek(buffer, posxxxxxxx);
            if (skyTopColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SkyTopColors");
            }

            if (skyTopColorsCount > 4096000) {
               return ValidationResult.error("SkyTopColors exceeds max length 4096000");
            }

            posxxxxxxx += VarInt.size(skyTopColorsCount);

            for (int i = 0; i < skyTopColorsCount; i++) {
               posxxxxxxx += 4;
               if (posxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxx += 4;
            }
         }

         if ((nullBits[1] & 4) != 0) {
            int skyBottomColorsOffset = buffer.getIntLE(offset + 62);
            if (skyBottomColorsOffset < 0 || skyBottomColorsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for SkyBottomColors");
            }

            int posxxxxxxxx = offset + 126 + skyBottomColorsOffset;
            int skyBottomColorsCount = VarInt.peek(buffer, posxxxxxxxx);
            if (skyBottomColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SkyBottomColors");
            }

            if (skyBottomColorsCount > 4096000) {
               return ValidationResult.error("SkyBottomColors exceeds max length 4096000");
            }

            posxxxxxxxx += VarInt.size(skyBottomColorsCount);

            for (int i = 0; i < skyBottomColorsCount; i++) {
               posxxxxxxxx += 4;
               if (posxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxx += 4;
            }
         }

         if ((nullBits[1] & 8) != 0) {
            int skySunsetColorsOffset = buffer.getIntLE(offset + 66);
            if (skySunsetColorsOffset < 0 || skySunsetColorsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for SkySunsetColors");
            }

            int posxxxxxxxxx = offset + 126 + skySunsetColorsOffset;
            int skySunsetColorsCount = VarInt.peek(buffer, posxxxxxxxxx);
            if (skySunsetColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SkySunsetColors");
            }

            if (skySunsetColorsCount > 4096000) {
               return ValidationResult.error("SkySunsetColors exceeds max length 4096000");
            }

            posxxxxxxxxx += VarInt.size(skySunsetColorsCount);

            for (int i = 0; i < skySunsetColorsCount; i++) {
               posxxxxxxxxx += 4;
               if (posxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxxx += 4;
            }
         }

         if ((nullBits[1] & 16) != 0) {
            int sunColorsOffset = buffer.getIntLE(offset + 70);
            if (sunColorsOffset < 0 || sunColorsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for SunColors");
            }

            int posxxxxxxxxxx = offset + 126 + sunColorsOffset;
            int sunColorsCount = VarInt.peek(buffer, posxxxxxxxxxx);
            if (sunColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SunColors");
            }

            if (sunColorsCount > 4096000) {
               return ValidationResult.error("SunColors exceeds max length 4096000");
            }

            posxxxxxxxxxx += VarInt.size(sunColorsCount);

            for (int i = 0; i < sunColorsCount; i++) {
               posxxxxxxxxxx += 4;
               if (posxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxxxx += 3;
            }
         }

         if ((nullBits[1] & 32) != 0) {
            int sunScalesOffset = buffer.getIntLE(offset + 74);
            if (sunScalesOffset < 0 || sunScalesOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for SunScales");
            }

            int posxxxxxxxxxxx = offset + 126 + sunScalesOffset;
            int sunScalesCount = VarInt.peek(buffer, posxxxxxxxxxxx);
            if (sunScalesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SunScales");
            }

            if (sunScalesCount > 4096000) {
               return ValidationResult.error("SunScales exceeds max length 4096000");
            }

            posxxxxxxxxxxx += VarInt.size(sunScalesCount);

            for (int i = 0; i < sunScalesCount; i++) {
               posxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         if ((nullBits[1] & 64) != 0) {
            int sunGlowColorsOffset = buffer.getIntLE(offset + 78);
            if (sunGlowColorsOffset < 0 || sunGlowColorsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for SunGlowColors");
            }

            int posxxxxxxxxxxxx = offset + 126 + sunGlowColorsOffset;
            int sunGlowColorsCount = VarInt.peek(buffer, posxxxxxxxxxxxx);
            if (sunGlowColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SunGlowColors");
            }

            if (sunGlowColorsCount > 4096000) {
               return ValidationResult.error("SunGlowColors exceeds max length 4096000");
            }

            posxxxxxxxxxxxx += VarInt.size(sunGlowColorsCount);

            for (int i = 0; i < sunGlowColorsCount; i++) {
               posxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxxxxxx += 4;
            }
         }

         if ((nullBits[1] & 128) != 0) {
            int moonColorsOffset = buffer.getIntLE(offset + 82);
            if (moonColorsOffset < 0 || moonColorsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for MoonColors");
            }

            int posxxxxxxxxxxxxx = offset + 126 + moonColorsOffset;
            int moonColorsCount = VarInt.peek(buffer, posxxxxxxxxxxxxx);
            if (moonColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for MoonColors");
            }

            if (moonColorsCount > 4096000) {
               return ValidationResult.error("MoonColors exceeds max length 4096000");
            }

            posxxxxxxxxxxxxx += VarInt.size(moonColorsCount);

            for (int i = 0; i < moonColorsCount; i++) {
               posxxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxxxxxxx += 4;
            }
         }

         if ((nullBits[2] & 1) != 0) {
            int moonScalesOffset = buffer.getIntLE(offset + 86);
            if (moonScalesOffset < 0 || moonScalesOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for MoonScales");
            }

            int posxxxxxxxxxxxxxx = offset + 126 + moonScalesOffset;
            int moonScalesCount = VarInt.peek(buffer, posxxxxxxxxxxxxxx);
            if (moonScalesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for MoonScales");
            }

            if (moonScalesCount > 4096000) {
               return ValidationResult.error("MoonScales exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxx += VarInt.size(moonScalesCount);

            for (int i = 0; i < moonScalesCount; i++) {
               posxxxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         if ((nullBits[2] & 2) != 0) {
            int moonGlowColorsOffset = buffer.getIntLE(offset + 90);
            if (moonGlowColorsOffset < 0 || moonGlowColorsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for MoonGlowColors");
            }

            int posxxxxxxxxxxxxxxx = offset + 126 + moonGlowColorsOffset;
            int moonGlowColorsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxx);
            if (moonGlowColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for MoonGlowColors");
            }

            if (moonGlowColorsCount > 4096000) {
               return ValidationResult.error("MoonGlowColors exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxx += VarInt.size(moonGlowColorsCount);

            for (int i = 0; i < moonGlowColorsCount; i++) {
               posxxxxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxxxxxxxxx += 4;
            }
         }

         if ((nullBits[2] & 4) != 0) {
            int fogColorsOffset = buffer.getIntLE(offset + 94);
            if (fogColorsOffset < 0 || fogColorsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for FogColors");
            }

            int posxxxxxxxxxxxxxxxx = offset + 126 + fogColorsOffset;
            int fogColorsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxx);
            if (fogColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for FogColors");
            }

            if (fogColorsCount > 4096000) {
               return ValidationResult.error("FogColors exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxx += VarInt.size(fogColorsCount);

            for (int i = 0; i < fogColorsCount; i++) {
               posxxxxxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxxxxxxxxxx += 3;
            }
         }

         if ((nullBits[2] & 8) != 0) {
            int fogHeightFalloffsOffset = buffer.getIntLE(offset + 98);
            if (fogHeightFalloffsOffset < 0 || fogHeightFalloffsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for FogHeightFalloffs");
            }

            int posxxxxxxxxxxxxxxxxx = offset + 126 + fogHeightFalloffsOffset;
            int fogHeightFalloffsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxx);
            if (fogHeightFalloffsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for FogHeightFalloffs");
            }

            if (fogHeightFalloffsCount > 4096000) {
               return ValidationResult.error("FogHeightFalloffs exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxx += VarInt.size(fogHeightFalloffsCount);

            for (int i = 0; i < fogHeightFalloffsCount; i++) {
               posxxxxxxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         if ((nullBits[2] & 16) != 0) {
            int fogDensitiesOffset = buffer.getIntLE(offset + 102);
            if (fogDensitiesOffset < 0 || fogDensitiesOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for FogDensities");
            }

            int posxxxxxxxxxxxxxxxxxx = offset + 126 + fogDensitiesOffset;
            int fogDensitiesCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxx);
            if (fogDensitiesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for FogDensities");
            }

            if (fogDensitiesCount > 4096000) {
               return ValidationResult.error("FogDensities exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxx += VarInt.size(fogDensitiesCount);

            for (int i = 0; i < fogDensitiesCount; i++) {
               posxxxxxxxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         if ((nullBits[2] & 32) != 0) {
            int screenEffectOffset = buffer.getIntLE(offset + 106);
            if (screenEffectOffset < 0 || screenEffectOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for ScreenEffect");
            }

            int posxxxxxxxxxxxxxxxxxxx = offset + 126 + screenEffectOffset;
            int screenEffectLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxx);
            if (screenEffectLen < 0) {
               return ValidationResult.error("Invalid string length for ScreenEffect");
            }

            if (screenEffectLen > 4096000) {
               return ValidationResult.error("ScreenEffect exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxx += VarInt.size(screenEffectLen);
            posxxxxxxxxxxxxxxxxxxx += screenEffectLen;
            if (posxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ScreenEffect");
            }
         }

         if ((nullBits[2] & 64) != 0) {
            int screenEffectColorsOffset = buffer.getIntLE(offset + 110);
            if (screenEffectColorsOffset < 0 || screenEffectColorsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for ScreenEffectColors");
            }

            int posxxxxxxxxxxxxxxxxxxxx = offset + 126 + screenEffectColorsOffset;
            int screenEffectColorsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxx);
            if (screenEffectColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ScreenEffectColors");
            }

            if (screenEffectColorsCount > 4096000) {
               return ValidationResult.error("ScreenEffectColors exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxxx += VarInt.size(screenEffectColorsCount);

            for (int i = 0; i < screenEffectColorsCount; i++) {
               posxxxxxxxxxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxxxxxxxxxxxxxx += 4;
            }
         }

         if ((nullBits[2] & 128) != 0) {
            int colorFiltersOffset = buffer.getIntLE(offset + 114);
            if (colorFiltersOffset < 0 || colorFiltersOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for ColorFilters");
            }

            int posxxxxxxxxxxxxxxxxxxxxx = offset + 126 + colorFiltersOffset;
            int colorFiltersCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxxx);
            if (colorFiltersCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ColorFilters");
            }

            if (colorFiltersCount > 4096000) {
               return ValidationResult.error("ColorFilters exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxxxx += VarInt.size(colorFiltersCount);

            for (int i = 0; i < colorFiltersCount; i++) {
               posxxxxxxxxxxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxxxxxxxxxxxxxxx += 3;
            }
         }

         if ((nullBits[3] & 1) != 0) {
            int waterTintsOffset = buffer.getIntLE(offset + 118);
            if (waterTintsOffset < 0 || waterTintsOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for WaterTints");
            }

            int posxxxxxxxxxxxxxxxxxxxxxx = offset + 126 + waterTintsOffset;
            int waterTintsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxxxx);
            if (waterTintsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for WaterTints");
            }

            if (waterTintsCount > 4096000) {
               return ValidationResult.error("WaterTints exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxxxxx += VarInt.size(waterTintsCount);

            for (int i = 0; i < waterTintsCount; i++) {
               posxxxxxxxxxxxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxxxxxxxxxxxxxxxx += 3;
            }
         }

         if ((nullBits[3] & 2) != 0) {
            int particleOffset = buffer.getIntLE(offset + 122);
            if (particleOffset < 0 || particleOffset > buffer.writerIndex() - offset - 126) {
               return ValidationResult.error("Invalid offset for Particle");
            }

            int posxxxxxxxxxxxxxxxxxxxxxxx = offset + 126 + particleOffset;
            ValidationResult particleResult = WeatherParticle.validateStructure(buffer, posxxxxxxxxxxxxxxxxxxxxxxx);
            if (!particleResult.isValid()) {
               return ValidationResult.error("Invalid Particle: " + particleResult.error());
            }

            posxxxxxxxxxxxxxxxxxxxxxxx += WeatherParticle.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxxxxxxxxx);
         }

         return ValidationResult.OK;
      }
   }

   public Weather clone() {
      Weather copy = new Weather();
      copy.id = this.id;
      copy.tagIndexes = this.tagIndexes != null ? Arrays.copyOf(this.tagIndexes, this.tagIndexes.length) : null;
      copy.stars = this.stars;
      copy.moons = this.moons != null ? new HashMap<>(this.moons) : null;
      copy.clouds = this.clouds != null ? Arrays.stream(this.clouds).map(ex -> ex.clone()).toArray(Cloud[]::new) : null;
      copy.sunlightDampingMultiplier = this.sunlightDampingMultiplier != null ? new HashMap<>(this.sunlightDampingMultiplier) : null;
      if (this.sunlightColors != null) {
         Map<Float, Color> m = new HashMap<>();

         for (Entry<Float, Color> e : this.sunlightColors.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.sunlightColors = m;
      }

      if (this.skyTopColors != null) {
         Map<Float, ColorAlpha> m = new HashMap<>();

         for (Entry<Float, ColorAlpha> e : this.skyTopColors.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.skyTopColors = m;
      }

      if (this.skyBottomColors != null) {
         Map<Float, ColorAlpha> m = new HashMap<>();

         for (Entry<Float, ColorAlpha> e : this.skyBottomColors.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.skyBottomColors = m;
      }

      if (this.skySunsetColors != null) {
         Map<Float, ColorAlpha> m = new HashMap<>();

         for (Entry<Float, ColorAlpha> e : this.skySunsetColors.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.skySunsetColors = m;
      }

      if (this.sunColors != null) {
         Map<Float, Color> m = new HashMap<>();

         for (Entry<Float, Color> e : this.sunColors.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.sunColors = m;
      }

      copy.sunScales = this.sunScales != null ? new HashMap<>(this.sunScales) : null;
      if (this.sunGlowColors != null) {
         Map<Float, ColorAlpha> m = new HashMap<>();

         for (Entry<Float, ColorAlpha> e : this.sunGlowColors.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.sunGlowColors = m;
      }

      if (this.moonColors != null) {
         Map<Float, ColorAlpha> m = new HashMap<>();

         for (Entry<Float, ColorAlpha> e : this.moonColors.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.moonColors = m;
      }

      copy.moonScales = this.moonScales != null ? new HashMap<>(this.moonScales) : null;
      if (this.moonGlowColors != null) {
         Map<Float, ColorAlpha> m = new HashMap<>();

         for (Entry<Float, ColorAlpha> e : this.moonGlowColors.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.moonGlowColors = m;
      }

      if (this.fogColors != null) {
         Map<Float, Color> m = new HashMap<>();

         for (Entry<Float, Color> e : this.fogColors.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.fogColors = m;
      }

      copy.fogHeightFalloffs = this.fogHeightFalloffs != null ? new HashMap<>(this.fogHeightFalloffs) : null;
      copy.fogDensities = this.fogDensities != null ? new HashMap<>(this.fogDensities) : null;
      copy.screenEffect = this.screenEffect;
      if (this.screenEffectColors != null) {
         Map<Float, ColorAlpha> m = new HashMap<>();

         for (Entry<Float, ColorAlpha> e : this.screenEffectColors.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.screenEffectColors = m;
      }

      if (this.colorFilters != null) {
         Map<Float, Color> m = new HashMap<>();

         for (Entry<Float, Color> e : this.colorFilters.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.colorFilters = m;
      }

      if (this.waterTints != null) {
         Map<Float, Color> m = new HashMap<>();

         for (Entry<Float, Color> e : this.waterTints.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.waterTints = m;
      }

      copy.particle = this.particle != null ? this.particle.clone() : null;
      copy.fog = this.fog != null ? this.fog.clone() : null;
      copy.fogOptions = this.fogOptions != null ? this.fogOptions.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Weather other)
            ? false
            : Objects.equals(this.id, other.id)
               && Arrays.equals(this.tagIndexes, other.tagIndexes)
               && Objects.equals(this.stars, other.stars)
               && Objects.equals(this.moons, other.moons)
               && Arrays.equals((Object[])this.clouds, (Object[])other.clouds)
               && Objects.equals(this.sunlightDampingMultiplier, other.sunlightDampingMultiplier)
               && Objects.equals(this.sunlightColors, other.sunlightColors)
               && Objects.equals(this.skyTopColors, other.skyTopColors)
               && Objects.equals(this.skyBottomColors, other.skyBottomColors)
               && Objects.equals(this.skySunsetColors, other.skySunsetColors)
               && Objects.equals(this.sunColors, other.sunColors)
               && Objects.equals(this.sunScales, other.sunScales)
               && Objects.equals(this.sunGlowColors, other.sunGlowColors)
               && Objects.equals(this.moonColors, other.moonColors)
               && Objects.equals(this.moonScales, other.moonScales)
               && Objects.equals(this.moonGlowColors, other.moonGlowColors)
               && Objects.equals(this.fogColors, other.fogColors)
               && Objects.equals(this.fogHeightFalloffs, other.fogHeightFalloffs)
               && Objects.equals(this.fogDensities, other.fogDensities)
               && Objects.equals(this.screenEffect, other.screenEffect)
               && Objects.equals(this.screenEffectColors, other.screenEffectColors)
               && Objects.equals(this.colorFilters, other.colorFilters)
               && Objects.equals(this.waterTints, other.waterTints)
               && Objects.equals(this.particle, other.particle)
               && Objects.equals(this.fog, other.fog)
               && Objects.equals(this.fogOptions, other.fogOptions);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Arrays.hashCode(this.tagIndexes);
      result = 31 * result + Objects.hashCode(this.stars);
      result = 31 * result + Objects.hashCode(this.moons);
      result = 31 * result + Arrays.hashCode((Object[])this.clouds);
      result = 31 * result + Objects.hashCode(this.sunlightDampingMultiplier);
      result = 31 * result + Objects.hashCode(this.sunlightColors);
      result = 31 * result + Objects.hashCode(this.skyTopColors);
      result = 31 * result + Objects.hashCode(this.skyBottomColors);
      result = 31 * result + Objects.hashCode(this.skySunsetColors);
      result = 31 * result + Objects.hashCode(this.sunColors);
      result = 31 * result + Objects.hashCode(this.sunScales);
      result = 31 * result + Objects.hashCode(this.sunGlowColors);
      result = 31 * result + Objects.hashCode(this.moonColors);
      result = 31 * result + Objects.hashCode(this.moonScales);
      result = 31 * result + Objects.hashCode(this.moonGlowColors);
      result = 31 * result + Objects.hashCode(this.fogColors);
      result = 31 * result + Objects.hashCode(this.fogHeightFalloffs);
      result = 31 * result + Objects.hashCode(this.fogDensities);
      result = 31 * result + Objects.hashCode(this.screenEffect);
      result = 31 * result + Objects.hashCode(this.screenEffectColors);
      result = 31 * result + Objects.hashCode(this.colorFilters);
      result = 31 * result + Objects.hashCode(this.waterTints);
      result = 31 * result + Objects.hashCode(this.particle);
      result = 31 * result + Objects.hashCode(this.fog);
      return 31 * result + Objects.hashCode(this.fogOptions);
   }
}
