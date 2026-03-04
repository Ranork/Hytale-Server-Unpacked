package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class ClearEntitiesCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_NO_SELECTION = Message.translation("server.commands.clearEntities.noSelection");

   public ClearEntitiesCommand() {
      super("clearEntities", "server.commands.clearEntities.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.requirePermission("hytale.editor.selection.clipboard");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRef);
         BlockSelection selection = builderState.getSelection();
         if (selection == null) {
            context.sendMessage(MESSAGE_NO_SELECTION);
         } else {
            Vector3i min = selection.getSelectionMin();
            Vector3i max = selection.getSelectionMax();
            int width = max.getX() - min.getX();
            int height = max.getY() - min.getY();
            int depth = max.getZ() - min.getZ();
            ArrayList<Ref<EntityStore>> entitiesToRemove = new ArrayList<>();
            BuilderToolsPlugin.forEachCopyableInSelection(world, min.getX(), min.getY(), min.getZ(), width, height, depth, entitiesToRemove::add);
            Store<EntityStore> entityStore = world.getEntityStore().getStore();

            for (Ref<EntityStore> entityRef : entitiesToRemove) {
               entityStore.removeEntity(entityRef, RemoveReason.REMOVE);
            }

            context.sendMessage(Message.translation("server.commands.clearEntities.cleared").param("count", entitiesToRemove.size()));
         }
      }
   }
}
