package com.hypixel.hytale.builtin.path.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.path.WorldPathData;
import com.hypixel.hytale.builtin.path.entities.PatrolPathMarkerEntity;
import com.hypixel.hytale.builtin.path.path.IPrefabPath;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PrefabPathEditCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_NPC_PATH_EDIT_NO_ENTITY_IN_VIEW = Message.translation("server.commands.npcpath.edit.noEntityInView");
   @Nonnull
   private final OptionalArg<UUID> pathIdArg = this.withOptionalArg("pathId", "server.commands.npcpath.edit.pathId.desc", ArgTypes.UUID);

   public PrefabPathEditCommand() {
      super("edit", "server.commands.npcpath.edit.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      UUID pathId;
      if (this.pathIdArg.provided(context)) {
         pathId = this.pathIdArg.get(context);
      } else {
         Ref<EntityStore> entityRef = TargetUtil.getTargetEntity(ref, store);
         if (entityRef == null || !entityRef.isValid()) {
            return;
         }

         if (!(EntityUtils.getEntity(ref, store) instanceof PatrolPathMarkerEntity pathMarkerEntity)) {
            context.sendMessage(MESSAGE_COMMANDS_NPC_PATH_EDIT_NO_ENTITY_IN_VIEW);
            return;
         }

         pathId = pathMarkerEntity.getPathId();
      }

      WorldPathData worldPathData = store.getResource(WorldPathData.getResourceType());
      IPrefabPath path = worldPathData.getPrefabPath(0, pathId, false);
      if (path == null) {
         context.sendMessage(Message.translation("server.npc.npcpath.pathMustBeLoaded").param("path", pathId.toString()));
      } else {
         BuilderToolsPlugin.getState(playerComponent, playerRef).setActivePrefabPath(pathId);
         context.sendMessage(Message.translation("server.npc.npcpath.editingPath").param("path", pathId.toString()));
      }
   }
}
