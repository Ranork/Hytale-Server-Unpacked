package com.hypixel.hytale.builtin.adventure.objectives.historydata;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import javax.annotation.Nonnull;

public abstract class ObjectiveRewardHistoryData {
   @Nonnull
   public static final CodecMapCodec<ObjectiveRewardHistoryData> CODEC = new CodecMapCodec<>("Type");
   @Nonnull
   public static final BuilderCodec<ObjectiveRewardHistoryData> BASE_CODEC = BuilderCodec.abstractBuilder(ObjectiveRewardHistoryData.class).build();
}
