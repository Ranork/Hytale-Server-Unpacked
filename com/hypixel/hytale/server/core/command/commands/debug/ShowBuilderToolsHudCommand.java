package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.interface_.HudComponent;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ShowBuilderToolsHudCommand extends AbstractPlayerCommand {
   @Nonnull
   private final FlagArg hideArg = this.withFlagArg("hide", "server.commands.builderToolsLegend.hide.desc");

   public ShowBuilderToolsHudCommand() {
      super("builderToolsLegend", "server.commands.builderToolsLegend.desc");
      this.setPermissionGroup(GameMode.Creative);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      HudManager hudManager = playerComponent.getHudManager();
      if (this.hideArg.provided(context)) {
         hudManager.hideHudComponents(playerRef, HudComponent.BuilderToolsLegend);
         hudManager.showHudComponents(playerRef, HudComponent.BuilderToolsMaterialSlotSelector);
      } else {
         hudManager.showHudComponents(playerRef, HudComponent.BuilderToolsLegend);
         hudManager.showHudComponents(playerRef, HudComponent.BuilderToolsMaterialSlotSelector);
      }
   }
}
