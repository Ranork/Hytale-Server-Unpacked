package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public class ClearEntitiesCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_NO_SELECTION = Message.translation("server.commands.clearEntities.noSelection");

   public ClearEntitiesCommand() {
      super("clearEntities", "server.commands.clearEntities.desc");
      this.setPermissionGroups("hytale:WorldEditor");
      this.requirePermission(HytalePermissions.EDITOR_SELECTION_CLIPBOARD);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerRef, store)) {
         BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRef);
         BlockSelection selection = builderState.getSelection();
         if (selection == null) {
            context.sendMessage(MESSAGE_NO_SELECTION);
         } else {
            Vector3i min = selection.getSelectionMin();
            Vector3i max = selection.getSelectionMax();
            int width = max.x() - min.x();
            int height = max.y() - min.y();
            int depth = max.z() - min.z();
            ReferenceArrayList<Ref<EntityStore>> entitiesToRemove = new ReferenceArrayList();
            BuilderToolsPlugin.forEachCopyableInSelection(world, min.x(), min.y(), min.z(), width, height, depth, entitiesToRemove::add);
            Store<EntityStore> entityStore = world.getEntityStore().getStore();
            ObjectListIterator var16 = entitiesToRemove.iterator();

            while (var16.hasNext()) {
               Ref<EntityStore> entityRef = (Ref<EntityStore>)var16.next();
               entityStore.removeEntity(entityRef, RemoveReason.REMOVE);
            }

            context.sendMessage(Message.translation("server.commands.clearEntities.cleared").param("count", entitiesToRemove.size()));
         }
      }
   }
}
