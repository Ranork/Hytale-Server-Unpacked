package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import javax.annotation.Nonnull;

public class ItemQuality
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, ItemQuality>>,
   NetworkSerializable<com.hypixel.hytale.protocol.ItemQuality> {
   @Nonnull
   public static final AssetBuilderCodec<String, ItemQuality> CODEC = AssetBuilderCodec.builder(
         ItemQuality.class,
         ItemQuality::new,
         Codec.STRING,
         (itemQuality, s) -> itemQuality.id = s,
         ItemQuality::getId,
         (itemQuality, data) -> itemQuality.data = data,
         itemQuality -> itemQuality.data
      )
      .append(
         new KeyedCodec<>("QualityValue", Codec.INTEGER), (itemQuality, integer) -> itemQuality.qualityValue = integer, itemQuality -> itemQuality.qualityValue
      )
      .documentation("Define the value of the quality to order them, 0 being the lowest quality.")
      .add()
      .<String>append(
         new KeyedCodec<>("ItemTooltipTexture", Codec.STRING),
         (itemQuality, s) -> itemQuality.itemTooltipTexture = s,
         itemQuality -> itemQuality.itemTooltipTexture
      )
      .documentation("The path to the texture of the item tooltip. It has to be located in Common/Items/Qualities.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyString())
      .addValidator(CommonAssetValidator.TEXTURE_ITEM_QUALITY)
      .add()
      .<String>append(
         new KeyedCodec<>("ItemTooltipArrowTexture", Codec.STRING),
         (itemQuality, s) -> itemQuality.itemTooltipArrowTexture = s,
         itemQuality -> itemQuality.itemTooltipArrowTexture
      )
      .documentation("The path to the texture of the item tooltip arrow. It has to be located in Common/Items/Qualities.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyString())
      .addValidator(CommonAssetValidator.TEXTURE_ITEM_QUALITY)
      .add()
      .<String>append(new KeyedCodec<>("SlotTexture", Codec.STRING), (itemQuality, s) -> itemQuality.slotTexture = s, itemQuality -> itemQuality.slotTexture)
      .documentation("The path to the texture of the item slot. It has to be located in Common/Items/Qualities.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyString())
      .addValidator(CommonAssetValidator.TEXTURE_ITEM_QUALITY)
      .add()
      .<String>append(
         new KeyedCodec<>("BlockSlotTexture", Codec.STRING), (itemQuality, s) -> itemQuality.blockSlotTexture = s, itemQuality -> itemQuality.blockSlotTexture
      )
      .documentation("The path to the texture of the item slot, if it has an associated block type. It has to be located in Common/Items/Qualities.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyString())
      .addValidator(CommonAssetValidator.TEXTURE_ITEM_QUALITY)
      .add()
      .<String>append(
         new KeyedCodec<>("SpecialSlotTexture", Codec.STRING),
         (itemQuality, s) -> itemQuality.specialSlotTexture = s,
         itemQuality -> itemQuality.specialSlotTexture
      )
      .documentation(
         "The path to the texture of the item slot used when RenderSpecialSlot is true and the item is consumable or usable. It has to be located in Common/Items/Qualities."
      )
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyString())
      .addValidator(CommonAssetValidator.TEXTURE_ITEM_QUALITY)
      .add()
      .<Color>append(new KeyedCodec<>("TextColor", ProtocolCodecs.COLOR), (itemQuality, s) -> itemQuality.textColor = s, itemQuality -> itemQuality.textColor)
      .documentation("The color that'll be used to display the text of the item in the inventory.")
      .addValidator(Validators.nonNull())
      .add()
      .<String>append(
         new KeyedCodec<>("LocalizationKey", Codec.STRING), (itemQuality, s) -> itemQuality.localizationKey = s, itemQuality -> itemQuality.localizationKey
      )
      .documentation("The localization key for the item quality name.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyString())
      .metadata(new UIEditor(new UIEditor.LocalizationKeyField("qualities.{assetId}", true)))
      .add()
      .<Boolean>append(
         new KeyedCodec<>("VisibleQualityLabel", Codec.BOOLEAN),
         (itemQuality, aBoolean) -> itemQuality.visibleQualityLabel = aBoolean,
         itemQuality -> itemQuality.visibleQualityLabel
      )
      .documentation("To specify the quality label should be displayed in the tooltip.")
      .add()
      .<Boolean>append(
         new KeyedCodec<>("RenderSpecialSlot", Codec.BOOLEAN),
         (itemQuality, aBoolean) -> itemQuality.renderSpecialSlot = aBoolean,
         itemQuality -> itemQuality.renderSpecialSlot
      )
      .documentation("To specify if we display a special slot texture if the item is a consumable or usable.")
      .add()
      .<ItemEntityConfig>append(
         new KeyedCodec<>("ItemEntityConfig", ItemEntityConfig.CODEC),
         (itemQuality, itemEntityConfig) -> itemQuality.itemEntityConfig = itemEntityConfig,
         itemQuality -> itemQuality.itemEntityConfig
      )
      .documentation(
         "Provides an ItemEntityConfig used for all items with their item quality set to this asset unless overridden by an ItemEntityConfig defined on the item itself."
      )
      .add()
      .<Boolean>append(
         new KeyedCodec<>("HideFromSearch", Codec.BOOLEAN),
         (itemQuality, aBoolean) -> itemQuality.hideFromSearch = aBoolean,
         itemQuality -> itemQuality.hideFromSearch
      )
      .documentation("Whether this item is hidden from typical public search, like the creative library")
      .add()
      .build();
   public static final int DEFAULT_INDEX = 0;
   public static final String DEFAULT_ID = "Default";
   @Nonnull
   public static final ItemQuality DEFAULT_ITEM_QUALITY = new ItemQuality("Default") {
      {
         this.qualityValue = -1;
         this.itemTooltipTexture = "UI/ItemQualities/Tooltips/ItemTooltipDefault.png";
         this.itemTooltipArrowTexture = "UI/ItemQualities/Tooltips/ItemTooltipDefaultArrow.png";
         this.slotTexture = "UI/ItemQualities/Slots/SlotDefault.png";
         this.blockSlotTexture = "UI/ItemQualities/Slots/SlotDefault.png";
         this.specialSlotTexture = "UI/ItemQualities/Slots/SpecialSlotDefault.png";
         this.textColor = ColorParseUtil.hexStringToColor("#c9d2dd");
         this.localizationKey = "server.general.qualities.Default";
         this.hideFromSearch = false;
      }
   };
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ItemQuality::getAssetStore));
   private static AssetStore<String, ItemQuality, IndexedLookupTableAssetMap<String, ItemQuality>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected int qualityValue;
   protected String itemTooltipTexture;
   protected String itemTooltipArrowTexture;
   protected String slotTexture;
   protected String blockSlotTexture;
   protected String specialSlotTexture;
   protected Color textColor;
   protected String localizationKey;
   protected boolean visibleQualityLabel;
   protected boolean renderSpecialSlot;
   protected ItemEntityConfig itemEntityConfig;
   protected boolean hideFromSearch = false;
   private transient SoftReference<com.hypixel.hytale.protocol.ItemQuality> cachedPacket;

   @Nonnull
   public static AssetStore<String, ItemQuality, IndexedLookupTableAssetMap<String, ItemQuality>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ItemQuality.class);
      }

      return ASSET_STORE;
   }

   @Nonnull
   public static IndexedLookupTableAssetMap<String, ItemQuality> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, ItemQuality>)getAssetStore().getAssetMap();
   }

   public ItemQuality(
      String id,
      int qualityValue,
      String itemTooltipTexture,
      String itemTooltipArrowTexture,
      String slotTexture,
      String blockSlotTexture,
      String specialSlotTexture,
      Color textColor,
      String localizationKey,
      boolean visibleQualityLabel,
      boolean renderSpecialSlot,
      boolean hideFromSearch,
      ItemEntityConfig itemEntityConfig
   ) {
      this.id = id;
      this.qualityValue = qualityValue;
      this.itemTooltipTexture = itemTooltipTexture;
      this.itemTooltipArrowTexture = itemTooltipArrowTexture;
      this.slotTexture = slotTexture;
      this.blockSlotTexture = blockSlotTexture;
      this.specialSlotTexture = specialSlotTexture;
      this.textColor = textColor;
      this.localizationKey = localizationKey;
      this.visibleQualityLabel = visibleQualityLabel;
      this.renderSpecialSlot = renderSpecialSlot;
      this.hideFromSearch = hideFromSearch;
      this.itemEntityConfig = itemEntityConfig;
   }

   public ItemQuality(@Nonnull String id) {
      this.id = id;
   }

   protected ItemQuality() {
   }

   public String getId() {
      return this.id;
   }

   public int getQualityValue() {
      return this.qualityValue;
   }

   public String getItemTooltipTexture() {
      return this.itemTooltipTexture;
   }

   public String getItemTooltipArrowTexture() {
      return this.itemTooltipArrowTexture;
   }

   public String getSlotTexture() {
      return this.slotTexture;
   }

   public String getBlockSlotTexture() {
      return this.blockSlotTexture;
   }

   public String getSpecialSlotTexture() {
      return this.specialSlotTexture;
   }

   public Color getTextColor() {
      return this.textColor;
   }

   public String getLocalizationKey() {
      return this.localizationKey;
   }

   public boolean isVisibleQualityLabel() {
      return this.visibleQualityLabel;
   }

   public boolean isRenderSpecialSlot() {
      return this.renderSpecialSlot;
   }

   public boolean isHiddenFromSearch() {
      return this.hideFromSearch;
   }

   public ItemEntityConfig getItemEntityConfig() {
      return this.itemEntityConfig;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ItemQuality toPacket() {
      com.hypixel.hytale.protocol.ItemQuality cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.ItemQuality packet = new com.hypixel.hytale.protocol.ItemQuality();
         packet.id = this.id;
         packet.itemTooltipTexture = this.itemTooltipTexture;
         packet.itemTooltipArrowTexture = this.itemTooltipArrowTexture;
         packet.slotTexture = this.slotTexture;
         packet.blockSlotTexture = this.blockSlotTexture;
         packet.specialSlotTexture = this.specialSlotTexture;
         packet.textColor = this.textColor;
         packet.localizationKey = this.localizationKey;
         packet.visibleQualityLabel = this.visibleQualityLabel;
         packet.renderSpecialSlot = this.renderSpecialSlot;
         packet.hideFromSearch = this.hideFromSearch;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemQuality{id='"
         + this.id
         + "', qualityValue="
         + this.qualityValue
         + ", itemTooltipTexture='"
         + this.itemTooltipTexture
         + "', itemTooltipArrowTexture='"
         + this.itemTooltipArrowTexture
         + "', slotTexture='"
         + this.slotTexture
         + "', blockSlotTexture='"
         + this.blockSlotTexture
         + "', specialSlotTexture='"
         + this.specialSlotTexture
         + "', textColor='"
         + this.textColor
         + "', localizationKey='"
         + this.localizationKey
         + "', visibleQualityLabel="
         + this.visibleQualityLabel
         + ", renderSpecialSlot="
         + this.renderSpecialSlot
         + ", itemEntityConfig="
         + this.itemEntityConfig
         + ", hideFromSearch="
         + this.hideFromSearch
         + "}";
   }
}
