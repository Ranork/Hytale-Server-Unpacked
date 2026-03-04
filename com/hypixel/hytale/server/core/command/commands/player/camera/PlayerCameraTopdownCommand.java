package com.hypixel.hytale.server.core.command.commands.player.camera;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.MouseInputType;
import com.hypixel.hytale.protocol.MovementForceRotationType;
import com.hypixel.hytale.protocol.PositionDistanceOffsetType;
import com.hypixel.hytale.protocol.RotationType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerCameraTopdownCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_CAMERA_TOPDOWN_SUCCESS = Message.translation("server.commands.camera.topdown.success");

   public PlayerCameraTopdownCommand() {
      super("topdown", "server.commands.camera.topdown.desc");
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
      ServerCameraSettings cameraSettings = new ServerCameraSettings();
      cameraSettings.positionLerpSpeed = 0.2F;
      cameraSettings.rotationLerpSpeed = 0.2F;
      cameraSettings.distance = 20.0F;
      cameraSettings.displayCursor = true;
      cameraSettings.isFirstPerson = false;
      cameraSettings.movementForceRotationType = MovementForceRotationType.Custom;
      cameraSettings.eyeOffset = true;
      cameraSettings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;
      cameraSettings.rotationType = RotationType.Custom;
      cameraSettings.rotation = new Direction(0.0F, (float) (-Math.PI / 2), 0.0F);
      cameraSettings.mouseInputType = MouseInputType.LookAtPlane;
      cameraSettings.planeNormal = new Vector3f(0.0F, 1.0F, 0.0F);
      playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, true, cameraSettings));
      context.sendMessage(MESSAGE_COMMANDS_CAMERA_TOPDOWN_SUCCESS);
   }
}
