package com.hypixel.hytale.builtin.adventure.memories.commands;

import com.hypixel.hytale.builtin.adventure.memories.component.PlayerMemories;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.player.UpdateMemoriesFeatureStatus;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class MemoriesCapacityCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<Integer> capacityArg = this.withRequiredArg("capacity", "server.commands.memories.capacity.capacity.desc", ArgTypes.INTEGER);

   public MemoriesCapacityCommand() {
      super("capacity", "server.commands.memories.capacity.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Integer capacity = this.capacityArg.get(context);
      PacketHandler playerConnection = playerRef.getPacketHandler();
      if (capacity <= 0) {
         store.tryRemoveComponent(ref, PlayerMemories.getComponentType());
         context.sendMessage(Message.translation("server.commands.memories.capacity.success").param("capacity", 0));
         playerConnection.writeNoCache(new UpdateMemoriesFeatureStatus(false));
      } else {
         PlayerMemories playerMemories = store.ensureAndGetComponent(ref, PlayerMemories.getComponentType());
         playerMemories.setMemoriesCapacity(capacity);
         context.sendMessage(Message.translation("server.commands.memories.capacity.success").param("capacity", capacity));
         playerConnection.writeNoCache(new UpdateMemoriesFeatureStatus(true));
      }
   }
}
