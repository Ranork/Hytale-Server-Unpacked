package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.commands;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.BrushOperationSetting;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.GlobalBrushOperation;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.builtin.buildertools.tooloperations.ToolOperation;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
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

public class BrushConfigListCommand extends AbstractPlayerCommand {
   public BrushConfigListCommand() {
      super("list", "server.commands.scriptedbrushes.list.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      BrushConfigCommandExecutor brushConfigCommandExecutor = ToolOperation.getOrCreatePrototypeSettings(uuidComponent.getUuid())
         .getBrushConfigCommandExecutor();
      Message header = Message.translation("server.commands.brushConfig.list.globalOperation.header");
      List<Message> values = new ObjectArrayList();

      for (GlobalBrushOperation operation : brushConfigCommandExecutor.getGlobalOperations().values()) {
         values.add(Message.translation("server.commands.brushConfig.list.globalOperation").param("name", operation.getName()));

         for (Entry<String, BrushOperationSetting<?>> entry : operation.getRegisteredOperationSettings().entrySet()) {
            values.add(
               Message.translation("server.commands.brushConfig.list.setting").param("name", entry.getKey()).param("value", entry.getValue().getValueString())
            );
         }
      }

      playerRef.sendMessage(MessageFormat.list(header, values));
      header = Message.translation("server.commands.brushConfig.list.sequentialOperation.header");
      values = new ObjectArrayList();

      for (int i = 0; i < brushConfigCommandExecutor.getSequentialOperations().size(); i++) {
         SequenceBrushOperation operation = brushConfigCommandExecutor.getSequentialOperations().get(i);
         values.add(Message.translation("server.commands.brushConfig.list.sequentialOperation").param("index", i).param("name", operation.getName()));

         for (Entry<String, BrushOperationSetting<?>> entry : operation.getRegisteredOperationSettings().entrySet()) {
            values.add(
               Message.translation("server.commands.brushConfig.list.setting").param("name", entry.getKey()).param("value", entry.getValue().getValueString())
            );
         }
      }

      playerRef.sendMessage(MessageFormat.list(header, values));
   }
}
