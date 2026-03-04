package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class RelativeDoublePosition {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_RELATIVE_POSITION_ARG = Message.translation("server.commands.errors.relativePositionArg");
   private final Coord x;
   private final Coord y;
   private final Coord z;

   public RelativeDoublePosition(@Nonnull Coord x, @Nonnull Coord y, @Nonnull Coord z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   @Nonnull
   public Vector3d getRelativePosition(@Nonnull Vector3d base, @Nonnull World world) {
      double relX = this.x.resolveXZ(base.x);
      double relZ = this.z.resolveXZ(base.z);
      double relY = this.y.resolveYAtWorldCoords(base.y, world, relX, relZ);
      return new Vector3d(relX, relY, relZ);
   }

   @Nonnull
   public Vector3d getRelativePosition(@Nonnull CommandContext context, @Nonnull World world, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      boolean relative = this.isRelative();
      if (relative && !context.isPlayer()) {
         throw new GeneralCommandException(MESSAGE_COMMANDS_ERRORS_RELATIVE_POSITION_ARG);
      } else {
         Vector3d basePosition;
         if (relative) {
            Ref<EntityStore> senderPlayerRef = context.senderAsPlayerRef();
            if (senderPlayerRef == null || !senderPlayerRef.isValid()) {
               throw new GeneralCommandException(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
            }

            TransformComponent transformComponent = componentAccessor.getComponent(senderPlayerRef, TransformComponent.getComponentType());

            assert transformComponent != null;

            basePosition = transformComponent.getPosition();
         } else {
            basePosition = Vector3d.ZERO;
         }

         return this.getRelativePosition(basePosition, world);
      }
   }

   public boolean isRelative() {
      return this.x.isRelative() || this.y.isRelative() || this.z.isRelative();
   }
}
