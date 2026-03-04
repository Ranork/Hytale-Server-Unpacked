package com.hypixel.hytale.server.core.command.commands.world.chunk;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.iterator.SpiralIterator;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkResendCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_CHUNK_RESEND_UNLOADED_ALL = Message.translation("server.commands.chunk.resend.unloadedAll");
   @Nonnull
   private final FlagArg clearCacheArg = this.withFlagArg("clearcache", "server.commands.chunk.resend.clearcache.desc");

   public ChunkResendCommand() {
      super("resend", "server.commands.chunk.resend.desc");
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
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      ChunkTracker chunkTrackerComponent = store.getComponent(ref, ChunkTracker.getComponentType());

      assert chunkTrackerComponent != null;

      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      int chunkX = MathUtil.floor(position.getX()) >> 5;
      int chunkZ = MathUtil.floor(position.getZ()) >> 5;
      if (this.clearCacheArg.provided(context)) {
         ChunkStore chunkStore = world.getChunkStore();
         Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
         SpiralIterator iterator = new SpiralIterator(chunkX, chunkZ, playerComponent.getViewRadius());

         while (iterator.hasNext()) {
            long chunkIndex = iterator.next();
            Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
            if (chunkRef != null && chunkRef.isValid()) {
               BlockChunk blockChunkComponent = chunkStoreStore.getComponent(chunkRef, BlockChunk.getComponentType());
               if (blockChunkComponent != null) {
                  for (int y = 0; y < 10; y++) {
                     blockChunkComponent.invalidateChunkSection(y);
                  }
               }
            }
         }
      }

      chunkTrackerComponent.unloadAll(playerRef);
      context.sendMessage(MESSAGE_COMMANDS_CHUNK_RESEND_UNLOADED_ALL);
   }
}
