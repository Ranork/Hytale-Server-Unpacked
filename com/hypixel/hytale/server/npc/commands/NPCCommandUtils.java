package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPCCommandUtils {
   @Nullable
   public static Pair<Ref<EntityStore>, NPCEntity> getTargetNpc(
      @Nonnull CommandContext context, @Nonnull EntityWrappedArg arg, @Nonnull Store<EntityStore> store
   ) {
      Ref<EntityStore> ref;
      if (arg.provided(context)) {
         ref = arg.get(store, context);
      } else {
         if (!context.isPlayer()) {
            context.sendMessage(Message.translation("server.commands.errors.playerOrArg").param("option", "entity"));
            return null;
         }

         Ref<EntityStore> playerRef = context.senderAsPlayerRef();
         if (playerRef == null || !playerRef.isValid()) {
            context.sendMessage(Message.translation("server.commands.errors.playerOrArg").param("option", "entity"));
            return null;
         }

         ref = TargetUtil.getTargetEntity(playerRef, store);
         if (ref == null) {
            context.sendMessage(Message.translation("server.commands.errors.no_entity_in_view").param("option", "entity"));
            return null;
         }
      }

      if (ref != null && ref.isValid()) {
         NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());
         if (npcComponent == null) {
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

            assert uuidComponent != null;

            UUID uuid = uuidComponent.getUuid();
            context.sendMessage(Message.translation("server.commands.errors.not_npc").param("uuid", uuid.toString()));
            return null;
         } else {
            return Pair.of(ref, npcComponent);
         }
      } else {
         return null;
      }
   }
}
