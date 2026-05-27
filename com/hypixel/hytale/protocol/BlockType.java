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

public class BlockType {
   public static final int NULLABLE_BIT_FIELD_SIZE = 5;
   public static final int FIXED_BLOCK_SIZE = 176;
   public static final int VARIABLE_FIELD_COUNT = 25;
   public static final int VARIABLE_BLOCK_START = 276;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String item;
   @Nullable
   public String name;
   public boolean unknown;
   @Nonnull
   public DrawType drawType = DrawType.Empty;
   @Nonnull
   public BlockMaterial material = BlockMaterial.Empty;
   @Nonnull
   public Opacity opacity = Opacity.Solid;
   @Nullable
   public ShaderType[] shaderEffect;
   public int hitbox;
   public int interactionHitbox;
   @Nullable
   public String model;
   @Nullable
   public ModelTexture[] modelTexture;
   public float modelScale;
   @Nullable
   public String modelAnimation;
   public boolean looping;
   public int maxSupportDistance;
   @Nonnull
   public BlockSupportsRequiredForType blockSupportsRequiredFor = BlockSupportsRequiredForType.Any;
   @Nullable
   public Map<BlockNeighbor, RequiredBlockFaceSupport[]> support;
   @Nullable
   public Map<BlockNeighbor, BlockFaceSupport[]> supporting;
   public boolean requiresAlphaBlending;
   @Nullable
   public BlockTextures[] cubeTextures;
   @Nullable
   public String cubeSideMaskTexture;
   @Nonnull
   public ShadingMode cubeShadingMode = ShadingMode.Standard;
   @Nonnull
   public RandomRotation randomRotation = RandomRotation.None;
   @Nonnull
   public VariantRotation variantRotation = VariantRotation.None;
   @Nonnull
   public Rotation rotationYawPlacementOffset = Rotation.None;
   public int blockSoundSetIndex;
   public int physicalMaterialIndex;
   public float soundOcclusionOpacity;
   public int ambientSoundEventIndex;
   @Nullable
   public ConditionalBlockSound[] conditionalSounds;
   @Nullable
   public ModelParticle[] particles;
   @Nullable
   public String blockParticleSetId;
   @Nullable
   public String blockBreakingDecalId;
   @Nullable
   public Color particleColor;
   @Nullable
   public Color textureComputedColor;
   @Nullable
   public ColorLight light;
   @Nullable
   public Tint tint;
   @Nullable
   public Tint biomeTint;
   public int group;
   @Nullable
   public String transitionTexture;
   @Nullable
   public int[] transitionToGroups;
   @Nullable
   public BlockMovementSettings movementSettings;
   @Nullable
   public BlockFlags flags;
   @Nullable
   public String interactionHint;
   @Nullable
   public BlockGathering gathering;
   @Nullable
   public BlockPlacementSettings placementSettings;
   @Nullable
   public ModelDisplay display;
   @Nullable
   public RailConfig rail;
   public boolean ignoreSupportWhenPlaced;
   @Nullable
   public Map<InteractionType, Integer> interactions;
   @Nullable
   public Map<String, Integer> states;
   public int transitionToTag;
   @Nullable
   public int[] tagIndexes;
   @Nullable
   public Bench bench;
   @Nullable
   public ConnectedBlockRuleSet connectedBlockRuleSet;

   public BlockType() {
   }

   public BlockType(
      @Nullable String item,
      @Nullable String name,
      boolean unknown,
      @Nonnull DrawType drawType,
      @Nonnull BlockMaterial material,
      @Nonnull Opacity opacity,
      @Nullable ShaderType[] shaderEffect,
      int hitbox,
      int interactionHitbox,
      @Nullable String model,
      @Nullable ModelTexture[] modelTexture,
      float modelScale,
      @Nullable String modelAnimation,
      boolean looping,
      int maxSupportDistance,
      @Nonnull BlockSupportsRequiredForType blockSupportsRequiredFor,
      @Nullable Map<BlockNeighbor, RequiredBlockFaceSupport[]> support,
      @Nullable Map<BlockNeighbor, BlockFaceSupport[]> supporting,
      boolean requiresAlphaBlending,
      @Nullable BlockTextures[] cubeTextures,
      @Nullable String cubeSideMaskTexture,
      @Nonnull ShadingMode cubeShadingMode,
      @Nonnull RandomRotation randomRotation,
      @Nonnull VariantRotation variantRotation,
      @Nonnull Rotation rotationYawPlacementOffset,
      int blockSoundSetIndex,
      int physicalMaterialIndex,
      float soundOcclusionOpacity,
      int ambientSoundEventIndex,
      @Nullable ConditionalBlockSound[] conditionalSounds,
      @Nullable ModelParticle[] particles,
      @Nullable String blockParticleSetId,
      @Nullable String blockBreakingDecalId,
      @Nullable Color particleColor,
      @Nullable Color textureComputedColor,
      @Nullable ColorLight light,
      @Nullable Tint tint,
      @Nullable Tint biomeTint,
      int group,
      @Nullable String transitionTexture,
      @Nullable int[] transitionToGroups,
      @Nullable BlockMovementSettings movementSettings,
      @Nullable BlockFlags flags,
      @Nullable String interactionHint,
      @Nullable BlockGathering gathering,
      @Nullable BlockPlacementSettings placementSettings,
      @Nullable ModelDisplay display,
      @Nullable RailConfig rail,
      boolean ignoreSupportWhenPlaced,
      @Nullable Map<InteractionType, Integer> interactions,
      @Nullable Map<String, Integer> states,
      int transitionToTag,
      @Nullable int[] tagIndexes,
      @Nullable Bench bench,
      @Nullable ConnectedBlockRuleSet connectedBlockRuleSet
   ) {
      this.item = item;
      this.name = name;
      this.unknown = unknown;
      this.drawType = drawType;
      this.material = material;
      this.opacity = opacity;
      this.shaderEffect = shaderEffect;
      this.hitbox = hitbox;
      this.interactionHitbox = interactionHitbox;
      this.model = model;
      this.modelTexture = modelTexture;
      this.modelScale = modelScale;
      this.modelAnimation = modelAnimation;
      this.looping = looping;
      this.maxSupportDistance = maxSupportDistance;
      this.blockSupportsRequiredFor = blockSupportsRequiredFor;
      this.support = support;
      this.supporting = supporting;
      this.requiresAlphaBlending = requiresAlphaBlending;
      this.cubeTextures = cubeTextures;
      this.cubeSideMaskTexture = cubeSideMaskTexture;
      this.cubeShadingMode = cubeShadingMode;
      this.randomRotation = randomRotation;
      this.variantRotation = variantRotation;
      this.rotationYawPlacementOffset = rotationYawPlacementOffset;
      this.blockSoundSetIndex = blockSoundSetIndex;
      this.physicalMaterialIndex = physicalMaterialIndex;
      this.soundOcclusionOpacity = soundOcclusionOpacity;
      this.ambientSoundEventIndex = ambientSoundEventIndex;
      this.conditionalSounds = conditionalSounds;
      this.particles = particles;
      this.blockParticleSetId = blockParticleSetId;
      this.blockBreakingDecalId = blockBreakingDecalId;
      this.particleColor = particleColor;
      this.textureComputedColor = textureComputedColor;
      this.light = light;
      this.tint = tint;
      this.biomeTint = biomeTint;
      this.group = group;
      this.transitionTexture = transitionTexture;
      this.transitionToGroups = transitionToGroups;
      this.movementSettings = movementSettings;
      this.flags = flags;
      this.interactionHint = interactionHint;
      this.gathering = gathering;
      this.placementSettings = placementSettings;
      this.display = display;
      this.rail = rail;
      this.ignoreSupportWhenPlaced = ignoreSupportWhenPlaced;
      this.interactions = interactions;
      this.states = states;
      this.transitionToTag = transitionToTag;
      this.tagIndexes = tagIndexes;
      this.bench = bench;
      this.connectedBlockRuleSet = connectedBlockRuleSet;
   }

   public BlockType(@Nonnull BlockType other) {
      this.item = other.item;
      this.name = other.name;
      this.unknown = other.unknown;
      this.drawType = other.drawType;
      this.material = other.material;
      this.opacity = other.opacity;
      this.shaderEffect = other.shaderEffect;
      this.hitbox = other.hitbox;
      this.interactionHitbox = other.interactionHitbox;
      this.model = other.model;
      this.modelTexture = other.modelTexture;
      this.modelScale = other.modelScale;
      this.modelAnimation = other.modelAnimation;
      this.looping = other.looping;
      this.maxSupportDistance = other.maxSupportDistance;
      this.blockSupportsRequiredFor = other.blockSupportsRequiredFor;
      this.support = other.support;
      this.supporting = other.supporting;
      this.requiresAlphaBlending = other.requiresAlphaBlending;
      this.cubeTextures = other.cubeTextures;
      this.cubeSideMaskTexture = other.cubeSideMaskTexture;
      this.cubeShadingMode = other.cubeShadingMode;
      this.randomRotation = other.randomRotation;
      this.variantRotation = other.variantRotation;
      this.rotationYawPlacementOffset = other.rotationYawPlacementOffset;
      this.blockSoundSetIndex = other.blockSoundSetIndex;
      this.physicalMaterialIndex = other.physicalMaterialIndex;
      this.soundOcclusionOpacity = other.soundOcclusionOpacity;
      this.ambientSoundEventIndex = other.ambientSoundEventIndex;
      this.conditionalSounds = other.conditionalSounds;
      this.particles = other.particles;
      this.blockParticleSetId = other.blockParticleSetId;
      this.blockBreakingDecalId = other.blockBreakingDecalId;
      this.particleColor = other.particleColor;
      this.textureComputedColor = other.textureComputedColor;
      this.light = other.light;
      this.tint = other.tint;
      this.biomeTint = other.biomeTint;
      this.group = other.group;
      this.transitionTexture = other.transitionTexture;
      this.transitionToGroups = other.transitionToGroups;
      this.movementSettings = other.movementSettings;
      this.flags = other.flags;
      this.interactionHint = other.interactionHint;
      this.gathering = other.gathering;
      this.placementSettings = other.placementSettings;
      this.display = other.display;
      this.rail = other.rail;
      this.ignoreSupportWhenPlaced = other.ignoreSupportWhenPlaced;
      this.interactions = other.interactions;
      this.states = other.states;
      this.transitionToTag = other.transitionToTag;
      this.tagIndexes = other.tagIndexes;
      this.bench = other.bench;
      this.connectedBlockRuleSet = other.connectedBlockRuleSet;
   }

   @Nonnull
   public static BlockType deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 276) {
         throw ProtocolException.bufferTooSmall("BlockType", 276, buf.readableBytes() - offset);
      } else {
         BlockType obj = new BlockType();
         byte[] nullBits = PacketIO.readBytes(buf, offset, 5);
         obj.unknown = buf.getByte(offset + 5) != 0;
         obj.drawType = DrawType.fromValue(buf.getByte(offset + 6));
         obj.material = BlockMaterial.fromValue(buf.getByte(offset + 7));
         obj.opacity = Opacity.fromValue(buf.getByte(offset + 8));
         obj.hitbox = buf.getIntLE(offset + 9);
         obj.interactionHitbox = buf.getIntLE(offset + 13);
         obj.modelScale = buf.getFloatLE(offset + 17);
         obj.looping = buf.getByte(offset + 21) != 0;
         obj.maxSupportDistance = buf.getIntLE(offset + 22);
         obj.blockSupportsRequiredFor = BlockSupportsRequiredForType.fromValue(buf.getByte(offset + 26));
         obj.requiresAlphaBlending = buf.getByte(offset + 27) != 0;
         obj.cubeShadingMode = ShadingMode.fromValue(buf.getByte(offset + 28));
         obj.randomRotation = RandomRotation.fromValue(buf.getByte(offset + 29));
         obj.variantRotation = VariantRotation.fromValue(buf.getByte(offset + 30));
         obj.rotationYawPlacementOffset = Rotation.fromValue(buf.getByte(offset + 31));
         obj.blockSoundSetIndex = buf.getIntLE(offset + 32);
         obj.physicalMaterialIndex = buf.getIntLE(offset + 36);
         obj.soundOcclusionOpacity = buf.getFloatLE(offset + 40);
         obj.ambientSoundEventIndex = buf.getIntLE(offset + 44);
         if ((nullBits[0] & 1) != 0) {
            obj.particleColor = Color.deserialize(buf, offset + 48);
         }

         if ((nullBits[0] & 2) != 0) {
            obj.textureComputedColor = Color.deserialize(buf, offset + 51);
         }

         if ((nullBits[0] & 4) != 0) {
            obj.light = ColorLight.deserialize(buf, offset + 54);
         }

         if ((nullBits[0] & 8) != 0) {
            obj.tint = Tint.deserialize(buf, offset + 58);
         }

         if ((nullBits[0] & 16) != 0) {
            obj.biomeTint = Tint.deserialize(buf, offset + 82);
         }

         obj.group = buf.getIntLE(offset + 106);
         if ((nullBits[0] & 32) != 0) {
            obj.movementSettings = BlockMovementSettings.deserialize(buf, offset + 110);
         }

         if ((nullBits[0] & 64) != 0) {
            obj.flags = BlockFlags.deserialize(buf, offset + 152);
         }

         if ((nullBits[0] & 128) != 0) {
            obj.placementSettings = BlockPlacementSettings.deserialize(buf, offset + 154);
         }

         obj.ignoreSupportWhenPlaced = buf.getByte(offset + 171) != 0;
         obj.transitionToTag = buf.getIntLE(offset + 172);
         if ((nullBits[1] & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 176);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("Item", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 276 + varPosBase0;
            int itemLen = VarInt.peek(buf, varPos0);
            if (itemLen < 0) {
               throw ProtocolException.invalidVarInt("Item");
            }

            int itemVarIntLen = VarInt.size(itemLen);
            if (itemLen > 4096000) {
               throw ProtocolException.stringTooLong("Item", itemLen, 4096000);
            }

            if (varPos0 + itemVarIntLen + itemLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Item", varPos0 + itemVarIntLen + itemLen, buf.readableBytes());
            }

            obj.item = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits[1] & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 180);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("Name", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 276 + varPosBase1;
            int nameLen = VarInt.peek(buf, varPos1);
            if (nameLen < 0) {
               throw ProtocolException.invalidVarInt("Name");
            }

            int nameVarIntLen = VarInt.size(nameLen);
            if (nameLen > 4096000) {
               throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
            }

            if (varPos1 + nameVarIntLen + nameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Name", varPos1 + nameVarIntLen + nameLen, buf.readableBytes());
            }

            obj.name = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits[1] & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 184);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("ShaderEffect", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 276 + varPosBase2;
            int shaderEffectCount = VarInt.peek(buf, varPos2);
            if (shaderEffectCount < 0) {
               throw ProtocolException.invalidVarInt("ShaderEffect");
            }

            int varIntLen = VarInt.size(shaderEffectCount);
            if (shaderEffectCount > 4096000) {
               throw ProtocolException.arrayTooLong("ShaderEffect", shaderEffectCount, 4096000);
            }

            if (varPos2 + varIntLen + shaderEffectCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ShaderEffect", varPos2 + varIntLen + shaderEffectCount * 1, buf.readableBytes());
            }

            obj.shaderEffect = new ShaderType[shaderEffectCount];
            int elemPos = varPos2 + varIntLen;

            for (int i = 0; i < shaderEffectCount; i++) {
               obj.shaderEffect[i] = ShaderType.fromValue(buf.getByte(elemPos));
               elemPos++;
            }
         }

         if ((nullBits[1] & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 188);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("Model", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 276 + varPosBase3;
            int modelLen = VarInt.peek(buf, varPos3);
            if (modelLen < 0) {
               throw ProtocolException.invalidVarInt("Model");
            }

            int modelVarIntLen = VarInt.size(modelLen);
            if (modelLen > 4096000) {
               throw ProtocolException.stringTooLong("Model", modelLen, 4096000);
            }

            if (varPos3 + modelVarIntLen + modelLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Model", varPos3 + modelVarIntLen + modelLen, buf.readableBytes());
            }

            obj.model = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
         }

         if ((nullBits[1] & 16) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 192);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("ModelTexture", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 276 + varPosBase4;
            int modelTextureCount = VarInt.peek(buf, varPos4);
            if (modelTextureCount < 0) {
               throw ProtocolException.invalidVarInt("ModelTexture");
            }

            int varIntLenx = VarInt.size(modelTextureCount);
            if (modelTextureCount > 4096000) {
               throw ProtocolException.arrayTooLong("ModelTexture", modelTextureCount, 4096000);
            }

            if (varPos4 + varIntLenx + modelTextureCount * 5L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ModelTexture", varPos4 + varIntLenx + modelTextureCount * 5, buf.readableBytes());
            }

            obj.modelTexture = new ModelTexture[modelTextureCount];
            int elemPos = varPos4 + varIntLenx;

            for (int i = 0; i < modelTextureCount; i++) {
               obj.modelTexture[i] = ModelTexture.deserialize(buf, elemPos);
               elemPos += ModelTexture.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits[1] & 32) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 196);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("ModelAnimation", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 276 + varPosBase5;
            int modelAnimationLen = VarInt.peek(buf, varPos5);
            if (modelAnimationLen < 0) {
               throw ProtocolException.invalidVarInt("ModelAnimation");
            }

            int modelAnimationVarIntLen = VarInt.size(modelAnimationLen);
            if (modelAnimationLen > 4096000) {
               throw ProtocolException.stringTooLong("ModelAnimation", modelAnimationLen, 4096000);
            }

            if (varPos5 + modelAnimationVarIntLen + modelAnimationLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ModelAnimation", varPos5 + modelAnimationVarIntLen + modelAnimationLen, buf.readableBytes());
            }

            obj.modelAnimation = PacketIO.readVarString(buf, varPos5, PacketIO.UTF8);
         }

         if ((nullBits[1] & 64) != 0) {
            int varPosBase6 = buf.getIntLE(offset + 200);
            if (varPosBase6 < 0 || varPosBase6 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("Support", varPosBase6, buf.readableBytes());
            }

            int varPos6 = offset + 276 + varPosBase6;
            int supportCount = VarInt.peek(buf, varPos6);
            if (supportCount < 0) {
               throw ProtocolException.invalidVarInt("Support");
            }

            int varIntLenxx = VarInt.size(supportCount);
            if (supportCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Support", supportCount, 4096000);
            }

            obj.support = new HashMap<>(supportCount);
            int dictPos = varPos6 + varIntLenxx;

            for (int i = 0; i < supportCount; i++) {
               BlockNeighbor key = BlockNeighbor.fromValue(buf.getByte(dictPos));
               int valLen = VarInt.peek(buf, ++dictPos);
               if (valLen < 0) {
                  throw ProtocolException.invalidVarInt("val");
               }

               int valVarLen = VarInt.size(valLen);
               if (valLen > 64) {
                  throw ProtocolException.arrayTooLong("val", valLen, 64);
               }

               if (dictPos + valVarLen + valLen * 17L > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("val", dictPos + valVarLen + valLen * 17, buf.readableBytes());
               }

               dictPos += valVarLen;
               RequiredBlockFaceSupport[] val = new RequiredBlockFaceSupport[valLen];

               for (int valIdx = 0; valIdx < valLen; valIdx++) {
                  val[valIdx] = RequiredBlockFaceSupport.deserialize(buf, dictPos);
                  dictPos += RequiredBlockFaceSupport.computeBytesConsumed(buf, dictPos);
               }

               if (obj.support.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("support", key);
               }
            }
         }

         if ((nullBits[1] & 128) != 0) {
            int varPosBase7 = buf.getIntLE(offset + 204);
            if (varPosBase7 < 0 || varPosBase7 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("Supporting", varPosBase7, buf.readableBytes());
            }

            int varPos7 = offset + 276 + varPosBase7;
            int supportingCount = VarInt.peek(buf, varPos7);
            if (supportingCount < 0) {
               throw ProtocolException.invalidVarInt("Supporting");
            }

            int varIntLenxx = VarInt.size(supportingCount);
            if (supportingCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Supporting", supportingCount, 4096000);
            }

            obj.supporting = new HashMap<>(supportingCount);
            int dictPos = varPos7 + varIntLenxx;

            for (int i = 0; i < supportingCount; i++) {
               BlockNeighbor keyx = BlockNeighbor.fromValue(buf.getByte(dictPos));
               int valLenx = VarInt.peek(buf, ++dictPos);
               if (valLenx < 0) {
                  throw ProtocolException.invalidVarInt("val");
               }

               int valVarLenx = VarInt.size(valLenx);
               if (valLenx > 64) {
                  throw ProtocolException.arrayTooLong("val", valLenx, 64);
               }

               if (dictPos + valVarLenx + valLenx * 1L > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("val", dictPos + valVarLenx + valLenx * 1, buf.readableBytes());
               }

               dictPos += valVarLenx;
               BlockFaceSupport[] val = new BlockFaceSupport[valLenx];

               for (int valIdx = 0; valIdx < valLenx; valIdx++) {
                  val[valIdx] = BlockFaceSupport.deserialize(buf, dictPos);
                  dictPos += BlockFaceSupport.computeBytesConsumed(buf, dictPos);
               }

               if (obj.supporting.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("supporting", keyx);
               }
            }
         }

         if ((nullBits[2] & 1) != 0) {
            int varPosBase8 = buf.getIntLE(offset + 208);
            if (varPosBase8 < 0 || varPosBase8 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("CubeTextures", varPosBase8, buf.readableBytes());
            }

            int varPos8 = offset + 276 + varPosBase8;
            int cubeTexturesCount = VarInt.peek(buf, varPos8);
            if (cubeTexturesCount < 0) {
               throw ProtocolException.invalidVarInt("CubeTextures");
            }

            int varIntLenxx = VarInt.size(cubeTexturesCount);
            if (cubeTexturesCount > 4096000) {
               throw ProtocolException.arrayTooLong("CubeTextures", cubeTexturesCount, 4096000);
            }

            if (varPos8 + varIntLenxx + cubeTexturesCount * 5L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("CubeTextures", varPos8 + varIntLenxx + cubeTexturesCount * 5, buf.readableBytes());
            }

            obj.cubeTextures = new BlockTextures[cubeTexturesCount];
            int elemPos = varPos8 + varIntLenxx;

            for (int i = 0; i < cubeTexturesCount; i++) {
               obj.cubeTextures[i] = BlockTextures.deserialize(buf, elemPos);
               elemPos += BlockTextures.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits[2] & 2) != 0) {
            int varPosBase9 = buf.getIntLE(offset + 212);
            if (varPosBase9 < 0 || varPosBase9 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("CubeSideMaskTexture", varPosBase9, buf.readableBytes());
            }

            int varPos9 = offset + 276 + varPosBase9;
            int cubeSideMaskTextureLen = VarInt.peek(buf, varPos9);
            if (cubeSideMaskTextureLen < 0) {
               throw ProtocolException.invalidVarInt("CubeSideMaskTexture");
            }

            int cubeSideMaskTextureVarIntLen = VarInt.size(cubeSideMaskTextureLen);
            if (cubeSideMaskTextureLen > 4096000) {
               throw ProtocolException.stringTooLong("CubeSideMaskTexture", cubeSideMaskTextureLen, 4096000);
            }

            if (varPos9 + cubeSideMaskTextureVarIntLen + cubeSideMaskTextureLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall(
                  "CubeSideMaskTexture", varPos9 + cubeSideMaskTextureVarIntLen + cubeSideMaskTextureLen, buf.readableBytes()
               );
            }

            obj.cubeSideMaskTexture = PacketIO.readVarString(buf, varPos9, PacketIO.UTF8);
         }

         if ((nullBits[2] & 4) != 0) {
            int varPosBase10 = buf.getIntLE(offset + 216);
            if (varPosBase10 < 0 || varPosBase10 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("ConditionalSounds", varPosBase10, buf.readableBytes());
            }

            int varPos10 = offset + 276 + varPosBase10;
            int conditionalSoundsCount = VarInt.peek(buf, varPos10);
            if (conditionalSoundsCount < 0) {
               throw ProtocolException.invalidVarInt("ConditionalSounds");
            }

            int varIntLenxxx = VarInt.size(conditionalSoundsCount);
            if (conditionalSoundsCount > 4096000) {
               throw ProtocolException.arrayTooLong("ConditionalSounds", conditionalSoundsCount, 4096000);
            }

            if (varPos10 + varIntLenxxx + conditionalSoundsCount * 8L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ConditionalSounds", varPos10 + varIntLenxxx + conditionalSoundsCount * 8, buf.readableBytes());
            }

            obj.conditionalSounds = new ConditionalBlockSound[conditionalSoundsCount];
            int elemPos = varPos10 + varIntLenxxx;

            for (int i = 0; i < conditionalSoundsCount; i++) {
               obj.conditionalSounds[i] = ConditionalBlockSound.deserialize(buf, elemPos);
               elemPos += ConditionalBlockSound.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits[2] & 8) != 0) {
            int varPosBase11 = buf.getIntLE(offset + 220);
            if (varPosBase11 < 0 || varPosBase11 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("Particles", varPosBase11, buf.readableBytes());
            }

            int varPos11 = offset + 276 + varPosBase11;
            int particlesCount = VarInt.peek(buf, varPos11);
            if (particlesCount < 0) {
               throw ProtocolException.invalidVarInt("Particles");
            }

            int varIntLenxxxx = VarInt.size(particlesCount);
            if (particlesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Particles", particlesCount, 4096000);
            }

            if (varPos11 + varIntLenxxxx + particlesCount * 34L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Particles", varPos11 + varIntLenxxxx + particlesCount * 34, buf.readableBytes());
            }

            obj.particles = new ModelParticle[particlesCount];
            int elemPos = varPos11 + varIntLenxxxx;

            for (int i = 0; i < particlesCount; i++) {
               obj.particles[i] = ModelParticle.deserialize(buf, elemPos);
               elemPos += ModelParticle.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits[2] & 16) != 0) {
            int varPosBase12 = buf.getIntLE(offset + 224);
            if (varPosBase12 < 0 || varPosBase12 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("BlockParticleSetId", varPosBase12, buf.readableBytes());
            }

            int varPos12 = offset + 276 + varPosBase12;
            int blockParticleSetIdLen = VarInt.peek(buf, varPos12);
            if (blockParticleSetIdLen < 0) {
               throw ProtocolException.invalidVarInt("BlockParticleSetId");
            }

            int blockParticleSetIdVarIntLen = VarInt.size(blockParticleSetIdLen);
            if (blockParticleSetIdLen > 4096000) {
               throw ProtocolException.stringTooLong("BlockParticleSetId", blockParticleSetIdLen, 4096000);
            }

            if (varPos12 + blockParticleSetIdVarIntLen + blockParticleSetIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("BlockParticleSetId", varPos12 + blockParticleSetIdVarIntLen + blockParticleSetIdLen, buf.readableBytes());
            }

            obj.blockParticleSetId = PacketIO.readVarString(buf, varPos12, PacketIO.UTF8);
         }

         if ((nullBits[2] & 32) != 0) {
            int varPosBase13 = buf.getIntLE(offset + 228);
            if (varPosBase13 < 0 || varPosBase13 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("BlockBreakingDecalId", varPosBase13, buf.readableBytes());
            }

            int varPos13 = offset + 276 + varPosBase13;
            int blockBreakingDecalIdLen = VarInt.peek(buf, varPos13);
            if (blockBreakingDecalIdLen < 0) {
               throw ProtocolException.invalidVarInt("BlockBreakingDecalId");
            }

            int blockBreakingDecalIdVarIntLen = VarInt.size(blockBreakingDecalIdLen);
            if (blockBreakingDecalIdLen > 4096000) {
               throw ProtocolException.stringTooLong("BlockBreakingDecalId", blockBreakingDecalIdLen, 4096000);
            }

            if (varPos13 + blockBreakingDecalIdVarIntLen + blockBreakingDecalIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall(
                  "BlockBreakingDecalId", varPos13 + blockBreakingDecalIdVarIntLen + blockBreakingDecalIdLen, buf.readableBytes()
               );
            }

            obj.blockBreakingDecalId = PacketIO.readVarString(buf, varPos13, PacketIO.UTF8);
         }

         if ((nullBits[2] & 64) != 0) {
            int varPosBase14 = buf.getIntLE(offset + 232);
            if (varPosBase14 < 0 || varPosBase14 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("TransitionTexture", varPosBase14, buf.readableBytes());
            }

            int varPos14 = offset + 276 + varPosBase14;
            int transitionTextureLen = VarInt.peek(buf, varPos14);
            if (transitionTextureLen < 0) {
               throw ProtocolException.invalidVarInt("TransitionTexture");
            }

            int transitionTextureVarIntLen = VarInt.size(transitionTextureLen);
            if (transitionTextureLen > 4096000) {
               throw ProtocolException.stringTooLong("TransitionTexture", transitionTextureLen, 4096000);
            }

            if (varPos14 + transitionTextureVarIntLen + transitionTextureLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("TransitionTexture", varPos14 + transitionTextureVarIntLen + transitionTextureLen, buf.readableBytes());
            }

            obj.transitionTexture = PacketIO.readVarString(buf, varPos14, PacketIO.UTF8);
         }

         if ((nullBits[2] & 128) != 0) {
            int varPosBase15 = buf.getIntLE(offset + 236);
            if (varPosBase15 < 0 || varPosBase15 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("TransitionToGroups", varPosBase15, buf.readableBytes());
            }

            int varPos15 = offset + 276 + varPosBase15;
            int transitionToGroupsCount = VarInt.peek(buf, varPos15);
            if (transitionToGroupsCount < 0) {
               throw ProtocolException.invalidVarInt("TransitionToGroups");
            }

            int varIntLenxxxxx = VarInt.size(transitionToGroupsCount);
            if (transitionToGroupsCount > 4096000) {
               throw ProtocolException.arrayTooLong("TransitionToGroups", transitionToGroupsCount, 4096000);
            }

            if (varPos15 + varIntLenxxxxx + transitionToGroupsCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("TransitionToGroups", varPos15 + varIntLenxxxxx + transitionToGroupsCount * 4, buf.readableBytes());
            }

            obj.transitionToGroups = new int[transitionToGroupsCount];

            for (int i = 0; i < transitionToGroupsCount; i++) {
               obj.transitionToGroups[i] = buf.getIntLE(varPos15 + varIntLenxxxxx + i * 4);
            }
         }

         if ((nullBits[3] & 1) != 0) {
            int varPosBase16 = buf.getIntLE(offset + 240);
            if (varPosBase16 < 0 || varPosBase16 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("InteractionHint", varPosBase16, buf.readableBytes());
            }

            int varPos16 = offset + 276 + varPosBase16;
            int interactionHintLen = VarInt.peek(buf, varPos16);
            if (interactionHintLen < 0) {
               throw ProtocolException.invalidVarInt("InteractionHint");
            }

            int interactionHintVarIntLen = VarInt.size(interactionHintLen);
            if (interactionHintLen > 4096000) {
               throw ProtocolException.stringTooLong("InteractionHint", interactionHintLen, 4096000);
            }

            if (varPos16 + interactionHintVarIntLen + interactionHintLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("InteractionHint", varPos16 + interactionHintVarIntLen + interactionHintLen, buf.readableBytes());
            }

            obj.interactionHint = PacketIO.readVarString(buf, varPos16, PacketIO.UTF8);
         }

         if ((nullBits[3] & 2) != 0) {
            int varPosBase17 = buf.getIntLE(offset + 244);
            if (varPosBase17 < 0 || varPosBase17 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("Gathering", varPosBase17, buf.readableBytes());
            }

            int varPos17 = offset + 276 + varPosBase17;
            obj.gathering = BlockGathering.deserialize(buf, varPos17);
         }

         if ((nullBits[3] & 4) != 0) {
            int varPosBase18 = buf.getIntLE(offset + 248);
            if (varPosBase18 < 0 || varPosBase18 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("Display", varPosBase18, buf.readableBytes());
            }

            int varPos18 = offset + 276 + varPosBase18;
            obj.display = ModelDisplay.deserialize(buf, varPos18);
         }

         if ((nullBits[3] & 8) != 0) {
            int varPosBase19 = buf.getIntLE(offset + 252);
            if (varPosBase19 < 0 || varPosBase19 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("Rail", varPosBase19, buf.readableBytes());
            }

            int varPos19 = offset + 276 + varPosBase19;
            obj.rail = RailConfig.deserialize(buf, varPos19);
         }

         if ((nullBits[3] & 16) != 0) {
            int varPosBase20 = buf.getIntLE(offset + 256);
            if (varPosBase20 < 0 || varPosBase20 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("Interactions", varPosBase20, buf.readableBytes());
            }

            int varPos20 = offset + 276 + varPosBase20;
            int interactionsCount = VarInt.peek(buf, varPos20);
            if (interactionsCount < 0) {
               throw ProtocolException.invalidVarInt("Interactions");
            }

            int varIntLenxxxxxx = VarInt.size(interactionsCount);
            if (interactionsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Interactions", interactionsCount, 4096000);
            }

            obj.interactions = new HashMap<>(interactionsCount);
            int dictPos = varPos20 + varIntLenxxxxxx;

            for (int i = 0; i < interactionsCount; i++) {
               InteractionType keyxx = InteractionType.fromValue(buf.getByte(dictPos));
               int val = buf.getIntLE(++dictPos);
               dictPos += 4;
               if (obj.interactions.put(keyxx, val) != null) {
                  throw ProtocolException.duplicateKey("interactions", keyxx);
               }
            }
         }

         if ((nullBits[3] & 32) != 0) {
            int varPosBase21 = buf.getIntLE(offset + 260);
            if (varPosBase21 < 0 || varPosBase21 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("States", varPosBase21, buf.readableBytes());
            }

            int varPos21 = offset + 276 + varPosBase21;
            int statesCount = VarInt.peek(buf, varPos21);
            if (statesCount < 0) {
               throw ProtocolException.invalidVarInt("States");
            }

            int varIntLenxxxxxx = VarInt.size(statesCount);
            if (statesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("States", statesCount, 4096000);
            }

            obj.states = new HashMap<>(statesCount);
            int dictPos = varPos21 + varIntLenxxxxxx;

            for (int ix = 0; ix < statesCount; ix++) {
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

               String keyxx = PacketIO.readVarString(buf, dictPos);
               dictPos += keyVarLen + keyLen;
               int val = buf.getIntLE(dictPos);
               dictPos += 4;
               if (obj.states.put(keyxx, val) != null) {
                  throw ProtocolException.duplicateKey("states", keyxx);
               }
            }
         }

         if ((nullBits[3] & 64) != 0) {
            int varPosBase22 = buf.getIntLE(offset + 264);
            if (varPosBase22 < 0 || varPosBase22 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("TagIndexes", varPosBase22, buf.readableBytes());
            }

            int varPos22 = offset + 276 + varPosBase22;
            int tagIndexesCount = VarInt.peek(buf, varPos22);
            if (tagIndexesCount < 0) {
               throw ProtocolException.invalidVarInt("TagIndexes");
            }

            int varIntLenxxxxxx = VarInt.size(tagIndexesCount);
            if (tagIndexesCount > 4096000) {
               throw ProtocolException.arrayTooLong("TagIndexes", tagIndexesCount, 4096000);
            }

            if (varPos22 + varIntLenxxxxxx + tagIndexesCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("TagIndexes", varPos22 + varIntLenxxxxxx + tagIndexesCount * 4, buf.readableBytes());
            }

            obj.tagIndexes = new int[tagIndexesCount];

            for (int ix = 0; ix < tagIndexesCount; ix++) {
               obj.tagIndexes[ix] = buf.getIntLE(varPos22 + varIntLenxxxxxx + ix * 4);
            }
         }

         if ((nullBits[3] & 128) != 0) {
            int varPosBase23 = buf.getIntLE(offset + 268);
            if (varPosBase23 < 0 || varPosBase23 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("Bench", varPosBase23, buf.readableBytes());
            }

            int varPos23 = offset + 276 + varPosBase23;
            obj.bench = Bench.deserialize(buf, varPos23);
         }

         if ((nullBits[4] & 1) != 0) {
            int varPosBase24 = buf.getIntLE(offset + 272);
            if (varPosBase24 < 0 || varPosBase24 > buf.writerIndex() - offset - 276) {
               throw ProtocolException.invalidOffset("ConnectedBlockRuleSet", varPosBase24, buf.readableBytes());
            }

            int varPos24 = offset + 276 + varPosBase24;
            obj.connectedBlockRuleSet = ConnectedBlockRuleSet.deserialize(buf, varPos24);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 5);
      int maxEnd = 276;
      if ((nullBits[1] & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 176);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("Item", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 276 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[1] & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 180);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("Name", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 276 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[1] & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 184);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("ShaderEffect", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 276 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen) + arrLen * 1;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[1] & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 188);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("Model", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 276 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(sl) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[1] & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 192);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("ModelTexture", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 276 + fieldOffset4;
         int arrLen = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos4 += ModelTexture.computeBytesConsumed(buf, pos4);
         }

         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[1] & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 196);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("ModelAnimation", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 276 + fieldOffset5;
         int sl = VarInt.peek(buf, pos5);
         pos5 += VarInt.size(sl) + sl;
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[1] & 64) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 200);
         if (fieldOffset6 < 0 || fieldOffset6 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("Support", fieldOffset6, maxEnd);
         }

         int pos6 = offset + 276 + fieldOffset6;
         int dictLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int al = VarInt.peek(buf, ++pos6);
            pos6 += VarInt.size(al);

            for (int j = 0; j < al; j++) {
               pos6 += RequiredBlockFaceSupport.computeBytesConsumed(buf, pos6);
            }
         }

         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[1] & 128) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 204);
         if (fieldOffset7 < 0 || fieldOffset7 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("Supporting", fieldOffset7, maxEnd);
         }

         int pos7 = offset + 276 + fieldOffset7;
         int dictLen = VarInt.peek(buf, pos7);
         pos7 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int al = VarInt.peek(buf, ++pos7);
            pos7 += VarInt.size(al);

            for (int j = 0; j < al; j++) {
               pos7 += BlockFaceSupport.computeBytesConsumed(buf, pos7);
            }
         }

         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      if ((nullBits[2] & 1) != 0) {
         int fieldOffset8 = buf.getIntLE(offset + 208);
         if (fieldOffset8 < 0 || fieldOffset8 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("CubeTextures", fieldOffset8, maxEnd);
         }

         int pos8 = offset + 276 + fieldOffset8;
         int arrLen = VarInt.peek(buf, pos8);
         pos8 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos8 += BlockTextures.computeBytesConsumed(buf, pos8);
         }

         if (pos8 - offset > maxEnd) {
            maxEnd = pos8 - offset;
         }
      }

      if ((nullBits[2] & 2) != 0) {
         int fieldOffset9 = buf.getIntLE(offset + 212);
         if (fieldOffset9 < 0 || fieldOffset9 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("CubeSideMaskTexture", fieldOffset9, maxEnd);
         }

         int pos9 = offset + 276 + fieldOffset9;
         int sl = VarInt.peek(buf, pos9);
         pos9 += VarInt.size(sl) + sl;
         if (pos9 - offset > maxEnd) {
            maxEnd = pos9 - offset;
         }
      }

      if ((nullBits[2] & 4) != 0) {
         int fieldOffset10 = buf.getIntLE(offset + 216);
         if (fieldOffset10 < 0 || fieldOffset10 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("ConditionalSounds", fieldOffset10, maxEnd);
         }

         int pos10 = offset + 276 + fieldOffset10;
         int arrLen = VarInt.peek(buf, pos10);
         pos10 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos10 += ConditionalBlockSound.computeBytesConsumed(buf, pos10);
         }

         if (pos10 - offset > maxEnd) {
            maxEnd = pos10 - offset;
         }
      }

      if ((nullBits[2] & 8) != 0) {
         int fieldOffset11 = buf.getIntLE(offset + 220);
         if (fieldOffset11 < 0 || fieldOffset11 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("Particles", fieldOffset11, maxEnd);
         }

         int pos11 = offset + 276 + fieldOffset11;
         int arrLen = VarInt.peek(buf, pos11);
         pos11 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos11 += ModelParticle.computeBytesConsumed(buf, pos11);
         }

         if (pos11 - offset > maxEnd) {
            maxEnd = pos11 - offset;
         }
      }

      if ((nullBits[2] & 16) != 0) {
         int fieldOffset12 = buf.getIntLE(offset + 224);
         if (fieldOffset12 < 0 || fieldOffset12 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("BlockParticleSetId", fieldOffset12, maxEnd);
         }

         int pos12 = offset + 276 + fieldOffset12;
         int sl = VarInt.peek(buf, pos12);
         pos12 += VarInt.size(sl) + sl;
         if (pos12 - offset > maxEnd) {
            maxEnd = pos12 - offset;
         }
      }

      if ((nullBits[2] & 32) != 0) {
         int fieldOffset13 = buf.getIntLE(offset + 228);
         if (fieldOffset13 < 0 || fieldOffset13 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("BlockBreakingDecalId", fieldOffset13, maxEnd);
         }

         int pos13 = offset + 276 + fieldOffset13;
         int sl = VarInt.peek(buf, pos13);
         pos13 += VarInt.size(sl) + sl;
         if (pos13 - offset > maxEnd) {
            maxEnd = pos13 - offset;
         }
      }

      if ((nullBits[2] & 64) != 0) {
         int fieldOffset14 = buf.getIntLE(offset + 232);
         if (fieldOffset14 < 0 || fieldOffset14 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("TransitionTexture", fieldOffset14, maxEnd);
         }

         int pos14 = offset + 276 + fieldOffset14;
         int sl = VarInt.peek(buf, pos14);
         pos14 += VarInt.size(sl) + sl;
         if (pos14 - offset > maxEnd) {
            maxEnd = pos14 - offset;
         }
      }

      if ((nullBits[2] & 128) != 0) {
         int fieldOffset15 = buf.getIntLE(offset + 236);
         if (fieldOffset15 < 0 || fieldOffset15 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("TransitionToGroups", fieldOffset15, maxEnd);
         }

         int pos15 = offset + 276 + fieldOffset15;
         int arrLen = VarInt.peek(buf, pos15);
         pos15 += VarInt.size(arrLen) + arrLen * 4;
         if (pos15 - offset > maxEnd) {
            maxEnd = pos15 - offset;
         }
      }

      if ((nullBits[3] & 1) != 0) {
         int fieldOffset16 = buf.getIntLE(offset + 240);
         if (fieldOffset16 < 0 || fieldOffset16 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("InteractionHint", fieldOffset16, maxEnd);
         }

         int pos16 = offset + 276 + fieldOffset16;
         int sl = VarInt.peek(buf, pos16);
         pos16 += VarInt.size(sl) + sl;
         if (pos16 - offset > maxEnd) {
            maxEnd = pos16 - offset;
         }
      }

      if ((nullBits[3] & 2) != 0) {
         int fieldOffset17 = buf.getIntLE(offset + 244);
         if (fieldOffset17 < 0 || fieldOffset17 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("Gathering", fieldOffset17, maxEnd);
         }

         int pos17 = offset + 276 + fieldOffset17;
         pos17 += BlockGathering.computeBytesConsumed(buf, pos17);
         if (pos17 - offset > maxEnd) {
            maxEnd = pos17 - offset;
         }
      }

      if ((nullBits[3] & 4) != 0) {
         int fieldOffset18 = buf.getIntLE(offset + 248);
         if (fieldOffset18 < 0 || fieldOffset18 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("Display", fieldOffset18, maxEnd);
         }

         int pos18 = offset + 276 + fieldOffset18;
         pos18 += ModelDisplay.computeBytesConsumed(buf, pos18);
         if (pos18 - offset > maxEnd) {
            maxEnd = pos18 - offset;
         }
      }

      if ((nullBits[3] & 8) != 0) {
         int fieldOffset19 = buf.getIntLE(offset + 252);
         if (fieldOffset19 < 0 || fieldOffset19 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("Rail", fieldOffset19, maxEnd);
         }

         int pos19 = offset + 276 + fieldOffset19;
         pos19 += RailConfig.computeBytesConsumed(buf, pos19);
         if (pos19 - offset > maxEnd) {
            maxEnd = pos19 - offset;
         }
      }

      if ((nullBits[3] & 16) != 0) {
         int fieldOffset20 = buf.getIntLE(offset + 256);
         if (fieldOffset20 < 0 || fieldOffset20 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("Interactions", fieldOffset20, maxEnd);
         }

         int pos20 = offset + 276 + fieldOffset20;
         int dictLen = VarInt.peek(buf, pos20);
         pos20 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos20 = ++pos20 + 4;
         }

         if (pos20 - offset > maxEnd) {
            maxEnd = pos20 - offset;
         }
      }

      if ((nullBits[3] & 32) != 0) {
         int fieldOffset21 = buf.getIntLE(offset + 260);
         if (fieldOffset21 < 0 || fieldOffset21 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("States", fieldOffset21, maxEnd);
         }

         int pos21 = offset + 276 + fieldOffset21;
         int dictLen = VarInt.peek(buf, pos21);
         pos21 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos21);
            pos21 += VarInt.size(sl) + sl;
            pos21 += 4;
         }

         if (pos21 - offset > maxEnd) {
            maxEnd = pos21 - offset;
         }
      }

      if ((nullBits[3] & 64) != 0) {
         int fieldOffset22 = buf.getIntLE(offset + 264);
         if (fieldOffset22 < 0 || fieldOffset22 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("TagIndexes", fieldOffset22, maxEnd);
         }

         int pos22 = offset + 276 + fieldOffset22;
         int arrLen = VarInt.peek(buf, pos22);
         pos22 += VarInt.size(arrLen) + arrLen * 4;
         if (pos22 - offset > maxEnd) {
            maxEnd = pos22 - offset;
         }
      }

      if ((nullBits[3] & 128) != 0) {
         int fieldOffset23 = buf.getIntLE(offset + 268);
         if (fieldOffset23 < 0 || fieldOffset23 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("Bench", fieldOffset23, maxEnd);
         }

         int pos23 = offset + 276 + fieldOffset23;
         pos23 += Bench.computeBytesConsumed(buf, pos23);
         if (pos23 - offset > maxEnd) {
            maxEnd = pos23 - offset;
         }
      }

      if ((nullBits[4] & 1) != 0) {
         int fieldOffset24 = buf.getIntLE(offset + 272);
         if (fieldOffset24 < 0 || fieldOffset24 > buf.writerIndex() - offset - 276) {
            throw ProtocolException.invalidOffset("ConnectedBlockRuleSet", fieldOffset24, maxEnd);
         }

         int pos24 = offset + 276 + fieldOffset24;
         pos24 += ConnectedBlockRuleSet.computeBytesConsumed(buf, pos24);
         if (pos24 - offset > maxEnd) {
            maxEnd = pos24 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 276L;
   }

   @Nullable
   public static String getItem(MemorySegment mem) {
      return getItem(mem, 0);
   }

   @Nullable
   public static String getItem(MemorySegment mem, int offset) {
      return hasItem(mem, offset)
         ? PacketIO.readVarString("Item", mem, offset + getValidatedOffset(mem, offset, 176, 276, "Item"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   @Nullable
   public static String getName(MemorySegment mem, int offset) {
      return hasName(mem, offset)
         ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 180, 276, "Name"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean getUnknown(MemorySegment mem) {
      return getUnknown(mem, 0);
   }

   public static boolean getUnknown(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5));
   }

   public static DrawType getDrawType(MemorySegment mem) {
      return getDrawType(mem, 0);
   }

   public static DrawType getDrawType(MemorySegment mem, int offset) {
      return DrawType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 6)));
   }

   public static BlockMaterial getMaterial(MemorySegment mem) {
      return getMaterial(mem, 0);
   }

   public static BlockMaterial getMaterial(MemorySegment mem, int offset) {
      return BlockMaterial.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 7)));
   }

   public static Opacity getOpacity(MemorySegment mem) {
      return getOpacity(mem, 0);
   }

   public static Opacity getOpacity(MemorySegment mem, int offset) {
      return Opacity.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 8)));
   }

   @Nullable
   public static ShaderType[] getShaderEffect(MemorySegment mem) {
      return getShaderEffect(mem, 0);
   }

   @Nullable
   public static ShaderType[] getShaderEffect(MemorySegment mem, int offset) {
      if (!hasShaderEffect(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 184, 276, "ShaderEffect");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ShaderEffect", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ShaderEffect", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ShaderEffect", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ShaderType[] data = new ShaderType[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ShaderType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   public static int getHitbox(MemorySegment mem) {
      return getHitbox(mem, 0);
   }

   public static int getHitbox(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 9));
   }

   public static int getInteractionHitbox(MemorySegment mem) {
      return getInteractionHitbox(mem, 0);
   }

   public static int getInteractionHitbox(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 13));
   }

   @Nullable
   public static String getModel(MemorySegment mem) {
      return getModel(mem, 0);
   }

   @Nullable
   public static String getModel(MemorySegment mem, int offset) {
      return hasModel(mem, offset)
         ? PacketIO.readVarString("Model", mem, offset + getValidatedOffset(mem, offset, 188, 276, "Model"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static ModelTexture[] getModelTexture(MemorySegment mem) {
      return getModelTexture(mem, 0);
   }

   @Nullable
   public static ModelTexture[] getModelTexture(MemorySegment mem, int offset) {
      if (!hasModelTexture(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 192, 276, "ModelTexture");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ModelTexture", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ModelTexture", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ModelTexture", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ModelTexture[] data = new ModelTexture[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ModelTexture.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static float getModelScale(MemorySegment mem) {
      return getModelScale(mem, 0);
   }

   public static float getModelScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 17));
   }

   @Nullable
   public static String getModelAnimation(MemorySegment mem) {
      return getModelAnimation(mem, 0);
   }

   @Nullable
   public static String getModelAnimation(MemorySegment mem, int offset) {
      return hasModelAnimation(mem, offset)
         ? PacketIO.readVarString("ModelAnimation", mem, offset + getValidatedOffset(mem, offset, 196, 276, "ModelAnimation"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean getLooping(MemorySegment mem) {
      return getLooping(mem, 0);
   }

   public static boolean getLooping(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 21));
   }

   public static int getMaxSupportDistance(MemorySegment mem) {
      return getMaxSupportDistance(mem, 0);
   }

   public static int getMaxSupportDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 22));
   }

   public static BlockSupportsRequiredForType getBlockSupportsRequiredFor(MemorySegment mem) {
      return getBlockSupportsRequiredFor(mem, 0);
   }

   public static BlockSupportsRequiredForType getBlockSupportsRequiredFor(MemorySegment mem, int offset) {
      return BlockSupportsRequiredForType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 26)));
   }

   @Nullable
   public static Map<BlockNeighbor, RequiredBlockFaceSupport[]> getSupport(MemorySegment mem) {
      return getSupport(mem, 0);
   }

   @Nullable
   public static Map<BlockNeighbor, RequiredBlockFaceSupport[]> getSupport(MemorySegment mem, int offset) {
      if (!hasSupport(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 200, 276, "Support");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Support", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Support", len, 4096000);
         } else {
            Map<BlockNeighbor, RequiredBlockFaceSupport[]> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               BlockNeighbor key = BlockNeighbor.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               long valuePacked = VarInt.getWithLength(mem, ++off);
               int valueLen = (int)valuePacked;
               int valueVarLen = (int)(valuePacked >>> 32);
               if (valueLen < 0) {
                  throw ProtocolException.negativeLength("value", valueLen);
               }

               if (valueLen > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLen, 64);
               }

               if (off + valueVarLen + valueLen * 17L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", off + valueVarLen + valueLen * 17, (int)mem.byteSize());
               }

               off += valueVarLen;
               RequiredBlockFaceSupport[] value = new RequiredBlockFaceSupport[valueLen];

               for (int valueIdx = 0; valueIdx < valueLen; valueIdx++) {
                  value[valueIdx] = RequiredBlockFaceSupport.toObject(mem, off);
                  off += value[valueIdx].computeSize();
               }

               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Support", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<BlockNeighbor, BlockFaceSupport[]> getSupporting(MemorySegment mem) {
      return getSupporting(mem, 0);
   }

   @Nullable
   public static Map<BlockNeighbor, BlockFaceSupport[]> getSupporting(MemorySegment mem, int offset) {
      if (!hasSupporting(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 204, 276, "Supporting");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Supporting", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Supporting", len, 4096000);
         } else {
            Map<BlockNeighbor, BlockFaceSupport[]> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               BlockNeighbor key = BlockNeighbor.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               long valuePacked = VarInt.getWithLength(mem, ++off);
               int valueLen = (int)valuePacked;
               int valueVarLen = (int)(valuePacked >>> 32);
               if (valueLen < 0) {
                  throw ProtocolException.negativeLength("value", valueLen);
               }

               if (valueLen > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLen, 64);
               }

               if (off + valueVarLen + valueLen * 1L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", off + valueVarLen + valueLen * 1, (int)mem.byteSize());
               }

               off += valueVarLen;
               BlockFaceSupport[] value = new BlockFaceSupport[valueLen];

               for (int valueIdx = 0; valueIdx < valueLen; valueIdx++) {
                  value[valueIdx] = BlockFaceSupport.toObject(mem, off);
                  off += value[valueIdx].computeSize();
               }

               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Supporting", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean getRequiresAlphaBlending(MemorySegment mem) {
      return getRequiresAlphaBlending(mem, 0);
   }

   public static boolean getRequiresAlphaBlending(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 27));
   }

   @Nullable
   public static BlockTextures[] getCubeTextures(MemorySegment mem) {
      return getCubeTextures(mem, 0);
   }

   @Nullable
   public static BlockTextures[] getCubeTextures(MemorySegment mem, int offset) {
      if (!hasCubeTextures(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 208, 276, "CubeTextures");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("CubeTextures", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("CubeTextures", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("CubeTextures", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               BlockTextures[] data = new BlockTextures[len];

               for (int i = 0; i < len; i++) {
                  data[i] = BlockTextures.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static String getCubeSideMaskTexture(MemorySegment mem) {
      return getCubeSideMaskTexture(mem, 0);
   }

   @Nullable
   public static String getCubeSideMaskTexture(MemorySegment mem, int offset) {
      return hasCubeSideMaskTexture(mem, offset)
         ? PacketIO.readVarString("CubeSideMaskTexture", mem, offset + getValidatedOffset(mem, offset, 212, 276, "CubeSideMaskTexture"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static ShadingMode getCubeShadingMode(MemorySegment mem) {
      return getCubeShadingMode(mem, 0);
   }

   public static ShadingMode getCubeShadingMode(MemorySegment mem, int offset) {
      return ShadingMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 28)));
   }

   public static RandomRotation getRandomRotation(MemorySegment mem) {
      return getRandomRotation(mem, 0);
   }

   public static RandomRotation getRandomRotation(MemorySegment mem, int offset) {
      return RandomRotation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 29)));
   }

   public static VariantRotation getVariantRotation(MemorySegment mem) {
      return getVariantRotation(mem, 0);
   }

   public static VariantRotation getVariantRotation(MemorySegment mem, int offset) {
      return VariantRotation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 30)));
   }

   public static Rotation getRotationYawPlacementOffset(MemorySegment mem) {
      return getRotationYawPlacementOffset(mem, 0);
   }

   public static Rotation getRotationYawPlacementOffset(MemorySegment mem, int offset) {
      return Rotation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 31)));
   }

   public static int getBlockSoundSetIndex(MemorySegment mem) {
      return getBlockSoundSetIndex(mem, 0);
   }

   public static int getBlockSoundSetIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 32));
   }

   public static int getPhysicalMaterialIndex(MemorySegment mem) {
      return getPhysicalMaterialIndex(mem, 0);
   }

   public static int getPhysicalMaterialIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 36));
   }

   public static float getSoundOcclusionOpacity(MemorySegment mem) {
      return getSoundOcclusionOpacity(mem, 0);
   }

   public static float getSoundOcclusionOpacity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 40));
   }

   public static int getAmbientSoundEventIndex(MemorySegment mem) {
      return getAmbientSoundEventIndex(mem, 0);
   }

   public static int getAmbientSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 44));
   }

   @Nullable
   public static ConditionalBlockSound[] getConditionalSounds(MemorySegment mem) {
      return getConditionalSounds(mem, 0);
   }

   @Nullable
   public static ConditionalBlockSound[] getConditionalSounds(MemorySegment mem, int offset) {
      if (!hasConditionalSounds(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 216, 276, "ConditionalSounds");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ConditionalSounds", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ConditionalSounds", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 8L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ConditionalSounds", off + lenOffset + len * 8, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ConditionalBlockSound[] data = new ConditionalBlockSound[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ConditionalBlockSound.toObject(mem, off + i * 8);
               }

               return data;
            }
         }
      }
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
         int off = offset + getValidatedOffset(mem, offset, 220, 276, "Particles");
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
   public static String getBlockParticleSetId(MemorySegment mem) {
      return getBlockParticleSetId(mem, 0);
   }

   @Nullable
   public static String getBlockParticleSetId(MemorySegment mem, int offset) {
      return hasBlockParticleSetId(mem, offset)
         ? PacketIO.readVarString("BlockParticleSetId", mem, offset + getValidatedOffset(mem, offset, 224, 276, "BlockParticleSetId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getBlockBreakingDecalId(MemorySegment mem) {
      return getBlockBreakingDecalId(mem, 0);
   }

   @Nullable
   public static String getBlockBreakingDecalId(MemorySegment mem, int offset) {
      return hasBlockBreakingDecalId(mem, offset)
         ? PacketIO.readVarString(
            "BlockBreakingDecalId", mem, offset + getValidatedOffset(mem, offset, 228, 276, "BlockBreakingDecalId"), 4096000, PacketIO.UTF8
         )
         : null;
   }

   @Nullable
   public static Color getParticleColor(MemorySegment mem) {
      return getParticleColor(mem, 0);
   }

   @Nullable
   public static Color getParticleColor(MemorySegment mem, int offset) {
      return hasParticleColor(mem, offset) ? Color.toObject(mem, offset + 48) : null;
   }

   @Nullable
   public static Color getTextureComputedColor(MemorySegment mem) {
      return getTextureComputedColor(mem, 0);
   }

   @Nullable
   public static Color getTextureComputedColor(MemorySegment mem, int offset) {
      return hasTextureComputedColor(mem, offset) ? Color.toObject(mem, offset + 51) : null;
   }

   @Nullable
   public static ColorLight getLight(MemorySegment mem) {
      return getLight(mem, 0);
   }

   @Nullable
   public static ColorLight getLight(MemorySegment mem, int offset) {
      return hasLight(mem, offset) ? ColorLight.toObject(mem, offset + 54) : null;
   }

   @Nullable
   public static Tint getTint(MemorySegment mem) {
      return getTint(mem, 0);
   }

   @Nullable
   public static Tint getTint(MemorySegment mem, int offset) {
      return hasTint(mem, offset) ? Tint.toObject(mem, offset + 58) : null;
   }

   @Nullable
   public static Tint getBiomeTint(MemorySegment mem) {
      return getBiomeTint(mem, 0);
   }

   @Nullable
   public static Tint getBiomeTint(MemorySegment mem, int offset) {
      return hasBiomeTint(mem, offset) ? Tint.toObject(mem, offset + 82) : null;
   }

   public static int getGroup(MemorySegment mem) {
      return getGroup(mem, 0);
   }

   public static int getGroup(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 106));
   }

   @Nullable
   public static String getTransitionTexture(MemorySegment mem) {
      return getTransitionTexture(mem, 0);
   }

   @Nullable
   public static String getTransitionTexture(MemorySegment mem, int offset) {
      return hasTransitionTexture(mem, offset)
         ? PacketIO.readVarString("TransitionTexture", mem, offset + getValidatedOffset(mem, offset, 232, 276, "TransitionTexture"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static int[] getTransitionToGroups(MemorySegment mem) {
      return getTransitionToGroups(mem, 0);
   }

   @Nullable
   public static int[] getTransitionToGroups(MemorySegment mem, int offset) {
      if (!hasTransitionToGroups(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 236, 276, "TransitionToGroups");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("TransitionToGroups", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("TransitionToGroups", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("TransitionToGroups", off + lenOffset + len * 4, (int)mem.byteSize());
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
   public static BlockMovementSettings getMovementSettings(MemorySegment mem) {
      return getMovementSettings(mem, 0);
   }

   @Nullable
   public static BlockMovementSettings getMovementSettings(MemorySegment mem, int offset) {
      return hasMovementSettings(mem, offset) ? BlockMovementSettings.toObject(mem, offset + 110) : null;
   }

   @Nullable
   public static BlockFlags getFlags(MemorySegment mem) {
      return getFlags(mem, 0);
   }

   @Nullable
   public static BlockFlags getFlags(MemorySegment mem, int offset) {
      return hasFlags(mem, offset) ? BlockFlags.toObject(mem, offset + 152) : null;
   }

   @Nullable
   public static String getInteractionHint(MemorySegment mem) {
      return getInteractionHint(mem, 0);
   }

   @Nullable
   public static String getInteractionHint(MemorySegment mem, int offset) {
      return hasInteractionHint(mem, offset)
         ? PacketIO.readVarString("InteractionHint", mem, offset + getValidatedOffset(mem, offset, 240, 276, "InteractionHint"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static BlockGathering getGathering(MemorySegment mem) {
      return getGathering(mem, 0);
   }

   @Nullable
   public static BlockGathering getGathering(MemorySegment mem, int offset) {
      return hasGathering(mem, offset) ? BlockGathering.toObject(mem, offset + getValidatedOffset(mem, offset, 244, 276, "Gathering")) : null;
   }

   @Nullable
   public static BlockPlacementSettings getPlacementSettings(MemorySegment mem) {
      return getPlacementSettings(mem, 0);
   }

   @Nullable
   public static BlockPlacementSettings getPlacementSettings(MemorySegment mem, int offset) {
      return hasPlacementSettings(mem, offset) ? BlockPlacementSettings.toObject(mem, offset + 154) : null;
   }

   @Nullable
   public static ModelDisplay getDisplay(MemorySegment mem) {
      return getDisplay(mem, 0);
   }

   @Nullable
   public static ModelDisplay getDisplay(MemorySegment mem, int offset) {
      return hasDisplay(mem, offset) ? ModelDisplay.toObject(mem, offset + getValidatedOffset(mem, offset, 248, 276, "Display")) : null;
   }

   @Nullable
   public static RailConfig getRail(MemorySegment mem) {
      return getRail(mem, 0);
   }

   @Nullable
   public static RailConfig getRail(MemorySegment mem, int offset) {
      return hasRail(mem, offset) ? RailConfig.toObject(mem, offset + getValidatedOffset(mem, offset, 252, 276, "Rail")) : null;
   }

   public static boolean getIgnoreSupportWhenPlaced(MemorySegment mem) {
      return getIgnoreSupportWhenPlaced(mem, 0);
   }

   public static boolean getIgnoreSupportWhenPlaced(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 171));
   }

   @Nullable
   public static Map<InteractionType, Integer> getInteractions(MemorySegment mem) {
      return getInteractions(mem, 0);
   }

   @Nullable
   public static Map<InteractionType, Integer> getInteractions(MemorySegment mem, int offset) {
      if (!hasInteractions(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 256, 276, "Interactions");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Interactions", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", len, 4096000);
         } else {
            Map<InteractionType, Integer> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               InteractionType key = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++off));
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Interactions", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<String, Integer> getStates(MemorySegment mem) {
      return getStates(mem, 0);
   }

   @Nullable
   public static Map<String, Integer> getStates(MemorySegment mem, int offset) {
      if (!hasStates(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 260, 276, "States");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("States", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("States", len, 4096000);
         } else {
            Map<String, Integer> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               int value = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("States", key);
               }
            }

            return data;
         }
      }
   }

   public static int getTransitionToTag(MemorySegment mem) {
      return getTransitionToTag(mem, 0);
   }

   public static int getTransitionToTag(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 172));
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
         int off = offset + getValidatedOffset(mem, offset, 264, 276, "TagIndexes");
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
   public static Bench getBench(MemorySegment mem) {
      return getBench(mem, 0);
   }

   @Nullable
   public static Bench getBench(MemorySegment mem, int offset) {
      return hasBench(mem, offset) ? Bench.toObject(mem, offset + getValidatedOffset(mem, offset, 268, 276, "Bench")) : null;
   }

   @Nullable
   public static ConnectedBlockRuleSet getConnectedBlockRuleSet(MemorySegment mem) {
      return getConnectedBlockRuleSet(mem, 0);
   }

   @Nullable
   public static ConnectedBlockRuleSet getConnectedBlockRuleSet(MemorySegment mem, int offset) {
      return hasConnectedBlockRuleSet(mem, offset)
         ? ConnectedBlockRuleSet.toObject(mem, offset + getValidatedOffset(mem, offset, 272, 276, "ConnectedBlockRuleSet"))
         : null;
   }

   public static boolean hasParticleColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasTextureComputedColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasLight(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasTint(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasBiomeTint(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasMovementSettings(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasFlags(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasPlacementSettings(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static boolean hasItem(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 1) != 0;
   }

   public static boolean hasName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 2) != 0;
   }

   public static boolean hasShaderEffect(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 4) != 0;
   }

   public static boolean hasModel(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 8) != 0;
   }

   public static boolean hasModelTexture(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 16) != 0;
   }

   public static boolean hasModelAnimation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 32) != 0;
   }

   public static boolean hasSupport(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 64) != 0;
   }

   public static boolean hasSupporting(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 128) != 0;
   }

   public static boolean hasCubeTextures(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 1) != 0;
   }

   public static boolean hasCubeSideMaskTexture(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 2) != 0;
   }

   public static boolean hasConditionalSounds(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 4) != 0;
   }

   public static boolean hasParticles(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 8) != 0;
   }

   public static boolean hasBlockParticleSetId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 16) != 0;
   }

   public static boolean hasBlockBreakingDecalId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 32) != 0;
   }

   public static boolean hasTransitionTexture(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 64) != 0;
   }

   public static boolean hasTransitionToGroups(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
      return (b & 128) != 0;
   }

   public static boolean hasInteractionHint(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3));
      return (b & 1) != 0;
   }

   public static boolean hasGathering(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3));
      return (b & 2) != 0;
   }

   public static boolean hasDisplay(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3));
      return (b & 4) != 0;
   }

   public static boolean hasRail(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3));
      return (b & 8) != 0;
   }

   public static boolean hasInteractions(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3));
      return (b & 16) != 0;
   }

   public static boolean hasStates(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3));
      return (b & 32) != 0;
   }

   public static boolean hasTagIndexes(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3));
      return (b & 64) != 0;
   }

   public static boolean hasBench(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3));
      return (b & 128) != 0;
   }

   public static boolean hasConnectedBlockRuleSet(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4));
      return (b & 1) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static BlockType toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockType toObject(MemorySegment mem, int offset) {
      if (offset + 276 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockType", offset + 276, (int)mem.byteSize());
      } else {
         ShaderType[] shaderEffect = null;
         if (hasShaderEffect(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 184, 276, "ShaderEffect");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ShaderEffect", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("ShaderEffect", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ShaderEffect", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            shaderEffect = new ShaderType[len];

            for (int i = 0; i < len; i++) {
               shaderEffect[i] = ShaderType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
            }
         }

         ModelTexture[] modelTexture = null;
         if (hasModelTexture(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 192, 276, "ModelTexture");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("ModelTexture", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("ModelTexture", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ModelTexture", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            modelTexture = new ModelTexture[lenx];

            for (int i = 0; i < lenx; i++) {
               modelTexture[i] = ModelTexture.toObject(mem, offx);
               offx += modelTexture[i].computeSize();
            }
         }

         Map<BlockNeighbor, RequiredBlockFaceSupport[]> support = null;
         if (hasSupport(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 200, 276, "Support");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("Support", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Support", lenxx, 4096000);
            }

            support = new HashMap<>(lenxx);
            offxx += (int)(packedxx >>> 32);

            for (int i = 0; i < lenxx; i++) {
               BlockNeighbor key = BlockNeighbor.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)offxx));
               long valuePacked = VarInt.getWithLength(mem, ++offxx);
               int valueLen = (int)valuePacked;
               int valueVarLen = (int)(valuePacked >>> 32);
               if (valueLen < 0) {
                  throw ProtocolException.negativeLength("value", valueLen);
               }

               if (valueLen > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLen, 64);
               }

               if (offxx + valueVarLen + valueLen * 17L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", offxx + valueVarLen + valueLen * 17, (int)mem.byteSize());
               }

               offxx += valueVarLen;
               RequiredBlockFaceSupport[] value = new RequiredBlockFaceSupport[valueLen];

               for (int valueIdx = 0; valueIdx < valueLen; valueIdx++) {
                  value[valueIdx] = RequiredBlockFaceSupport.toObject(mem, offxx);
                  offxx += value[valueIdx].computeSize();
               }

               if (support.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Support", key);
               }
            }
         }

         Map<BlockNeighbor, BlockFaceSupport[]> supporting = null;
         if (hasSupporting(mem, offset)) {
            int offxxx = offset + getValidatedOffset(mem, offset, 204, 276, "Supporting");
            long packedxxx = VarInt.getWithLength(mem, offxxx);
            int lenxxx = (int)packedxxx;
            if (lenxxx < 0) {
               throw ProtocolException.negativeLength("Supporting", lenxxx);
            }

            if (lenxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Supporting", lenxxx, 4096000);
            }

            supporting = new HashMap<>(lenxxx);
            offxxx += (int)(packedxxx >>> 32);

            for (int i = 0; i < lenxxx; i++) {
               BlockNeighbor keyx = BlockNeighbor.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)offxxx));
               long valuePackedx = VarInt.getWithLength(mem, ++offxxx);
               int valueLenx = (int)valuePackedx;
               int valueVarLenx = (int)(valuePackedx >>> 32);
               if (valueLenx < 0) {
                  throw ProtocolException.negativeLength("value", valueLenx);
               }

               if (valueLenx > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLenx, 64);
               }

               if (offxxx + valueVarLenx + valueLenx * 1L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", offxxx + valueVarLenx + valueLenx * 1, (int)mem.byteSize());
               }

               offxxx += valueVarLenx;
               BlockFaceSupport[] value = new BlockFaceSupport[valueLenx];

               for (int valueIdx = 0; valueIdx < valueLenx; valueIdx++) {
                  value[valueIdx] = BlockFaceSupport.toObject(mem, offxxx);
                  offxxx += value[valueIdx].computeSize();
               }

               if (supporting.put(keyx, value) != null) {
                  throw ProtocolException.duplicateKey("Supporting", keyx);
               }
            }
         }

         BlockTextures[] cubeTextures = null;
         if (hasCubeTextures(mem, offset)) {
            int offxxxx = offset + getValidatedOffset(mem, offset, 208, 276, "CubeTextures");
            long packedxxxx = VarInt.getWithLength(mem, offxxxx);
            int lenxxxx = (int)packedxxxx;
            if (lenxxxx < 0) {
               throw ProtocolException.negativeLength("CubeTextures", lenxxxx);
            }

            if (lenxxxx > 4096000) {
               throw ProtocolException.arrayTooLong("CubeTextures", lenxxxx, 4096000);
            }

            int lenOffset = (int)(packedxxxx >>> 32);
            if (offxxxx + lenOffset + lenxxxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("CubeTextures", offxxxx + lenOffset + lenxxxx, (int)mem.byteSize());
            }

            offxxxx += lenOffset;
            cubeTextures = new BlockTextures[lenxxxx];

            for (int i = 0; i < lenxxxx; i++) {
               cubeTextures[i] = BlockTextures.toObject(mem, offxxxx);
               offxxxx += cubeTextures[i].computeSize();
            }
         }

         ConditionalBlockSound[] conditionalSounds = null;
         if (hasConditionalSounds(mem, offset)) {
            int offxxxxx = offset + getValidatedOffset(mem, offset, 216, 276, "ConditionalSounds");
            long packedxxxxx = VarInt.getWithLength(mem, offxxxxx);
            int lenxxxxx = (int)packedxxxxx;
            if (lenxxxxx < 0) {
               throw ProtocolException.negativeLength("ConditionalSounds", lenxxxxx);
            }

            if (lenxxxxx > 4096000) {
               throw ProtocolException.arrayTooLong("ConditionalSounds", lenxxxxx, 4096000);
            }

            int lenOffset = (int)(packedxxxxx >>> 32);
            if (offxxxxx + lenOffset + lenxxxxx * 8L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ConditionalSounds", offxxxxx + lenOffset + lenxxxxx * 8, (int)mem.byteSize());
            }

            offxxxxx += lenOffset;
            conditionalSounds = new ConditionalBlockSound[lenxxxxx];

            for (int i = 0; i < lenxxxxx; i++) {
               conditionalSounds[i] = ConditionalBlockSound.toObject(mem, offxxxxx + i * 8);
            }
         }

         ModelParticle[] particles = null;
         if (hasParticles(mem, offset)) {
            int offxxxxxx = offset + getValidatedOffset(mem, offset, 220, 276, "Particles");
            long packedxxxxxx = VarInt.getWithLength(mem, offxxxxxx);
            int lenxxxxxx = (int)packedxxxxxx;
            if (lenxxxxxx < 0) {
               throw ProtocolException.negativeLength("Particles", lenxxxxxx);
            }

            if (lenxxxxxx > 4096000) {
               throw ProtocolException.arrayTooLong("Particles", lenxxxxxx, 4096000);
            }

            int lenOffset = (int)(packedxxxxxx >>> 32);
            if (offxxxxxx + lenOffset + lenxxxxxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Particles", offxxxxxx + lenOffset + lenxxxxxx, (int)mem.byteSize());
            }

            offxxxxxx += lenOffset;
            particles = new ModelParticle[lenxxxxxx];

            for (int i = 0; i < lenxxxxxx; i++) {
               particles[i] = ModelParticle.toObject(mem, offxxxxxx);
               offxxxxxx += particles[i].computeSize();
            }
         }

         int[] transitionToGroups = null;
         if (hasTransitionToGroups(mem, offset)) {
            int offxxxxxxx = offset + getValidatedOffset(mem, offset, 236, 276, "TransitionToGroups");
            long packedxxxxxxx = VarInt.getWithLength(mem, offxxxxxxx);
            int lenxxxxxxx = (int)packedxxxxxxx;
            if (lenxxxxxxx < 0) {
               throw ProtocolException.negativeLength("TransitionToGroups", lenxxxxxxx);
            }

            if (lenxxxxxxx > 4096000) {
               throw ProtocolException.arrayTooLong("TransitionToGroups", lenxxxxxxx, 4096000);
            }

            int lenOffset = (int)(packedxxxxxxx >>> 32);
            if (offxxxxxxx + lenOffset + lenxxxxxxx * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("TransitionToGroups", offxxxxxxx + lenOffset + lenxxxxxxx * 4, (int)mem.byteSize());
            }

            offxxxxxxx += lenOffset;
            transitionToGroups = new int[lenxxxxxxx];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, offxxxxxxx, transitionToGroups, 0, lenxxxxxxx);
         }

         Map<InteractionType, Integer> interactions = null;
         if (hasInteractions(mem, offset)) {
            int offxxxxxxxx = offset + getValidatedOffset(mem, offset, 256, 276, "Interactions");
            long packedxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxx);
            int lenxxxxxxxx = (int)packedxxxxxxxx;
            if (lenxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("Interactions", lenxxxxxxxx);
            }

            if (lenxxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Interactions", lenxxxxxxxx, 4096000);
            }

            interactions = new HashMap<>(lenxxxxxxxx);
            offxxxxxxxx += (int)(packedxxxxxxxx >>> 32);

            for (int i = 0; i < lenxxxxxxxx; i++) {
               InteractionType keyxx = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)offxxxxxxxx));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++offxxxxxxxx));
               offxxxxxxxx += 4;
               if (interactions.put(keyxx, value) != null) {
                  throw ProtocolException.duplicateKey("Interactions", keyxx);
               }
            }
         }

         Map<String, Integer> states = null;
         if (hasStates(mem, offset)) {
            int offxxxxxxxxx = offset + getValidatedOffset(mem, offset, 260, 276, "States");
            long packedxxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxxx);
            int lenxxxxxxxxx = (int)packedxxxxxxxxx;
            if (lenxxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("States", lenxxxxxxxxx);
            }

            if (lenxxxxxxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("States", lenxxxxxxxxx, 4096000);
            }

            states = new HashMap<>(lenxxxxxxxxx);
            offxxxxxxxxx += (int)(packedxxxxxxxxx >>> 32);

            for (int ix = 0; ix < lenxxxxxxxxx; ix++) {
               long keyPacked = VarInt.getWithLength(mem, offxxxxxxxxx);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String keyxx = PacketIO.readVarString("key", mem, offxxxxxxxxx, 16384000, PacketIO.UTF8);
               offxxxxxxxxx += nkey;
               int value = mem.get(PacketIO.PROTO_INT, (long)offxxxxxxxxx);
               offxxxxxxxxx += 4;
               if (states.put(keyxx, value) != null) {
                  throw ProtocolException.duplicateKey("States", keyxx);
               }
            }
         }

         int[] tagIndexes = null;
         if (hasTagIndexes(mem, offset)) {
            int offxxxxxxxxxx = offset + getValidatedOffset(mem, offset, 264, 276, "TagIndexes");
            long packedxxxxxxxxxx = VarInt.getWithLength(mem, offxxxxxxxxxx);
            int lenxxxxxxxxxx = (int)packedxxxxxxxxxx;
            if (lenxxxxxxxxxx < 0) {
               throw ProtocolException.negativeLength("TagIndexes", lenxxxxxxxxxx);
            }

            if (lenxxxxxxxxxx > 4096000) {
               throw ProtocolException.arrayTooLong("TagIndexes", lenxxxxxxxxxx, 4096000);
            }

            int lenOffset = (int)(packedxxxxxxxxxx >>> 32);
            if (offxxxxxxxxxx + lenOffset + lenxxxxxxxxxx * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("TagIndexes", offxxxxxxxxxx + lenOffset + lenxxxxxxxxxx * 4, (int)mem.byteSize());
            }

            offxxxxxxxxxx += lenOffset;
            tagIndexes = new int[lenxxxxxxxxxx];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, offxxxxxxxxxx, tagIndexes, 0, lenxxxxxxxxxx);
         }

         return new BlockType(
            hasItem(mem, offset)
               ? PacketIO.readVarString("Item", mem, offset + getValidatedOffset(mem, offset, 176, 276, "Item"), 4096000, PacketIO.UTF8)
               : null,
            hasName(mem, offset)
               ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 180, 276, "Name"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5)),
            DrawType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 6))),
            BlockMaterial.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 7))),
            Opacity.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 8))),
            shaderEffect,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 13)),
            hasModel(mem, offset)
               ? PacketIO.readVarString("Model", mem, offset + getValidatedOffset(mem, offset, 188, 276, "Model"), 4096000, PacketIO.UTF8)
               : null,
            modelTexture,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 17)),
            hasModelAnimation(mem, offset)
               ? PacketIO.readVarString("ModelAnimation", mem, offset + getValidatedOffset(mem, offset, 196, 276, "ModelAnimation"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 21)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 22)),
            BlockSupportsRequiredForType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 26))),
            support,
            supporting,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 27)),
            cubeTextures,
            hasCubeSideMaskTexture(mem, offset)
               ? PacketIO.readVarString(
                  "CubeSideMaskTexture", mem, offset + getValidatedOffset(mem, offset, 212, 276, "CubeSideMaskTexture"), 4096000, PacketIO.UTF8
               )
               : null,
            ShadingMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 28))),
            RandomRotation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 29))),
            VariantRotation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 30))),
            Rotation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 31))),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 32)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 36)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 40)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 44)),
            conditionalSounds,
            particles,
            hasBlockParticleSetId(mem, offset)
               ? PacketIO.readVarString(
                  "BlockParticleSetId", mem, offset + getValidatedOffset(mem, offset, 224, 276, "BlockParticleSetId"), 4096000, PacketIO.UTF8
               )
               : null,
            hasBlockBreakingDecalId(mem, offset)
               ? PacketIO.readVarString(
                  "BlockBreakingDecalId", mem, offset + getValidatedOffset(mem, offset, 228, 276, "BlockBreakingDecalId"), 4096000, PacketIO.UTF8
               )
               : null,
            hasParticleColor(mem, offset) ? Color.toObject(mem, offset + 48) : null,
            hasTextureComputedColor(mem, offset) ? Color.toObject(mem, offset + 51) : null,
            hasLight(mem, offset) ? ColorLight.toObject(mem, offset + 54) : null,
            hasTint(mem, offset) ? Tint.toObject(mem, offset + 58) : null,
            hasBiomeTint(mem, offset) ? Tint.toObject(mem, offset + 82) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 106)),
            hasTransitionTexture(mem, offset)
               ? PacketIO.readVarString(
                  "TransitionTexture", mem, offset + getValidatedOffset(mem, offset, 232, 276, "TransitionTexture"), 4096000, PacketIO.UTF8
               )
               : null,
            transitionToGroups,
            hasMovementSettings(mem, offset) ? BlockMovementSettings.toObject(mem, offset + 110) : null,
            hasFlags(mem, offset) ? BlockFlags.toObject(mem, offset + 152) : null,
            hasInteractionHint(mem, offset)
               ? PacketIO.readVarString("InteractionHint", mem, offset + getValidatedOffset(mem, offset, 240, 276, "InteractionHint"), 4096000, PacketIO.UTF8)
               : null,
            hasGathering(mem, offset) ? BlockGathering.toObject(mem, offset + getValidatedOffset(mem, offset, 244, 276, "Gathering")) : null,
            hasPlacementSettings(mem, offset) ? BlockPlacementSettings.toObject(mem, offset + 154) : null,
            hasDisplay(mem, offset) ? ModelDisplay.toObject(mem, offset + getValidatedOffset(mem, offset, 248, 276, "Display")) : null,
            hasRail(mem, offset) ? RailConfig.toObject(mem, offset + getValidatedOffset(mem, offset, 252, 276, "Rail")) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 171)),
            interactions,
            states,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 172)),
            tagIndexes,
            hasBench(mem, offset) ? Bench.toObject(mem, offset + getValidatedOffset(mem, offset, 268, 276, "Bench")) : null,
            hasConnectedBlockRuleSet(mem, offset)
               ? ConnectedBlockRuleSet.toObject(mem, offset + getValidatedOffset(mem, offset, 272, 276, "ConnectedBlockRuleSet"))
               : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[5];
      if (this.particleColor != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.textureComputedColor != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.light != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.tint != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.biomeTint != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.movementSettings != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.flags != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.placementSettings != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.item != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      if (this.name != null) {
         nullBits[1] = (byte)(nullBits[1] | 2);
      }

      if (this.shaderEffect != null) {
         nullBits[1] = (byte)(nullBits[1] | 4);
      }

      if (this.model != null) {
         nullBits[1] = (byte)(nullBits[1] | 8);
      }

      if (this.modelTexture != null) {
         nullBits[1] = (byte)(nullBits[1] | 16);
      }

      if (this.modelAnimation != null) {
         nullBits[1] = (byte)(nullBits[1] | 32);
      }

      if (this.support != null) {
         nullBits[1] = (byte)(nullBits[1] | 64);
      }

      if (this.supporting != null) {
         nullBits[1] = (byte)(nullBits[1] | 128);
      }

      if (this.cubeTextures != null) {
         nullBits[2] = (byte)(nullBits[2] | 1);
      }

      if (this.cubeSideMaskTexture != null) {
         nullBits[2] = (byte)(nullBits[2] | 2);
      }

      if (this.conditionalSounds != null) {
         nullBits[2] = (byte)(nullBits[2] | 4);
      }

      if (this.particles != null) {
         nullBits[2] = (byte)(nullBits[2] | 8);
      }

      if (this.blockParticleSetId != null) {
         nullBits[2] = (byte)(nullBits[2] | 16);
      }

      if (this.blockBreakingDecalId != null) {
         nullBits[2] = (byte)(nullBits[2] | 32);
      }

      if (this.transitionTexture != null) {
         nullBits[2] = (byte)(nullBits[2] | 64);
      }

      if (this.transitionToGroups != null) {
         nullBits[2] = (byte)(nullBits[2] | 128);
      }

      if (this.interactionHint != null) {
         nullBits[3] = (byte)(nullBits[3] | 1);
      }

      if (this.gathering != null) {
         nullBits[3] = (byte)(nullBits[3] | 2);
      }

      if (this.display != null) {
         nullBits[3] = (byte)(nullBits[3] | 4);
      }

      if (this.rail != null) {
         nullBits[3] = (byte)(nullBits[3] | 8);
      }

      if (this.interactions != null) {
         nullBits[3] = (byte)(nullBits[3] | 16);
      }

      if (this.states != null) {
         nullBits[3] = (byte)(nullBits[3] | 32);
      }

      if (this.tagIndexes != null) {
         nullBits[3] = (byte)(nullBits[3] | 64);
      }

      if (this.bench != null) {
         nullBits[3] = (byte)(nullBits[3] | 128);
      }

      if (this.connectedBlockRuleSet != null) {
         nullBits[4] = (byte)(nullBits[4] | 1);
      }

      buf.writeBytes(nullBits);
      buf.writeByte(this.unknown ? 1 : 0);
      buf.writeByte(this.drawType.getValue());
      buf.writeByte(this.material.getValue());
      buf.writeByte(this.opacity.getValue());
      buf.writeIntLE(this.hitbox);
      buf.writeIntLE(this.interactionHitbox);
      buf.writeFloatLE(this.modelScale);
      buf.writeByte(this.looping ? 1 : 0);
      buf.writeIntLE(this.maxSupportDistance);
      buf.writeByte(this.blockSupportsRequiredFor.getValue());
      buf.writeByte(this.requiresAlphaBlending ? 1 : 0);
      buf.writeByte(this.cubeShadingMode.getValue());
      buf.writeByte(this.randomRotation.getValue());
      buf.writeByte(this.variantRotation.getValue());
      buf.writeByte(this.rotationYawPlacementOffset.getValue());
      buf.writeIntLE(this.blockSoundSetIndex);
      buf.writeIntLE(this.physicalMaterialIndex);
      buf.writeFloatLE(this.soundOcclusionOpacity);
      buf.writeIntLE(this.ambientSoundEventIndex);
      if (this.particleColor != null) {
         this.particleColor.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      if (this.textureComputedColor != null) {
         this.textureComputedColor.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      if (this.light != null) {
         this.light.serialize(buf);
      } else {
         buf.writeZero(4);
      }

      if (this.tint != null) {
         this.tint.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      if (this.biomeTint != null) {
         this.biomeTint.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      buf.writeIntLE(this.group);
      if (this.movementSettings != null) {
         this.movementSettings.serialize(buf);
      } else {
         buf.writeZero(42);
      }

      if (this.flags != null) {
         this.flags.serialize(buf);
      } else {
         buf.writeZero(2);
      }

      if (this.placementSettings != null) {
         this.placementSettings.serialize(buf);
      } else {
         buf.writeZero(17);
      }

      buf.writeByte(this.ignoreSupportWhenPlaced ? 1 : 0);
      buf.writeIntLE(this.transitionToTag);
      int itemOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int shaderEffectOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int modelOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int modelTextureOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int modelAnimationOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int supportOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int supportingOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int cubeTexturesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int cubeSideMaskTextureOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int conditionalSoundsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int particlesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int blockParticleSetIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int blockBreakingDecalIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int transitionTextureOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int transitionToGroupsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int interactionHintOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int gatheringOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int displayOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int railOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int interactionsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int statesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int tagIndexesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int benchOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int connectedBlockRuleSetOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.item != null) {
         buf.setIntLE(itemOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.item, 4096000);
      } else {
         buf.setIntLE(itemOffsetSlot, -1);
      }

      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.shaderEffect != null) {
         buf.setIntLE(shaderEffectOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.shaderEffect.length > 4096000) {
            throw ProtocolException.arrayTooLong("ShaderEffect", this.shaderEffect.length, 4096000);
         }

         VarInt.write(buf, this.shaderEffect.length);

         for (ShaderType item : this.shaderEffect) {
            buf.writeByte(item.getValue());
         }
      } else {
         buf.setIntLE(shaderEffectOffsetSlot, -1);
      }

      if (this.model != null) {
         buf.setIntLE(modelOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.model, 4096000);
      } else {
         buf.setIntLE(modelOffsetSlot, -1);
      }

      if (this.modelTexture != null) {
         buf.setIntLE(modelTextureOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.modelTexture.length > 4096000) {
            throw ProtocolException.arrayTooLong("ModelTexture", this.modelTexture.length, 4096000);
         }

         VarInt.write(buf, this.modelTexture.length);

         for (ModelTexture item : this.modelTexture) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(modelTextureOffsetSlot, -1);
      }

      if (this.modelAnimation != null) {
         buf.setIntLE(modelAnimationOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.modelAnimation, 4096000);
      } else {
         buf.setIntLE(modelAnimationOffsetSlot, -1);
      }

      if (this.support != null) {
         buf.setIntLE(supportOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.support.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Support", this.support.size(), 4096000);
         }

         VarInt.write(buf, this.support.size());

         for (Entry<BlockNeighbor, RequiredBlockFaceSupport[]> e : this.support.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            VarInt.write(buf, e.getValue().length);

            for (RequiredBlockFaceSupport arrItem : e.getValue()) {
               arrItem.serialize(buf);
            }
         }
      } else {
         buf.setIntLE(supportOffsetSlot, -1);
      }

      if (this.supporting != null) {
         buf.setIntLE(supportingOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.supporting.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Supporting", this.supporting.size(), 4096000);
         }

         VarInt.write(buf, this.supporting.size());

         for (Entry<BlockNeighbor, BlockFaceSupport[]> e : this.supporting.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            VarInt.write(buf, e.getValue().length);

            for (BlockFaceSupport arrItem : e.getValue()) {
               arrItem.serialize(buf);
            }
         }
      } else {
         buf.setIntLE(supportingOffsetSlot, -1);
      }

      if (this.cubeTextures != null) {
         buf.setIntLE(cubeTexturesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.cubeTextures.length > 4096000) {
            throw ProtocolException.arrayTooLong("CubeTextures", this.cubeTextures.length, 4096000);
         }

         VarInt.write(buf, this.cubeTextures.length);

         for (BlockTextures item : this.cubeTextures) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(cubeTexturesOffsetSlot, -1);
      }

      if (this.cubeSideMaskTexture != null) {
         buf.setIntLE(cubeSideMaskTextureOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.cubeSideMaskTexture, 4096000);
      } else {
         buf.setIntLE(cubeSideMaskTextureOffsetSlot, -1);
      }

      if (this.conditionalSounds != null) {
         buf.setIntLE(conditionalSoundsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.conditionalSounds.length > 4096000) {
            throw ProtocolException.arrayTooLong("ConditionalSounds", this.conditionalSounds.length, 4096000);
         }

         VarInt.write(buf, this.conditionalSounds.length);

         for (ConditionalBlockSound item : this.conditionalSounds) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(conditionalSoundsOffsetSlot, -1);
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

      if (this.blockParticleSetId != null) {
         buf.setIntLE(blockParticleSetIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.blockParticleSetId, 4096000);
      } else {
         buf.setIntLE(blockParticleSetIdOffsetSlot, -1);
      }

      if (this.blockBreakingDecalId != null) {
         buf.setIntLE(blockBreakingDecalIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.blockBreakingDecalId, 4096000);
      } else {
         buf.setIntLE(blockBreakingDecalIdOffsetSlot, -1);
      }

      if (this.transitionTexture != null) {
         buf.setIntLE(transitionTextureOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.transitionTexture, 4096000);
      } else {
         buf.setIntLE(transitionTextureOffsetSlot, -1);
      }

      if (this.transitionToGroups != null) {
         buf.setIntLE(transitionToGroupsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.transitionToGroups.length > 4096000) {
            throw ProtocolException.arrayTooLong("TransitionToGroups", this.transitionToGroups.length, 4096000);
         }

         VarInt.write(buf, this.transitionToGroups.length);

         for (int item : this.transitionToGroups) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(transitionToGroupsOffsetSlot, -1);
      }

      if (this.interactionHint != null) {
         buf.setIntLE(interactionHintOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.interactionHint, 4096000);
      } else {
         buf.setIntLE(interactionHintOffsetSlot, -1);
      }

      if (this.gathering != null) {
         buf.setIntLE(gatheringOffsetSlot, buf.writerIndex() - varBlockStart);
         this.gathering.serialize(buf);
      } else {
         buf.setIntLE(gatheringOffsetSlot, -1);
      }

      if (this.display != null) {
         buf.setIntLE(displayOffsetSlot, buf.writerIndex() - varBlockStart);
         this.display.serialize(buf);
      } else {
         buf.setIntLE(displayOffsetSlot, -1);
      }

      if (this.rail != null) {
         buf.setIntLE(railOffsetSlot, buf.writerIndex() - varBlockStart);
         this.rail.serialize(buf);
      } else {
         buf.setIntLE(railOffsetSlot, -1);
      }

      if (this.interactions != null) {
         buf.setIntLE(interactionsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.interactions.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", this.interactions.size(), 4096000);
         }

         VarInt.write(buf, this.interactions.size());

         for (Entry<InteractionType, Integer> e : this.interactions.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            buf.writeIntLE(e.getValue());
         }
      } else {
         buf.setIntLE(interactionsOffsetSlot, -1);
      }

      if (this.states != null) {
         buf.setIntLE(statesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.states.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("States", this.states.size(), 4096000);
         }

         VarInt.write(buf, this.states.size());

         for (Entry<String, Integer> e : this.states.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            buf.writeIntLE(e.getValue());
         }
      } else {
         buf.setIntLE(statesOffsetSlot, -1);
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

      if (this.bench != null) {
         buf.setIntLE(benchOffsetSlot, buf.writerIndex() - varBlockStart);
         this.bench.serialize(buf);
      } else {
         buf.setIntLE(benchOffsetSlot, -1);
      }

      if (this.connectedBlockRuleSet != null) {
         buf.setIntLE(connectedBlockRuleSetOffsetSlot, buf.writerIndex() - varBlockStart);
         this.connectedBlockRuleSet.serialize(buf);
      } else {
         buf.setIntLE(connectedBlockRuleSetOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.particleColor != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.textureComputedColor != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.light != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.tint != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.biomeTint != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.movementSettings != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.flags != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.placementSettings != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      nullBits = 0;
      if (this.item != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.name != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.shaderEffect != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.model != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.modelTexture != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.modelAnimation != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.support != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.supporting != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), nullBits);
      nullBits = 0;
      if (this.cubeTextures != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.cubeSideMaskTexture != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.conditionalSounds != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.particles != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.blockParticleSetId != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.blockBreakingDecalId != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.transitionTexture != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.transitionToGroups != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), nullBits);
      nullBits = 0;
      if (this.interactionHint != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.gathering != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.display != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.rail != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.interactions != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.states != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.tagIndexes != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.bench != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 3), nullBits);
      nullBits = 0;
      if (this.connectedBlockRuleSet != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 4), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 5, this.unknown);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 6), (byte)this.drawType.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 7), (byte)this.material.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 8), (byte)this.opacity.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 9), this.hitbox);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 13), this.interactionHitbox);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 17), this.modelScale);
      mem.set(PacketIO.PROTO_BOOL, offset + 21, this.looping);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 22), this.maxSupportDistance);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 26), (byte)this.blockSupportsRequiredFor.getValue());
      mem.set(PacketIO.PROTO_BOOL, offset + 27, this.requiresAlphaBlending);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 28), (byte)this.cubeShadingMode.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 29), (byte)this.randomRotation.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 30), (byte)this.variantRotation.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 31), (byte)this.rotationYawPlacementOffset.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 32), this.blockSoundSetIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 36), this.physicalMaterialIndex);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 40), this.soundOcclusionOpacity);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 44), this.ambientSoundEventIndex);
      if (this.particleColor != null) {
         this.particleColor.serialize(mem, offset + 48);
      } else {
         mem.asSlice(offset + 48, 3L).fill((byte)0);
      }

      if (this.textureComputedColor != null) {
         this.textureComputedColor.serialize(mem, offset + 51);
      } else {
         mem.asSlice(offset + 51, 3L).fill((byte)0);
      }

      if (this.light != null) {
         this.light.serialize(mem, offset + 54);
      } else {
         mem.asSlice(offset + 54, 4L).fill((byte)0);
      }

      if (this.tint != null) {
         this.tint.serialize(mem, offset + 58);
      } else {
         mem.asSlice(offset + 58, 24L).fill((byte)0);
      }

      if (this.biomeTint != null) {
         this.biomeTint.serialize(mem, offset + 82);
      } else {
         mem.asSlice(offset + 82, 24L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_INT, (long)(offset + 106), this.group);
      if (this.movementSettings != null) {
         this.movementSettings.serialize(mem, offset + 110);
      } else {
         mem.asSlice(offset + 110, 42L).fill((byte)0);
      }

      if (this.flags != null) {
         this.flags.serialize(mem, offset + 152);
      } else {
         mem.asSlice(offset + 152, 2L).fill((byte)0);
      }

      if (this.placementSettings != null) {
         this.placementSettings.serialize(mem, offset + 154);
      } else {
         mem.asSlice(offset + 154, 17L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 171, this.ignoreSupportWhenPlaced);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 172), this.transitionToTag);
      int varOffset = offset + 276;
      if (this.item != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 176), varOffset - offset - 276);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.item, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 176), -1);
      }

      if (this.name != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 180), varOffset - offset - 276);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 180), -1);
      }

      if (this.shaderEffect != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 184), varOffset - offset - 276);
         if (this.shaderEffect.length > 4096000) {
            throw ProtocolException.arrayTooLong("ShaderEffect", this.shaderEffect.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.shaderEffect.length);

         for (int i = 0; i < this.shaderEffect.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.shaderEffect[i].getValue());
         }

         varOffset += this.shaderEffect.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 184), -1);
      }

      if (this.model != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 188), varOffset - offset - 276);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.model, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 188), -1);
      }

      if (this.modelTexture != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 192), varOffset - offset - 276);
         if (this.modelTexture.length > 4096000) {
            throw ProtocolException.arrayTooLong("ModelTexture", this.modelTexture.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.modelTexture.length);
         int modelTextureValueOffset = 0;

         for (int i = 0; i < this.modelTexture.length; i++) {
            modelTextureValueOffset += this.modelTexture[i].serialize(mem, varOffset + modelTextureValueOffset);
         }

         varOffset += modelTextureValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 192), -1);
      }

      if (this.modelAnimation != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 196), varOffset - offset - 276);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.modelAnimation, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 196), -1);
      }

      if (this.support != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 200), varOffset - offset - 276);
         if (this.support.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Support", this.support.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.support.size());

         for (Entry<BlockNeighbor, RequiredBlockFaceSupport[]> e : this.support.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            varOffset = ++varOffset + VarInt.set(mem, varOffset, e.getValue().length);

            for (RequiredBlockFaceSupport arrItem : e.getValue()) {
               varOffset += arrItem.serialize(mem, varOffset);
            }
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 200), -1);
      }

      if (this.supporting != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 204), varOffset - offset - 276);
         if (this.supporting.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Supporting", this.supporting.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.supporting.size());

         for (Entry<BlockNeighbor, BlockFaceSupport[]> e : this.supporting.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            varOffset = ++varOffset + VarInt.set(mem, varOffset, e.getValue().length);

            for (BlockFaceSupport arrItem : e.getValue()) {
               varOffset += arrItem.serialize(mem, varOffset);
            }
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 204), -1);
      }

      if (this.cubeTextures != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 208), varOffset - offset - 276);
         if (this.cubeTextures.length > 4096000) {
            throw ProtocolException.arrayTooLong("CubeTextures", this.cubeTextures.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.cubeTextures.length);
         int cubeTexturesValueOffset = 0;

         for (int i = 0; i < this.cubeTextures.length; i++) {
            cubeTexturesValueOffset += this.cubeTextures[i].serialize(mem, varOffset + cubeTexturesValueOffset);
         }

         varOffset += cubeTexturesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 208), -1);
      }

      if (this.cubeSideMaskTexture != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 212), varOffset - offset - 276);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.cubeSideMaskTexture, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 212), -1);
      }

      if (this.conditionalSounds != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 216), varOffset - offset - 276);
         if (this.conditionalSounds.length > 4096000) {
            throw ProtocolException.arrayTooLong("ConditionalSounds", this.conditionalSounds.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.conditionalSounds.length);
         int conditionalSoundsValueOffset = 0;

         for (int i = 0; i < this.conditionalSounds.length; i++) {
            conditionalSoundsValueOffset += this.conditionalSounds[i].serialize(mem, varOffset + conditionalSoundsValueOffset);
         }

         varOffset += conditionalSoundsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 216), -1);
      }

      if (this.particles != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 220), varOffset - offset - 276);
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
         mem.set(PacketIO.PROTO_INT, (long)(offset + 220), -1);
      }

      if (this.blockParticleSetId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 224), varOffset - offset - 276);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.blockParticleSetId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 224), -1);
      }

      if (this.blockBreakingDecalId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 228), varOffset - offset - 276);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.blockBreakingDecalId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 228), -1);
      }

      if (this.transitionTexture != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 232), varOffset - offset - 276);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.transitionTexture, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 232), -1);
      }

      if (this.transitionToGroups != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 236), varOffset - offset - 276);
         if (this.transitionToGroups.length > 4096000) {
            throw ProtocolException.arrayTooLong("TransitionToGroups", this.transitionToGroups.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.transitionToGroups.length);
         MemorySegment.copy(this.transitionToGroups, 0, mem, PacketIO.PROTO_INT, varOffset, this.transitionToGroups.length);
         varOffset += this.transitionToGroups.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 236), -1);
      }

      if (this.interactionHint != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 240), varOffset - offset - 276);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.interactionHint, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 240), -1);
      }

      if (this.gathering != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 244), varOffset - offset - 276);
         varOffset += this.gathering.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 244), -1);
      }

      if (this.display != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 248), varOffset - offset - 276);
         varOffset += this.display.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 248), -1);
      }

      if (this.rail != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 252), varOffset - offset - 276);
         varOffset += this.rail.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 252), -1);
      }

      if (this.interactions != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 256), varOffset - offset - 276);
         if (this.interactions.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", this.interactions.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.interactions.size());

         for (Entry<InteractionType, Integer> e : this.interactions.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            mem.set(PacketIO.PROTO_INT, (long)(++varOffset), e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 256), -1);
      }

      if (this.states != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 260), varOffset - offset - 276);
         if (this.states.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("States", this.states.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.states.size());

         for (Entry<String, Integer> e : this.states.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 260), -1);
      }

      if (this.tagIndexes != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 264), varOffset - offset - 276);
         if (this.tagIndexes.length > 4096000) {
            throw ProtocolException.arrayTooLong("TagIndexes", this.tagIndexes.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.tagIndexes.length);
         MemorySegment.copy(this.tagIndexes, 0, mem, PacketIO.PROTO_INT, varOffset, this.tagIndexes.length);
         varOffset += this.tagIndexes.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 264), -1);
      }

      if (this.bench != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 268), varOffset - offset - 276);
         varOffset += this.bench.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 268), -1);
      }

      if (this.connectedBlockRuleSet != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 272), varOffset - offset - 276);
         varOffset += this.connectedBlockRuleSet.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 272), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 276;
      if (this.item != null) {
         size += PacketIO.stringSize(this.item);
      }

      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.shaderEffect != null) {
         size += VarInt.size(this.shaderEffect.length) + this.shaderEffect.length * 1;
      }

      if (this.model != null) {
         size += PacketIO.stringSize(this.model);
      }

      if (this.modelTexture != null) {
         int modelTextureSize = 0;

         for (ModelTexture elem : this.modelTexture) {
            modelTextureSize += elem.computeSize();
         }

         size += VarInt.size(this.modelTexture.length) + modelTextureSize;
      }

      if (this.modelAnimation != null) {
         size += PacketIO.stringSize(this.modelAnimation);
      }

      if (this.support != null) {
         int supportSize = 0;

         for (Entry<BlockNeighbor, RequiredBlockFaceSupport[]> kvp : this.support.entrySet()) {
            supportSize += 1 + VarInt.size(kvp.getValue().length) + Arrays.stream(kvp.getValue()).mapToInt(inner -> inner.computeSize()).sum();
         }

         size += VarInt.size(this.support.size()) + supportSize;
      }

      if (this.supporting != null) {
         int supportingSize = 0;

         for (Entry<BlockNeighbor, BlockFaceSupport[]> kvp : this.supporting.entrySet()) {
            supportingSize += 1 + VarInt.size(kvp.getValue().length) + Arrays.stream(kvp.getValue()).mapToInt(inner -> inner.computeSize()).sum();
         }

         size += VarInt.size(this.supporting.size()) + supportingSize;
      }

      if (this.cubeTextures != null) {
         int cubeTexturesSize = 0;

         for (BlockTextures elem : this.cubeTextures) {
            cubeTexturesSize += elem.computeSize();
         }

         size += VarInt.size(this.cubeTextures.length) + cubeTexturesSize;
      }

      if (this.cubeSideMaskTexture != null) {
         size += PacketIO.stringSize(this.cubeSideMaskTexture);
      }

      if (this.conditionalSounds != null) {
         size += VarInt.size(this.conditionalSounds.length) + this.conditionalSounds.length * 8;
      }

      if (this.particles != null) {
         int particlesSize = 0;

         for (ModelParticle elem : this.particles) {
            particlesSize += elem.computeSize();
         }

         size += VarInt.size(this.particles.length) + particlesSize;
      }

      if (this.blockParticleSetId != null) {
         size += PacketIO.stringSize(this.blockParticleSetId);
      }

      if (this.blockBreakingDecalId != null) {
         size += PacketIO.stringSize(this.blockBreakingDecalId);
      }

      if (this.transitionTexture != null) {
         size += PacketIO.stringSize(this.transitionTexture);
      }

      if (this.transitionToGroups != null) {
         size += VarInt.size(this.transitionToGroups.length) + this.transitionToGroups.length * 4;
      }

      if (this.interactionHint != null) {
         size += PacketIO.stringSize(this.interactionHint);
      }

      if (this.gathering != null) {
         size += this.gathering.computeSize();
      }

      if (this.display != null) {
         size += this.display.computeSize();
      }

      if (this.rail != null) {
         size += this.rail.computeSize();
      }

      if (this.interactions != null) {
         size += VarInt.size(this.interactions.size()) + this.interactions.size() * 5;
      }

      if (this.states != null) {
         int statesSize = 0;

         for (Entry<String, Integer> kvp : this.states.entrySet()) {
            statesSize += PacketIO.stringSize(kvp.getKey()) + 4;
         }

         size += VarInt.size(this.states.size()) + statesSize;
      }

      if (this.tagIndexes != null) {
         size += VarInt.size(this.tagIndexes.length) + this.tagIndexes.length * 4;
      }

      if (this.bench != null) {
         size += this.bench.computeSize();
      }

      if (this.connectedBlockRuleSet != null) {
         size += this.connectedBlockRuleSet.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 276) {
         return ValidationResult.error("Buffer too small: expected at least 276 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 5);
         int v = buffer.getByte(offset + 6) & 255;
         if (v >= 5) {
            return ValidationResult.error("Invalid DrawType value for DrawType");
         } else {
            v = buffer.getByte(offset + 7) & 255;
            if (v >= 2) {
               return ValidationResult.error("Invalid BlockMaterial value for Material");
            } else {
               v = buffer.getByte(offset + 8) & 255;
               if (v >= 4) {
                  return ValidationResult.error("Invalid Opacity value for Opacity");
               } else {
                  v = buffer.getByte(offset + 26) & 255;
                  if (v >= 2) {
                     return ValidationResult.error("Invalid BlockSupportsRequiredForType value for BlockSupportsRequiredFor");
                  } else {
                     v = buffer.getByte(offset + 28) & 255;
                     if (v >= 4) {
                        return ValidationResult.error("Invalid ShadingMode value for CubeShadingMode");
                     } else {
                        v = buffer.getByte(offset + 29) & 255;
                        if (v >= 5) {
                           return ValidationResult.error("Invalid RandomRotation value for RandomRotation");
                        } else {
                           v = buffer.getByte(offset + 30) & 255;
                           if (v >= 8) {
                              return ValidationResult.error("Invalid VariantRotation value for VariantRotation");
                           } else {
                              v = buffer.getByte(offset + 31) & 255;
                              if (v >= 4) {
                                 return ValidationResult.error("Invalid Rotation value for RotationYawPlacementOffset");
                              } else {
                                 if ((nullBits[1] & 1) != 0) {
                                    v = buffer.getIntLE(offset + 176);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for Item");
                                    }

                                    int pos = offset + 276 + v;
                                    int itemLen = VarInt.peek(buffer, pos);
                                    if (itemLen < 0) {
                                       return ValidationResult.error("Invalid string length for Item");
                                    }

                                    if (itemLen > 4096000) {
                                       return ValidationResult.error("Item exceeds max length 4096000");
                                    }

                                    pos += VarInt.size(itemLen);
                                    pos += itemLen;
                                    if (pos > buffer.writerIndex()) {
                                       return ValidationResult.error("Buffer overflow reading Item");
                                    }
                                 }

                                 if ((nullBits[1] & 2) != 0) {
                                    v = buffer.getIntLE(offset + 180);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for Name");
                                    }

                                    int posx = offset + 276 + v;
                                    int nameLen = VarInt.peek(buffer, posx);
                                    if (nameLen < 0) {
                                       return ValidationResult.error("Invalid string length for Name");
                                    }

                                    if (nameLen > 4096000) {
                                       return ValidationResult.error("Name exceeds max length 4096000");
                                    }

                                    posx += VarInt.size(nameLen);
                                    posx += nameLen;
                                    if (posx > buffer.writerIndex()) {
                                       return ValidationResult.error("Buffer overflow reading Name");
                                    }
                                 }

                                 if ((nullBits[1] & 4) != 0) {
                                    v = buffer.getIntLE(offset + 184);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for ShaderEffect");
                                    }

                                    int posxx = offset + 276 + v;
                                    int shaderEffectCount = VarInt.peek(buffer, posxx);
                                    if (shaderEffectCount < 0) {
                                       return ValidationResult.error("Invalid array count for ShaderEffect");
                                    }

                                    if (shaderEffectCount > 4096000) {
                                       return ValidationResult.error("ShaderEffect exceeds max length 4096000");
                                    }

                                    posxx += VarInt.size(shaderEffectCount);
                                    if (posxx + shaderEffectCount * 1L > buffer.writerIndex()) {
                                       return ValidationResult.error("Buffer overflow reading ShaderEffect");
                                    }

                                    for (int i = 0; i < shaderEffectCount; i++) {
                                       int vx = buffer.getByte(posxx) & 255;
                                       if (vx >= 10) {
                                          return ValidationResult.error("Invalid ShaderType value for ShaderEffect[i]");
                                       }

                                       posxx++;
                                    }
                                 }

                                 if ((nullBits[1] & 8) != 0) {
                                    v = buffer.getIntLE(offset + 188);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for Model");
                                    }

                                    int posxxx = offset + 276 + v;
                                    int modelLen = VarInt.peek(buffer, posxxx);
                                    if (modelLen < 0) {
                                       return ValidationResult.error("Invalid string length for Model");
                                    }

                                    if (modelLen > 4096000) {
                                       return ValidationResult.error("Model exceeds max length 4096000");
                                    }

                                    posxxx += VarInt.size(modelLen);
                                    posxxx += modelLen;
                                    if (posxxx > buffer.writerIndex()) {
                                       return ValidationResult.error("Buffer overflow reading Model");
                                    }
                                 }

                                 if ((nullBits[1] & 16) != 0) {
                                    v = buffer.getIntLE(offset + 192);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for ModelTexture");
                                    }

                                    int posxxxx = offset + 276 + v;
                                    int modelTextureCount = VarInt.peek(buffer, posxxxx);
                                    if (modelTextureCount < 0) {
                                       return ValidationResult.error("Invalid array count for ModelTexture");
                                    }

                                    if (modelTextureCount > 4096000) {
                                       return ValidationResult.error("ModelTexture exceeds max length 4096000");
                                    }

                                    posxxxx += VarInt.size(modelTextureCount);

                                    for (int i = 0; i < modelTextureCount; i++) {
                                       ValidationResult structResult = ModelTexture.validateStructure(buffer, posxxxx);
                                       if (!structResult.isValid()) {
                                          return ValidationResult.error("Invalid ModelTexture in ModelTexture[" + i + "]: " + structResult.error());
                                       }

                                       posxxxx += ModelTexture.computeBytesConsumed(buffer, posxxxx);
                                    }
                                 }

                                 if ((nullBits[1] & 32) != 0) {
                                    v = buffer.getIntLE(offset + 196);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for ModelAnimation");
                                    }

                                    int posxxxxx = offset + 276 + v;
                                    int modelAnimationLen = VarInt.peek(buffer, posxxxxx);
                                    if (modelAnimationLen < 0) {
                                       return ValidationResult.error("Invalid string length for ModelAnimation");
                                    }

                                    if (modelAnimationLen > 4096000) {
                                       return ValidationResult.error("ModelAnimation exceeds max length 4096000");
                                    }

                                    posxxxxx += VarInt.size(modelAnimationLen);
                                    posxxxxx += modelAnimationLen;
                                    if (posxxxxx > buffer.writerIndex()) {
                                       return ValidationResult.error("Buffer overflow reading ModelAnimation");
                                    }
                                 }

                                 if ((nullBits[1] & 64) != 0) {
                                    v = buffer.getIntLE(offset + 200);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for Support");
                                    }

                                    int posxxxxxx = offset + 276 + v;
                                    int supportCount = VarInt.peek(buffer, posxxxxxx);
                                    if (supportCount < 0) {
                                       return ValidationResult.error("Invalid dictionary count for Support");
                                    }

                                    if (supportCount > 4096000) {
                                       return ValidationResult.error("Support exceeds max length 4096000");
                                    }

                                    posxxxxxx += VarInt.size(supportCount);

                                    for (int i = 0; i < supportCount; i++) {
                                       int vx = buffer.getByte(posxxxxxx) & 255;
                                       if (vx >= 26) {
                                          return ValidationResult.error("Invalid BlockNeighbor value for key");
                                       }

                                       vx = VarInt.peek(buffer, ++posxxxxxx);
                                       if (vx < 0) {
                                          return ValidationResult.error("Invalid array count for value");
                                       }

                                       posxxxxxx += VarInt.size(vx);

                                       for (int valueArrIdx = 0; valueArrIdx < vx; valueArrIdx++) {
                                          posxxxxxx += RequiredBlockFaceSupport.computeBytesConsumed(buffer, posxxxxxx);
                                       }
                                    }
                                 }

                                 if ((nullBits[1] & 128) != 0) {
                                    v = buffer.getIntLE(offset + 204);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for Supporting");
                                    }

                                    int posxxxxxxx = offset + 276 + v;
                                    int supportingCount = VarInt.peek(buffer, posxxxxxxx);
                                    if (supportingCount < 0) {
                                       return ValidationResult.error("Invalid dictionary count for Supporting");
                                    }

                                    if (supportingCount > 4096000) {
                                       return ValidationResult.error("Supporting exceeds max length 4096000");
                                    }

                                    posxxxxxxx += VarInt.size(supportingCount);

                                    for (int i = 0; i < supportingCount; i++) {
                                       int vxx = buffer.getByte(posxxxxxxx) & 255;
                                       if (vxx >= 26) {
                                          return ValidationResult.error("Invalid BlockNeighbor value for key");
                                       }

                                       vxx = VarInt.peek(buffer, ++posxxxxxxx);
                                       if (vxx < 0) {
                                          return ValidationResult.error("Invalid array count for value");
                                       }

                                       posxxxxxxx += VarInt.size(vxx);

                                       for (int valueArrIdx = 0; valueArrIdx < vxx; valueArrIdx++) {
                                          posxxxxxxx += BlockFaceSupport.computeBytesConsumed(buffer, posxxxxxxx);
                                       }
                                    }
                                 }

                                 if ((nullBits[2] & 1) != 0) {
                                    v = buffer.getIntLE(offset + 208);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for CubeTextures");
                                    }

                                    int posxxxxxxxx = offset + 276 + v;
                                    int cubeTexturesCount = VarInt.peek(buffer, posxxxxxxxx);
                                    if (cubeTexturesCount < 0) {
                                       return ValidationResult.error("Invalid array count for CubeTextures");
                                    }

                                    if (cubeTexturesCount > 4096000) {
                                       return ValidationResult.error("CubeTextures exceeds max length 4096000");
                                    }

                                    posxxxxxxxx += VarInt.size(cubeTexturesCount);

                                    for (int i = 0; i < cubeTexturesCount; i++) {
                                       ValidationResult structResult = BlockTextures.validateStructure(buffer, posxxxxxxxx);
                                       if (!structResult.isValid()) {
                                          return ValidationResult.error("Invalid BlockTextures in CubeTextures[" + i + "]: " + structResult.error());
                                       }

                                       posxxxxxxxx += BlockTextures.computeBytesConsumed(buffer, posxxxxxxxx);
                                    }
                                 }

                                 if ((nullBits[2] & 2) != 0) {
                                    v = buffer.getIntLE(offset + 212);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for CubeSideMaskTexture");
                                    }

                                    int posxxxxxxxxx = offset + 276 + v;
                                    int cubeSideMaskTextureLen = VarInt.peek(buffer, posxxxxxxxxx);
                                    if (cubeSideMaskTextureLen < 0) {
                                       return ValidationResult.error("Invalid string length for CubeSideMaskTexture");
                                    }

                                    if (cubeSideMaskTextureLen > 4096000) {
                                       return ValidationResult.error("CubeSideMaskTexture exceeds max length 4096000");
                                    }

                                    posxxxxxxxxx += VarInt.size(cubeSideMaskTextureLen);
                                    posxxxxxxxxx += cubeSideMaskTextureLen;
                                    if (posxxxxxxxxx > buffer.writerIndex()) {
                                       return ValidationResult.error("Buffer overflow reading CubeSideMaskTexture");
                                    }
                                 }

                                 if ((nullBits[2] & 4) != 0) {
                                    v = buffer.getIntLE(offset + 216);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for ConditionalSounds");
                                    }

                                    int posxxxxxxxxxx = offset + 276 + v;
                                    int conditionalSoundsCount = VarInt.peek(buffer, posxxxxxxxxxx);
                                    if (conditionalSoundsCount < 0) {
                                       return ValidationResult.error("Invalid array count for ConditionalSounds");
                                    }

                                    if (conditionalSoundsCount > 4096000) {
                                       return ValidationResult.error("ConditionalSounds exceeds max length 4096000");
                                    }

                                    posxxxxxxxxxx += VarInt.size(conditionalSoundsCount);
                                    posxxxxxxxxxx += conditionalSoundsCount * 8;
                                    if (posxxxxxxxxxx > buffer.writerIndex()) {
                                       return ValidationResult.error("Buffer overflow reading ConditionalSounds");
                                    }
                                 }

                                 if ((nullBits[2] & 8) != 0) {
                                    v = buffer.getIntLE(offset + 220);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for Particles");
                                    }

                                    int posxxxxxxxxxxx = offset + 276 + v;
                                    int particlesCount = VarInt.peek(buffer, posxxxxxxxxxxx);
                                    if (particlesCount < 0) {
                                       return ValidationResult.error("Invalid array count for Particles");
                                    }

                                    if (particlesCount > 4096000) {
                                       return ValidationResult.error("Particles exceeds max length 4096000");
                                    }

                                    posxxxxxxxxxxx += VarInt.size(particlesCount);

                                    for (int i = 0; i < particlesCount; i++) {
                                       ValidationResult structResult = ModelParticle.validateStructure(buffer, posxxxxxxxxxxx);
                                       if (!structResult.isValid()) {
                                          return ValidationResult.error("Invalid ModelParticle in Particles[" + i + "]: " + structResult.error());
                                       }

                                       posxxxxxxxxxxx += ModelParticle.computeBytesConsumed(buffer, posxxxxxxxxxxx);
                                    }
                                 }

                                 if ((nullBits[2] & 16) != 0) {
                                    v = buffer.getIntLE(offset + 224);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for BlockParticleSetId");
                                    }

                                    int posxxxxxxxxxxxx = offset + 276 + v;
                                    int blockParticleSetIdLen = VarInt.peek(buffer, posxxxxxxxxxxxx);
                                    if (blockParticleSetIdLen < 0) {
                                       return ValidationResult.error("Invalid string length for BlockParticleSetId");
                                    }

                                    if (blockParticleSetIdLen > 4096000) {
                                       return ValidationResult.error("BlockParticleSetId exceeds max length 4096000");
                                    }

                                    posxxxxxxxxxxxx += VarInt.size(blockParticleSetIdLen);
                                    posxxxxxxxxxxxx += blockParticleSetIdLen;
                                    if (posxxxxxxxxxxxx > buffer.writerIndex()) {
                                       return ValidationResult.error("Buffer overflow reading BlockParticleSetId");
                                    }
                                 }

                                 if ((nullBits[2] & 32) != 0) {
                                    v = buffer.getIntLE(offset + 228);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for BlockBreakingDecalId");
                                    }

                                    int posxxxxxxxxxxxxx = offset + 276 + v;
                                    int blockBreakingDecalIdLen = VarInt.peek(buffer, posxxxxxxxxxxxxx);
                                    if (blockBreakingDecalIdLen < 0) {
                                       return ValidationResult.error("Invalid string length for BlockBreakingDecalId");
                                    }

                                    if (blockBreakingDecalIdLen > 4096000) {
                                       return ValidationResult.error("BlockBreakingDecalId exceeds max length 4096000");
                                    }

                                    posxxxxxxxxxxxxx += VarInt.size(blockBreakingDecalIdLen);
                                    posxxxxxxxxxxxxx += blockBreakingDecalIdLen;
                                    if (posxxxxxxxxxxxxx > buffer.writerIndex()) {
                                       return ValidationResult.error("Buffer overflow reading BlockBreakingDecalId");
                                    }
                                 }

                                 if ((nullBits[2] & 64) != 0) {
                                    v = buffer.getIntLE(offset + 232);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for TransitionTexture");
                                    }

                                    int posxxxxxxxxxxxxxx = offset + 276 + v;
                                    int transitionTextureLen = VarInt.peek(buffer, posxxxxxxxxxxxxxx);
                                    if (transitionTextureLen < 0) {
                                       return ValidationResult.error("Invalid string length for TransitionTexture");
                                    }

                                    if (transitionTextureLen > 4096000) {
                                       return ValidationResult.error("TransitionTexture exceeds max length 4096000");
                                    }

                                    posxxxxxxxxxxxxxx += VarInt.size(transitionTextureLen);
                                    posxxxxxxxxxxxxxx += transitionTextureLen;
                                    if (posxxxxxxxxxxxxxx > buffer.writerIndex()) {
                                       return ValidationResult.error("Buffer overflow reading TransitionTexture");
                                    }
                                 }

                                 if ((nullBits[2] & 128) != 0) {
                                    v = buffer.getIntLE(offset + 236);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for TransitionToGroups");
                                    }

                                    int posxxxxxxxxxxxxxxx = offset + 276 + v;
                                    int transitionToGroupsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxx);
                                    if (transitionToGroupsCount < 0) {
                                       return ValidationResult.error("Invalid array count for TransitionToGroups");
                                    }

                                    if (transitionToGroupsCount > 4096000) {
                                       return ValidationResult.error("TransitionToGroups exceeds max length 4096000");
                                    }

                                    posxxxxxxxxxxxxxxx += VarInt.size(transitionToGroupsCount);
                                    posxxxxxxxxxxxxxxx += transitionToGroupsCount * 4;
                                    if (posxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                                       return ValidationResult.error("Buffer overflow reading TransitionToGroups");
                                    }
                                 }

                                 if ((nullBits[3] & 1) != 0) {
                                    v = buffer.getIntLE(offset + 240);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for InteractionHint");
                                    }

                                    int posxxxxxxxxxxxxxxxx = offset + 276 + v;
                                    int interactionHintLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxxx);
                                    if (interactionHintLen < 0) {
                                       return ValidationResult.error("Invalid string length for InteractionHint");
                                    }

                                    if (interactionHintLen > 4096000) {
                                       return ValidationResult.error("InteractionHint exceeds max length 4096000");
                                    }

                                    posxxxxxxxxxxxxxxxx += VarInt.size(interactionHintLen);
                                    posxxxxxxxxxxxxxxxx += interactionHintLen;
                                    if (posxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                                       return ValidationResult.error("Buffer overflow reading InteractionHint");
                                    }
                                 }

                                 if ((nullBits[3] & 2) != 0) {
                                    v = buffer.getIntLE(offset + 244);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for Gathering");
                                    }

                                    int posxxxxxxxxxxxxxxxxx = offset + 276 + v;
                                    ValidationResult gatheringResult = BlockGathering.validateStructure(buffer, posxxxxxxxxxxxxxxxxx);
                                    if (!gatheringResult.isValid()) {
                                       return ValidationResult.error("Invalid Gathering: " + gatheringResult.error());
                                    }

                                    posxxxxxxxxxxxxxxxxx += BlockGathering.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxxx);
                                 }

                                 if ((nullBits[3] & 4) != 0) {
                                    v = buffer.getIntLE(offset + 248);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for Display");
                                    }

                                    int posxxxxxxxxxxxxxxxxx = offset + 276 + v;
                                    ValidationResult displayResult = ModelDisplay.validateStructure(buffer, posxxxxxxxxxxxxxxxxx);
                                    if (!displayResult.isValid()) {
                                       return ValidationResult.error("Invalid Display: " + displayResult.error());
                                    }

                                    posxxxxxxxxxxxxxxxxx += ModelDisplay.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxxx);
                                 }

                                 if ((nullBits[3] & 8) != 0) {
                                    v = buffer.getIntLE(offset + 252);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for Rail");
                                    }

                                    int posxxxxxxxxxxxxxxxxx = offset + 276 + v;
                                    ValidationResult railResult = RailConfig.validateStructure(buffer, posxxxxxxxxxxxxxxxxx);
                                    if (!railResult.isValid()) {
                                       return ValidationResult.error("Invalid Rail: " + railResult.error());
                                    }

                                    posxxxxxxxxxxxxxxxxx += RailConfig.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxxx);
                                 }

                                 if ((nullBits[3] & 16) != 0) {
                                    v = buffer.getIntLE(offset + 256);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for Interactions");
                                    }

                                    int posxxxxxxxxxxxxxxxxx = offset + 276 + v;
                                    int interactionsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxx);
                                    if (interactionsCount < 0) {
                                       return ValidationResult.error("Invalid dictionary count for Interactions");
                                    }

                                    if (interactionsCount > 4096000) {
                                       return ValidationResult.error("Interactions exceeds max length 4096000");
                                    }

                                    posxxxxxxxxxxxxxxxxx += VarInt.size(interactionsCount);

                                    for (int i = 0; i < interactionsCount; i++) {
                                       int vxxx = buffer.getByte(posxxxxxxxxxxxxxxxxx) & 255;
                                       if (vxxx >= 25) {
                                          return ValidationResult.error("Invalid InteractionType value for key");
                                       }

                                       posxxxxxxxxxxxxxxxxx = ++posxxxxxxxxxxxxxxxxx + 4;
                                       if (posxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                                          return ValidationResult.error("Buffer overflow reading value");
                                       }
                                    }
                                 }

                                 if ((nullBits[3] & 32) != 0) {
                                    v = buffer.getIntLE(offset + 260);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for States");
                                    }

                                    int posxxxxxxxxxxxxxxxxxx = offset + 276 + v;
                                    int statesCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxx);
                                    if (statesCount < 0) {
                                       return ValidationResult.error("Invalid dictionary count for States");
                                    }

                                    if (statesCount > 4096000) {
                                       return ValidationResult.error("States exceeds max length 4096000");
                                    }

                                    posxxxxxxxxxxxxxxxxxx += VarInt.size(statesCount);

                                    for (int i = 0; i < statesCount; i++) {
                                       int keyLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxx);
                                       if (keyLen < 0) {
                                          return ValidationResult.error("Invalid string length for key");
                                       }

                                       if (keyLen > 4096000) {
                                          return ValidationResult.error("key exceeds max length 4096000");
                                       }

                                       posxxxxxxxxxxxxxxxxxx += VarInt.size(keyLen);
                                       posxxxxxxxxxxxxxxxxxx += keyLen;
                                       if (posxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                                          return ValidationResult.error("Buffer overflow reading key");
                                       }

                                       posxxxxxxxxxxxxxxxxxx += 4;
                                       if (posxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                                          return ValidationResult.error("Buffer overflow reading value");
                                       }
                                    }
                                 }

                                 if ((nullBits[3] & 64) != 0) {
                                    v = buffer.getIntLE(offset + 264);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for TagIndexes");
                                    }

                                    int posxxxxxxxxxxxxxxxxxxx = offset + 276 + v;
                                    int tagIndexesCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxx);
                                    if (tagIndexesCount < 0) {
                                       return ValidationResult.error("Invalid array count for TagIndexes");
                                    }

                                    if (tagIndexesCount > 4096000) {
                                       return ValidationResult.error("TagIndexes exceeds max length 4096000");
                                    }

                                    posxxxxxxxxxxxxxxxxxxx += VarInt.size(tagIndexesCount);
                                    posxxxxxxxxxxxxxxxxxxx += tagIndexesCount * 4;
                                    if (posxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                                       return ValidationResult.error("Buffer overflow reading TagIndexes");
                                    }
                                 }

                                 if ((nullBits[3] & 128) != 0) {
                                    v = buffer.getIntLE(offset + 268);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for Bench");
                                    }

                                    int posxxxxxxxxxxxxxxxxxxxx = offset + 276 + v;
                                    ValidationResult benchResult = Bench.validateStructure(buffer, posxxxxxxxxxxxxxxxxxxxx);
                                    if (!benchResult.isValid()) {
                                       return ValidationResult.error("Invalid Bench: " + benchResult.error());
                                    }

                                    posxxxxxxxxxxxxxxxxxxxx += Bench.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxxxxxx);
                                 }

                                 if ((nullBits[4] & 1) != 0) {
                                    v = buffer.getIntLE(offset + 272);
                                    if (v < 0 || v > buffer.writerIndex() - offset - 276) {
                                       return ValidationResult.error("Invalid offset for ConnectedBlockRuleSet");
                                    }

                                    int posxxxxxxxxxxxxxxxxxxxx = offset + 276 + v;
                                    ValidationResult connectedBlockRuleSetResult = ConnectedBlockRuleSet.validateStructure(buffer, posxxxxxxxxxxxxxxxxxxxx);
                                    if (!connectedBlockRuleSetResult.isValid()) {
                                       return ValidationResult.error("Invalid ConnectedBlockRuleSet: " + connectedBlockRuleSetResult.error());
                                    }

                                    posxxxxxxxxxxxxxxxxxxxx += ConnectedBlockRuleSet.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxxxxxx);
                                 }

                                 return ValidationResult.OK;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public BlockType clone() {
      BlockType copy = new BlockType();
      copy.item = this.item;
      copy.name = this.name;
      copy.unknown = this.unknown;
      copy.drawType = this.drawType;
      copy.material = this.material;
      copy.opacity = this.opacity;
      copy.shaderEffect = this.shaderEffect != null ? Arrays.copyOf(this.shaderEffect, this.shaderEffect.length) : null;
      copy.hitbox = this.hitbox;
      copy.interactionHitbox = this.interactionHitbox;
      copy.model = this.model;
      copy.modelTexture = this.modelTexture != null ? Arrays.stream(this.modelTexture).map(ex -> ex.clone()).toArray(ModelTexture[]::new) : null;
      copy.modelScale = this.modelScale;
      copy.modelAnimation = this.modelAnimation;
      copy.looping = this.looping;
      copy.maxSupportDistance = this.maxSupportDistance;
      copy.blockSupportsRequiredFor = this.blockSupportsRequiredFor;
      if (this.support != null) {
         Map<BlockNeighbor, RequiredBlockFaceSupport[]> m = new HashMap<>();

         for (Entry<BlockNeighbor, RequiredBlockFaceSupport[]> e : this.support.entrySet()) {
            m.put(e.getKey(), Arrays.stream(e.getValue()).map(x -> x.clone()).toArray(RequiredBlockFaceSupport[]::new));
         }

         copy.support = m;
      }

      if (this.supporting != null) {
         Map<BlockNeighbor, BlockFaceSupport[]> m = new HashMap<>();

         for (Entry<BlockNeighbor, BlockFaceSupport[]> e : this.supporting.entrySet()) {
            m.put(e.getKey(), Arrays.stream(e.getValue()).map(x -> x.clone()).toArray(BlockFaceSupport[]::new));
         }

         copy.supporting = m;
      }

      copy.requiresAlphaBlending = this.requiresAlphaBlending;
      copy.cubeTextures = this.cubeTextures != null ? Arrays.stream(this.cubeTextures).map(ex -> ex.clone()).toArray(BlockTextures[]::new) : null;
      copy.cubeSideMaskTexture = this.cubeSideMaskTexture;
      copy.cubeShadingMode = this.cubeShadingMode;
      copy.randomRotation = this.randomRotation;
      copy.variantRotation = this.variantRotation;
      copy.rotationYawPlacementOffset = this.rotationYawPlacementOffset;
      copy.blockSoundSetIndex = this.blockSoundSetIndex;
      copy.physicalMaterialIndex = this.physicalMaterialIndex;
      copy.soundOcclusionOpacity = this.soundOcclusionOpacity;
      copy.ambientSoundEventIndex = this.ambientSoundEventIndex;
      copy.conditionalSounds = this.conditionalSounds != null
         ? Arrays.stream(this.conditionalSounds).map(ex -> ex.clone()).toArray(ConditionalBlockSound[]::new)
         : null;
      copy.particles = this.particles != null ? Arrays.stream(this.particles).map(ex -> ex.clone()).toArray(ModelParticle[]::new) : null;
      copy.blockParticleSetId = this.blockParticleSetId;
      copy.blockBreakingDecalId = this.blockBreakingDecalId;
      copy.particleColor = this.particleColor != null ? this.particleColor.clone() : null;
      copy.textureComputedColor = this.textureComputedColor != null ? this.textureComputedColor.clone() : null;
      copy.light = this.light != null ? this.light.clone() : null;
      copy.tint = this.tint != null ? this.tint.clone() : null;
      copy.biomeTint = this.biomeTint != null ? this.biomeTint.clone() : null;
      copy.group = this.group;
      copy.transitionTexture = this.transitionTexture;
      copy.transitionToGroups = this.transitionToGroups != null ? Arrays.copyOf(this.transitionToGroups, this.transitionToGroups.length) : null;
      copy.movementSettings = this.movementSettings != null ? this.movementSettings.clone() : null;
      copy.flags = this.flags != null ? this.flags.clone() : null;
      copy.interactionHint = this.interactionHint;
      copy.gathering = this.gathering != null ? this.gathering.clone() : null;
      copy.placementSettings = this.placementSettings != null ? this.placementSettings.clone() : null;
      copy.display = this.display != null ? this.display.clone() : null;
      copy.rail = this.rail != null ? this.rail.clone() : null;
      copy.ignoreSupportWhenPlaced = this.ignoreSupportWhenPlaced;
      copy.interactions = this.interactions != null ? new HashMap<>(this.interactions) : null;
      copy.states = this.states != null ? new HashMap<>(this.states) : null;
      copy.transitionToTag = this.transitionToTag;
      copy.tagIndexes = this.tagIndexes != null ? Arrays.copyOf(this.tagIndexes, this.tagIndexes.length) : null;
      copy.bench = this.bench != null ? this.bench.clone() : null;
      copy.connectedBlockRuleSet = this.connectedBlockRuleSet != null ? this.connectedBlockRuleSet.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockType other)
            ? false
            : Objects.equals(this.item, other.item)
               && Objects.equals(this.name, other.name)
               && this.unknown == other.unknown
               && Objects.equals(this.drawType, other.drawType)
               && Objects.equals(this.material, other.material)
               && Objects.equals(this.opacity, other.opacity)
               && Arrays.equals((Object[])this.shaderEffect, (Object[])other.shaderEffect)
               && this.hitbox == other.hitbox
               && this.interactionHitbox == other.interactionHitbox
               && Objects.equals(this.model, other.model)
               && Arrays.equals((Object[])this.modelTexture, (Object[])other.modelTexture)
               && this.modelScale == other.modelScale
               && Objects.equals(this.modelAnimation, other.modelAnimation)
               && this.looping == other.looping
               && this.maxSupportDistance == other.maxSupportDistance
               && Objects.equals(this.blockSupportsRequiredFor, other.blockSupportsRequiredFor)
               && Objects.equals(this.support, other.support)
               && Objects.equals(this.supporting, other.supporting)
               && this.requiresAlphaBlending == other.requiresAlphaBlending
               && Arrays.equals((Object[])this.cubeTextures, (Object[])other.cubeTextures)
               && Objects.equals(this.cubeSideMaskTexture, other.cubeSideMaskTexture)
               && Objects.equals(this.cubeShadingMode, other.cubeShadingMode)
               && Objects.equals(this.randomRotation, other.randomRotation)
               && Objects.equals(this.variantRotation, other.variantRotation)
               && Objects.equals(this.rotationYawPlacementOffset, other.rotationYawPlacementOffset)
               && this.blockSoundSetIndex == other.blockSoundSetIndex
               && this.physicalMaterialIndex == other.physicalMaterialIndex
               && this.soundOcclusionOpacity == other.soundOcclusionOpacity
               && this.ambientSoundEventIndex == other.ambientSoundEventIndex
               && Arrays.equals((Object[])this.conditionalSounds, (Object[])other.conditionalSounds)
               && Arrays.equals((Object[])this.particles, (Object[])other.particles)
               && Objects.equals(this.blockParticleSetId, other.blockParticleSetId)
               && Objects.equals(this.blockBreakingDecalId, other.blockBreakingDecalId)
               && Objects.equals(this.particleColor, other.particleColor)
               && Objects.equals(this.textureComputedColor, other.textureComputedColor)
               && Objects.equals(this.light, other.light)
               && Objects.equals(this.tint, other.tint)
               && Objects.equals(this.biomeTint, other.biomeTint)
               && this.group == other.group
               && Objects.equals(this.transitionTexture, other.transitionTexture)
               && Arrays.equals(this.transitionToGroups, other.transitionToGroups)
               && Objects.equals(this.movementSettings, other.movementSettings)
               && Objects.equals(this.flags, other.flags)
               && Objects.equals(this.interactionHint, other.interactionHint)
               && Objects.equals(this.gathering, other.gathering)
               && Objects.equals(this.placementSettings, other.placementSettings)
               && Objects.equals(this.display, other.display)
               && Objects.equals(this.rail, other.rail)
               && this.ignoreSupportWhenPlaced == other.ignoreSupportWhenPlaced
               && Objects.equals(this.interactions, other.interactions)
               && Objects.equals(this.states, other.states)
               && this.transitionToTag == other.transitionToTag
               && Arrays.equals(this.tagIndexes, other.tagIndexes)
               && Objects.equals(this.bench, other.bench)
               && Objects.equals(this.connectedBlockRuleSet, other.connectedBlockRuleSet);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.item);
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Boolean.hashCode(this.unknown);
      result = 31 * result + Objects.hashCode(this.drawType);
      result = 31 * result + Objects.hashCode(this.material);
      result = 31 * result + Objects.hashCode(this.opacity);
      result = 31 * result + Arrays.hashCode((Object[])this.shaderEffect);
      result = 31 * result + Integer.hashCode(this.hitbox);
      result = 31 * result + Integer.hashCode(this.interactionHitbox);
      result = 31 * result + Objects.hashCode(this.model);
      result = 31 * result + Arrays.hashCode((Object[])this.modelTexture);
      result = 31 * result + Float.hashCode(this.modelScale);
      result = 31 * result + Objects.hashCode(this.modelAnimation);
      result = 31 * result + Boolean.hashCode(this.looping);
      result = 31 * result + Integer.hashCode(this.maxSupportDistance);
      result = 31 * result + Objects.hashCode(this.blockSupportsRequiredFor);
      result = 31 * result + Objects.hashCode(this.support);
      result = 31 * result + Objects.hashCode(this.supporting);
      result = 31 * result + Boolean.hashCode(this.requiresAlphaBlending);
      result = 31 * result + Arrays.hashCode((Object[])this.cubeTextures);
      result = 31 * result + Objects.hashCode(this.cubeSideMaskTexture);
      result = 31 * result + Objects.hashCode(this.cubeShadingMode);
      result = 31 * result + Objects.hashCode(this.randomRotation);
      result = 31 * result + Objects.hashCode(this.variantRotation);
      result = 31 * result + Objects.hashCode(this.rotationYawPlacementOffset);
      result = 31 * result + Integer.hashCode(this.blockSoundSetIndex);
      result = 31 * result + Integer.hashCode(this.physicalMaterialIndex);
      result = 31 * result + Float.hashCode(this.soundOcclusionOpacity);
      result = 31 * result + Integer.hashCode(this.ambientSoundEventIndex);
      result = 31 * result + Arrays.hashCode((Object[])this.conditionalSounds);
      result = 31 * result + Arrays.hashCode((Object[])this.particles);
      result = 31 * result + Objects.hashCode(this.blockParticleSetId);
      result = 31 * result + Objects.hashCode(this.blockBreakingDecalId);
      result = 31 * result + Objects.hashCode(this.particleColor);
      result = 31 * result + Objects.hashCode(this.textureComputedColor);
      result = 31 * result + Objects.hashCode(this.light);
      result = 31 * result + Objects.hashCode(this.tint);
      result = 31 * result + Objects.hashCode(this.biomeTint);
      result = 31 * result + Integer.hashCode(this.group);
      result = 31 * result + Objects.hashCode(this.transitionTexture);
      result = 31 * result + Arrays.hashCode(this.transitionToGroups);
      result = 31 * result + Objects.hashCode(this.movementSettings);
      result = 31 * result + Objects.hashCode(this.flags);
      result = 31 * result + Objects.hashCode(this.interactionHint);
      result = 31 * result + Objects.hashCode(this.gathering);
      result = 31 * result + Objects.hashCode(this.placementSettings);
      result = 31 * result + Objects.hashCode(this.display);
      result = 31 * result + Objects.hashCode(this.rail);
      result = 31 * result + Boolean.hashCode(this.ignoreSupportWhenPlaced);
      result = 31 * result + Objects.hashCode(this.interactions);
      result = 31 * result + Objects.hashCode(this.states);
      result = 31 * result + Integer.hashCode(this.transitionToTag);
      result = 31 * result + Arrays.hashCode(this.tagIndexes);
      result = 31 * result + Objects.hashCode(this.bench);
      return 31 * result + Objects.hashCode(this.connectedBlockRuleSet);
   }
}
