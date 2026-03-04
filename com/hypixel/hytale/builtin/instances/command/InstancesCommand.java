package com.hypixel.hytale.builtin.instances.command;

import com.hypixel.hytale.builtin.instances.page.InstanceListPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class InstancesCommand extends AbstractPlayerCommand {
   public InstancesCommand() {
      super("instances", "server.commands.instances.desc");
      this.addAliases("instance", "inst");
      this.addSubCommand(new InstancesCommand.InstancesEditCommand());
      this.addSubCommand(new InstanceSpawnCommand());
      this.addSubCommand(new InstanceExitCommand());
      this.addSubCommand(new InstanceMigrateCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      playerComponent.getPageManager().openCustomPage(ref, store, new InstanceListPage(playerRef));
   }

   public static class InstancesEditCommand extends AbstractCommandCollection {
      public InstancesEditCommand() {
         super("edit", "server.commands.instances.edit.desc");
         this.addAliases("modify");
         this.addSubCommand(new InstanceEditNewCommand());
         this.addSubCommand(new InstanceEditCopyCommand());
         this.addSubCommand(new InstanceEditLoadCommand());
         this.addSubCommand(new InstanceEditListCommand());
      }
   }
}
