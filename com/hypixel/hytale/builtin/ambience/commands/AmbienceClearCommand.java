package com.hypixel.hytale.builtin.ambience.commands;

import com.hypixel.hytale.builtin.ambience.resources.AmbienceResource;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class AmbienceClearCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_SERVER_COMMANDS_AMBIENCE_CLEAR_SUCCESS = Message.translation("server.commands.ambience.clear.success");

   public AmbienceClearCommand() {
      super("clear", "server.commands.ambience.clear.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      AmbienceResource ambienceResource = store.getResource(AmbienceResource.getResourceType());
      ambienceResource.setForcedMusicAmbience(null);
      context.sendMessage(MESSAGE_SERVER_COMMANDS_AMBIENCE_CLEAR_SUCCESS);
   }
}
