package com.hypixel.hytale.server.core.command.commands.utility.sound;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.audio.PlaySoundPage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SoundCommand extends AbstractPlayerCommand {
   public SoundCommand() {
      super("sound", "server.commands.sound.desc");
      this.addSubCommand(new SoundPlay2DCommand());
      this.addSubCommand(new SoundPlay3DCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      playerComponent.getPageManager().openCustomPage(ref, store, new PlaySoundPage(playerRefComponent));
   }
}
