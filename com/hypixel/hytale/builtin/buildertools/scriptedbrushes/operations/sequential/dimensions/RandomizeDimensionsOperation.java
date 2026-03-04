package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.dimensions;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntegerRange;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class RandomizeDimensionsOperation extends SequenceBrushOperation {
   public static final BuilderCodec<RandomizeDimensionsOperation> CODEC = BuilderCodec.builder(
         RandomizeDimensionsOperation.class, RandomizeDimensionsOperation::new
      )
      .append(new KeyedCodec<>("WidthRange", RelativeIntegerRange.CODEC), (op, val) -> op.widthRangeArg = val, op -> op.widthRangeArg)
      .documentation("The range of values for the width, optionally relative using tilde")
      .add()
      .<RelativeIntegerRange>append(new KeyedCodec<>("HeightRange", RelativeIntegerRange.CODEC), (op, val) -> op.heightRangeArg = val, op -> op.heightRangeArg)
      .documentation("The range of values for the height, optionally relative using tilde")
      .add()
      .documentation("Randomize the dimensions of the brush area")
      .build();
   @Nonnull
   public RelativeIntegerRange widthRangeArg = new RelativeIntegerRange(1, 1);
   @Nonnull
   public RelativeIntegerRange heightRangeArg = new RelativeIntegerRange(1, 1);

   public RandomizeDimensionsOperation() {
      super("Randomize Dimensions", "Randomize the dimensions of the brush area", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      brushConfig.setShapeWidth(this.widthRangeArg.getNumberInRange(brushConfig.getShapeWidth()));
      brushConfig.setShapeHeight(this.heightRangeArg.getNumberInRange(brushConfig.getShapeHeight()));
   }
}
