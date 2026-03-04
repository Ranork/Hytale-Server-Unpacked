package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.dimensions;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeInteger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class DimensionsOperation extends SequenceBrushOperation {
   public static final BuilderCodec<DimensionsOperation> CODEC = BuilderCodec.builder(DimensionsOperation.class, DimensionsOperation::new)
      .append(new KeyedCodec<>("Width", RelativeInteger.CODEC), (op, val) -> op.widthArg = val, op -> op.widthArg)
      .documentation("Sets the width of the brush to the specified amount, optionally relative to the existing amount when using prefixing with tilde")
      .add()
      .<RelativeInteger>append(new KeyedCodec<>("Height", RelativeInteger.CODEC), (op, val) -> op.heightArg = val, op -> op.heightArg)
      .documentation("Sets the height of the brush to the specified amount, optionally relative to the existing amount when using prefixing with tilde")
      .add()
      .documentation("Set, add, or subtract from the dimensions of the brush area")
      .build();
   @Nonnull
   public RelativeInteger widthArg = new RelativeInteger(3, false);
   @Nonnull
   public RelativeInteger heightArg = new RelativeInteger(3, false);

   public DimensionsOperation() {
      super("Modify Dimensions", "Set, add, or subtract from the dimensions of the brush area", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      int width = this.widthArg.resolve(brushConfig.getShapeWidth());
      int height = this.heightArg.resolve(brushConfig.getShapeHeight());
      brushConfig.setShapeWidth(MathUtil.clamp(width, 1, 75));
      brushConfig.setShapeHeight(MathUtil.clamp(height, 1, 75));
   }
}
