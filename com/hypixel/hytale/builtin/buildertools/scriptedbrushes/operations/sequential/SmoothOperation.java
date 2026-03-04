package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigEditStore;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SmoothOperation extends SequenceBrushOperation {
   public static final BuilderCodec<SmoothOperation> CODEC = BuilderCodec.builder(SmoothOperation.class, SmoothOperation::new)
      .append(new KeyedCodec<>("SmoothStrength", Codec.INTEGER), (op, val) -> op.smoothStrength = val, op -> op.smoothStrength)
      .documentation("The strength of smoothing")
      .add()
      .documentation("Smooths the blocks within the brush area as to make the area more natural looking")
      .build();
   @Nonnull
   public Integer smoothStrength = 2;
   private int smoothVolume;
   private int smoothRadius;

   public SmoothOperation() {
      super("Smooth Blocks", "Smooths the blocks within the brush area as to make the area more natural looking", true);
   }

   private void updateVolumeAndRadius() {
      int strength = this.smoothStrength;
      this.smoothRadius = Math.min(strength, 4);
      int smoothRange = this.smoothRadius * 2 + 1;
      this.smoothVolume = smoothRange * smoothRange * smoothRange;
   }

   @Override
   public boolean modifyBlocks(
      Ref<EntityStore> ref,
      BrushConfig brushConfig,
      BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull BrushConfigEditStore edit,
      int x,
      int y,
      int z,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      int currentBlock = edit.getBlock(x, y, z);
      BuilderToolsPlugin.BuilderState.BlocksSampleData data = edit.getBlockSampledataIncludingPreviousStages(x, y, z, 2);
      if (currentBlock != data.mainBlock && data.mainBlockCount > this.smoothVolume * 0.5F) {
         edit.setBlock(x, y, z, data.mainBlock);
      }

      return true;
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.updateVolumeAndRadius();
   }
}
