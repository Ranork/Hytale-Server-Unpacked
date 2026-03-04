package com.hypixel.hytale.builtin.teleport.commands.teleport.variant;

import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.Coord;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeFloat;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TeleportToCoordinatesCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<Coord> xArg = this.withRequiredArg("x", "server.commands.teleport.x.desc", ArgTypes.RELATIVE_DOUBLE_COORD);
   @Nonnull
   private final RequiredArg<Coord> yArg = this.withRequiredArg("y", "server.commands.teleport.y.desc", ArgTypes.RELATIVE_DOUBLE_COORD);
   @Nonnull
   private final RequiredArg<Coord> zArg = this.withRequiredArg("z", "server.commands.teleport.z.desc", ArgTypes.RELATIVE_DOUBLE_COORD);
   @Nonnull
   private final OptionalArg<RelativeFloat> yawArg = this.withOptionalArg("yaw", "server.commands.teleport.yaw.desc", ArgTypes.RELATIVE_FLOAT);
   @Nonnull
   private final OptionalArg<RelativeFloat> pitchArg = this.withOptionalArg("pitch", "server.commands.teleport.pitch.desc", ArgTypes.RELATIVE_FLOAT);
   @Nonnull
   private final OptionalArg<RelativeFloat> rollArg = this.withOptionalArg("roll", "server.commands.teleport.roll.desc", ArgTypes.RELATIVE_FLOAT);

   public TeleportToCoordinatesCommand() {
      super("server.commands.teleport.toCoordinates.desc");
      this.requirePermission(HytalePermissions.fromCommand("teleport.self"));
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      Vector3d previousPos = transformComponent.getPosition().clone();
      Vector3f previousHeadRotation = headRotationComponent.getRotation().clone();
      Vector3f previousBodyRotation = transformComponent.getRotation().clone();
      Coord relX = this.xArg.get(context);
      Coord relY = this.yArg.get(context);
      Coord relZ = this.zArg.get(context);
      double x = relX.resolveXZ(previousPos.getX());
      double z = relZ.resolveXZ(previousPos.getZ());
      double y = relY.resolveYAtWorldCoords(previousPos.getY(), world, x, z);
      float yaw = this.yawArg.provided(context)
         ? this.yawArg.get(context).resolve(previousHeadRotation.getYaw() * (180.0F / (float)Math.PI)) * (float) (Math.PI / 180.0)
         : Float.NaN;
      float pitch = this.pitchArg.provided(context)
         ? this.pitchArg.get(context).resolve(previousHeadRotation.getPitch() * (180.0F / (float)Math.PI)) * (float) (Math.PI / 180.0)
         : Float.NaN;
      float roll = this.rollArg.provided(context)
         ? this.rollArg.get(context).resolve(previousHeadRotation.getRoll() * (180.0F / (float)Math.PI)) * (float) (Math.PI / 180.0)
         : Float.NaN;
      Teleport teleport = Teleport.createForPlayer(new Vector3d(x, y, z), new Vector3f(previousBodyRotation.getPitch(), yaw, previousBodyRotation.getRoll()))
         .setHeadRotation(new Vector3f(pitch, yaw, roll));
      store.addComponent(ref, Teleport.getComponentType(), teleport);
      boolean hasRotation = this.yawArg.provided(context) || this.pitchArg.provided(context) || this.rollArg.provided(context);
      if (hasRotation) {
         float displayYaw = Float.isNaN(yaw) ? previousHeadRotation.getYaw() * (180.0F / (float)Math.PI) : yaw * (180.0F / (float)Math.PI);
         float displayPitch = Float.isNaN(pitch) ? previousHeadRotation.getPitch() * (180.0F / (float)Math.PI) : pitch * (180.0F / (float)Math.PI);
         float displayRoll = Float.isNaN(roll) ? previousHeadRotation.getRoll() * (180.0F / (float)Math.PI) : roll * (180.0F / (float)Math.PI);
         context.sendMessage(
            Message.translation("server.commands.teleport.teleportedToCoordinatesWithLook")
               .param("x", x)
               .param("y", y)
               .param("z", z)
               .param("yaw", displayYaw)
               .param("pitch", displayPitch)
               .param("roll", displayRoll)
         );
      } else {
         context.sendMessage(Message.translation("server.commands.teleport.teleportedToCoordinates").param("x", x).param("y", y).param("z", z));
      }

      store.ensureAndGetComponent(ref, TeleportHistory.getComponentType())
         .append(world, previousPos, previousHeadRotation, String.format("Teleport to (%s, %s, %s)", x, y, z));
   }
}
