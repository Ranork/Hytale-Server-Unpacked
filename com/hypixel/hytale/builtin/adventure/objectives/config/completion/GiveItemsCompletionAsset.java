package com.hypixel.hytale.builtin.adventure.objectives.config.completion;

import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import javax.annotation.Nonnull;

public class GiveItemsCompletionAsset extends ObjectiveCompletionAsset {
   @Nonnull
   public static final BuilderCodec<GiveItemsCompletionAsset> CODEC = BuilderCodec.builder(
         GiveItemsCompletionAsset.class, GiveItemsCompletionAsset::new, BASE_CODEC
      )
      .append(
         new KeyedCodec<>("DropList", new ContainedAssetCodec<>(ItemDropList.class, ItemDropList.CODEC)),
         (objective, dropListId) -> objective.dropListId = dropListId,
         objective -> objective.dropListId
      )
      .addValidator(Validators.nonNull())
      .addValidator(ItemDropList.VALIDATOR_CACHE.getValidator())
      .add()
      .build();
   protected String dropListId;

   public GiveItemsCompletionAsset(String dropListId) {
      this.dropListId = dropListId;
   }

   protected GiveItemsCompletionAsset() {
   }

   public String getDropListId() {
      return this.dropListId;
   }

   @Nonnull
   @Override
   public String toString() {
      return "GiveItemsCompletionAsset{dropListId='" + this.dropListId + "'} " + super.toString();
   }
}
