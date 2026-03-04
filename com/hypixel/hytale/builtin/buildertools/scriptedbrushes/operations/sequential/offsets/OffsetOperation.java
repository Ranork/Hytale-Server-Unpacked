package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.offsets;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.LoadIntFromToolArgOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeVector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class OffsetOperation extends SequenceBrushOperation {
   public static final BuilderCodec<OffsetOperation> CODEC = BuilderCodec.builder(OffsetOperation.class, OffsetOperation::new)
      .append(new KeyedCodec<>("Offset", RelativeVector3i.CODEC), (op, val) -> op.offsetArg = val, op -> op.offsetArg)
      .documentation("Sets the offset in 3 dimensions, each value is optionally relative by prefixing it with a tilde")
      .add()
      .documentation("Offset the brush location by a specified amount from the clicked origin")
      .<LoadIntFromToolArgOperation.TargetField>append(
         new KeyedCodec<>("TargetField", new EnumCodec<>(LoadIntFromToolArgOperation.TargetField.class)),
         (op, val) -> op.targetFieldArg = val,
         op -> op.targetFieldArg
      )
      .documentation("The brush config field to set (Width, Height, Density, Thickness, OffsetX, OffsetY, OffsetZ)")
      .add()
      .<Boolean>append(new KeyedCodec<>("Negate", Codec.BOOLEAN, true), (op, val) -> op.negateArg = val, op -> op.negateArg)
      .documentation("Whether to invert the sign of the relative field")
      .add()
      .build();
   @Nonnull
   public RelativeVector3i offsetArg = RelativeVector3i.ZERO;
   @Nonnull
   public LoadIntFromToolArgOperation.TargetField targetFieldArg = LoadIntFromToolArgOperation.TargetField.None;
   public boolean negateArg = false;

   public OffsetOperation() {
      super("Modify Offset", "Offset the brush location by a specified amount from the clicked origin", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Vector3i offsetVector = this.offsetArg.resolve(brushConfig.getOriginOffset());
      if (this.targetFieldArg != LoadIntFromToolArgOperation.TargetField.None) {
         int relativeFieldValue = this.targetFieldArg.getValue(brushConfig);
         if (this.negateArg) {
            relativeFieldValue *= -1;
         }

         if (this.offsetArg.isRelativeX()) {
            offsetVector.setX(offsetVector.getX() + relativeFieldValue);
         }

         if (this.offsetArg.isRelativeY()) {
            offsetVector.setY(offsetVector.getY() + relativeFieldValue);
         }

         if (this.offsetArg.isRelativeZ()) {
            offsetVector.setZ(offsetVector.getZ() + relativeFieldValue);
         }
      }

      brushConfig.setOriginOffset(offsetVector);
   }
}
