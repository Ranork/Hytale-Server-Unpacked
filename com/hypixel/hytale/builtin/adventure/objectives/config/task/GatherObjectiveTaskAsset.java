package com.hypixel.hytale.builtin.adventure.objectives.config.task;

import com.hypixel.hytale.builtin.adventure.objectives.config.taskcondition.TaskConditionAsset;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class GatherObjectiveTaskAsset extends CountObjectiveTaskAsset {
   @Nonnull
   public static final BuilderCodec<GatherObjectiveTaskAsset> CODEC = BuilderCodec.builder(
         GatherObjectiveTaskAsset.class, GatherObjectiveTaskAsset::new, CountObjectiveTaskAsset.CODEC
      )
      .append(
         new KeyedCodec<>("BlockTagOrItemId", BlockTagOrItemIdField.CODEC),
         (gatherObjectiveTaskAsset, blockTagOrItemIdField) -> gatherObjectiveTaskAsset.blockTagOrItemIdField = blockTagOrItemIdField,
         gatherObjectiveTaskAsset -> gatherObjectiveTaskAsset.blockTagOrItemIdField
      )
      .addValidator(Validators.nonNull())
      .add()
      .build();
   protected BlockTagOrItemIdField blockTagOrItemIdField;

   public GatherObjectiveTaskAsset(
      String descriptionId, TaskConditionAsset[] taskConditions, Vector3i[] mapMarkers, int count, BlockTagOrItemIdField blockTagOrItemIdField
   ) {
      super(descriptionId, taskConditions, mapMarkers, count);
      this.blockTagOrItemIdField = blockTagOrItemIdField;
   }

   protected GatherObjectiveTaskAsset() {
   }

   @Nonnull
   @Override
   public ObjectiveTaskAsset.TaskScope getTaskScope() {
      return ObjectiveTaskAsset.TaskScope.PLAYER_AND_MARKER;
   }

   public BlockTagOrItemIdField getBlockTagOrItemIdField() {
      return this.blockTagOrItemIdField;
   }

   @Override
   protected boolean matchesAsset0(ObjectiveTaskAsset task) {
      if (!super.matchesAsset0(task)) {
         return false;
      } else {
         return !(task instanceof GatherObjectiveTaskAsset) ? false : ((GatherObjectiveTaskAsset)task).blockTagOrItemIdField.equals(this.blockTagOrItemIdField);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "GatherObjectiveTaskAsset{blockTagOrItemIdTask=" + this.blockTagOrItemIdField + "} " + super.toString();
   }
}
