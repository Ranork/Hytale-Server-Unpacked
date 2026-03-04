package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.buildertools.BrushShape;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ShapeOperation extends SequenceBrushOperation {
   public static final BuilderCodec<ShapeOperation> CODEC = BuilderCodec.builder(ShapeOperation.class, ShapeOperation::new)
      .append(new KeyedCodec<>("Shape", new EnumCodec<>(BrushShape.class)), (op, val) -> op.brushShapeArg = val, op -> op.brushShapeArg)
      .documentation("Changes the brush shape")
      .add()
      .documentation("Changes the shape of the brush editing area")
      .build();
   @Nonnull
   public BrushShape brushShapeArg = BrushShape.Sphere;

   public ShapeOperation() {
      super("Shape", "Changes the shape of the brush editing area", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      brushConfig.setShape(this.brushShapeArg);
   }
}
