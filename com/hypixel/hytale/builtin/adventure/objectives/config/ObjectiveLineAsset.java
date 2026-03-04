package com.hypixel.hytale.builtin.adventure.objectives.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import java.text.MessageFormat;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ObjectiveLineAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ObjectiveLineAsset>> {
   @Nonnull
   public static AssetBuilderCodec<String, ObjectiveLineAsset> CODEC = AssetBuilderCodec.builder(
         ObjectiveLineAsset.class,
         ObjectiveLineAsset::new,
         Codec.STRING,
         (objectiveLine, s) -> objectiveLine.id = s,
         objectiveLine -> objectiveLine.id,
         (objectiveLine, data) -> objectiveLine.extraData = data,
         objectiveLine -> objectiveLine.extraData
      )
      .appendInherited(
         new KeyedCodec<>("Category", Codec.STRING),
         (objectiveAsset, s) -> objectiveAsset.category = s,
         objectiveAsset -> objectiveAsset.category,
         (objectiveAsset, parent) -> objectiveAsset.category = parent.category
      )
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("ObjectiveIds", Codec.STRING_ARRAY),
         (objectiveLineAsset, strings) -> objectiveLineAsset.objectiveIds = strings,
         objectiveLineAsset -> objectiveLineAsset.objectiveIds,
         (objectiveLineAsset, parent) -> objectiveLineAsset.objectiveIds = parent.objectiveIds
      )
      .addValidator(Validators.nonEmptyArray())
      .addValidator(Validators.uniqueInArray())
      .addValidator(ObjectiveAsset.VALIDATOR_CACHE.getArrayValidator())
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("NextObjectiveLineIds", Codec.STRING_ARRAY),
         (objectiveLineAsset, strings) -> objectiveLineAsset.nextObjectiveLineIds = strings,
         objectiveLineAsset -> objectiveLineAsset.nextObjectiveLineIds,
         (objectiveLineAsset, parent) -> objectiveLineAsset.nextObjectiveLineIds = parent.nextObjectiveLineIds
      )
      .addValidator(Validators.uniqueInArray())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("TitleId", Codec.STRING),
         (objectiveLineAsset, s) -> objectiveLineAsset.objectiveTitleKey = s,
         objectiveLineAsset -> objectiveLineAsset.objectiveTitleKey,
         (objectiveLineAsset, parent) -> objectiveLineAsset.objectiveTitleKey = parent.objectiveTitleKey
      )
      .metadata(new UIEditor(new UIEditor.LocalizationKeyField("objectivelines.{assetId}.title", true)))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("DescriptionId", Codec.STRING),
         (objectiveLineAsset, s) -> objectiveLineAsset.objectiveDescriptionKey = s,
         objectiveLineAsset -> objectiveLineAsset.objectiveDescriptionKey,
         (objectiveLineAsset, parent) -> objectiveLineAsset.objectiveDescriptionKey = parent.objectiveDescriptionKey
      )
      .metadata(new UIEditor(new UIEditor.LocalizationKeyField("objectivelines.{assetId}.desc", true)))
      .add()
      .afterDecode(objectiveAsset -> {
         if (objectiveAsset.objectiveTitleKey != null) {
            objectiveAsset.objectiveTitleKey = MessageFormat.format("objectivelines.{0}.title", objectiveAsset.id);
         }

         if (objectiveAsset.objectiveDescriptionKey != null) {
            objectiveAsset.objectiveDescriptionKey = MessageFormat.format("objectivelines.{0}.desc", objectiveAsset.id);
         }
      })
      .build();
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ObjectiveLineAsset::getAssetStore));
   private static AssetStore<String, ObjectiveLineAsset, DefaultAssetMap<String, ObjectiveLineAsset>> ASSET_STORE;
   protected AssetExtraInfo.Data extraData;
   protected String id;
   protected String category;
   protected String[] objectiveIds;
   protected String objectiveTitleKey;
   protected String objectiveDescriptionKey;
   protected String[] nextObjectiveLineIds;

   public static AssetStore<String, ObjectiveLineAsset, DefaultAssetMap<String, ObjectiveLineAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ObjectiveLineAsset.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ObjectiveLineAsset> getAssetMap() {
      return (DefaultAssetMap<String, ObjectiveLineAsset>)getAssetStore().getAssetMap();
   }

   public ObjectiveLineAsset(
      String id, String category, String[] objectiveIds, String objectiveTitleKey, String objectiveDescriptionKey, String[] nextObjectiveLineIds
   ) {
      this.id = id;
      this.category = category;
      this.objectiveIds = objectiveIds;
      this.objectiveTitleKey = objectiveTitleKey;
      this.objectiveDescriptionKey = objectiveDescriptionKey;
      this.nextObjectiveLineIds = nextObjectiveLineIds;
   }

   protected ObjectiveLineAsset() {
   }

   public String getId() {
      return this.id;
   }

   public String getCategory() {
      return this.category;
   }

   public String[] getObjectiveIds() {
      return this.objectiveIds;
   }

   @Nullable
   public String getNextObjectiveId(String currentObjectiveId) {
      if (this.objectiveIds != null && this.objectiveIds.length != 0) {
         for (int i = 0; i < this.objectiveIds.length - 1; i++) {
            if (this.objectiveIds[i].equals(currentObjectiveId)) {
               return this.objectiveIds[i + 1];
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public String getObjectiveTitleKey() {
      return this.objectiveTitleKey;
   }

   public String getObjectiveDescriptionKey() {
      return this.objectiveDescriptionKey;
   }

   public String[] getNextObjectiveLineIds() {
      return this.nextObjectiveLineIds;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ObjectiveLineAsset{id='"
         + this.id
         + "', category='"
         + this.category
         + "', objectiveIds="
         + Arrays.toString((Object[])this.objectiveIds)
         + ", objectiveTitleKey='"
         + this.objectiveTitleKey
         + "', objectiveDescriptionKey='"
         + this.objectiveDescriptionKey
         + "', nextObjectiveLineIds="
         + Arrays.toString((Object[])this.nextObjectiveLineIds)
         + "}";
   }
}
