package com.hypixel.hytale.builtin.mounts.commands;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MountCheckCommand extends AbstractTargetPlayerCommand {
   private static final Message MESSAGE_COMMANDS_CHECK_NO_COMPONENT = Message.translation("server.commands.check.noComponent");
   private static final Message MESSAGE_COMMANDS_CHECK_MOUNTED_TO_ENTITY = Message.translation("server.commands.check.mountedToEntity");
   private static final Message MESSAGE_COMMANDS_CHECK_MOUNTED_TO_BLOCK = Message.translation("server.commands.check.mountedToBlock");
   private static final Message MESSAGE_COMMANDS_CHECK_UNKNOWN_STATUS = Message.translation("server.commands.check.unknownStatus");

   public MountCheckCommand() {
      super("check", "server.commands.check.desc");
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
      MountedComponent mountedComponent = store.getComponent(ref, MountedComponent.getComponentType());
      if (mountedComponent == null) {
         playerRef.sendMessage(MESSAGE_COMMANDS_CHECK_NO_COMPONENT);
      } else {
         if (mountedComponent.getMountedToEntity() != null) {
            playerRef.sendMessage(MESSAGE_COMMANDS_CHECK_MOUNTED_TO_ENTITY);
         } else if (mountedComponent.getMountedToBlock() != null) {
            playerRef.sendMessage(MESSAGE_COMMANDS_CHECK_MOUNTED_TO_BLOCK);
         } else {
            playerRef.sendMessage(MESSAGE_COMMANDS_CHECK_UNKNOWN_STATUS);
         }
      }
   }
}
