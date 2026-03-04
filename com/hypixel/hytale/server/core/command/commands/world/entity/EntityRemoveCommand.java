package com.hypixel.hytale.server.core.command.commands.world.entity;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityRemoveCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_GENERAL_NO_ENTITY_IN_VIEW = Message.translation("server.general.noEntityInView");
   @Nonnull
   private final EntityWrappedArg entityArg = this.withOptionalArg("entity", "server.commands.entity.remove.entity.desc", ArgTypes.ENTITY_ID);
   @Nonnull
   private final FlagArg othersFlag = this.withFlagArg("others", "server.commands.entity.remove.others.desc");

   public EntityRemoveCommand() {
      super("remove", "server.commands.entity.remove.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Ref<EntityStore> entityRef = this.entityArg.get(store, context);
      if (entityRef != null && entityRef.isValid()) {
         if (this.othersFlag.provided(context)) {
            store.forEachEntityParallel((index, archetypeChunk, commandBuffer) -> {
               if (!archetypeChunk.getArchetype().contains(Player.getComponentType())) {
                  if (!archetypeChunk.getReferenceTo(index).equals(entityRef)) {
                     commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
                  }
               }
            });
         } else {
            removeEntity(context.senderAsPlayerRef(), entityRef, store);
         }
      } else {
         context.sendMessage(MESSAGE_GENERAL_NO_ENTITY_IN_VIEW);
      }
   }

   public static void removeEntity(
      @Nullable Ref<EntityStore> playerRef, @Nonnull Ref<EntityStore> entityReference, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (playerRef != null && playerRef.isValid()) {
         EntityTrackerSystems.EntityViewer entityViewer = componentAccessor.getComponent(playerRef, EntityTrackerSystems.EntityViewer.getComponentType());
         if (entityViewer != null && !entityViewer.visible.contains(entityReference)) {
            PlayerRef playerRefComponent = componentAccessor.getComponent(playerRef, PlayerRef.getComponentType());

            assert playerRefComponent != null;

            playerRefComponent.sendMessage(Message.translation("server.general.entity.remove.unableToRemove").param("id", entityReference.getIndex()));
            return;
         }
      }

      componentAccessor.removeEntity(entityReference, EntityStore.REGISTRY.newHolder(), RemoveReason.REMOVE);
   }
}
