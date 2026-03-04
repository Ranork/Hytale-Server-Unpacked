package com.hypixel.hytale.server.core.command.system.basecommands;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public abstract class AbstractTargetEntityCommand extends AbstractAsyncCommand {
   private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
   private static final Message MESSAGE_GENERAL_NO_ENTITY_IN_VIEW = Message.translation("server.general.noEntityInView");
   @Nonnull
   private final OptionalArg<World> worldArg = this.withOptionalArg("world", "server.commands.worldthread.arg.desc", ArgTypes.WORLD);
   @Nonnull
   private final OptionalArg<Double> radiusArg = this.withOptionalArg("radius", "server.commands.entity.radius.desc", ArgTypes.DOUBLE)
      .addValidator(Validators.greaterThan(0.0));
   @Nonnull
   private final OptionalArg<PlayerRef> playerArg = this.withOptionalArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);
   @Nonnull
   private final EntityWrappedArg entityArg = this.withOptionalArg("entity", "server.commands.entity.entity.desc", ArgTypes.ENTITY_ID);

   public AbstractTargetEntityCommand(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   public AbstractTargetEntityCommand(@Nonnull String name, @Nonnull String description, boolean requiresConfirmation) {
      super(name, description, requiresConfirmation);
   }

   public AbstractTargetEntityCommand(@Nonnull String description) {
      super(description);
   }

   @Nonnull
   @Override
   protected final CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      World world;
      if (this.worldArg.provided(context)) {
         world = this.worldArg.get(context);
      } else {
         if (!context.isPlayer()) {
            context.sendMessage(Message.translation("server.commands.errors.playerOrArg").param("option", "world"));
            return CompletableFuture.completedFuture(null);
         }

         Ref<EntityStore> ref = context.senderAsPlayerRef();
         if (ref == null || !ref.isValid()) {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
            return CompletableFuture.completedFuture(null);
         }

         world = ref.getStore().getExternalData().getWorld();
      }

      Store<EntityStore> store = world.getEntityStore().getStore();
      return this.runAsync(context, () -> {
         ObjectList<Ref<EntityStore>> entitiesToOperateOn;
         if (this.radiusArg.provided(context)) {
            if (!context.isPlayer()) {
               context.sendMessage(Message.translation("server.commands.errors.playerOrArg").param("option", "radius"));
               return;
            }

            Ref<EntityStore> playerRef = context.senderAsPlayerRef();
            if (playerRef == null || !playerRef.isValid()) {
               context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
               return;
            }

            TransformComponent transformComponent = store.getComponent(playerRef, TransformComponent.getComponentType());
            if (transformComponent == null) {
               context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
               return;
            }

            double radius = this.radiusArg.get(context);
            Vector3d position = transformComponent.getPosition();
            entitiesToOperateOn = new ObjectArrayList();
            ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
            SpatialResource<Ref<EntityStore>, EntityStore> entitySpatialResource = store.getResource(EntityModule.get().getEntitySpatialResourceType());
            entitySpatialResource.getSpatialStructure().collect(position, radius, results);
            entitiesToOperateOn.addAll(results);
            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = store.getResource(EntityModule.get().getPlayerSpatialResourceType());
            playerSpatialResource.getSpatialStructure().collect(position, radius, results);
            entitiesToOperateOn.addAll(results);
         } else if (this.playerArg.provided(context)) {
            PlayerRef targetPlayerRef = this.playerArg.get(context);
            Ref<EntityStore> targetRef = targetPlayerRef.getReference();
            if (targetRef == null || !targetRef.isValid()) {
               context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
               return;
            }

            entitiesToOperateOn = ObjectLists.singleton(targetRef);
         } else if (this.entityArg.provided(context)) {
            Ref<EntityStore> entityRef = this.entityArg.get(store, context);
            if (entityRef == null || !entityRef.isValid()) {
               context.sendMessage(MESSAGE_GENERAL_NO_ENTITY_IN_VIEW);
               return;
            }

            entitiesToOperateOn = ObjectLists.singleton(entityRef);
         } else {
            if (!context.isPlayer()) {
               context.sendMessage(Message.translation("server.commands.errors.playerOrArg").param("option", "entity"));
               return;
            }

            Ref<EntityStore> playerRefx = context.senderAsPlayerRef();
            if (playerRefx == null || !playerRefx.isValid()) {
               context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
               return;
            }

            Ref<EntityStore> entityRef = TargetUtil.getTargetEntity(playerRefx, store);
            if (entityRef == null) {
               context.sendMessage(MESSAGE_GENERAL_NO_ENTITY_IN_VIEW);
               return;
            }

            entitiesToOperateOn = ObjectLists.singleton(entityRef);
         }

         this.execute(context, entitiesToOperateOn, world, store);
      }, world);
   }

   protected abstract void execute(
      @Nonnull CommandContext var1, @Nonnull ObjectList<Ref<EntityStore>> var2, @Nonnull World var3, @Nonnull Store<EntityStore> var4
   );
}
