package com.hypixel.hytale.server.core.command.commands.player.stats;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.commands.world.entity.stats.EntityStatsGetCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerStatsGetCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private final RequiredArg<String> entityStatNameArg = this.withRequiredArg("statName", "server.commands.player.stats.get.statName.desc", ArgTypes.STRING);

   public PlayerStatsGetCommand() {
      super("get", "server.commands.player.stats.get.desc");
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
      String entityStat = this.entityStatNameArg.get(context);
      EntityStatsGetCommand.getEntityStat(context, Collections.singletonList(playerRef.getReference()), entityStat, store);
   }
}
