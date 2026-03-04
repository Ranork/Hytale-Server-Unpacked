package com.hypixel.hytale.server.core.command.commands.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class SudoCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_SU_INVALID_USAGE = Message.translation("server.commands.sudo.invalidusage");
   @Nonnull
   private final RequiredArg<String> playerArg = this.withRequiredArg("player", "server.commands.sudo.player.desc", ArgTypes.STRING);

   public SudoCommand() {
      super("sudo", "server.commands.sudo.desc");
      this.addAliases("su");
      this.setAllowsExtraArguments(true);
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      String playerName = this.playerArg.get(context);
      String inputString = context.getInputString();
      String rawArgs = CommandUtil.stripCommandName(inputString);
      int commandIndex = rawArgs.indexOf(32);
      if (commandIndex == -1) {
         context.sendMessage(MESSAGE_COMMANDS_SU_INVALID_USAGE);
      } else {
         String commandToExecute = rawArgs.substring(commandIndex + 1).trim();
         if (commandToExecute.isEmpty()) {
            context.sendMessage(MESSAGE_COMMANDS_SU_INVALID_USAGE);
         } else {
            if (commandToExecute.charAt(0) == '/') {
               commandToExecute = commandToExecute.substring(1);
            }

            List<PlayerRef> players;
            if (playerName.equals("*")) {
               players = Universe.get().getPlayers();
            } else {
               PlayerRef player = Universe.get().getPlayer(playerName, NameMatching.DEFAULT);
               if (player == null) {
                  context.sendMessage(Message.translation("server.commands.errors.noSuchPlayer").param("username", playerName));
                  return;
               }

               players = new ObjectArrayList();
               players.add(player);
            }

            if (players.isEmpty()) {
               context.sendMessage(Message.translation("server.commands.errors.noSuchPlayer").param("username", playerName));
            } else {
               String finalCommand = commandToExecute;

               for (PlayerRef player : players) {
                  Ref<EntityStore> ref = player.getReference();
                  if (ref != null && ref.isValid()) {
                     Store<EntityStore> store = ref.getStore();
                     World world = store.getExternalData().getWorld();
                     world.execute(() -> {
                        Player playerComponent = store.getComponent(ref, Player.getComponentType());

                        assert playerComponent != null;

                        CommandManager.get().handleCommand(playerComponent, finalCommand);
                     });
                  }
               }
            }
         }
      }
   }
}
