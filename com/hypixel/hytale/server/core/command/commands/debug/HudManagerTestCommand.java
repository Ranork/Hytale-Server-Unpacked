package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.HudComponent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HudManagerTestCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_HUD_TEST_SHOWN_SELF = Message.translation("server.commands.hudtest.shown.self");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_HUT_TEST_HIDDEN_SELF = Message.translation("server.commands.hudtest.hidden.self");
   @Nonnull
   private final FlagArg resetHudFlag = this.withFlagArg("reset", "server.commands.hudtest.reset.desc");

   public HudManagerTestCommand() {
      super("hudtest", "server.commands.hudtest.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      HudManager hudManager = playerComponent.getHudManager();
      boolean isTargetingOther = !ref.equals(sourceRef);
      if (this.resetHudFlag.provided(context)) {
         hudManager.showHudComponents(playerRef, HudComponent.Hotbar);
         if (isTargetingOther) {
            context.sendMessage(Message.translation("server.commands.hudtest.shown.other").param("username", playerRefComponent.getUsername()));
         } else {
            context.sendMessage(MESSAGE_COMMANDS_HUD_TEST_SHOWN_SELF);
         }
      } else {
         hudManager.hideHudComponents(playerRef, HudComponent.Hotbar);
         if (isTargetingOther) {
            context.sendMessage(Message.translation("server.commands.hudtest.hidden.other").param("username", playerRefComponent.getUsername()));
         } else {
            context.sendMessage(MESSAGE_COMMANDS_HUT_TEST_HIDDEN_SELF);
         }
      }
   }
}
