package com.hypixel.hytale.builtin.adventure.objectives.config.task;

import com.hypixel.hytale.builtin.adventure.objectives.config.taskcondition.TaskConditionAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UseEntityObjectiveTaskAsset extends CountObjectiveTaskAsset {
   @Nonnull
   public static final BuilderCodec<UseEntityObjectiveTaskAsset> CODEC = BuilderCodec.builder(
         UseEntityObjectiveTaskAsset.class, UseEntityObjectiveTaskAsset::new, CountObjectiveTaskAsset.CODEC
      )
      .append(
         new KeyedCodec<>("TaskId", Codec.STRING),
         (useEntityObjectiveTaskAsset, s) -> useEntityObjectiveTaskAsset.taskId = s,
         useEntityObjectiveTaskAsset -> useEntityObjectiveTaskAsset.taskId
      )
      .addValidator(Validators.nonNull())
      .add()
      .append(
         new KeyedCodec<>("AnimationIdToPlay", Codec.STRING),
         (useEntityObjectiveTaskAsset, s) -> useEntityObjectiveTaskAsset.animationIdToPlay = s,
         useEntityObjectiveTaskAsset -> useEntityObjectiveTaskAsset.animationIdToPlay
      )
      .add()
      .append(
         new KeyedCodec<>("Dialog", UseEntityObjectiveTaskAsset.DialogOptions.CODEC),
         (useEntityObjectiveTask, dialogOptions) -> useEntityObjectiveTask.dialogOptions = dialogOptions,
         useEntityObjectiveTask -> useEntityObjectiveTask.dialogOptions
      )
      .add()
      .build();
   protected String taskId;
   protected String animationIdToPlay;
   protected UseEntityObjectiveTaskAsset.DialogOptions dialogOptions;

   public UseEntityObjectiveTaskAsset(
      String descriptionId,
      TaskConditionAsset[] taskConditions,
      Vector3i[] mapMarkers,
      int count,
      String taskId,
      String animationIdToPlay,
      UseEntityObjectiveTaskAsset.DialogOptions dialogOptions
   ) {
      super(descriptionId, taskConditions, mapMarkers, count);
      this.taskId = taskId;
      this.animationIdToPlay = animationIdToPlay;
      this.dialogOptions = dialogOptions;
   }

   protected UseEntityObjectiveTaskAsset() {
   }

   @Nonnull
   @Override
   public ObjectiveTaskAsset.TaskScope getTaskScope() {
      return ObjectiveTaskAsset.TaskScope.PLAYER_AND_MARKER;
   }

   public String getTaskId() {
      return this.taskId;
   }

   public String getAnimationIdToPlay() {
      return this.animationIdToPlay;
   }

   public UseEntityObjectiveTaskAsset.DialogOptions getDialogOptions() {
      return this.dialogOptions;
   }

   @Override
   protected boolean matchesAsset0(ObjectiveTaskAsset task) {
      if (!super.matchesAsset0(task)) {
         return false;
      } else if (task instanceof UseEntityObjectiveTaskAsset asset) {
         if (!Objects.equals(asset.animationIdToPlay, this.animationIdToPlay)) {
            return false;
         } else {
            return !Objects.equals(asset.dialogOptions, this.dialogOptions) ? false : asset.taskId.equals(this.taskId);
         }
      } else {
         return false;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "UseEntityObjectiveTaskAsset{taskId='"
         + this.taskId
         + "', animationIdToPlay='"
         + this.animationIdToPlay
         + "', dialogOptions="
         + this.dialogOptions
         + "} "
         + super.toString();
   }

   public static class DialogOptions {
      @Nonnull
      public static BuilderCodec<UseEntityObjectiveTaskAsset.DialogOptions> CODEC = BuilderCodec.builder(
            UseEntityObjectiveTaskAsset.DialogOptions.class, UseEntityObjectiveTaskAsset.DialogOptions::new
         )
         .append(
            new KeyedCodec<>("EntityNameKey", Codec.STRING),
            (dialogOptions, s) -> dialogOptions.entityNameKey = s,
            dialogOptions -> dialogOptions.entityNameKey
         )
         .addValidator(Validators.nonNull())
         .add()
         .<String>append(
            new KeyedCodec<>("DialogKey", Codec.STRING), (dialogOptions, s) -> dialogOptions.dialogKey = s, dialogOptions -> dialogOptions.dialogKey
         )
         .addValidator(Validators.nonNull())
         .add()
         .build();
      protected String entityNameKey;
      protected String dialogKey;

      public DialogOptions(String entityNameKey, String dialogKey) {
         this.entityNameKey = entityNameKey;
         this.dialogKey = dialogKey;
      }

      protected DialogOptions() {
      }

      public String getEntityNameKey() {
         return this.entityNameKey;
      }

      public String getDialogKey() {
         return this.dialogKey;
      }

      @Override
      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            UseEntityObjectiveTaskAsset.DialogOptions that = (UseEntityObjectiveTaskAsset.DialogOptions)o;
            return !this.entityNameKey.equals(that.entityNameKey) ? false : this.dialogKey.equals(that.dialogKey);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.entityNameKey.hashCode();
         return 31 * result + this.dialogKey.hashCode();
      }

      @Nonnull
      @Override
      public String toString() {
         return "DialogOptions{entityNameKey='" + this.entityNameKey + "', dialogKey='" + this.dialogKey + "'}";
      }
   }
}
