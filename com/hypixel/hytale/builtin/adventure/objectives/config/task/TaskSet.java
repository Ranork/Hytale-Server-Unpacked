package com.hypixel.hytale.builtin.adventure.objectives.config.task;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import java.text.MessageFormat;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class TaskSet {
   @Nonnull
   public static final BuilderCodec<TaskSet> CODEC = BuilderCodec.builder(TaskSet.class, TaskSet::new)
      .append(new KeyedCodec<>("DescriptionId", Codec.STRING), (taskSet, s) -> taskSet.descriptionId = s, taskSet -> taskSet.descriptionId)
      .add()
      .<ObjectiveTaskAsset[]>append(
         new KeyedCodec<>("Tasks", new ArrayCodec<>(ObjectiveTaskAsset.CODEC, ObjectiveTaskAsset[]::new)),
         (taskSet, objectiveTaskAssets) -> taskSet.tasks = objectiveTaskAssets,
         taskSet -> taskSet.tasks
      )
      .addValidator(Validators.nonEmptyArray())
      .add()
      .build();
   @Nonnull
   public static final String TASKSET_DESCRIPTION_KEY = "server.objectives.{0}.taskSet.{1}";
   protected String descriptionId;
   protected ObjectiveTaskAsset[] tasks;

   public TaskSet(String descriptionId, ObjectiveTaskAsset[] tasks) {
      this.descriptionId = descriptionId;
      this.tasks = tasks;
   }

   protected TaskSet() {
   }

   public String getDescriptionId() {
      return this.descriptionId;
   }

   @Nonnull
   public String getDescriptionKey(String objectiveId, int taskSetIndex) {
      return this.descriptionId != null ? this.descriptionId : MessageFormat.format("server.objectives.{0}.taskSet.{1}", objectiveId, taskSetIndex);
   }

   public ObjectiveTaskAsset[] getTasks() {
      return this.tasks;
   }

   @Nonnull
   @Override
   public String toString() {
      return "TaskSet{descriptionId='" + this.descriptionId + "', tasks=" + Arrays.toString((Object[])this.tasks) + "}";
   }
}
