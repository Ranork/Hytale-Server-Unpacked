package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.commands;

import com.hypixel.hytale.builtin.buildertools.tooloperations.ToolOperation;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class BrushConfigCommand extends AbstractCommandCollection {
   public BrushConfigCommand() {
      super("scriptedbrushes", "server.commands.scriptedbrushes.desc");
      this.addAliases("scriptbrush", "scriptedbrush", "sb");
      this.requirePermission("hytale.editor.brush.config");
      this.addSubCommand(new BrushConfigClearCommand());
      this.addSubCommand(new BrushConfigListCommand());
      this.addSubCommand(new BrushConfigDebugStepCommand());
      this.addSubCommand(new BrushConfigExitCommand());
      this.addSubCommand(new BrushConfigLoadCommand());
      this.addSubCommand(
         new AbstractPlayerCommand("info", "server.commands.scriptedbrushes.info.desc") {
            @Override
            protected void execute(
               @Nonnull CommandContext context,
               @Nonnull Store<EntityStore> store,
               @Nonnull Ref<EntityStore> ref,
               @Nonnull PlayerRef playerRef,
               @Nonnull World world
            ) {
               String infoString = ToolOperation.getOrCreatePrototypeSettings(playerRef.getUuid()).getBrushConfig().getInfo();
               context.sendMessage(Message.raw(infoString));
            }
         }
      );
   }
}
