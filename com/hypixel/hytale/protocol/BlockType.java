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

public class BlockType {
   public static final int NULLABLE_BIT_FIELD_SIZE = 4;
   public static final int FIXED_BLOCK_SIZE = 164;
   public static final int VARIABLE_FIELD_COUNT = 24;
   public static final int VARIABLE_BLOCK_START = 260;
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
   public int ambientSoundEventIndex;
   @Nullable
   public ModelParticle[] particles;
   @Nullable
   public String blockParticleSetId;
   @Nullable
   public String blockBreakingDecalId;
   @Nullable
   public Color particleColor;
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
      int ambientSoundEventIndex,
      @Nullable ModelParticle[] particles,
      @Nullable String blockParticleSetId,
      @Nullable String blockBreakingDecalId,
      @Nullable Color particleColor,
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
      this.ambientSoundEventIndex = ambientSoundEventIndex;
      this.particles = particles;
      this.blockParticleSetId = blockParticleSetId;
      this.blockBreakingDecalId = blockBreakingDecalId;
      this.particleColor = particleColor;
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
      this.ambientSoundEventIndex = other.ambientSoundEventIndex;
      this.particles = other.particles;
      this.blockParticleSetId = other.blockParticleSetId;
      this.blockBreakingDecalId = other.blockBreakingDecalId;
      this.particleColor = other.particleColor;
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
      BlockType obj = new BlockType();
      byte[] nullBits = PacketIO.readBytes(buf, offset, 4);
      obj.unknown = buf.getByte(offset + 4) != 0;
      obj.drawType = DrawType.fromValue(buf.getByte(offset + 5));
      obj.material = BlockMaterial.fromValue(buf.getByte(offset + 6));
      obj.opacity = Opacity.fromValue(buf.getByte(offset + 7));
      obj.hitbox = buf.getIntLE(offset + 8);
      obj.interactionHitbox = buf.getIntLE(offset + 12);
      obj.modelScale = buf.getFloatLE(offset + 16);
      obj.looping = buf.getByte(offset + 20) != 0;
      obj.maxSupportDistance = buf.getIntLE(offset + 21);
      obj.blockSupportsRequiredFor = BlockSupportsRequiredForType.fromValue(buf.getByte(offset + 25));
      obj.requiresAlphaBlending = buf.getByte(offset + 26) != 0;
      obj.cubeShadingMode = ShadingMode.fromValue(buf.getByte(offset + 27));
      obj.randomRotation = RandomRotation.fromValue(buf.getByte(offset + 28));
      obj.variantRotation = VariantRotation.fromValue(buf.getByte(offset + 29));
      obj.rotationYawPlacementOffset = Rotation.fromValue(buf.getByte(offset + 30));
      obj.blockSoundSetIndex = buf.getIntLE(offset + 31);
      obj.ambientSoundEventIndex = buf.getIntLE(offset + 35);
      if ((nullBits[0] & 1) != 0) {
         obj.particleColor = Color.deserialize(buf, offset + 39);
      }

      if ((nullBits[0] & 2) != 0) {
         obj.light = ColorLight.deserialize(buf, offset + 42);
      }

      if ((nullBits[0] & 4) != 0) {
         obj.tint = Tint.deserialize(buf, offset + 46);
      }

      if ((nullBits[0] & 8) != 0) {
         obj.biomeTint = Tint.deserialize(buf, offset + 70);
      }

      obj.group = buf.getIntLE(offset + 94);
      if ((nullBits[0] & 16) != 0) {
         obj.movementSettings = BlockMovementSettings.deserialize(buf, offset + 98);
      }

      if ((nullBits[0] & 32) != 0) {
         obj.flags = BlockFlags.deserialize(buf, offset + 140);
      }

      if ((nullBits[0] & 64) != 0) {
         obj.placementSettings = BlockPlacementSettings.deserialize(buf, offset + 142);
      }

      obj.ignoreSupportWhenPlaced = buf.getByte(offset + 159) != 0;
      obj.transitionToTag = buf.getIntLE(offset + 160);
      if ((nullBits[0] & 128) != 0) {
         int varPos0 = offset + 260 + buf.getIntLE(offset + 164);
         int itemLen = VarInt.peek(buf, varPos0);
         if (itemLen < 0) {
            throw ProtocolException.negativeLength("Item", itemLen);
         }

         if (itemLen > 4096000) {
            throw ProtocolException.stringTooLong("Item", itemLen, 4096000);
         }

         obj.item = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits[1] & 1) != 0) {
         int varPos1 = offset + 260 + buf.getIntLE(offset + 168);
         int nameLen = VarInt.peek(buf, varPos1);
         if (nameLen < 0) {
            throw ProtocolException.negativeLength("Name", nameLen);
         }

         if (nameLen > 4096000) {
            throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
         }

         obj.name = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits[1] & 2) != 0) {
         int varPos2 = offset + 260 + buf.getIntLE(offset + 172);
         int shaderEffectCount = VarInt.peek(buf, varPos2);
         if (shaderEffectCount < 0) {
            throw ProtocolException.negativeLength("ShaderEffect", shaderEffectCount);
         }

         if (shaderEffectCount > 4096000) {
            throw ProtocolException.arrayTooLong("ShaderEffect", shaderEffectCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
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

      if ((nullBits[1] & 4) != 0) {
         int varPos3 = offset + 260 + buf.getIntLE(offset + 176);
         int modelLen = VarInt.peek(buf, varPos3);
         if (modelLen < 0) {
            throw ProtocolException.negativeLength("Model", modelLen);
         }

         if (modelLen > 4096000) {
            throw ProtocolException.stringTooLong("Model", modelLen, 4096000);
         }

         obj.model = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
      }

      if ((nullBits[1] & 8) != 0) {
         int varPos4 = offset + 260 + buf.getIntLE(offset + 180);
         int modelTextureCount = VarInt.peek(buf, varPos4);
         if (modelTextureCount < 0) {
            throw ProtocolException.negativeLength("ModelTexture", modelTextureCount);
         }

         if (modelTextureCount > 4096000) {
            throw ProtocolException.arrayTooLong("ModelTexture", modelTextureCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos4);
         if (varPos4 + varIntLen + modelTextureCount * 5L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("ModelTexture", varPos4 + varIntLen + modelTextureCount * 5, buf.readableBytes());
         }

         obj.modelTexture = new ModelTexture[modelTextureCount];
         int elemPos = varPos4 + varIntLen;

         for (int i = 0; i < modelTextureCount; i++) {
            obj.modelTexture[i] = ModelTexture.deserialize(buf, elemPos);
            elemPos += ModelTexture.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits[1] & 16) != 0) {
         int varPos5 = offset + 260 + buf.getIntLE(offset + 184);
         int modelAnimationLen = VarInt.peek(buf, varPos5);
         if (modelAnimationLen < 0) {
            throw ProtocolException.negativeLength("ModelAnimation", modelAnimationLen);
         }

         if (modelAnimationLen > 4096000) {
            throw ProtocolException.stringTooLong("ModelAnimation", modelAnimationLen, 4096000);
         }

         obj.modelAnimation = PacketIO.readVarString(buf, varPos5, PacketIO.UTF8);
      }

      if ((nullBits[1] & 32) != 0) {
         int varPos6 = offset + 260 + buf.getIntLE(offset + 188);
         int supportCount = VarInt.peek(buf, varPos6);
         if (supportCount < 0) {
            throw ProtocolException.negativeLength("Support", supportCount);
         }

         if (supportCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Support", supportCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos6);
         obj.support = new HashMap<>(supportCount);
         int dictPos = varPos6 + varIntLen;

         for (int i = 0; i < supportCount; i++) {
            BlockNeighbor key = BlockNeighbor.fromValue(buf.getByte(dictPos));
            int valLen = VarInt.peek(buf, ++dictPos);
            if (valLen < 0) {
               throw ProtocolException.negativeLength("val", valLen);
            }

            if (valLen > 64) {
               throw ProtocolException.arrayTooLong("val", valLen, 64);
            }

            int valVarLen = VarInt.length(buf, dictPos);
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

      if ((nullBits[1] & 64) != 0) {
         int varPos7 = offset + 260 + buf.getIntLE(offset + 192);
         int supportingCount = VarInt.peek(buf, varPos7);
         if (supportingCount < 0) {
            throw ProtocolException.negativeLength("Supporting", supportingCount);
         }

         if (supportingCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Supporting", supportingCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos7);
         obj.supporting = new HashMap<>(supportingCount);
         int dictPos = varPos7 + varIntLen;

         for (int i = 0; i < supportingCount; i++) {
            BlockNeighbor keyx = BlockNeighbor.fromValue(buf.getByte(dictPos));
            int valLenx = VarInt.peek(buf, ++dictPos);
            if (valLenx < 0) {
               throw ProtocolException.negativeLength("val", valLenx);
            }

            if (valLenx > 64) {
               throw ProtocolException.arrayTooLong("val", valLenx, 64);
            }

            int valVarLenx = VarInt.length(buf, dictPos);
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

      if ((nullBits[1] & 128) != 0) {
         int varPos8 = offset + 260 + buf.getIntLE(offset + 196);
         int cubeTexturesCount = VarInt.peek(buf, varPos8);
         if (cubeTexturesCount < 0) {
            throw ProtocolException.negativeLength("CubeTextures", cubeTexturesCount);
         }

         if (cubeTexturesCount > 4096000) {
            throw ProtocolException.arrayTooLong("CubeTextures", cubeTexturesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos8);
         if (varPos8 + varIntLen + cubeTexturesCount * 5L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("CubeTextures", varPos8 + varIntLen + cubeTexturesCount * 5, buf.readableBytes());
         }

         obj.cubeTextures = new BlockTextures[cubeTexturesCount];
         int elemPos = varPos8 + varIntLen;

         for (int i = 0; i < cubeTexturesCount; i++) {
            obj.cubeTextures[i] = BlockTextures.deserialize(buf, elemPos);
            elemPos += BlockTextures.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits[2] & 1) != 0) {
         int varPos9 = offset + 260 + buf.getIntLE(offset + 200);
         int cubeSideMaskTextureLen = VarInt.peek(buf, varPos9);
         if (cubeSideMaskTextureLen < 0) {
            throw ProtocolException.negativeLength("CubeSideMaskTexture", cubeSideMaskTextureLen);
         }

         if (cubeSideMaskTextureLen > 4096000) {
            throw ProtocolException.stringTooLong("CubeSideMaskTexture", cubeSideMaskTextureLen, 4096000);
         }

         obj.cubeSideMaskTexture = PacketIO.readVarString(buf, varPos9, PacketIO.UTF8);
      }

      if ((nullBits[2] & 2) != 0) {
         int varPos10 = offset + 260 + buf.getIntLE(offset + 204);
         int particlesCount = VarInt.peek(buf, varPos10);
         if (particlesCount < 0) {
            throw ProtocolException.negativeLength("Particles", particlesCount);
         }

         if (particlesCount > 4096000) {
            throw ProtocolException.arrayTooLong("Particles", particlesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos10);
         if (varPos10 + varIntLen + particlesCount * 34L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Particles", varPos10 + varIntLen + particlesCount * 34, buf.readableBytes());
         }

         obj.particles = new ModelParticle[particlesCount];
         int elemPos = varPos10 + varIntLen;

         for (int i = 0; i < particlesCount; i++) {
            obj.particles[i] = ModelParticle.deserialize(buf, elemPos);
            elemPos += ModelParticle.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits[2] & 4) != 0) {
         int varPos11 = offset + 260 + buf.getIntLE(offset + 208);
         int blockParticleSetIdLen = VarInt.peek(buf, varPos11);
         if (blockParticleSetIdLen < 0) {
            throw ProtocolException.negativeLength("BlockParticleSetId", blockParticleSetIdLen);
         }

         if (blockParticleSetIdLen > 4096000) {
            throw ProtocolException.stringTooLong("BlockParticleSetId", blockParticleSetIdLen, 4096000);
         }

         obj.blockParticleSetId = PacketIO.readVarString(buf, varPos11, PacketIO.UTF8);
      }

      if ((nullBits[2] & 8) != 0) {
         int varPos12 = offset + 260 + buf.getIntLE(offset + 212);
         int blockBreakingDecalIdLen = VarInt.peek(buf, varPos12);
         if (blockBreakingDecalIdLen < 0) {
            throw ProtocolException.negativeLength("BlockBreakingDecalId", blockBreakingDecalIdLen);
         }

         if (blockBreakingDecalIdLen > 4096000) {
            throw ProtocolException.stringTooLong("BlockBreakingDecalId", blockBreakingDecalIdLen, 4096000);
         }

         obj.blockBreakingDecalId = PacketIO.readVarString(buf, varPos12, PacketIO.UTF8);
      }

      if ((nullBits[2] & 16) != 0) {
         int varPos13 = offset + 260 + buf.getIntLE(offset + 216);
         int transitionTextureLen = VarInt.peek(buf, varPos13);
         if (transitionTextureLen < 0) {
            throw ProtocolException.negativeLength("TransitionTexture", transitionTextureLen);
         }

         if (transitionTextureLen > 4096000) {
            throw ProtocolException.stringTooLong("TransitionTexture", transitionTextureLen, 4096000);
         }

         obj.transitionTexture = PacketIO.readVarString(buf, varPos13, PacketIO.UTF8);
      }

      if ((nullBits[2] & 32) != 0) {
         int varPos14 = offset + 260 + buf.getIntLE(offset + 220);
         int transitionToGroupsCount = VarInt.peek(buf, varPos14);
         if (transitionToGroupsCount < 0) {
            throw ProtocolException.negativeLength("TransitionToGroups", transitionToGroupsCount);
         }

         if (transitionToGroupsCount > 4096000) {
            throw ProtocolException.arrayTooLong("TransitionToGroups", transitionToGroupsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos14);
         if (varPos14 + varIntLen + transitionToGroupsCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("TransitionToGroups", varPos14 + varIntLen + transitionToGroupsCount * 4, buf.readableBytes());
         }

         obj.transitionToGroups = new int[transitionToGroupsCount];

         for (int i = 0; i < transitionToGroupsCount; i++) {
            obj.transitionToGroups[i] = buf.getIntLE(varPos14 + varIntLen + i * 4);
         }
      }

      if ((nullBits[2] & 64) != 0) {
         int varPos15 = offset + 260 + buf.getIntLE(offset + 224);
         int interactionHintLen = VarInt.peek(buf, varPos15);
         if (interactionHintLen < 0) {
            throw ProtocolException.negativeLength("InteractionHint", interactionHintLen);
         }

         if (interactionHintLen > 4096000) {
            throw ProtocolException.stringTooLong("InteractionHint", interactionHintLen, 4096000);
         }

         obj.interactionHint = PacketIO.readVarString(buf, varPos15, PacketIO.UTF8);
      }

      if ((nullBits[2] & 128) != 0) {
         int varPos16 = offset + 260 + buf.getIntLE(offset + 228);
         obj.gathering = BlockGathering.deserialize(buf, varPos16);
      }

      if ((nullBits[3] & 1) != 0) {
         int varPos17 = offset + 260 + buf.getIntLE(offset + 232);
         obj.display = ModelDisplay.deserialize(buf, varPos17);
      }

      if ((nullBits[3] & 2) != 0) {
         int varPos18 = offset + 260 + buf.getIntLE(offset + 236);
         obj.rail = RailConfig.deserialize(buf, varPos18);
      }

      if ((nullBits[3] & 4) != 0) {
         int varPos19 = offset + 260 + buf.getIntLE(offset + 240);
         int interactionsCount = VarInt.peek(buf, varPos19);
         if (interactionsCount < 0) {
            throw ProtocolException.negativeLength("Interactions", interactionsCount);
         }

         if (interactionsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Interactions", interactionsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos19);
         obj.interactions = new HashMap<>(interactionsCount);
         int dictPos = varPos19 + varIntLen;

         for (int i = 0; i < interactionsCount; i++) {
            InteractionType keyxx = InteractionType.fromValue(buf.getByte(dictPos));
            int val = buf.getIntLE(++dictPos);
            dictPos += 4;
            if (obj.interactions.put(keyxx, val) != null) {
               throw ProtocolException.duplicateKey("interactions", keyxx);
            }
         }
      }

      if ((nullBits[3] & 8) != 0) {
         int varPos20 = offset + 260 + buf.getIntLE(offset + 244);
         int statesCount = VarInt.peek(buf, varPos20);
         if (statesCount < 0) {
            throw ProtocolException.negativeLength("States", statesCount);
         }

         if (statesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("States", statesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos20);
         obj.states = new HashMap<>(statesCount);
         int dictPos = varPos20 + varIntLen;

         for (int ix = 0; ix < statesCount; ix++) {
            int keyLen = VarInt.peek(buf, dictPos);
            if (keyLen < 0) {
               throw ProtocolException.negativeLength("key", keyLen);
            }

            if (keyLen > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLen, 4096000);
            }

            int keyVarLen = VarInt.length(buf, dictPos);
            String keyxx = PacketIO.readVarString(buf, dictPos);
            dictPos += keyVarLen + keyLen;
            int val = buf.getIntLE(dictPos);
            dictPos += 4;
            if (obj.states.put(keyxx, val) != null) {
               throw ProtocolException.duplicateKey("states", keyxx);
            }
         }
      }

      if ((nullBits[3] & 16) != 0) {
         int varPos21 = offset + 260 + buf.getIntLE(offset + 248);
         int tagIndexesCount = VarInt.peek(buf, varPos21);
         if (tagIndexesCount < 0) {
            throw ProtocolException.negativeLength("TagIndexes", tagIndexesCount);
         }

         if (tagIndexesCount > 4096000) {
            throw ProtocolException.arrayTooLong("TagIndexes", tagIndexesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos21);
         if (varPos21 + varIntLen + tagIndexesCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("TagIndexes", varPos21 + varIntLen + tagIndexesCount * 4, buf.readableBytes());
         }

         obj.tagIndexes = new int[tagIndexesCount];

         for (int ix = 0; ix < tagIndexesCount; ix++) {
            obj.tagIndexes[ix] = buf.getIntLE(varPos21 + varIntLen + ix * 4);
         }
      }

      if ((nullBits[3] & 32) != 0) {
         int varPos22 = offset + 260 + buf.getIntLE(offset + 252);
         obj.bench = Bench.deserialize(buf, varPos22);
      }

      if ((nullBits[3] & 64) != 0) {
         int varPos23 = offset + 260 + buf.getIntLE(offset + 256);
         obj.connectedBlockRuleSet = ConnectedBlockRuleSet.deserialize(buf, varPos23);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 4);
      int maxEnd = 260;
      if ((nullBits[0] & 128) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 164);
         int pos0 = offset + 260 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 168);
         int pos1 = offset + 260 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[1] & 2) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 172);
         int pos2 = offset + 260 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + arrLen * 1;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[1] & 4) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 176);
         int pos3 = offset + 260 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[1] & 8) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 180);
         int pos4 = offset + 260 + fieldOffset4;
         int arrLen = VarInt.peek(buf, pos4);
         pos4 += VarInt.length(buf, pos4);

         for (int i = 0; i < arrLen; i++) {
            pos4 += ModelTexture.computeBytesConsumed(buf, pos4);
         }

         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[1] & 16) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 184);
         int pos5 = offset + 260 + fieldOffset5;
         int sl = VarInt.peek(buf, pos5);
         pos5 += VarInt.length(buf, pos5) + sl;
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[1] & 32) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 188);
         int pos6 = offset + 260 + fieldOffset6;
         int dictLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.length(buf, pos6);

         for (int i = 0; i < dictLen; i++) {
            int al = VarInt.peek(buf, ++pos6);
            pos6 += VarInt.length(buf, pos6);

            for (int j = 0; j < al; j++) {
               pos6 += RequiredBlockFaceSupport.computeBytesConsumed(buf, pos6);
            }
         }

         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[1] & 64) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 192);
         int pos7 = offset + 260 + fieldOffset7;
         int dictLen = VarInt.peek(buf, pos7);
         pos7 += VarInt.length(buf, pos7);

         for (int i = 0; i < dictLen; i++) {
            int al = VarInt.peek(buf, ++pos7);
            pos7 += VarInt.length(buf, pos7);

            for (int j = 0; j < al; j++) {
               pos7 += BlockFaceSupport.computeBytesConsumed(buf, pos7);
            }
         }

         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      if ((nullBits[1] & 128) != 0) {
         int fieldOffset8 = buf.getIntLE(offset + 196);
         int pos8 = offset + 260 + fieldOffset8;
         int arrLen = VarInt.peek(buf, pos8);
         pos8 += VarInt.length(buf, pos8);

         for (int i = 0; i < arrLen; i++) {
            pos8 += BlockTextures.computeBytesConsumed(buf, pos8);
         }

         if (pos8 - offset > maxEnd) {
            maxEnd = pos8 - offset;
         }
      }

      if ((nullBits[2] & 1) != 0) {
         int fieldOffset9 = buf.getIntLE(offset + 200);
         int pos9 = offset + 260 + fieldOffset9;
         int sl = VarInt.peek(buf, pos9);
         pos9 += VarInt.length(buf, pos9) + sl;
         if (pos9 - offset > maxEnd) {
            maxEnd = pos9 - offset;
         }
      }

      if ((nullBits[2] & 2) != 0) {
         int fieldOffset10 = buf.getIntLE(offset + 204);
         int pos10 = offset + 260 + fieldOffset10;
         int arrLen = VarInt.peek(buf, pos10);
         pos10 += VarInt.length(buf, pos10);

         for (int i = 0; i < arrLen; i++) {
            pos10 += ModelParticle.computeBytesConsumed(buf, pos10);
         }

         if (pos10 - offset > maxEnd) {
            maxEnd = pos10 - offset;
         }
      }

      if ((nullBits[2] & 4) != 0) {
         int fieldOffset11 = buf.getIntLE(offset + 208);
         int pos11 = offset + 260 + fieldOffset11;
         int sl = VarInt.peek(buf, pos11);
         pos11 += VarInt.length(buf, pos11) + sl;
         if (pos11 - offset > maxEnd) {
            maxEnd = pos11 - offset;
         }
      }

      if ((nullBits[2] & 8) != 0) {
         int fieldOffset12 = buf.getIntLE(offset + 212);
         int pos12 = offset + 260 + fieldOffset12;
         int sl = VarInt.peek(buf, pos12);
         pos12 += VarInt.length(buf, pos12) + sl;
         if (pos12 - offset > maxEnd) {
            maxEnd = pos12 - offset;
         }
      }

      if ((nullBits[2] & 16) != 0) {
         int fieldOffset13 = buf.getIntLE(offset + 216);
         int pos13 = offset + 260 + fieldOffset13;
         int sl = VarInt.peek(buf, pos13);
         pos13 += VarInt.length(buf, pos13) + sl;
         if (pos13 - offset > maxEnd) {
            maxEnd = pos13 - offset;
         }
      }

      if ((nullBits[2] & 32) != 0) {
         int fieldOffset14 = buf.getIntLE(offset + 220);
         int pos14 = offset + 260 + fieldOffset14;
         int arrLen = VarInt.peek(buf, pos14);
         pos14 += VarInt.length(buf, pos14) + arrLen * 4;
         if (pos14 - offset > maxEnd) {
            maxEnd = pos14 - offset;
         }
      }

      if ((nullBits[2] & 64) != 0) {
         int fieldOffset15 = buf.getIntLE(offset + 224);
         int pos15 = offset + 260 + fieldOffset15;
         int sl = VarInt.peek(buf, pos15);
         pos15 += VarInt.length(buf, pos15) + sl;
         if (pos15 - offset > maxEnd) {
            maxEnd = pos15 - offset;
         }
      }

      if ((nullBits[2] & 128) != 0) {
         int fieldOffset16 = buf.getIntLE(offset + 228);
         int pos16 = offset + 260 + fieldOffset16;
         pos16 += BlockGathering.computeBytesConsumed(buf, pos16);
         if (pos16 - offset > maxEnd) {
            maxEnd = pos16 - offset;
         }
      }

      if ((nullBits[3] & 1) != 0) {
         int fieldOffset17 = buf.getIntLE(offset + 232);
         int pos17 = offset + 260 + fieldOffset17;
         pos17 += ModelDisplay.computeBytesConsumed(buf, pos17);
         if (pos17 - offset > maxEnd) {
            maxEnd = pos17 - offset;
         }
      }

      if ((nullBits[3] & 2) != 0) {
         int fieldOffset18 = buf.getIntLE(offset + 236);
         int pos18 = offset + 260 + fieldOffset18;
         pos18 += RailConfig.computeBytesConsumed(buf, pos18);
         if (pos18 - offset > maxEnd) {
            maxEnd = pos18 - offset;
         }
      }

      if ((nullBits[3] & 4) != 0) {
         int fieldOffset19 = buf.getIntLE(offset + 240);
         int pos19 = offset + 260 + fieldOffset19;
         int dictLen = VarInt.peek(buf, pos19);
         pos19 += VarInt.length(buf, pos19);

         for (int i = 0; i < dictLen; i++) {
            pos19 = ++pos19 + 4;
         }

         if (pos19 - offset > maxEnd) {
            maxEnd = pos19 - offset;
         }
      }

      if ((nullBits[3] & 8) != 0) {
         int fieldOffset20 = buf.getIntLE(offset + 244);
         int pos20 = offset + 260 + fieldOffset20;
         int dictLen = VarInt.peek(buf, pos20);
         pos20 += VarInt.length(buf, pos20);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos20);
            pos20 += VarInt.length(buf, pos20) + sl;
            pos20 += 4;
         }

         if (pos20 - offset > maxEnd) {
            maxEnd = pos20 - offset;
         }
      }

      if ((nullBits[3] & 16) != 0) {
         int fieldOffset21 = buf.getIntLE(offset + 248);
         int pos21 = offset + 260 + fieldOffset21;
         int arrLen = VarInt.peek(buf, pos21);
         pos21 += VarInt.length(buf, pos21) + arrLen * 4;
         if (pos21 - offset > maxEnd) {
            maxEnd = pos21 - offset;
         }
      }

      if ((nullBits[3] & 32) != 0) {
         int fieldOffset22 = buf.getIntLE(offset + 252);
         int pos22 = offset + 260 + fieldOffset22;
         pos22 += Bench.computeBytesConsumed(buf, pos22);
         if (pos22 - offset > maxEnd) {
            maxEnd = pos22 - offset;
         }
      }

      if ((nullBits[3] & 64) != 0) {
         int fieldOffset23 = buf.getIntLE(offset + 256);
         int pos23 = offset + 260 + fieldOffset23;
         pos23 += ConnectedBlockRuleSet.computeBytesConsumed(buf, pos23);
         if (pos23 - offset > maxEnd) {
            maxEnd = pos23 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[4];
      if (this.particleColor != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.light != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.tint != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.biomeTint != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.movementSettings != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.flags != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.placementSettings != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.item != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.name != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      if (this.shaderEffect != null) {
         nullBits[1] = (byte)(nullBits[1] | 2);
      }

      if (this.model != null) {
         nullBits[1] = (byte)(nullBits[1] | 4);
      }

      if (this.modelTexture != null) {
         nullBits[1] = (byte)(nullBits[1] | 8);
      }

      if (this.modelAnimation != null) {
         nullBits[1] = (byte)(nullBits[1] | 16);
      }

      if (this.support != null) {
         nullBits[1] = (byte)(nullBits[1] | 32);
      }

      if (this.supporting != null) {
         nullBits[1] = (byte)(nullBits[1] | 64);
      }

      if (this.cubeTextures != null) {
         nullBits[1] = (byte)(nullBits[1] | 128);
      }

      if (this.cubeSideMaskTexture != null) {
         nullBits[2] = (byte)(nullBits[2] | 1);
      }

      if (this.particles != null) {
         nullBits[2] = (byte)(nullBits[2] | 2);
      }

      if (this.blockParticleSetId != null) {
         nullBits[2] = (byte)(nullBits[2] | 4);
      }

      if (this.blockBreakingDecalId != null) {
         nullBits[2] = (byte)(nullBits[2] | 8);
      }

      if (this.transitionTexture != null) {
         nullBits[2] = (byte)(nullBits[2] | 16);
      }

      if (this.transitionToGroups != null) {
         nullBits[2] = (byte)(nullBits[2] | 32);
      }

      if (this.interactionHint != null) {
         nullBits[2] = (byte)(nullBits[2] | 64);
      }

      if (this.gathering != null) {
         nullBits[2] = (byte)(nullBits[2] | 128);
      }

      if (this.display != null) {
         nullBits[3] = (byte)(nullBits[3] | 1);
      }

      if (this.rail != null) {
         nullBits[3] = (byte)(nullBits[3] | 2);
      }

      if (this.interactions != null) {
         nullBits[3] = (byte)(nullBits[3] | 4);
      }

      if (this.states != null) {
         nullBits[3] = (byte)(nullBits[3] | 8);
      }

      if (this.tagIndexes != null) {
         nullBits[3] = (byte)(nullBits[3] | 16);
      }

      if (this.bench != null) {
         nullBits[3] = (byte)(nullBits[3] | 32);
      }

      if (this.connectedBlockRuleSet != null) {
         nullBits[3] = (byte)(nullBits[3] | 64);
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
      buf.writeIntLE(this.ambientSoundEventIndex);
      if (this.particleColor != null) {
         this.particleColor.serialize(buf);
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

   public int computeSize() {
      int size = 260;
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
      if (buffer.readableBytes() - offset < 260) {
         return ValidationResult.error("Buffer too small: expected at least 260 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 4);
         if ((nullBits[0] & 128) != 0) {
            int itemOffset = buffer.getIntLE(offset + 164);
            if (itemOffset < 0) {
               return ValidationResult.error("Invalid offset for Item");
            }

            int pos = offset + 260 + itemOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Item");
            }

            int itemLen = VarInt.peek(buffer, pos);
            if (itemLen < 0) {
               return ValidationResult.error("Invalid string length for Item");
            }

            if (itemLen > 4096000) {
               return ValidationResult.error("Item exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += itemLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Item");
            }
         }

         if ((nullBits[1] & 1) != 0) {
            int nameOffset = buffer.getIntLE(offset + 168);
            if (nameOffset < 0) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int posx = offset + 260 + nameOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Name");
            }

            int nameLen = VarInt.peek(buffer, posx);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += nameLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits[1] & 2) != 0) {
            int shaderEffectOffset = buffer.getIntLE(offset + 172);
            if (shaderEffectOffset < 0) {
               return ValidationResult.error("Invalid offset for ShaderEffect");
            }

            int posxx = offset + 260 + shaderEffectOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ShaderEffect");
            }

            int shaderEffectCount = VarInt.peek(buffer, posxx);
            if (shaderEffectCount < 0) {
               return ValidationResult.error("Invalid array count for ShaderEffect");
            }

            if (shaderEffectCount > 4096000) {
               return ValidationResult.error("ShaderEffect exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += shaderEffectCount * 1;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ShaderEffect");
            }
         }

         if ((nullBits[1] & 4) != 0) {
            int modelOffset = buffer.getIntLE(offset + 176);
            if (modelOffset < 0) {
               return ValidationResult.error("Invalid offset for Model");
            }

            int posxxx = offset + 260 + modelOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Model");
            }

            int modelLen = VarInt.peek(buffer, posxxx);
            if (modelLen < 0) {
               return ValidationResult.error("Invalid string length for Model");
            }

            if (modelLen > 4096000) {
               return ValidationResult.error("Model exceeds max length 4096000");
            }

            posxxx += VarInt.length(buffer, posxxx);
            posxxx += modelLen;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Model");
            }
         }

         if ((nullBits[1] & 8) != 0) {
            int modelTextureOffset = buffer.getIntLE(offset + 180);
            if (modelTextureOffset < 0) {
               return ValidationResult.error("Invalid offset for ModelTexture");
            }

            int posxxxx = offset + 260 + modelTextureOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ModelTexture");
            }

            int modelTextureCount = VarInt.peek(buffer, posxxxx);
            if (modelTextureCount < 0) {
               return ValidationResult.error("Invalid array count for ModelTexture");
            }

            if (modelTextureCount > 4096000) {
               return ValidationResult.error("ModelTexture exceeds max length 4096000");
            }

            posxxxx += VarInt.length(buffer, posxxxx);

            for (int i = 0; i < modelTextureCount; i++) {
               ValidationResult structResult = ModelTexture.validateStructure(buffer, posxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ModelTexture in ModelTexture[" + i + "]: " + structResult.error());
               }

               posxxxx += ModelTexture.computeBytesConsumed(buffer, posxxxx);
            }
         }

         if ((nullBits[1] & 16) != 0) {
            int modelAnimationOffset = buffer.getIntLE(offset + 184);
            if (modelAnimationOffset < 0) {
               return ValidationResult.error("Invalid offset for ModelAnimation");
            }

            int posxxxxx = offset + 260 + modelAnimationOffset;
            if (posxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ModelAnimation");
            }

            int modelAnimationLen = VarInt.peek(buffer, posxxxxx);
            if (modelAnimationLen < 0) {
               return ValidationResult.error("Invalid string length for ModelAnimation");
            }

            if (modelAnimationLen > 4096000) {
               return ValidationResult.error("ModelAnimation exceeds max length 4096000");
            }

            posxxxxx += VarInt.length(buffer, posxxxxx);
            posxxxxx += modelAnimationLen;
            if (posxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ModelAnimation");
            }
         }

         if ((nullBits[1] & 32) != 0) {
            int supportOffset = buffer.getIntLE(offset + 188);
            if (supportOffset < 0) {
               return ValidationResult.error("Invalid offset for Support");
            }

            int posxxxxxx = offset + 260 + supportOffset;
            if (posxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Support");
            }

            int supportCount = VarInt.peek(buffer, posxxxxxx);
            if (supportCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Support");
            }

            if (supportCount > 4096000) {
               return ValidationResult.error("Support exceeds max length 4096000");
            }

            posxxxxxx += VarInt.length(buffer, posxxxxxx);

            for (int i = 0; i < supportCount; i++) {
               int valueArrCount = VarInt.peek(buffer, ++posxxxxxx);
               if (valueArrCount < 0) {
                  return ValidationResult.error("Invalid array count for value");
               }

               posxxxxxx += VarInt.length(buffer, posxxxxxx);

               for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                  posxxxxxx += RequiredBlockFaceSupport.computeBytesConsumed(buffer, posxxxxxx);
               }
            }
         }

         if ((nullBits[1] & 64) != 0) {
            int supportingOffset = buffer.getIntLE(offset + 192);
            if (supportingOffset < 0) {
               return ValidationResult.error("Invalid offset for Supporting");
            }

            int posxxxxxxx = offset + 260 + supportingOffset;
            if (posxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Supporting");
            }

            int supportingCount = VarInt.peek(buffer, posxxxxxxx);
            if (supportingCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Supporting");
            }

            if (supportingCount > 4096000) {
               return ValidationResult.error("Supporting exceeds max length 4096000");
            }

            posxxxxxxx += VarInt.length(buffer, posxxxxxxx);

            for (int i = 0; i < supportingCount; i++) {
               int valueArrCount = VarInt.peek(buffer, ++posxxxxxxx);
               if (valueArrCount < 0) {
                  return ValidationResult.error("Invalid array count for value");
               }

               posxxxxxxx += VarInt.length(buffer, posxxxxxxx);

               for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                  posxxxxxxx += BlockFaceSupport.computeBytesConsumed(buffer, posxxxxxxx);
               }
            }
         }

         if ((nullBits[1] & 128) != 0) {
            int cubeTexturesOffset = buffer.getIntLE(offset + 196);
            if (cubeTexturesOffset < 0) {
               return ValidationResult.error("Invalid offset for CubeTextures");
            }

            int posxxxxxxxx = offset + 260 + cubeTexturesOffset;
            if (posxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for CubeTextures");
            }

            int cubeTexturesCount = VarInt.peek(buffer, posxxxxxxxx);
            if (cubeTexturesCount < 0) {
               return ValidationResult.error("Invalid array count for CubeTextures");
            }

            if (cubeTexturesCount > 4096000) {
               return ValidationResult.error("CubeTextures exceeds max length 4096000");
            }

            posxxxxxxxx += VarInt.length(buffer, posxxxxxxxx);

            for (int i = 0; i < cubeTexturesCount; i++) {
               ValidationResult structResult = BlockTextures.validateStructure(buffer, posxxxxxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid BlockTextures in CubeTextures[" + i + "]: " + structResult.error());
               }

               posxxxxxxxx += BlockTextures.computeBytesConsumed(buffer, posxxxxxxxx);
            }
         }

         if ((nullBits[2] & 1) != 0) {
            int cubeSideMaskTextureOffset = buffer.getIntLE(offset + 200);
            if (cubeSideMaskTextureOffset < 0) {
               return ValidationResult.error("Invalid offset for CubeSideMaskTexture");
            }

            int posxxxxxxxxx = offset + 260 + cubeSideMaskTextureOffset;
            if (posxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for CubeSideMaskTexture");
            }

            int cubeSideMaskTextureLen = VarInt.peek(buffer, posxxxxxxxxx);
            if (cubeSideMaskTextureLen < 0) {
               return ValidationResult.error("Invalid string length for CubeSideMaskTexture");
            }

            if (cubeSideMaskTextureLen > 4096000) {
               return ValidationResult.error("CubeSideMaskTexture exceeds max length 4096000");
            }

            posxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxx);
            posxxxxxxxxx += cubeSideMaskTextureLen;
            if (posxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading CubeSideMaskTexture");
            }
         }

         if ((nullBits[2] & 2) != 0) {
            int particlesOffset = buffer.getIntLE(offset + 204);
            if (particlesOffset < 0) {
               return ValidationResult.error("Invalid offset for Particles");
            }

            int posxxxxxxxxxx = offset + 260 + particlesOffset;
            if (posxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Particles");
            }

            int particlesCount = VarInt.peek(buffer, posxxxxxxxxxx);
            if (particlesCount < 0) {
               return ValidationResult.error("Invalid array count for Particles");
            }

            if (particlesCount > 4096000) {
               return ValidationResult.error("Particles exceeds max length 4096000");
            }

            posxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxx);

            for (int i = 0; i < particlesCount; i++) {
               ValidationResult structResult = ModelParticle.validateStructure(buffer, posxxxxxxxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ModelParticle in Particles[" + i + "]: " + structResult.error());
               }

               posxxxxxxxxxx += ModelParticle.computeBytesConsumed(buffer, posxxxxxxxxxx);
            }
         }

         if ((nullBits[2] & 4) != 0) {
            int blockParticleSetIdOffset = buffer.getIntLE(offset + 208);
            if (blockParticleSetIdOffset < 0) {
               return ValidationResult.error("Invalid offset for BlockParticleSetId");
            }

            int posxxxxxxxxxxx = offset + 260 + blockParticleSetIdOffset;
            if (posxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for BlockParticleSetId");
            }

            int blockParticleSetIdLen = VarInt.peek(buffer, posxxxxxxxxxxx);
            if (blockParticleSetIdLen < 0) {
               return ValidationResult.error("Invalid string length for BlockParticleSetId");
            }

            if (blockParticleSetIdLen > 4096000) {
               return ValidationResult.error("BlockParticleSetId exceeds max length 4096000");
            }

            posxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxx);
            posxxxxxxxxxxx += blockParticleSetIdLen;
            if (posxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading BlockParticleSetId");
            }
         }

         if ((nullBits[2] & 8) != 0) {
            int blockBreakingDecalIdOffset = buffer.getIntLE(offset + 212);
            if (blockBreakingDecalIdOffset < 0) {
               return ValidationResult.error("Invalid offset for BlockBreakingDecalId");
            }

            int posxxxxxxxxxxxx = offset + 260 + blockBreakingDecalIdOffset;
            if (posxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for BlockBreakingDecalId");
            }

            int blockBreakingDecalIdLen = VarInt.peek(buffer, posxxxxxxxxxxxx);
            if (blockBreakingDecalIdLen < 0) {
               return ValidationResult.error("Invalid string length for BlockBreakingDecalId");
            }

            if (blockBreakingDecalIdLen > 4096000) {
               return ValidationResult.error("BlockBreakingDecalId exceeds max length 4096000");
            }

            posxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxx);
            posxxxxxxxxxxxx += blockBreakingDecalIdLen;
            if (posxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading BlockBreakingDecalId");
            }
         }

         if ((nullBits[2] & 16) != 0) {
            int transitionTextureOffset = buffer.getIntLE(offset + 216);
            if (transitionTextureOffset < 0) {
               return ValidationResult.error("Invalid offset for TransitionTexture");
            }

            int posxxxxxxxxxxxxx = offset + 260 + transitionTextureOffset;
            if (posxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for TransitionTexture");
            }

            int transitionTextureLen = VarInt.peek(buffer, posxxxxxxxxxxxxx);
            if (transitionTextureLen < 0) {
               return ValidationResult.error("Invalid string length for TransitionTexture");
            }

            if (transitionTextureLen > 4096000) {
               return ValidationResult.error("TransitionTexture exceeds max length 4096000");
            }

            posxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxx);
            posxxxxxxxxxxxxx += transitionTextureLen;
            if (posxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading TransitionTexture");
            }
         }

         if ((nullBits[2] & 32) != 0) {
            int transitionToGroupsOffset = buffer.getIntLE(offset + 220);
            if (transitionToGroupsOffset < 0) {
               return ValidationResult.error("Invalid offset for TransitionToGroups");
            }

            int posxxxxxxxxxxxxxx = offset + 260 + transitionToGroupsOffset;
            if (posxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for TransitionToGroups");
            }

            int transitionToGroupsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxx);
            if (transitionToGroupsCount < 0) {
               return ValidationResult.error("Invalid array count for TransitionToGroups");
            }

            if (transitionToGroupsCount > 4096000) {
               return ValidationResult.error("TransitionToGroups exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxx);
            posxxxxxxxxxxxxxx += transitionToGroupsCount * 4;
            if (posxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading TransitionToGroups");
            }
         }

         if ((nullBits[2] & 64) != 0) {
            int interactionHintOffset = buffer.getIntLE(offset + 224);
            if (interactionHintOffset < 0) {
               return ValidationResult.error("Invalid offset for InteractionHint");
            }

            int posxxxxxxxxxxxxxxx = offset + 260 + interactionHintOffset;
            if (posxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for InteractionHint");
            }

            int interactionHintLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxx);
            if (interactionHintLen < 0) {
               return ValidationResult.error("Invalid string length for InteractionHint");
            }

            if (interactionHintLen > 4096000) {
               return ValidationResult.error("InteractionHint exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxx);
            posxxxxxxxxxxxxxxx += interactionHintLen;
            if (posxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading InteractionHint");
            }
         }

         if ((nullBits[2] & 128) != 0) {
            int gatheringOffset = buffer.getIntLE(offset + 228);
            if (gatheringOffset < 0) {
               return ValidationResult.error("Invalid offset for Gathering");
            }

            int posxxxxxxxxxxxxxxxx = offset + 260 + gatheringOffset;
            if (posxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Gathering");
            }

            ValidationResult gatheringResult = BlockGathering.validateStructure(buffer, posxxxxxxxxxxxxxxxx);
            if (!gatheringResult.isValid()) {
               return ValidationResult.error("Invalid Gathering: " + gatheringResult.error());
            }

            posxxxxxxxxxxxxxxxx += BlockGathering.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxx);
         }

         if ((nullBits[3] & 1) != 0) {
            int displayOffset = buffer.getIntLE(offset + 232);
            if (displayOffset < 0) {
               return ValidationResult.error("Invalid offset for Display");
            }

            int posxxxxxxxxxxxxxxxxx = offset + 260 + displayOffset;
            if (posxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Display");
            }

            ValidationResult displayResult = ModelDisplay.validateStructure(buffer, posxxxxxxxxxxxxxxxxx);
            if (!displayResult.isValid()) {
               return ValidationResult.error("Invalid Display: " + displayResult.error());
            }

            posxxxxxxxxxxxxxxxxx += ModelDisplay.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxxx);
         }

         if ((nullBits[3] & 2) != 0) {
            int railOffset = buffer.getIntLE(offset + 236);
            if (railOffset < 0) {
               return ValidationResult.error("Invalid offset for Rail");
            }

            int posxxxxxxxxxxxxxxxxxx = offset + 260 + railOffset;
            if (posxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Rail");
            }

            ValidationResult railResult = RailConfig.validateStructure(buffer, posxxxxxxxxxxxxxxxxxx);
            if (!railResult.isValid()) {
               return ValidationResult.error("Invalid Rail: " + railResult.error());
            }

            posxxxxxxxxxxxxxxxxxx += RailConfig.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxxxx);
         }

         if ((nullBits[3] & 4) != 0) {
            int interactionsOffset = buffer.getIntLE(offset + 240);
            if (interactionsOffset < 0) {
               return ValidationResult.error("Invalid offset for Interactions");
            }

            int posxxxxxxxxxxxxxxxxxxx = offset + 260 + interactionsOffset;
            if (posxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Interactions");
            }

            int interactionsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxx);
            if (interactionsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Interactions");
            }

            if (interactionsCount > 4096000) {
               return ValidationResult.error("Interactions exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxx);

            for (int i = 0; i < interactionsCount; i++) {
               posxxxxxxxxxxxxxxxxxxx = ++posxxxxxxxxxxxxxxxxxxx + 4;
               if (posxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         if ((nullBits[3] & 8) != 0) {
            int statesOffset = buffer.getIntLE(offset + 244);
            if (statesOffset < 0) {
               return ValidationResult.error("Invalid offset for States");
            }

            int posxxxxxxxxxxxxxxxxxxxx = offset + 260 + statesOffset;
            if (posxxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for States");
            }

            int statesCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxx);
            if (statesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for States");
            }

            if (statesCount > 4096000) {
               return ValidationResult.error("States exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxxx);

            for (int ix = 0; ix < statesCount; ix++) {
               int keyLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxx);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posxxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxxx);
               posxxxxxxxxxxxxxxxxxxxx += keyLen;
               if (posxxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxxxxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         if ((nullBits[3] & 16) != 0) {
            int tagIndexesOffset = buffer.getIntLE(offset + 248);
            if (tagIndexesOffset < 0) {
               return ValidationResult.error("Invalid offset for TagIndexes");
            }

            int posxxxxxxxxxxxxxxxxxxxxx = offset + 260 + tagIndexesOffset;
            if (posxxxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for TagIndexes");
            }

            int tagIndexesCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxxx);
            if (tagIndexesCount < 0) {
               return ValidationResult.error("Invalid array count for TagIndexes");
            }

            if (tagIndexesCount > 4096000) {
               return ValidationResult.error("TagIndexes exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxxxx);
            posxxxxxxxxxxxxxxxxxxxxx += tagIndexesCount * 4;
            if (posxxxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading TagIndexes");
            }
         }

         if ((nullBits[3] & 32) != 0) {
            int benchOffset = buffer.getIntLE(offset + 252);
            if (benchOffset < 0) {
               return ValidationResult.error("Invalid offset for Bench");
            }

            int posxxxxxxxxxxxxxxxxxxxxxx = offset + 260 + benchOffset;
            if (posxxxxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Bench");
            }

            ValidationResult benchResult = Bench.validateStructure(buffer, posxxxxxxxxxxxxxxxxxxxxxx);
            if (!benchResult.isValid()) {
               return ValidationResult.error("Invalid Bench: " + benchResult.error());
            }

            posxxxxxxxxxxxxxxxxxxxxxx += Bench.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxxxxxxxx);
         }

         if ((nullBits[3] & 64) != 0) {
            int connectedBlockRuleSetOffset = buffer.getIntLE(offset + 256);
            if (connectedBlockRuleSetOffset < 0) {
               return ValidationResult.error("Invalid offset for ConnectedBlockRuleSet");
            }

            int posxxxxxxxxxxxxxxxxxxxxxxx = offset + 260 + connectedBlockRuleSetOffset;
            if (posxxxxxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ConnectedBlockRuleSet");
            }

            ValidationResult connectedBlockRuleSetResult = ConnectedBlockRuleSet.validateStructure(buffer, posxxxxxxxxxxxxxxxxxxxxxxx);
            if (!connectedBlockRuleSetResult.isValid()) {
               return ValidationResult.error("Invalid ConnectedBlockRuleSet: " + connectedBlockRuleSetResult.error());
            }

            posxxxxxxxxxxxxxxxxxxxxxxx += ConnectedBlockRuleSet.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxxxxxxxxx);
         }

         return ValidationResult.OK;
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
      copy.ambientSoundEventIndex = this.ambientSoundEventIndex;
      copy.particles = this.particles != null ? Arrays.stream(this.particles).map(ex -> ex.clone()).toArray(ModelParticle[]::new) : null;
      copy.blockParticleSetId = this.blockParticleSetId;
      copy.blockBreakingDecalId = this.blockBreakingDecalId;
      copy.particleColor = this.particleColor != null ? this.particleColor.clone() : null;
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
               && this.ambientSoundEventIndex == other.ambientSoundEventIndex
               && Arrays.equals((Object[])this.particles, (Object[])other.particles)
               && Objects.equals(this.blockParticleSetId, other.blockParticleSetId)
               && Objects.equals(this.blockBreakingDecalId, other.blockBreakingDecalId)
               && Objects.equals(this.particleColor, other.particleColor)
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
      result = 31 * result + Integer.hashCode(this.ambientSoundEventIndex);
      result = 31 * result + Arrays.hashCode((Object[])this.particles);
      result = 31 * result + Objects.hashCode(this.blockParticleSetId);
      result = 31 * result + Objects.hashCode(this.blockBreakingDecalId);
      result = 31 * result + Objects.hashCode(this.particleColor);
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
