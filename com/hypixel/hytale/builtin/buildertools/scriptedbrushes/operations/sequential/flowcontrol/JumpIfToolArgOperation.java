package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderTool;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import javax.annotation.Nonnull;

public class JumpIfToolArgOperation extends SequenceBrushOperation {
   public static final BuilderCodec<JumpIfToolArgOperation> CODEC = BuilderCodec.builder(JumpIfToolArgOperation.class, JumpIfToolArgOperation::new)
      .append(new KeyedCodec<>("ArgName", Codec.STRING), (op, val) -> op.argNameArg = val, op -> op.argNameArg)
      .documentation("The name of the tool arg to compare")
      .add()
      .<JumpIfToolArgOperation.ComparisonType>append(
         new KeyedCodec<>("ComparisonType", new EnumCodec<>(JumpIfToolArgOperation.ComparisonType.class)),
         (op, val) -> op.comparisonTypeArg = val,
         op -> op.comparisonTypeArg
      )
      .documentation("The type of comparison to perform")
      .add()
      .<String>append(new KeyedCodec<>("ComparisonValue", Codec.STRING), (op, val) -> op.comparisonValueArg = val, op -> op.comparisonValueArg)
      .documentation("The value to compare against (for boolean: 'true' or 'false', for string: the exact string or dropdown option)")
      .add()
      .<String>append(new KeyedCodec<>("StoredIndexName", Codec.STRING), (op, val) -> op.indexVariableNameArg = val, op -> op.indexVariableNameArg)
      .documentation("The labeled index to jump to, previous or future")
      .add()
      .documentation("Jump stack execution based on a builder tool argument comparison (supports checkbox/bool and dropdown/option types)")
      .build();
   @Nonnull
   public String argNameArg = "";
   @Nonnull
   public JumpIfToolArgOperation.ComparisonType comparisonTypeArg = JumpIfToolArgOperation.ComparisonType.Equals;
   @Nonnull
   public String comparisonValueArg = "";
   @Nonnull
   public String indexVariableNameArg = "Undefined";

   public JumpIfToolArgOperation() {
      super("Jump If Tool Arg", "Jump stack execution based on a builder tool argument comparison", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      BuilderTool builderTool = BuilderTool.getActiveBuilderTool(playerComponent);
      if (builderTool == null) {
         brushConfig.setErrorFlag("JumpIfToolArg: No active builder tool");
      } else {
         ItemStack itemStack = playerComponent.getInventory().getItemInHand();
         if (itemStack == null) {
            brushConfig.setErrorFlag("JumpIfToolArg: No item in hand");
         } else {
            BuilderTool.ArgData argData = builderTool.getItemArgData(itemStack);
            Map<String, Object> toolArgs = argData.tool();
            if (toolArgs != null && toolArgs.containsKey(this.argNameArg)) {
               Object argValue = toolArgs.get(this.argNameArg);
               boolean shouldJump = false;
               if (argValue instanceof Boolean) {
                  boolean boolValue = (Boolean)argValue;
                  boolean expectedValue = Boolean.parseBoolean(this.comparisonValueArg);
                  switch (this.comparisonTypeArg) {
                     case Equals:
                        shouldJump = boolValue == expectedValue;
                        break;
                     case NotEquals:
                        shouldJump = boolValue != expectedValue;
                  }
               } else if (argValue instanceof String stringValue) {
                  switch (this.comparisonTypeArg) {
                     case Equals:
                        shouldJump = stringValue.equalsIgnoreCase(this.comparisonValueArg);
                        break;
                     case NotEquals:
                        shouldJump = !stringValue.equalsIgnoreCase(this.comparisonValueArg);
                        break;
                     case Contains:
                        shouldJump = stringValue.toLowerCase().contains(this.comparisonValueArg.toLowerCase());
                  }
               } else {
                  String stringValue = argValue.toString();
                  switch (this.comparisonTypeArg) {
                     case Equals:
                        shouldJump = stringValue.equalsIgnoreCase(this.comparisonValueArg);
                        break;
                     case NotEquals:
                        shouldJump = !stringValue.equalsIgnoreCase(this.comparisonValueArg);
                        break;
                     case Contains:
                        shouldJump = stringValue.toLowerCase().contains(this.comparisonValueArg.toLowerCase());
                  }
               }

               if (shouldJump) {
                  brushConfigCommandExecutor.loadOperatingIndex(this.indexVariableNameArg);
               }
            } else {
               brushConfig.setErrorFlag("JumpIfToolArg: Tool arg '" + this.argNameArg + "' not found");
            }
         }
      }
   }

   public static enum ComparisonType {
      Equals,
      NotEquals,
      Contains;
   }
}
