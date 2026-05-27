package com.hypixel.hytale.builtin.triggervolumes.command;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class TriggerVolumeInfoCommand extends AbstractWorldCommand {
   private final RequiredArg<String> nameArg = this.withRequiredArg("name", "server.commands.triggervolume.info.name.desc", TriggerVolumeArgTypes.VOLUME_NAME);

   public TriggerVolumeInfoCommand() {
      super("info", "server.commands.triggervolume.info.desc");
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
            Vector3d position = entry.getPosition();
            String shapeId = TriggerVolumeShape.CODEC.getIdFor((Class<? extends TriggerVolumeShape>)entry.getShape().getClass());
            context.sendMessage(
               Message.translation("server.commands.triggervolume.info.header")
                  .param("name", name)
                  .insert("\n")
                  .insert(Message.translation("server.commands.triggervolume.info.name").param("value", entry.getId()))
                  .insert("\n")
                  .insert(Message.translation("server.commands.triggervolume.info.world").param("value", entry.getWorldName()))
                  .insert("\n")
                  .insert(
                     Message.translation("server.commands.triggervolume.info.position")
                        .param("value", String.format("%.1f, %.1f, %.1f", position.x(), position.y(), position.z()))
                  )
                  .insert("\n")
                  .insert(Message.translation("server.commands.triggervolume.info.shape").param("value", shapeId != null ? shapeId : "?"))
                  .insert("\n")
                  .insert(Message.translation("server.commands.triggervolume.info.enabled").param("value", entry.isEnabled()))
                  .insert("\n")
                  .insert(Message.translation("server.commands.triggervolume.info.effects").param("value", entry.getEffects().size()))
                  .insert("\n")
                  .insert(Message.translation("server.commands.triggervolume.info.targets").param("value", entry.getTargetTypes().toString()))
                  .insert("\n")
                  .insert(Message.translation("server.commands.triggervolume.info.tracked").param("value", entry.getTrackedEntities().size()))
            );
         }
      }
   }
}
