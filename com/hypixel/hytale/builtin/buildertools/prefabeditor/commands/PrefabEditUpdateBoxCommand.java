package com.hypixel.hytale.builtin.buildertools.prefabeditor.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditingMetadata;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PrefabEditUpdateBoxCommand extends AbstractPlayerCommand {
   @Nonnull
   private final FlagArg confirmAnchorDeletionArg = this.withFlagArg("confirm", "server.commands.editprefab.setbox.confirm.desc");

   public PrefabEditUpdateBoxCommand() {
      super("setBox", "server.commands.editprefab.setbox.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      UUID playerUUID = playerRef.getUuid();
      PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
      PrefabEditSession prefabEditSession = prefabEditSessionManager.getPrefabEditSession(playerUUID);
      if (prefabEditSession == null) {
         context.sendMessage(Message.translation("server.commands.editprefab.notInEditSession"));
      } else {
         PrefabEditingMetadata selectedPrefab = prefabEditSession.getSelectedPrefab(playerUUID);
         if (selectedPrefab == null) {
            context.sendMessage(Message.translation("server.commands.editprefab.noPrefabSelected"));
         } else {
            boolean didMoveAnchor = false;
            BlockSelection currSelection = BuilderToolsPlugin.getState(playerComponent, playerRef).getSelection();
            if (currSelection != null && !this.isLocationWithinSelection(selectedPrefab.getAnchorEntityPosition(), currSelection)) {
               if (!this.confirmAnchorDeletionArg.get(context)) {
                  context.sendMessage(Message.translation("server.commands.editprefab.setbox.anchorOutsideNewSelection"));
                  return;
               }

               didMoveAnchor = true;
               selectedPrefab.setAnchorPoint(currSelection.getSelectionMin(), world);
               selectedPrefab.sendAnchorHighlightingPacket(playerRef.getPacketHandler());
            }

            boolean finalDidMoveAnchor = didMoveAnchor;
            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> {
               BlockSelection selection = s.getSelection();
               if (selection == null) {
                  context.sendMessage(Message.translation("server.commands.editprefab.noSelection"));
               } else {
                  Vector3i selectionMin = selection.getSelectionMin();
                  Vector3i selectionMax = selection.getSelectionMax();
                  prefabEditSession.updatePrefabBounds(selectedPrefab.getUuid(), selectionMin, selectionMax);
                  context.sendMessage(Message.translation("server.commands.editprefab.setbox.success"));
                  if (finalDidMoveAnchor) {
                     context.sendMessage(Message.translation("server.commands.editprefab.setbox.success.movedAnchor"));
                  }
               }
            });
         }
      }
   }

   public boolean isLocationWithinSelection(@Nonnull Vector3i location, @Nonnull BlockSelection selection) {
      Vector3i selectionMin = selection.getSelectionMin();
      Vector3i selectionMax = selection.getSelectionMax();
      return location.x >= selectionMin.x
         && location.x <= selectionMax.x
         && location.y >= selectionMin.y
         && location.y <= selectionMax.y
         && location.z >= selectionMin.z
         && location.z <= selectionMax.z;
   }
}
