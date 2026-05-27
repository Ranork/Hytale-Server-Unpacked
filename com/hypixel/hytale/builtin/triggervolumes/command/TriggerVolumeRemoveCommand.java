package com.hypixel.hytale.builtin.triggervolumes.command;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TriggerVolumeRemoveCommand extends AbstractWorldCommand {
   private final RequiredArg<String> nameArg = this.withRequiredArg("name", "server.commands.triggervolume.remove.name.desc", TriggerVolumeArgTypes.VOLUME_NAME);

   public TriggerVolumeRemoveCommand() {
      super("remove", "server.commands.triggervolume.remove.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      String name = this.nameArg.get(context);
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         if (!manager.hasVolume(name)) {
            context.sendMessage(Message.translation("server.commands.triggervolume.notFound").param("name", name));
         } else {
            manager.unregister(name);
            manager.notifyViewersRemove(name);
            context.sendMessage(Message.translation("server.commands.triggervolume.remove.success").param("name", name));
         }
      }
   }
}
