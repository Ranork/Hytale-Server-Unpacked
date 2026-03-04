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
      Weather obj = new Weather();
      byte[] nullBits = PacketIO.readBytes(buf, offset, 4);
      if ((nullBits[0] & 1) != 0) {
         obj.fog = NearFar.deserialize(buf, offset + 4);
      }

      if ((nullBits[0] & 2) != 0) {
         obj.fogOptions = FogOptions.deserialize(buf, offset + 12);
      }

      if ((nullBits[0] & 4) != 0) {
         int varPos0 = offset + 126 + buf.getIntLE(offset + 30);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits[0] & 8) != 0) {
         int varPos1 = offset + 126 + buf.getIntLE(offset + 34);
         int tagIndexesCount = VarInt.peek(buf, varPos1);
         if (tagIndexesCount < 0) {
            throw ProtocolException.negativeLength("TagIndexes", tagIndexesCount);
         }

         if (tagIndexesCount > 4096000) {
            throw ProtocolException.arrayTooLong("TagIndexes", tagIndexesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + tagIndexesCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("TagIndexes", varPos1 + varIntLen + tagIndexesCount * 4, buf.readableBytes());
         }

         obj.tagIndexes = new int[tagIndexesCount];

         for (int i = 0; i < tagIndexesCount; i++) {
            obj.tagIndexes[i] = buf.getIntLE(varPos1 + varIntLen + i * 4);
         }
      }

      if ((nullBits[0] & 16) != 0) {
         int varPos2 = offset + 126 + buf.getIntLE(offset + 38);
         int starsLen = VarInt.peek(buf, varPos2);
         if (starsLen < 0) {
            throw ProtocolException.negativeLength("Stars", starsLen);
         }

         if (starsLen > 4096000) {
            throw ProtocolException.stringTooLong("Stars", starsLen, 4096000);
         }

         obj.stars = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      if ((nullBits[0] & 32) != 0) {
         int varPos3 = offset + 126 + buf.getIntLE(offset + 42);
         int moonsCount = VarInt.peek(buf, varPos3);
         if (moonsCount < 0) {
            throw ProtocolException.negativeLength("Moons", moonsCount);
         }

         if (moonsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Moons", moonsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos3);
         obj.moons = new HashMap<>(moonsCount);
         int dictPos = varPos3 + varIntLen;

         for (int i = 0; i < moonsCount; i++) {
            int key = buf.getIntLE(dictPos);
            dictPos += 4;
            int valLen = VarInt.peek(buf, dictPos);
            if (valLen < 0) {
               throw ProtocolException.negativeLength("val", valLen);
            }

            if (valLen > 4096000) {
               throw ProtocolException.stringTooLong("val", valLen, 4096000);
            }

            int valVarLen = VarInt.length(buf, dictPos);
            String val = PacketIO.readVarString(buf, dictPos);
            dictPos += valVarLen + valLen;
            if (obj.moons.put(key, val) != null) {
               throw ProtocolException.duplicateKey("moons", key);
            }
         }
      }

      if ((nullBits[0] & 64) != 0) {
         int varPos4 = offset + 126 + buf.getIntLE(offset + 46);
         int cloudsCount = VarInt.peek(buf, varPos4);
         if (cloudsCount < 0) {
            throw ProtocolException.negativeLength("Clouds", cloudsCount);
         }

         if (cloudsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Clouds", cloudsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos4);
         if (varPos4 + varIntLen + cloudsCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Clouds", varPos4 + varIntLen + cloudsCount * 1, buf.readableBytes());
         }

         obj.clouds = new Cloud[cloudsCount];
         int elemPos = varPos4 + varIntLen;

         for (int i = 0; i < cloudsCount; i++) {
            obj.clouds[i] = Cloud.deserialize(buf, elemPos);
            elemPos += Cloud.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int varPos5 = offset + 126 + buf.getIntLE(offset + 50);
         int sunlightDampingMultiplierCount = VarInt.peek(buf, varPos5);
         if (sunlightDampingMultiplierCount < 0) {
            throw ProtocolException.negativeLength("SunlightDampingMultiplier", sunlightDampingMultiplierCount);
         }

         if (sunlightDampingMultiplierCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunlightDampingMultiplier", sunlightDampingMultiplierCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos5);
         obj.sunlightDampingMultiplier = new HashMap<>(sunlightDampingMultiplierCount);
         int dictPos = varPos5 + varIntLen;

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
         int varPos6 = offset + 126 + buf.getIntLE(offset + 54);
         int sunlightColorsCount = VarInt.peek(buf, varPos6);
         if (sunlightColorsCount < 0) {
            throw ProtocolException.negativeLength("SunlightColors", sunlightColorsCount);
         }

         if (sunlightColorsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunlightColors", sunlightColorsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos6);
         obj.sunlightColors = new HashMap<>(sunlightColorsCount);
         int dictPos = varPos6 + varIntLen;

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
         int varPos7 = offset + 126 + buf.getIntLE(offset + 58);
         int skyTopColorsCount = VarInt.peek(buf, varPos7);
         if (skyTopColorsCount < 0) {
            throw ProtocolException.negativeLength("SkyTopColors", skyTopColorsCount);
         }

         if (skyTopColorsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SkyTopColors", skyTopColorsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos7);
         obj.skyTopColors = new HashMap<>(skyTopColorsCount);
         int dictPos = varPos7 + varIntLen;

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
         int varPos8 = offset + 126 + buf.getIntLE(offset + 62);
         int skyBottomColorsCount = VarInt.peek(buf, varPos8);
         if (skyBottomColorsCount < 0) {
            throw ProtocolException.negativeLength("SkyBottomColors", skyBottomColorsCount);
         }

         if (skyBottomColorsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SkyBottomColors", skyBottomColorsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos8);
         obj.skyBottomColors = new HashMap<>(skyBottomColorsCount);
         int dictPos = varPos8 + varIntLen;

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
         int varPos9 = offset + 126 + buf.getIntLE(offset + 66);
         int skySunsetColorsCount = VarInt.peek(buf, varPos9);
         if (skySunsetColorsCount < 0) {
            throw ProtocolException.negativeLength("SkySunsetColors", skySunsetColorsCount);
         }

         if (skySunsetColorsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SkySunsetColors", skySunsetColorsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos9);
         obj.skySunsetColors = new HashMap<>(skySunsetColorsCount);
         int dictPos = varPos9 + varIntLen;

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
         int varPos10 = offset + 126 + buf.getIntLE(offset + 70);
         int sunColorsCount = VarInt.peek(buf, varPos10);
         if (sunColorsCount < 0) {
            throw ProtocolException.negativeLength("SunColors", sunColorsCount);
         }

         if (sunColorsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunColors", sunColorsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos10);
         obj.sunColors = new HashMap<>(sunColorsCount);
         int dictPos = varPos10 + varIntLen;

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
         int varPos11 = offset + 126 + buf.getIntLE(offset + 74);
         int sunScalesCount = VarInt.peek(buf, varPos11);
         if (sunScalesCount < 0) {
            throw ProtocolException.negativeLength("SunScales", sunScalesCount);
         }

         if (sunScalesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunScales", sunScalesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos11);
         obj.sunScales = new HashMap<>(sunScalesCount);
         int dictPos = varPos11 + varIntLen;

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
         int varPos12 = offset + 126 + buf.getIntLE(offset + 78);
         int sunGlowColorsCount = VarInt.peek(buf, varPos12);
         if (sunGlowColorsCount < 0) {
            throw ProtocolException.negativeLength("SunGlowColors", sunGlowColorsCount);
         }

         if (sunGlowColorsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SunGlowColors", sunGlowColorsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos12);
         obj.sunGlowColors = new HashMap<>(sunGlowColorsCount);
         int dictPos = varPos12 + varIntLen;

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
         int varPos13 = offset + 126 + buf.getIntLE(offset + 82);
         int moonColorsCount = VarInt.peek(buf, varPos13);
         if (moonColorsCount < 0) {
            throw ProtocolException.negativeLength("MoonColors", moonColorsCount);
         }

         if (moonColorsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MoonColors", moonColorsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos13);
         obj.moonColors = new HashMap<>(moonColorsCount);
         int dictPos = varPos13 + varIntLen;

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
         int varPos14 = offset + 126 + buf.getIntLE(offset + 86);
         int moonScalesCount = VarInt.peek(buf, varPos14);
         if (moonScalesCount < 0) {
            throw ProtocolException.negativeLength("MoonScales", moonScalesCount);
         }

         if (moonScalesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MoonScales", moonScalesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos14);
         obj.moonScales = new HashMap<>(moonScalesCount);
         int dictPos = varPos14 + varIntLen;

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
         int varPos15 = offset + 126 + buf.getIntLE(offset + 90);
         int moonGlowColorsCount = VarInt.peek(buf, varPos15);
         if (moonGlowColorsCount < 0) {
            throw ProtocolException.negativeLength("MoonGlowColors", moonGlowColorsCount);
         }

         if (moonGlowColorsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MoonGlowColors", moonGlowColorsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos15);
         obj.moonGlowColors = new HashMap<>(moonGlowColorsCount);
         int dictPos = varPos15 + varIntLen;

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
         int varPos16 = offset + 126 + buf.getIntLE(offset + 94);
         int fogColorsCount = VarInt.peek(buf, varPos16);
         if (fogColorsCount < 0) {
            throw ProtocolException.negativeLength("FogColors", fogColorsCount);
         }

         if (fogColorsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FogColors", fogColorsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos16);
         obj.fogColors = new HashMap<>(fogColorsCount);
         int dictPos = varPos16 + varIntLen;

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
         int varPos17 = offset + 126 + buf.getIntLE(offset + 98);
         int fogHeightFalloffsCount = VarInt.peek(buf, varPos17);
         if (fogHeightFalloffsCount < 0) {
            throw ProtocolException.negativeLength("FogHeightFalloffs", fogHeightFalloffsCount);
         }

         if (fogHeightFalloffsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FogHeightFalloffs", fogHeightFalloffsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos17);
         obj.fogHeightFalloffs = new HashMap<>(fogHeightFalloffsCount);
         int dictPos = varPos17 + varIntLen;

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
         int varPos18 = offset + 126 + buf.getIntLE(offset + 102);
         int fogDensitiesCount = VarInt.peek(buf, varPos18);
         if (fogDensitiesCount < 0) {
            throw ProtocolException.negativeLength("FogDensities", fogDensitiesCount);
         }

         if (fogDensitiesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FogDensities", fogDensitiesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos18);
         obj.fogDensities = new HashMap<>(fogDensitiesCount);
         int dictPos = varPos18 + varIntLen;

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
         int varPos19 = offset + 126 + buf.getIntLE(offset + 106);
         int screenEffectLen = VarInt.peek(buf, varPos19);
         if (screenEffectLen < 0) {
            throw ProtocolException.negativeLength("ScreenEffect", screenEffectLen);
         }

         if (screenEffectLen > 4096000) {
            throw ProtocolException.stringTooLong("ScreenEffect", screenEffectLen, 4096000);
         }

         obj.screenEffect = PacketIO.readVarString(buf, varPos19, PacketIO.UTF8);
      }

      if ((nullBits[2] & 64) != 0) {
         int varPos20 = offset + 126 + buf.getIntLE(offset + 110);
         int screenEffectColorsCount = VarInt.peek(buf, varPos20);
         if (screenEffectColorsCount < 0) {
            throw ProtocolException.negativeLength("ScreenEffectColors", screenEffectColorsCount);
         }

         if (screenEffectColorsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ScreenEffectColors", screenEffectColorsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos20);
         obj.screenEffectColors = new HashMap<>(screenEffectColorsCount);
         int dictPos = varPos20 + varIntLen;

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
         int varPos21 = offset + 126 + buf.getIntLE(offset + 114);
         int colorFiltersCount = VarInt.peek(buf, varPos21);
         if (colorFiltersCount < 0) {
            throw ProtocolException.negativeLength("ColorFilters", colorFiltersCount);
         }

         if (colorFiltersCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ColorFilters", colorFiltersCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos21);
         obj.colorFilters = new HashMap<>(colorFiltersCount);
         int dictPos = varPos21 + varIntLen;

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
         int varPos22 = offset + 126 + buf.getIntLE(offset + 118);
         int waterTintsCount = VarInt.peek(buf, varPos22);
         if (waterTintsCount < 0) {
            throw ProtocolException.negativeLength("WaterTints", waterTintsCount);
         }

         if (waterTintsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("WaterTints", waterTintsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos22);
         obj.waterTints = new HashMap<>(waterTintsCount);
         int dictPos = varPos22 + varIntLen;

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
         int varPos23 = offset + 126 + buf.getIntLE(offset + 122);
         obj.particle = WeatherParticle.deserialize(buf, varPos23);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 4);
      int maxEnd = 126;
      if ((nullBits[0] & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 30);
         int pos0 = offset + 126 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[0] & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 34);
         int pos1 = offset + 126 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + arrLen * 4;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[0] & 16) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 38);
         int pos2 = offset + 126 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[0] & 32) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 42);
         int pos3 = offset + 126 + fieldOffset3;
         int dictLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3);

         for (int i = 0; i < dictLen; i++) {
            pos3 += 4;
            int sl = VarInt.peek(buf, pos3);
            pos3 += VarInt.length(buf, pos3) + sl;
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[0] & 64) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 46);
         int pos4 = offset + 126 + fieldOffset4;
         int arrLen = VarInt.peek(buf, pos4);
         pos4 += VarInt.length(buf, pos4);

         for (int i = 0; i < arrLen; i++) {
            pos4 += Cloud.computeBytesConsumed(buf, pos4);
         }

         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 50);
         int pos5 = offset + 126 + fieldOffset5;
         int dictLen = VarInt.peek(buf, pos5);
         pos5 += VarInt.length(buf, pos5);

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
         int pos6 = offset + 126 + fieldOffset6;
         int dictLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.length(buf, pos6);

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
         int pos7 = offset + 126 + fieldOffset7;
         int dictLen = VarInt.peek(buf, pos7);
         pos7 += VarInt.length(buf, pos7);

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
         int pos8 = offset + 126 + fieldOffset8;
         int dictLen = VarInt.peek(buf, pos8);
         pos8 += VarInt.length(buf, pos8);

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
         int pos9 = offset + 126 + fieldOffset9;
         int dictLen = VarInt.peek(buf, pos9);
         pos9 += VarInt.length(buf, pos9);

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
         int pos10 = offset + 126 + fieldOffset10;
         int dictLen = VarInt.peek(buf, pos10);
         pos10 += VarInt.length(buf, pos10);

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
         int pos11 = offset + 126 + fieldOffset11;
         int dictLen = VarInt.peek(buf, pos11);
         pos11 += VarInt.length(buf, pos11);

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
         int pos12 = offset + 126 + fieldOffset12;
         int dictLen = VarInt.peek(buf, pos12);
         pos12 += VarInt.length(buf, pos12);

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
         int pos13 = offset + 126 + fieldOffset13;
         int dictLen = VarInt.peek(buf, pos13);
         pos13 += VarInt.length(buf, pos13);

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
         int pos14 = offset + 126 + fieldOffset14;
         int dictLen = VarInt.peek(buf, pos14);
         pos14 += VarInt.length(buf, pos14);

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
         int pos15 = offset + 126 + fieldOffset15;
         int dictLen = VarInt.peek(buf, pos15);
         pos15 += VarInt.length(buf, pos15);

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
         int pos16 = offset + 126 + fieldOffset16;
         int dictLen = VarInt.peek(buf, pos16);
         pos16 += VarInt.length(buf, pos16);

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
         int pos17 = offset + 126 + fieldOffset17;
         int dictLen = VarInt.peek(buf, pos17);
         pos17 += VarInt.length(buf, pos17);

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
         int pos18 = offset + 126 + fieldOffset18;
         int dictLen = VarInt.peek(buf, pos18);
         pos18 += VarInt.length(buf, pos18);

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
         int pos19 = offset + 126 + fieldOffset19;
         int sl = VarInt.peek(buf, pos19);
         pos19 += VarInt.length(buf, pos19) + sl;
         if (pos19 - offset > maxEnd) {
            maxEnd = pos19 - offset;
         }
      }

      if ((nullBits[2] & 64) != 0) {
         int fieldOffset20 = buf.getIntLE(offset + 110);
         int pos20 = offset + 126 + fieldOffset20;
         int dictLen = VarInt.peek(buf, pos20);
         pos20 += VarInt.length(buf, pos20);

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
         int pos21 = offset + 126 + fieldOffset21;
         int dictLen = VarInt.peek(buf, pos21);
         pos21 += VarInt.length(buf, pos21);

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
         int pos22 = offset + 126 + fieldOffset22;
         int dictLen = VarInt.peek(buf, pos22);
         pos22 += VarInt.length(buf, pos22);

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
         int pos23 = offset + 126 + fieldOffset23;
         pos23 += WeatherParticle.computeBytesConsumed(buf, pos23);
         if (pos23 - offset > maxEnd) {
            maxEnd = pos23 - offset;
         }
      }

      return maxEnd;
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
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 126 + idOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Id");
            }

            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits[0] & 8) != 0) {
            int tagIndexesOffset = buffer.getIntLE(offset + 34);
            if (tagIndexesOffset < 0) {
               return ValidationResult.error("Invalid offset for TagIndexes");
            }

            int posx = offset + 126 + tagIndexesOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for TagIndexes");
            }

            int tagIndexesCount = VarInt.peek(buffer, posx);
            if (tagIndexesCount < 0) {
               return ValidationResult.error("Invalid array count for TagIndexes");
            }

            if (tagIndexesCount > 4096000) {
               return ValidationResult.error("TagIndexes exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += tagIndexesCount * 4;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading TagIndexes");
            }
         }

         if ((nullBits[0] & 16) != 0) {
            int starsOffset = buffer.getIntLE(offset + 38);
            if (starsOffset < 0) {
               return ValidationResult.error("Invalid offset for Stars");
            }

            int posxx = offset + 126 + starsOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Stars");
            }

            int starsLen = VarInt.peek(buffer, posxx);
            if (starsLen < 0) {
               return ValidationResult.error("Invalid string length for Stars");
            }

            if (starsLen > 4096000) {
               return ValidationResult.error("Stars exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += starsLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Stars");
            }
         }

         if ((nullBits[0] & 32) != 0) {
            int moonsOffset = buffer.getIntLE(offset + 42);
            if (moonsOffset < 0) {
               return ValidationResult.error("Invalid offset for Moons");
            }

            int posxxx = offset + 126 + moonsOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Moons");
            }

            int moonsCount = VarInt.peek(buffer, posxxx);
            if (moonsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Moons");
            }

            if (moonsCount > 4096000) {
               return ValidationResult.error("Moons exceeds max length 4096000");
            }

            posxxx += VarInt.length(buffer, posxxx);

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

               posxxx += VarInt.length(buffer, posxxx);
               posxxx += valueLen;
               if (posxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         if ((nullBits[0] & 64) != 0) {
            int cloudsOffset = buffer.getIntLE(offset + 46);
            if (cloudsOffset < 0) {
               return ValidationResult.error("Invalid offset for Clouds");
            }

            int posxxxx = offset + 126 + cloudsOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Clouds");
            }

            int cloudsCount = VarInt.peek(buffer, posxxxx);
            if (cloudsCount < 0) {
               return ValidationResult.error("Invalid array count for Clouds");
            }

            if (cloudsCount > 4096000) {
               return ValidationResult.error("Clouds exceeds max length 4096000");
            }

            posxxxx += VarInt.length(buffer, posxxxx);

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
            if (sunlightDampingMultiplierOffset < 0) {
               return ValidationResult.error("Invalid offset for SunlightDampingMultiplier");
            }

            int posxxxxx = offset + 126 + sunlightDampingMultiplierOffset;
            if (posxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SunlightDampingMultiplier");
            }

            int sunlightDampingMultiplierCount = VarInt.peek(buffer, posxxxxx);
            if (sunlightDampingMultiplierCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SunlightDampingMultiplier");
            }

            if (sunlightDampingMultiplierCount > 4096000) {
               return ValidationResult.error("SunlightDampingMultiplier exceeds max length 4096000");
            }

            posxxxxx += VarInt.length(buffer, posxxxxx);

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
            if (sunlightColorsOffset < 0) {
               return ValidationResult.error("Invalid offset for SunlightColors");
            }

            int posxxxxxx = offset + 126 + sunlightColorsOffset;
            if (posxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SunlightColors");
            }

            int sunlightColorsCount = VarInt.peek(buffer, posxxxxxx);
            if (sunlightColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SunlightColors");
            }

            if (sunlightColorsCount > 4096000) {
               return ValidationResult.error("SunlightColors exceeds max length 4096000");
            }

            posxxxxxx += VarInt.length(buffer, posxxxxxx);

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
            if (skyTopColorsOffset < 0) {
               return ValidationResult.error("Invalid offset for SkyTopColors");
            }

            int posxxxxxxx = offset + 126 + skyTopColorsOffset;
            if (posxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SkyTopColors");
            }

            int skyTopColorsCount = VarInt.peek(buffer, posxxxxxxx);
            if (skyTopColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SkyTopColors");
            }

            if (skyTopColorsCount > 4096000) {
               return ValidationResult.error("SkyTopColors exceeds max length 4096000");
            }

            posxxxxxxx += VarInt.length(buffer, posxxxxxxx);

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
            if (skyBottomColorsOffset < 0) {
               return ValidationResult.error("Invalid offset for SkyBottomColors");
            }

            int posxxxxxxxx = offset + 126 + skyBottomColorsOffset;
            if (posxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SkyBottomColors");
            }

            int skyBottomColorsCount = VarInt.peek(buffer, posxxxxxxxx);
            if (skyBottomColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SkyBottomColors");
            }

            if (skyBottomColorsCount > 4096000) {
               return ValidationResult.error("SkyBottomColors exceeds max length 4096000");
            }

            posxxxxxxxx += VarInt.length(buffer, posxxxxxxxx);

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
            if (skySunsetColorsOffset < 0) {
               return ValidationResult.error("Invalid offset for SkySunsetColors");
            }

            int posxxxxxxxxx = offset + 126 + skySunsetColorsOffset;
            if (posxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SkySunsetColors");
            }

            int skySunsetColorsCount = VarInt.peek(buffer, posxxxxxxxxx);
            if (skySunsetColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SkySunsetColors");
            }

            if (skySunsetColorsCount > 4096000) {
               return ValidationResult.error("SkySunsetColors exceeds max length 4096000");
            }

            posxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxx);

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
            if (sunColorsOffset < 0) {
               return ValidationResult.error("Invalid offset for SunColors");
            }

            int posxxxxxxxxxx = offset + 126 + sunColorsOffset;
            if (posxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SunColors");
            }

            int sunColorsCount = VarInt.peek(buffer, posxxxxxxxxxx);
            if (sunColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SunColors");
            }

            if (sunColorsCount > 4096000) {
               return ValidationResult.error("SunColors exceeds max length 4096000");
            }

            posxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxx);

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
            if (sunScalesOffset < 0) {
               return ValidationResult.error("Invalid offset for SunScales");
            }

            int posxxxxxxxxxxx = offset + 126 + sunScalesOffset;
            if (posxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SunScales");
            }

            int sunScalesCount = VarInt.peek(buffer, posxxxxxxxxxxx);
            if (sunScalesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SunScales");
            }

            if (sunScalesCount > 4096000) {
               return ValidationResult.error("SunScales exceeds max length 4096000");
            }

            posxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxx);

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
            if (sunGlowColorsOffset < 0) {
               return ValidationResult.error("Invalid offset for SunGlowColors");
            }

            int posxxxxxxxxxxxx = offset + 126 + sunGlowColorsOffset;
            if (posxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SunGlowColors");
            }

            int sunGlowColorsCount = VarInt.peek(buffer, posxxxxxxxxxxxx);
            if (sunGlowColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SunGlowColors");
            }

            if (sunGlowColorsCount > 4096000) {
               return ValidationResult.error("SunGlowColors exceeds max length 4096000");
            }

            posxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxx);

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
            if (moonColorsOffset < 0) {
               return ValidationResult.error("Invalid offset for MoonColors");
            }

            int posxxxxxxxxxxxxx = offset + 126 + moonColorsOffset;
            if (posxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for MoonColors");
            }

            int moonColorsCount = VarInt.peek(buffer, posxxxxxxxxxxxxx);
            if (moonColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for MoonColors");
            }

            if (moonColorsCount > 4096000) {
               return ValidationResult.error("MoonColors exceeds max length 4096000");
            }

            posxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxx);

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
            if (moonScalesOffset < 0) {
               return ValidationResult.error("Invalid offset for MoonScales");
            }

            int posxxxxxxxxxxxxxx = offset + 126 + moonScalesOffset;
            if (posxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for MoonScales");
            }

            int moonScalesCount = VarInt.peek(buffer, posxxxxxxxxxxxxxx);
            if (moonScalesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for MoonScales");
            }

            if (moonScalesCount > 4096000) {
               return ValidationResult.error("MoonScales exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxx);

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
            if (moonGlowColorsOffset < 0) {
               return ValidationResult.error("Invalid offset for MoonGlowColors");
            }

            int posxxxxxxxxxxxxxxx = offset + 126 + moonGlowColorsOffset;
            if (posxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for MoonGlowColors");
            }

            int moonGlowColorsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxx);
            if (moonGlowColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for MoonGlowColors");
            }

            if (moonGlowColorsCount > 4096000) {
               return ValidationResult.error("MoonGlowColors exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxx);

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
            if (fogColorsOffset < 0) {
               return ValidationResult.error("Invalid offset for FogColors");
            }

            int posxxxxxxxxxxxxxxxx = offset + 126 + fogColorsOffset;
            if (posxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FogColors");
            }

            int fogColorsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxx);
            if (fogColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for FogColors");
            }

            if (fogColorsCount > 4096000) {
               return ValidationResult.error("FogColors exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxx);

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
            if (fogHeightFalloffsOffset < 0) {
               return ValidationResult.error("Invalid offset for FogHeightFalloffs");
            }

            int posxxxxxxxxxxxxxxxxx = offset + 126 + fogHeightFalloffsOffset;
            if (posxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FogHeightFalloffs");
            }

            int fogHeightFalloffsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxx);
            if (fogHeightFalloffsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for FogHeightFalloffs");
            }

            if (fogHeightFalloffsCount > 4096000) {
               return ValidationResult.error("FogHeightFalloffs exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxx);

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
            if (fogDensitiesOffset < 0) {
               return ValidationResult.error("Invalid offset for FogDensities");
            }

            int posxxxxxxxxxxxxxxxxxx = offset + 126 + fogDensitiesOffset;
            if (posxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FogDensities");
            }

            int fogDensitiesCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxx);
            if (fogDensitiesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for FogDensities");
            }

            if (fogDensitiesCount > 4096000) {
               return ValidationResult.error("FogDensities exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxx);

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
            if (screenEffectOffset < 0) {
               return ValidationResult.error("Invalid offset for ScreenEffect");
            }

            int posxxxxxxxxxxxxxxxxxxx = offset + 126 + screenEffectOffset;
            if (posxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ScreenEffect");
            }

            int screenEffectLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxx);
            if (screenEffectLen < 0) {
               return ValidationResult.error("Invalid string length for ScreenEffect");
            }

            if (screenEffectLen > 4096000) {
               return ValidationResult.error("ScreenEffect exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxx);
            posxxxxxxxxxxxxxxxxxxx += screenEffectLen;
            if (posxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ScreenEffect");
            }
         }

         if ((nullBits[2] & 64) != 0) {
            int screenEffectColorsOffset = buffer.getIntLE(offset + 110);
            if (screenEffectColorsOffset < 0) {
               return ValidationResult.error("Invalid offset for ScreenEffectColors");
            }

            int posxxxxxxxxxxxxxxxxxxxx = offset + 126 + screenEffectColorsOffset;
            if (posxxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ScreenEffectColors");
            }

            int screenEffectColorsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxx);
            if (screenEffectColorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ScreenEffectColors");
            }

            if (screenEffectColorsCount > 4096000) {
               return ValidationResult.error("ScreenEffectColors exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxxx);

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
            if (colorFiltersOffset < 0) {
               return ValidationResult.error("Invalid offset for ColorFilters");
            }

            int posxxxxxxxxxxxxxxxxxxxxx = offset + 126 + colorFiltersOffset;
            if (posxxxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ColorFilters");
            }

            int colorFiltersCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxxx);
            if (colorFiltersCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ColorFilters");
            }

            if (colorFiltersCount > 4096000) {
               return ValidationResult.error("ColorFilters exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxxxx);

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
            if (waterTintsOffset < 0) {
               return ValidationResult.error("Invalid offset for WaterTints");
            }

            int posxxxxxxxxxxxxxxxxxxxxxx = offset + 126 + waterTintsOffset;
            if (posxxxxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for WaterTints");
            }

            int waterTintsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxxxx);
            if (waterTintsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for WaterTints");
            }

            if (waterTintsCount > 4096000) {
               return ValidationResult.error("WaterTints exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxxxxx);

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
            if (particleOffset < 0) {
               return ValidationResult.error("Invalid offset for Particle");
            }

            int posxxxxxxxxxxxxxxxxxxxxxxx = offset + 126 + particleOffset;
            if (posxxxxxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Particle");
            }

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
