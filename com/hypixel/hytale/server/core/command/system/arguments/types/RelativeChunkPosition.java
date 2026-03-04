package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class RelativeChunkPosition {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_WORLD_UNPSPECIFIED = Message.translation("server.commands.errors.worldUnspecified");
   private final IntCoord x;
   private final IntCoord z;

   public RelativeChunkPosition(@Nonnull IntCoord x, @Nonnull IntCoord z) {
      this.x = x;
      this.z = z;
   }

   @Nonnull
   public Vector2i getChunkPosition(@Nonnull CommandContext context, ComponentAccessor<EntityStore> componentAccessor) {
      Ref<EntityStore> playerRef = context.senderAsPlayerRef();
      if (playerRef == null) {
         throw new GeneralCommandException(MESSAGE_COMMANDS_ERRORS_WORLD_UNPSPECIFIED);
      } else if (!playerRef.isValid()) {
         throw new GeneralCommandException(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
      } else {
         TransformComponent transformComponent = componentAccessor.getComponent(playerRef, TransformComponent.getComponentType());

         assert transformComponent != null;

         return this.getChunkPosition(transformComponent.getPosition());
      }
   }

   @Nonnull
   public Vector2i getChunkPosition(@Nonnull Vector3d base) {
      int relX = this.x.isNotBase() ? this.x.getValue() : this.x.resolveXZ(MathUtil.floor(base.x)) >> 5;
      int relZ = this.z.isNotBase() ? this.z.getValue() : this.z.resolveXZ(MathUtil.floor(base.z)) >> 5;
      return new Vector2i(relX, relZ);
   }

   public boolean isRelative() {
      return this.x.isRelative() || this.z.isRelative();
   }
}
