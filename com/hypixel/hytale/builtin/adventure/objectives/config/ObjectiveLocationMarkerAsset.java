package com.hypixel.hytale.builtin.adventure.objectives.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.adventure.objectives.config.markerarea.ObjectiveLocationMarkerArea;
import com.hypixel.hytale.builtin.adventure.objectives.config.objectivesetup.ObjectiveTypeSetup;
import com.hypixel.hytale.builtin.adventure.objectives.config.triggercondition.ObjectiveLocationTriggerCondition;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ObjectiveLocationMarkerAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ObjectiveLocationMarkerAsset>> {
   @Nonnull
   public static final AssetBuilderCodec<String, ObjectiveLocationMarkerAsset> CODEC = AssetBuilderCodec.builder(
         ObjectiveLocationMarkerAsset.class,
         ObjectiveLocationMarkerAsset::new,
         Codec.STRING,
         (t, k) -> t.id = k,
         t -> t.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .append(
         new KeyedCodec<>("Setup", ObjectiveTypeSetup.CODEC),
         (objectiveLocationMarkerAsset, objectiveTypeSetup) -> objectiveLocationMarkerAsset.objectiveTypeSetup = objectiveTypeSetup,
         objectiveLocationMarkerAsset -> objectiveLocationMarkerAsset.objectiveTypeSetup
      )
      .addValidator(Validators.nonNull())
      .add()
      .<ObjectiveLocationMarkerArea>append(
         new KeyedCodec<>("Area", ObjectiveLocationMarkerArea.CODEC),
         (objectiveLocationMarkerAsset, area) -> objectiveLocationMarkerAsset.area = area,
         objectiveLocationMarkerAsset -> objectiveLocationMarkerAsset.area
      )
      .addValidator(Validators.nonNull())
      .add()
      .<String[]>append(
         new KeyedCodec<>("EnvironmentIds", Codec.STRING_ARRAY),
         (objectiveLocationMarkerAsset, strings) -> objectiveLocationMarkerAsset.environmentIds = strings,
         objectiveLocationMarkerAsset -> objectiveLocationMarkerAsset.environmentIds
      )
      .addValidator(Environment.VALIDATOR_CACHE.getArrayValidator())
      .add()
      .append(
         new KeyedCodec<>("TriggerConditions", new ArrayCodec<>(ObjectiveLocationTriggerCondition.CODEC, ObjectiveLocationTriggerCondition[]::new)),
         (objectiveLocationMarkerAsset, objectiveLocationTriggerConditions) -> objectiveLocationMarkerAsset.triggerConditions = objectiveLocationTriggerConditions,
         objectiveLocationMarkerAsset -> objectiveLocationMarkerAsset.triggerConditions
      )
      .add()
      .afterDecode(objectiveLocationMarkerAsset -> {
         if (objectiveLocationMarkerAsset.environmentIds != null && objectiveLocationMarkerAsset.environmentIds.length > 0) {
            objectiveLocationMarkerAsset.environmentIndexes = new int[objectiveLocationMarkerAsset.environmentIds.length];

            for (int i = 0; i < objectiveLocationMarkerAsset.environmentIds.length; i++) {
               String key = objectiveLocationMarkerAsset.environmentIds[i];
               int index = Environment.getAssetMap().getIndex(key);
               if (index == Integer.MIN_VALUE) {
                  throw new IllegalArgumentException("Unknown key! " + key);
               }

               objectiveLocationMarkerAsset.environmentIndexes[i] = index;
            }

            Arrays.sort(objectiveLocationMarkerAsset.environmentIndexes);
         }
      })
      .build();
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ObjectiveLocationMarkerAsset::getAssetStore));
   private static AssetStore<String, ObjectiveLocationMarkerAsset, DefaultAssetMap<String, ObjectiveLocationMarkerAsset>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected ObjectiveTypeSetup objectiveTypeSetup;
   protected ObjectiveLocationMarkerArea area;
   protected String[] environmentIds;
   protected int[] environmentIndexes;
   protected ObjectiveLocationTriggerCondition[] triggerConditions;

   public static AssetStore<String, ObjectiveLocationMarkerAsset, DefaultAssetMap<String, ObjectiveLocationMarkerAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ObjectiveLocationMarkerAsset.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ObjectiveLocationMarkerAsset> getAssetMap() {
      return (DefaultAssetMap<String, ObjectiveLocationMarkerAsset>)getAssetStore().getAssetMap();
   }

   public String getId() {
      return this.id;
   }

   public ObjectiveTypeSetup getObjectiveTypeSetup() {
      return this.objectiveTypeSetup;
   }

   public ObjectiveLocationMarkerArea getArea() {
      return this.area;
   }

   public String[] getEnvironmentIds() {
      return this.environmentIds;
   }

   public int[] getEnvironmentIndexes() {
      return this.environmentIndexes;
   }

   public ObjectiveLocationTriggerCondition[] getTriggerConditions() {
      return this.triggerConditions;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ObjectiveLocationMarkerAsset{id='"
         + this.id
         + "', objectiveTypeSetup="
         + this.objectiveTypeSetup
         + ", area="
         + this.area
         + ", environmentIds="
         + Arrays.toString((Object[])this.environmentIds)
         + ", triggerConditions="
         + Arrays.toString((Object[])this.triggerConditions)
         + "}";
   }
}
