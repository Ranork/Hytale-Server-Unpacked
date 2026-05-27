package com.hypixel.hytale.builtin.triggervolumes.command;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import javax.annotation.Nonnull;

public class TriggerVolumeEnableTagCommand extends AbstractWorldCommand {
   private final RequiredArg<String> tagArg = this.withRequiredArg("tag", "server.commands.triggervolume.enabletag.tag.desc", ArgTypes.STRING);

   public TriggerVolumeEnableTagCommand() {
      super("enabletag", "server.commands.triggervolume.enabletag.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      String tag = this.tagArg.get(context);
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         int tagIndex = AssetRegistry.getTagIndex(tag);
         if (tagIndex == Integer.MIN_VALUE) {
            context.sendMessage(Message.translation("server.commands.triggervolume.enabletag.tagNotFound").param("tag", tag));
         } else {
            List<VolumeEntry> matched = manager.getVolumesByTag(tagIndex);
            if (matched.isEmpty()) {
               context.sendMessage(Message.translation("server.commands.triggervolume.enabletag.noMatch").param("tag", tag));
            } else {
               int count = 0;

               for (VolumeEntry entry : matched) {
                  if (!entry.isEnabled()) {
                     entry.setEnabled(true);
                     count++;
                  }
               }

               if (count > 0) {
                  manager.markSpatialDirty();
               }

               manager.notifyViewers();
               context.sendMessage(Message.translation("server.commands.triggervolume.enabletag.success").param("count", count).param("tag", tag));
            }
         }
      }
   }
}
