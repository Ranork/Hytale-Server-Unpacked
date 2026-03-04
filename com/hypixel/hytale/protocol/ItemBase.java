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

public class ItemBase {
   public static final int NULLABLE_BIT_FIELD_SIZE = 4;
   public static final int FIXED_BLOCK_SIZE = 147;
   public static final int VARIABLE_FIELD_COUNT = 26;
   public static final int VARIABLE_BLOCK_START = 251;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public String model;
   public float scale;
   @Nullable
   public String texture;
   @Nullable
   public String animation;
   @Nullable
   public String playerAnimationsId;
   public boolean usePlayerAnimations;
   public int maxStack;
   public int reticleIndex;
   @Nullable
   public String icon;
   @Nullable
   public AssetIconProperties iconProperties;
   @Nullable
   public ItemTranslationProperties translationProperties;
   public int itemLevel;
   public int qualityIndex;
   @Nullable
   public ItemResourceType[] resourceTypes;
   public boolean consumable;
   public boolean variant;
   public int blockId;
   @Nullable
   public ItemTool tool;
   @Nullable
   public ItemWeapon weapon;
   @Nullable
   public ItemArmor armor;
   @Nullable
   public ItemGlider gliderConfig;
   @Nullable
   public ItemUtility utility;
   @Nullable
   public BlockSelectorToolData blockSelectorTool;
   @Nullable
   public ItemBuilderToolData builderToolData;
   @Nullable
   public ItemEntityConfig itemEntity;
   @Nullable
   public String set;
   @Nullable
   public String[] categories;
   @Nullable
   public ModelParticle[] particles;
   @Nullable
   public ModelParticle[] firstPersonParticles;
   @Nullable
   public ModelTrail[] trails;
   @Nullable
   public ColorLight light;
   public double durability;
   public int soundEventIndex;
   public int itemSoundSetIndex;
   @Nullable
   public Map<InteractionType, Integer> interactions;
   @Nullable
   public Map<String, Integer> interactionVars;
   @Nullable
   public InteractionConfiguration interactionConfig;
   @Nullable
   public String droppedItemAnimation;
   @Nullable
   public int[] tagIndexes;
   @Nullable
   public Map<Integer, ItemAppearanceCondition[]> itemAppearanceConditions;
   @Nullable
   public int[] displayEntityStatsHUD;
   @Nullable
   public ItemPullbackConfiguration pullbackConfig;
   public boolean clipsGeometry;
   public boolean renderDeployablePreview;

   public ItemBase() {
   }

   public ItemBase(
      @Nullable String id,
      @Nullable String model,
      float scale,
      @Nullable String texture,
      @Nullable String animation,
      @Nullable String playerAnimationsId,
      boolean usePlayerAnimations,
      int maxStack,
      int reticleIndex,
      @Nullable String icon,
      @Nullable AssetIconProperties iconProperties,
      @Nullable ItemTranslationProperties translationProperties,
      int itemLevel,
      int qualityIndex,
      @Nullable ItemResourceType[] resourceTypes,
      boolean consumable,
      boolean variant,
      int blockId,
      @Nullable ItemTool tool,
      @Nullable ItemWeapon weapon,
      @Nullable ItemArmor armor,
      @Nullable ItemGlider gliderConfig,
      @Nullable ItemUtility utility,
      @Nullable BlockSelectorToolData blockSelectorTool,
      @Nullable ItemBuilderToolData builderToolData,
      @Nullable ItemEntityConfig itemEntity,
      @Nullable String set,
      @Nullable String[] categories,
      @Nullable ModelParticle[] particles,
      @Nullable ModelParticle[] firstPersonParticles,
      @Nullable ModelTrail[] trails,
      @Nullable ColorLight light,
      double durability,
      int soundEventIndex,
      int itemSoundSetIndex,
      @Nullable Map<InteractionType, Integer> interactions,
      @Nullable Map<String, Integer> interactionVars,
      @Nullable InteractionConfiguration interactionConfig,
      @Nullable String droppedItemAnimation,
      @Nullable int[] tagIndexes,
      @Nullable Map<Integer, ItemAppearanceCondition[]> itemAppearanceConditions,
      @Nullable int[] displayEntityStatsHUD,
      @Nullable ItemPullbackConfiguration pullbackConfig,
      boolean clipsGeometry,
      boolean renderDeployablePreview
   ) {
      this.id = id;
      this.model = model;
      this.scale = scale;
      this.texture = texture;
      this.animation = animation;
      this.playerAnimationsId = playerAnimationsId;
      this.usePlayerAnimations = usePlayerAnimations;
      this.maxStack = maxStack;
      this.reticleIndex = reticleIndex;
      this.icon = icon;
      this.iconProperties = iconProperties;
      this.translationProperties = translationProperties;
      this.itemLevel = itemLevel;
      this.qualityIndex = qualityIndex;
      this.resourceTypes = resourceTypes;
      this.consumable = consumable;
      this.variant = variant;
      this.blockId = blockId;
      this.tool = tool;
      this.weapon = weapon;
      this.armor = armor;
      this.gliderConfig = gliderConfig;
      this.utility = utility;
      this.blockSelectorTool = blockSelectorTool;
      this.builderToolData = builderToolData;
      this.itemEntity = itemEntity;
      this.set = set;
      this.categories = categories;
      this.particles = particles;
      this.firstPersonParticles = firstPersonParticles;
      this.trails = trails;
      this.light = light;
      this.durability = durability;
      this.soundEventIndex = soundEventIndex;
      this.itemSoundSetIndex = itemSoundSetIndex;
      this.interactions = interactions;
      this.interactionVars = interactionVars;
      this.interactionConfig = interactionConfig;
      this.droppedItemAnimation = droppedItemAnimation;
      this.tagIndexes = tagIndexes;
      this.itemAppearanceConditions = itemAppearanceConditions;
      this.displayEntityStatsHUD = displayEntityStatsHUD;
      this.pullbackConfig = pullbackConfig;
      this.clipsGeometry = clipsGeometry;
      this.renderDeployablePreview = renderDeployablePreview;
   }

   public ItemBase(@Nonnull ItemBase other) {
      this.id = other.id;
      this.model = other.model;
      this.scale = other.scale;
      this.texture = other.texture;
      this.animation = other.animation;
      this.playerAnimationsId = other.playerAnimationsId;
      this.usePlayerAnimations = other.usePlayerAnimations;
      this.maxStack = other.maxStack;
      this.reticleIndex = other.reticleIndex;
      this.icon = other.icon;
      this.iconProperties = other.iconProperties;
      this.translationProperties = other.translationProperties;
      this.itemLevel = other.itemLevel;
      this.qualityIndex = other.qualityIndex;
      this.resourceTypes = other.resourceTypes;
      this.consumable = other.consumable;
      this.variant = other.variant;
      this.blockId = other.blockId;
      this.tool = other.tool;
      this.weapon = other.weapon;
      this.armor = other.armor;
      this.gliderConfig = other.gliderConfig;
      this.utility = other.utility;
      this.blockSelectorTool = other.blockSelectorTool;
      this.builderToolData = other.builderToolData;
      this.itemEntity = other.itemEntity;
      this.set = other.set;
      this.categories = other.categories;
      this.particles = other.particles;
      this.firstPersonParticles = other.firstPersonParticles;
      this.trails = other.trails;
      this.light = other.light;
      this.durability = other.durability;
      this.soundEventIndex = other.soundEventIndex;
      this.itemSoundSetIndex = other.itemSoundSetIndex;
      this.interactions = other.interactions;
      this.interactionVars = other.interactionVars;
      this.interactionConfig = other.interactionConfig;
      this.droppedItemAnimation = other.droppedItemAnimation;
      this.tagIndexes = other.tagIndexes;
      this.itemAppearanceConditions = other.itemAppearanceConditions;
      this.displayEntityStatsHUD = other.displayEntityStatsHUD;
      this.pullbackConfig = other.pullbackConfig;
      this.clipsGeometry = other.clipsGeometry;
      this.renderDeployablePreview = other.renderDeployablePreview;
   }

   @Nonnull
   public static ItemBase deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemBase obj = new ItemBase();
      byte[] nullBits = PacketIO.readBytes(buf, offset, 4);
      obj.scale = buf.getFloatLE(offset + 4);
      obj.usePlayerAnimations = buf.getByte(offset + 8) != 0;
      obj.maxStack = buf.getIntLE(offset + 9);
      obj.reticleIndex = buf.getIntLE(offset + 13);
      if ((nullBits[0] & 1) != 0) {
         obj.iconProperties = AssetIconProperties.deserialize(buf, offset + 17);
      }

      obj.itemLevel = buf.getIntLE(offset + 42);
      obj.qualityIndex = buf.getIntLE(offset + 46);
      obj.consumable = buf.getByte(offset + 50) != 0;
      obj.variant = buf.getByte(offset + 51) != 0;
      obj.blockId = buf.getIntLE(offset + 52);
      if ((nullBits[0] & 2) != 0) {
         obj.gliderConfig = ItemGlider.deserialize(buf, offset + 56);
      }

      if ((nullBits[0] & 4) != 0) {
         obj.blockSelectorTool = BlockSelectorToolData.deserialize(buf, offset + 72);
      }

      if ((nullBits[0] & 8) != 0) {
         obj.light = ColorLight.deserialize(buf, offset + 76);
      }

      obj.durability = buf.getDoubleLE(offset + 80);
      obj.soundEventIndex = buf.getIntLE(offset + 88);
      obj.itemSoundSetIndex = buf.getIntLE(offset + 92);
      if ((nullBits[0] & 16) != 0) {
         obj.pullbackConfig = ItemPullbackConfiguration.deserialize(buf, offset + 96);
      }

      obj.clipsGeometry = buf.getByte(offset + 145) != 0;
      obj.renderDeployablePreview = buf.getByte(offset + 146) != 0;
      if ((nullBits[0] & 32) != 0) {
         int varPos0 = offset + 251 + buf.getIntLE(offset + 147);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits[0] & 64) != 0) {
         int varPos1 = offset + 251 + buf.getIntLE(offset + 151);
         int modelLen = VarInt.peek(buf, varPos1);
         if (modelLen < 0) {
            throw ProtocolException.negativeLength("Model", modelLen);
         }

         if (modelLen > 4096000) {
            throw ProtocolException.stringTooLong("Model", modelLen, 4096000);
         }

         obj.model = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits[0] & 128) != 0) {
         int varPos2 = offset + 251 + buf.getIntLE(offset + 155);
         int textureLen = VarInt.peek(buf, varPos2);
         if (textureLen < 0) {
            throw ProtocolException.negativeLength("Texture", textureLen);
         }

         if (textureLen > 4096000) {
            throw ProtocolException.stringTooLong("Texture", textureLen, 4096000);
         }

         obj.texture = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      if ((nullBits[1] & 1) != 0) {
         int varPos3 = offset + 251 + buf.getIntLE(offset + 159);
         int animationLen = VarInt.peek(buf, varPos3);
         if (animationLen < 0) {
            throw ProtocolException.negativeLength("Animation", animationLen);
         }

         if (animationLen > 4096000) {
            throw ProtocolException.stringTooLong("Animation", animationLen, 4096000);
         }

         obj.animation = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
      }

      if ((nullBits[1] & 2) != 0) {
         int varPos4 = offset + 251 + buf.getIntLE(offset + 163);
         int playerAnimationsIdLen = VarInt.peek(buf, varPos4);
         if (playerAnimationsIdLen < 0) {
            throw ProtocolException.negativeLength("PlayerAnimationsId", playerAnimationsIdLen);
         }

         if (playerAnimationsIdLen > 4096000) {
            throw ProtocolException.stringTooLong("PlayerAnimationsId", playerAnimationsIdLen, 4096000);
         }

         obj.playerAnimationsId = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
      }

      if ((nullBits[1] & 4) != 0) {
         int varPos5 = offset + 251 + buf.getIntLE(offset + 167);
         int iconLen = VarInt.peek(buf, varPos5);
         if (iconLen < 0) {
            throw ProtocolException.negativeLength("Icon", iconLen);
         }

         if (iconLen > 4096000) {
            throw ProtocolException.stringTooLong("Icon", iconLen, 4096000);
         }

         obj.icon = PacketIO.readVarString(buf, varPos5, PacketIO.UTF8);
      }

      if ((nullBits[1] & 8) != 0) {
         int varPos6 = offset + 251 + buf.getIntLE(offset + 171);
         obj.translationProperties = ItemTranslationProperties.deserialize(buf, varPos6);
      }

      if ((nullBits[1] & 16) != 0) {
         int varPos7 = offset + 251 + buf.getIntLE(offset + 175);
         int resourceTypesCount = VarInt.peek(buf, varPos7);
         if (resourceTypesCount < 0) {
            throw ProtocolException.negativeLength("ResourceTypes", resourceTypesCount);
         }

         if (resourceTypesCount > 4096000) {
            throw ProtocolException.arrayTooLong("ResourceTypes", resourceTypesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos7);
         if (varPos7 + varIntLen + resourceTypesCount * 5L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("ResourceTypes", varPos7 + varIntLen + resourceTypesCount * 5, buf.readableBytes());
         }

         obj.resourceTypes = new ItemResourceType[resourceTypesCount];
         int elemPos = varPos7 + varIntLen;

         for (int i = 0; i < resourceTypesCount; i++) {
            obj.resourceTypes[i] = ItemResourceType.deserialize(buf, elemPos);
            elemPos += ItemResourceType.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits[1] & 32) != 0) {
         int varPos8 = offset + 251 + buf.getIntLE(offset + 179);
         obj.tool = ItemTool.deserialize(buf, varPos8);
      }

      if ((nullBits[1] & 64) != 0) {
         int varPos9 = offset + 251 + buf.getIntLE(offset + 183);
         obj.weapon = ItemWeapon.deserialize(buf, varPos9);
      }

      if ((nullBits[1] & 128) != 0) {
         int varPos10 = offset + 251 + buf.getIntLE(offset + 187);
         obj.armor = ItemArmor.deserialize(buf, varPos10);
      }

      if ((nullBits[2] & 1) != 0) {
         int varPos11 = offset + 251 + buf.getIntLE(offset + 191);
         obj.utility = ItemUtility.deserialize(buf, varPos11);
      }

      if ((nullBits[2] & 2) != 0) {
         int varPos12 = offset + 251 + buf.getIntLE(offset + 195);
         obj.builderToolData = ItemBuilderToolData.deserialize(buf, varPos12);
      }

      if ((nullBits[2] & 4) != 0) {
         int varPos13 = offset + 251 + buf.getIntLE(offset + 199);
         obj.itemEntity = ItemEntityConfig.deserialize(buf, varPos13);
      }

      if ((nullBits[2] & 8) != 0) {
         int varPos14 = offset + 251 + buf.getIntLE(offset + 203);
         int setLen = VarInt.peek(buf, varPos14);
         if (setLen < 0) {
            throw ProtocolException.negativeLength("Set", setLen);
         }

         if (setLen > 4096000) {
            throw ProtocolException.stringTooLong("Set", setLen, 4096000);
         }

         obj.set = PacketIO.readVarString(buf, varPos14, PacketIO.UTF8);
      }

      if ((nullBits[2] & 16) != 0) {
         int varPos15 = offset + 251 + buf.getIntLE(offset + 207);
         int categoriesCount = VarInt.peek(buf, varPos15);
         if (categoriesCount < 0) {
            throw ProtocolException.negativeLength("Categories", categoriesCount);
         }

         if (categoriesCount > 4096000) {
            throw ProtocolException.arrayTooLong("Categories", categoriesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos15);
         if (varPos15 + varIntLen + categoriesCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Categories", varPos15 + varIntLen + categoriesCount * 1, buf.readableBytes());
         }

         obj.categories = new String[categoriesCount];
         int elemPos = varPos15 + varIntLen;

         for (int i = 0; i < categoriesCount; i++) {
            int strLen = VarInt.peek(buf, elemPos);
            if (strLen < 0) {
               throw ProtocolException.negativeLength("categories[" + i + "]", strLen);
            }

            if (strLen > 4096000) {
               throw ProtocolException.stringTooLong("categories[" + i + "]", strLen, 4096000);
            }

            int strVarLen = VarInt.length(buf, elemPos);
            obj.categories[i] = PacketIO.readVarString(buf, elemPos);
            elemPos += strVarLen + strLen;
         }
      }

      if ((nullBits[2] & 32) != 0) {
         int varPos16 = offset + 251 + buf.getIntLE(offset + 211);
         int particlesCount = VarInt.peek(buf, varPos16);
         if (particlesCount < 0) {
            throw ProtocolException.negativeLength("Particles", particlesCount);
         }

         if (particlesCount > 4096000) {
            throw ProtocolException.arrayTooLong("Particles", particlesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos16);
         if (varPos16 + varIntLen + particlesCount * 34L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Particles", varPos16 + varIntLen + particlesCount * 34, buf.readableBytes());
         }

         obj.particles = new ModelParticle[particlesCount];
         int elemPos = varPos16 + varIntLen;

         for (int i = 0; i < particlesCount; i++) {
            obj.particles[i] = ModelParticle.deserialize(buf, elemPos);
            elemPos += ModelParticle.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits[2] & 64) != 0) {
         int varPos17 = offset + 251 + buf.getIntLE(offset + 215);
         int firstPersonParticlesCount = VarInt.peek(buf, varPos17);
         if (firstPersonParticlesCount < 0) {
            throw ProtocolException.negativeLength("FirstPersonParticles", firstPersonParticlesCount);
         }

         if (firstPersonParticlesCount > 4096000) {
            throw ProtocolException.arrayTooLong("FirstPersonParticles", firstPersonParticlesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos17);
         if (varPos17 + varIntLen + firstPersonParticlesCount * 34L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("FirstPersonParticles", varPos17 + varIntLen + firstPersonParticlesCount * 34, buf.readableBytes());
         }

         obj.firstPersonParticles = new ModelParticle[firstPersonParticlesCount];
         int elemPos = varPos17 + varIntLen;

         for (int i = 0; i < firstPersonParticlesCount; i++) {
            obj.firstPersonParticles[i] = ModelParticle.deserialize(buf, elemPos);
            elemPos += ModelParticle.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits[2] & 128) != 0) {
         int varPos18 = offset + 251 + buf.getIntLE(offset + 219);
         int trailsCount = VarInt.peek(buf, varPos18);
         if (trailsCount < 0) {
            throw ProtocolException.negativeLength("Trails", trailsCount);
         }

         if (trailsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Trails", trailsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos18);
         if (varPos18 + varIntLen + trailsCount * 27L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Trails", varPos18 + varIntLen + trailsCount * 27, buf.readableBytes());
         }

         obj.trails = new ModelTrail[trailsCount];
         int elemPos = varPos18 + varIntLen;

         for (int i = 0; i < trailsCount; i++) {
            obj.trails[i] = ModelTrail.deserialize(buf, elemPos);
            elemPos += ModelTrail.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits[3] & 1) != 0) {
         int varPos19 = offset + 251 + buf.getIntLE(offset + 223);
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
            InteractionType key = InteractionType.fromValue(buf.getByte(dictPos));
            int val = buf.getIntLE(++dictPos);
            dictPos += 4;
            if (obj.interactions.put(key, val) != null) {
               throw ProtocolException.duplicateKey("interactions", key);
            }
         }
      }

      if ((nullBits[3] & 2) != 0) {
         int varPos20 = offset + 251 + buf.getIntLE(offset + 227);
         int interactionVarsCount = VarInt.peek(buf, varPos20);
         if (interactionVarsCount < 0) {
            throw ProtocolException.negativeLength("InteractionVars", interactionVarsCount);
         }

         if (interactionVarsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("InteractionVars", interactionVarsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos20);
         obj.interactionVars = new HashMap<>(interactionVarsCount);
         int dictPos = varPos20 + varIntLen;

         for (int ix = 0; ix < interactionVarsCount; ix++) {
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
            int val = buf.getIntLE(dictPos);
            dictPos += 4;
            if (obj.interactionVars.put(key, val) != null) {
               throw ProtocolException.duplicateKey("interactionVars", key);
            }
         }
      }

      if ((nullBits[3] & 4) != 0) {
         int varPos21 = offset + 251 + buf.getIntLE(offset + 231);
         obj.interactionConfig = InteractionConfiguration.deserialize(buf, varPos21);
      }

      if ((nullBits[3] & 8) != 0) {
         int varPos22 = offset + 251 + buf.getIntLE(offset + 235);
         int droppedItemAnimationLen = VarInt.peek(buf, varPos22);
         if (droppedItemAnimationLen < 0) {
            throw ProtocolException.negativeLength("DroppedItemAnimation", droppedItemAnimationLen);
         }

         if (droppedItemAnimationLen > 4096000) {
            throw ProtocolException.stringTooLong("DroppedItemAnimation", droppedItemAnimationLen, 4096000);
         }

         obj.droppedItemAnimation = PacketIO.readVarString(buf, varPos22, PacketIO.UTF8);
      }

      if ((nullBits[3] & 16) != 0) {
         int varPos23 = offset + 251 + buf.getIntLE(offset + 239);
         int tagIndexesCount = VarInt.peek(buf, varPos23);
         if (tagIndexesCount < 0) {
            throw ProtocolException.negativeLength("TagIndexes", tagIndexesCount);
         }

         if (tagIndexesCount > 4096000) {
            throw ProtocolException.arrayTooLong("TagIndexes", tagIndexesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos23);
         if (varPos23 + varIntLen + tagIndexesCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("TagIndexes", varPos23 + varIntLen + tagIndexesCount * 4, buf.readableBytes());
         }

         obj.tagIndexes = new int[tagIndexesCount];

         for (int ix = 0; ix < tagIndexesCount; ix++) {
            obj.tagIndexes[ix] = buf.getIntLE(varPos23 + varIntLen + ix * 4);
         }
      }

      if ((nullBits[3] & 32) != 0) {
         int varPos24 = offset + 251 + buf.getIntLE(offset + 243);
         int itemAppearanceConditionsCount = VarInt.peek(buf, varPos24);
         if (itemAppearanceConditionsCount < 0) {
            throw ProtocolException.negativeLength("ItemAppearanceConditions", itemAppearanceConditionsCount);
         }

         if (itemAppearanceConditionsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemAppearanceConditions", itemAppearanceConditionsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos24);
         obj.itemAppearanceConditions = new HashMap<>(itemAppearanceConditionsCount);
         int dictPos = varPos24 + varIntLen;

         for (int ix = 0; ix < itemAppearanceConditionsCount; ix++) {
            int key = buf.getIntLE(dictPos);
            dictPos += 4;
            int valLen = VarInt.peek(buf, dictPos);
            if (valLen < 0) {
               throw ProtocolException.negativeLength("val", valLen);
            }

            if (valLen > 64) {
               throw ProtocolException.arrayTooLong("val", valLen, 64);
            }

            int valVarLen = VarInt.length(buf, dictPos);
            if (dictPos + valVarLen + valLen * 18L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("val", dictPos + valVarLen + valLen * 18, buf.readableBytes());
            }

            dictPos += valVarLen;
            ItemAppearanceCondition[] val = new ItemAppearanceCondition[valLen];

            for (int valIdx = 0; valIdx < valLen; valIdx++) {
               val[valIdx] = ItemAppearanceCondition.deserialize(buf, dictPos);
               dictPos += ItemAppearanceCondition.computeBytesConsumed(buf, dictPos);
            }

            if (obj.itemAppearanceConditions.put(key, val) != null) {
               throw ProtocolException.duplicateKey("itemAppearanceConditions", key);
            }
         }
      }

      if ((nullBits[3] & 64) != 0) {
         int varPos25 = offset + 251 + buf.getIntLE(offset + 247);
         int displayEntityStatsHUDCount = VarInt.peek(buf, varPos25);
         if (displayEntityStatsHUDCount < 0) {
            throw ProtocolException.negativeLength("DisplayEntityStatsHUD", displayEntityStatsHUDCount);
         }

         if (displayEntityStatsHUDCount > 4096000) {
            throw ProtocolException.arrayTooLong("DisplayEntityStatsHUD", displayEntityStatsHUDCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos25);
         if (varPos25 + varIntLen + displayEntityStatsHUDCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("DisplayEntityStatsHUD", varPos25 + varIntLen + displayEntityStatsHUDCount * 4, buf.readableBytes());
         }

         obj.displayEntityStatsHUD = new int[displayEntityStatsHUDCount];

         for (int ix = 0; ix < displayEntityStatsHUDCount; ix++) {
            obj.displayEntityStatsHUD[ix] = buf.getIntLE(varPos25 + varIntLen + ix * 4);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 4);
      int maxEnd = 251;
      if ((nullBits[0] & 32) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 147);
         int pos0 = offset + 251 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[0] & 64) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 151);
         int pos1 = offset + 251 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 155);
         int pos2 = offset + 251 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 159);
         int pos3 = offset + 251 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[1] & 2) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 163);
         int pos4 = offset + 251 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.length(buf, pos4) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[1] & 4) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 167);
         int pos5 = offset + 251 + fieldOffset5;
         int sl = VarInt.peek(buf, pos5);
         pos5 += VarInt.length(buf, pos5) + sl;
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[1] & 8) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 171);
         int pos6 = offset + 251 + fieldOffset6;
         pos6 += ItemTranslationProperties.computeBytesConsumed(buf, pos6);
         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[1] & 16) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 175);
         int pos7 = offset + 251 + fieldOffset7;
         int arrLen = VarInt.peek(buf, pos7);
         pos7 += VarInt.length(buf, pos7);

         for (int i = 0; i < arrLen; i++) {
            pos7 += ItemResourceType.computeBytesConsumed(buf, pos7);
         }

         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      if ((nullBits[1] & 32) != 0) {
         int fieldOffset8 = buf.getIntLE(offset + 179);
         int pos8 = offset + 251 + fieldOffset8;
         pos8 += ItemTool.computeBytesConsumed(buf, pos8);
         if (pos8 - offset > maxEnd) {
            maxEnd = pos8 - offset;
         }
      }

      if ((nullBits[1] & 64) != 0) {
         int fieldOffset9 = buf.getIntLE(offset + 183);
         int pos9 = offset + 251 + fieldOffset9;
         pos9 += ItemWeapon.computeBytesConsumed(buf, pos9);
         if (pos9 - offset > maxEnd) {
            maxEnd = pos9 - offset;
         }
      }

      if ((nullBits[1] & 128) != 0) {
         int fieldOffset10 = buf.getIntLE(offset + 187);
         int pos10 = offset + 251 + fieldOffset10;
         pos10 += ItemArmor.computeBytesConsumed(buf, pos10);
         if (pos10 - offset > maxEnd) {
            maxEnd = pos10 - offset;
         }
      }

      if ((nullBits[2] & 1) != 0) {
         int fieldOffset11 = buf.getIntLE(offset + 191);
         int pos11 = offset + 251 + fieldOffset11;
         pos11 += ItemUtility.computeBytesConsumed(buf, pos11);
         if (pos11 - offset > maxEnd) {
            maxEnd = pos11 - offset;
         }
      }

      if ((nullBits[2] & 2) != 0) {
         int fieldOffset12 = buf.getIntLE(offset + 195);
         int pos12 = offset + 251 + fieldOffset12;
         pos12 += ItemBuilderToolData.computeBytesConsumed(buf, pos12);
         if (pos12 - offset > maxEnd) {
            maxEnd = pos12 - offset;
         }
      }

      if ((nullBits[2] & 4) != 0) {
         int fieldOffset13 = buf.getIntLE(offset + 199);
         int pos13 = offset + 251 + fieldOffset13;
         pos13 += ItemEntityConfig.computeBytesConsumed(buf, pos13);
         if (pos13 - offset > maxEnd) {
            maxEnd = pos13 - offset;
         }
      }

      if ((nullBits[2] & 8) != 0) {
         int fieldOffset14 = buf.getIntLE(offset + 203);
         int pos14 = offset + 251 + fieldOffset14;
         int sl = VarInt.peek(buf, pos14);
         pos14 += VarInt.length(buf, pos14) + sl;
         if (pos14 - offset > maxEnd) {
            maxEnd = pos14 - offset;
         }
      }

      if ((nullBits[2] & 16) != 0) {
         int fieldOffset15 = buf.getIntLE(offset + 207);
         int pos15 = offset + 251 + fieldOffset15;
         int arrLen = VarInt.peek(buf, pos15);
         pos15 += VarInt.length(buf, pos15);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos15);
            pos15 += VarInt.length(buf, pos15) + sl;
         }

         if (pos15 - offset > maxEnd) {
            maxEnd = pos15 - offset;
         }
      }

      if ((nullBits[2] & 32) != 0) {
         int fieldOffset16 = buf.getIntLE(offset + 211);
         int pos16 = offset + 251 + fieldOffset16;
         int arrLen = VarInt.peek(buf, pos16);
         pos16 += VarInt.length(buf, pos16);

         for (int i = 0; i < arrLen; i++) {
            pos16 += ModelParticle.computeBytesConsumed(buf, pos16);
         }

         if (pos16 - offset > maxEnd) {
            maxEnd = pos16 - offset;
         }
      }

      if ((nullBits[2] & 64) != 0) {
         int fieldOffset17 = buf.getIntLE(offset + 215);
         int pos17 = offset + 251 + fieldOffset17;
         int arrLen = VarInt.peek(buf, pos17);
         pos17 += VarInt.length(buf, pos17);

         for (int i = 0; i < arrLen; i++) {
            pos17 += ModelParticle.computeBytesConsumed(buf, pos17);
         }

         if (pos17 - offset > maxEnd) {
            maxEnd = pos17 - offset;
         }
      }

      if ((nullBits[2] & 128) != 0) {
         int fieldOffset18 = buf.getIntLE(offset + 219);
         int pos18 = offset + 251 + fieldOffset18;
         int arrLen = VarInt.peek(buf, pos18);
         pos18 += VarInt.length(buf, pos18);

         for (int i = 0; i < arrLen; i++) {
            pos18 += ModelTrail.computeBytesConsumed(buf, pos18);
         }

         if (pos18 - offset > maxEnd) {
            maxEnd = pos18 - offset;
         }
      }

      if ((nullBits[3] & 1) != 0) {
         int fieldOffset19 = buf.getIntLE(offset + 223);
         int pos19 = offset + 251 + fieldOffset19;
         int dictLen = VarInt.peek(buf, pos19);
         pos19 += VarInt.length(buf, pos19);

         for (int i = 0; i < dictLen; i++) {
            pos19 = ++pos19 + 4;
         }

         if (pos19 - offset > maxEnd) {
            maxEnd = pos19 - offset;
         }
      }

      if ((nullBits[3] & 2) != 0) {
         int fieldOffset20 = buf.getIntLE(offset + 227);
         int pos20 = offset + 251 + fieldOffset20;
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

      if ((nullBits[3] & 4) != 0) {
         int fieldOffset21 = buf.getIntLE(offset + 231);
         int pos21 = offset + 251 + fieldOffset21;
         pos21 += InteractionConfiguration.computeBytesConsumed(buf, pos21);
         if (pos21 - offset > maxEnd) {
            maxEnd = pos21 - offset;
         }
      }

      if ((nullBits[3] & 8) != 0) {
         int fieldOffset22 = buf.getIntLE(offset + 235);
         int pos22 = offset + 251 + fieldOffset22;
         int sl = VarInt.peek(buf, pos22);
         pos22 += VarInt.length(buf, pos22) + sl;
         if (pos22 - offset > maxEnd) {
            maxEnd = pos22 - offset;
         }
      }

      if ((nullBits[3] & 16) != 0) {
         int fieldOffset23 = buf.getIntLE(offset + 239);
         int pos23 = offset + 251 + fieldOffset23;
         int arrLen = VarInt.peek(buf, pos23);
         pos23 += VarInt.length(buf, pos23) + arrLen * 4;
         if (pos23 - offset > maxEnd) {
            maxEnd = pos23 - offset;
         }
      }

      if ((nullBits[3] & 32) != 0) {
         int fieldOffset24 = buf.getIntLE(offset + 243);
         int pos24 = offset + 251 + fieldOffset24;
         int dictLen = VarInt.peek(buf, pos24);
         pos24 += VarInt.length(buf, pos24);

         for (int i = 0; i < dictLen; i++) {
            pos24 += 4;
            int al = VarInt.peek(buf, pos24);
            pos24 += VarInt.length(buf, pos24);

            for (int j = 0; j < al; j++) {
               pos24 += ItemAppearanceCondition.computeBytesConsumed(buf, pos24);
            }
         }

         if (pos24 - offset > maxEnd) {
            maxEnd = pos24 - offset;
         }
      }

      if ((nullBits[3] & 64) != 0) {
         int fieldOffset25 = buf.getIntLE(offset + 247);
         int pos25 = offset + 251 + fieldOffset25;
         int arrLen = VarInt.peek(buf, pos25);
         pos25 += VarInt.length(buf, pos25) + arrLen * 4;
         if (pos25 - offset > maxEnd) {
            maxEnd = pos25 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[4];
      if (this.iconProperties != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.gliderConfig != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.blockSelectorTool != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.light != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.pullbackConfig != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.id != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.model != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.texture != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.animation != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      if (this.playerAnimationsId != null) {
         nullBits[1] = (byte)(nullBits[1] | 2);
      }

      if (this.icon != null) {
         nullBits[1] = (byte)(nullBits[1] | 4);
      }

      if (this.translationProperties != null) {
         nullBits[1] = (byte)(nullBits[1] | 8);
      }

      if (this.resourceTypes != null) {
         nullBits[1] = (byte)(nullBits[1] | 16);
      }

      if (this.tool != null) {
         nullBits[1] = (byte)(nullBits[1] | 32);
      }

      if (this.weapon != null) {
         nullBits[1] = (byte)(nullBits[1] | 64);
      }

      if (this.armor != null) {
         nullBits[1] = (byte)(nullBits[1] | 128);
      }

      if (this.utility != null) {
         nullBits[2] = (byte)(nullBits[2] | 1);
      }

      if (this.builderToolData != null) {
         nullBits[2] = (byte)(nullBits[2] | 2);
      }

      if (this.itemEntity != null) {
         nullBits[2] = (byte)(nullBits[2] | 4);
      }

      if (this.set != null) {
         nullBits[2] = (byte)(nullBits[2] | 8);
      }

      if (this.categories != null) {
         nullBits[2] = (byte)(nullBits[2] | 16);
      }

      if (this.particles != null) {
         nullBits[2] = (byte)(nullBits[2] | 32);
      }

      if (this.firstPersonParticles != null) {
         nullBits[2] = (byte)(nullBits[2] | 64);
      }

      if (this.trails != null) {
         nullBits[2] = (byte)(nullBits[2] | 128);
      }

      if (this.interactions != null) {
         nullBits[3] = (byte)(nullBits[3] | 1);
      }

      if (this.interactionVars != null) {
         nullBits[3] = (byte)(nullBits[3] | 2);
      }

      if (this.interactionConfig != null) {
         nullBits[3] = (byte)(nullBits[3] | 4);
      }

      if (this.droppedItemAnimation != null) {
         nullBits[3] = (byte)(nullBits[3] | 8);
      }

      if (this.tagIndexes != null) {
         nullBits[3] = (byte)(nullBits[3] | 16);
      }

      if (this.itemAppearanceConditions != null) {
         nullBits[3] = (byte)(nullBits[3] | 32);
      }

      if (this.displayEntityStatsHUD != null) {
         nullBits[3] = (byte)(nullBits[3] | 64);
      }

      buf.writeBytes(nullBits);
      buf.writeFloatLE(this.scale);
      buf.writeByte(this.usePlayerAnimations ? 1 : 0);
      buf.writeIntLE(this.maxStack);
      buf.writeIntLE(this.reticleIndex);
      if (this.iconProperties != null) {
         this.iconProperties.serialize(buf);
      } else {
         buf.writeZero(25);
      }

      buf.writeIntLE(this.itemLevel);
      buf.writeIntLE(this.qualityIndex);
      buf.writeByte(this.consumable ? 1 : 0);
      buf.writeByte(this.variant ? 1 : 0);
      buf.writeIntLE(this.blockId);
      if (this.gliderConfig != null) {
         this.gliderConfig.serialize(buf);
      } else {
         buf.writeZero(16);
      }

      if (this.blockSelectorTool != null) {
         this.blockSelectorTool.serialize(buf);
      } else {
         buf.writeZero(4);
      }

      if (this.light != null) {
         this.light.serialize(buf);
      } else {
         buf.writeZero(4);
      }

      buf.writeDoubleLE(this.durability);
      buf.writeIntLE(this.soundEventIndex);
      buf.writeIntLE(this.itemSoundSetIndex);
      if (this.pullbackConfig != null) {
         this.pullbackConfig.serialize(buf);
      } else {
         buf.writeZero(49);
      }

      buf.writeByte(this.clipsGeometry ? 1 : 0);
      buf.writeByte(this.renderDeployablePreview ? 1 : 0);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int modelOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int textureOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int animationOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int playerAnimationsIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int iconOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int translationPropertiesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int resourceTypesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int toolOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int weaponOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int armorOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int utilityOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int builderToolDataOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int itemEntityOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int setOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int categoriesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int particlesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int firstPersonParticlesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int trailsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int interactionsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int interactionVarsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int interactionConfigOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int droppedItemAnimationOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int tagIndexesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int itemAppearanceConditionsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int displayEntityStatsHUDOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.model != null) {
         buf.setIntLE(modelOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.model, 4096000);
      } else {
         buf.setIntLE(modelOffsetSlot, -1);
      }

      if (this.texture != null) {
         buf.setIntLE(textureOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.texture, 4096000);
      } else {
         buf.setIntLE(textureOffsetSlot, -1);
      }

      if (this.animation != null) {
         buf.setIntLE(animationOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.animation, 4096000);
      } else {
         buf.setIntLE(animationOffsetSlot, -1);
      }

      if (this.playerAnimationsId != null) {
         buf.setIntLE(playerAnimationsIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.playerAnimationsId, 4096000);
      } else {
         buf.setIntLE(playerAnimationsIdOffsetSlot, -1);
      }

      if (this.icon != null) {
         buf.setIntLE(iconOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.icon, 4096000);
      } else {
         buf.setIntLE(iconOffsetSlot, -1);
      }

      if (this.translationProperties != null) {
         buf.setIntLE(translationPropertiesOffsetSlot, buf.writerIndex() - varBlockStart);
         this.translationProperties.serialize(buf);
      } else {
         buf.setIntLE(translationPropertiesOffsetSlot, -1);
      }

      if (this.resourceTypes != null) {
         buf.setIntLE(resourceTypesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.resourceTypes.length > 4096000) {
            throw ProtocolException.arrayTooLong("ResourceTypes", this.resourceTypes.length, 4096000);
         }

         VarInt.write(buf, this.resourceTypes.length);

         for (ItemResourceType item : this.resourceTypes) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(resourceTypesOffsetSlot, -1);
      }

      if (this.tool != null) {
         buf.setIntLE(toolOffsetSlot, buf.writerIndex() - varBlockStart);
         this.tool.serialize(buf);
      } else {
         buf.setIntLE(toolOffsetSlot, -1);
      }

      if (this.weapon != null) {
         buf.setIntLE(weaponOffsetSlot, buf.writerIndex() - varBlockStart);
         this.weapon.serialize(buf);
      } else {
         buf.setIntLE(weaponOffsetSlot, -1);
      }

      if (this.armor != null) {
         buf.setIntLE(armorOffsetSlot, buf.writerIndex() - varBlockStart);
         this.armor.serialize(buf);
      } else {
         buf.setIntLE(armorOffsetSlot, -1);
      }

      if (this.utility != null) {
         buf.setIntLE(utilityOffsetSlot, buf.writerIndex() - varBlockStart);
         this.utility.serialize(buf);
      } else {
         buf.setIntLE(utilityOffsetSlot, -1);
      }

      if (this.builderToolData != null) {
         buf.setIntLE(builderToolDataOffsetSlot, buf.writerIndex() - varBlockStart);
         this.builderToolData.serialize(buf);
      } else {
         buf.setIntLE(builderToolDataOffsetSlot, -1);
      }

      if (this.itemEntity != null) {
         buf.setIntLE(itemEntityOffsetSlot, buf.writerIndex() - varBlockStart);
         this.itemEntity.serialize(buf);
      } else {
         buf.setIntLE(itemEntityOffsetSlot, -1);
      }

      if (this.set != null) {
         buf.setIntLE(setOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.set, 4096000);
      } else {
         buf.setIntLE(setOffsetSlot, -1);
      }

      if (this.categories != null) {
         buf.setIntLE(categoriesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.categories.length > 4096000) {
            throw ProtocolException.arrayTooLong("Categories", this.categories.length, 4096000);
         }

         VarInt.write(buf, this.categories.length);

         for (String item : this.categories) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(categoriesOffsetSlot, -1);
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

      if (this.firstPersonParticles != null) {
         buf.setIntLE(firstPersonParticlesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.firstPersonParticles.length > 4096000) {
            throw ProtocolException.arrayTooLong("FirstPersonParticles", this.firstPersonParticles.length, 4096000);
         }

         VarInt.write(buf, this.firstPersonParticles.length);

         for (ModelParticle item : this.firstPersonParticles) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(firstPersonParticlesOffsetSlot, -1);
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

      if (this.interactionVars != null) {
         buf.setIntLE(interactionVarsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.interactionVars.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("InteractionVars", this.interactionVars.size(), 4096000);
         }

         VarInt.write(buf, this.interactionVars.size());

         for (Entry<String, Integer> e : this.interactionVars.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            buf.writeIntLE(e.getValue());
         }
      } else {
         buf.setIntLE(interactionVarsOffsetSlot, -1);
      }

      if (this.interactionConfig != null) {
         buf.setIntLE(interactionConfigOffsetSlot, buf.writerIndex() - varBlockStart);
         this.interactionConfig.serialize(buf);
      } else {
         buf.setIntLE(interactionConfigOffsetSlot, -1);
      }

      if (this.droppedItemAnimation != null) {
         buf.setIntLE(droppedItemAnimationOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.droppedItemAnimation, 4096000);
      } else {
         buf.setIntLE(droppedItemAnimationOffsetSlot, -1);
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

      if (this.itemAppearanceConditions != null) {
         buf.setIntLE(itemAppearanceConditionsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.itemAppearanceConditions.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemAppearanceConditions", this.itemAppearanceConditions.size(), 4096000);
         }

         VarInt.write(buf, this.itemAppearanceConditions.size());

         for (Entry<Integer, ItemAppearanceCondition[]> e : this.itemAppearanceConditions.entrySet()) {
            buf.writeIntLE(e.getKey());
            VarInt.write(buf, e.getValue().length);

            for (ItemAppearanceCondition arrItem : e.getValue()) {
               arrItem.serialize(buf);
            }
         }
      } else {
         buf.setIntLE(itemAppearanceConditionsOffsetSlot, -1);
      }

      if (this.displayEntityStatsHUD != null) {
         buf.setIntLE(displayEntityStatsHUDOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.displayEntityStatsHUD.length > 4096000) {
            throw ProtocolException.arrayTooLong("DisplayEntityStatsHUD", this.displayEntityStatsHUD.length, 4096000);
         }

         VarInt.write(buf, this.displayEntityStatsHUD.length);

         for (int item : this.displayEntityStatsHUD) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(displayEntityStatsHUDOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 251;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.model != null) {
         size += PacketIO.stringSize(this.model);
      }

      if (this.texture != null) {
         size += PacketIO.stringSize(this.texture);
      }

      if (this.animation != null) {
         size += PacketIO.stringSize(this.animation);
      }

      if (this.playerAnimationsId != null) {
         size += PacketIO.stringSize(this.playerAnimationsId);
      }

      if (this.icon != null) {
         size += PacketIO.stringSize(this.icon);
      }

      if (this.translationProperties != null) {
         size += this.translationProperties.computeSize();
      }

      if (this.resourceTypes != null) {
         int resourceTypesSize = 0;

         for (ItemResourceType elem : this.resourceTypes) {
            resourceTypesSize += elem.computeSize();
         }

         size += VarInt.size(this.resourceTypes.length) + resourceTypesSize;
      }

      if (this.tool != null) {
         size += this.tool.computeSize();
      }

      if (this.weapon != null) {
         size += this.weapon.computeSize();
      }

      if (this.armor != null) {
         size += this.armor.computeSize();
      }

      if (this.utility != null) {
         size += this.utility.computeSize();
      }

      if (this.builderToolData != null) {
         size += this.builderToolData.computeSize();
      }

      if (this.itemEntity != null) {
         size += this.itemEntity.computeSize();
      }

      if (this.set != null) {
         size += PacketIO.stringSize(this.set);
      }

      if (this.categories != null) {
         int categoriesSize = 0;

         for (String elem : this.categories) {
            categoriesSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.categories.length) + categoriesSize;
      }

      if (this.particles != null) {
         int particlesSize = 0;

         for (ModelParticle elem : this.particles) {
            particlesSize += elem.computeSize();
         }

         size += VarInt.size(this.particles.length) + particlesSize;
      }

      if (this.firstPersonParticles != null) {
         int firstPersonParticlesSize = 0;

         for (ModelParticle elem : this.firstPersonParticles) {
            firstPersonParticlesSize += elem.computeSize();
         }

         size += VarInt.size(this.firstPersonParticles.length) + firstPersonParticlesSize;
      }

      if (this.trails != null) {
         int trailsSize = 0;

         for (ModelTrail elem : this.trails) {
            trailsSize += elem.computeSize();
         }

         size += VarInt.size(this.trails.length) + trailsSize;
      }

      if (this.interactions != null) {
         size += VarInt.size(this.interactions.size()) + this.interactions.size() * 5;
      }

      if (this.interactionVars != null) {
         int interactionVarsSize = 0;

         for (Entry<String, Integer> kvp : this.interactionVars.entrySet()) {
            interactionVarsSize += PacketIO.stringSize(kvp.getKey()) + 4;
         }

         size += VarInt.size(this.interactionVars.size()) + interactionVarsSize;
      }

      if (this.interactionConfig != null) {
         size += this.interactionConfig.computeSize();
      }

      if (this.droppedItemAnimation != null) {
         size += PacketIO.stringSize(this.droppedItemAnimation);
      }

      if (this.tagIndexes != null) {
         size += VarInt.size(this.tagIndexes.length) + this.tagIndexes.length * 4;
      }

      if (this.itemAppearanceConditions != null) {
         int itemAppearanceConditionsSize = 0;

         for (Entry<Integer, ItemAppearanceCondition[]> kvp : this.itemAppearanceConditions.entrySet()) {
            itemAppearanceConditionsSize += 4 + VarInt.size(kvp.getValue().length) + Arrays.stream(kvp.getValue()).mapToInt(inner -> inner.computeSize()).sum();
         }

         size += VarInt.size(this.itemAppearanceConditions.size()) + itemAppearanceConditionsSize;
      }

      if (this.displayEntityStatsHUD != null) {
         size += VarInt.size(this.displayEntityStatsHUD.length) + this.displayEntityStatsHUD.length * 4;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 251) {
         return ValidationResult.error("Buffer too small: expected at least 251 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 4);
         if ((nullBits[0] & 32) != 0) {
            int idOffset = buffer.getIntLE(offset + 147);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 251 + idOffset;
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

         if ((nullBits[0] & 64) != 0) {
            int modelOffset = buffer.getIntLE(offset + 151);
            if (modelOffset < 0) {
               return ValidationResult.error("Invalid offset for Model");
            }

            int posx = offset + 251 + modelOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Model");
            }

            int modelLen = VarInt.peek(buffer, posx);
            if (modelLen < 0) {
               return ValidationResult.error("Invalid string length for Model");
            }

            if (modelLen > 4096000) {
               return ValidationResult.error("Model exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += modelLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Model");
            }
         }

         if ((nullBits[0] & 128) != 0) {
            int textureOffset = buffer.getIntLE(offset + 155);
            if (textureOffset < 0) {
               return ValidationResult.error("Invalid offset for Texture");
            }

            int posxx = offset + 251 + textureOffset;
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

         if ((nullBits[1] & 1) != 0) {
            int animationOffset = buffer.getIntLE(offset + 159);
            if (animationOffset < 0) {
               return ValidationResult.error("Invalid offset for Animation");
            }

            int posxxx = offset + 251 + animationOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Animation");
            }

            int animationLen = VarInt.peek(buffer, posxxx);
            if (animationLen < 0) {
               return ValidationResult.error("Invalid string length for Animation");
            }

            if (animationLen > 4096000) {
               return ValidationResult.error("Animation exceeds max length 4096000");
            }

            posxxx += VarInt.length(buffer, posxxx);
            posxxx += animationLen;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Animation");
            }
         }

         if ((nullBits[1] & 2) != 0) {
            int playerAnimationsIdOffset = buffer.getIntLE(offset + 163);
            if (playerAnimationsIdOffset < 0) {
               return ValidationResult.error("Invalid offset for PlayerAnimationsId");
            }

            int posxxxx = offset + 251 + playerAnimationsIdOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for PlayerAnimationsId");
            }

            int playerAnimationsIdLen = VarInt.peek(buffer, posxxxx);
            if (playerAnimationsIdLen < 0) {
               return ValidationResult.error("Invalid string length for PlayerAnimationsId");
            }

            if (playerAnimationsIdLen > 4096000) {
               return ValidationResult.error("PlayerAnimationsId exceeds max length 4096000");
            }

            posxxxx += VarInt.length(buffer, posxxxx);
            posxxxx += playerAnimationsIdLen;
            if (posxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading PlayerAnimationsId");
            }
         }

         if ((nullBits[1] & 4) != 0) {
            int iconOffset = buffer.getIntLE(offset + 167);
            if (iconOffset < 0) {
               return ValidationResult.error("Invalid offset for Icon");
            }

            int posxxxxx = offset + 251 + iconOffset;
            if (posxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Icon");
            }

            int iconLen = VarInt.peek(buffer, posxxxxx);
            if (iconLen < 0) {
               return ValidationResult.error("Invalid string length for Icon");
            }

            if (iconLen > 4096000) {
               return ValidationResult.error("Icon exceeds max length 4096000");
            }

            posxxxxx += VarInt.length(buffer, posxxxxx);
            posxxxxx += iconLen;
            if (posxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Icon");
            }
         }

         if ((nullBits[1] & 8) != 0) {
            int translationPropertiesOffset = buffer.getIntLE(offset + 171);
            if (translationPropertiesOffset < 0) {
               return ValidationResult.error("Invalid offset for TranslationProperties");
            }

            int posxxxxxx = offset + 251 + translationPropertiesOffset;
            if (posxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for TranslationProperties");
            }

            ValidationResult translationPropertiesResult = ItemTranslationProperties.validateStructure(buffer, posxxxxxx);
            if (!translationPropertiesResult.isValid()) {
               return ValidationResult.error("Invalid TranslationProperties: " + translationPropertiesResult.error());
            }

            posxxxxxx += ItemTranslationProperties.computeBytesConsumed(buffer, posxxxxxx);
         }

         if ((nullBits[1] & 16) != 0) {
            int resourceTypesOffset = buffer.getIntLE(offset + 175);
            if (resourceTypesOffset < 0) {
               return ValidationResult.error("Invalid offset for ResourceTypes");
            }

            int posxxxxxxx = offset + 251 + resourceTypesOffset;
            if (posxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ResourceTypes");
            }

            int resourceTypesCount = VarInt.peek(buffer, posxxxxxxx);
            if (resourceTypesCount < 0) {
               return ValidationResult.error("Invalid array count for ResourceTypes");
            }

            if (resourceTypesCount > 4096000) {
               return ValidationResult.error("ResourceTypes exceeds max length 4096000");
            }

            posxxxxxxx += VarInt.length(buffer, posxxxxxxx);

            for (int i = 0; i < resourceTypesCount; i++) {
               ValidationResult structResult = ItemResourceType.validateStructure(buffer, posxxxxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ItemResourceType in ResourceTypes[" + i + "]: " + structResult.error());
               }

               posxxxxxxx += ItemResourceType.computeBytesConsumed(buffer, posxxxxxxx);
            }
         }

         if ((nullBits[1] & 32) != 0) {
            int toolOffset = buffer.getIntLE(offset + 179);
            if (toolOffset < 0) {
               return ValidationResult.error("Invalid offset for Tool");
            }

            int posxxxxxxxx = offset + 251 + toolOffset;
            if (posxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Tool");
            }

            ValidationResult toolResult = ItemTool.validateStructure(buffer, posxxxxxxxx);
            if (!toolResult.isValid()) {
               return ValidationResult.error("Invalid Tool: " + toolResult.error());
            }

            posxxxxxxxx += ItemTool.computeBytesConsumed(buffer, posxxxxxxxx);
         }

         if ((nullBits[1] & 64) != 0) {
            int weaponOffset = buffer.getIntLE(offset + 183);
            if (weaponOffset < 0) {
               return ValidationResult.error("Invalid offset for Weapon");
            }

            int posxxxxxxxxx = offset + 251 + weaponOffset;
            if (posxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Weapon");
            }

            ValidationResult weaponResult = ItemWeapon.validateStructure(buffer, posxxxxxxxxx);
            if (!weaponResult.isValid()) {
               return ValidationResult.error("Invalid Weapon: " + weaponResult.error());
            }

            posxxxxxxxxx += ItemWeapon.computeBytesConsumed(buffer, posxxxxxxxxx);
         }

         if ((nullBits[1] & 128) != 0) {
            int armorOffset = buffer.getIntLE(offset + 187);
            if (armorOffset < 0) {
               return ValidationResult.error("Invalid offset for Armor");
            }

            int posxxxxxxxxxx = offset + 251 + armorOffset;
            if (posxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Armor");
            }

            ValidationResult armorResult = ItemArmor.validateStructure(buffer, posxxxxxxxxxx);
            if (!armorResult.isValid()) {
               return ValidationResult.error("Invalid Armor: " + armorResult.error());
            }

            posxxxxxxxxxx += ItemArmor.computeBytesConsumed(buffer, posxxxxxxxxxx);
         }

         if ((nullBits[2] & 1) != 0) {
            int utilityOffset = buffer.getIntLE(offset + 191);
            if (utilityOffset < 0) {
               return ValidationResult.error("Invalid offset for Utility");
            }

            int posxxxxxxxxxxx = offset + 251 + utilityOffset;
            if (posxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Utility");
            }

            ValidationResult utilityResult = ItemUtility.validateStructure(buffer, posxxxxxxxxxxx);
            if (!utilityResult.isValid()) {
               return ValidationResult.error("Invalid Utility: " + utilityResult.error());
            }

            posxxxxxxxxxxx += ItemUtility.computeBytesConsumed(buffer, posxxxxxxxxxxx);
         }

         if ((nullBits[2] & 2) != 0) {
            int builderToolDataOffset = buffer.getIntLE(offset + 195);
            if (builderToolDataOffset < 0) {
               return ValidationResult.error("Invalid offset for BuilderToolData");
            }

            int posxxxxxxxxxxxx = offset + 251 + builderToolDataOffset;
            if (posxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for BuilderToolData");
            }

            ValidationResult builderToolDataResult = ItemBuilderToolData.validateStructure(buffer, posxxxxxxxxxxxx);
            if (!builderToolDataResult.isValid()) {
               return ValidationResult.error("Invalid BuilderToolData: " + builderToolDataResult.error());
            }

            posxxxxxxxxxxxx += ItemBuilderToolData.computeBytesConsumed(buffer, posxxxxxxxxxxxx);
         }

         if ((nullBits[2] & 4) != 0) {
            int itemEntityOffset = buffer.getIntLE(offset + 199);
            if (itemEntityOffset < 0) {
               return ValidationResult.error("Invalid offset for ItemEntity");
            }

            int posxxxxxxxxxxxxx = offset + 251 + itemEntityOffset;
            if (posxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ItemEntity");
            }

            ValidationResult itemEntityResult = ItemEntityConfig.validateStructure(buffer, posxxxxxxxxxxxxx);
            if (!itemEntityResult.isValid()) {
               return ValidationResult.error("Invalid ItemEntity: " + itemEntityResult.error());
            }

            posxxxxxxxxxxxxx += ItemEntityConfig.computeBytesConsumed(buffer, posxxxxxxxxxxxxx);
         }

         if ((nullBits[2] & 8) != 0) {
            int setOffset = buffer.getIntLE(offset + 203);
            if (setOffset < 0) {
               return ValidationResult.error("Invalid offset for Set");
            }

            int posxxxxxxxxxxxxxx = offset + 251 + setOffset;
            if (posxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Set");
            }

            int setLen = VarInt.peek(buffer, posxxxxxxxxxxxxxx);
            if (setLen < 0) {
               return ValidationResult.error("Invalid string length for Set");
            }

            if (setLen > 4096000) {
               return ValidationResult.error("Set exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxx);
            posxxxxxxxxxxxxxx += setLen;
            if (posxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Set");
            }
         }

         if ((nullBits[2] & 16) != 0) {
            int categoriesOffset = buffer.getIntLE(offset + 207);
            if (categoriesOffset < 0) {
               return ValidationResult.error("Invalid offset for Categories");
            }

            int posxxxxxxxxxxxxxxx = offset + 251 + categoriesOffset;
            if (posxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Categories");
            }

            int categoriesCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxx);
            if (categoriesCount < 0) {
               return ValidationResult.error("Invalid array count for Categories");
            }

            if (categoriesCount > 4096000) {
               return ValidationResult.error("Categories exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxx);

            for (int i = 0; i < categoriesCount; i++) {
               int strLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxx);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Categories");
               }

               posxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxx);
               posxxxxxxxxxxxxxxx += strLen;
               if (posxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in Categories");
               }
            }
         }

         if ((nullBits[2] & 32) != 0) {
            int particlesOffset = buffer.getIntLE(offset + 211);
            if (particlesOffset < 0) {
               return ValidationResult.error("Invalid offset for Particles");
            }

            int posxxxxxxxxxxxxxxxx = offset + 251 + particlesOffset;
            if (posxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Particles");
            }

            int particlesCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxx);
            if (particlesCount < 0) {
               return ValidationResult.error("Invalid array count for Particles");
            }

            if (particlesCount > 4096000) {
               return ValidationResult.error("Particles exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxx);

            for (int i = 0; i < particlesCount; i++) {
               ValidationResult structResult = ModelParticle.validateStructure(buffer, posxxxxxxxxxxxxxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ModelParticle in Particles[" + i + "]: " + structResult.error());
               }

               posxxxxxxxxxxxxxxxx += ModelParticle.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxx);
            }
         }

         if ((nullBits[2] & 64) != 0) {
            int firstPersonParticlesOffset = buffer.getIntLE(offset + 215);
            if (firstPersonParticlesOffset < 0) {
               return ValidationResult.error("Invalid offset for FirstPersonParticles");
            }

            int posxxxxxxxxxxxxxxxxx = offset + 251 + firstPersonParticlesOffset;
            if (posxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FirstPersonParticles");
            }

            int firstPersonParticlesCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxx);
            if (firstPersonParticlesCount < 0) {
               return ValidationResult.error("Invalid array count for FirstPersonParticles");
            }

            if (firstPersonParticlesCount > 4096000) {
               return ValidationResult.error("FirstPersonParticles exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxx);

            for (int i = 0; i < firstPersonParticlesCount; i++) {
               ValidationResult structResult = ModelParticle.validateStructure(buffer, posxxxxxxxxxxxxxxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ModelParticle in FirstPersonParticles[" + i + "]: " + structResult.error());
               }

               posxxxxxxxxxxxxxxxxx += ModelParticle.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxxx);
            }
         }

         if ((nullBits[2] & 128) != 0) {
            int trailsOffset = buffer.getIntLE(offset + 219);
            if (trailsOffset < 0) {
               return ValidationResult.error("Invalid offset for Trails");
            }

            int posxxxxxxxxxxxxxxxxxx = offset + 251 + trailsOffset;
            if (posxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Trails");
            }

            int trailsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxx);
            if (trailsCount < 0) {
               return ValidationResult.error("Invalid array count for Trails");
            }

            if (trailsCount > 4096000) {
               return ValidationResult.error("Trails exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxx);

            for (int i = 0; i < trailsCount; i++) {
               ValidationResult structResult = ModelTrail.validateStructure(buffer, posxxxxxxxxxxxxxxxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ModelTrail in Trails[" + i + "]: " + structResult.error());
               }

               posxxxxxxxxxxxxxxxxxx += ModelTrail.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxxxx);
            }
         }

         if ((nullBits[3] & 1) != 0) {
            int interactionsOffset = buffer.getIntLE(offset + 223);
            if (interactionsOffset < 0) {
               return ValidationResult.error("Invalid offset for Interactions");
            }

            int posxxxxxxxxxxxxxxxxxxx = offset + 251 + interactionsOffset;
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

         if ((nullBits[3] & 2) != 0) {
            int interactionVarsOffset = buffer.getIntLE(offset + 227);
            if (interactionVarsOffset < 0) {
               return ValidationResult.error("Invalid offset for InteractionVars");
            }

            int posxxxxxxxxxxxxxxxxxxxx = offset + 251 + interactionVarsOffset;
            if (posxxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for InteractionVars");
            }

            int interactionVarsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxx);
            if (interactionVarsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for InteractionVars");
            }

            if (interactionVarsCount > 4096000) {
               return ValidationResult.error("InteractionVars exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxxx);

            for (int ix = 0; ix < interactionVarsCount; ix++) {
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

         if ((nullBits[3] & 4) != 0) {
            int interactionConfigOffset = buffer.getIntLE(offset + 231);
            if (interactionConfigOffset < 0) {
               return ValidationResult.error("Invalid offset for InteractionConfig");
            }

            int posxxxxxxxxxxxxxxxxxxxxx = offset + 251 + interactionConfigOffset;
            if (posxxxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for InteractionConfig");
            }

            ValidationResult interactionConfigResult = InteractionConfiguration.validateStructure(buffer, posxxxxxxxxxxxxxxxxxxxxx);
            if (!interactionConfigResult.isValid()) {
               return ValidationResult.error("Invalid InteractionConfig: " + interactionConfigResult.error());
            }

            posxxxxxxxxxxxxxxxxxxxxx += InteractionConfiguration.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxxxxxxx);
         }

         if ((nullBits[3] & 8) != 0) {
            int droppedItemAnimationOffset = buffer.getIntLE(offset + 235);
            if (droppedItemAnimationOffset < 0) {
               return ValidationResult.error("Invalid offset for DroppedItemAnimation");
            }

            int posxxxxxxxxxxxxxxxxxxxxxx = offset + 251 + droppedItemAnimationOffset;
            if (posxxxxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for DroppedItemAnimation");
            }

            int droppedItemAnimationLen = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxxxx);
            if (droppedItemAnimationLen < 0) {
               return ValidationResult.error("Invalid string length for DroppedItemAnimation");
            }

            if (droppedItemAnimationLen > 4096000) {
               return ValidationResult.error("DroppedItemAnimation exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxxxxx);
            posxxxxxxxxxxxxxxxxxxxxxx += droppedItemAnimationLen;
            if (posxxxxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading DroppedItemAnimation");
            }
         }

         if ((nullBits[3] & 16) != 0) {
            int tagIndexesOffset = buffer.getIntLE(offset + 239);
            if (tagIndexesOffset < 0) {
               return ValidationResult.error("Invalid offset for TagIndexes");
            }

            int posxxxxxxxxxxxxxxxxxxxxxxx = offset + 251 + tagIndexesOffset;
            if (posxxxxxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for TagIndexes");
            }

            int tagIndexesCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxxxxx);
            if (tagIndexesCount < 0) {
               return ValidationResult.error("Invalid array count for TagIndexes");
            }

            if (tagIndexesCount > 4096000) {
               return ValidationResult.error("TagIndexes exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxxxxxx);
            posxxxxxxxxxxxxxxxxxxxxxxx += tagIndexesCount * 4;
            if (posxxxxxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading TagIndexes");
            }
         }

         if ((nullBits[3] & 32) != 0) {
            int itemAppearanceConditionsOffset = buffer.getIntLE(offset + 243);
            if (itemAppearanceConditionsOffset < 0) {
               return ValidationResult.error("Invalid offset for ItemAppearanceConditions");
            }

            int posxxxxxxxxxxxxxxxxxxxxxxxx = offset + 251 + itemAppearanceConditionsOffset;
            if (posxxxxxxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ItemAppearanceConditions");
            }

            int itemAppearanceConditionsCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxxxxxx);
            if (itemAppearanceConditionsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ItemAppearanceConditions");
            }

            if (itemAppearanceConditionsCount > 4096000) {
               return ValidationResult.error("ItemAppearanceConditions exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxxxxxxx);

            for (int ix = 0; ix < itemAppearanceConditionsCount; ix++) {
               posxxxxxxxxxxxxxxxxxxxxxxxx += 4;
               if (posxxxxxxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               int valueArrCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxxxxxx);
               if (valueArrCount < 0) {
                  return ValidationResult.error("Invalid array count for value");
               }

               posxxxxxxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxxxxxxx);

               for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                  posxxxxxxxxxxxxxxxxxxxxxxxx += ItemAppearanceCondition.computeBytesConsumed(buffer, posxxxxxxxxxxxxxxxxxxxxxxxx);
               }
            }
         }

         if ((nullBits[3] & 64) != 0) {
            int displayEntityStatsHUDOffset = buffer.getIntLE(offset + 247);
            if (displayEntityStatsHUDOffset < 0) {
               return ValidationResult.error("Invalid offset for DisplayEntityStatsHUD");
            }

            int posxxxxxxxxxxxxxxxxxxxxxxxxx = offset + 251 + displayEntityStatsHUDOffset;
            if (posxxxxxxxxxxxxxxxxxxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for DisplayEntityStatsHUD");
            }

            int displayEntityStatsHUDCount = VarInt.peek(buffer, posxxxxxxxxxxxxxxxxxxxxxxxxx);
            if (displayEntityStatsHUDCount < 0) {
               return ValidationResult.error("Invalid array count for DisplayEntityStatsHUD");
            }

            if (displayEntityStatsHUDCount > 4096000) {
               return ValidationResult.error("DisplayEntityStatsHUD exceeds max length 4096000");
            }

            posxxxxxxxxxxxxxxxxxxxxxxxxx += VarInt.length(buffer, posxxxxxxxxxxxxxxxxxxxxxxxxx);
            posxxxxxxxxxxxxxxxxxxxxxxxxx += displayEntityStatsHUDCount * 4;
            if (posxxxxxxxxxxxxxxxxxxxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading DisplayEntityStatsHUD");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemBase clone() {
      ItemBase copy = new ItemBase();
      copy.id = this.id;
      copy.model = this.model;
      copy.scale = this.scale;
      copy.texture = this.texture;
      copy.animation = this.animation;
      copy.playerAnimationsId = this.playerAnimationsId;
      copy.usePlayerAnimations = this.usePlayerAnimations;
      copy.maxStack = this.maxStack;
      copy.reticleIndex = this.reticleIndex;
      copy.icon = this.icon;
      copy.iconProperties = this.iconProperties != null ? this.iconProperties.clone() : null;
      copy.translationProperties = this.translationProperties != null ? this.translationProperties.clone() : null;
      copy.itemLevel = this.itemLevel;
      copy.qualityIndex = this.qualityIndex;
      copy.resourceTypes = this.resourceTypes != null ? Arrays.stream(this.resourceTypes).map(ex -> ex.clone()).toArray(ItemResourceType[]::new) : null;
      copy.consumable = this.consumable;
      copy.variant = this.variant;
      copy.blockId = this.blockId;
      copy.tool = this.tool != null ? this.tool.clone() : null;
      copy.weapon = this.weapon != null ? this.weapon.clone() : null;
      copy.armor = this.armor != null ? this.armor.clone() : null;
      copy.gliderConfig = this.gliderConfig != null ? this.gliderConfig.clone() : null;
      copy.utility = this.utility != null ? this.utility.clone() : null;
      copy.blockSelectorTool = this.blockSelectorTool != null ? this.blockSelectorTool.clone() : null;
      copy.builderToolData = this.builderToolData != null ? this.builderToolData.clone() : null;
      copy.itemEntity = this.itemEntity != null ? this.itemEntity.clone() : null;
      copy.set = this.set;
      copy.categories = this.categories != null ? Arrays.copyOf(this.categories, this.categories.length) : null;
      copy.particles = this.particles != null ? Arrays.stream(this.particles).map(ex -> ex.clone()).toArray(ModelParticle[]::new) : null;
      copy.firstPersonParticles = this.firstPersonParticles != null
         ? Arrays.stream(this.firstPersonParticles).map(ex -> ex.clone()).toArray(ModelParticle[]::new)
         : null;
      copy.trails = this.trails != null ? Arrays.stream(this.trails).map(ex -> ex.clone()).toArray(ModelTrail[]::new) : null;
      copy.light = this.light != null ? this.light.clone() : null;
      copy.durability = this.durability;
      copy.soundEventIndex = this.soundEventIndex;
      copy.itemSoundSetIndex = this.itemSoundSetIndex;
      copy.interactions = this.interactions != null ? new HashMap<>(this.interactions) : null;
      copy.interactionVars = this.interactionVars != null ? new HashMap<>(this.interactionVars) : null;
      copy.interactionConfig = this.interactionConfig != null ? this.interactionConfig.clone() : null;
      copy.droppedItemAnimation = this.droppedItemAnimation;
      copy.tagIndexes = this.tagIndexes != null ? Arrays.copyOf(this.tagIndexes, this.tagIndexes.length) : null;
      if (this.itemAppearanceConditions != null) {
         Map<Integer, ItemAppearanceCondition[]> m = new HashMap<>();

         for (Entry<Integer, ItemAppearanceCondition[]> e : this.itemAppearanceConditions.entrySet()) {
            m.put(e.getKey(), Arrays.stream(e.getValue()).map(x -> x.clone()).toArray(ItemAppearanceCondition[]::new));
         }

         copy.itemAppearanceConditions = m;
      }

      copy.displayEntityStatsHUD = this.displayEntityStatsHUD != null ? Arrays.copyOf(this.displayEntityStatsHUD, this.displayEntityStatsHUD.length) : null;
      copy.pullbackConfig = this.pullbackConfig != null ? this.pullbackConfig.clone() : null;
      copy.clipsGeometry = this.clipsGeometry;
      copy.renderDeployablePreview = this.renderDeployablePreview;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemBase other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.model, other.model)
               && this.scale == other.scale
               && Objects.equals(this.texture, other.texture)
               && Objects.equals(this.animation, other.animation)
               && Objects.equals(this.playerAnimationsId, other.playerAnimationsId)
               && this.usePlayerAnimations == other.usePlayerAnimations
               && this.maxStack == other.maxStack
               && this.reticleIndex == other.reticleIndex
               && Objects.equals(this.icon, other.icon)
               && Objects.equals(this.iconProperties, other.iconProperties)
               && Objects.equals(this.translationProperties, other.translationProperties)
               && this.itemLevel == other.itemLevel
               && this.qualityIndex == other.qualityIndex
               && Arrays.equals((Object[])this.resourceTypes, (Object[])other.resourceTypes)
               && this.consumable == other.consumable
               && this.variant == other.variant
               && this.blockId == other.blockId
               && Objects.equals(this.tool, other.tool)
               && Objects.equals(this.weapon, other.weapon)
               && Objects.equals(this.armor, other.armor)
               && Objects.equals(this.gliderConfig, other.gliderConfig)
               && Objects.equals(this.utility, other.utility)
               && Objects.equals(this.blockSelectorTool, other.blockSelectorTool)
               && Objects.equals(this.builderToolData, other.builderToolData)
               && Objects.equals(this.itemEntity, other.itemEntity)
               && Objects.equals(this.set, other.set)
               && Arrays.equals((Object[])this.categories, (Object[])other.categories)
               && Arrays.equals((Object[])this.particles, (Object[])other.particles)
               && Arrays.equals((Object[])this.firstPersonParticles, (Object[])other.firstPersonParticles)
               && Arrays.equals((Object[])this.trails, (Object[])other.trails)
               && Objects.equals(this.light, other.light)
               && this.durability == other.durability
               && this.soundEventIndex == other.soundEventIndex
               && this.itemSoundSetIndex == other.itemSoundSetIndex
               && Objects.equals(this.interactions, other.interactions)
               && Objects.equals(this.interactionVars, other.interactionVars)
               && Objects.equals(this.interactionConfig, other.interactionConfig)
               && Objects.equals(this.droppedItemAnimation, other.droppedItemAnimation)
               && Arrays.equals(this.tagIndexes, other.tagIndexes)
               && Objects.equals(this.itemAppearanceConditions, other.itemAppearanceConditions)
               && Arrays.equals(this.displayEntityStatsHUD, other.displayEntityStatsHUD)
               && Objects.equals(this.pullbackConfig, other.pullbackConfig)
               && this.clipsGeometry == other.clipsGeometry
               && this.renderDeployablePreview == other.renderDeployablePreview;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Objects.hashCode(this.model);
      result = 31 * result + Float.hashCode(this.scale);
      result = 31 * result + Objects.hashCode(this.texture);
      result = 31 * result + Objects.hashCode(this.animation);
      result = 31 * result + Objects.hashCode(this.playerAnimationsId);
      result = 31 * result + Boolean.hashCode(this.usePlayerAnimations);
      result = 31 * result + Integer.hashCode(this.maxStack);
      result = 31 * result + Integer.hashCode(this.reticleIndex);
      result = 31 * result + Objects.hashCode(this.icon);
      result = 31 * result + Objects.hashCode(this.iconProperties);
      result = 31 * result + Objects.hashCode(this.translationProperties);
      result = 31 * result + Integer.hashCode(this.itemLevel);
      result = 31 * result + Integer.hashCode(this.qualityIndex);
      result = 31 * result + Arrays.hashCode((Object[])this.resourceTypes);
      result = 31 * result + Boolean.hashCode(this.consumable);
      result = 31 * result + Boolean.hashCode(this.variant);
      result = 31 * result + Integer.hashCode(this.blockId);
      result = 31 * result + Objects.hashCode(this.tool);
      result = 31 * result + Objects.hashCode(this.weapon);
      result = 31 * result + Objects.hashCode(this.armor);
      result = 31 * result + Objects.hashCode(this.gliderConfig);
      result = 31 * result + Objects.hashCode(this.utility);
      result = 31 * result + Objects.hashCode(this.blockSelectorTool);
      result = 31 * result + Objects.hashCode(this.builderToolData);
      result = 31 * result + Objects.hashCode(this.itemEntity);
      result = 31 * result + Objects.hashCode(this.set);
      result = 31 * result + Arrays.hashCode((Object[])this.categories);
      result = 31 * result + Arrays.hashCode((Object[])this.particles);
      result = 31 * result + Arrays.hashCode((Object[])this.firstPersonParticles);
      result = 31 * result + Arrays.hashCode((Object[])this.trails);
      result = 31 * result + Objects.hashCode(this.light);
      result = 31 * result + Double.hashCode(this.durability);
      result = 31 * result + Integer.hashCode(this.soundEventIndex);
      result = 31 * result + Integer.hashCode(this.itemSoundSetIndex);
      result = 31 * result + Objects.hashCode(this.interactions);
      result = 31 * result + Objects.hashCode(this.interactionVars);
      result = 31 * result + Objects.hashCode(this.interactionConfig);
      result = 31 * result + Objects.hashCode(this.droppedItemAnimation);
      result = 31 * result + Arrays.hashCode(this.tagIndexes);
      result = 31 * result + Objects.hashCode(this.itemAppearanceConditions);
      result = 31 * result + Arrays.hashCode(this.displayEntityStatsHUD);
      result = 31 * result + Objects.hashCode(this.pullbackConfig);
      result = 31 * result + Boolean.hashCode(this.clipsGeometry);
      return 31 * result + Boolean.hashCode(this.renderDeployablePreview);
   }
}
