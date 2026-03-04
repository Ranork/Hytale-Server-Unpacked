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
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderTool;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class CircleOffsetFromArgOperation extends SequenceBrushOperation {
   public static final int MAX_REPETITIONS = 100;
   public static final int IDLE_STATE = -1;
   public static final double TWO_PI = Math.PI * 2;
   public static final BuilderCodec<CircleOffsetFromArgOperation> CODEC = BuilderCodec.builder(
         CircleOffsetFromArgOperation.class, CircleOffsetFromArgOperation::new
      )
      .append(new KeyedCodec<>("StoredIndexName", Codec.STRING), (op, val) -> op.indexNameArg = val, op -> op.indexNameArg)
      .documentation("The name of the previously stored index to begin the loop at. Note: This can only be an index previous to the current.")
      .add()
      .<String>append(new KeyedCodec<>("NumberCirclePointsArg", Codec.STRING, true), (op, val) -> op.numCirclePointsArg = val, op -> op.numCirclePointsArg)
      .documentation("The name of the Int tool arg to load the value from")
      .add()
      .<String>append(new KeyedCodec<>("CircleRadiusArg", Codec.STRING, true), (op, val) -> op.circleRadiusArg = val, op -> op.circleRadiusArg)
      .documentation("The name of the Int tool arg to load the value from")
      .add()
      .<Boolean>append(new KeyedCodec<>("FlipDirection", Codec.BOOLEAN, true), (op, val) -> op.flipArg = val, op -> op.flipArg)
      .documentation("Whether to invert the direction of the circle. Useful for non-zero offset modifiers.")
      .add()
      .<Boolean>append(new KeyedCodec<>("RotateDirection", Codec.BOOLEAN, true), (op, val) -> op.rotateArg = val, op -> op.rotateArg)
      .documentation("Whether to invert the direction of the circle. Useful for non-zero offset modifiers.")
      .add()
      .build();
   @Nonnull
   public String indexNameArg = "Undefined";
   public String numCirclePointsArg = "";
   private int numCirclePointsArgVal = 3;
   public String circleRadiusArg = "";
   private int circleRadiusArgVal = 5;
   private int previousCirclePointsVal = 3;
   private int previousCircleRadiusVal = 5;
   public boolean flipArg = false;
   public boolean rotateArg = false;
   private int repetitionsRemaining = -1;
   @Nonnull
   private List<Vector3i> offsetsInCircle = new ObjectArrayList();
   @Nonnull
   private Vector3i offsetWhenFirstReachedOperation = Vector3i.ZERO;
   @Nonnull
   private Vector3i previousCircleOffset = Vector3i.ZERO;

   public CircleOffsetFromArgOperation() {
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
      int numPointsOnCircle = this.numCirclePointsArgVal;
      int circleRadius = this.circleRadiusArgVal;
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
         Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         BuilderTool builderTool = BuilderTool.getActiveBuilderTool(playerComponent);
         if (builderTool == null) {
            brushConfig.setErrorFlag("CircleOffsetFromArg: No active builder tool");
            return;
         }

         ItemStack itemStack = playerComponent.getInventory().getItemInHand();
         if (itemStack == null) {
            brushConfig.setErrorFlag("CircleOffsetFromArg: No item in hand");
            return;
         }

         BuilderTool.ArgData argData = builderTool.getItemArgData(itemStack);
         Map<String, Object> toolArgs = argData.tool();
         if (toolArgs == null || !toolArgs.containsKey(this.numCirclePointsArg)) {
            brushConfig.setErrorFlag("CircleOffsetFromArg: Tool arg '" + this.numCirclePointsArg + "' not found");
            return;
         }

         if (toolArgs == null || !toolArgs.containsKey(this.numCirclePointsArg)) {
            brushConfig.setErrorFlag("CircleOffsetFromArg: Tool arg '" + this.numCirclePointsArg + "' not found");
            return;
         }

         Object numCirclePointsArgValue = toolArgs.get(this.numCirclePointsArg);
         Object circleRadiusArgValue = toolArgs.get(this.circleRadiusArg);
         if (!(numCirclePointsArgValue instanceof Integer)) {
            brushConfig.setErrorFlag(
               "LoadCircleLoop: Tool arg '"
                  + this.numCirclePointsArg
                  + "' is not an Int type (found "
                  + numCirclePointsArgValue.getClass().getSimpleName()
                  + ")"
            );
            return;
         }

         if (!(circleRadiusArgValue instanceof Integer)) {
            brushConfig.setErrorFlag(
               "LoadCircleLoop: Tool arg '" + this.circleRadiusArg + "' is not an Int type (found " + circleRadiusArgValue.getClass().getSimpleName() + ")"
            );
            return;
         }

         this.numCirclePointsArgVal = (Integer)numCirclePointsArgValue;
         this.circleRadiusArgVal = (Integer)circleRadiusArgValue;
         this.previousCirclePointsVal = this.numCirclePointsArgVal;
         this.previousCircleRadiusVal = this.circleRadiusArgVal;
         this.resetInternalState();
         this.offsetWhenFirstReachedOperation = brushConfig.getOriginOffset();
         if (this.numCirclePointsArgVal > 100) {
            brushConfig.setErrorFlag("Cannot have more than 100 repetitions");
            return;
         }

         this.repetitionsRemaining = this.numCirclePointsArgVal;
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
