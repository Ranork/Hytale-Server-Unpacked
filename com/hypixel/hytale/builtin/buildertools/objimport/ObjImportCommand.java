package com.hypixel.hytale.builtin.buildertools.objimport;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ObjImportCommand extends AbstractPlayerCommand {
   public ObjImportCommand() {
      super("importobj", "server.commands.importobj.desc");
      this.addAliases("obj");
      this.setPermissionGroups("hytale:WorldEditor");
      this.requirePermission(HytalePermissions.EDITOR_SELECTION_CLIPBOARD);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         playerComponent.getPageManager().openCustomPage(ref, store, new ObjImportPage(playerRef));
      }
   }
}
