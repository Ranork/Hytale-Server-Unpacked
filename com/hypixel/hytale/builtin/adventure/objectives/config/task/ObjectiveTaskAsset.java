package com.hypixel.hytale.builtin.adventure.objectives.config.task;

import com.hypixel.hytale.builtin.adventure.objectives.config.taskcondition.TaskConditionAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import java.text.MessageFormat;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ObjectiveTaskAsset {
   @Nonnull
   public static final CodecMapCodec<ObjectiveTaskAsset> CODEC = new CodecMapCodec<>("Type");
   @Nonnull
   public static final BuilderCodec<ObjectiveTaskAsset> BASE_CODEC = BuilderCodec.abstractBuilder(ObjectiveTaskAsset.class)
      .append(
         new KeyedCodec<>("DescriptionId", Codec.STRING),
         (objectiveTaskAsset, s) -> objectiveTaskAsset.descriptionId = s,
         objectiveTaskAsset -> objectiveTaskAsset.descriptionId
      )
      .add()
      .append(
         new KeyedCodec<>("TaskConditions", new ArrayCodec<>(TaskConditionAsset.CODEC, TaskConditionAsset[]::new)),
         (useBlockObjectiveTaskAsset, inventoryConditions) -> useBlockObjectiveTaskAsset.taskConditions = inventoryConditions,
         useBlockObjectiveTaskAsset -> useBlockObjectiveTaskAsset.taskConditions
      )
      .add()
      .append(
         new KeyedCodec<>("MapMarkerPositions", new ArrayCodec<>(Vector3i.CODEC, Vector3i[]::new)),
         (taskAsset, positions) -> taskAsset.mapMarkers = positions,
         taskAsset -> taskAsset.mapMarkers
      )
      .add()
      .build();
   @Nonnull
   public static final String TASK_DESCRIPTION_KEY = "server.objectives.{0}.taskSet.{1}.task.{2}";
   protected String descriptionId;
   protected TaskConditionAsset[] taskConditions;
   protected Vector3i[] mapMarkers;
   private String defaultDescriptionId;

   public ObjectiveTaskAsset(String descriptionId, TaskConditionAsset[] taskConditions, Vector3i[] mapMarkers) {
      this.descriptionId = descriptionId;
      this.taskConditions = taskConditions;
   }

   protected ObjectiveTaskAsset() {
   }

   public String getDescriptionId() {
      return this.descriptionId;
   }

   @Nonnull
   public String getDescriptionKey(String objectiveId, int taskSetIndex, int taskIndex) {
      if (this.descriptionId != null) {
         return this.descriptionId;
      } else {
         if (this.defaultDescriptionId == null) {
            this.defaultDescriptionId = MessageFormat.format("server.objectives.{0}.taskSet.{1}.task.{2}", objectiveId, taskSetIndex, taskIndex);
         }

         return this.defaultDescriptionId;
      }
   }

   public TaskConditionAsset[] getTaskConditions() {
      return this.taskConditions;
   }

   @Nullable
   public Vector3i[] getMapMarkers() {
      return this.mapMarkers;
   }

   public abstract ObjectiveTaskAsset.TaskScope getTaskScope();

   public boolean matchesAsset(@Nonnull ObjectiveTaskAsset task) {
      if (!Arrays.equals((Object[])task.taskConditions, (Object[])this.taskConditions)) {
         return false;
      } else if (!Arrays.equals((Object[])task.mapMarkers, (Object[])this.mapMarkers)) {
         return false;
      } else {
         return !task.getClass().equals(this.getClass()) ? false : this.matchesAsset0(task);
      }
   }

   protected abstract boolean matchesAsset0(ObjectiveTaskAsset var1);

   @Nonnull
   @Override
   public String toString() {
      return "ObjectiveTaskAsset{descriptionId='"
         + this.descriptionId
         + "', taskConditions="
         + Arrays.toString((Object[])this.taskConditions)
         + ", mapMarkers="
         + Arrays.toString((Object[])this.mapMarkers)
         + "}";
   }

   public static enum TaskScope {
      PLAYER,
      MARKER,
      PLAYER_AND_MARKER;

      public boolean isTaskPossibleForMarker() {
         return this == MARKER || this == PLAYER_AND_MARKER;
      }

      public boolean isTaskPossibleForPlayer() {
         return this == PLAYER || this == PLAYER_AND_MARKER;
      }
   }
}
