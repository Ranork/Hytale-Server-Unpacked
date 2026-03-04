package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.commands;

import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.BrushOperationSetting;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.builtin.buildertools.tooloperations.ToolOperation;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class BrushConfigDebugStepCommand extends AbstractPlayerCommand {
   private final DefaultArg<Integer> numStepsArg = this.withDefaultArg(
         "steps", "server.commands.scriptedbrushes.step.steps.desc", ArgTypes.INTEGER, 1, "server.commands.scriptedbrushes.step.steps.default"
      )
      .addValidator(Validators.range(1, 100));

   public BrushConfigDebugStepCommand() {
      super("step", "server.commands.scriptedbrushes.step.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      int numSteps = this.numStepsArg.get(context);
      PrototypePlayerBuilderToolSettings prototypeSettings = ToolOperation.getOrCreatePrototypeSettings(uuidComponent.getUuid());
      BrushConfig brushConfig = prototypeSettings.getBrushConfig();
      BrushConfigCommandExecutor brushConfigCommandExecutor = prototypeSettings.getBrushConfigCommandExecutor();
      if (!brushConfig.isCurrentlyExecuting()) {
         playerRef.sendMessage(Message.translation("server.commands.brushConfig.debug.notStarted"));
      } else {
         int indexAtStart = brushConfigCommandExecutor.getCurrentOperationIndex();
         int indexAtEnd = 0;
         BrushConfig.BCExecutionStatus status = null;

         for (int i = 0; i < numSteps; i++) {
            indexAtEnd = brushConfigCommandExecutor.getCurrentOperationIndex();
            status = brushConfigCommandExecutor.step(ref, true, store);
            if (!status.equals(BrushConfig.BCExecutionStatus.Continue)) {
               break;
            }
         }

         if (status == BrushConfig.BCExecutionStatus.Complete) {
            playerRef.sendMessage(Message.translation("server.commands.brushConfig.debug.finished"));
         }

         Message header = Message.translation("server.commands.brushConfig.debug.executed");
         List<Message> values = new ObjectArrayList();

         for (int ix = indexAtStart; ix <= indexAtEnd; ix++) {
            SequenceBrushOperation brushOperation = brushConfigCommandExecutor.getSequentialOperations().get(ix);
            values.add(Message.translation("server.commands.brushConfig.list.sequentialOperation").param("index", ix).param("name", brushOperation.getName()));

            for (Entry<String, BrushOperationSetting<?>> entry : brushOperation.getRegisteredOperationSettings().entrySet()) {
               values.add(
                  Message.translation("server.commands.brushConfig.list.setting")
                     .param("name", entry.getKey())
                     .param("value", entry.getValue().getValueString())
               );
            }
         }

         playerRef.sendMessage(MessageFormat.list(header, values));
      }
   }
}
