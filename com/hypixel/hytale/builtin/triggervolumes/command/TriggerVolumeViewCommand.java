package com.hypixel.hytale.builtin.triggervolumes.command;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.player.UpdateTriggerVolumeDisplay;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public class TriggerVolumeViewCommand extends AbstractWorldCommand {
   public TriggerVolumeViewCommand() {
      super("view", "server.commands.triggervolume.view.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         PlayerRef playerRef = context.senderAs(PlayerRef.class);
         UUID uuid = playerRef.getUuid();
         if (manager.isViewing(uuid, TriggerVolumeManager.ViewSource.COMMAND)) {
            manager.removeViewer(uuid, TriggerVolumeManager.ViewSource.COMMAND);
            if (!manager.isViewing(uuid)) {
               playerRef.getPacketHandler().write(new UpdateTriggerVolumeDisplay());
            }

            context.sendMessage(Message.translation("server.commands.triggervolume.view.disabled"));
         } else {
            manager.addViewer(uuid, TriggerVolumeManager.ViewSource.COMMAND);
            manager.sendVolumeDisplay(playerRef);
            context.sendMessage(Message.translation("server.commands.triggervolume.view.enabled"));
         }
      }
   }
}
