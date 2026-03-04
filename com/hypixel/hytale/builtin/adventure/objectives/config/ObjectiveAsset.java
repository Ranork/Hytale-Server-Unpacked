package com.hypixel.hytale.builtin.adventure.objectives.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.completion.ObjectiveCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.ObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.TaskSet;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ObjectiveAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ObjectiveAsset>> {
   @Nonnull
   public static final AssetBuilderCodec<String, ObjectiveAsset> CODEC = AssetBuilderCodec.builder(
         ObjectiveAsset.class,
         ObjectiveAsset::new,
         Codec.STRING,
         (objective, s) -> objective.id = s,
         objective -> objective.id,
         (objective, data) -> objective.extraData = data,
         objective -> objective.extraData
      )
      .appendInherited(
         new KeyedCodec<>("Category", Codec.STRING),
         (objectiveAsset, s) -> objectiveAsset.category = s,
         objectiveAsset -> objectiveAsset.category,
         (objectiveAsset, parent) -> objectiveAsset.category = parent.category
      )
      .add()
      .<TaskSet[]>appendInherited(
         new KeyedCodec<>("TaskSets", new ArrayCodec<>(TaskSet.CODEC, TaskSet[]::new)),
         (objective, tasks) -> objective.taskSets = tasks,
         objective -> objective.taskSets,
         (objective, parent) -> objective.taskSets = parent.taskSets
      )
      .addValidator(Validators.nonEmptyArray())
      .add()
      .appendInherited(
         new KeyedCodec<>("Completions", new ArrayCodec<>(ObjectiveCompletionAsset.CODEC, ObjectiveCompletionAsset[]::new)),
         (objective, rewards) -> objective.completionHandlers = rewards,
         objective -> objective.completionHandlers,
         (objective, parent) -> objective.completionHandlers = parent.completionHandlers
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("TitleId", Codec.STRING),
         (objectiveAsset, s) -> objectiveAsset.objectiveTitleKey = s,
         objectiveAsset -> objectiveAsset.objectiveTitleKey,
         (objectiveAsset, parent) -> objectiveAsset.objectiveTitleKey = parent.objectiveTitleKey
      )
      .metadata(new UIEditor(new UIEditor.LocalizationKeyField("server.objectives.{assetId}.title", true)))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("DescriptionId", Codec.STRING),
         (objectiveAsset, s) -> objectiveAsset.objectiveDescriptionKey = s,
         objectiveAsset -> objectiveAsset.objectiveDescriptionKey,
         (objectiveAsset, parent) -> objectiveAsset.objectiveDescriptionKey = parent.objectiveDescriptionKey
      )
      .metadata(new UIEditor(new UIEditor.LocalizationKeyField("server.objectives.{assetId}.desc")))
      .add()
      .appendInherited(
         new KeyedCodec<>("RemoveOnItemDrop", Codec.BOOLEAN),
         (objectiveAsset, aBoolean) -> objectiveAsset.removeOnItemDrop = aBoolean,
         objectiveAsset -> objectiveAsset.removeOnItemDrop,
         (objectiveAsset, parent) -> objectiveAsset.removeOnItemDrop = parent.removeOnItemDrop
      )
      .add()
      .afterDecode(objectiveAsset -> {
         if (objectiveAsset.objectiveTitleKey == null) {
            objectiveAsset.objectiveTitleKey = MessageFormat.format("server.objectives.{0}.title", objectiveAsset.id);
         }

         if (objectiveAsset.objectiveDescriptionKey == null) {
            objectiveAsset.objectiveDescriptionKey = MessageFormat.format("server.objectives.{0}.desc", objectiveAsset.id);
         }
      })
      .build();
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ObjectiveAsset::getAssetStore));
   private static AssetStore<String, ObjectiveAsset, DefaultAssetMap<String, ObjectiveAsset>> ASSET_STORE;
   protected AssetExtraInfo.Data extraData;
   protected String id;
   protected String category;
   protected TaskSet[] taskSets;
   protected ObjectiveCompletionAsset[] completionHandlers;
   protected String objectiveTitleKey;
   protected String objectiveDescriptionKey;
   protected boolean removeOnItemDrop;

   public static AssetStore<String, ObjectiveAsset, DefaultAssetMap<String, ObjectiveAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ObjectiveAsset.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ObjectiveAsset> getAssetMap() {
      return (DefaultAssetMap<String, ObjectiveAsset>)getAssetStore().getAssetMap();
   }

   public ObjectiveAsset(
      String id,
      String category,
      TaskSet[] taskSets,
      ObjectiveCompletionAsset[] completionHandlers,
      String objectiveTitleKey,
      String objectiveDescriptionKey,
      boolean removeOnItemDrop
   ) {
      this.id = id;
      this.category = category;
      this.taskSets = taskSets;
      this.completionHandlers = completionHandlers;
      this.objectiveTitleKey = objectiveTitleKey;
      this.objectiveDescriptionKey = objectiveDescriptionKey;
      this.removeOnItemDrop = removeOnItemDrop;
   }

   protected ObjectiveAsset() {
   }

   public String getId() {
      return this.id;
   }

   public String getCategory() {
      return this.category;
   }

   public String getTitleKey() {
      return this.objectiveTitleKey;
   }

   public String getDescriptionKey() {
      return this.objectiveDescriptionKey;
   }

   public TaskSet[] getTaskSets() {
      return this.taskSets;
   }

   public ObjectiveCompletionAsset[] getCompletionHandlers() {
      return this.completionHandlers;
   }

   public String getObjectiveTitleKey() {
      return this.objectiveTitleKey;
   }

   public String getObjectiveDescriptionKey() {
      return this.objectiveDescriptionKey;
   }

   public boolean isRemoveOnItemDrop() {
      return this.removeOnItemDrop;
   }

   public boolean isValidForPlayer() {
      for (TaskSet taskSet : this.taskSets) {
         for (ObjectiveTaskAsset task : taskSet.getTasks()) {
            if (!task.getTaskScope().isTaskPossibleForPlayer()) {
               ObjectivePlugin.get().getLogger().at(Level.WARNING).log("Task %s isn't valid for Player held objective", task.getClass().toString());
               return false;
            }
         }
      }

      return true;
   }

   public boolean isValidForMarker() {
      for (TaskSet taskSet : this.taskSets) {
         for (ObjectiveTaskAsset task : taskSet.getTasks()) {
            if (!task.getTaskScope().isTaskPossibleForMarker()) {
               ObjectivePlugin.get().getLogger().at(Level.WARNING).log("Task %s isn't valid for Marker held objective", task.getClass().toString());
               return false;
            }
         }
      }

      return true;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ObjectiveAsset{id='"
         + this.id
         + "', category='"
         + this.category
         + "', taskSets="
         + Arrays.toString((Object[])this.taskSets)
         + ", completionHandlers="
         + Arrays.toString((Object[])this.completionHandlers)
         + ", objectiveTitleKey='"
         + this.objectiveTitleKey
         + "', objectiveDescriptionKey='"
         + this.objectiveDescriptionKey
         + "', removeOnItemDrop="
         + this.removeOnItemDrop
         + "}";
   }
}
