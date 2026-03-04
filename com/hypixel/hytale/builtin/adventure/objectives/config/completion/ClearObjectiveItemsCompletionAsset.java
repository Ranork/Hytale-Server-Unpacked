package com.hypixel.hytale.builtin.adventure.objectives.config.completion;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ClearObjectiveItemsCompletionAsset extends ObjectiveCompletionAsset {
   @Nonnull
   public static final BuilderCodec<ClearObjectiveItemsCompletionAsset> CODEC = BuilderCodec.builder(
         ClearObjectiveItemsCompletionAsset.class, ClearObjectiveItemsCompletionAsset::new, BASE_CODEC
      )
      .build();

   protected ClearObjectiveItemsCompletionAsset() {
   }

   @Nonnull
   @Override
   public String toString() {
      return "ClearObjectiveItemsCompletionAsset{} " + super.toString();
   }
}
