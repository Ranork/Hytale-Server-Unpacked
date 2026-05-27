package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmbienceFXConditions {
   public static final int NULLABLE_BIT_FIELD_SIZE = 3;
   public static final int FIXED_BLOCK_SIZE = 101;
   public static final int VARIABLE_FIELD_COUNT = 9;
   public static final int VARIABLE_BLOCK_START = 137;
   public static final int MAX_SIZE = 221184182;
   public boolean never;
   @Nullable
   public int[] environmentIndices;
   @Nullable
   public int[] weatherIndices;
   @Nullable
   public int[] fluidFXIndices;
   public int environmentTagPatternIndex;
   public int weatherTagPatternIndex;
   @Nullable
   public AmbienceFXBlockSoundSet[] surroundingBlockSoundSets;
   @Nullable
   public Range altitude;
   @Nullable
   public Rangeb walls;
   public boolean roof;
   public int roofMaterialTagPatternIndex;
   public boolean floor;
   @Nullable
   public Rangeb sunLightLevel;
   @Nullable
   public Rangeb torchLightLevel;
   @Nullable
   public Rangeb globalLightLevel;
   @Nullable
   public Rangef dayTime;
   @Nullable
   public SpaceSize[] space;
   @Nullable
   public ShelterType[] shelter;
   @Nullable
   public SurfaceType[] surfaces;
   @Nonnull
   public RoofState roofState = RoofState.Any;
   @Nullable
   public Rangef spaceScaleRange;
   @Nullable
   public Rangef spaceScaleMinRange;
   @Nullable
   public Rangef spaceScaleMaxRange;
   @Nullable
   public Rangef escapedRayPercentRange;
   @Nullable
   public Rangef reflectionCoeffRange;
   @Nullable
   public Rangef absorptionCoeffRange;
   @Nullable
   public Rangef roofDistanceRange;
   @Nullable
   public AmbienceFXPhysicalMaterial[] surfacePhysicalMaterials;
   public boolean surfacePhysicalMaterialsMatchAny;
   @Nullable
   public AmbienceFXPhysicalMaterial[] exteriorRoofPhysicalMaterials;
   public boolean exteriorRoofPhysicalMaterialsMatchAny;

   public AmbienceFXConditions() {
   }

   public AmbienceFXConditions(
      boolean never,
      @Nullable int[] environmentIndices,
      @Nullable int[] weatherIndices,
      @Nullable int[] fluidFXIndices,
      int environmentTagPatternIndex,
      int weatherTagPatternIndex,
      @Nullable AmbienceFXBlockSoundSet[] surroundingBlockSoundSets,
      @Nullable Range altitude,
      @Nullable Rangeb walls,
      boolean roof,
      int roofMaterialTagPatternIndex,
      boolean floor,
      @Nullable Rangeb sunLightLevel,
      @Nullable Rangeb torchLightLevel,
      @Nullable Rangeb globalLightLevel,
      @Nullable Rangef dayTime,
      @Nullable SpaceSize[] space,
      @Nullable ShelterType[] shelter,
      @Nullable SurfaceType[] surfaces,
      @Nonnull RoofState roofState,
      @Nullable Rangef spaceScaleRange,
      @Nullable Rangef spaceScaleMinRange,
      @Nullable Rangef spaceScaleMaxRange,
      @Nullable Rangef escapedRayPercentRange,
      @Nullable Rangef reflectionCoeffRange,
      @Nullable Rangef absorptionCoeffRange,
      @Nullable Rangef roofDistanceRange,
      @Nullable AmbienceFXPhysicalMaterial[] surfacePhysicalMaterials,
      boolean surfacePhysicalMaterialsMatchAny,
      @Nullable AmbienceFXPhysicalMaterial[] exteriorRoofPhysicalMaterials,
      boolean exteriorRoofPhysicalMaterialsMatchAny
   ) {
      this.never = never;
      this.environmentIndices = environmentIndices;
      this.weatherIndices = weatherIndices;
      this.fluidFXIndices = fluidFXIndices;
      this.environmentTagPatternIndex = environmentTagPatternIndex;
      this.weatherTagPatternIndex = weatherTagPatternIndex;
      this.surroundingBlockSoundSets = surroundingBlockSoundSets;
      this.altitude = altitude;
      this.walls = walls;
      this.roof = roof;
      this.roofMaterialTagPatternIndex = roofMaterialTagPatternIndex;
      this.floor = floor;
      this.sunLightLevel = sunLightLevel;
      this.torchLightLevel = torchLightLevel;
      this.globalLightLevel = globalLightLevel;
      this.dayTime = dayTime;
      this.space = space;
      this.shelter = shelter;
      this.surfaces = surfaces;
      this.roofState = roofState;
      this.spaceScaleRange = spaceScaleRange;
      this.spaceScaleMinRange = spaceScaleMinRange;
      this.spaceScaleMaxRange = spaceScaleMaxRange;
      this.escapedRayPercentRange = escapedRayPercentRange;
      this.reflectionCoeffRange = reflectionCoeffRange;
      this.absorptionCoeffRange = absorptionCoeffRange;
      this.roofDistanceRange = roofDistanceRange;
      this.surfacePhysicalMaterials = surfacePhysicalMaterials;
      this.surfacePhysicalMaterialsMatchAny = surfacePhysicalMaterialsMatchAny;
      this.exteriorRoofPhysicalMaterials = exteriorRoofPhysicalMaterials;
      this.exteriorRoofPhysicalMaterialsMatchAny = exteriorRoofPhysicalMaterialsMatchAny;
   }

   public AmbienceFXConditions(@Nonnull AmbienceFXConditions other) {
      this.never = other.never;
      this.environmentIndices = other.environmentIndices;
      this.weatherIndices = other.weatherIndices;
      this.fluidFXIndices = other.fluidFXIndices;
      this.environmentTagPatternIndex = other.environmentTagPatternIndex;
      this.weatherTagPatternIndex = other.weatherTagPatternIndex;
      this.surroundingBlockSoundSets = other.surroundingBlockSoundSets;
      this.altitude = other.altitude;
      this.walls = other.walls;
      this.roof = other.roof;
      this.roofMaterialTagPatternIndex = other.roofMaterialTagPatternIndex;
      this.floor = other.floor;
      this.sunLightLevel = other.sunLightLevel;
      this.torchLightLevel = other.torchLightLevel;
      this.globalLightLevel = other.globalLightLevel;
      this.dayTime = other.dayTime;
      this.space = other.space;
      this.shelter = other.shelter;
      this.surfaces = other.surfaces;
      this.roofState = other.roofState;
      this.spaceScaleRange = other.spaceScaleRange;
      this.spaceScaleMinRange = other.spaceScaleMinRange;
      this.spaceScaleMaxRange = other.spaceScaleMaxRange;
      this.escapedRayPercentRange = other.escapedRayPercentRange;
      this.reflectionCoeffRange = other.reflectionCoeffRange;
      this.absorptionCoeffRange = other.absorptionCoeffRange;
      this.roofDistanceRange = other.roofDistanceRange;
      this.surfacePhysicalMaterials = other.surfacePhysicalMaterials;
      this.surfacePhysicalMaterialsMatchAny = other.surfacePhysicalMaterialsMatchAny;
      this.exteriorRoofPhysicalMaterials = other.exteriorRoofPhysicalMaterials;
      this.exteriorRoofPhysicalMaterialsMatchAny = other.exteriorRoofPhysicalMaterialsMatchAny;
   }

   @Nonnull
   public static AmbienceFXConditions deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 137) {
         throw ProtocolException.bufferTooSmall("AmbienceFXConditions", 137, buf.readableBytes() - offset);
      } else {
         AmbienceFXConditions obj = new AmbienceFXConditions();
         byte[] nullBits = PacketIO.readBytes(buf, offset, 3);
         obj.never = buf.getByte(offset + 3) != 0;
         obj.environmentTagPatternIndex = buf.getIntLE(offset + 4);
         obj.weatherTagPatternIndex = buf.getIntLE(offset + 8);
         if ((nullBits[0] & 1) != 0) {
            obj.altitude = Range.deserialize(buf, offset + 12);
         }

         if ((nullBits[0] & 2) != 0) {
            obj.walls = Rangeb.deserialize(buf, offset + 20);
         }

         obj.roof = buf.getByte(offset + 22) != 0;
         obj.roofMaterialTagPatternIndex = buf.getIntLE(offset + 23);
         obj.floor = buf.getByte(offset + 27) != 0;
         if ((nullBits[0] & 4) != 0) {
            obj.sunLightLevel = Rangeb.deserialize(buf, offset + 28);
         }

         if ((nullBits[0] & 8) != 0) {
            obj.torchLightLevel = Rangeb.deserialize(buf, offset + 30);
         }

         if ((nullBits[0] & 16) != 0) {
            obj.globalLightLevel = Rangeb.deserialize(buf, offset + 32);
         }

         if ((nullBits[0] & 32) != 0) {
            obj.dayTime = Rangef.deserialize(buf, offset + 34);
         }

         obj.roofState = RoofState.fromValue(buf.getByte(offset + 42));
         if ((nullBits[0] & 64) != 0) {
            obj.spaceScaleRange = Rangef.deserialize(buf, offset + 43);
         }

         if ((nullBits[0] & 128) != 0) {
            obj.spaceScaleMinRange = Rangef.deserialize(buf, offset + 51);
         }

         if ((nullBits[1] & 1) != 0) {
            obj.spaceScaleMaxRange = Rangef.deserialize(buf, offset + 59);
         }

         if ((nullBits[1] & 2) != 0) {
            obj.escapedRayPercentRange = Rangef.deserialize(buf, offset + 67);
         }

         if ((nullBits[1] & 4) != 0) {
            obj.reflectionCoeffRange = Rangef.deserialize(buf, offset + 75);
         }

         if ((nullBits[1] & 8) != 0) {
            obj.absorptionCoeffRange = Rangef.deserialize(buf, offset + 83);
         }

         if ((nullBits[1] & 16) != 0) {
            obj.roofDistanceRange = Rangef.deserialize(buf, offset + 91);
         }

         obj.surfacePhysicalMaterialsMatchAny = buf.getByte(offset + 99) != 0;
         obj.exteriorRoofPhysicalMaterialsMatchAny = buf.getByte(offset + 100) != 0;
         if ((nullBits[1] & 32) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 101);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 137) {
               throw ProtocolException.invalidOffset("EnvironmentIndices", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 137 + varPosBase0;
            int environmentIndicesCount = VarInt.peek(buf, varPos0);
            if (environmentIndicesCount < 0) {
               throw ProtocolException.invalidVarInt("EnvironmentIndices");
            }

            int varIntLen = VarInt.size(environmentIndicesCount);
            if (environmentIndicesCount > 4096000) {
               throw ProtocolException.arrayTooLong("EnvironmentIndices", environmentIndicesCount, 4096000);
            }

            if (varPos0 + varIntLen + environmentIndicesCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("EnvironmentIndices", varPos0 + varIntLen + environmentIndicesCount * 4, buf.readableBytes());
            }

            obj.environmentIndices = new int[environmentIndicesCount];

            for (int i = 0; i < environmentIndicesCount; i++) {
               obj.environmentIndices[i] = buf.getIntLE(varPos0 + varIntLen + i * 4);
            }
         }

         if ((nullBits[1] & 64) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 105);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 137) {
               throw ProtocolException.invalidOffset("WeatherIndices", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 137 + varPosBase1;
            int weatherIndicesCount = VarInt.peek(buf, varPos1);
            if (weatherIndicesCount < 0) {
               throw ProtocolException.invalidVarInt("WeatherIndices");
            }

            int varIntLenx = VarInt.size(weatherIndicesCount);
            if (weatherIndicesCount > 4096000) {
               throw ProtocolException.arrayTooLong("WeatherIndices", weatherIndicesCount, 4096000);
            }

            if (varPos1 + varIntLenx + weatherIndicesCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("WeatherIndices", varPos1 + varIntLenx + weatherIndicesCount * 4, buf.readableBytes());
            }

            obj.weatherIndices = new int[weatherIndicesCount];

            for (int i = 0; i < weatherIndicesCount; i++) {
               obj.weatherIndices[i] = buf.getIntLE(varPos1 + varIntLenx + i * 4);
            }
         }

         if ((nullBits[1] & 128) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 109);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 137) {
               throw ProtocolException.invalidOffset("FluidFXIndices", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 137 + varPosBase2;
            int fluidFXIndicesCount = VarInt.peek(buf, varPos2);
            if (fluidFXIndicesCount < 0) {
               throw ProtocolException.invalidVarInt("FluidFXIndices");
            }

            int varIntLenxx = VarInt.size(fluidFXIndicesCount);
            if (fluidFXIndicesCount > 4096000) {
               throw ProtocolException.arrayTooLong("FluidFXIndices", fluidFXIndicesCount, 4096000);
            }

            if (varPos2 + varIntLenxx + fluidFXIndicesCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("FluidFXIndices", varPos2 + varIntLenxx + fluidFXIndicesCount * 4, buf.readableBytes());
            }

            obj.fluidFXIndices = new int[fluidFXIndicesCount];

            for (int i = 0; i < fluidFXIndicesCount; i++) {
               obj.fluidFXIndices[i] = buf.getIntLE(varPos2 + varIntLenxx + i * 4);
            }
         }

         if ((nullBits[2] & 1) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 113);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 137) {
               throw ProtocolException.invalidOffset("SurroundingBlockSoundSets", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 137 + varPosBase3;
            int surroundingBlockSoundSetsCount = VarInt.peek(buf, varPos3);
            if (surroundingBlockSoundSetsCount < 0) {
               throw ProtocolException.invalidVarInt("SurroundingBlockSoundSets");
            }

            int varIntLenxxx = VarInt.size(surroundingBlockSoundSetsCount);
            if (surroundingBlockSoundSetsCount > 4096000) {
               throw ProtocolException.arrayTooLong("SurroundingBlockSoundSets", surroundingBlockSoundSetsCount, 4096000);
            }

            if (varPos3 + varIntLenxxx + surroundingBlockSoundSetsCount * 13L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall(
                  "SurroundingBlockSoundSets", varPos3 + varIntLenxxx + surroundingBlockSoundSetsCount * 13, buf.readableBytes()
               );
            }

            obj.surroundingBlockSoundSets = new AmbienceFXBlockSoundSet[surroundingBlockSoundSetsCount];
            int elemPos = varPos3 + varIntLenxxx;

            for (int i = 0; i < surroundingBlockSoundSetsCount; i++) {
               obj.surroundingBlockSoundSets[i] = AmbienceFXBlockSoundSet.deserialize(buf, elemPos);
               elemPos += AmbienceFXBlockSoundSet.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits[2] & 2) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 117);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 137) {
               throw ProtocolException.invalidOffset("Space", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 137 + varPosBase4;
            int spaceCount = VarInt.peek(buf, varPos4);
            if (spaceCount < 0) {
               throw ProtocolException.invalidVarInt("Space");
            }

            int varIntLenxxxx = VarInt.size(spaceCount);
            if (spaceCount > 4096000) {
               throw ProtocolException.arrayTooLong("Space", spaceCount, 4096000);
            }

            if (varPos4 + varIntLenxxxx + spaceCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Space", varPos4 + varIntLenxxxx + spaceCount * 1, buf.readableBytes());
            }

            obj.space = new SpaceSize[spaceCount];
            int elemPos = varPos4 + varIntLenxxxx;

            for (int i = 0; i < spaceCount; i++) {
               obj.space[i] = SpaceSize.fromValue(buf.getByte(elemPos));
               elemPos++;
            }
         }

         if ((nullBits[2] & 4) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 121);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 137) {
               throw ProtocolException.invalidOffset("Shelter", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 137 + varPosBase5;
            int shelterCount = VarInt.peek(buf, varPos5);
            if (shelterCount < 0) {
               throw ProtocolException.invalidVarInt("Shelter");
            }

            int varIntLenxxxxx = VarInt.size(shelterCount);
            if (shelterCount > 4096000) {
               throw ProtocolException.arrayTooLong("Shelter", shelterCount, 4096000);
            }

            if (varPos5 + varIntLenxxxxx + shelterCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Shelter", varPos5 + varIntLenxxxxx + shelterCount * 1, buf.readableBytes());
            }

            obj.shelter = new ShelterType[shelterCount];
            int elemPos = varPos5 + varIntLenxxxxx;

            for (int i = 0; i < shelterCount; i++) {
               obj.shelter[i] = ShelterType.fromValue(buf.getByte(elemPos));
               elemPos++;
            }
         }

         if ((nullBits[2] & 8) != 0) {
            int varPosBase6 = buf.getIntLE(offset + 125);
            if (varPosBase6 < 0 || varPosBase6 > buf.writerIndex() - offset - 137) {
               throw ProtocolException.invalidOffset("Surfaces", varPosBase6, buf.readableBytes());
            }

            int varPos6 = offset + 137 + varPosBase6;
            int surfacesCount = VarInt.peek(buf, varPos6);
            if (surfacesCount < 0) {
               throw ProtocolException.invalidVarInt("Surfaces");
            }

            int varIntLenxxxxxx = VarInt.size(surfacesCount);
            if (surfacesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Surfaces", surfacesCount, 4096000);
            }

            if (varPos6 + varIntLenxxxxxx + surfacesCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Surfaces", varPos6 + varIntLenxxxxxx + surfacesCount * 1, buf.readableBytes());
            }

            obj.surfaces = new SurfaceType[surfacesCount];
            int elemPos = varPos6 + varIntLenxxxxxx;

            for (int i = 0; i < surfacesCount; i++) {
               obj.surfaces[i] = SurfaceType.fromValue(buf.getByte(elemPos));
               elemPos++;
            }
         }

         if ((nullBits[2] & 16) != 0) {
            int varPosBase7 = buf.getIntLE(offset + 129);
            if (varPosBase7 < 0 || varPosBase7 > buf.writerIndex() - offset - 137) {
               throw ProtocolException.invalidOffset("SurfacePhysicalMaterials", varPosBase7, buf.readableBytes());
            }

            int varPos7 = offset + 137 + varPosBase7;
            int surfacePhysicalMaterialsCount = VarInt.peek(buf, varPos7);
            if (surfacePhysicalMaterialsCount < 0) {
               throw ProtocolException.invalidVarInt("SurfacePhysicalMaterials");
            }

            int varIntLenxxxxxxx = VarInt.size(surfacePhysicalMaterialsCount);
            if (surfacePhysicalMaterialsCount > 4096000) {
               throw ProtocolException.arrayTooLong("SurfacePhysicalMaterials", surfacePhysicalMaterialsCount, 4096000);
            }

            if (varPos7 + varIntLenxxxxxxx + surfacePhysicalMaterialsCount * 13L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall(
                  "SurfacePhysicalMaterials", varPos7 + varIntLenxxxxxxx + surfacePhysicalMaterialsCount * 13, buf.readableBytes()
               );
            }

            obj.surfacePhysicalMaterials = new AmbienceFXPhysicalMaterial[surfacePhysicalMaterialsCount];
            int elemPos = varPos7 + varIntLenxxxxxxx;

            for (int i = 0; i < surfacePhysicalMaterialsCount; i++) {
               obj.surfacePhysicalMaterials[i] = AmbienceFXPhysicalMaterial.deserialize(buf, elemPos);
               elemPos += AmbienceFXPhysicalMaterial.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits[2] & 32) != 0) {
            int varPosBase8 = buf.getIntLE(offset + 133);
            if (varPosBase8 < 0 || varPosBase8 > buf.writerIndex() - offset - 137) {
               throw ProtocolException.invalidOffset("ExteriorRoofPhysicalMaterials", varPosBase8, buf.readableBytes());
            }

            int varPos8 = offset + 137 + varPosBase8;
            int exteriorRoofPhysicalMaterialsCount = VarInt.peek(buf, varPos8);
            if (exteriorRoofPhysicalMaterialsCount < 0) {
               throw ProtocolException.invalidVarInt("ExteriorRoofPhysicalMaterials");
            }

            int varIntLenxxxxxxxx = VarInt.size(exteriorRoofPhysicalMaterialsCount);
            if (exteriorRoofPhysicalMaterialsCount > 4096000) {
               throw ProtocolException.arrayTooLong("ExteriorRoofPhysicalMaterials", exteriorRoofPhysicalMaterialsCount, 4096000);
            }

            if (varPos8 + varIntLenxxxxxxxx + exteriorRoofPhysicalMaterialsCount * 13L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall(
                  "ExteriorRoofPhysicalMaterials", varPos8 + varIntLenxxxxxxxx + exteriorRoofPhysicalMaterialsCount * 13, buf.readableBytes()
               );
            }

            obj.exteriorRoofPhysicalMaterials = new AmbienceFXPhysicalMaterial[exteriorRoofPhysicalMaterialsCount];
            int elemPos = varPos8 + varIntLenxxxxxxxx;

            for (int i = 0; i < exteriorRoofPhysicalMaterialsCount; i++) {
               obj.exteriorRoofPhysicalMaterials[i] = AmbienceFXPhysicalMaterial.deserialize(buf, elemPos);
               elemPos += AmbienceFXPhysicalMaterial.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 3);
      int maxEnd = 137;
      if ((nullBits[1] & 32) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 101);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 137) {
            throw ProtocolException.invalidOffset("EnvironmentIndices", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 137 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen) + arrLen * 4;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[1] & 64) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 105);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 137) {
            throw ProtocolException.invalidOffset("WeatherIndices", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 137 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen) + arrLen * 4;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[1] & 128) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 109);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 137) {
            throw ProtocolException.invalidOffset("FluidFXIndices", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 137 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen) + arrLen * 4;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[2] & 1) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 113);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 137) {
            throw ProtocolException.invalidOffset("SurroundingBlockSoundSets", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 137 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos3 += AmbienceFXBlockSoundSet.computeBytesConsumed(buf, pos3);
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[2] & 2) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 117);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 137) {
            throw ProtocolException.invalidOffset("Space", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 137 + fieldOffset4;
         int arrLen = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(arrLen) + arrLen * 1;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[2] & 4) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 121);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 137) {
            throw ProtocolException.invalidOffset("Shelter", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 137 + fieldOffset5;
         int arrLen = VarInt.peek(buf, pos5);
         pos5 += VarInt.size(arrLen) + arrLen * 1;
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[2] & 8) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 125);
         if (fieldOffset6 < 0 || fieldOffset6 > buf.writerIndex() - offset - 137) {
            throw ProtocolException.invalidOffset("Surfaces", fieldOffset6, maxEnd);
         }

         int pos6 = offset + 137 + fieldOffset6;
         int arrLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.size(arrLen) + arrLen * 1;
         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[2] & 16) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 129);
         if (fieldOffset7 < 0 || fieldOffset7 > buf.writerIndex() - offset - 137) {
            throw ProtocolException.invalidOffset("SurfacePhysicalMaterials", fieldOffset7, maxEnd);
         }

         int pos7 = offset + 137 + fieldOffset7;
         int arrLen = VarInt.peek(buf, pos7);
         pos7 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos7 += AmbienceFXPhysicalMaterial.computeBytesConsumed(buf, pos7);
         }

         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      if ((nullBits[2] & 32) != 0) {
         int fieldOffset8 = buf.getIntLE(offset + 133);
         if (fieldOffset8 < 0 || fieldOffset8 > buf.writerIndex() - offset - 137) {
            throw ProtocolException.invalidOffset("ExteriorRoofPhysicalMaterials", fieldOffset8, maxEnd);
         }

         int pos8 = offset + 137 + fieldOffset8;
         int arrLen = VarInt.peek(buf, pos8);
         pos8 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos8 += AmbienceFXPhysicalMaterial.computeBytesConsumed(buf, pos8);
         }

         if (pos8 - offset > maxEnd) {
            maxEnd = pos8 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 137L;
   }

   public static boolean getNever(MemorySegment mem) {
      return getNever(mem, 0);
   }

   public static boolean getNever(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3));
   }

   @Nullable
   public static int[] getEnvironmentIndices(MemorySegment mem) {
      return getEnvironmentIndices(mem, 0);
   }

   @Nullable
   public static int[] getEnvironmentIndices(MemorySegment mem, int offset) {
      if (!hasEnvironmentIndices(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 101, 137, "EnvironmentIndices");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("EnvironmentIndices", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("EnvironmentIndices", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("EnvironmentIndices", off + lenOffset + len * 4, (int)mem.byteSize());
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
   public static int[] getWeatherIndices(MemorySegment mem) {
      return getWeatherIndices(mem, 0);
   }

   @Nullable
   public static int[] getWeatherIndices(MemorySegment mem, int offset) {
      if (!hasWeatherIndices(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 105, 137, "WeatherIndices");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("WeatherIndices", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("WeatherIndices", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("WeatherIndices", off + lenOffset + len * 4, (int)mem.byteSize());
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
   public static int[] getFluidFXIndices(MemorySegment mem) {
      return getFluidFXIndices(mem, 0);
   }

   @Nullable
   public static int[] getFluidFXIndices(MemorySegment mem, int offset) {
      if (!hasFluidFXIndices(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 109, 137, "FluidFXIndices");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("FluidFXIndices", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("FluidFXIndices", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FluidFXIndices", off + lenOffset + len * 4, (int)mem.byteSize());
            } else {
               off += lenOffset;
               int[] data = new int[len];
               MemorySegment.copy(mem, PacketIO.PROTO_INT, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static int getEnvironmentTagPatternIndex(MemorySegment mem) {
      return getEnvironmentTagPatternIndex(mem, 0);
   }

   public static int getEnvironmentTagPatternIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static int getWeatherTagPatternIndex(MemorySegment mem) {
      return getWeatherTagPatternIndex(mem, 0);
   }

   public static int getWeatherTagPatternIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 8));
   }

   @Nullable
   public static AmbienceFXBlockSoundSet[] getSurroundingBlockSoundSets(MemorySegment mem) {
      return getSurroundingBlockSoundSets(mem, 0);
   }

   @Nullable
   public static AmbienceFXBlockSoundSet[] getSurroundingBlockSoundSets(MemorySegment mem, int offset) {
      if (!hasSurroundingBlockSoundSets(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 113, 137, "SurroundingBlockSoundSets");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SurroundingBlockSoundSets", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("SurroundingBlockSoundSets", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 13L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("SurroundingBlockSoundSets", off + lenOffset + len * 13, (int)mem.byteSize());
            } else {
               off += lenOffset;
               AmbienceFXBlockSoundSet[] data = new AmbienceFXBlockSoundSet[len];

               for (int i = 0; i < len; i++) {
                  data[i] = AmbienceFXBlockSoundSet.toObject(mem, off + i * 13);
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static Range getAltitude(MemorySegment mem) {
      return getAltitude(mem, 0);
   }

   @Nullable
   public static Range getAltitude(MemorySegment mem, int offset) {
      return hasAltitude(mem, offset) ? Range.toObject(mem, offset + 12) : null;
   }

   @Nullable
   public static Rangeb getWalls(MemorySegment mem) {
      return getWalls(mem, 0);
   }

   @Nullable
   public static Rangeb getWalls(MemorySegment mem, int offset) {
      return hasWalls(mem, offset) ? Rangeb.toObject(mem, offset + 20) : null;
   }

   public static boolean getRoof(MemorySegment mem) {
      return getRoof(mem, 0);
   }

   public static boolean getRoof(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 22));
   }

   public static int getRoofMaterialTagPatternIndex(MemorySegment mem) {
      return getRoofMaterialTagPatternIndex(mem, 0);
   }

   public static int getRoofMaterialTagPatternIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 23));
   }

   public static boolean getFloor(MemorySegment mem) {
      return getFloor(mem, 0);
   }

   public static boolean getFloor(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 27));
   }

   @Nullable
   public static Rangeb getSunLightLevel(MemorySegment mem) {
      return getSunLightLevel(mem, 0);
   }

   @Nullable
   public static Rangeb getSunLightLevel(MemorySegment mem, int offset) {
      return hasSunLightLevel(mem, offset) ? Rangeb.toObject(mem, offset + 28) : null;
   }

   @Nullable
   public static Rangeb getTorchLightLevel(MemorySegment mem) {
      return getTorchLightLevel(mem, 0);
   }

   @Nullable
   public static Rangeb getTorchLightLevel(MemorySegment mem, int offset) {
      return hasTorchLightLevel(mem, offset) ? Rangeb.toObject(mem, offset + 30) : null;
   }

   @Nullable
   public static Rangeb getGlobalLightLevel(MemorySegment mem) {
      return getGlobalLightLevel(mem, 0);
   }

   @Nullable
   public static Rangeb getGlobalLightLevel(MemorySegment mem, int offset) {
      return hasGlobalLightLevel(mem, offset) ? Rangeb.toObject(mem, offset + 32) : null;
   }

   @Nullable
   public static Rangef getDayTime(MemorySegment mem) {
      return getDayTime(mem, 0);
   }

   @Nullable
   public static Rangef getDayTime(MemorySegment mem, int offset) {
      return hasDayTime(mem, offset) ? Rangef.toObject(mem, offset + 34) : null;
   }

   @Nullable
   public static SpaceSize[] getSpace(MemorySegment mem) {
      return getSpace(mem, 0);
   }

   @Nullable
   public static SpaceSize[] getSpace(MemorySegment mem, int offset) {
      if (!hasSpace(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 117, 137, "Space");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Space", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Space", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Space", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               SpaceSize[] data = new SpaceSize[len];

               for (int i = 0; i < len; i++) {
                  data[i] = SpaceSize.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static ShelterType[] getShelter(MemorySegment mem) {
      return getShelter(mem, 0);
   }

   @Nullable
   public static ShelterType[] getShelter(MemorySegment mem, int offset) {
      if (!hasShelter(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 121, 137, "Shelter");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Shelter", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Shelter", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Shelter", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ShelterType[] data = new ShelterType[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ShelterType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static SurfaceType[] getSurfaces(MemorySegment mem) {
      return getSurfaces(mem, 0);
   }

   @Nullable
   public static SurfaceType[] getSurfaces(MemorySegment mem, int offset) {
      if (!hasSurfaces(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 125, 137, "Surfaces");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Surfaces", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Surfaces", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Surfaces", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               SurfaceType[] data = new SurfaceType[len];

               for (int i = 0; i < len; i++) {
                  data[i] = SurfaceType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   public static RoofState getRoofState(MemorySegment mem) {
      return getRoofState(mem, 0);
   }

   public static RoofState getRoofState(MemorySegment mem, int offset) {
      return RoofState.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 42)));
   }

   @Nullable
   public static Rangef getSpaceScaleRange(MemorySegment mem) {
      return getSpaceScaleRange(mem, 0);
   }

   @Nullable
   public static Rangef getSpaceScaleRange(MemorySegment mem, int offset) {
      return hasSpaceScaleRange(mem, offset) ? Rangef.toObject(mem, offset + 43) : null;
   }

   @Nullable
   public static Rangef getSpaceScaleMinRange(MemorySegment mem) {
      return getSpaceScaleMinRange(mem, 0);
   }

   @Nullable
   public static Rangef getSpaceScaleMinRange(MemorySegment mem, int offset) {
      return hasSpaceScaleMinRange(mem, offset) ? Rangef.toObject(mem, offset + 51) : null;
   }

   @Nullable
   public static Rangef getSpaceScaleMaxRange(MemorySegment mem) {
      return getSpaceScaleMaxRange(mem, 0);
   }

   @Nullable
   public static Rangef getSpaceScaleMaxRange(MemorySegment mem, int offset) {
      return hasSpaceScaleMaxRange(mem, offset) ? Rangef.toObject(mem, offset + 59) : null;
   }

   @Nullable
   public static Rangef getEscapedRayPercentRange(MemorySegment mem) {
      return getEscapedRayPercentRange(mem, 0);
   }

   @Nullable
   public static Rangef getEscapedRayPercentRange(MemorySegment mem, int offset) {
      return hasEscapedRayPercentRange(mem, offset) ? Rangef.toObject(mem, offset + 67) : null;
   }

   @Nullable
   public static Rangef getReflectionCoeffRange(MemorySegment mem) {
      return getReflectionCoeffRange(mem, 0);
   }

   @Nullable
   public static Rangef getReflectionCoeffRange(MemorySegment mem, int offset) {
      return hasReflectionCoeffRange(mem, offset) ? Rangef.toObject(mem, offset + 75) : null;
   }

   @Nullable
   public static Rangef getAbsorptionCoeffRange(MemorySegment mem) {
      return getAbsorptionCoeffRange(mem, 0);
   }

   @Nullable
   public static Rangef getAbsorptionCoeffRange(MemorySegment mem, int offset) {
      return hasAbsorptionCoeffRange(mem, offset) ? Rangef.toObject(mem, offset + 83) : null;
   }

   @Nullable
   public static Rangef getRoofDistanceRange(MemorySegment mem) {
      return getRoofDistanceRange(mem, 0);
   }

   @Nullable
   public static Rangef getRoofDistanceRange(MemorySegment mem, int offset) {
      return hasRoofDistanceRange(mem, offset) ? Rangef.toObject(mem, offset + 91) : null;
   }

   @Nullable
   public static AmbienceFXPhysicalMaterial[] getSurfacePhysicalMaterials(MemorySegment mem) {
      return getSurfacePhysicalMaterials(mem, 0);
   }

   @Nullable
   public static AmbienceFXPhysicalMaterial[] getSurfacePhysicalMaterials(MemorySegment mem, int offset) {
      if (!hasSurfacePhysicalMaterials(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 129, 137, "SurfacePhysicalMaterials");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SurfacePhysicalMaterials", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("SurfacePhysicalMaterials", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 13L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("SurfacePhysicalMaterials", off + lenOffset + len * 13, (int)mem.byteSize());
            } else {
               off += lenOffset;
               AmbienceFXPhysicalMaterial[] data = new AmbienceFXPhysicalMaterial[len];

               for (int i = 0; i < len; i++) {
                  data[i] = AmbienceFXPhysicalMaterial.toObject(mem, off + i * 13);
               }

               return data;
            }
         }
      }
   }

   public static boolean getSurfacePhysicalMaterialsMatchAny(MemorySegment mem) {
      return getSurfacePhysicalMaterialsMatchAny(mem, 0);
   }

   public static boolean getSurfacePhysicalMaterialsMatchAny(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 99));
   }

   @Nullable
   public static AmbienceFXPhysicalMaterial[] getExteriorRoofPhysicalMaterials(MemorySegment mem) {
      return getExteriorRoofPhysicalMaterials(mem, 0);
   }

   @Nullable
   public static AmbienceFXPhysicalMaterial[] getExteriorRoofPhysicalMaterials(MemorySegment mem, int offset) {
      if (!hasExteriorRoofPhysicalMaterials(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 133, 137, "ExteriorRoofPhysicalMaterials");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ExteriorRoofPhysicalMaterials", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ExteriorRoofPhysicalMaterials", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 13L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ExteriorRoofPhysicalMaterials", off + lenOffset + len * 13, (int)mem.byteSize());
            } else {
               off += lenOffset;
               AmbienceFXPhysicalMaterial[] data = new AmbienceFXPhysicalMaterial[len];

               for (int i = 0; i < len; i++) {
                  data[i] = AmbienceFXPhysicalMaterial.toObject(mem, off + i * 13);
               }

               return data;
            }
         }
      }
   }

   public static boolean getExteriorRoofPhysicalMaterialsMatchAny(MemorySegment mem) {
      return getExteriorRoofPhysicalMaterialsMatchAny(mem, 0);
   }

   public static boolean getExteriorRoofPhysicalMaterialsMatchAny(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 100));
   }

   public static boolean hasAltitude(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasWalls(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasSunLightLevel(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasTorchLightLevel(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasGlobalLightLevel(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasDayTime(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasSpaceScaleRange(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasSpaceScaleMinRange(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static boolean hasSpaceScaleMaxRange(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 1) != 0;
   }

   public static boolean hasEscapedRayPercentRange(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 2) != 0;
   }

   public static boolean hasReflectionCoeffRange(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 4) != 0;
   }

   public static boolean hasAbsorptionCoeffRange(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 8) != 0;
   }

   public static boolean hasRoofDistanceRange(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 16) != 0;
   }

   public static boolean hasEnvironmentIndices(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 32) != 0;
   }

   public static boolean hasWeatherIndices(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 64) != 0;
   }

   public static boolean hasFluidFXIndices(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 128) != 0;
   }

   public static boolean hasSurroundingBlockSoundSets(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 1) != 0;
   }

   public static boolean hasSpace(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 2) != 0;
   }

   public static boolean hasShelter(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 4) != 0;
   }

   public static boolean hasSurfaces(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 8) != 0;
   }

   public static boolean hasSurfacePhysicalMaterials(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 16) != 0;
   }

   public static boolean hasExteriorRoofPhysicalMaterials(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
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

   public static AmbienceFXConditions toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AmbienceFXConditions toObject(MemorySegment mem, int offset) {
      if (offset + 137 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AmbienceFXConditions", offset + 137, (int)mem.byteSize());
      } else {
         int[] environmentIndices = null;
         if (hasEnvironmentIndices(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 101, 137, "EnvironmentIndices");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("EnvironmentIndices", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("EnvironmentIndices", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("EnvironmentIndices", off + lenOffset + len * 4, (int)mem.byteSize());
            }

            off += lenOffset;
            environmentIndices = new int[len];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, off, environmentIndices, 0, len);
         }

         int[] weatherIndices = null;
         if (hasWeatherIndices(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 105, 137, "WeatherIndices");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("WeatherIndices", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("WeatherIndices", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("WeatherIndices", offx + lenOffset + lenx * 4, (int)mem.byteSize());
            }

            offx += lenOffset;
            weatherIndices = new int[lenx];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, offx, weatherIndices, 0, lenx);
         }

         int[] fluidFXIndices = null;
         if (hasFluidFXIndices(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 109, 137, "FluidFXIndices");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("FluidFXIndices", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("FluidFXIndices", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FluidFXIndices", offxx + lenOffset + lenxx * 4, (int)mem.byteSize());
            }

            offxx += lenOffset;
            fluidFXIndices = new int[lenxx];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, offxx, fluidFXIndices, 0, lenxx);
         }

         AmbienceFXBlockSoundSet[] surroundingBlockSoundSets = null;
         if (hasSurroundingBlockSoundSets(mem, offset)) {
            int offxxx = offset + getValidatedOffset(mem, offset, 113, 137, "SurroundingBlockSoundSets");
            long packedxxx = VarInt.getWithLength(mem, offxxx);
            int lenxxx = (int)packedxxx;
            if (lenxxx < 0) {
               throw ProtocolException.negativeLength("SurroundingBlockSoundSets", lenxxx);
            }

            if (lenxxx > 4096000) {
               throw ProtocolException.arrayTooLong("SurroundingBlockSoundSets", lenxxx, 4096000);
            }

            int lenOffset = (int)(packedxxx >>> 32);
            if (offxxx + lenOffset + lenxxx * 13L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("SurroundingBlockSoundSets", offxxx + lenOffset + lenxxx * 13, (int)mem.byteSize());
            }

            offxxx += lenOffset;
            surroundingBlockSoundSets = new AmbienceFXBlockSoundSet[lenxxx];

            for (int i = 0; i < lenxxx; i++) {
               surroundingBlockSoundSets[i] = AmbienceFXBlockSoundSet.toObject(mem, offxxx + i * 13);
            }
         }

         SpaceSize[] space = null;
         if (hasSpace(mem, offset)) {
            int offxxxx = offset + getValidatedOffset(mem, offset, 117, 137, "Space");
            long packedxxxx = VarInt.getWithLength(mem, offxxxx);
            int lenxxxx = (int)packedxxxx;
            if (lenxxxx < 0) {
               throw ProtocolException.negativeLength("Space", lenxxxx);
            }

            if (lenxxxx > 4096000) {
               throw ProtocolException.arrayTooLong("Space", lenxxxx, 4096000);
            }

            int lenOffset = (int)(packedxxxx >>> 32);
            if (offxxxx + lenOffset + lenxxxx * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Space", offxxxx + lenOffset + lenxxxx * 1, (int)mem.byteSize());
            }

            offxxxx += lenOffset;
            space = new SpaceSize[lenxxxx];

            for (int i = 0; i < lenxxxx; i++) {
               space[i] = SpaceSize.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offxxxx + i * 1)));
            }
         }

         ShelterType[] shelter = null;
         if (hasShelter(mem, offset)) {
            int offxxxxx = offset + getValidatedOffset(mem, offset, 121, 137, "Shelter");
            long packedxxxxx = VarInt.getWithLength(mem, offxxxxx);
            int lenxxxxx = (int)packedxxxxx;
            if (lenxxxxx < 0) {
               throw ProtocolException.negativeLength("Shelter", lenxxxxx);
            }

            if (lenxxxxx > 4096000) {
               throw ProtocolException.arrayTooLong("Shelter", lenxxxxx, 4096000);
            }

            int lenOffset = (int)(packedxxxxx >>> 32);
            if (offxxxxx + lenOffset + lenxxxxx * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Shelter", offxxxxx + lenOffset + lenxxxxx * 1, (int)mem.byteSize());
            }

            offxxxxx += lenOffset;
            shelter = new ShelterType[lenxxxxx];

            for (int i = 0; i < lenxxxxx; i++) {
               shelter[i] = ShelterType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offxxxxx + i * 1)));
            }
         }

         SurfaceType[] surfaces = null;
         if (hasSurfaces(mem, offset)) {
            int offxxxxxx = offset + getValidatedOffset(mem, offset, 125, 137, "Surfaces");
            long packedxxxxxx = VarInt.getWithLength(mem, offxxxxxx);
            int lenxxxxxx = (int)packedxxxxxx;
            if (lenxxxxxx < 0) {
               throw ProtocolException.negativeLength("Surfaces", lenxxxxxx);
            }

            if (lenxxxxxx > 4096000) {
               throw ProtocolException.arrayTooLong("Surfaces", lenxxxxxx, 4096000);
            }

            int lenOffset = (int)(packedxxxxxx >>> 32);
            if (offxxxxxx + lenOffset + lenxxxxxx * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Surfaces", offxxxxxx + lenOffset + lenxxxxxx * 1, (int)mem.byteSize());
            }

            offxxxxxx += lenOffset;
            surfaces = new SurfaceType[lenxxxxxx];

            for (int i = 0; i < lenxxxxxx; i++) {
               surfaces[i] = SurfaceType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offxxxxxx + i * 1)));
            }
         }

         AmbienceFXPhysicalMaterial[] surfacePhysicalMaterials = null;
         if (hasSurfacePhysicalMaterials(mem, offset)) {
            int offxxxxxxx = offset + getValidatedOffset(mem, offset, 129, 137, "SurfacePhysicalMaterials");
            long packedxxxxxxx = VarInt.getWithLength(mem, offxxxxxxx);
            int lenxxxxxxx = (int)packedxxxxxxx;
            if (lenxxxxxxx < 0) {
               throw ProtocolException.negativeLength("SurfacePhysicalMaterials", lenxxxxxxx);
            }

            if (lenxxxxxxx > 4096000) {
               throw ProtocolException.arrayTooLong("SurfacePhysicalMaterials", lenxxxxxxx, 4096000);
            }

            int lenOffset = (int)(packedxxxxxxx >>> 32);
            if (offxxxxxxx + lenOffset + lenxxxxxxx * 13L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("SurfacePhysicalMaterials", offxxxxxxx + lenOffset + lenxxxxxxx * 13, (int)mem.byteSize());
            }

            offxxxxxxx += lenOffset;
            surfacePhysicalMaterials = new AmbienceFXPhysicalMaterial[lenxxxxxxx];

            for (int i = 0; i < lenxxxxxxx; i++) {
               surfacePhysicalMaterials[i] = AmbienceFXPhysicalMaterial.toObject(mem, offxxxxxxx + i * 13);
            }
         }

         AmbienceFXPhysicalMaterial[] exteriorRoofPhysicalMaterials = null;
         if (hasExteriorRoofPhysicalMaterials(mem, offset)) {
            int offxxxxxxxx = offset + getValidatedOffset(mem, offset, 133, 137, "ExteriorRoofPhysicalMaterials");
            long packedxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxx);
            int lenxxxxxxxx = (int)packedxxxxxxxx;
            if (lenxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("ExteriorRoofPhysicalMaterials", lenxxxxxxxx);
            }

            if (lenxxxxxxxx > 4096000) {
               throw ProtocolException.arrayTooLong("ExteriorRoofPhysicalMaterials", lenxxxxxxxx, 4096000);
            }

            int lenOffset = (int)(packedxxxxxxxx >>> 32);
            if (offxxxxxxxx + lenOffset + lenxxxxxxxx * 13L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ExteriorRoofPhysicalMaterials", offxxxxxxxx + lenOffset + lenxxxxxxxx * 13, (int)mem.byteSize());
            }

            offxxxxxxxx += lenOffset;
            exteriorRoofPhysicalMaterials = new AmbienceFXPhysicalMaterial[lenxxxxxxxx];

            for (int i = 0; i < lenxxxxxxxx; i++) {
               exteriorRoofPhysicalMaterials[i] = AmbienceFXPhysicalMaterial.toObject(mem, offxxxxxxxx + i * 13);
            }
         }

         return new AmbienceFXConditions(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3)),
            environmentIndices,
            weatherIndices,
            fluidFXIndices,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 8)),
            surroundingBlockSoundSets,
            hasAltitude(mem, offset) ? Range.toObject(mem, offset + 12) : null,
            hasWalls(mem, offset) ? Rangeb.toObject(mem, offset + 20) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 22)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 23)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 27)),
            hasSunLightLevel(mem, offset) ? Rangeb.toObject(mem, offset + 28) : null,
            hasTorchLightLevel(mem, offset) ? Rangeb.toObject(mem, offset + 30) : null,
            hasGlobalLightLevel(mem, offset) ? Rangeb.toObject(mem, offset + 32) : null,
            hasDayTime(mem, offset) ? Rangef.toObject(mem, offset + 34) : null,
            space,
            shelter,
            surfaces,
            RoofState.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 42))),
            hasSpaceScaleRange(mem, offset) ? Rangef.toObject(mem, offset + 43) : null,
            hasSpaceScaleMinRange(mem, offset) ? Rangef.toObject(mem, offset + 51) : null,
            hasSpaceScaleMaxRange(mem, offset) ? Rangef.toObject(mem, offset + 59) : null,
            hasEscapedRayPercentRange(mem, offset) ? Rangef.toObject(mem, offset + 67) : null,
            hasReflectionCoeffRange(mem, offset) ? Rangef.toObject(mem, offset + 75) : null,
            hasAbsorptionCoeffRange(mem, offset) ? Rangef.toObject(mem, offset + 83) : null,
            hasRoofDistanceRange(mem, offset) ? Rangef.toObject(mem, offset + 91) : null,
            surfacePhysicalMaterials,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 99)),
            exteriorRoofPhysicalMaterials,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 100))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[3];
      if (this.altitude != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.walls != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.sunLightLevel != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.torchLightLevel != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.globalLightLevel != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.dayTime != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.spaceScaleRange != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.spaceScaleMinRange != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.spaceScaleMaxRange != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      if (this.escapedRayPercentRange != null) {
         nullBits[1] = (byte)(nullBits[1] | 2);
      }

      if (this.reflectionCoeffRange != null) {
         nullBits[1] = (byte)(nullBits[1] | 4);
      }

      if (this.absorptionCoeffRange != null) {
         nullBits[1] = (byte)(nullBits[1] | 8);
      }

      if (this.roofDistanceRange != null) {
         nullBits[1] = (byte)(nullBits[1] | 16);
      }

      if (this.environmentIndices != null) {
         nullBits[1] = (byte)(nullBits[1] | 32);
      }

      if (this.weatherIndices != null) {
         nullBits[1] = (byte)(nullBits[1] | 64);
      }

      if (this.fluidFXIndices != null) {
         nullBits[1] = (byte)(nullBits[1] | 128);
      }

      if (this.surroundingBlockSoundSets != null) {
         nullBits[2] = (byte)(nullBits[2] | 1);
      }

      if (this.space != null) {
         nullBits[2] = (byte)(nullBits[2] | 2);
      }

      if (this.shelter != null) {
         nullBits[2] = (byte)(nullBits[2] | 4);
      }

      if (this.surfaces != null) {
         nullBits[2] = (byte)(nullBits[2] | 8);
      }

      if (this.surfacePhysicalMaterials != null) {
         nullBits[2] = (byte)(nullBits[2] | 16);
      }

      if (this.exteriorRoofPhysicalMaterials != null) {
         nullBits[2] = (byte)(nullBits[2] | 32);
      }

      buf.writeBytes(nullBits);
      buf.writeByte(this.never ? 1 : 0);
      buf.writeIntLE(this.environmentTagPatternIndex);
      buf.writeIntLE(this.weatherTagPatternIndex);
      if (this.altitude != null) {
         this.altitude.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.walls != null) {
         this.walls.serialize(buf);
      } else {
         buf.writeZero(2);
      }

      buf.writeByte(this.roof ? 1 : 0);
      buf.writeIntLE(this.roofMaterialTagPatternIndex);
      buf.writeByte(this.floor ? 1 : 0);
      if (this.sunLightLevel != null) {
         this.sunLightLevel.serialize(buf);
      } else {
         buf.writeZero(2);
      }

      if (this.torchLightLevel != null) {
         this.torchLightLevel.serialize(buf);
      } else {
         buf.writeZero(2);
      }

      if (this.globalLightLevel != null) {
         this.globalLightLevel.serialize(buf);
      } else {
         buf.writeZero(2);
      }

      if (this.dayTime != null) {
         this.dayTime.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeByte(this.roofState.getValue());
      if (this.spaceScaleRange != null) {
         this.spaceScaleRange.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.spaceScaleMinRange != null) {
         this.spaceScaleMinRange.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.spaceScaleMaxRange != null) {
         this.spaceScaleMaxRange.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.escapedRayPercentRange != null) {
         this.escapedRayPercentRange.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.reflectionCoeffRange != null) {
         this.reflectionCoeffRange.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.absorptionCoeffRange != null) {
         this.absorptionCoeffRange.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.roofDistanceRange != null) {
         this.roofDistanceRange.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeByte(this.surfacePhysicalMaterialsMatchAny ? 1 : 0);
      buf.writeByte(this.exteriorRoofPhysicalMaterialsMatchAny ? 1 : 0);
      int environmentIndicesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int weatherIndicesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int fluidFXIndicesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int surroundingBlockSoundSetsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int spaceOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int shelterOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int surfacesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int surfacePhysicalMaterialsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int exteriorRoofPhysicalMaterialsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.environmentIndices != null) {
         buf.setIntLE(environmentIndicesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.environmentIndices.length > 4096000) {
            throw ProtocolException.arrayTooLong("EnvironmentIndices", this.environmentIndices.length, 4096000);
         }

         VarInt.write(buf, this.environmentIndices.length);

         for (int item : this.environmentIndices) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(environmentIndicesOffsetSlot, -1);
      }

      if (this.weatherIndices != null) {
         buf.setIntLE(weatherIndicesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.weatherIndices.length > 4096000) {
            throw ProtocolException.arrayTooLong("WeatherIndices", this.weatherIndices.length, 4096000);
         }

         VarInt.write(buf, this.weatherIndices.length);

         for (int item : this.weatherIndices) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(weatherIndicesOffsetSlot, -1);
      }

      if (this.fluidFXIndices != null) {
         buf.setIntLE(fluidFXIndicesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.fluidFXIndices.length > 4096000) {
            throw ProtocolException.arrayTooLong("FluidFXIndices", this.fluidFXIndices.length, 4096000);
         }

         VarInt.write(buf, this.fluidFXIndices.length);

         for (int item : this.fluidFXIndices) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(fluidFXIndicesOffsetSlot, -1);
      }

      if (this.surroundingBlockSoundSets != null) {
         buf.setIntLE(surroundingBlockSoundSetsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.surroundingBlockSoundSets.length > 4096000) {
            throw ProtocolException.arrayTooLong("SurroundingBlockSoundSets", this.surroundingBlockSoundSets.length, 4096000);
         }

         VarInt.write(buf, this.surroundingBlockSoundSets.length);

         for (AmbienceFXBlockSoundSet item : this.surroundingBlockSoundSets) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(surroundingBlockSoundSetsOffsetSlot, -1);
      }

      if (this.space != null) {
         buf.setIntLE(spaceOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.space.length > 4096000) {
            throw ProtocolException.arrayTooLong("Space", this.space.length, 4096000);
         }

         VarInt.write(buf, this.space.length);

         for (SpaceSize item : this.space) {
            buf.writeByte(item.getValue());
         }
      } else {
         buf.setIntLE(spaceOffsetSlot, -1);
      }

      if (this.shelter != null) {
         buf.setIntLE(shelterOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.shelter.length > 4096000) {
            throw ProtocolException.arrayTooLong("Shelter", this.shelter.length, 4096000);
         }

         VarInt.write(buf, this.shelter.length);

         for (ShelterType item : this.shelter) {
            buf.writeByte(item.getValue());
         }
      } else {
         buf.setIntLE(shelterOffsetSlot, -1);
      }

      if (this.surfaces != null) {
         buf.setIntLE(surfacesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.surfaces.length > 4096000) {
            throw ProtocolException.arrayTooLong("Surfaces", this.surfaces.length, 4096000);
         }

         VarInt.write(buf, this.surfaces.length);

         for (SurfaceType item : this.surfaces) {
            buf.writeByte(item.getValue());
         }
      } else {
         buf.setIntLE(surfacesOffsetSlot, -1);
      }

      if (this.surfacePhysicalMaterials != null) {
         buf.setIntLE(surfacePhysicalMaterialsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.surfacePhysicalMaterials.length > 4096000) {
            throw ProtocolException.arrayTooLong("SurfacePhysicalMaterials", this.surfacePhysicalMaterials.length, 4096000);
         }

         VarInt.write(buf, this.surfacePhysicalMaterials.length);

         for (AmbienceFXPhysicalMaterial item : this.surfacePhysicalMaterials) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(surfacePhysicalMaterialsOffsetSlot, -1);
      }

      if (this.exteriorRoofPhysicalMaterials != null) {
         buf.setIntLE(exteriorRoofPhysicalMaterialsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.exteriorRoofPhysicalMaterials.length > 4096000) {
            throw ProtocolException.arrayTooLong("ExteriorRoofPhysicalMaterials", this.exteriorRoofPhysicalMaterials.length, 4096000);
         }

         VarInt.write(buf, this.exteriorRoofPhysicalMaterials.length);

         for (AmbienceFXPhysicalMaterial item : this.exteriorRoofPhysicalMaterials) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(exteriorRoofPhysicalMaterialsOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.altitude != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.walls != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.sunLightLevel != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.torchLightLevel != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.globalLightLevel != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.dayTime != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.spaceScaleRange != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.spaceScaleMinRange != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      nullBits = 0;
      if (this.spaceScaleMaxRange != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.escapedRayPercentRange != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.reflectionCoeffRange != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.absorptionCoeffRange != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.roofDistanceRange != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.environmentIndices != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.weatherIndices != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.fluidFXIndices != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), nullBits);
      nullBits = 0;
      if (this.surroundingBlockSoundSets != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.space != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.shelter != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.surfaces != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.surfacePhysicalMaterials != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.exteriorRoofPhysicalMaterials != null) {
         nullBits = (byte)(nullBits | 32);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 3, this.never);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.environmentTagPatternIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 8), this.weatherTagPatternIndex);
      if (this.altitude != null) {
         this.altitude.serialize(mem, offset + 12);
      } else {
         mem.asSlice(offset + 12, 8L).fill((byte)0);
      }

      if (this.walls != null) {
         this.walls.serialize(mem, offset + 20);
      } else {
         mem.asSlice(offset + 20, 2L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 22, this.roof);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 23), this.roofMaterialTagPatternIndex);
      mem.set(PacketIO.PROTO_BOOL, offset + 27, this.floor);
      if (this.sunLightLevel != null) {
         this.sunLightLevel.serialize(mem, offset + 28);
      } else {
         mem.asSlice(offset + 28, 2L).fill((byte)0);
      }

      if (this.torchLightLevel != null) {
         this.torchLightLevel.serialize(mem, offset + 30);
      } else {
         mem.asSlice(offset + 30, 2L).fill((byte)0);
      }

      if (this.globalLightLevel != null) {
         this.globalLightLevel.serialize(mem, offset + 32);
      } else {
         mem.asSlice(offset + 32, 2L).fill((byte)0);
      }

      if (this.dayTime != null) {
         this.dayTime.serialize(mem, offset + 34);
      } else {
         mem.asSlice(offset + 34, 8L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 42), (byte)this.roofState.getValue());
      if (this.spaceScaleRange != null) {
         this.spaceScaleRange.serialize(mem, offset + 43);
      } else {
         mem.asSlice(offset + 43, 8L).fill((byte)0);
      }

      if (this.spaceScaleMinRange != null) {
         this.spaceScaleMinRange.serialize(mem, offset + 51);
      } else {
         mem.asSlice(offset + 51, 8L).fill((byte)0);
      }

      if (this.spaceScaleMaxRange != null) {
         this.spaceScaleMaxRange.serialize(mem, offset + 59);
      } else {
         mem.asSlice(offset + 59, 8L).fill((byte)0);
      }

      if (this.escapedRayPercentRange != null) {
         this.escapedRayPercentRange.serialize(mem, offset + 67);
      } else {
         mem.asSlice(offset + 67, 8L).fill((byte)0);
      }

      if (this.reflectionCoeffRange != null) {
         this.reflectionCoeffRange.serialize(mem, offset + 75);
      } else {
         mem.asSlice(offset + 75, 8L).fill((byte)0);
      }

      if (this.absorptionCoeffRange != null) {
         this.absorptionCoeffRange.serialize(mem, offset + 83);
      } else {
         mem.asSlice(offset + 83, 8L).fill((byte)0);
      }

      if (this.roofDistanceRange != null) {
         this.roofDistanceRange.serialize(mem, offset + 91);
      } else {
         mem.asSlice(offset + 91, 8L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 99, this.surfacePhysicalMaterialsMatchAny);
      mem.set(PacketIO.PROTO_BOOL, offset + 100, this.exteriorRoofPhysicalMaterialsMatchAny);
      int varOffset = offset + 137;
      if (this.environmentIndices != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 101), varOffset - offset - 137);
         if (this.environmentIndices.length > 4096000) {
            throw ProtocolException.arrayTooLong("EnvironmentIndices", this.environmentIndices.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.environmentIndices.length);
         MemorySegment.copy(this.environmentIndices, 0, mem, PacketIO.PROTO_INT, varOffset, this.environmentIndices.length);
         varOffset += this.environmentIndices.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 101), -1);
      }

      if (this.weatherIndices != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 105), varOffset - offset - 137);
         if (this.weatherIndices.length > 4096000) {
            throw ProtocolException.arrayTooLong("WeatherIndices", this.weatherIndices.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.weatherIndices.length);
         MemorySegment.copy(this.weatherIndices, 0, mem, PacketIO.PROTO_INT, varOffset, this.weatherIndices.length);
         varOffset += this.weatherIndices.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 105), -1);
      }

      if (this.fluidFXIndices != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 109), varOffset - offset - 137);
         if (this.fluidFXIndices.length > 4096000) {
            throw ProtocolException.arrayTooLong("FluidFXIndices", this.fluidFXIndices.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.fluidFXIndices.length);
         MemorySegment.copy(this.fluidFXIndices, 0, mem, PacketIO.PROTO_INT, varOffset, this.fluidFXIndices.length);
         varOffset += this.fluidFXIndices.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 109), -1);
      }

      if (this.surroundingBlockSoundSets != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 113), varOffset - offset - 137);
         if (this.surroundingBlockSoundSets.length > 4096000) {
            throw ProtocolException.arrayTooLong("SurroundingBlockSoundSets", this.surroundingBlockSoundSets.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.surroundingBlockSoundSets.length);
         int surroundingBlockSoundSetsValueOffset = 0;

         for (int i = 0; i < this.surroundingBlockSoundSets.length; i++) {
            surroundingBlockSoundSetsValueOffset += this.surroundingBlockSoundSets[i].serialize(mem, varOffset + surroundingBlockSoundSetsValueOffset);
         }

         varOffset += surroundingBlockSoundSetsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 113), -1);
      }

      if (this.space != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 117), varOffset - offset - 137);
         if (this.space.length > 4096000) {
            throw ProtocolException.arrayTooLong("Space", this.space.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.space.length);

         for (int i = 0; i < this.space.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.space[i].getValue());
         }

         varOffset += this.space.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 117), -1);
      }

      if (this.shelter != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 121), varOffset - offset - 137);
         if (this.shelter.length > 4096000) {
            throw ProtocolException.arrayTooLong("Shelter", this.shelter.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.shelter.length);

         for (int i = 0; i < this.shelter.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.shelter[i].getValue());
         }

         varOffset += this.shelter.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 121), -1);
      }

      if (this.surfaces != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 125), varOffset - offset - 137);
         if (this.surfaces.length > 4096000) {
            throw ProtocolException.arrayTooLong("Surfaces", this.surfaces.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.surfaces.length);

         for (int i = 0; i < this.surfaces.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.surfaces[i].getValue());
         }

         varOffset += this.surfaces.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 125), -1);
      }

      if (this.surfacePhysicalMaterials != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 129), varOffset - offset - 137);
         if (this.surfacePhysicalMaterials.length > 4096000) {
            throw ProtocolException.arrayTooLong("SurfacePhysicalMaterials", this.surfacePhysicalMaterials.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.surfacePhysicalMaterials.length);
         int surfacePhysicalMaterialsValueOffset = 0;

         for (int i = 0; i < this.surfacePhysicalMaterials.length; i++) {
            surfacePhysicalMaterialsValueOffset += this.surfacePhysicalMaterials[i].serialize(mem, varOffset + surfacePhysicalMaterialsValueOffset);
         }

         varOffset += surfacePhysicalMaterialsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 129), -1);
      }

      if (this.exteriorRoofPhysicalMaterials != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 133), varOffset - offset - 137);
         if (this.exteriorRoofPhysicalMaterials.length > 4096000) {
            throw ProtocolException.arrayTooLong("ExteriorRoofPhysicalMaterials", this.exteriorRoofPhysicalMaterials.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.exteriorRoofPhysicalMaterials.length);
         int exteriorRoofPhysicalMaterialsValueOffset = 0;

         for (int i = 0; i < this.exteriorRoofPhysicalMaterials.length; i++) {
            exteriorRoofPhysicalMaterialsValueOffset += this.exteriorRoofPhysicalMaterials[i]
               .serialize(mem, varOffset + exteriorRoofPhysicalMaterialsValueOffset);
         }

         varOffset += exteriorRoofPhysicalMaterialsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 133), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 137;
      if (this.environmentIndices != null) {
         size += VarInt.size(this.environmentIndices.length) + this.environmentIndices.length * 4;
      }

      if (this.weatherIndices != null) {
         size += VarInt.size(this.weatherIndices.length) + this.weatherIndices.length * 4;
      }

      if (this.fluidFXIndices != null) {
         size += VarInt.size(this.fluidFXIndices.length) + this.fluidFXIndices.length * 4;
      }

      if (this.surroundingBlockSoundSets != null) {
         size += VarInt.size(this.surroundingBlockSoundSets.length) + this.surroundingBlockSoundSets.length * 13;
      }

      if (this.space != null) {
         size += VarInt.size(this.space.length) + this.space.length * 1;
      }

      if (this.shelter != null) {
         size += VarInt.size(this.shelter.length) + this.shelter.length * 1;
      }

      if (this.surfaces != null) {
         size += VarInt.size(this.surfaces.length) + this.surfaces.length * 1;
      }

      if (this.surfacePhysicalMaterials != null) {
         size += VarInt.size(this.surfacePhysicalMaterials.length) + this.surfacePhysicalMaterials.length * 13;
      }

      if (this.exteriorRoofPhysicalMaterials != null) {
         size += VarInt.size(this.exteriorRoofPhysicalMaterials.length) + this.exteriorRoofPhysicalMaterials.length * 13;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 137) {
         return ValidationResult.error("Buffer too small: expected at least 137 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 3);
         int v = buffer.getByte(offset + 42) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid RoofState value for RoofState");
         } else {
            if ((nullBits[1] & 32) != 0) {
               v = buffer.getIntLE(offset + 101);
               if (v < 0 || v > buffer.writerIndex() - offset - 137) {
                  return ValidationResult.error("Invalid offset for EnvironmentIndices");
               }

               int pos = offset + 137 + v;
               int environmentIndicesCount = VarInt.peek(buffer, pos);
               if (environmentIndicesCount < 0) {
                  return ValidationResult.error("Invalid array count for EnvironmentIndices");
               }

               if (environmentIndicesCount > 4096000) {
                  return ValidationResult.error("EnvironmentIndices exceeds max length 4096000");
               }

               pos += VarInt.size(environmentIndicesCount);
               pos += environmentIndicesCount * 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading EnvironmentIndices");
               }
            }

            if ((nullBits[1] & 64) != 0) {
               v = buffer.getIntLE(offset + 105);
               if (v < 0 || v > buffer.writerIndex() - offset - 137) {
                  return ValidationResult.error("Invalid offset for WeatherIndices");
               }

               int posx = offset + 137 + v;
               int weatherIndicesCount = VarInt.peek(buffer, posx);
               if (weatherIndicesCount < 0) {
                  return ValidationResult.error("Invalid array count for WeatherIndices");
               }

               if (weatherIndicesCount > 4096000) {
                  return ValidationResult.error("WeatherIndices exceeds max length 4096000");
               }

               posx += VarInt.size(weatherIndicesCount);
               posx += weatherIndicesCount * 4;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading WeatherIndices");
               }
            }

            if ((nullBits[1] & 128) != 0) {
               v = buffer.getIntLE(offset + 109);
               if (v < 0 || v > buffer.writerIndex() - offset - 137) {
                  return ValidationResult.error("Invalid offset for FluidFXIndices");
               }

               int posxx = offset + 137 + v;
               int fluidFXIndicesCount = VarInt.peek(buffer, posxx);
               if (fluidFXIndicesCount < 0) {
                  return ValidationResult.error("Invalid array count for FluidFXIndices");
               }

               if (fluidFXIndicesCount > 4096000) {
                  return ValidationResult.error("FluidFXIndices exceeds max length 4096000");
               }

               posxx += VarInt.size(fluidFXIndicesCount);
               posxx += fluidFXIndicesCount * 4;
               if (posxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading FluidFXIndices");
               }
            }

            if ((nullBits[2] & 1) != 0) {
               v = buffer.getIntLE(offset + 113);
               if (v < 0 || v > buffer.writerIndex() - offset - 137) {
                  return ValidationResult.error("Invalid offset for SurroundingBlockSoundSets");
               }

               int posxxx = offset + 137 + v;
               int surroundingBlockSoundSetsCount = VarInt.peek(buffer, posxxx);
               if (surroundingBlockSoundSetsCount < 0) {
                  return ValidationResult.error("Invalid array count for SurroundingBlockSoundSets");
               }

               if (surroundingBlockSoundSetsCount > 4096000) {
                  return ValidationResult.error("SurroundingBlockSoundSets exceeds max length 4096000");
               }

               posxxx += VarInt.size(surroundingBlockSoundSetsCount);
               posxxx += surroundingBlockSoundSetsCount * 13;
               if (posxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading SurroundingBlockSoundSets");
               }
            }

            if ((nullBits[2] & 2) != 0) {
               v = buffer.getIntLE(offset + 117);
               if (v < 0 || v > buffer.writerIndex() - offset - 137) {
                  return ValidationResult.error("Invalid offset for Space");
               }

               int posxxxx = offset + 137 + v;
               int spaceCount = VarInt.peek(buffer, posxxxx);
               if (spaceCount < 0) {
                  return ValidationResult.error("Invalid array count for Space");
               }

               if (spaceCount > 4096000) {
                  return ValidationResult.error("Space exceeds max length 4096000");
               }

               posxxxx += VarInt.size(spaceCount);
               if (posxxxx + spaceCount * 1L > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Space");
               }

               for (int i = 0; i < spaceCount; i++) {
                  int vx = buffer.getByte(posxxxx) & 255;
                  if (vx >= 6) {
                     return ValidationResult.error("Invalid SpaceSize value for Space[i]");
                  }

                  posxxxx++;
               }
            }

            if ((nullBits[2] & 4) != 0) {
               v = buffer.getIntLE(offset + 121);
               if (v < 0 || v > buffer.writerIndex() - offset - 137) {
                  return ValidationResult.error("Invalid offset for Shelter");
               }

               int posxxxxx = offset + 137 + v;
               int shelterCount = VarInt.peek(buffer, posxxxxx);
               if (shelterCount < 0) {
                  return ValidationResult.error("Invalid array count for Shelter");
               }

               if (shelterCount > 4096000) {
                  return ValidationResult.error("Shelter exceeds max length 4096000");
               }

               posxxxxx += VarInt.size(shelterCount);
               if (posxxxxx + shelterCount * 1L > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Shelter");
               }

               for (int i = 0; i < shelterCount; i++) {
                  int vx = buffer.getByte(posxxxxx) & 255;
                  if (vx >= 5) {
                     return ValidationResult.error("Invalid ShelterType value for Shelter[i]");
                  }

                  posxxxxx++;
               }
            }

            if ((nullBits[2] & 8) != 0) {
               v = buffer.getIntLE(offset + 125);
               if (v < 0 || v > buffer.writerIndex() - offset - 137) {
                  return ValidationResult.error("Invalid offset for Surfaces");
               }

               int posxxxxxx = offset + 137 + v;
               int surfacesCount = VarInt.peek(buffer, posxxxxxx);
               if (surfacesCount < 0) {
                  return ValidationResult.error("Invalid array count for Surfaces");
               }

               if (surfacesCount > 4096000) {
                  return ValidationResult.error("Surfaces exceeds max length 4096000");
               }

               posxxxxxx += VarInt.size(surfacesCount);
               if (posxxxxxx + surfacesCount * 1L > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Surfaces");
               }

               for (int i = 0; i < surfacesCount; i++) {
                  int vx = buffer.getByte(posxxxxxx) & 255;
                  if (vx >= 4) {
                     return ValidationResult.error("Invalid SurfaceType value for Surfaces[i]");
                  }

                  posxxxxxx++;
               }
            }

            if ((nullBits[2] & 16) != 0) {
               v = buffer.getIntLE(offset + 129);
               if (v < 0 || v > buffer.writerIndex() - offset - 137) {
                  return ValidationResult.error("Invalid offset for SurfacePhysicalMaterials");
               }

               int posxxxxxxx = offset + 137 + v;
               int surfacePhysicalMaterialsCount = VarInt.peek(buffer, posxxxxxxx);
               if (surfacePhysicalMaterialsCount < 0) {
                  return ValidationResult.error("Invalid array count for SurfacePhysicalMaterials");
               }

               if (surfacePhysicalMaterialsCount > 4096000) {
                  return ValidationResult.error("SurfacePhysicalMaterials exceeds max length 4096000");
               }

               posxxxxxxx += VarInt.size(surfacePhysicalMaterialsCount);
               posxxxxxxx += surfacePhysicalMaterialsCount * 13;
               if (posxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading SurfacePhysicalMaterials");
               }
            }

            if ((nullBits[2] & 32) != 0) {
               v = buffer.getIntLE(offset + 133);
               if (v < 0 || v > buffer.writerIndex() - offset - 137) {
                  return ValidationResult.error("Invalid offset for ExteriorRoofPhysicalMaterials");
               }

               int posxxxxxxxx = offset + 137 + v;
               int exteriorRoofPhysicalMaterialsCount = VarInt.peek(buffer, posxxxxxxxx);
               if (exteriorRoofPhysicalMaterialsCount < 0) {
                  return ValidationResult.error("Invalid array count for ExteriorRoofPhysicalMaterials");
               }

               if (exteriorRoofPhysicalMaterialsCount > 4096000) {
                  return ValidationResult.error("ExteriorRoofPhysicalMaterials exceeds max length 4096000");
               }

               posxxxxxxxx += VarInt.size(exteriorRoofPhysicalMaterialsCount);
               posxxxxxxxx += exteriorRoofPhysicalMaterialsCount * 13;
               if (posxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading ExteriorRoofPhysicalMaterials");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public AmbienceFXConditions clone() {
      AmbienceFXConditions copy = new AmbienceFXConditions();
      copy.never = this.never;
      copy.environmentIndices = this.environmentIndices != null ? Arrays.copyOf(this.environmentIndices, this.environmentIndices.length) : null;
      copy.weatherIndices = this.weatherIndices != null ? Arrays.copyOf(this.weatherIndices, this.weatherIndices.length) : null;
      copy.fluidFXIndices = this.fluidFXIndices != null ? Arrays.copyOf(this.fluidFXIndices, this.fluidFXIndices.length) : null;
      copy.environmentTagPatternIndex = this.environmentTagPatternIndex;
      copy.weatherTagPatternIndex = this.weatherTagPatternIndex;
      copy.surroundingBlockSoundSets = this.surroundingBlockSoundSets != null
         ? Arrays.stream(this.surroundingBlockSoundSets).map(e -> e.clone()).toArray(AmbienceFXBlockSoundSet[]::new)
         : null;
      copy.altitude = this.altitude != null ? this.altitude.clone() : null;
      copy.walls = this.walls != null ? this.walls.clone() : null;
      copy.roof = this.roof;
      copy.roofMaterialTagPatternIndex = this.roofMaterialTagPatternIndex;
      copy.floor = this.floor;
      copy.sunLightLevel = this.sunLightLevel != null ? this.sunLightLevel.clone() : null;
      copy.torchLightLevel = this.torchLightLevel != null ? this.torchLightLevel.clone() : null;
      copy.globalLightLevel = this.globalLightLevel != null ? this.globalLightLevel.clone() : null;
      copy.dayTime = this.dayTime != null ? this.dayTime.clone() : null;
      copy.space = this.space != null ? Arrays.copyOf(this.space, this.space.length) : null;
      copy.shelter = this.shelter != null ? Arrays.copyOf(this.shelter, this.shelter.length) : null;
      copy.surfaces = this.surfaces != null ? Arrays.copyOf(this.surfaces, this.surfaces.length) : null;
      copy.roofState = this.roofState;
      copy.spaceScaleRange = this.spaceScaleRange != null ? this.spaceScaleRange.clone() : null;
      copy.spaceScaleMinRange = this.spaceScaleMinRange != null ? this.spaceScaleMinRange.clone() : null;
      copy.spaceScaleMaxRange = this.spaceScaleMaxRange != null ? this.spaceScaleMaxRange.clone() : null;
      copy.escapedRayPercentRange = this.escapedRayPercentRange != null ? this.escapedRayPercentRange.clone() : null;
      copy.reflectionCoeffRange = this.reflectionCoeffRange != null ? this.reflectionCoeffRange.clone() : null;
      copy.absorptionCoeffRange = this.absorptionCoeffRange != null ? this.absorptionCoeffRange.clone() : null;
      copy.roofDistanceRange = this.roofDistanceRange != null ? this.roofDistanceRange.clone() : null;
      copy.surfacePhysicalMaterials = this.surfacePhysicalMaterials != null
         ? Arrays.stream(this.surfacePhysicalMaterials).map(e -> e.clone()).toArray(AmbienceFXPhysicalMaterial[]::new)
         : null;
      copy.surfacePhysicalMaterialsMatchAny = this.surfacePhysicalMaterialsMatchAny;
      copy.exteriorRoofPhysicalMaterials = this.exteriorRoofPhysicalMaterials != null
         ? Arrays.stream(this.exteriorRoofPhysicalMaterials).map(e -> e.clone()).toArray(AmbienceFXPhysicalMaterial[]::new)
         : null;
      copy.exteriorRoofPhysicalMaterialsMatchAny = this.exteriorRoofPhysicalMaterialsMatchAny;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AmbienceFXConditions other)
            ? false
            : this.never == other.never
               && Arrays.equals(this.environmentIndices, other.environmentIndices)
               && Arrays.equals(this.weatherIndices, other.weatherIndices)
               && Arrays.equals(this.fluidFXIndices, other.fluidFXIndices)
               && this.environmentTagPatternIndex == other.environmentTagPatternIndex
               && this.weatherTagPatternIndex == other.weatherTagPatternIndex
               && Arrays.equals((Object[])this.surroundingBlockSoundSets, (Object[])other.surroundingBlockSoundSets)
               && Objects.equals(this.altitude, other.altitude)
               && Objects.equals(this.walls, other.walls)
               && this.roof == other.roof
               && this.roofMaterialTagPatternIndex == other.roofMaterialTagPatternIndex
               && this.floor == other.floor
               && Objects.equals(this.sunLightLevel, other.sunLightLevel)
               && Objects.equals(this.torchLightLevel, other.torchLightLevel)
               && Objects.equals(this.globalLightLevel, other.globalLightLevel)
               && Objects.equals(this.dayTime, other.dayTime)
               && Arrays.equals((Object[])this.space, (Object[])other.space)
               && Arrays.equals((Object[])this.shelter, (Object[])other.shelter)
               && Arrays.equals((Object[])this.surfaces, (Object[])other.surfaces)
               && Objects.equals(this.roofState, other.roofState)
               && Objects.equals(this.spaceScaleRange, other.spaceScaleRange)
               && Objects.equals(this.spaceScaleMinRange, other.spaceScaleMinRange)
               && Objects.equals(this.spaceScaleMaxRange, other.spaceScaleMaxRange)
               && Objects.equals(this.escapedRayPercentRange, other.escapedRayPercentRange)
               && Objects.equals(this.reflectionCoeffRange, other.reflectionCoeffRange)
               && Objects.equals(this.absorptionCoeffRange, other.absorptionCoeffRange)
               && Objects.equals(this.roofDistanceRange, other.roofDistanceRange)
               && Arrays.equals((Object[])this.surfacePhysicalMaterials, (Object[])other.surfacePhysicalMaterials)
               && this.surfacePhysicalMaterialsMatchAny == other.surfacePhysicalMaterialsMatchAny
               && Arrays.equals((Object[])this.exteriorRoofPhysicalMaterials, (Object[])other.exteriorRoofPhysicalMaterials)
               && this.exteriorRoofPhysicalMaterialsMatchAny == other.exteriorRoofPhysicalMaterialsMatchAny;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Boolean.hashCode(this.never);
      result = 31 * result + Arrays.hashCode(this.environmentIndices);
      result = 31 * result + Arrays.hashCode(this.weatherIndices);
      result = 31 * result + Arrays.hashCode(this.fluidFXIndices);
      result = 31 * result + Integer.hashCode(this.environmentTagPatternIndex);
      result = 31 * result + Integer.hashCode(this.weatherTagPatternIndex);
      result = 31 * result + Arrays.hashCode((Object[])this.surroundingBlockSoundSets);
      result = 31 * result + Objects.hashCode(this.altitude);
      result = 31 * result + Objects.hashCode(this.walls);
      result = 31 * result + Boolean.hashCode(this.roof);
      result = 31 * result + Integer.hashCode(this.roofMaterialTagPatternIndex);
      result = 31 * result + Boolean.hashCode(this.floor);
      result = 31 * result + Objects.hashCode(this.sunLightLevel);
      result = 31 * result + Objects.hashCode(this.torchLightLevel);
      result = 31 * result + Objects.hashCode(this.globalLightLevel);
      result = 31 * result + Objects.hashCode(this.dayTime);
      result = 31 * result + Arrays.hashCode((Object[])this.space);
      result = 31 * result + Arrays.hashCode((Object[])this.shelter);
      result = 31 * result + Arrays.hashCode((Object[])this.surfaces);
      result = 31 * result + Objects.hashCode(this.roofState);
      result = 31 * result + Objects.hashCode(this.spaceScaleRange);
      result = 31 * result + Objects.hashCode(this.spaceScaleMinRange);
      result = 31 * result + Objects.hashCode(this.spaceScaleMaxRange);
      result = 31 * result + Objects.hashCode(this.escapedRayPercentRange);
      result = 31 * result + Objects.hashCode(this.reflectionCoeffRange);
      result = 31 * result + Objects.hashCode(this.absorptionCoeffRange);
      result = 31 * result + Objects.hashCode(this.roofDistanceRange);
      result = 31 * result + Arrays.hashCode((Object[])this.surfacePhysicalMaterials);
      result = 31 * result + Boolean.hashCode(this.surfacePhysicalMaterialsMatchAny);
      result = 31 * result + Arrays.hashCode((Object[])this.exteriorRoofPhysicalMaterials);
      return 31 * result + Boolean.hashCode(this.exteriorRoofPhysicalMaterialsMatchAny);
   }
}
