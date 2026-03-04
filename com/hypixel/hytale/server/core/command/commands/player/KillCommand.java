package com.hypixel.hytale.server.core.command.commands.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class KillCommand extends AbstractPlayerCommand {
   public KillCommand() {
      super("kill", "server.commands.kill.desc");
      this.requirePermission(HytalePermissions.fromCommand("kill.self"));
      this.addUsageVariant(new KillCommand.KillOtherCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Damage.CommandSource damageSource = new Damage.CommandSource(context.sender(), this.getName());
      DeathComponent.tryAddComponent(store, ref, new Damage(damageSource, DamageCause.COMMAND, 2.1474836E9F));
      context.sendMessage(Message.translation("server.commands.kill.success.self"));
   }

   private static class KillOtherCommand extends CommandBase {
      private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
      @Nonnull
      private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);

      KillOtherCommand() {
         super("server.commands.kill.other.desc");
         this.requirePermission(HytalePermissions.fromCommand("kill.other"));
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         PlayerRef targetPlayerRef = this.playerArg.get(context);
         Ref<EntityStore> ref = targetPlayerRef.getReference();
         if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(() -> {
               Player playerComponent = store.getComponent(ref, Player.getComponentType());
               if (playerComponent == null) {
                  context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
               } else {
                  PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

                  assert playerRefComponent != null;

                  Damage.CommandSource damageSource = new Damage.CommandSource(context.sender(), "kill");
                  DeathComponent.tryAddComponent(store, ref, new Damage(damageSource, DamageCause.COMMAND, 2.1474836E9F));
                  context.sendMessage(Message.translation("server.commands.kill.success.other").param("username", playerRefComponent.getUsername()));
               }
            });
         } else {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
         }
      }
   }
}
