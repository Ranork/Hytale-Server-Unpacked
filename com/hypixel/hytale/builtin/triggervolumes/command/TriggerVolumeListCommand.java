package com.hypixel.hytale.builtin.triggervolumes.command;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import javax.annotation.Nonnull;

public class TriggerVolumeListCommand extends AbstractWorldCommand {
   public TriggerVolumeListCommand() {
      super("list", "server.commands.triggervolume.list.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         Map<String, VolumeEntry> volumes = manager.getVolumesMap();
         if (volumes.isEmpty()) {
            context.sendMessage(Message.translation("server.commands.triggervolume.list.empty"));
         } else {
            Message msg = Message.translation("server.commands.triggervolume.list.header").param("count", volumes.size());

            for (VolumeEntry entry : volumes.values()) {
               String shapeId = TriggerVolumeShape.CODEC.getIdFor((Class<? extends TriggerVolumeShape>)entry.getShape().getClass());
               msg.insert("\n")
                  .insert(
                     Message.translation("server.commands.triggervolume.list.entry")
                        .param("id", entry.getId())
                        .param("shape", shapeId != null ? shapeId : "?")
                        .param("enabled", entry.isEnabled())
                  );
            }

            context.sendMessage(msg);
         }
      }
   }
}
