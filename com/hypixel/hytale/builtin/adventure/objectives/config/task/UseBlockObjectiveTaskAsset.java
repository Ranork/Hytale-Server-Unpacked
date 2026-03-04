package com.hypixel.hytale.builtin.adventure.objectives.config.task;

import com.hypixel.hytale.builtin.adventure.objectives.config.taskcondition.TaskConditionAsset;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class UseBlockObjectiveTaskAsset extends CountObjectiveTaskAsset {
   @Nonnull
   public static final BuilderCodec<UseBlockObjectiveTaskAsset> CODEC = BuilderCodec.builder(
         UseBlockObjectiveTaskAsset.class, UseBlockObjectiveTaskAsset::new, CountObjectiveTaskAsset.CODEC
      )
      .append(
         new KeyedCodec<>("BlockTagOrItemId", BlockTagOrItemIdField.CODEC),
         (useBlockObjectiveTaskAsset, blockTypeOrSetTaskField) -> useBlockObjectiveTaskAsset.blockTagOrItemIdField = blockTypeOrSetTaskField,
         useBlockObjectiveTaskAsset -> useBlockObjectiveTaskAsset.blockTagOrItemIdField
      )
      .addValidator(Validators.nonNull())
      .add()
      .build();
   protected BlockTagOrItemIdField blockTagOrItemIdField;

   public UseBlockObjectiveTaskAsset(
      String descriptionId, TaskConditionAsset[] taskConditions, Vector3i[] mapMarkers, int count, BlockTagOrItemIdField blockTagOrItemIdField
   ) {
      super(descriptionId, taskConditions, mapMarkers, count);
      this.blockTagOrItemIdField = blockTagOrItemIdField;
   }

   protected UseBlockObjectiveTaskAsset() {
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
         return !(task instanceof UseBlockObjectiveTaskAsset)
            ? false
            : ((UseBlockObjectiveTaskAsset)task).blockTagOrItemIdField.equals(this.blockTagOrItemIdField);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "UseBlockObjectiveTaskAsset{blockTagOrItemIdField=" + this.blockTagOrItemIdField + "} " + super.toString();
   }
}
