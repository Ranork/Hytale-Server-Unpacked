package com.hypixel.hytale.builtin.path.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.path.WorldPathData;
import com.hypixel.hytale.builtin.path.path.IPrefabPath;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PrefabPathMergeCommand extends AbstractPlayerCommand {
   public static final Message MESSAGE_COMMANDS_NPC_PATH_MERGE_NO_ACTIVE_PATH = Message.translation("server.commands.npcpath.merge.noActivePath");
   @Nonnull
   private final RequiredArg<UUID> targetPathIdArg = this.withRequiredArg("pathName", "server.commands.npcpath.merge.pathName.desc", ArgTypes.UUID);

   public PrefabPathMergeCommand() {
      super("merge", "server.commands.npcpath.merge.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      UUID activePathId = BuilderToolsPlugin.getState(playerComponent, playerRef).getActivePrefabPath();
      if (activePathId == null) {
         throw new GeneralCommandException(MESSAGE_COMMANDS_NPC_PATH_MERGE_NO_ACTIVE_PATH);
      } else {
         UUID targetPathId = this.targetPathIdArg.get(context);
         WorldPathData worldPathData = store.getResource(WorldPathData.getResourceType());
         IPrefabPath activePath = worldPathData.getPrefabPath(0, activePathId, false);
         if (activePath != null && activePath.isFullyLoaded()) {
            IPrefabPath targetPath = worldPathData.getPrefabPath(0, targetPathId, false);
            if (targetPath != null && targetPath.isFullyLoaded()) {
               targetPath.mergeInto(activePath, targetPath.getWorldGenId(), store);
               worldPathData.removePrefabPath(0, targetPathId);
               playerRef.sendMessage(
                  Message.translation("server.npc.npcpath.pathMergedInto")
                     .param("targetPathName", targetPathId.toString())
                     .param("activePathName", activePathId.toString())
               );
            } else {
               playerRef.sendMessage(Message.translation("server.npc.npcpath.pathMustBeLoaded").param("path", targetPathId.toString()));
            }
         } else {
            playerRef.sendMessage(Message.translation("server.npc.npcpath.pathMustBeLoaded").param("path", activePathId.toString()));
         }
      }
   }
}
