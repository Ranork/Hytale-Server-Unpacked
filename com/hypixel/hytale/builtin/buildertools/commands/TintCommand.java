package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TintCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<String> colorArg = this.withRequiredArg("color", "server.commands.tint.color.desc", ArgTypes.STRING);

   public TintCommand() {
      super("tint", "server.commands.tint.desc");
      this.setPermissionGroup(GameMode.Creative);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         String color = this.colorArg.get(context);

         int colorI;
         try {
            if (color.charAt(0) == '#') {
               colorI = Integer.parseInt(color.substring(1), 16);
            } else {
               colorI = Integer.parseInt(color);
            }
         } catch (NumberFormatException var10) {
            context.sendMessage(Message.translation("server.builderTools.tint.colorFormatError").param("color", color));
            return;
         }

         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.tint(r, colorI, componentAccessor));
      }
   }
}
