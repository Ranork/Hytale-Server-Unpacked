package com.hypixel.hytale.server.core.command.commands.player.camera;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerCameraResetCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_CAMERA_RESET_SUCCESS = Message.translation("server.commands.camera.reset.success");

   public PlayerCameraResetCommand() {
      super("reset", "server.commands.camera.reset.desc");
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
      playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, null));
      context.sendMessage(MESSAGE_COMMANDS_CAMERA_RESET_SUCCESS);
   }
}
