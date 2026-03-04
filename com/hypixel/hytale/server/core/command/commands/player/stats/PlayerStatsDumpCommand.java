package com.hypixel.hytale.server.core.command.commands.player.stats;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.commands.world.entity.stats.EntityStatsDumpCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerStatsDumpCommand extends AbstractTargetPlayerCommand {
   public PlayerStatsDumpCommand() {
      super("dump", "server.commands.player.stats.dump.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      EntityStatsDumpCommand.dumpEntityStatsData(context, Collections.singletonList(playerRef.getReference()), store);
   }
}
