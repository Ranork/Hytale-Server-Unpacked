package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.offsets;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntegerRange;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class RandomOffsetOperation extends SequenceBrushOperation {
   public static final BuilderCodec<RandomOffsetOperation> CODEC = BuilderCodec.builder(RandomOffsetOperation.class, RandomOffsetOperation::new)
      .append(new KeyedCodec<>("XOffsetRange", RelativeIntegerRange.CODEC), (op, val) -> op.xOffsetArg = val, op -> op.xOffsetArg)
      .documentation("The range of allowed values for the X offset")
      .add()
      .<RelativeIntegerRange>append(new KeyedCodec<>("YOffsetRange", RelativeIntegerRange.CODEC), (op, val) -> op.yOffsetArg = val, op -> op.yOffsetArg)
      .documentation("The range of allowed values for the Z offset")
      .add()
      .<RelativeIntegerRange>append(new KeyedCodec<>("ZOffsetRange", RelativeIntegerRange.CODEC), (op, val) -> op.zOffsetArg = val, op -> op.zOffsetArg)
      .documentation("The range of allowed values for the Y offset")
      .add()
      .documentation("Randomly offset the brush location from the clicked origin")
      .build();
   @Nonnull
   public RelativeIntegerRange xOffsetArg = new RelativeIntegerRange(1, 1);
   @Nonnull
   public RelativeIntegerRange yOffsetArg = new RelativeIntegerRange(1, 1);
   @Nonnull
   public RelativeIntegerRange zOffsetArg = new RelativeIntegerRange(1, 1);

   public RandomOffsetOperation() {
      super("Randomize Offset", "Randomly offset the brush location from the clicked origin", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Vector3i offset = new Vector3i(
         this.xOffsetArg.getNumberInRange(brushConfig.getOriginOffset().x),
         this.yOffsetArg.getNumberInRange(brushConfig.getOriginOffset().y),
         this.zOffsetArg.getNumberInRange(brushConfig.getOriginOffset().z)
      );
      brushConfig.setOriginOffset(offset);
   }
}
