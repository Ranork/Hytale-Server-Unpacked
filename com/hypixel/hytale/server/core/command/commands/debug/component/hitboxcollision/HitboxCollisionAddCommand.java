package com.hypixel.hytale.server.core.command.commands.debug.component.hitboxcollision;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HitboxCollisionAddCommand extends AbstractCommandCollection {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_TARGET_NOT_IN_WORLD = Message.translation("server.commands.errors.targetNotInWorld");

   public HitboxCollisionAddCommand() {
      super("add", "server.commands.hitboxcollision.add.desc");
      this.addSubCommand(new HitboxCollisionAddCommand.HitboxCollisionAddEntityCommand());
      this.addSubCommand(new HitboxCollisionAddCommand.HitboxCollisionAddSelfCommand());
   }

   public static class HitboxCollisionAddEntityCommand extends AbstractWorldCommand {
      @Nonnull
      public static final Message MESSAGE_COMMANDS_HIT_BOX_COLLISION_ADD_ALREADY_ADDED = Message.translation("server.commands.hitboxcollision.add.alreadyAdded");
      @Nonnull
      public static final Message MESSAGE_COMMANDS_HIT_BOX_COLLISION_ADD_SUCCESS = Message.translation("server.commands.hitboxcollision.add.success");
      @Nonnull
      private final RequiredArg<HitboxCollisionConfig> hitboxCollisionConfigArg = this.withRequiredArg(
         "hitboxCollisionConfig", "server.commands.hitboxcollision.add.hitboxCollisionConfig.desc", ArgTypes.HITBOX_COLLISION_CONFIG
      );
      @Nonnull
      private final EntityWrappedArg entityArg = this.withOptionalArg("entity", "server.commands.hitboxcollision.add.entityArg.desc", ArgTypes.ENTITY_ID);

      public HitboxCollisionAddEntityCommand() {
         super("entity", "server.commands.hitboxcollision.add.entity.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         Ref<EntityStore> entityRef = this.entityArg.get(store, context);
         if (entityRef != null && entityRef.isValid()) {
            HitboxCollisionConfig hitboxCollisionConfig = this.hitboxCollisionConfigArg.get(context);
            if (store.getArchetype(entityRef).contains(HitboxCollision.getComponentType())) {
               context.sendMessage(MESSAGE_COMMANDS_HIT_BOX_COLLISION_ADD_ALREADY_ADDED);
            } else {
               store.addComponent(entityRef, HitboxCollision.getComponentType(), new HitboxCollision(hitboxCollisionConfig));
               context.sendMessage(MESSAGE_COMMANDS_HIT_BOX_COLLISION_ADD_SUCCESS);
            }
         } else {
            context.sendMessage(HitboxCollisionAddCommand.MESSAGE_COMMANDS_ERRORS_TARGET_NOT_IN_WORLD);
         }
      }
   }

   public static class HitboxCollisionAddSelfCommand extends AbstractTargetPlayerCommand {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_HIT_BOX_COLLISION_ADD_ALREADY_ADDED = Message.translation(
         "server.commands.hitboxcollision.add.alreadyAdded"
      );
      @Nonnull
      private static final Message MESSAGE_COMMANDS_HIT_BOX_COLLISION_ADD_SUCCESS = Message.translation("server.commands.hitboxcollision.add.success");
      @Nonnull
      private final RequiredArg<HitboxCollisionConfig> hitboxCollisionConfigArg = this.withRequiredArg(
         "hitboxCollisionConfig", "server.commands.hitboxcollision.add.hitboxCollisionConfig.desc", ArgTypes.HITBOX_COLLISION_CONFIG
      );

      public HitboxCollisionAddSelfCommand() {
         super("self", "server.commands.hitboxcollision.add.self.desc");
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
         if (store.getArchetype(ref).contains(HitboxCollision.getComponentType())) {
            context.sendMessage(MESSAGE_COMMANDS_HIT_BOX_COLLISION_ADD_ALREADY_ADDED);
         } else {
            HitboxCollisionConfig hitboxCollisionConfig = this.hitboxCollisionConfigArg.get(context);
            store.addComponent(ref, HitboxCollision.getComponentType(), new HitboxCollision(hitboxCollisionConfig));
            context.sendMessage(MESSAGE_COMMANDS_HIT_BOX_COLLISION_ADD_SUCCESS);
         }
      }
   }
}
