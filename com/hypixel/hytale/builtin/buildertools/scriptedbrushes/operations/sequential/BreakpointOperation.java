package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol.JumpIfCompareOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BreakpointOperation extends SequenceBrushOperation {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final BuilderCodec<BreakpointOperation> CODEC = BuilderCodec.builder(BreakpointOperation.class, BreakpointOperation::new)
      .append(new KeyedCodec<>("Label", Codec.STRING), (op, val) -> op.label = val, op -> op.label)
      .documentation("Identifier for this breakpoint")
      .add()
      .<Boolean>append(new KeyedCodec<>("PrintMessage", Codec.BOOLEAN), (op, val) -> op.printMessage = val, op -> op.printMessage)
      .documentation("Print a message when breakpoint is reached")
      .add()
      .<Boolean>append(new KeyedCodec<>("PrintState", Codec.BOOLEAN), (op, val) -> op.printState = val, op -> op.printState)
      .documentation("Print brush state when breakpoint is reached")
      .add()
      .<Boolean>append(new KeyedCodec<>("EnterStepMode", Codec.BOOLEAN), (op, val) -> op.enterStepMode = val, op -> op.enterStepMode)
      .documentation("Enter step-through mode (use /sb step to continue)")
      .add()
      .<JumpIfCompareOperation.BrushConfigIntegerComparison>append(
         new KeyedCodec<>("Condition", JumpIfCompareOperation.BrushConfigIntegerComparison.CODEC), (op, val) -> op.condition = val, op -> op.condition
      )
      .documentation("Optional condition - breakpoint only triggers if condition passes")
      .add()
      .documentation("Debug breakpoint for scripted brushes")
      .build();
   @Nonnull
   private String label = "";
   @Nonnull
   private Boolean printMessage = false;
   @Nonnull
   private Boolean printState = false;
   @Nonnull
   private Boolean enterStepMode = false;
   @Nullable
   private JumpIfCompareOperation.BrushConfigIntegerComparison condition = null;

   public BreakpointOperation() {
      super("Breakpoint", "Debug breakpoint for scripted brushes", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor executor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (executor.isEnableBreakpoints()) {
         if (this.condition == null || this.condition.apply(brushConfig)) {
            int currentIndex = executor.getCurrentOperationIndex();
            BrushConfigCommandExecutor.DebugOutputTarget outputTarget = executor.getDebugOutputTarget();
            PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());
            String labelDisplay = this.label.isEmpty() ? "unnamed" : this.label;
            boolean hasAnyOutput = this.printMessage || this.printState || this.enterStepMode;
            if (hasAnyOutput) {
               if (this.shouldSendToChat(outputTarget) && playerRefComponent != null) {
                  playerRefComponent.sendMessage(
                     Message.translation("server.builderTools.brushConfig.debug.breakpointReached").param("label", labelDisplay).param("index", currentIndex)
                  );
               }

               if (this.shouldSendToConsole(outputTarget)) {
                  LOGGER.at(Level.INFO).log("[Breakpoint] '%s' reached at operation #%d", labelDisplay, currentIndex);
               }
            }

            if (this.printState) {
               String stateInfo = brushConfig.getInfo();
               if (this.shouldSendToChat(outputTarget) && playerRefComponent != null) {
                  playerRefComponent.sendMessage(
                     Message.translation("server.builderTools.brushConfig.debug.breakpointState").param("index", currentIndex).param("state", stateInfo)
                  );
               }

               if (this.shouldSendToConsole(outputTarget)) {
                  LOGGER.at(Level.INFO).log("[Breakpoint] [Operation #%d] %s", currentIndex, stateInfo);
               }
            }

            if (this.enterStepMode) {
               if (this.shouldSendToChat(outputTarget) && playerRefComponent != null) {
                  playerRefComponent.sendMessage(
                     Message.translation("server.builderTools.brushConfig.debug.breakpointEnteringStepMode").param("label", labelDisplay)
                  );
               }

               if (this.shouldSendToConsole(outputTarget)) {
                  LOGGER.at(Level.INFO).log("[Breakpoint] '%s' - Entering step-through mode", labelDisplay);
               }

               executor.setInDebugSteppingMode(true);
            }
         }
      }
   }

   private boolean shouldSendToChat(BrushConfigCommandExecutor.DebugOutputTarget target) {
      return target == BrushConfigCommandExecutor.DebugOutputTarget.Chat || target == BrushConfigCommandExecutor.DebugOutputTarget.Both;
   }

   private boolean shouldSendToConsole(BrushConfigCommandExecutor.DebugOutputTarget target) {
      return target == BrushConfigCommandExecutor.DebugOutputTarget.Console || target == BrushConfigCommandExecutor.DebugOutputTarget.Both;
   }
}
