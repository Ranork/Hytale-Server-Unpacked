package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.transforms;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.builtin.buildertools.tooloperations.transform.Rotate;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Rotation;
import com.hypixel.hytale.protocol.packets.buildertools.BrushAxis;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class RotateOperation extends SequenceBrushOperation {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final BuilderCodec<RotateOperation> CODEC = BuilderCodec.builder(RotateOperation.class, RotateOperation::new)
      .append(new KeyedCodec<>("RotationAxis", new EnumCodec<>(BrushAxis.class)), (op, val) -> op.rotationAxisArg = val, op -> op.rotationAxisArg)
      .add()
      .append(new KeyedCodec<>("RotationAngle", new EnumCodec<>(Rotation.class)), (op, val) -> op.rotationAngleArg = val, op -> op.rotationAngleArg)
      .add()
      .append(
         new KeyedCodec<>("RotationOrigin", new EnumCodec<>(RotateOperation.RotationOrigin.class)),
         (op, val) -> op.rotationOriginArg = val,
         op -> op.rotationOriginArg
      )
      .add()
      .documentation("Rotates the brush based on axis, angle, and origin")
      .build();
   @Nonnull
   public Rotation rotationAngleArg = Rotation.None;
   @Nonnull
   public BrushAxis rotationAxisArg = BrushAxis.None;
   @Nonnull
   public RotateOperation.RotationOrigin rotationOriginArg = RotateOperation.RotationOrigin.OffsetCenter;

   public RotateOperation() {
      super("Rotation", "Changes the orientation of the brush shape with a rotation", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.rotationOriginArg == RotateOperation.RotationOrigin.OffsetCenter) {
         brushConfig.setTransformOrigin(brushConfig.getOriginAfterOffset());
      } else if (this.rotationOriginArg == RotateOperation.RotationOrigin.ClickCenter) {
         brushConfig.setTransformOrigin(brushConfig.getExecutionOrigin());
      } else if (this.rotationOriginArg == RotateOperation.RotationOrigin.Player) {
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());
         if (transformComponent == null) {
            brushConfig.setErrorFlag("Could not get the player's position.");
            return;
         }

         Vector3d position = transformComponent.getPosition();
         brushConfig.setTransformOrigin(new Vector3i(MathUtil.floor(position.getX()), MathUtil.floor(position.getY()), MathUtil.floor(position.getZ())));
      }

      brushConfig.setTransform(Rotate.forAxisAndAngle(this.rotationAxisArg, this.rotationAngleArg));
   }

   public static enum RotationOrigin {
      OffsetCenter,
      ClickCenter,
      Player;
   }
}
