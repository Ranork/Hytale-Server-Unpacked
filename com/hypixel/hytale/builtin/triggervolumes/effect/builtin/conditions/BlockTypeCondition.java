package com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class BlockTypeCondition extends TriggerCondition {
   @Nonnull
   public static final BuilderCodec<BlockTypeCondition> CODEC = BuilderCodec.builder(BlockTypeCondition.class, BlockTypeCondition::new, BASE_CODEC)
      .append(
         new KeyedCodec<>("BlockType", Codec.STRING_ARRAY, false),
         (condition, blockTypes) -> condition.blockTypes = blockTypes != null ? blockTypes : new String[0],
         condition -> condition.blockTypes.length == 0 ? null : condition.blockTypes
      )
      .add()
      .build();
   @Nonnull
   private String[] blockTypes = new String[0];

   @Nonnull
   public static BlockTypeCondition create(@Nonnull TriggerEventType eventType, @Nonnull String... blockTypes) {
      BlockTypeCondition condition = new BlockTypeCondition();
      condition.setEventType(eventType);
      condition.blockTypes = blockTypes;
      return condition;
   }

   @Override
   public boolean test(@Nonnull TriggerContext context) {
      String blockId = context.getBlockId();
      return blockId != null && this.blockTypes.length != 0 ? Arrays.asList(this.blockTypes).contains(blockId) : false;
   }
}
