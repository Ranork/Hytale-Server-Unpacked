package com.hypixel.hytale.builtin.triggervolumes.command;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TriggerVolumeEnableCommand extends AbstractWorldCommand {
   private final RequiredArg<String> nameArg = this.withRequiredArg("name", "server.commands.triggervolume.enable.name.desc", TriggerVolumeArgTypes.VOLUME_NAME);

   public TriggerVolumeEnableCommand() {
      super("enable", "server.commands.triggervolume.enable.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      String name = this.nameArg.get(context);
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         VolumeEntry entry = manager.getVolume(name);
         if (entry == null) {
            context.sendMessage(Message.translation("server.commands.triggervolume.notFound").param("name", name));
         } else {
            entry.setEnabled(true);
            manager.markSpatialDirty();
            manager.notifyViewersAdd(entry);
            context.sendMessage(Message.translation("server.commands.triggervolume.enable.success").param("name", name));
         }
      }
   }
}
