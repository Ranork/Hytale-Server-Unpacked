package com.hypixel.hytale.builtin.teleport.commands.teleport.variant;

import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
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
import org.joml.Vector3d;

public class TeleportToCoordinatesCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<Coord> xArg = this.withRequiredArg("x", "server.commands.teleport.x.desc", ArgTypes.RELATIVE_DOUBLE_COORD)
      .withSuggestionOverride(ArgTypes.RELATIVE_POSITION);
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

      Vector3d previousPos = new Vector3d(transformComponent.getPosition());
      Rotation3f previousHeadRotation = new Rotation3f(headRotationComponent.getRotation());
      Rotation3f previousBodyRotation = new Rotation3f(transformComponent.getRotation());
      Coord relX = this.xArg.get(context);
      Coord relY = this.yArg.get(context);
      Coord relZ = this.zArg.get(context);
      double x = relX.resolveXZ(previousPos.x());
      double z = relZ.resolveXZ(previousPos.z());
      double y = relY.resolveYAtWorldCoords(previousPos.y(), world, x, z);
      float yaw = this.yawArg.provided(context)
         ? this.yawArg.get(context).resolve(previousHeadRotation.yaw() * (180.0F / (float)Math.PI)) * (float) (Math.PI / 180.0)
         : Float.NaN;
      float pitch = this.pitchArg.provided(context)
         ? this.pitchArg.get(context).resolve(previousHeadRotation.pitch() * (180.0F / (float)Math.PI)) * (float) (Math.PI / 180.0)
         : Float.NaN;
      float roll = this.rollArg.provided(context)
         ? this.rollArg.get(context).resolve(previousHeadRotation.roll() * (180.0F / (float)Math.PI)) * (float) (Math.PI / 180.0)
         : Float.NaN;
      Teleport teleport = Teleport.createForPlayer(new Vector3d(x, y, z), new Rotation3f(previousBodyRotation.pitch(), yaw, previousBodyRotation.roll()))
         .setHeadRotation(new Rotation3f(pitch, yaw, roll));
      store.addComponent(ref, Teleport.getComponentType(), teleport);
      boolean hasRotation = this.yawArg.provided(context) || this.pitchArg.provided(context) || this.rollArg.provided(context);
      if (hasRotation) {
         float displayYaw = Float.isNaN(yaw) ? previousHeadRotation.yaw() * (180.0F / (float)Math.PI) : yaw * (180.0F / (float)Math.PI);
         float displayPitch = Float.isNaN(pitch) ? previousHeadRotation.pitch() * (180.0F / (float)Math.PI) : pitch * (180.0F / (float)Math.PI);
         float displayRoll = Float.isNaN(roll) ? previousHeadRotation.roll() * (180.0F / (float)Math.PI) : roll * (180.0F / (float)Math.PI);
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
