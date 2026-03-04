package com.hypixel.hytale.builtin.parkour.commands;

import com.hypixel.hytale.builtin.parkour.ParkourPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.UUID;
import javax.annotation.Nonnull;

public class CheckpointRemoveCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_CHECKPOINT_REMOVE_FAILED = Message.translation("server.commands.checkpoint.remove.failed");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_CHECKPOINT_REMOVE_SUCCESS = Message.translation("server.commands.checkpoint.remove.success");
   @Nonnull
   private final RequiredArg<Integer> indexArg = this.withRequiredArg("index", "server.commands.checkpoint.remove.index.desc", ArgTypes.INTEGER);

   public CheckpointRemoveCommand() {
      super("remove", "server.commands.checkpoint.remove.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      int index = this.indexArg.get(context);
      Int2ObjectMap<UUID> checkpointUUIDMap = ParkourPlugin.get().getCheckpointUUIDMap();
      UUID uuid = (UUID)checkpointUUIDMap.get(index);
      if (uuid == null) {
         context.sendMessage(MESSAGE_COMMANDS_CHECKPOINT_REMOVE_FAILED);
      } else {
         Ref<EntityStore> ref = store.getExternalData().getRefFromUUID(uuid);
         if (ref != null && ref.isValid()) {
            store.removeEntity(ref, RemoveReason.REMOVE);
            checkpointUUIDMap.remove(index);
            ParkourPlugin.get().updateLastIndex();
            context.sendMessage(MESSAGE_COMMANDS_CHECKPOINT_REMOVE_SUCCESS);
         } else {
            context.sendMessage(MESSAGE_COMMANDS_CHECKPOINT_REMOVE_FAILED);
         }
      }
   }
}
