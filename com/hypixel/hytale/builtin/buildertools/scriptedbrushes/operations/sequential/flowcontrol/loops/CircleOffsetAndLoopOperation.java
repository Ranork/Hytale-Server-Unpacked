package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.loops;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class CircleOffsetAndLoopOperation extends SequenceBrushOperation {
   public static final int MAX_REPETITIONS = 100;
   public static final int IDLE_STATE = -1;
   public static final double TWO_PI = Math.PI * 2;
   public static final BuilderCodec<CircleOffsetAndLoopOperation> CODEC = BuilderCodec.builder(
         CircleOffsetAndLoopOperation.class, CircleOffsetAndLoopOperation::new
      )
      .append(new KeyedCodec<>("StoredIndexName", Codec.STRING), (op, val) -> op.indexNameArg = val, op -> op.indexNameArg)
      .documentation("The name of the previously stored index to begin the loop at. Note: This can only be an index previous to the current.")
      .add()
      .<Integer>append(new KeyedCodec<>("NumberOfCirclePoints", Codec.INTEGER), (op, val) -> op.numberOfCirclePointsArg = val, op -> op.numberOfCirclePointsArg)
      .documentation("The amount of equidistant points on the circle to loop at")
      .add()
      .<Integer>append(new KeyedCodec<>("CircleRadius", Codec.INTEGER), (op, val) -> op.circleRadiusArg = val, op -> op.circleRadiusArg)
      .documentation("The radius of the circle")
      .add()
      .<Boolean>append(new KeyedCodec<>("FlipDirection", Codec.BOOLEAN, true), (op, val) -> op.flipArg = val, op -> op.flipArg)
      .documentation("Whether to invert the direction of the circle. Useful for non-zero offset modifiers.")
      .add()
      .<Boolean>append(new KeyedCodec<>("RotateDirection", Codec.BOOLEAN, true), (op, val) -> op.rotateArg = val, op -> op.rotateArg)
      .documentation("Whether to invert the direction of the circle. Useful for non-zero offset modifiers.")
      .add()
      .documentation("Loops specified instructions and changes the offset after each loop in order to execute around a circle")
      .build();
   @Nonnull
   public String indexNameArg = "Undefined";
   @Nonnull
   public Integer numberOfCirclePointsArg = 3;
   @Nonnull
   public Integer circleRadiusArg = 5;
   public boolean flipArg = false;
   public boolean rotateArg = false;
   private int repetitionsRemaining = -1;
   @Nonnull
   private List<Vector3i> offsetsInCircle = new ObjectArrayList();
   @Nonnull
   private Vector3i offsetWhenFirstReachedOperation = Vector3i.ZERO;
   @Nonnull
   private Vector3i previousCircleOffset = Vector3i.ZERO;

   public CircleOffsetAndLoopOperation() {
      super(
         "Loop Previous And Set Offset In Circle",
         "Loops specified instructions and changes the offset after each loop in order to execute around a circle",
         false
      );
   }

   @Override
   public void resetInternalState() {
      this.repetitionsRemaining = -1;
      this.offsetsInCircle.clear();
      this.offsetWhenFirstReachedOperation = Vector3i.ZERO;
      this.previousCircleOffset = Vector3i.ZERO;
      int numPointsOnCircle = this.numberOfCirclePointsArg;
      int circleRadius = this.circleRadiusArg;
      double theta = (Math.PI * 2) / numPointsOnCircle;

      for (int i = 0; i < numPointsOnCircle; i++) {
         if (this.rotateArg) {
            this.offsetsInCircle
               .add(
                  new Vector3i(
                     this.doubleToNearestInt(circleRadius * Math.cos(theta * i)) * -1, 0, this.doubleToNearestInt(circleRadius * Math.sin(theta * i)) * -1
                  )
               );
         } else {
            this.offsetsInCircle
               .add(new Vector3i(this.doubleToNearestInt(circleRadius * Math.cos(theta * i)), 0, this.doubleToNearestInt(circleRadius * Math.sin(theta * i))));
         }
      }

      if (this.flipArg) {
         this.offsetsInCircle = this.offsetsInCircle.reversed();
      }
   }

   private int doubleToNearestInt(double number) {
      return (int)Math.floor(number + 0.5);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.repetitionsRemaining == -1) {
         this.resetInternalState();
         this.offsetWhenFirstReachedOperation = brushConfig.getOriginOffset();
         if (this.numberOfCirclePointsArg > 100) {
            brushConfig.setErrorFlag("Cannot have more than 100 repetitions");
            return;
         }

         this.repetitionsRemaining = this.numberOfCirclePointsArg;
      }

      if (this.repetitionsRemaining == 0) {
         this.repetitionsRemaining = -1;
         brushConfig.setOriginOffset(this.offsetWhenFirstReachedOperation);
      } else {
         Vector3i offsetVector = brushConfig.getOriginOffset()
            .subtract(this.previousCircleOffset)
            .add(this.offsetsInCircle.get(this.repetitionsRemaining - 1).clone());
         this.previousCircleOffset = this.offsetsInCircle.get(this.repetitionsRemaining - 1).clone();
         brushConfig.setOriginOffset(offsetVector);
         brushConfigCommandExecutor.loadOperatingIndex(this.indexNameArg, false);
         this.repetitionsRemaining--;
      }
   }
}
