package com.hypixel.hytale.builtin.parkour.commands;

import com.hypixel.hytale.builtin.parkour.ParkourPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class CheckpointResetCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_CHECKPOINT_RESET_SUCCESS = Message.translation("server.commands.checkpoint.reset.success");

   public CheckpointResetCommand() {
      super("reset", "server.commands.checkpoint.reset.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      ParkourPlugin.get().resetPlayer(uuidComponent.getUuid());
      context.sendMessage(MESSAGE_COMMANDS_CHECKPOINT_RESET_SUCCESS);
   }
}
