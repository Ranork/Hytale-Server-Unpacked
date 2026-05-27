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

public class TriggerVolumeUnassignEffectCommand extends AbstractWorldCommand {
   private final RequiredArg<String> volumeNameArg = this.withRequiredArg(
      "volumeName", "server.commands.triggervolume.unassigneffect.volumeName.desc", TriggerVolumeArgTypes.VOLUME_NAME
   );

   public TriggerVolumeUnassignEffectCommand() {
      super("unassigneffect", "server.commands.triggervolume.unassigneffect.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      String volumeName = this.volumeNameArg.get(context);
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         VolumeEntry entry = manager.getVolume(volumeName);
         if (entry == null) {
            context.sendMessage(Message.translation("server.commands.triggervolume.notFound").param("name", volumeName));
         } else {
            entry.getEffects().clear();
            entry.setEffectAssetRef(null);
            context.sendMessage(Message.translation("server.commands.triggervolume.unassigneffect.success").param("volumeName", volumeName));
         }
      }
   }
}
