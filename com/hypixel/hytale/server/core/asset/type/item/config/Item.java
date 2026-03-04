package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.metadata.AllowEmptyObject;
import com.hypixel.hytale.codec.schema.metadata.ui.UIButton;
import com.hypixel.hytale.codec.schema.metadata.ui.UICreateButtons;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorPreview;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart;
import com.hypixel.hytale.codec.schema.metadata.ui.UIPropertyTitle;
import com.hypixel.hytale.codec.schema.metadata.ui.UIRebuildCaches;
import com.hypixel.hytale.codec.schema.metadata.ui.UISidebarButtons;
import com.hypixel.hytale.codec.schema.metadata.ui.UITypeIcon;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.MapUtil;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ItemBase;
import com.hypixel.hytale.protocol.ItemResourceType;
import com.hypixel.hytale.protocol.ModelTrail;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderToolData;
import com.hypixel.hytale.server.core.asset.type.itemanimation.config.ItemPlayerAnimations;
import com.hypixel.hytale.server.core.asset.type.itemsound.config.ItemSoundSet;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.UnarmedInteractions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionConfiguration;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ChangeActiveSlotInteraction;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Item implements JsonAssetWithMap<String, DefaultAssetMap<String, Item>>, NetworkSerializable<ItemBase> {
   private static final AssetBuilderCodec.Builder<String, Item> CODEC_BUILDER = AssetBuilderCodec.builder(
         Item.class,
         Item::new,
         Codec.STRING,
         (item, blockTypeKey) -> item.id = blockTypeKey,
         item -> item.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .metadata(new UIEditorPreview(UIEditorPreview.PreviewType.ITEM))
      .metadata(new UITypeIcon("Item.png"))
      .metadata(
         new UIRebuildCaches(
            false,
            UIRebuildCaches.ClientCache.MODELS,
            UIRebuildCaches.ClientCache.BLOCK_TEXTURES,
            UIRebuildCaches.ClientCache.MODEL_TEXTURES,
            UIRebuildCaches.ClientCache.MAP_GEOMETRY,
            UIRebuildCaches.ClientCache.ITEM_ICONS
         )
      )
      .metadata(new UISidebarButtons(new UIButton("server.assetEditor.buttons.equipItem", "EquipItem")))
      .metadata(new UICreateButtons(new UIButton("server.assetEditor.buttons.createAndEquipItem", "EquipItem")))
      .<String>appendInherited(new KeyedCodec<>("Icon", Codec.STRING), (item, s) -> item.icon = s, item -> item.icon, (item, parent) -> item.icon = parent.icon)
      .addValidator(CommonAssetValidator.ICON_ITEM)
      .metadata(new UIEditor(new UIEditor.Icon("Icons/ItemsGenerated/{assetId}.png", 64, 64)))
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.ITEM_ICONS))
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("Categories", new ArrayCodec<>(Codec.STRING, String[]::new).metadata(new UIEditor(new UIEditor.Dropdown("ItemCategories")))),
         (item, s) -> item.categories = s,
         item -> item.categories,
         (item, parent) -> item.categories = parent.categories
      )
      .addValidatorLate(() -> ItemCategory.VALIDATOR_CACHE.getArrayValidator().late())
      .documentation("A list of categories this item will be shown in on the creative library menu.")
      .add()
      .<AssetIconProperties>appendInherited(
         new KeyedCodec<>("IconProperties", AssetIconProperties.CODEC),
         (item, s) -> item.iconProperties = s,
         item -> item.iconProperties,
         (item, parent) -> item.iconProperties = parent.iconProperties
      )
      .metadata(UIDisplayMode.HIDDEN)
      .add()
      .<ItemTranslationProperties>appendInherited(
         new KeyedCodec<>("TranslationProperties", ItemTranslationProperties.CODEC),
         (item, s) -> item.translationProperties = s,
         item -> item.translationProperties,
         (item, parent) -> item.translationProperties = parent.translationProperties
      )
      .documentation("The translation properties for this item asset.")
      .add()
      .appendInherited(
         new KeyedCodec<>("ItemLevel", Codec.INTEGER),
         (item, s) -> item.itemLevel = s,
         item -> item.itemLevel,
         (item, parent) -> item.itemLevel = parent.itemLevel
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("Reticle", new ContainedAssetCodec<>(ItemReticleConfig.class, ItemReticleConfig.CODEC)),
         (item, s) -> item.reticleId = s,
         item -> item.reticleId,
         (item, parent) -> item.reticleId = parent.reticleId
      )
      .addValidator(ItemReticleConfig.VALIDATOR_CACHE.getValidator())
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("MaxStack", Codec.INTEGER), (item, s) -> item.maxStack = s, item -> item.maxStack, (item, parent) -> item.maxStack = parent.maxStack
      )
      .metadata(new UIPropertyTitle("Max Stack Size"))
      .documentation("The maximum amount this item can be stacked in the inventory")
      .addValidator(Validators.greaterThan(0))
      .add()
      .<String>append(new KeyedCodec<>("Quality", Codec.STRING), (item, s) -> item.qualityId = s, item -> item.qualityId)
      .addValidator(ItemQuality.VALIDATOR_CACHE.getValidator())
      .add()
      .<ItemEntityConfig>appendInherited(
         new KeyedCodec<>("ItemEntity", ItemEntityConfig.CODEC),
         (item, itemEntityConfig) -> item.itemEntityConfig = itemEntityConfig,
         item -> item.itemEntityConfig,
         (item, parent) -> item.itemEntityConfig = parent.itemEntityConfig
      )
      .addValidator(Validators.nonNull())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("SoundEventId", Codec.STRING),
         (item, s) -> item.soundEventId = s,
         item -> item.soundEventId,
         (item, parent) -> item.soundEventId = parent.soundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("ItemSoundSetId", Codec.STRING),
         (item, s) -> item.itemSoundSetId = s,
         item -> item.itemSoundSetId,
         (item, parent) -> item.itemSoundSetId = parent.itemSoundSetId
      )
      .addValidator(Validators.nonNull())
      .addValidator(ItemSoundSet.VALIDATOR_CACHE.getValidator())
      .add()
      .appendInherited(new KeyedCodec<>("Set", Codec.STRING), (item, s) -> item.set = s, item -> item.set, (item, parent) -> item.set = parent.set)
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("Model", Codec.STRING), (item, s) -> item.model = s, item -> item.model, (item, parent) -> item.model = parent.model
      )
      .addValidator(CommonAssetValidator.MODEL_ITEM)
      .metadata(new UIEditorSectionStart("Rendering"))
      .metadata(new UIRebuildCaches(false, UIRebuildCaches.ClientCache.MODELS))
      .metadata(new UIPropertyTitle("Item Model"))
      .documentation("The model used for rendering this item. If this is a block, BlockType.Model should be used instead.")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("Scale", Codec.DOUBLE),
         (item, s) -> item.scale = s.floatValue(),
         item -> (double)item.scale,
         (item, parent) -> item.scale = parent.scale
      )
      .metadata(new UIPropertyTitle("Item Scale"))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("Texture", Codec.STRING), (item, s) -> item.texture = s, item -> item.texture, (item, parent) -> item.texture = parent.texture
      )
      .addValidator(CommonAssetValidator.TEXTURE_ITEM)
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .metadata(new UIPropertyTitle("Item Texture"))
      .documentation("The texture used for rendering this item. If this is a block, block specific properties should be used instead.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("Animation", Codec.STRING),
         (item, s) -> item.animation = s,
         item -> item.animation,
         (item, parent) -> item.animation = parent.animation
      )
      .addValidator(CommonAssetValidator.ANIMATION_ITEM_BLOCK)
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .metadata(new UIPropertyTitle("Item Animation"))
      .documentation("The animation used for rendering this item. If this is a block, block specific properties should be used instead.")
      .add()
      .appendInherited(
         new KeyedCodec<>("UsePlayerAnimations", Codec.BOOLEAN),
         (item, s) -> item.usePlayerAnimations = s,
         item -> item.usePlayerAnimations,
         (item, parent) -> item.usePlayerAnimations = parent.usePlayerAnimations
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("PlayerAnimationsId", ItemPlayerAnimations.CHILD_CODEC),
         (item, s) -> item.playerAnimationsId = s,
         item -> item.playerAnimationsId,
         (item, parent) -> item.playerAnimationsId = parent.playerAnimationsId
      )
      .addValidator(Validators.nonNull())
      .addValidator(ItemPlayerAnimations.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("DroppedItemAnimation", Codec.STRING),
         (item, animation) -> item.droppedItemAnimation = animation,
         item -> item.droppedItemAnimation,
         (item, parent) -> item.droppedItemAnimation = parent.droppedItemAnimation
      )
      .addValidator(CommonAssetValidator.ANIMATION_ITEM_BLOCK)
      .add()
      .<ModelParticle[]>appendInherited(
         new KeyedCodec<>("Particles", ModelParticle.ARRAY_CODEC),
         (item, s) -> item.particles = s,
         item -> item.particles,
         (item, parent) -> item.particles = parent.particles
      )
      .metadata(new UIPropertyTitle("Item Particles"))
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .documentation("The particles played for this item. If this is a block, block specific properties should be used instead.")
      .add()
      .<ModelParticle[]>appendInherited(
         new KeyedCodec<>("FirstPersonParticles", ModelParticle.ARRAY_CODEC),
         (item, s) -> item.firstPersonParticles = s,
         item -> item.firstPersonParticles,
         (item, parent) -> item.firstPersonParticles = parent.firstPersonParticles
      )
      .metadata(new UIPropertyTitle("Item First Person Particles"))
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .documentation("The particles played for this item when in first person. If this is a block, block specific properties should be used instead.")
      .add()
      .<ModelTrail[]>appendInherited(
         new KeyedCodec<>("Trails", ModelAsset.MODEL_TRAIL_ARRAY_CODEC),
         (item, s) -> item.trails = s,
         item -> item.trails,
         (item, parent) -> item.trails = parent.trails
      )
      .metadata(new UIPropertyTitle("Item Trails"))
      .documentation("The trail attached to this item")
      .add()
      .<ColorLight>appendInherited(
         new KeyedCodec<>("Light", ProtocolCodecs.COLOR_LIGHT), (item, o) -> item.light = o, item -> item.light, (item, parent) -> item.light = parent.light
      )
      .metadata(new UIPropertyTitle("Item Light"))
      .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.MODELS))
      .documentation("The light this item is emitting when being held or dropped. For block light, see Block properties")
      .add()
      .<CraftingRecipe>append(new KeyedCodec<>("Recipe", CraftingRecipe.CODEC), (item, s) -> item.recipeToGenerate = s, item -> item.recipeToGenerate)
      .metadata(new UIEditorSectionStart("Crafting"))
      .add()
      .appendInherited(
         new KeyedCodec<>(
            "ResourceTypes",
            new ArrayCodec<>(
               BuilderCodec.builder(ItemResourceType.class, ItemResourceType::new)
                  .append(new KeyedCodec<>("Id", Codec.STRING), (itemResourceType, s) -> itemResourceType.id = s, itemResourceType -> itemResourceType.id)
                  .addValidator(ResourceType.VALIDATOR_CACHE.getValidator())
                  .addValidator(Validators.nonNull())
                  .add()
                  .<Integer>append(
                     new KeyedCodec<>("Quantity", Codec.INTEGER),
                     (itemResourceType, s) -> itemResourceType.quantity = s,
                     itemResourceType -> itemResourceType.quantity
                  )
                  .addValidator(Validators.greaterThan(0))
                  .add()
                  .build(),
               ItemResourceType[]::new
            )
         ),
         (item, s) -> item.resourceTypes = s,
         item -> item.resourceTypes,
         (item, parent) -> item.resourceTypes = parent.resourceTypes
      )
      .add()
      .<ItemTool>appendInherited(
         new KeyedCodec<>("Tool", ItemTool.CODEC), (item, s) -> item.tool = s, item -> item.tool, (item, parent) -> item.tool = parent.tool
      )
      .metadata(new UIEditorSectionStart("Functionality"))
      .add()
      .appendInherited(
         new KeyedCodec<>("BlockSelectorTool", BlockSelectorToolData.CODEC),
         (item, s) -> item.blockSelectorToolData = s,
         item -> item.blockSelectorToolData,
         (item, parent) -> item.blockSelectorToolData = parent.blockSelectorToolData
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("BuilderTool", BuilderToolData.CODEC),
         (item, s) -> item.builderToolData = s,
         item -> item.builderToolData,
         (item, parent) -> item.builderToolData = parent.builderToolData
      )
      .add()
      .<ItemWeapon>appendInherited(
         new KeyedCodec<>("Weapon", ItemWeapon.CODEC), (item, s) -> item.weapon = s, item -> item.weapon, (item, parent) -> item.weapon = parent.weapon
      )
      .metadata(AllowEmptyObject.INSTANCE)
      .add()
      .appendInherited(new KeyedCodec<>("Armor", ItemArmor.CODEC), (item, s) -> item.armor = s, item -> item.armor, (item, parent) -> item.armor = parent.armor)
      .add()
      .appendInherited(
         new KeyedCodec<>("Glider", ItemGlider.CODEC), (item, s) -> item.glider = s, item -> item.glider, (item, parent) -> item.glider = parent.glider
      )
      .add()
      .<ItemUtility>appendInherited(
         new KeyedCodec<>("Utility", ItemUtility.CODEC), (item, s) -> item.utility = s, item -> item.utility, (item, parent) -> item.utility = parent.utility
      )
      .addValidator(Validators.nonNull())
      .add()
      .appendInherited(
         new KeyedCodec<>("PortalKey", PortalKey.CODEC),
         (item, s) -> item.portalKey = s,
         item -> item.portalKey,
         (item, parent) -> item.portalKey = parent.portalKey
      )
      .add()
      .<ItemStackContainerConfig>appendInherited(
         new KeyedCodec<>("Container", ItemStackContainerConfig.CODEC),
         (item, s) -> item.itemStackContainerConfig = s,
         item -> item.itemStackContainerConfig,
         (item, parent) -> item.itemStackContainerConfig = parent.itemStackContainerConfig
      )
      .addValidator(Validators.nonNull())
      .add()
      .appendInherited(
         new KeyedCodec<>("Consumable", Codec.BOOLEAN),
         (item, s) -> item.consumable = s,
         item -> item.consumable,
         (item, parent) -> item.consumable = parent.consumable
      )
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("Variant", Codec.BOOLEAN), (item, b) -> item.variant = b, item -> item.variant, (item, parent) -> item.variant = parent.variant
      )
      .documentation(
         "Whether this item is a variant of another. Typically this is only the case for connected blocks. If this item is marked as a variant, then we filter it out of the item library menu by default, unless the player chooses to display variants."
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("MaxDurability", Codec.DOUBLE),
         (item, s) -> item.maxDurability = s,
         item -> item.maxDurability,
         (item, parent) -> item.maxDurability = parent.maxDurability
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("FuelQuality", Codec.DOUBLE),
         (item, s) -> item.fuelQuality = s,
         item -> item.fuelQuality,
         (item, parent) -> item.fuelQuality = parent.fuelQuality
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("DurabilityLossOnHit", Codec.DOUBLE),
         (item, s) -> item.durabilityLossOnHit = s,
         item -> item.durabilityLossOnHit,
         (item, parent) -> item.durabilityLossOnHit = parent.durabilityLossOnHit
      )
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DurabilityLossOnDeath", Codec.BOOLEAN),
         (item, s) -> item.durabilityLossOnDeath = s,
         item -> item.durabilityLossOnDeath,
         (item, parent) -> item.durabilityLossOnDeath = parent.durabilityLossOnDeath
      )
      .documentation("Whether this item should loose durability on death, if so configured in DeathConfig.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("BlockType", new ContainedAssetCodec<>(BlockType.class, BlockType.CODEC, ContainedAssetCodec.Mode.INHERIT_ID_AND_PARENT)),
         (item, s) -> item.hasBlockType = true,
         item -> item.blockId,
         (item, parent) -> item.blockId = parent.blockId
      )
      .metadata(new UIEditorSectionStart("Block"))
      .metadata(
         new UIRebuildCaches(false, UIRebuildCaches.ClientCache.MODELS, UIRebuildCaches.ClientCache.BLOCK_TEXTURES, UIRebuildCaches.ClientCache.MODEL_TEXTURES)
      )
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
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
      .<InteractionConfiguration>appendInherited(
         new KeyedCodec<>("InteractionConfig", InteractionConfiguration.CODEC),
         (item, v) -> item.interactionConfig = v,
         item -> item.interactionConfig,
         (item, parent) -> item.interactionConfig = parent.interactionConfig
      )
      .addValidator(Validators.nonNull())
      .add()
      .<Map>appendInherited(
         new KeyedCodec<>("InteractionVars", new MapCodec<>(RootInteraction.CHILD_ASSET_CODEC, HashMap::new)),
         (item, v) -> item.interactionVars = MapUtil.combineUnmodifiable(item.interactionVars, v),
         item -> item.interactionVars,
         (item, parent) -> item.interactionVars = parent.interactionVars
      )
      .addValidator(RootInteraction.VALIDATOR_CACHE.getMapValueValidator())
      .add()
      .<Map>appendInherited(
         new KeyedCodec<>(
            "ItemAppearanceConditions", new MapCodec<>(new ArrayCodec<>(ItemAppearanceCondition.CODEC, ItemAppearanceCondition[]::new), HashMap::new)
         ),
         (item, stringMap) -> item.itemAppearanceConditions = stringMap,
         item -> item.itemAppearanceConditions,
         (item, parent) -> item.itemAppearanceConditions = parent.itemAppearanceConditions
      )
      .documentation("Define per EntityStat an array of ItemAppearanceCondition. Only a single condition will be applied to the item at the same time.")
      .addValidator(EntityStatType.VALIDATOR_CACHE.getMapKeyValidator().late())
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("DisplayEntityStatsHUD", Codec.STRING_ARRAY),
         (item, strings) -> item.rawDisplayEntityStatsHUD = strings,
         item -> item.rawDisplayEntityStatsHUD,
         (item, parent) -> item.rawDisplayEntityStatsHUD = parent.rawDisplayEntityStatsHUD
      )
      .documentation("Used to indicate to the client whether an EntityStat HUD UI needs to be displayed")
      .add()
      .<ItemPullbackConfig>appendInherited(
         new KeyedCodec<>("PullbackConfig", ItemPullbackConfig.CODEC),
         (item, s) -> item.pullbackConfig = s,
         item -> item.pullbackConfig,
         (item, parent) -> item.pullbackConfig = parent.pullbackConfig
      )
      .documentation("Overrides the offset of first person arms when close to obstacles")
      .add()
      .appendInherited(
         new KeyedCodec<>("ClipsGeometry", Codec.BOOLEAN),
         (item, s) -> item.clipsGeometry = s,
         item -> item.clipsGeometry,
         (item, parent) -> item.clipsGeometry = parent.clipsGeometry
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("RenderDeployablePreview", Codec.BOOLEAN),
         (item, s) -> item.renderDeployablePreview = s,
         item -> item.renderDeployablePreview,
         (item, parent) -> item.renderDeployablePreview = parent.renderDeployablePreview
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("DropOnDeath", Codec.BOOLEAN),
         (item, aBoolean) -> item.dropOnDeath = aBoolean,
         item -> item.dropOnDeath,
         (item, parent) -> item.dropOnDeath = parent.dropOnDeath
      )
      .add()
      .afterDecode(Item::processConfig);
   public static final AssetCodec<String, Item> CODEC = CODEC_BUILDER.build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(Item::getAssetStore));
   private static AssetStore<String, Item, DefaultAssetMap<String, Item>> ASSET_STORE;
   public static final String UNKNOWN_TEXTURE = "Items/Unknown.png";
   public static final Item UNKNOWN = new Item("Unknown") {
      {
         this.playerAnimationsId = "Item";
         this.model = "Items/CreativeTools/EditorTool.blockymodel";
         this.texture = "Items/Unknown.png";
         this.maxStack = 100;
         this.itemEntityConfig = ItemEntityConfig.DEFAULT;
         this.interactionConfig = InteractionConfiguration.DEFAULT;
         this.interactions = Map.of(InteractionType.SwapFrom, ChangeActiveSlotInteraction.DEFAULT_ROOT.getId());
      }
   };
   protected AssetExtraInfo.Data data;
   protected String id;
   protected String icon;
   protected AssetIconProperties iconProperties;
   protected ItemTranslationProperties translationProperties;
   protected String reticleId;
   protected int reticleIndex = 0;
   protected int itemLevel;
   protected int maxStack = -1;
   protected String qualityId;
   protected int qualityIndex = 0;
   protected CraftingRecipe recipeToGenerate;
   protected String blockId;
   protected boolean hasBlockType;
   protected boolean consumable;
   protected boolean variant;
   protected ItemTool tool;
   protected BlockSelectorToolData blockSelectorToolData;
   protected BuilderToolData builderToolData;
   protected ItemWeapon weapon;
   protected ItemArmor armor;
   protected ItemGlider glider;
   protected ItemUtility utility = ItemUtility.DEFAULT;
   protected ItemStackContainerConfig itemStackContainerConfig = ItemStackContainerConfig.DEFAULT;
   protected PortalKey portalKey;
   protected String playerAnimationsId = "Default";
   protected boolean usePlayerAnimations = false;
   protected String model;
   protected float scale = 1.0F;
   protected String texture = "Items/Unknown.png";
   protected String animation;
   protected String[] categories;
   protected String set;
   protected String soundEventId;
   protected transient int soundEventIndex;
   protected String itemSoundSetId = "ISS_Default";
   protected transient int itemSoundSetIndex;
   protected ModelParticle[] particles;
   protected ModelParticle[] firstPersonParticles;
   protected ModelTrail[] trails;
   protected ColorLight light;
   protected ItemResourceType[] resourceTypes;
   protected Map<String, String> stateToBlock;
   protected Map<String, String> blockToState;
   protected Map<InteractionType, String> interactions = Collections.emptyMap();
   protected Map<String, String> interactionVars = Collections.emptyMap();
   protected InteractionConfiguration interactionConfig;
   protected ItemEntityConfig itemEntityConfig;
   protected String droppedItemAnimation;
   protected double maxDurability;
   protected double fuelQuality = 1.0;
   protected double durabilityLossOnHit;
   protected Map<String, ItemAppearanceCondition[]> itemAppearanceConditions;
   protected String[] rawDisplayEntityStatsHUD;
   @Nullable
   protected int[] displayEntityStatsHUD;
   protected ItemPullbackConfig pullbackConfig;
   protected boolean clipsGeometry;
   protected boolean renderDeployablePreview;
   protected boolean dropOnDeath;
   protected boolean durabilityLossOnDeath = true;
   private transient SoftReference<ItemBase> cachedPacket;

   public static AssetStore<String, Item, DefaultAssetMap<String, Item>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(Item.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, Item> getAssetMap() {
      return (DefaultAssetMap<String, Item>)getAssetStore().getAssetMap();
   }

   protected Item() {
   }

   public Item(String id) {
      this.id = id;
   }

   public Item(@Nonnull Item other) {
      this.data = other.data;
      this.id = other.id;
      this.icon = other.icon;
      this.iconProperties = other.iconProperties;
      this.translationProperties = other.translationProperties;
      this.reticleId = other.reticleId;
      this.itemLevel = other.itemLevel;
      this.maxStack = other.maxStack;
      this.qualityId = other.qualityId;
      this.recipeToGenerate = other.recipeToGenerate;
      this.consumable = other.consumable;
      this.variant = other.variant;
      this.playerAnimationsId = other.playerAnimationsId;
      this.usePlayerAnimations = other.usePlayerAnimations;
      this.model = other.model;
      this.scale = other.scale;
      this.texture = other.texture;
      this.animation = other.animation;
      this.tool = other.tool;
      this.blockSelectorToolData = other.blockSelectorToolData;
      this.builderToolData = other.builderToolData;
      this.weapon = other.weapon;
      this.armor = other.armor;
      this.utility = other.utility;
      this.portalKey = other.portalKey;
      this.categories = other.categories;
      this.set = other.set;
      this.soundEventId = other.soundEventId;
      this.soundEventIndex = other.soundEventIndex;
      this.itemSoundSetId = other.itemSoundSetId;
      this.itemSoundSetIndex = other.itemSoundSetIndex;
      this.particles = other.particles;
      this.firstPersonParticles = other.firstPersonParticles;
      this.trails = other.trails;
      this.light = other.light;
      this.resourceTypes = other.resourceTypes;
      this.interactions = other.interactions;
      this.interactionVars = other.interactionVars;
      this.interactionConfig = other.interactionConfig;
      this.droppedItemAnimation = other.droppedItemAnimation;
      this.itemEntityConfig = other.itemEntityConfig;
      this.stateToBlock = other.stateToBlock;
      this.blockId = other.blockId;
      this.hasBlockType = other.hasBlockType;
      this.displayEntityStatsHUD = other.displayEntityStatsHUD;
      this.pullbackConfig = other.pullbackConfig;
      this.clipsGeometry = other.clipsGeometry;
      this.renderDeployablePreview = other.renderDeployablePreview;
      this.dropOnDeath = other.dropOnDeath;
   }

   @Nonnull
   public ItemBase toPacket() {
      ItemBase cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         ItemBase packet = new ItemBase();
         packet.id = this.id;
         if (this.icon != null) {
            packet.icon = this.icon;
         }

         if (this.iconProperties != null) {
            packet.iconProperties = this.iconProperties.toPacket();
         }

         if (this.translationProperties != null) {
            packet.translationProperties = this.translationProperties.toPacket();
         }

         if (this.model != null) {
            packet.model = this.model;
         }

         packet.scale = this.scale;
         if (this.texture != null) {
            packet.texture = this.texture;
         }

         if (this.animation != null) {
            packet.animation = this.animation;
         }

         packet.playerAnimationsId = this.playerAnimationsId;
         packet.usePlayerAnimations = this.usePlayerAnimations;
         packet.reticleIndex = this.reticleIndex;
         packet.maxStack = this.maxStack;
         packet.itemLevel = this.itemLevel;
         packet.qualityIndex = this.qualityIndex;
         if (this.blockId != null) {
            packet.blockId = BlockType.getAssetMap().getIndexOrDefault(this.blockId, 1);
            if (packet.blockId == 0) {
               throw new IllegalArgumentException("Block Id Can't be 0");
            }
         }

         packet.consumable = this.consumable;
         packet.variant = this.variant;
         if (this.tool != null) {
            packet.tool = this.tool.toPacket();
         }

         if (this.blockSelectorToolData != null) {
            packet.blockSelectorTool = this.blockSelectorToolData.toPacket();
         }

         if (this.builderToolData != null) {
            packet.builderToolData = this.builderToolData.toPacket();
         }

         if (this.weapon != null) {
            packet.weapon = this.weapon.toPacket();
         }

         if (this.armor != null) {
            packet.armor = this.armor.toPacket();
         }

         if (this.glider != null) {
            packet.gliderConfig = this.glider.toPacket();
         }

         if (this.utility != null) {
            packet.utility = this.utility.toPacket();
         }

         if (this.categories != null && this.categories.length > 0) {
            packet.categories = this.categories;
         }

         if (this.set != null) {
            packet.set = this.set;
         }

         packet.soundEventIndex = this.soundEventIndex;
         packet.itemSoundSetIndex = this.itemSoundSetIndex;
         if (this.particles != null && this.particles.length > 0) {
            packet.particles = new com.hypixel.hytale.protocol.ModelParticle[this.particles.length];

            for (int i = 0; i < this.particles.length; i++) {
               packet.particles[i] = this.particles[i].toPacket();
            }
         }

         if (this.firstPersonParticles != null && this.firstPersonParticles.length > 0) {
            packet.firstPersonParticles = new com.hypixel.hytale.protocol.ModelParticle[this.firstPersonParticles.length];

            for (int i = 0; i < this.firstPersonParticles.length; i++) {
               packet.firstPersonParticles[i] = this.firstPersonParticles[i].toPacket();
            }
         }

         if (this.trails != null && this.trails.length > 0) {
            packet.trails = this.trails;
         }

         if (this.light != null) {
            packet.light = this.light;
         }

         if (this.resourceTypes != null && this.resourceTypes.length > 0) {
            packet.resourceTypes = this.resourceTypes;
         }

         Object2IntOpenHashMap<InteractionType> interactionsIntMap = new Object2IntOpenHashMap();

         for (Entry<InteractionType, String> e : this.interactions.entrySet()) {
            interactionsIntMap.put(e.getKey(), RootInteraction.getRootInteractionIdOrUnknown(e.getValue()));
         }

         packet.interactions = interactionsIntMap;
         Object2IntOpenHashMap<String> interactionVarsIntMap = new Object2IntOpenHashMap();

         for (Entry<String, String> e : this.interactionVars.entrySet()) {
            interactionVarsIntMap.put(e.getKey(), RootInteraction.getRootInteractionIdOrUnknown(e.getValue()));
         }

         packet.interactionVars = interactionVarsIntMap;
         packet.interactionConfig = this.interactionConfig.toPacket();
         packet.durability = this.getMaxDurability();
         packet.itemEntity = this.itemEntityConfig.toPacket();
         if (this.droppedItemAnimation != null) {
            packet.droppedItemAnimation = this.droppedItemAnimation;
         }

         if (this.itemAppearanceConditions != null) {
            HashMap<Integer, com.hypixel.hytale.protocol.ItemAppearanceCondition[]> map = new HashMap<>();

            for (Entry<String, ItemAppearanceCondition[]> entry : this.itemAppearanceConditions.entrySet()) {
               ItemAppearanceCondition[] conditions = entry.getValue();
               com.hypixel.hytale.protocol.ItemAppearanceCondition[] protocolConditions = new com.hypixel.hytale.protocol.ItemAppearanceCondition[conditions.length];

               for (int i = 0; i < conditions.length; i++) {
                  protocolConditions[i] = conditions[i].toPacket();
               }

               map.put(EntityStatType.getAssetMap().getIndex(entry.getKey()), protocolConditions);
            }

            packet.itemAppearanceConditions = map;
         }

         if (this.data != null) {
            IntSet expandedTagIndexes = this.data.getExpandedTagIndexes();
            if (expandedTagIndexes != null) {
               packet.tagIndexes = expandedTagIndexes.toIntArray();
            }
         }

         packet.displayEntityStatsHUD = this.displayEntityStatsHUD;
         if (this.pullbackConfig != null) {
            packet.pullbackConfig = this.pullbackConfig.toPacket();
         }

         packet.clipsGeometry = this.clipsGeometry;
         packet.renderDeployablePreview = this.renderDeployablePreview;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   @Nullable
   public String getItemIdForState(String state) {
      return this.stateToBlock != null ? this.stateToBlock.get(state) : null;
   }

   @Nullable
   public Item getItemForState(String state) {
      String id = this.getItemIdForState(state);
      return id == null ? null : getAssetMap().getAsset(id);
   }

   public boolean isState() {
      return this.getStateForItem(this.id) != null;
   }

   @Nullable
   public String getStateForItem(@Nonnull Item item) {
      return this.getStateForItem(item.getId());
   }

   @Nullable
   public String getStateForItem(String key) {
      return this.blockToState != null ? this.blockToState.get(key) : null;
   }

   public AssetExtraInfo.Data getData() {
      return this.data;
   }

   public String getId() {
      return this.id;
   }

   public String getBlockId() {
      return this.blockId;
   }

   @Nonnull
   public String getTranslationKey() {
      if (this.translationProperties != null) {
         String nameTranslation = this.translationProperties.getName();
         if (nameTranslation != null) {
            return nameTranslation;
         }
      }

      return "server.items." + this.id + ".name";
   }

   @Nonnull
   public String getDescriptionTranslationKey() {
      if (this.translationProperties != null) {
         String descriptionTranslation = this.translationProperties.getDescription();
         if (descriptionTranslation != null) {
            return descriptionTranslation;
         }
      }

      return "server.items." + this.id + ".description";
   }

   public String getModel() {
      return this.model;
   }

   public String getTexture() {
      return this.texture;
   }

   public boolean isConsumable() {
      return this.consumable;
   }

   public boolean isVariant() {
      return this.variant;
   }

   public boolean getUsePlayerAnimations() {
      return this.usePlayerAnimations;
   }

   public String getPlayerAnimationsId() {
      return this.playerAnimationsId;
   }

   public String getIcon() {
      return this.icon;
   }

   public AssetIconProperties getIconProperties() {
      return this.iconProperties;
   }

   public ItemTranslationProperties getTranslationProperties() {
      return this.translationProperties;
   }

   public float getScale() {
      return this.scale;
   }

   public String getReticleId() {
      return this.reticleId;
   }

   public int getItemLevel() {
      return this.itemLevel;
   }

   public int getMaxStack() {
      return this.maxStack;
   }

   public int getQualityIndex() {
      return this.qualityIndex;
   }

   public ItemTool getTool() {
      return this.tool;
   }

   public BlockSelectorToolData getBlockSelectorToolData() {
      return this.blockSelectorToolData;
   }

   public BuilderToolData getBuilderToolData() {
      return this.builderToolData;
   }

   public ItemArmor getArmor() {
      return this.armor;
   }

   public ItemGlider getGlider() {
      return this.glider;
   }

   @Nonnull
   public ItemUtility getUtility() {
      return this.utility;
   }

   @Nullable
   public PortalKey getPortalKey() {
      return this.portalKey;
   }

   @Nonnull
   public ItemStackContainerConfig getItemStackContainerConfig() {
      return this.itemStackContainerConfig;
   }

   public String[] getCategories() {
      return this.categories;
   }

   public String getSoundEventId() {
      return this.soundEventId;
   }

   public int getSoundEventIndex() {
      return this.soundEventIndex;
   }

   public boolean hasBlockType() {
      return this.blockId != null;
   }

   public ItemWeapon getWeapon() {
      return this.weapon;
   }

   public ItemResourceType[] getResourceTypes() {
      return this.resourceTypes;
   }

   public double getMaxDurability() {
      return this.maxDurability;
   }

   public ColorLight getLight() {
      return this.light;
   }

   public Map<InteractionType, String> getInteractions() {
      return this.interactions;
   }

   public Map<String, String> getInteractionVars() {
      return this.interactionVars;
   }

   public ItemEntityConfig getItemEntityConfig() {
      return this.itemEntityConfig;
   }

   public String getDroppedItemAnimation() {
      return this.droppedItemAnimation;
   }

   public double getDurabilityLossOnHit() {
      return this.durabilityLossOnHit;
   }

   public boolean getDurabilityLossOnDeath() {
      return this.durabilityLossOnDeath;
   }

   public int[] getDisplayEntityStatsHUD() {
      return this.displayEntityStatsHUD;
   }

   public ItemPullbackConfig getPullbackConfig() {
      return this.pullbackConfig;
   }

   public boolean getClipsGeometry() {
      return this.clipsGeometry;
   }

   public boolean getRenderDeployablePreview() {
      return this.renderDeployablePreview;
   }

   public double getFuelQuality() {
      return this.fuelQuality;
   }

   public InteractionConfiguration getInteractionConfig() {
      return this.interactionConfig;
   }

   public int getItemSoundSetIndex() {
      return this.itemSoundSetIndex;
   }

   public void collectRecipesToGenerate(Collection<CraftingRecipe> recipes) {
      if (this.recipeToGenerate != null) {
         recipes.add(this.recipeToGenerate);
      }
   }

   public boolean hasRecipesToGenerate() {
      return this.recipeToGenerate != null;
   }

   public boolean dropsOnDeath() {
      return this.dropOnDeath;
   }

   protected void processConfig() {
      if (this.hasBlockType) {
         this.blockId = this.id;
      }

      if (this.maxStack == -1) {
         if (this.tool == null && this.weapon == null && this.armor == null && this.builderToolData == null && this.blockSelectorToolData == null) {
            this.maxStack = 100;
         } else {
            this.maxStack = 1;
         }
      }

      Map<InteractionType, String> interactions = this.interactions.isEmpty() ? new EnumMap<>(InteractionType.class) : new EnumMap<>(this.interactions);
      DefaultAssetMap<String, UnarmedInteractions> unarmedInteractionsAssetMap = UnarmedInteractions.getAssetMap();
      UnarmedInteractions fallbackInteractions = this.playerAnimationsId != null ? unarmedInteractionsAssetMap.getAsset(this.playerAnimationsId) : null;
      if (fallbackInteractions != null) {
         for (Entry<InteractionType, String> entry : fallbackInteractions.getInteractions().entrySet()) {
            interactions.putIfAbsent(entry.getKey(), entry.getValue());
         }
      }

      UnarmedInteractions defaultUnarmedInteractions = unarmedInteractionsAssetMap.getAsset("Empty");
      if (defaultUnarmedInteractions != null) {
         for (Entry<InteractionType, String> entry : defaultUnarmedInteractions.getInteractions().entrySet()) {
            interactions.putIfAbsent(entry.getKey(), entry.getValue());
         }
      }

      this.interactions = Collections.unmodifiableMap(interactions);
      if (this.reticleId != null) {
         this.reticleIndex = ItemReticleConfig.getAssetMap().getIndexOrDefault(this.reticleId, 0);
      }

      IndexedLookupTableAssetMap<String, ItemQuality> itemQualityAssetMap = ItemQuality.getAssetMap();
      if (this.qualityId != null) {
         this.qualityIndex = itemQualityAssetMap.getIndexOrDefault(this.qualityId, 0);
         ItemQuality itemQuality = itemQualityAssetMap.getAsset(this.qualityIndex);
         if (itemQuality != null) {
            this.itemEntityConfig = itemQuality.getItemEntityConfig();
         }
      }

      if (this.itemEntityConfig == null) {
         if (this.blockId != null) {
            this.itemEntityConfig = ItemEntityConfig.DEFAULT_BLOCK;
         } else {
            this.itemEntityConfig = ItemEntityConfig.DEFAULT;
         }
      }

      if (this.interactionConfig == null) {
         if (this.weapon != null) {
            this.interactionConfig = InteractionConfiguration.DEFAULT_WEAPON;
         } else {
            this.interactionConfig = InteractionConfiguration.DEFAULT;
         }
      }

      if (this.soundEventId != null) {
         this.soundEventIndex = SoundEvent.getAssetMap().getIndex(this.soundEventId);
      }

      this.itemSoundSetIndex = ItemSoundSet.getAssetMap().getIndex(this.itemSoundSetId);
      if (this.stateToBlock != null) {
         Map<String, String> map = new Object2ObjectOpenHashMap();

         for (Entry<String, String> entry : this.stateToBlock.entrySet()) {
            map.put(entry.getValue(), entry.getKey());
         }

         this.blockToState = Collections.unmodifiableMap(map);
      }

      if (this.recipeToGenerate != null) {
         CraftingRecipe recipe = this.recipeToGenerate;
         CraftingRecipe newRecipe = new CraftingRecipe(recipe);
         MaterialQuantity primaryOutput = new MaterialQuantity(this.id, null, null, newRecipe.primaryOutputQuantity, null);
         if (newRecipe.outputs == null || newRecipe.outputs.length == 0) {
            newRecipe.outputs = new MaterialQuantity[]{primaryOutput};
         }

         newRecipe.primaryOutput = primaryOutput;
         newRecipe.id = CraftingRecipe.generateIdFromItemRecipe(this, 0);
         this.recipeToGenerate = newRecipe;
      }

      this.displayEntityStatsHUD = EntityStatsModule.resolveEntityStats(this.rawDisplayEntityStatsHUD);
   }

   static {
      CODEC_BUILDER.<Map>appendInherited(
            new KeyedCodec<>("State", new MapCodec(new ContainedAssetCodec<>(Item.class, CODEC, ContainedAssetCodec.Mode.INJECT_PARENT), HashMap::new)),
            (item, m) -> item.stateToBlock = m,
            item -> item.stateToBlock,
            (item, parent) -> item.stateToBlock = parent.stateToBlock
         )
         .metadata(new UIEditorSectionStart("State"))
         .add();
   }
}
