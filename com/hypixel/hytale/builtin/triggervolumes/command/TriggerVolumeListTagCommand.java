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

public class TriggerVolumeListTagCommand extends AbstractWorldCommand {
   private final RequiredArg<String> tagArg = this.withRequiredArg("tag", "server.commands.triggervolume.listtag.tag.desc", ArgTypes.STRING);

   public TriggerVolumeListTagCommand() {
      super("listtag", "server.commands.triggervolume.listtag.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      String tag = this.tagArg.get(context);
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         int tagIndex = AssetRegistry.getTagIndex(tag);
         if (tagIndex == Integer.MIN_VALUE) {
            context.sendMessage(Message.translation("server.commands.triggervolume.listtag.tagNotFound").param("tag", tag));
         } else {
            List<VolumeEntry> matched = manager.getVolumesByTag(tagIndex);
            if (matched.isEmpty()) {
               context.sendMessage(Message.translation("server.commands.triggervolume.listtag.noMatch").param("tag", tag));
            } else {
               Message msg = Message.translation("server.commands.triggervolume.listtag.header").param("count", matched.size()).param("tag", tag);

               for (VolumeEntry entry : matched) {
                  msg.insert("\n")
                     .insert(Message.translation("server.commands.triggervolume.listtag.entry").param("id", entry.getId()).param("enabled", entry.isEnabled()));
               }

               context.sendMessage(msg);
            }
         }
      }
   }
}
