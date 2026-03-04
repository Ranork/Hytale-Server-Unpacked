package com.hypixel.hytale.builtin.adventure.objectives.config.completion;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import javax.annotation.Nonnull;

public abstract class ObjectiveCompletionAsset {
   @Nonnull
   public static final CodecMapCodec<ObjectiveCompletionAsset> CODEC = new CodecMapCodec<>("Type");
   @Nonnull
   public static final BuilderCodec<ObjectiveCompletionAsset> BASE_CODEC = BuilderCodec.abstractBuilder(ObjectiveCompletionAsset.class).build();

   protected ObjectiveCompletionAsset() {
   }

   @Nonnull
   @Override
   public String toString() {
      return "ObjectiveCompletionAsset{}";
   }
}
