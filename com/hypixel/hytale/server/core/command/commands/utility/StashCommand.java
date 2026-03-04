package com.hypixel.hytale.server.core.command.commands.utility;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.util.TargetUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StashCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_STASH_DROP_LIST_SET = Message.translation("server.commands.stash.droplistSet");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_STASH_NO_DROP_LIST = Message.translation("server.commands.stash.noDroplist");
   @Nonnull
   private static final Message MESSAGE_GENERAL_BLOCK_TARGET_NOT_IN_RANGE = Message.translation("server.general.blockTargetNotInRange");
   private static final int DISTANCE_MAX = 10;
   @Nonnull
   private final OptionalArg<String> setArg = this.withOptionalArg("set", "server.commands.stash.setDroplist.desc", ArgTypes.STRING);

   public StashCommand() {
      super("stash", "server.commands.stash.getDroplist.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      ItemContainerState itemContainerState = this.getItemContainerState(ref, world, context, store);
      if (itemContainerState != null) {
         if (this.setArg.provided(context)) {
            String dropList = this.setArg.get(context);
            itemContainerState.setDroplist(dropList);
            context.sendMessage(MESSAGE_COMMANDS_STASH_DROP_LIST_SET);
         } else {
            String droplist = itemContainerState.getDroplist();
            if (droplist != null) {
               context.sendMessage(Message.translation("server.commands.stash.currentDroplist").param("droplist", droplist));
            } else {
               context.sendMessage(MESSAGE_COMMANDS_STASH_NO_DROP_LIST);
            }
         }
      }
   }

   @Nullable
   private ItemContainerState getItemContainerState(
      @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull CommandContext context, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Vector3i block = TargetUtil.getTargetBlock(ref, 10.0, componentAccessor);
      if (block == null) {
         context.sendMessage(MESSAGE_GENERAL_BLOCK_TARGET_NOT_IN_RANGE);
         return null;
      } else {
         ChunkStore chunkStore = world.getChunkStore();
         long chunkIndex = ChunkUtil.indexChunkFromBlock(block.x, block.z);
         Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
         if (chunkRef != null && chunkRef.isValid()) {
            Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
            BlockChunk blockChunkComponent = chunkStoreStore.getComponent(chunkRef, BlockChunk.getComponentType());

            assert blockChunkComponent != null;

            WorldChunk worldChunkComponent = chunkStoreStore.getComponent(chunkRef, WorldChunk.getComponentType());

            assert worldChunkComponent != null;

            BlockSection section = blockChunkComponent.getSectionAtBlockY(block.y);
            int filler = section.getFiller(block.x, block.y, block.z);
            if (filler != 0) {
               block.x = block.x - FillerBlockUtil.unpackX(filler);
               block.y = block.y - FillerBlockUtil.unpackY(filler);
               block.z = block.z - FillerBlockUtil.unpackZ(filler);
            }

            BlockState state = worldChunkComponent.getState(block.x, block.y, block.z);
            if (!(state instanceof ItemContainerState)) {
               context.sendMessage(Message.translation("server.general.containerNotFound").param("block", block.toString()));
               return null;
            } else {
               return (ItemContainerState)state;
            }
         } else {
            int chunkX = ChunkUtil.chunkCoordinate(block.x);
            int chunkZ = ChunkUtil.chunkCoordinate(block.z);
            context.sendMessage(
               Message.translation("server.commands.errors.chunkNotLoaded").param("chunkX", chunkX).param("chunkZ", chunkZ).param("world", world.getName())
            );
            return null;
         }
      }
   }
}
