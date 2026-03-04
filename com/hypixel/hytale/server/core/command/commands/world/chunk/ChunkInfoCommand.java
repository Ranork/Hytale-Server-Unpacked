package com.hypixel.hytale.server.core.command.commands.world.chunk;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeChunkPosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.EntityChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ChunkInfoCommand extends AbstractWorldCommand {
   @Nonnull
   private final RequiredArg<RelativeChunkPosition> chunkPosArg = this.withRequiredArg(
      "x z", "server.commands.chunk.info.position.desc", ArgTypes.RELATIVE_CHUNK_POSITION
   );

   public ChunkInfoCommand() {
      super("info", "server.commands.chunk.info.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      RelativeChunkPosition chunkPosition = this.chunkPosArg.get(context);
      Vector2i position = chunkPosition.getChunkPosition(context, store);
      ChunkStore chunkStore = world.getChunkStore();
      Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
      long chunkIndex = ChunkUtil.indexChunk(position.x, position.y);
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      if (chunkRef != null && chunkRef.isValid()) {
         WorldChunk worldChunkComponent = chunkStoreStore.getComponent(chunkRef, WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         BlockChunk blockChunkComponent = chunkStoreStore.getComponent(chunkRef, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         Message msg = Message.translation("server.commands.chunkinfo.chunk")
            .param("chunkX", position.x)
            .param("chunkZ", position.y)
            .param("startInit", worldChunkComponent.is(ChunkFlag.START_INIT))
            .param("init", worldChunkComponent.is(ChunkFlag.INIT))
            .param("newlyGenerated", worldChunkComponent.is(ChunkFlag.NEWLY_GENERATED))
            .param("onDisk", worldChunkComponent.is(ChunkFlag.ON_DISK))
            .param("ticking", worldChunkComponent.is(ChunkFlag.TICKING))
            .param("keepLoaded", worldChunkComponent.shouldKeepLoaded())
            .param("saving", worldChunkComponent.getNeedsSaving())
            .param("savingChunk", blockChunkComponent.getNeedsSaving());

         for (int i = 0; i < 10; i++) {
            BlockSection section = blockChunkComponent.getSectionAtIndex(i);
            msg.insert(Message.translation("server.commands.chunkinfo.section").param("index", i));
            if (section instanceof BlockSection) {
               msg.insert(Message.translation("server.commands.chunkinfo.dataType").param("data", section.getChunkSection().getClass().getSimpleName()));
            }

            msg.insert(
               Message.translation("server.commands.chunkinfo.sectionInfo")
                  .param("ticking", section.hasTicking())
                  .param("solidAir", section.isSolidAir())
                  .param("count", section.count())
                  .param("counts", section.valueCounts().toString())
            );
         }

         BlockComponentChunk blockStateChunk = chunkStoreStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
         EntityChunk entityChunk = chunkStoreStore.getComponent(chunkRef, EntityChunk.getComponentType());
         if (blockStateChunk != null && entityChunk != null) {
            msg.insert(
               Message.translation("server.commands.chunkinfo.blockStateChunk")
                  .param("saving", blockStateChunk.getNeedsSaving())
                  .param("countStates", blockStateChunk.getEntityHolders().size() + blockStateChunk.getEntityReferences().size())
                  .param("savingEntity", entityChunk.getNeedsSaving())
                  .param("countEntities", entityChunk.getEntityHolders().size() + entityChunk.getEntityReferences().size())
            );
         }

         context.sendMessage(msg);
      } else {
         context.sendMessage(Message.translation("server.general.chunkNotLoaded").param("chunkX", position.x).param("chunkZ", position.y));
         context.sendMessage(Message.translation("server.commands.chunkinfo.load.usage").param("chunkX", position.x).param("chunkZ", position.y));
      }
   }
}
