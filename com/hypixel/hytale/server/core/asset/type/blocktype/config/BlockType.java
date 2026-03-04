package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.codec.codecs.map.MergedEnumMapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart;
import com.hypixel.hytale.codec.schema.metadata.ui.UIPropertyTitle;
import com.hypixel.hytale.codec.schema.metadata.ui.UIRebuildCaches;
import com.hypixel.hytale.codec.store.StoredCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.common.util.MapUtil;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.protocol.BlockFlags;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.BlockNeighbor;
import com.hypixel.hytale.protocol.BlockTextures;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.DrawType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ModelTexture;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.protocol.RailConfig;
import com.hypixel.hytale.protocol.RailPoint;
import com.hypixel.hytale.protocol.RandomRotation;
import com.hypixel.hytale.protocol.ShaderType;
import com.hypixel.hytale.protocol.ShadingMode;
import com.hypixel.hytale.protocol.Tint;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.type.blockbreakingdecal.config.BlockBreakingDecal;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blockparticle.config.BlockParticleSet;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.RandomTickProcedure;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.TickProcedure;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingData;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.mountpoints.RotatedMountPointsArray;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BlockTypeListAsset;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.PrefabListAsset;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionTypeUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.chunk.section.palette.ISectionPalette;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlockRuleSet;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.io.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.ToIntFunction;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockType implements JsonAssetWithMap<String, BlockTypeAssetMap<String, BlockType>>, NetworkSerializable<com.hypixel.hytale.protocol.BlockType> {
   public static final AssetBuilderCodec<String, BlockType> CODEC = AssetBuilderCodec.builder(
         BlockType.class, BlockType::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
      )
      .documentation("The definition for a block in the game. Can only be defined within an **Item** and not standalone.")
      .<String>appendInherited(
         new KeyedCodec<>("Group", Codec.STRING),
         (blockType, o) -> blockType.group = o,
         blockType -> blockType.group,
         (blockType, parent) -> blockType.group = parent.group
      )
      .documentation(
         "Sets the group for this block. Used by **BlockSets**.\n\nA group of _\"@Tech\"_ will prevent physics from being automatically applied to the block."
      )
      .metadata(new UIEditor(new UIEditor.TextField("BlockGroups")))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("BlockListAssetId", Codec.STRING),
         (blockType, blockListAssetId) -> blockType.blockListAssetId = blockListAssetId,
         blockType -> blockType.blockListAssetId,
         (blockType, parent) -> blockType.blockListAssetId = parent.blockListAssetId
      )
      .addValidator(BlockTypeListAsset.VALIDATOR_CACHE.getValidator())
      .documentation("The name of a BlockList asset, for use  in builder tool brushes")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("PrefabListAssetId", Codec.STRING),
         (blockType, prefabListAssetId) -> blockType.prefabListAssetId = prefabListAssetId,
         blockType -> blockType.prefabListAssetId,
         (blockType, parent) -> blockType.prefabListAssetId = parent.prefabListAssetId
      )
      .addValidator(PrefabListAsset.VALIDATOR_CACHE.getValidator())
      .documentation("The name of a PrefabList asset, for use  in builder tool brushes")
      .add()
      .<DrawType>appendInherited(
         new KeyedCodec<>("DrawType", new EnumCodec<>(DrawType.class)),
         (blockType, o) -> blockType.drawType = o,
         blockType -> blockType.drawType,
         (blockType, parent) -> blockType.drawType = parent.drawType
      )
      .addValidator(Validators.nonNull())
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS, UIRebuildCaches.ClientCache.BLOCK_TEXTURES, UIRebuildCaches.ClientCache.MODEL_TEXTURES))
      .metadata(new UIEditorSectionStart("Rendering"))
      .add()
      .<BlockTypeTextures[]>appendInherited(
         new KeyedCodec<>("Textures", new ArrayCodec<>(BlockTypeTextures.CODEC, BlockTypeTextures[]::new)),
         (blockType, o) -> blockType.textures = o,
         blockType -> blockType.textures,
         (blockType, parent) -> blockType.textures = parent.textures
      )
      .metadata(new UIPropertyTitle("Block Textures"))
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS, UIRebuildCaches.ClientCache.BLOCK_TEXTURES))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("TextureSideMask", Codec.STRING),
         (blockType, o) -> blockType.textureSideMask = o,
         blockType -> blockType.textureSideMask,
         (blockType, parent) -> blockType.textureSideMask = parent.textureSideMask
      )
      .addValidator(CommonAssetValidator.TEXTURE_ITEM)
      .metadata(new UIPropertyTitle("Block Texture Side Mask"))
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS, UIRebuildCaches.ClientCache.BLOCK_TEXTURES))
      .add()
      .<ShadingMode>appendInherited(
         new KeyedCodec<>("CubeShadingMode", new EnumCodec<>(ShadingMode.class)),
         (blockType, o) -> blockType.cubeShadingMode = o,
         blockType -> blockType.cubeShadingMode,
         (blockType, parent) -> blockType.cubeShadingMode = parent.cubeShadingMode
      )
      .addValidator(Validators.nonNull())
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .add()
      .<CustomModelTexture[]>appendInherited(
         new KeyedCodec<>("CustomModelTexture", new ArrayCodec<>(CustomModelTexture.CODEC, CustomModelTexture[]::new)),
         (blockType, o) -> blockType.customModelTexture = o,
         blockType -> blockType.customModelTexture,
         (blockType, parent) -> blockType.customModelTexture = parent.customModelTexture
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS, UIRebuildCaches.ClientCache.BLOCK_TEXTURES))
      .metadata(new UIPropertyTitle("Block Model Textures"))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("CustomModel", Codec.STRING),
         (blockType, o) -> blockType.customModel = o,
         blockType -> blockType.customModel,
         (blockType, parent) -> blockType.customModel = parent.customModel
      )
      .addValidator(CommonAssetValidator.MODEL_ITEM)
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .metadata(new UIPropertyTitle("Block Model"))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("BlockBreakingDecalId", Codec.STRING),
         (blockType, s) -> blockType.blockBreakingDecalId = s,
         blockType -> blockType.blockBreakingDecalId,
         (blockType, parent) -> blockType.blockBreakingDecalId = parent.blockBreakingDecalId
      )
      .documentation("The block breaking decal defined here defines the decal asset that should be overlaid when this block is damaged")
      .addValidator(BlockBreakingDecal.VALIDATOR_CACHE.getValidator())
      .add()
      .<BlockMaterial>appendInherited(
         new KeyedCodec<>("Material", new EnumCodec<>(BlockMaterial.class)),
         (blockType, o) -> blockType.material = o,
         blockType -> blockType.material,
         (blockType, parent) -> blockType.material = parent.material
      )
      .addValidator(Validators.nonNull())
      .add()
      .<Opacity>appendInherited(
         new KeyedCodec<>("Opacity", new EnumCodec<>(Opacity.class)),
         (blockType, o) -> blockType.opacity = o,
         blockType -> blockType.opacity,
         (blockType, parent) -> blockType.opacity = parent.opacity
      )
      .addValidator(Validators.nonNull())
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("RequiresAlphaBlending", Codec.BOOLEAN),
         (blockType, o) -> blockType.requiresAlphaBlending = o,
         blockType -> blockType.requiresAlphaBlending,
         (blockType, parent) -> blockType.requiresAlphaBlending = parent.requiresAlphaBlending
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("CustomModelScale", Codec.FLOAT),
         (blockType, o) -> blockType.customModelScale = o,
         blockType -> blockType.customModelScale,
         (blockType, parent) -> blockType.customModelScale = parent.customModelScale
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("CustomModelAnimation", Codec.STRING),
         (blockType, o) -> blockType.customModelAnimation = o,
         blockType -> blockType.customModelAnimation,
         (blockType, parent) -> blockType.customModelAnimation = parent.customModelAnimation
      )
      .addValidator(CommonAssetValidator.ANIMATION_ITEM_BLOCK)
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .metadata(new UIPropertyTitle("Block Model Animation"))
      .add()
      .<ColorLight>appendInherited(
         new KeyedCodec<>("Light", ProtocolCodecs.COLOR_LIGHT),
         (blockType, o) -> blockType.light = o,
         blockType -> blockType.light,
         (blockType, parent) -> blockType.light = parent.light
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .metadata(new UIPropertyTitle("Block Light"))
      .add()
      .appendInherited(
         new KeyedCodec<>("TickProcedure", TickProcedure.CODEC),
         (blockType, v) -> blockType.tickProcedure = v,
         blockType -> blockType.tickProcedure,
         (blockType, parent) -> blockType.tickProcedure = parent.tickProcedure
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("RandomTickProcedure", RandomTickProcedure.CODEC),
         (blockType, v) -> blockType.randomTickProcedure = v,
         blockType -> blockType.randomTickProcedure,
         (blockType, parent) -> blockType.randomTickProcedure = parent.randomTickProcedure
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ConnectedBlockRuleSet", ConnectedBlockRuleSet.CODEC),
         (blockType, connectedBlockRuleSet) -> blockType.connectedBlockRuleSet = connectedBlockRuleSet,
         blockType -> blockType.connectedBlockRuleSet,
         (blockType, parent) -> blockType.connectedBlockRuleSet = parent.connectedBlockRuleSet
      )
      .add()
      .<ShaderType[]>appendInherited(
         new KeyedCodec<>("Effect", new ArrayCodec<>(new EnumCodec<>(ShaderType.class), ShaderType[]::new)),
         (blockType, o) -> blockType.effect = o,
         blockType -> blockType.effect,
         (blockType, parent) -> blockType.effect = parent.effect
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("TransitionTexture", Codec.STRING),
         (blockType, o) -> blockType.transitionTexture = o,
         blockType -> blockType.transitionTexture,
         (blockType, parent) -> blockType.transitionTexture = parent.transitionTexture
      )
      .addValidator(CommonAssetValidator.TEXTURE_ITEM)
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS, UIRebuildCaches.ClientCache.BLOCK_TEXTURES))
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("TransitionToGroups", new ArrayCodec<>(Codec.STRING, String[]::new).metadata(new UIEditor(new UIEditor.TextField("BlockGroups")))),
         (blockType, o) -> blockType.transitionToGroups = o,
         blockType -> blockType.transitionToGroups,
         (blockType, parent) -> blockType.transitionToGroups = parent.transitionToGroups
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("TransitionToTag", Codec.STRING),
         (blockType, o) -> blockType.transitionToTag = o,
         blockType -> blockType.transitionToTag,
         (blockType, parent) -> blockType.transitionToTag = parent.transitionToTag
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("BlockParticleSetId", Codec.STRING),
         (blockType, s) -> blockType.blockParticleSetId = s,
         blockType -> blockType.blockParticleSetId,
         (blockType, parent) -> blockType.blockParticleSetId = parent.blockParticleSetId
      )
      .documentation(
         "The block particle set defined here defines which particles should be spawned when an entity interacts with this block (like when stepping on it for example"
      )
      .addValidator(BlockParticleSet.VALIDATOR_CACHE.getValidator())
      .metadata(new UIEditorSectionStart("Particles"))
      .add()
      .appendInherited(
         new KeyedCodec<>("ParticleColor", ProtocolCodecs.COLOR),
         (blockType, s) -> blockType.particleColor = s,
         blockType -> blockType.particleColor,
         (blockType, parent) -> blockType.particleColor = parent.particleColor
      )
      .add()
      .<ModelParticle[]>appendInherited(
         new KeyedCodec<>("Particles", ModelParticle.ARRAY_CODEC),
         (blockType, s) -> blockType.particles = s,
         blockType -> blockType.particles,
         (blockType, parent) -> blockType.particles = parent.particles
      )
      .documentation("The particles defined here will be spawned on top of blocks of this type placed in the world.")
      .metadata(new UIPropertyTitle("Block Particles"))
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .addValidator(Validators.nonNullArrayElements())
      .add()
      .<RandomRotation>appendInherited(
         new KeyedCodec<>("RandomRotation", new EnumCodec<>(RandomRotation.class)),
         (blockType, o) -> blockType.randomRotation = o,
         blockType -> blockType.randomRotation,
         (blockType, parent) -> blockType.randomRotation = parent.randomRotation
      )
      .metadata(new UIEditorSectionStart("Rotation"))
      .addValidator(Validators.nonNull())
      .add()
      .<VariantRotation>appendInherited(
         new KeyedCodec<>("VariantRotation", new EnumCodec<>(VariantRotation.class)),
         (blockType, o) -> blockType.variantRotation = o,
         blockType -> blockType.variantRotation,
         (blockType, parent) -> blockType.variantRotation = parent.variantRotation
      )
      .addValidator(Validators.nonNull())
      .add()
      .appendInherited(
         new KeyedCodec<>("FlipType", new EnumCodec<>(BlockFlipType.class)),
         (blockType, o) -> blockType.flipType = o,
         blockType -> blockType.flipType,
         (blockType, parent) -> blockType.flipType = parent.flipType
      )
      .add()
      .<Rotation>appendInherited(
         new KeyedCodec<>("RotationYawPlacementOffset", new EnumCodec<>(Rotation.class)),
         (blockType, o) -> blockType.rotationYawPlacementOffset = o,
         blockType -> blockType.rotationYawPlacementOffset,
         (blockType, parent) -> blockType.rotationYawPlacementOffset = parent.rotationYawPlacementOffset
      )
      .addValidator(Validators.nonNull())
      .add()
      .<RotatedMountPointsArray>appendInherited(
         new KeyedCodec<>("Seats", RotatedMountPointsArray.CODEC),
         (blockType, o) -> blockType.seats = o,
         blockType -> blockType.seats,
         (blockType, parent) -> blockType.seats = parent.seats
      )
      .metadata(new UIEditorSectionStart("Behaviour"))
      .documentation("The details of the seats on this block.")
      .add()
      .<RotatedMountPointsArray>appendInherited(
         new KeyedCodec<>("Beds", RotatedMountPointsArray.CODEC),
         (blockType, o) -> blockType.beds = o,
         blockType -> blockType.beds,
         (blockType, parent) -> blockType.beds = parent.beds
      )
      .documentation("The details of the beds for this block.")
      .add()
      .appendInherited(
         new KeyedCodec<>("MovementSettings", BlockMovementSettings.CODEC),
         (blockType, o) -> blockType.movementSettings = o,
         blockType -> blockType.movementSettings,
         (blockType, parent) -> blockType.movementSettings = parent.movementSettings
      )
      .add()
      .appendInherited(
         new KeyedCodec<>(
            "Flags",
            BuilderCodec.builder(BlockFlags.class, BlockFlags::new)
               .appendInherited(
                  new KeyedCodec<>("IsUsable", Codec.BOOLEAN),
                  (blockFlags, b) -> blockFlags.isUsable = b,
                  blockFlags -> blockFlags.isUsable,
                  (blockFlags, parent) -> blockFlags.isUsable = parent.isUsable
               )
               .add()
               .appendInherited(
                  new KeyedCodec<>("IsStackable", Codec.BOOLEAN),
                  (blockFlags, b) -> blockFlags.isStackable = b,
                  blockFlags -> blockFlags.isStackable,
                  (blockFlags, parent) -> blockFlags.isStackable = parent.isStackable
               )
               .add()
               .build()
         ),
         (blockType, o) -> blockType.flags = o,
         blockType -> blockType.flags,
         (blockType, parent) -> blockType.flags = new BlockFlags(parent.flags)
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Bench", Bench.CODEC),
         (blockType, s) -> blockType.bench = s,
         blockType -> blockType.bench,
         (blockType, parent) -> blockType.bench = parent.bench
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Gathering", BlockGathering.CODEC),
         (blockType, s) -> blockType.gathering = s,
         blockType -> blockType.gathering,
         (blockType, parent) -> blockType.gathering = parent.gathering
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("PlacementSettings", BlockPlacementSettings.CODEC),
         (blockType, s) -> blockType.placementSettings = s,
         blockType -> blockType.placementSettings,
         (blockType, parent) -> blockType.placementSettings = parent.placementSettings
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Farming", FarmingData.CODEC),
         (blockType, farming) -> blockType.farming = farming,
         blockType -> blockType.farming,
         (blockType, parent) -> blockType.farming = parent.farming
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("IsDoor", Codec.BOOLEAN),
         (blockType, s) -> blockType.isDoor = s,
         blockType -> blockType.isDoor,
         (blockType, parent) -> blockType.isDoor = parent.isDoor
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AllowsMultipleUsers", Codec.BOOLEAN),
         (blockType, b) -> blockType.allowsMultipleUsers = b,
         blockType -> blockType.allowsMultipleUsers,
         (blockType, parent) -> blockType.allowsMultipleUsers = parent.allowsMultipleUsers
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("HitboxType", Codec.STRING),
         (blockType, o) -> blockType.hitboxType = o,
         blockType -> blockType.hitboxType,
         (blockType, parent) -> blockType.hitboxType = parent.hitboxType
      )
      .addValidator(BlockBoundingBoxes.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("InteractionHitboxType", Codec.STRING),
         (blockType, o) -> blockType.interactionHitboxType = o,
         blockType -> blockType.interactionHitboxType,
         (blockType, parent) -> blockType.interactionHitboxType = parent.interactionHitboxType
      )
      .addValidator(BlockBoundingBoxes.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("InteractionHint", Codec.STRING),
         (blockType, s) -> blockType.interactionHint = s,
         blockType -> blockType.interactionHint,
         (blockType, parent) -> blockType.interactionHint = parent.interactionHint
      )
      .documentation(
         "This property allows to specify custom text that will be displayed underneath the crosshair when the player aims at this block. The value of this property should be a reference to a translation. *{key}* will be replaced with the interaction input binding."
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("DamageToEntities", Codec.INTEGER),
         (blockType, s) -> blockType.damageToEntities = s,
         blockType -> blockType.damageToEntities,
         (blockType, parent) -> blockType.damageToEntities = parent.damageToEntities
      )
      .add()
      .<Map<InteractionType, String>>appendInherited(
         new KeyedCodec<>("Interactions", new EnumMapCodec<>(InteractionType.class, RootInteraction.CHILD_ASSET_CODEC)),
         (item, v) -> item.interactions = MapUtil.combineUnmodifiable(item.interactions, v, () -> new EnumMap<>(InteractionType.class)),
         item -> item.interactions,
         (item, parent) -> item.interactions = parent.interactions
      )
      .addValidator(RootInteraction.VALIDATOR_CACHE.getMapValueValidator())
      .metadata(new UIEditorSectionStart("Interactions"))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("BlockSoundSetId", Codec.STRING),
         (blockType, o) -> blockType.blockSoundSetId = o,
         blockType -> blockType.blockSoundSetId,
         (blockType, parent) -> blockType.blockSoundSetId = parent.blockSoundSetId
      )
      .documentation("Sets the **BlockSoundSet** that will be used for this block for various events e.g. placement, breaking")
      .addValidator(BlockSoundSet.VALIDATOR_CACHE.getValidator())
      .metadata(new UIEditorSectionStart("Sounds"))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("AmbientSoundEventId", Codec.STRING),
         (blockType, s) -> blockType.ambientSoundEventId = s,
         blockType -> blockType.ambientSoundEventId,
         (blockType, parent) -> blockType.ambientSoundEventId = parent.ambientSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .addValidator(SoundEventValidators.LOOPING)
      .documentation("A looping ambient sound event that emits from this block when placed in the world or held in-hand.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("InteractionSoundEventId", Codec.STRING),
         (blockType, s) -> blockType.interactionSoundEventId = s,
         blockType -> blockType.interactionSoundEventId,
         (blockType, parent) -> blockType.interactionSoundEventId = parent.interactionSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .addValidator(SoundEventValidators.ONESHOT)
      .documentation("A oneshot sound event that plays upon interaction with this block.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("Looping", Codec.BOOLEAN),
         (blockType, s) -> blockType.isLooping = s,
         blockType -> blockType.isLooping,
         (blockType, parent) -> blockType.isLooping = parent.isLooping
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .add()
      .<SupportDropType>appendInherited(
         new KeyedCodec<>("SupportDropType", SupportDropType.CODEC),
         (blockType, o) -> blockType.supportDropType = o,
         blockType -> blockType.supportDropType,
         (blockType, parent) -> blockType.supportDropType = parent.supportDropType
      )
      .metadata(new UIEditorSectionStart("Support"))
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("MaxSupportDistance", Codec.INTEGER),
         (blockType, i) -> blockType.maxSupportDistance = i,
         blockType -> blockType.maxSupportDistance,
         (blockType, parent) -> blockType.maxSupportDistance = parent.maxSupportDistance
      )
      .addValidator(Validators.range(0, 14))
      .add()
      .<BlockSupportsRequiredForType>appendInherited(
         new KeyedCodec<>("SupportsRequiredFor", new EnumCodec<>(BlockSupportsRequiredForType.class)),
         (blockType, o) -> blockType.blockSupportsRequiredFor = o,
         blockType -> blockType.blockSupportsRequiredFor,
         (blockType, parent) -> blockType.blockSupportsRequiredFor = parent.blockSupportsRequiredFor
      )
      .addValidator(Validators.nonNull())
      .add()
      .<Map<BlockFace, RequiredBlockFaceSupport[]>>appendInherited(
         new KeyedCodec<>(
            "Support",
            new MergedEnumMapCodec<>(
               BlockFace.class,
               MergedBlockFaces.class,
               MergedBlockFaces::getComponents,
               ArrayUtil::combine,
               new ArrayCodec<>(RequiredBlockFaceSupport.CODEC, RequiredBlockFaceSupport[]::new)
            )
         ),
         (blockType, o) -> blockType.support = o,
         blockType -> blockType.support,
         (blockType, parent) -> blockType.support = parent.support
      )
      .addValidator(RequiredBlockFaceSupportValidator.INSTANCE)
      .documentation(
         "A set of \"Required Support\" conditions. If met, the block won't fall off from block physics checks.\n*If this field is empty the block is automatically considered supported.*\n"
      )
      .add()
      .appendInherited(
         new KeyedCodec<>(
            "Supporting",
            new MergedEnumMapCodec<>(
               BlockFace.class,
               MergedBlockFaces.class,
               MergedBlockFaces::getComponents,
               ArrayUtil::combine,
               new ArrayCodec<>(BlockFaceSupport.CODEC, BlockFaceSupport[]::new)
            )
         ),
         (blockType, o) -> blockType.supporting = o,
         blockType -> blockType.supporting,
         (blockType, parent) -> blockType.supporting = parent.supporting
      )
      .add()
      .documentation("The counter-party to \"Support\". This block offers supporting faces which can match the face requirements of adjacent/nearby blocks.")
      .<Boolean>appendInherited(
         new KeyedCodec<>("IgnoreSupportWhenPlaced", Codec.BOOLEAN),
         (o, i) -> o.ignoreSupportWhenPlaced = i,
         o -> o.ignoreSupportWhenPlaced,
         (o, p) -> o.ignoreSupportWhenPlaced = p.ignoreSupportWhenPlaced
      )
      .documentation("Whether when this block is placed by a player that the support requirements should be ignored.")
      .add()
      .<String[]>append(
         new KeyedCodec<>("Aliases", new ArrayCodec<>(Codec.STRING, String[]::new)), (blockType, o) -> blockType.aliases = o, blockType -> blockType.aliases
      )
      .documentation("Specifies the alternatives names (aliases) for a block type for use in command matching")
      .add()
      .<Color[]>append(new KeyedCodec<>("Tint", ProtocolCodecs.COLOR_ARRAY), (blockType, o) -> {
         blockType.tintUp = o;
         blockType.tintDown = o;
         blockType.tintNorth = o;
         blockType.tintSouth = o;
         blockType.tintWest = o;
         blockType.tintEast = o;
      }, blockType -> null)
      .metadata(new UIEditorSectionStart("Tint"))
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MAP_GEOMETRY))
      .add()
      .<Color[]>appendInherited(
         new KeyedCodec<>("TintUp", ProtocolCodecs.COLOR_ARRAY),
         (blockType, o) -> blockType.tintUp = o,
         blockType -> blockType.tintUp,
         (blockType, parent) -> blockType.tintUp = parent.tintUp
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MAP_GEOMETRY))
      .add()
      .<Color[]>appendInherited(
         new KeyedCodec<>("TintDown", ProtocolCodecs.COLOR_ARRAY),
         (blockType, o) -> blockType.tintDown = o,
         blockType -> blockType.tintDown,
         (blockType, parent) -> blockType.tintDown = parent.tintDown
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MAP_GEOMETRY))
      .add()
      .<Color[]>appendInherited(
         new KeyedCodec<>("TintNorth", ProtocolCodecs.COLOR_ARRAY),
         (blockType, o) -> blockType.tintNorth = o,
         blockType -> blockType.tintNorth,
         (blockType, parent) -> blockType.tintNorth = parent.tintNorth
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MAP_GEOMETRY))
      .add()
      .<Color[]>appendInherited(
         new KeyedCodec<>("TintSouth", ProtocolCodecs.COLOR_ARRAY),
         (blockType, o) -> blockType.tintSouth = o,
         blockType -> blockType.tintSouth,
         (blockType, parent) -> blockType.tintSouth = parent.tintSouth
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MAP_GEOMETRY))
      .add()
      .<Color[]>appendInherited(
         new KeyedCodec<>("TintWest", ProtocolCodecs.COLOR_ARRAY),
         (blockType, o) -> blockType.tintWest = o,
         blockType -> blockType.tintWest,
         (blockType, parent) -> blockType.tintWest = parent.tintWest
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MAP_GEOMETRY))
      .add()
      .<Color[]>appendInherited(
         new KeyedCodec<>("TintEast", ProtocolCodecs.COLOR_ARRAY),
         (blockType, o) -> blockType.tintEast = o,
         blockType -> blockType.tintEast,
         (blockType, parent) -> blockType.tintEast = parent.tintEast
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MAP_GEOMETRY))
      .add()
      .<Integer>append(new KeyedCodec<>("BiomeTint", Codec.INTEGER), (blockType, o) -> {
         blockType.biomeTintUp = o;
         blockType.biomeTintDown = o;
         blockType.biomeTintNorth = o;
         blockType.biomeTintSouth = o;
         blockType.biomeTintWest = o;
         blockType.biomeTintEast = o;
      }, blockType -> null)
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MAP_GEOMETRY))
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("BiomeTintUp", Codec.INTEGER),
         (blockType, o) -> blockType.biomeTintUp = o,
         blockType -> blockType.biomeTintUp,
         (blockType, parent) -> blockType.biomeTintUp = parent.biomeTintUp
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MAP_GEOMETRY))
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("BiomeTintDown", Codec.INTEGER),
         (blockType, o) -> blockType.biomeTintDown = o,
         blockType -> blockType.biomeTintDown,
         (blockType, parent) -> blockType.biomeTintDown = parent.biomeTintDown
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MAP_GEOMETRY))
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("BiomeTintNorth", Codec.INTEGER),
         (blockType, o) -> blockType.biomeTintNorth = o,
         blockType -> blockType.biomeTintNorth,
         (blockType, parent) -> blockType.biomeTintNorth = parent.biomeTintNorth
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MAP_GEOMETRY))
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("BiomeTintSouth", Codec.INTEGER),
         (blockType, o) -> blockType.biomeTintSouth = o,
         blockType -> blockType.biomeTintSouth,
         (blockType, parent) -> blockType.biomeTintSouth = parent.biomeTintSouth
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MAP_GEOMETRY))
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("BiomeTintWest", Codec.INTEGER),
         (blockType, o) -> blockType.biomeTintWest = o,
         blockType -> blockType.biomeTintWest,
         (blockType, parent) -> blockType.biomeTintWest = parent.biomeTintWest
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MAP_GEOMETRY))
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("BiomeTintEast", Codec.INTEGER),
         (blockType, o) -> blockType.biomeTintEast = o,
         blockType -> blockType.biomeTintEast,
         (blockType, parent) -> blockType.biomeTintEast = parent.biomeTintEast
      )
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MAP_GEOMETRY))
      .add()
      .<StateData>appendInherited(new KeyedCodec<>("State", StateData.CODEC), (blockType, s) -> {
         s.copyFrom(blockType.state);
         blockType.state = s;
      }, blockType -> blockType.state, (blockType, parent) -> blockType.state = parent.state)
      .metadata(new UIEditorSectionStart("State"))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<Holder<ChunkStore>>appendInherited(
         new KeyedCodec<>("BlockEntity", new StoredCodec<>(ChunkStore.HOLDER_CODEC_KEY)),
         (blockType, s) -> blockType.blockEntity = s,
         blockType -> blockType.blockEntity,
         (blockType, parent) -> blockType.blockEntity = parent.blockEntity
      )
      .metadata(new UIEditorSectionStart("Components"))
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .appendInherited(
         new KeyedCodec<>("Rail", ProtocolCodecs.RAIL_CONFIG_CODEC), (o, v) -> o.railConfig = v, o -> o.railConfig, (o, p) -> o.railConfig = p.railConfig
      )
      .add()
      .afterDecode(BlockType::processConfig)
      .build();
   public static final String[] EMPTY_ALIAS_LIST = new String[0];
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(BlockType::getAssetStore));
   public static final String UNKNOWN_TEXTURE = "BlockTextures/Unknown.png";
   public static final ModelTexture[] UNKNOWN_CUSTOM_MODEL_TEXTURE = new ModelTexture[]{new ModelTexture("BlockTextures/Unknown.png", 1.0F)};
   public static final BlockTextures[] UNKNOWN_BLOCK_TEXTURES = new BlockTextures[]{
      new BlockTextures(
         "BlockTextures/Unknown.png",
         "BlockTextures/Unknown.png",
         "BlockTextures/Unknown.png",
         "BlockTextures/Unknown.png",
         "BlockTextures/Unknown.png",
         "BlockTextures/Unknown.png",
         1.0F
      )
   };
   public static final Map<BlockFace, RequiredBlockFaceSupport[]> REQUIRED_BOTTOM_FACE_SUPPORT = Collections.unmodifiableMap(
      new EnumMap<BlockFace, RequiredBlockFaceSupport[]>(BlockFace.class) {
         {
            this.put(BlockFace.DOWN, new RequiredBlockFaceSupport[]{new RequiredBlockFaceSupport("Full")});
         }
      }
   );
   public static final BlockFaceSupport[] BLOCK_FACE_SUPPORT_ALL_ARRAY = new BlockFaceSupport[]{BlockFaceSupport.ALL};
   public static final Map<BlockFace, BlockFaceSupport[]> ALL_SUPPORTING_FACES = Collections.unmodifiableMap(
      new EnumMap<BlockFace, BlockFaceSupport[]>(BlockFace.class) {
         {
            for (BlockFace blockFace : BlockFace.VALUES) {
               this.put(blockFace, BlockType.BLOCK_FACE_SUPPORT_ALL_ARRAY);
            }
         }
      }
   );
   public static final ShaderType[] DEFAULT_SHADER_EFFECTS = new ShaderType[]{ShaderType.None};
   public static final BlockType DEFAULT_BLOCK_TYPE = new BlockType();
   public static final ISectionPalette.KeySerializer KEY_SERIALIZER = (buf, id) -> {
      String key = getAssetMap().getAssetOrDefault(id, BlockType.UNKNOWN).getId();
      ByteBufUtil.writeUTF(buf, key);
   };
   public static final ToIntFunction<ByteBuf> KEY_DESERIALIZER = byteBuf -> {
      String blockType = ByteBufUtil.readUTF(byteBuf);
      return getBlockIdOrUnknown(blockType, "Failed to find block '%s' in chunk section!", blockType);
   };
   public static final String EMPTY_KEY = "Empty";
   public static final String UNKNOWN_KEY = "Unknown";
   public static final String DEBUG_CUBE_KEY = "Debug_Cube";
   public static final String DEBUG_MODEL_KEY = "Debug_Model";
   public static final int EMPTY_ID = 0;
   public static final BlockType EMPTY = new BlockType("Empty") {
      {
         this.drawType = DrawType.Empty;
         this.material = BlockMaterial.Empty;
         this.opacity = Opacity.Transparent;
         this.group = "Air";
         this.support = Collections.emptyMap();
         this.processConfig();
      }
   };
   public static final int UNKNOWN_ID = 1;
   public static final BlockType UNKNOWN = new BlockType("Unknown") {
      {
         this.unknown = true;
         this.drawType = DrawType.Cube;
         this.material = BlockMaterial.Solid;
         this.processConfig();
      }
   };
   public static final int DEBUG_CUBE_ID = 2;
   public static final BlockType DEBUG_CUBE = new BlockType("Debug_Cube") {
      {
         this.drawType = DrawType.Cube;
         this.material = BlockMaterial.Solid;
         this.variantRotation = VariantRotation.Debug;
         this.textures = new BlockTypeTextures[]{
            new BlockTypeTextures(
               "BlockTextures/_Debug/Up.png",
               "BlockTextures/_Debug/Down.png",
               "BlockTextures/_Debug/North.png",
               "BlockTextures/_Debug/South.png",
               "BlockTextures/_Debug/East.png",
               "BlockTextures/_Debug/West.png",
               1
            )
         };
         this.processConfig();
      }
   };
   public static final int DEBUG_MODEL_ID = 3;
   public static final BlockType DEBUG_MODEL = new BlockType("Debug_Model") {
      {
         this.drawType = DrawType.Model;
         this.material = BlockMaterial.Empty;
         this.variantRotation = VariantRotation.Debug;
         this.customModel = "Blocks/_Debug/Model.blockymodel";
         this.customModelTexture = new CustomModelTexture[]{new CustomModelTexture("Blocks/_Debug/Texture.png", 1)};
         this.processConfig();
      }
   };
   public static final String TECHNICAL_BLOCK_GROUP = "@Tech";
   private static AssetStore<String, BlockType, BlockTypeAssetMap<String, BlockType>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected boolean unknown;
   protected String group;
   protected String blockListAssetId;
   protected String prefabListAssetId;
   protected String blockSoundSetId = "EMPTY";
   protected transient int blockSoundSetIndex = 0;
   protected ModelParticle[] particles;
   protected String blockParticleSetId;
   protected String blockBreakingDecalId;
   protected Color particleColor;
   protected TickProcedure tickProcedure;
   private RandomTickProcedure randomTickProcedure;
   protected ShaderType[] effect;
   protected BlockTypeTextures[] textures;
   protected String textureSideMask;
   @Nonnull
   protected ShadingMode cubeShadingMode = ShadingMode.Standard;
   @Nullable
   protected String customModel;
   @Nullable
   protected CustomModelTexture[] customModelTexture;
   protected float customModelScale = 1.0F;
   protected String customModelAnimation;
   @Nonnull
   protected DrawType drawType = DrawType.Cube;
   @Nonnull
   protected BlockMaterial material = BlockMaterial.Empty;
   @Nonnull
   protected Opacity opacity = Opacity.Solid;
   protected boolean requiresAlphaBlending;
   protected Color[] tintUp;
   protected Color[] tintDown;
   protected Color[] tintNorth;
   protected Color[] tintSouth;
   protected Color[] tintWest;
   protected Color[] tintEast;
   protected int biomeTintUp;
   protected int biomeTintDown;
   protected int biomeTintNorth;
   protected int biomeTintSouth;
   protected int biomeTintWest;
   protected int biomeTintEast;
   @Nonnull
   protected BlockSupportsRequiredForType blockSupportsRequiredFor = BlockSupportsRequiredForType.All;
   @Nonnull
   protected RandomRotation randomRotation = RandomRotation.None;
   @Nonnull
   protected VariantRotation variantRotation = VariantRotation.None;
   protected BlockFlipType flipType = BlockFlipType.SYMMETRIC;
   @Nonnull
   protected Rotation rotationYawPlacementOffset = Rotation.None;
   @Nullable
   protected RotatedMountPointsArray seats;
   @Nullable
   protected RotatedMountPointsArray beds;
   protected String transitionTexture;
   protected String[] transitionToGroups;
   protected String transitionToTag;
   protected String hitboxType = "Full";
   protected transient int hitboxTypeIndex = 0;
   @Nullable
   protected String interactionHitboxType;
   protected transient int interactionHitboxTypeIndex = Integer.MIN_VALUE;
   protected ColorLight light;
   protected BlockMovementSettings movementSettings = new BlockMovementSettings();
   protected BlockFlags flags = new BlockFlags(false, true);
   protected String interactionHint;
   protected boolean isTrigger;
   @Deprecated
   protected boolean isDoor;
   protected int damageToEntities;
   protected boolean allowsMultipleUsers = true;
   @Nullable
   protected ConnectedBlockRuleSet connectedBlockRuleSet;
   protected Bench bench;
   protected BlockGathering gathering;
   protected BlockPlacementSettings placementSettings;
   protected StateData state;
   protected String ambientSoundEventId;
   protected transient int ambientSoundEventIndex;
   protected String interactionSoundEventId;
   protected transient int interactionSoundEventIndex;
   protected boolean isLooping;
   protected Holder<ChunkStore> blockEntity;
   protected FarmingData farming;
   protected SupportDropType supportDropType = SupportDropType.BREAK;
   protected int maxSupportDistance;
   @Nullable
   protected Map<BlockFace, RequiredBlockFaceSupport[]> support;
   @Nullable
   protected transient Map<BlockFace, RequiredBlockFaceSupport[]>[] rotatedSupport;
   @Nullable
   protected Map<BlockFace, BlockFaceSupport[]> supporting;
   @Nullable
   protected transient Map<BlockFace, BlockFaceSupport[]>[] rotatedSupporting;
   protected boolean ignoreSupportWhenPlaced;
   protected Map<InteractionType, String> interactions = Collections.emptyMap();
   @Nullable
   protected RailConfig railConfig;
   @Nullable
   protected RailConfig[] rotatedRailConfig;
   protected String[] aliases = EMPTY_ALIAS_LIST;
   @Nullable
   private transient String defaultStateKey;
   @Nullable
   private transient SoftReference<com.hypixel.hytale.protocol.BlockType> cachedPacket;

   @Nullable
   public static BlockType fromString(@Nonnull String input) {
      return getAssetMap().getAsset(input);
   }

   public static AssetStore<String, BlockType, BlockTypeAssetMap<String, BlockType>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(BlockType.class);
      }

      return ASSET_STORE;
   }

   public static BlockTypeAssetMap<String, BlockType> getAssetMap() {
      return (BlockTypeAssetMap<String, BlockType>)getAssetStore().getAssetMap();
   }

   public BlockType() {
   }

   public BlockType(String id) {
      this.id = id;
   }

   public BlockType(@Nonnull BlockType other) {
      this.data = other.data;
      this.id = other.id;
      this.unknown = other.unknown;
      this.group = other.group;
      this.blockSoundSetId = other.blockSoundSetId;
      this.blockSoundSetIndex = other.blockSoundSetIndex;
      this.particles = other.particles;
      this.blockParticleSetId = other.blockParticleSetId;
      this.blockBreakingDecalId = other.blockBreakingDecalId;
      this.particleColor = other.particleColor;
      this.tickProcedure = other.tickProcedure;
      this.effect = other.effect;
      this.textures = other.textures;
      this.textureSideMask = other.textureSideMask;
      this.customModelTexture = other.customModelTexture;
      this.drawType = other.drawType;
      this.material = other.material;
      this.opacity = other.opacity;
      this.requiresAlphaBlending = other.requiresAlphaBlending;
      this.customModel = other.customModel;
      this.customModelScale = other.customModelScale;
      this.customModelAnimation = other.customModelAnimation;
      this.tintUp = other.tintUp;
      this.tintDown = other.tintDown;
      this.tintNorth = other.tintNorth;
      this.tintSouth = other.tintSouth;
      this.tintWest = other.tintWest;
      this.tintEast = other.tintEast;
      this.biomeTintUp = other.biomeTintUp;
      this.biomeTintDown = other.biomeTintDown;
      this.biomeTintNorth = other.biomeTintNorth;
      this.biomeTintSouth = other.biomeTintSouth;
      this.biomeTintWest = other.biomeTintWest;
      this.biomeTintEast = other.biomeTintEast;
      this.randomRotation = other.randomRotation;
      this.variantRotation = other.variantRotation;
      this.flipType = other.flipType;
      this.rotationYawPlacementOffset = other.rotationYawPlacementOffset;
      this.seats = other.seats;
      this.transitionTexture = other.transitionTexture;
      this.transitionToGroups = other.transitionToGroups;
      this.transitionToTag = other.transitionToTag;
      this.hitboxType = other.hitboxType;
      this.hitboxTypeIndex = other.hitboxTypeIndex;
      this.interactionHitboxType = other.interactionHitboxType;
      this.interactionHitboxTypeIndex = other.interactionHitboxTypeIndex;
      this.light = other.light;
      this.movementSettings = other.movementSettings;
      this.flags = other.flags;
      this.interactionHint = other.interactionHint;
      this.isTrigger = other.isTrigger;
      this.damageToEntities = other.damageToEntities;
      this.bench = other.bench;
      this.gathering = other.gathering;
      this.placementSettings = other.placementSettings;
      this.state = other.state;
      this.blockEntity = other.blockEntity;
      this.farming = other.farming;
      this.supportDropType = other.supportDropType;
      this.maxSupportDistance = other.maxSupportDistance;
      this.support = other.support;
      this.supporting = other.supporting;
      this.cubeShadingMode = other.cubeShadingMode;
      this.allowsMultipleUsers = other.allowsMultipleUsers;
      this.interactions = other.interactions;
      this.ambientSoundEventId = other.ambientSoundEventId;
      this.ambientSoundEventIndex = other.ambientSoundEventIndex;
      this.interactionSoundEventId = other.interactionSoundEventId;
      this.interactionSoundEventIndex = other.interactionSoundEventIndex;
      this.isLooping = other.isLooping;
      this.isDoor = other.isDoor;
      this.blockSupportsRequiredFor = other.blockSupportsRequiredFor;
      this.connectedBlockRuleSet = other.connectedBlockRuleSet;
      this.railConfig = other.railConfig;
      this.ignoreSupportWhenPlaced = other.ignoreSupportWhenPlaced;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.BlockType toPacket() {
      com.hypixel.hytale.protocol.BlockType cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         BlockTypeAssetMap<String, BlockType> blockTypeAssetMap = getAssetMap();
         com.hypixel.hytale.protocol.BlockType packet = new com.hypixel.hytale.protocol.BlockType();
         packet.name = this.id;
         Item item = this.getItem();
         if (item != null) {
            packet.item = item.getId();
         }

         packet.unknown = this.unknown;
         if (this.group != null) {
            packet.group = getAssetMap().getGroupId(this.group);
         }

         packet.blockSoundSetIndex = this.blockSoundSetIndex;
         packet.blockParticleSetId = this.blockParticleSetId;
         packet.blockBreakingDecalId = this.blockBreakingDecalId;
         packet.particleColor = this.particleColor;
         if (this.support != null) {
            Object2ObjectOpenHashMap<BlockNeighbor, com.hypixel.hytale.protocol.RequiredBlockFaceSupport[]> supportMap = new Object2ObjectOpenHashMap();

            for (Entry<BlockFace, RequiredBlockFaceSupport[]> entry : this.support.entrySet()) {
               RequiredBlockFaceSupport[] supports = entry.getValue();
               com.hypixel.hytale.protocol.RequiredBlockFaceSupport[] protocolSupports = new com.hypixel.hytale.protocol.RequiredBlockFaceSupport[supports.length];

               for (int i = 0; i < supports.length; i++) {
                  protocolSupports[i] = supports[i].toPacket();
               }

               supportMap.put(entry.getKey().toProtocolBlockNeighbor(), protocolSupports);
            }

            packet.support = supportMap;
         }

         if (this.supporting != null) {
            Object2ObjectOpenHashMap<BlockNeighbor, com.hypixel.hytale.protocol.BlockFaceSupport[]> supportingMap = new Object2ObjectOpenHashMap();

            for (Entry<BlockFace, BlockFaceSupport[]> entry : this.supporting.entrySet()) {
               BlockFaceSupport[] blockFaceSupports = entry.getValue();
               com.hypixel.hytale.protocol.BlockFaceSupport[] protocolBlockFaceSupports = new com.hypixel.hytale.protocol.BlockFaceSupport[blockFaceSupports.length];

               for (int i = 0; i < blockFaceSupports.length; i++) {
                  protocolBlockFaceSupports[i] = blockFaceSupports[i].toPacket();
               }

               supportingMap.put(entry.getKey().toProtocolBlockNeighbor(), protocolBlockFaceSupports);
            }

            packet.supporting = supportingMap;
         }
         packet.blockSupportsRequiredFor = switch (this.blockSupportsRequiredFor) {
            case Any -> com.hypixel.hytale.protocol.BlockSupportsRequiredForType.Any;
            case All -> com.hypixel.hytale.protocol.BlockSupportsRequiredForType.All;
         };
         packet.maxSupportDistance = this.maxSupportDistance;
         if (this.effect != null && this.effect.length > 0) {
            packet.shaderEffect = this.effect;
         } else {
            packet.shaderEffect = DEFAULT_SHADER_EFFECTS;
         }

         if (this.textures != null && this.textures.length > 0) {
            int totalWeight = 0;

            for (BlockTypeTextures texture : this.textures) {
               totalWeight = (int)(totalWeight + texture.getWeight());
            }

            BlockTextures[] texturePackets = new BlockTextures[this.textures.length];

            for (int i = 0; i < this.textures.length; i++) {
               texturePackets[i] = this.textures[i].toPacket(totalWeight);
            }

            packet.cubeTextures = texturePackets;
         } else {
            packet.cubeTextures = UNKNOWN_BLOCK_TEXTURES;
         }

         packet.cubeSideMaskTexture = this.textureSideMask;
         packet.cubeShadingMode = this.cubeShadingMode;
         if (this.customModelTexture != null && this.customModelTexture.length > 0) {
            int totalWeight = 0;

            for (CustomModelTexture modelTexture : this.customModelTexture) {
               totalWeight += modelTexture.getWeight();
            }

            ModelTexture[] texturePackets = new ModelTexture[this.customModelTexture.length];

            for (int i = 0; i < this.customModelTexture.length; i++) {
               texturePackets[i] = this.customModelTexture[i].toPacket(totalWeight);
            }

            packet.modelTexture = texturePackets;
         } else {
            packet.modelTexture = UNKNOWN_CUSTOM_MODEL_TEXTURE;
         }

         packet.drawType = this.drawType;
         packet.requiresAlphaBlending = this.requiresAlphaBlending;
         if (this.customModel != null) {
            packet.model = this.customModel;
         }

         packet.modelScale = this.customModelScale;
         if (this.customModelAnimation != null) {
            packet.modelAnimation = this.customModelAnimation;
         }

         packet.tint = new Tint();
         if (this.tintUp != null && this.tintUp.length > 0) {
            packet.tint.top = ColorParseUtil.colorToARGBInt(this.tintUp[0]);
         } else {
            packet.tint.top = -1;
         }

         if (this.tintDown != null && this.tintDown.length > 0) {
            packet.tint.bottom = ColorParseUtil.colorToARGBInt(this.tintDown[0]);
         } else {
            packet.tint.bottom = -1;
         }

         if (this.tintNorth != null && this.tintNorth.length > 0) {
            packet.tint.back = ColorParseUtil.colorToARGBInt(this.tintNorth[0]);
         } else {
            packet.tint.back = -1;
         }

         if (this.tintSouth != null && this.tintSouth.length > 0) {
            packet.tint.front = ColorParseUtil.colorToARGBInt(this.tintSouth[0]);
         } else {
            packet.tint.front = -1;
         }

         if (this.tintWest != null && this.tintWest.length > 0) {
            packet.tint.left = ColorParseUtil.colorToARGBInt(this.tintWest[0]);
         } else {
            packet.tint.left = -1;
         }

         if (this.tintEast != null && this.tintEast.length > 0) {
            packet.tint.right = ColorParseUtil.colorToARGBInt(this.tintEast[0]);
         } else {
            packet.tint.right = -1;
         }

         packet.biomeTint = new Tint(this.biomeTintUp, this.biomeTintDown, this.biomeTintSouth, this.biomeTintNorth, this.biomeTintWest, this.biomeTintEast);
         packet.variantRotation = this.variantRotation.toPacket();
         packet.randomRotation = this.randomRotation;
         packet.rotationYawPlacementOffset = this.rotationYawPlacementOffset.toPacket();
         packet.opacity = this.opacity;
         if (this.transitionTexture != null) {
            packet.transitionTexture = this.transitionTexture;
         }

         if (this.transitionToGroups != null && this.transitionToGroups.length > 0) {
            int[] arr = new int[this.transitionToGroups.length];

            for (int i = 0; i < this.transitionToGroups.length; i++) {
               arr[i] = blockTypeAssetMap.getGroupId(this.transitionToGroups[i]);
            }

            packet.transitionToGroups = arr;
         }

         if (this.transitionToTag != null) {
            packet.transitionToTag = AssetRegistry.getOrCreateTagIndex(this.transitionToTag);
         } else {
            packet.transitionToTag = Integer.MIN_VALUE;
         }

         packet.material = this.material;
         packet.hitbox = this.hitboxTypeIndex;
         packet.interactionHitbox = this.interactionHitboxTypeIndex;
         packet.light = this.light;
         packet.movementSettings = this.movementSettings.toPacket();
         packet.flags = this.flags;
         packet.interactionHint = this.interactionHint;
         if (this.gathering != null) {
            packet.gathering = this.gathering.toPacket();
         }

         if (this.placementSettings != null) {
            packet.placementSettings = this.placementSettings.toPacket();
         }

         packet.looping = this.isLooping;
         packet.ambientSoundEventIndex = this.ambientSoundEventIndex;
         if (this.particles != null && this.particles.length > 0) {
            packet.particles = new com.hypixel.hytale.protocol.ModelParticle[this.particles.length];

            for (int i = 0; i < this.particles.length; i++) {
               packet.particles[i] = this.particles[i].toPacket();
            }
         }

         Object2IntOpenHashMap<InteractionType> interactionMap = new Object2IntOpenHashMap();

         for (Entry<InteractionType, String> e : this.interactions.entrySet()) {
            interactionMap.put(e.getKey(), RootInteraction.getRootInteractionIdOrUnknown(e.getValue()));
         }

         packet.interactions = interactionMap;
         if (this.state != null) {
            packet.states = this.state.toPacket(this);
            String def = this.getBlockKeyForState("default");
            if (def != null) {
               packet.states.put("default", getAssetMap().getIndex(def));
            }
         }

         if (this.data != null) {
            IntSet tags = this.data.getExpandedTagIndexes();
            if (tags != null) {
               packet.tagIndexes = tags.toIntArray();
            }
         }

         packet.rail = this.railConfig;
         packet.ignoreSupportWhenPlaced = this.ignoreSupportWhenPlaced;
         if (this.bench != null) {
            packet.bench = this.bench.toPacket();
         }

         if (this.connectedBlockRuleSet != null) {
            packet.connectedBlockRuleSet = this.connectedBlockRuleSet.toPacket(blockTypeAssetMap);
         }

         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   public String getId() {
      return this.id;
   }

   public AssetExtraInfo.Data getData() {
      return this.data;
   }

   @Nullable
   public Item getItem() {
      if (this.data == null) {
         return null;
      } else {
         String itemKey = this.data.getContainerKey(Item.class);
         return itemKey == null ? null : Item.getAssetMap().getAsset(itemKey);
      }
   }

   public boolean isState() {
      return this.getStateForBlock(this.id) != null;
   }

   @Nullable
   public BlockType getBlockForState(@Nonnull String state) {
      String key = this.getBlockKeyForState(state);
      return key == null ? null : getAssetMap().getAsset(key);
   }

   @Nullable
   public String getBlockKeyForState(@Nonnull String state) {
      String key;
      if (state.equals("default")) {
         key = this.getDefaultStateKey();
      } else {
         key = this.state != null ? this.state.getBlockForState(state) : null;
      }

      return key;
   }

   public String getDefaultStateKey() {
      if (this.defaultStateKey == null) {
         this.defaultStateKey = this.data.getContainerKey(BlockType.class);
      }

      return this.defaultStateKey;
   }

   @Nullable
   public String getStateForBlock(@Nonnull BlockType blockType) {
      return this.getStateForBlock(blockType.getId());
   }

   @Nullable
   public String getStateForBlock(@Nonnull String blockTypeKey) {
      return this.state != null ? this.state.getStateForBlock(blockTypeKey) : null;
   }

   public boolean isUnknown() {
      return this.unknown;
   }

   public String getGroup() {
      return this.group;
   }

   public String getBlockSoundSetId() {
      return this.blockSoundSetId;
   }

   public int getBlockSoundSetIndex() {
      return this.blockSoundSetIndex;
   }

   public ModelParticle[] getParticles() {
      return this.particles;
   }

   public String getBlockParticleSetId() {
      return this.blockParticleSetId;
   }

   public String getBlockBreakingDecalId() {
      return this.blockBreakingDecalId;
   }

   public Color getParticleColor() {
      return this.particleColor;
   }

   public TickProcedure getTickProcedure() {
      return this.tickProcedure;
   }

   public RandomTickProcedure getRandomTickProcedure() {
      return this.randomTickProcedure;
   }

   public ShaderType[] getEffect() {
      return this.effect;
   }

   public BlockTypeTextures[] getTextures() {
      return this.textures;
   }

   public String getTextureSideMask() {
      return this.textureSideMask;
   }

   @Nullable
   public CustomModelTexture[] getCustomModelTexture() {
      return this.customModelTexture;
   }

   public DrawType getDrawType() {
      return this.drawType;
   }

   public BlockMaterial getMaterial() {
      return this.material;
   }

   public Opacity getOpacity() {
      return this.opacity;
   }

   @Nullable
   public String getCustomModel() {
      return this.customModel;
   }

   public float getCustomModelScale() {
      return this.customModelScale;
   }

   public String getCustomModelAnimation() {
      return this.customModelAnimation;
   }

   public Color[] getTintUp() {
      return this.tintUp;
   }

   public Color[] getTintDown() {
      return this.tintDown;
   }

   public Color[] getTintNorth() {
      return this.tintNorth;
   }

   public Color[] getTintSouth() {
      return this.tintSouth;
   }

   public Color[] getTintWest() {
      return this.tintWest;
   }

   public Color[] getTintEast() {
      return this.tintEast;
   }

   public int getBiomeTintUp() {
      return this.biomeTintUp;
   }

   public int getBiomeTintDown() {
      return this.biomeTintDown;
   }

   public int getBiomeTintNorth() {
      return this.biomeTintNorth;
   }

   public int getBiomeTintSouth() {
      return this.biomeTintSouth;
   }

   public int getBiomeTintWest() {
      return this.biomeTintWest;
   }

   public int getBiomeTintEast() {
      return this.biomeTintEast;
   }

   @Nullable
   public ConnectedBlockRuleSet getConnectedBlockRuleSet() {
      return this.connectedBlockRuleSet;
   }

   public BlockSupportsRequiredForType getBlockSupportsRequiredFor() {
      return this.blockSupportsRequiredFor;
   }

   public RandomRotation getRandomRotation() {
      return this.randomRotation;
   }

   @Nonnull
   public VariantRotation getVariantRotation() {
      return this.variantRotation;
   }

   public BlockFlipType getFlipType() {
      return this.flipType;
   }

   public Rotation getRotationYawPlacementOffset() {
      return this.rotationYawPlacementOffset;
   }

   @Nullable
   public RotatedMountPointsArray getSeats() {
      return this.seats;
   }

   @Nullable
   public RotatedMountPointsArray getBeds() {
      return this.beds;
   }

   public String getTransitionTexture() {
      return this.transitionTexture;
   }

   public String[] getTransitionToGroups() {
      return this.transitionToGroups;
   }

   public String getBlockListAssetId() {
      return this.blockListAssetId;
   }

   public String getPrefabListAssetId() {
      return this.prefabListAssetId;
   }

   public String getHitboxType() {
      return this.hitboxType;
   }

   public int getHitboxTypeIndex() {
      return this.hitboxTypeIndex;
   }

   @Nullable
   public String getInteractionHitboxType() {
      return this.interactionHitboxType;
   }

   public int getInteractionHitboxTypeIndex() {
      return this.interactionHitboxTypeIndex;
   }

   public ColorLight getLight() {
      return this.light;
   }

   public BlockMovementSettings getMovementSettings() {
      return this.movementSettings;
   }

   public BlockFlags getFlags() {
      return this.flags;
   }

   public String getInteractionHint() {
      return this.interactionHint;
   }

   public boolean isTrigger() {
      return this.isTrigger;
   }

   public int getDamageToEntities() {
      return this.damageToEntities;
   }

   public Bench getBench() {
      return this.bench;
   }

   public BlockGathering getGathering() {
      return this.gathering;
   }

   public BlockPlacementSettings getPlacementSettings() {
      return this.placementSettings;
   }

   public StateData getState() {
      return this.state;
   }

   public Holder<ChunkStore> getBlockEntity() {
      return this.blockEntity;
   }

   public String getAmbientSoundEventId() {
      return this.ambientSoundEventId;
   }

   public int getAmbientSoundEventIndex() {
      return this.ambientSoundEventIndex;
   }

   public String getInteractionSoundEventId() {
      return this.interactionSoundEventId;
   }

   public int getInteractionSoundEventIndex() {
      return this.interactionSoundEventIndex;
   }

   public boolean isLooping() {
      return this.isLooping;
   }

   public FarmingData getFarming() {
      return this.farming;
   }

   public SupportDropType getSupportDropType() {
      return this.supportDropType;
   }

   public int getMaxSupportDistance() {
      return this.maxSupportDistance;
   }

   public boolean isFullySupportive() {
      return this.supporting == ALL_SUPPORTING_FACES;
   }

   @Nullable
   public Map<BlockFace, RequiredBlockFaceSupport[]> getSupport(int rotationIndex) {
      if (this.support != null && rotationIndex != 0) {
         if (this.rotatedSupport == null) {
            this.rotatedSupport = new Map[RotationTuple.VALUES.length];
         }

         Map<BlockFace, RequiredBlockFaceSupport[]> rotatedSupportArray = this.rotatedSupport[rotationIndex];
         if (rotatedSupportArray == null) {
            RotationTuple rotation = RotationTuple.get(rotationIndex);
            Map<BlockFace, List<RequiredBlockFaceSupport>> rotatedSupport = new EnumMap<>(BlockFace.class);

            for (Entry<BlockFace, RequiredBlockFaceSupport[]> entry : this.support.entrySet()) {
               BlockFace blockFace = entry.getKey();
               RequiredBlockFaceSupport[] requiredBlockFaceSupports = entry.getValue();
               BlockFace rotatedBlockFace = BlockFace.rotate(blockFace, rotation.yaw(), rotation.pitch(), rotation.roll());

               for (RequiredBlockFaceSupport requiredBlockFaceSupport : requiredBlockFaceSupports) {
                  if (requiredBlockFaceSupport.isRotated()) {
                     RequiredBlockFaceSupport rotatedRequiredBlockFaceSupport = RequiredBlockFaceSupport.rotate(
                        requiredBlockFaceSupport, rotation.yaw(), rotation.pitch(), rotation.roll()
                     );
                     rotatedSupport.computeIfAbsent(rotatedBlockFace, k -> new ObjectArrayList()).add(rotatedRequiredBlockFaceSupport);
                  } else {
                     rotatedSupport.computeIfAbsent(blockFace, k -> new ObjectArrayList()).add(requiredBlockFaceSupport);
                  }
               }
            }

            rotatedSupportArray = new EnumMap<>(BlockFace.class);

            for (Entry<BlockFace, List<RequiredBlockFaceSupport>> entry : rotatedSupport.entrySet()) {
               rotatedSupportArray.put(entry.getKey(), entry.getValue().toArray(RequiredBlockFaceSupport[]::new));
            }

            this.rotatedSupport[rotationIndex] = rotatedSupportArray;
         }

         return rotatedSupportArray;
      } else {
         return this.support;
      }
   }

   @Nullable
   public Map<BlockFace, BlockFaceSupport[]> getSupporting(int rotationIndex) {
      if (this.supporting != null && rotationIndex != 0) {
         if (this.rotatedSupporting == null) {
            this.rotatedSupporting = new Map[RotationTuple.VALUES.length];
         }

         Map<BlockFace, BlockFaceSupport[]> rotatedSupportingArray = this.rotatedSupporting[rotationIndex];
         if (rotatedSupportingArray == null) {
            RotationTuple rotation = RotationTuple.get(rotationIndex);
            if (this.isFullySupportive()) {
               rotatedSupportingArray = ALL_SUPPORTING_FACES;
            } else {
               Map<BlockFace, List<BlockFaceSupport>> rotatedSupporting = new EnumMap<>(BlockFace.class);

               for (Entry<BlockFace, BlockFaceSupport[]> entry : this.supporting.entrySet()) {
                  BlockFace blockFace = entry.getKey();
                  BlockFaceSupport[] blockFaceSupports = entry.getValue();
                  BlockFace rotatedBlockFace = BlockFace.rotate(blockFace, rotation.yaw(), rotation.pitch(), rotation.roll());

                  for (BlockFaceSupport blockFaceSupport : blockFaceSupports) {
                     BlockFaceSupport rotatedBlockFaceSupport = BlockFaceSupport.rotate(blockFaceSupport, rotation.yaw(), rotation.pitch(), rotation.roll());
                     rotatedSupporting.computeIfAbsent(rotatedBlockFace, k -> new ObjectArrayList()).add(rotatedBlockFaceSupport);
                  }
               }

               rotatedSupportingArray = new EnumMap<>(BlockFace.class);

               for (Entry<BlockFace, List<BlockFaceSupport>> entry : rotatedSupporting.entrySet()) {
                  rotatedSupportingArray.put(entry.getKey(), entry.getValue().toArray(BlockFaceSupport[]::new));
               }
            }

            this.rotatedSupporting[rotationIndex] = rotatedSupportingArray;
         }

         return rotatedSupportingArray;
      } else {
         return this.supporting;
      }
   }

   public boolean hasSupport() {
      return this.support != null && !this.support.isEmpty() || this.maxSupportDistance > 0;
   }

   public boolean isAllowsMultipleUsers() {
      return this.allowsMultipleUsers;
   }

   public Map<InteractionType, String> getInteractions() {
      return this.interactions;
   }

   @Nullable
   public RailConfig getRailConfig(int rotationIndex) {
      if (this.railConfig != null && rotationIndex != 0) {
         if (this.rotatedRailConfig == null) {
            this.rotatedRailConfig = new RailConfig[RotationTuple.VALUES.length];
         }

         RailConfig rotatedRail = this.rotatedRailConfig[rotationIndex];
         if (rotatedRail == null) {
            RotationTuple rotation = RotationTuple.get(rotationIndex);
            rotatedRail = this.railConfig.clone();

            for (RailPoint p : rotatedRail.points) {
               Vector3f hyPoint = new Vector3f(p.point.x - 0.5F, p.point.y - 0.5F, p.point.z - 0.5F);
               hyPoint = Rotation.rotate(hyPoint, rotation.yaw(), rotation.pitch(), rotation.roll());
               p.point.x = hyPoint.x + 0.5F;
               p.point.y = hyPoint.y + 0.5F;
               p.point.z = hyPoint.z + 0.5F;
               Vector3f hyNormal = new Vector3f(p.normal.x, p.normal.y, p.normal.z);
               hyNormal = Rotation.rotate(hyNormal, rotation.yaw(), rotation.pitch(), rotation.roll());
               p.normal.x = hyNormal.x;
               p.normal.y = hyNormal.y;
               p.normal.z = hyNormal.z;
            }

            this.rotatedRailConfig[rotationIndex] = rotatedRail;
         }

         return rotatedRail;
      } else {
         return this.railConfig;
      }
   }

   @Deprecated
   public boolean isDoor() {
      return this.isDoor;
   }

   public boolean shouldIgnoreSupportWhenPlaced() {
      return this.ignoreSupportWhenPlaced;
   }

   public boolean canBePlacedAsDeco() {
      return this.ignoreSupportWhenPlaced || this.gathering != null && this.gathering.shouldUseDefaultDropWhenPlaced();
   }

   protected void processConfig() {
      if (this.bench != null) {
         if (this.state == null && this.bench.getType() == BenchType.Processing) {
            this.state = new StateData("processingBench");
         }

         this.flags.isUsable = true;
         if (this.interactionHint == null) {
            this.interactionHint = "server.interactionHints.open";
         }
      } else if (this.state == null || !"container".equalsIgnoreCase(this.state.getId()) && !"Door".equalsIgnoreCase(this.state.getId())) {
         if (this.gathering != null && this.gathering.isHarvestable()) {
            this.flags.isUsable = true;
            if (this.interactionHint == null) {
               this.interactionHint = "server.interactionHints.gather";
            }
         } else if (this.seats != null && this.seats.size() > 0 && this.interactionHint == null) {
            this.interactionHint = "server.interactionHints.sit";
         }
      } else {
         this.flags.isUsable = true;
         if (this.interactionHint == null) {
            this.interactionHint = "server.interactionHints.open";
         }
      }

      if (this.interactions.containsKey(InteractionType.Use)) {
         this.flags.isUsable = true;
         if (this.interactionHint == null) {
            this.interactionHint = "server.interactionHints.generic";
         }
      }

      if (this.flags.isUsable && this.interactionHint == null) {
         this.interactionHint = "server.interactionHints.generic";
      }

      if (this.ambientSoundEventId != null) {
         this.ambientSoundEventIndex = SoundEvent.getAssetMap().getIndex(this.ambientSoundEventId);
      }

      if (this.interactionSoundEventId != null) {
         this.interactionSoundEventIndex = SoundEvent.getAssetMap().getIndex(this.interactionSoundEventId);
      }

      if (this.support == null && this.material == BlockMaterial.Empty && !"@Tech".equals(this.group)) {
         this.support = REQUIRED_BOTTOM_FACE_SUPPORT;
      }

      if (this.supporting == null
         && (this.drawType == DrawType.Cube || this.drawType == DrawType.CubeWithModel || this.drawType == DrawType.GizmoCube)
         && this.material == BlockMaterial.Solid) {
         this.supporting = ALL_SUPPORTING_FACES;
      } else if (this.supporting == null) {
         this.supporting = Collections.emptyMap();
      }

      this.hitboxTypeIndex = this.hitboxType.equals("Full") ? 0 : BlockBoundingBoxes.getAssetMap().getIndex(this.hitboxType);
      if (this.hitboxTypeIndex == Integer.MIN_VALUE) {
         HytaleLogger.getLogger().at(Level.WARNING).log("Unknown hitbox '%s' for block '%s', using default", this.hitboxType, this.getId());
         this.hitboxTypeIndex = 0;
      }

      if (this.interactionHitboxType != null) {
         this.interactionHitboxTypeIndex = this.interactionHitboxType.equals("Full")
            ? 0
            : BlockBoundingBoxes.getAssetMap().getIndex(this.interactionHitboxType);
         if (this.interactionHitboxTypeIndex == Integer.MIN_VALUE) {
            HytaleLogger.getLogger()
               .at(Level.WARNING)
               .log("Unknown interaction hitbox '%s' for block '%s', using collision hitbox", this.interactionHitboxType, this.getId());
            this.interactionHitboxTypeIndex = this.hitboxTypeIndex;
         }
      }

      this.blockSoundSetIndex = this.blockSoundSetId.equals("EMPTY") ? 0 : BlockSoundSet.getAssetMap().getIndex(this.blockSoundSetId);
      if (this.blockSoundSetIndex == Integer.MIN_VALUE) {
         HytaleLogger.getLogger().at(Level.WARNING).log("Unknown block sound set '%s' for block '%s', using empty", this.blockSoundSetId, this.getId());
         this.blockSoundSetIndex = 0;
      }

      for (InteractionType type : this.interactions.keySet()) {
         if (InteractionTypeUtils.isCollisionType(type)) {
            this.isTrigger = true;
            break;
         }
      }

      if (this.bench != null && !this.interactions.containsKey(InteractionType.Use)) {
         Map<InteractionType, String> interactions = this.interactions.isEmpty() ? new EnumMap<>(InteractionType.class) : new EnumMap<>(this.interactions);
         RootInteraction rootInteraction = this.bench.getRootInteraction();
         if (rootInteraction != null) {
            interactions.put(InteractionType.Use, rootInteraction.getId());
         }

         this.interactions = Collections.unmodifiableMap(interactions);
      }
   }

   @Nonnull
   public static BlockType getUnknownFor(String blockTypeKey) {
      return UNKNOWN.clone(blockTypeKey);
   }

   public void getBlockCenter(int rotationIndex, @Nonnull Vector3d outCenter) {
      BlockBoundingBoxes hitboxAsset = BlockBoundingBoxes.getAssetMap().getAsset(this.hitboxTypeIndex);
      if (hitboxAsset == null) {
         throw new IllegalStateException("Unknown hitbox: " + this.hitboxType);
      } else {
         BlockBoundingBoxes.RotatedVariantBoxes rotatedHitbox = hitboxAsset.get(rotationIndex);
         Box boundingBox = rotatedHitbox.getBoundingBox();
         outCenter.assign(boundingBox.middleX(), boundingBox.middleY(), boundingBox.middleZ());
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "BlockType{id="
         + this.id
         + ", unknown="
         + this.unknown
         + ", group='"
         + this.group
         + "', blockSoundSetId='"
         + this.blockSoundSetId
         + "', blockSoundSetIndex="
         + this.blockSoundSetIndex
         + ", particles="
         + Arrays.toString((Object[])this.particles)
         + ", blockParticleSetId='"
         + this.blockParticleSetId
         + "', blockBreakingDecalId='"
         + this.blockBreakingDecalId
         + "', particleColor="
         + this.particleColor
         + ", effect="
         + Arrays.toString((Object[])this.effect)
         + ", textures="
         + Arrays.toString((Object[])this.textures)
         + ", textureSideMask='"
         + this.textureSideMask
         + "', cubeShadingMode="
         + this.cubeShadingMode
         + ", customModel='"
         + this.customModel
         + "', customModelTexture="
         + Arrays.toString((Object[])this.customModelTexture)
         + ", customModelScale="
         + this.customModelScale
         + ", customModelAnimation='"
         + this.customModelAnimation
         + "', drawType="
         + this.drawType
         + ", material="
         + this.material
         + ", opacity="
         + this.opacity
         + ", requiresAlphaBlending="
         + this.requiresAlphaBlending
         + ", tickProcedure"
         + this.tickProcedure
         + ", tintUp="
         + Arrays.toString((Object[])this.tintUp)
         + ", tintDown="
         + Arrays.toString((Object[])this.tintDown)
         + ", tintNorth="
         + Arrays.toString((Object[])this.tintNorth)
         + ", tintSouth="
         + Arrays.toString((Object[])this.tintSouth)
         + ", tintWest="
         + Arrays.toString((Object[])this.tintWest)
         + ", tintEast="
         + Arrays.toString((Object[])this.tintEast)
         + ", biomeTintUp="
         + this.biomeTintUp
         + ", biomeTintDown="
         + this.biomeTintDown
         + ", biomeTintNorth="
         + this.biomeTintNorth
         + ", biomeTintSouth="
         + this.biomeTintSouth
         + ", biomeTintWest="
         + this.biomeTintWest
         + ", biomeTintEast="
         + this.biomeTintEast
         + ", randomRotation="
         + this.randomRotation
         + ", variantRotation="
         + this.variantRotation
         + ", flipType="
         + this.flipType
         + ", rotationYawPlacementOffset="
         + this.rotationYawPlacementOffset
         + ", transitionTexture='"
         + this.transitionTexture
         + "', transitionToGroups="
         + Arrays.toString((Object[])this.transitionToGroups)
         + ", hitboxType='"
         + this.hitboxType
         + "', hitboxTypeIndex="
         + this.hitboxTypeIndex
         + ", interactionHitboxType='"
         + this.interactionHitboxType
         + "', interactionHitboxTypeIndex="
         + this.interactionHitboxTypeIndex
         + ", light="
         + this.light
         + ", movementSettings="
         + this.movementSettings
         + ", flags="
         + this.flags
         + ", interactionHint='"
         + this.interactionHint
         + "', isTrigger="
         + this.isTrigger
         + ", damageToEntities="
         + this.damageToEntities
         + ", allowsMultipleUsers="
         + this.allowsMultipleUsers
         + ", bench="
         + this.bench
         + ", gathering="
         + this.gathering
         + ", placementSettings="
         + this.placementSettings
         + ", state="
         + this.state
         + ", ambientSoundEventId='"
         + this.ambientSoundEventId
         + "', ambientSoundEventIndex='"
         + this.ambientSoundEventIndex
         + "', interactionSoundEventId='"
         + this.interactionSoundEventId
         + "', interactionSoundEventIndex='"
         + this.interactionSoundEventIndex
         + "', isLooping="
         + this.isLooping
         + ", farming="
         + this.farming
         + ", supportDropType="
         + this.supportDropType
         + ", maxSupportDistance="
         + this.maxSupportDistance
         + ", support="
         + this.support
         + ", supporting="
         + this.supporting
         + ", interactions="
         + this.interactions
         + ", railConfig="
         + this.railConfig
         + "}";
   }

   @Nonnull
   public BlockType clone(String newKey) {
      if (this.id != null && this.id.equals(newKey)) {
         return this;
      } else {
         BlockType blockType = new BlockType(this);
         blockType.id = newKey;
         blockType.cachedPacket = null;
         return blockType;
      }
   }

   public static int getBlockIdOrUnknown(@Nonnull String blockTypeKey, String message, Object... params) {
      return getBlockIdOrUnknown(getAssetMap(), blockTypeKey, message, params);
   }

   public static int getBlockIdOrUnknown(@Nonnull BlockTypeAssetMap<String, BlockType> assetMap, @Nonnull String blockTypeKey, String message, Object... params) {
      int blockId = assetMap.getIndex(blockTypeKey);
      if (blockId == Integer.MIN_VALUE) {
         HytaleLogger.getLogger().at(Level.WARNING).logVarargs(message, params);
         AssetRegistry.getAssetStore(BlockType.class)
            .loadAssets("Hytale:Hytale", Collections.singletonList(getUnknownFor(blockTypeKey)), AssetUpdateQuery.DEFAULT_NO_REBUILD);
         int index = assetMap.getIndex(blockTypeKey);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + blockTypeKey);
         }

         blockId = index;
      }

      return blockId;
   }

   static {
      StateData.addDefinitions();
   }
}
