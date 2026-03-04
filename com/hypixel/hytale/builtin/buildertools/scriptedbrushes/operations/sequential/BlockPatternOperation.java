package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class BlockPatternOperation extends SequenceBrushOperation {
   public static final BuilderCodec<BlockPatternOperation> CODEC = BuilderCodec.builder(BlockPatternOperation.class, BlockPatternOperation::new)
      .append(new KeyedCodec<>("BlockPattern", BlockPattern.CODEC), (op, val) -> op.blockPatternArg = val, op -> op.blockPatternArg)
      .documentation("The pattern of blocks to use in your set")
      .add()
      .documentation("Change the material of the brush to a pattern of blocks")
      .build();
   @Nonnull
   public BlockPattern blockPatternArg = BlockPattern.parse("Rock_Stone");

   public BlockPatternOperation() {
      super("Block Pattern", "Change the material of the brush to a pattern of blocks", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      brushConfig.setPattern(this.blockPatternArg);
   }
}
