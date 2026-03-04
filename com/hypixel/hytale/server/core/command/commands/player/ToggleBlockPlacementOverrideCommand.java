package com.hypixel.hytale.server.core.command.commands.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ToggleBlockPlacementOverrideCommand extends AbstractPlayerCommand {
   public ToggleBlockPlacementOverrideCommand() {
      super("toggleBlockPlacementOverride", "server.commands.toggleBlockPlacementOverride.desc");
      this.addAliases("tbpo", "togglePlacement");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      playerComponent.setOverrideBlockPlacementRestrictions(ref, !playerComponent.isOverrideBlockPlacementRestrictions(), store);
      context.sendMessage(
         Message.translation(
            "server.commands.toggleBlockPlacementOverride." + (playerComponent.isOverrideBlockPlacementRestrictions() ? "enabled" : "disabled")
         )
      );
   }
}
