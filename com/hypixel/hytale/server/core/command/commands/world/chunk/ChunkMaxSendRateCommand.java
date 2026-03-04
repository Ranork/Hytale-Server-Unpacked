package com.hypixel.hytale.server.core.command.commands.world.chunk;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkMaxSendRateCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private final OptionalArg<Integer> secArg = this.withOptionalArg("sec", "server.commands.chunk.maxsendrate.sec.desc", ArgTypes.INTEGER);
   @Nonnull
   private final OptionalArg<Integer> tickArg = this.withOptionalArg("tick", "server.commands.chunk.maxsendrate.tick.desc", ArgTypes.INTEGER);

   public ChunkMaxSendRateCommand() {
      super("maxsendrate", "server.commands.chunk.maxsendrate.desc");
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
      ChunkTracker chunkTracker = store.getComponent(ref, ChunkTracker.getComponentType());

      assert chunkTracker != null;

      if (this.secArg.provided(context)) {
         int sec = this.secArg.get(context);
         chunkTracker.setMaxChunksPerSecond(sec);
         context.sendMessage(Message.translation("server.commands.chunk.maxsendrate.sec.set").param("value", sec));
      }

      if (this.tickArg.provided(context)) {
         int tick = this.tickArg.get(context);
         chunkTracker.setMaxChunksPerTick(tick);
         context.sendMessage(Message.translation("server.commands.chunk.maxsendrate.tick.set").param("value", tick));
      }

      context.sendMessage(
         Message.translation("server.commands.chunk.maxsendrate.summary")
            .param("perSecond", chunkTracker.getMaxChunksPerSecond())
            .param("perTick", chunkTracker.getMaxChunksPerTick())
      );
   }
}
