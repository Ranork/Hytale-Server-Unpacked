package com.hypixel.hytale.server.core.command.commands.utility.lighting;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.lighting.ChunkLightingManager;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

public class LightingInfoCommand extends AbstractWorldCommand {
   @Nonnull
   private final FlagArg detailFlag = this.withFlagArg("detail", "server.commands.lighting.info.detail.desc");

   public LightingInfoCommand() {
      super("info", "server.commands.lighting.info.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Store<ChunkStore> chunkStoreStore = world.getChunkStore().getStore();
      ChunkLightingManager chunkLighting = world.getChunkLighting();
      context.sendMessage(
         Message.translation("server.commands.lighting.info.summary")
            .param("queueSize", chunkLighting.getQueueSize())
            .param("lightCalculation", chunkLighting.getLightCalculation().getClass().getSimpleName())
      );
      if (this.detailFlag.get(context)) {
         AtomicInteger total = new AtomicInteger();
         AtomicInteger localLightCount = new AtomicInteger();
         AtomicInteger globalLightCount = new AtomicInteger();
         chunkStoreStore.forEachEntityParallel(WorldChunk.getComponentType(), (index, archetypeChunk, commandBuffer) -> {
            int hasLocalCount = 0;
            int hasGlobalCount = 0;
            BlockChunk blockChunkComponent = archetypeChunk.getComponent(index, BlockChunk.getComponentType());

            assert blockChunkComponent != null;

            for (int y = 0; y < 10; y++) {
               BlockSection section = blockChunkComponent.getSectionAtBlockY(y);
               if (section.hasLocalLight()) {
                  hasLocalCount++;
               }

               if (section.hasGlobalLight()) {
                  hasGlobalCount++;
               }
            }

            total.getAndAdd(10);
            localLightCount.getAndAdd(hasLocalCount);
            globalLightCount.getAndAdd(hasGlobalCount);
         });
         context.sendMessage(
            Message.translation("server.commands.lighting.info.chunkDetails")
               .param("totalChunkSections", total.get())
               .param("chunksWithLocalLight", localLightCount.get())
               .param("chunksWithGlobalLight", globalLightCount.get())
         );
      }
   }
}
