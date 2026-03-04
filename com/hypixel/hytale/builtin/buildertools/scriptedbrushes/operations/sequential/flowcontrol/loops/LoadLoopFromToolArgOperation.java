package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.loops;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderTool;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import javax.annotation.Nonnull;

public class LoadLoopFromToolArgOperation extends SequenceBrushOperation {
   public static final int MAX_REPETITIONS = 100;
   public static final int IDLE_STATE = -1;
   public static final BuilderCodec<LoadLoopFromToolArgOperation> CODEC = BuilderCodec.builder(
         LoadLoopFromToolArgOperation.class, LoadLoopFromToolArgOperation::new
      )
      .append(new KeyedCodec<>("StoredIndexName", Codec.STRING), (op, val) -> op.indexNameArg = val, op -> op.indexNameArg)
      .documentation("The name of the previously stored index to begin the loop at. Note: This can only be an index previous to the current.")
      .add()
      .<String>append(new KeyedCodec<>("ArgName", Codec.STRING), (op, val) -> op.argNameArg = val, op -> op.argNameArg)
      .documentation("The amount of additional times to repeat the loop after the initial, normal execution")
      .add()
      .documentation("Loop the execution of instructions a set amount of times")
      .build();
   @Nonnull
   public String indexNameArg = "Undefined";
   @Nonnull
   public String argNameArg = "";
   private int repetitionsRemaining = -1;

   public LoadLoopFromToolArgOperation() {
      super("Loop Operations", "Loop the execution of instructions a variable amount of times", false);
   }

   @Override
   public void resetInternalState() {
      this.repetitionsRemaining = -1;
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
            brushConfig.setErrorFlag("LoadLoop: No active builder tool");
            return;
         }

         ItemStack itemStack = playerComponent.getInventory().getItemInHand();
         if (itemStack == null) {
            brushConfig.setErrorFlag("LoadLoop: No item in hand");
            return;
         }

         BuilderTool.ArgData argData = builderTool.getItemArgData(itemStack);
         Map<String, Object> toolArgs = argData.tool();
         if (toolArgs == null || !toolArgs.containsKey(this.argNameArg)) {
            brushConfig.setErrorFlag("LoadLoop: Tool arg '" + this.argNameArg + "' not found");
            return;
         }

         Object argValue = toolArgs.get(this.argNameArg);
         if (!(argValue instanceof Integer intValue)) {
            brushConfig.setErrorFlag("LoadLoop: Tool arg '" + this.argNameArg + "' is not an Int type (found " + argValue.getClass().getSimpleName() + ")");
            return;
         }

         if (intValue > 100 || intValue < 0) {
            brushConfig.setErrorFlag("Cannot have more than 100 repetitions, or negative repetitions");
            return;
         }

         this.repetitionsRemaining = intValue;
      }

      if (this.repetitionsRemaining == 0) {
         this.repetitionsRemaining = -1;
      } else {
         this.repetitionsRemaining--;
         brushConfigCommandExecutor.loadOperatingIndex(this.indexNameArg, false);
      }
   }
}
