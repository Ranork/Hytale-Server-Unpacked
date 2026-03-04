package com.hypixel.hytale.server.core.command.commands.debug.component.hitboxcollision;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class HitboxCollisionRemoveCommand extends AbstractCommandCollection {
   private static final Message MESSAGE_COMMANDS_ERRORS_TARGET_NOT_IN_WORLD = Message.translation("server.commands.errors.targetNotInWorld");

   public HitboxCollisionRemoveCommand() {
      super("remove", "server.commands.hitboxcollision.remove.desc");
      this.addSubCommand(new HitboxCollisionRemoveCommand.HitboxCollisionRemoveEntityCommand());
      this.addSubCommand(new HitboxCollisionRemoveCommand.HitboxCollisionRemoveSelfCommand());
   }

   public static class HitboxCollisionRemoveEntityCommand extends AbstractWorldCommand {
      @Nonnull
      private final EntityWrappedArg entityArg = this.withRequiredArg("entity", "server.commands.hitboxcollision.remove.entityArg.desc", ArgTypes.ENTITY_ID);

      public HitboxCollisionRemoveEntityCommand() {
         super("entity", "server.commands.hitboxcollision.remove.entity.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         Ref<EntityStore> entityRef = this.entityArg.get(store, context);
         if (entityRef != null && entityRef.isValid()) {
            store.tryRemoveComponent(entityRef, HitboxCollision.getComponentType());
            context.sendMessage(Message.translation("server.commands.hitboxcollision.remove.success"));
         } else {
            context.sendMessage(HitboxCollisionRemoveCommand.MESSAGE_COMMANDS_ERRORS_TARGET_NOT_IN_WORLD);
         }
      }
   }

   public static class HitboxCollisionRemoveSelfCommand extends AbstractPlayerCommand {
      public HitboxCollisionRemoveSelfCommand() {
         super("self", "server.commands.hitboxcollision.remove.self.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         store.tryRemoveComponent(ref, HitboxCollision.getComponentType());
         context.sendMessage(Message.translation("server.commands.hitboxcollision.remove.success"));
      }
   }
}
