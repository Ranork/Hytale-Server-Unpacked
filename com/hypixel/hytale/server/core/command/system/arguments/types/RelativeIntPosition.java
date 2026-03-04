package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class RelativeIntPosition {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_RELATIVE_POSITION_ARG = Message.translation("server.commands.errors.relativePositionArg");
   @Nonnull
   private final IntCoord x;
   @Nonnull
   private final IntCoord y;
   @Nonnull
   private final IntCoord z;

   public RelativeIntPosition(@Nonnull IntCoord x, @Nonnull IntCoord y, @Nonnull IntCoord z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   @Nonnull
   public Vector3i getBlockPosition(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d base = transformComponent.getPosition();
      World world = componentAccessor.getExternalData().getWorld();
      return this.getBlockPosition(base, world.getChunkStore());
   }

   @Nonnull
   public Vector3i getBlockPosition(@Nonnull CommandContext context, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      boolean relative = this.isRelative();
      Ref<EntityStore> playerRef = context.isPlayer() ? context.senderAsPlayerRef() : null;
      if (playerRef != null) {
         if (!playerRef.isValid()) {
            throw new GeneralCommandException(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
         } else {
            return this.getBlockPosition(playerRef, componentAccessor);
         }
      } else if (relative) {
         throw new GeneralCommandException(MESSAGE_COMMANDS_ERRORS_RELATIVE_POSITION_ARG);
      } else {
         Vector3d base = Vector3d.ZERO;
         World world = componentAccessor.getExternalData().getWorld();
         return this.getBlockPosition(base, world.getChunkStore());
      }
   }

   @Nonnull
   public Vector3i getBlockPosition(@Nonnull Vector3d base, @Nonnull ChunkStore chunkStore) {
      int relX = this.x.resolveXZ(MathUtil.floor(base.x));
      int relZ = this.z.resolveXZ(MathUtil.floor(base.z));
      int relY = this.y.resolveYAtWorldCoords(MathUtil.floor(base.y), chunkStore, relX, relZ);
      return new Vector3i(relX, relY, relZ);
   }

   public boolean isRelative() {
      return this.x.isRelative() || this.y.isRelative() || this.z.isRelative();
   }
}
