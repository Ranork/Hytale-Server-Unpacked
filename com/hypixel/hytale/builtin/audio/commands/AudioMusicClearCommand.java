package com.hypixel.hytale.builtin.audio.commands;

import com.hypixel.hytale.builtin.audio.components.ForcedMusicTracker;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayersCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import javax.annotation.Nonnull;

public class AudioMusicClearCommand extends AbstractTargetPlayersCommand {
   public AudioMusicClearCommand() {
      super("clear", "server.commands.audio.music.clear.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull List<Ref<EntityStore>> targets) {
      int affected = 0;

      for (Ref<EntityStore> ref : targets) {
         ForcedMusicTracker tracker = store.getComponent(ref, ForcedMusicTracker.getComponentType());
         if (tracker != null) {
            tracker.setCurrentContainerIndex(0);
            affected++;
         }
      }

      context.sendMessage(Message.translation("server.commands.audio.music.clear.success").param("count", affected));
   }
}
