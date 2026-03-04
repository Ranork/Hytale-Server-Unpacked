package com.hypixel.hytale.builtin.buildertools.prefabeditor.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditingMetadata;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import java.util.List;
import javax.annotation.Nonnull;

public class PrefabEditKillEntitiesCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_NOT_IN_EDIT_SESSION = Message.translation("servers.commands.editprefab.notInEditSession");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EDIT_PREFAB_NO_PREFAB_SELECTED = Message.translation("server.commands.editprefab.noPrefabSelected");

   public PrefabEditKillEntitiesCommand() {
      super("kill", "server.commands.editprefab.kill.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
      PrefabEditSession prefabEditSession = prefabEditSessionManager.getPrefabEditSession(playerRef.getUuid());
      if (prefabEditSession == null) {
         context.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_NOT_IN_EDIT_SESSION);
      } else {
         PrefabEditingMetadata selectedPrefab = prefabEditSession.getSelectedPrefab(playerRef.getUuid());
         if (selectedPrefab == null) {
            context.sendMessage(MESSAGE_COMMANDS_EDIT_PREFAB_NO_PREFAB_SELECTED);
         } else {
            Vector3i selectionMax = selectedPrefab.getMaxPoint();
            Vector3i selectionMin = selectedPrefab.getMinPoint();
            Vector3i lengths = selectionMax.subtract(selectionMin);
            Vector3d min = new Vector3d(selectionMin.x, selectionMin.y, selectionMin.z);
            Vector3d max = new Vector3d(selectionMax.x + 1, selectionMax.y + 1, selectionMax.z + 1);
            List<Ref<EntityStore>> entitiesInBox = TargetUtil.getAllEntitiesInBox(min, max, store);

            for (Ref<EntityStore> entityRef : entitiesInBox) {
               store.removeEntity(entityRef, RemoveReason.REMOVE);
            }

            context.sendMessage(Message.translation("server.commands.editprefab.kill.done").param("amount", entitiesInBox.size()));
         }
      }
   }
}
