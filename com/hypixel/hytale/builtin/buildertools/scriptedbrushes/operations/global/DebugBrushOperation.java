package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.global;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.GlobalBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class DebugBrushOperation extends GlobalBrushOperation {
   public static final BuilderCodec<DebugBrushOperation> CODEC = BuilderCodec.builder(DebugBrushOperation.class, DebugBrushOperation::new)
      .append(new KeyedCodec<>("PrintOperations", Codec.BOOLEAN), (op, val) -> op.printOperations = val, op -> op.printOperations)
      .documentation("Prints the index and name of each operation as it executes")
      .add()
      .<Boolean>append(new KeyedCodec<>("StepThrough", Codec.BOOLEAN), (op, val) -> op.stepThrough = val, op -> op.stepThrough)
      .documentation("Enables manual step-through mode (pause after each operation)")
      .add()
      .<Boolean>append(new KeyedCodec<>("EnableBreakpoints", Codec.BOOLEAN), (op, val) -> op.enableBreakpoints = val, op -> op.enableBreakpoints)
      .documentation("Master toggle for breakpoint operations")
      .add()
      .<BrushConfigCommandExecutor.DebugOutputTarget>append(
         new KeyedCodec<>("OutputTarget", new EnumCodec<>(BrushConfigCommandExecutor.DebugOutputTarget.class)),
         (op, val) -> op.outputTarget = val,
         op -> op.outputTarget
      )
      .documentation("Where debug messages are sent (Chat, Console, or Both)")
      .add()
      .<Boolean>append(new KeyedCodec<>("BreakOnError", Codec.BOOLEAN), (op, val) -> op.breakOnError = val, op -> op.breakOnError)
      .documentation("Pause on error instead of terminating execution")
      .add()
      .documentation("Debug options for scripted brushes")
      .build();
   @Nonnull
   private Boolean printOperations = false;
   @Nonnull
   private Boolean stepThrough = false;
   @Nonnull
   private Boolean enableBreakpoints = false;
   @Nonnull
   private BrushConfigCommandExecutor.DebugOutputTarget outputTarget = BrushConfigCommandExecutor.DebugOutputTarget.Chat;
   @Nonnull
   private Boolean breakOnError = false;

   public DebugBrushOperation() {
      super("Debug Step-Through", "Debug options for scripted brushes");
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      brushConfigCommandExecutor.setInDebugSteppingMode(this.stepThrough);
      brushConfigCommandExecutor.setPrintOperations(this.printOperations);
      brushConfigCommandExecutor.setEnableBreakpoints(this.enableBreakpoints);
      brushConfigCommandExecutor.setDebugOutputTarget(this.outputTarget);
      brushConfigCommandExecutor.setBreakOnError(this.breakOnError);
   }
}
