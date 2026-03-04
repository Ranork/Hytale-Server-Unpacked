package com.hypixel.hytale.server.core.command.commands.player.camera;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ClientCameraView;
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

public class PlayerCameraSideScrollerCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_CAMERA_SIDESCROLLER_SUCCESS = Message.translation("server.commands.camera.sidescroller.success");

   public PlayerCameraSideScrollerCommand() {
      super("sidescroller", "server.commands.camera.sidescroller.desc");
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
      cameraSettings.distance = 15.0F;
      cameraSettings.displayCursor = true;
      cameraSettings.isFirstPerson = false;
      cameraSettings.movementForceRotationType = MovementForceRotationType.Custom;
      cameraSettings.movementMultiplier = new Vector3f(1.0F, 1.0F, 0.0F);
      cameraSettings.eyeOffset = true;
      cameraSettings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;
      cameraSettings.rotationType = RotationType.Custom;
      cameraSettings.mouseInputType = MouseInputType.LookAtPlane;
      cameraSettings.planeNormal = new Vector3f(0.0F, 0.0F, 1.0F);
      playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, true, cameraSettings));
      context.sendMessage(MESSAGE_COMMANDS_CAMERA_SIDESCROLLER_SUCCESS);
   }
}
