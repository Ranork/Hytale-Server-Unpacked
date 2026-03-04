package com.hypixel.hytale.server.core.command.commands.utility.lighting;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.lighting.ChunkLightingManager;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class LightingInvalidateCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_INVALIDATED_LIGHTING = Message.translation("server.commands.invalidatedlighting");
   @Nonnull
   private final FlagArg oneFlag = this.withFlagArg("one", "server.commands.invalidatelighting.one.desc");

   public LightingInvalidateCommand() {
      super("invalidate", "server.commands.invalidatelighting.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      ChunkLightingManager chunkLighting = world.getChunkLighting();
      ChunkStore chunkStore = world.getChunkStore();
      if (this.oneFlag.get(context)) {
         if (!context.isPlayer()) {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
            return;
         }

         Ref<EntityStore> ref = context.senderAsPlayerRef();
         if (ref == null || !ref.isValid()) {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
            return;
         }

         Store<EntityStore> entityStore = ref.getStore();
         TransformComponent transformComponent = entityStore.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         long chunkIndex = ChunkUtil.indexChunkFromBlock((int)position.getX(), (int)position.getZ());
         int chunkX = ChunkUtil.xOfChunkIndex(chunkIndex);
         int chunkZ = ChunkUtil.zOfChunkIndex(chunkIndex);
         Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
         if (chunkReference == null || !chunkReference.isValid()) {
            Message errorMessage = Message.translation("server.commands.errors.chunkNotLoaded")
               .param("chunkX", chunkX)
               .param("chunkZ", chunkZ)
               .param("world", world.getName());
            context.sendMessage(errorMessage);
            return;
         }

         Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
         BlockChunk blockChunkComponent = chunkStoreStore.getComponent(chunkReference, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         int chunkY = MathUtil.floor(position.getY()) >> 5;
         BlockSection section = blockChunkComponent.getSectionAtBlockY(chunkY);
         section.invalidateLocalLight();
         blockChunkComponent.invalidateChunkSection(chunkY);
         Vector3i chunkPosition = new Vector3i(blockChunkComponent.getX(), chunkY, blockChunkComponent.getZ());
         chunkLighting.addToQueue(chunkPosition);
         context.sendMessage(Message.translation("server.commands.invalidatelighting.success").param("chunkPosition", chunkPosition.toString()));
      } else {
         chunkLighting.invalidateLoadedChunks();
         context.sendMessage(MESSAGE_COMMANDS_INVALIDATED_LIGHTING);
      }
   }
}
