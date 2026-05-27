package com.hypixel.hytale.builtin.audio.commands;

import com.hypixel.hytale.builtin.audio.components.ForcedMusicTracker;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.musiccontainer.config.MusicContainer;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayersCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import javax.annotation.Nonnull;

public class AudioMusicForceCommand extends AbstractTargetPlayersCommand {
   @Nonnull
   private final RequiredArg<MusicContainer> musicContainerArg = this.withRequiredArg(
      "musicContainerId", "server.commands.audio.music.force.arg.musiccontainerid.desc", ArgTypes.MUSIC_CONTAINER_ASSET
   );

   public AudioMusicForceCommand() {
      super("force", "server.commands.audio.music.force.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull List<Ref<EntityStore>> targets) {
      MusicContainer musicContainer = this.musicContainerArg.get(context);
      int containerIndex = MusicContainer.getAssetMap().getIndex(musicContainer.getId());
      int affected = 0;

      for (Ref<EntityStore> ref : targets) {
         ForcedMusicTracker tracker = store.getComponent(ref, ForcedMusicTracker.getComponentType());
         if (tracker != null) {
            tracker.setCurrentContainerIndex(containerIndex);
            affected++;
         }
      }

      context.sendMessage(Message.translation("server.commands.audio.music.force.success").param("music", musicContainer.getId()).param("count", affected));
   }
}
