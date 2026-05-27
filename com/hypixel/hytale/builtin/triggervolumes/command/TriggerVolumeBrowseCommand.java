package com.hypixel.hytale.builtin.triggervolumes.command;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.ui.TriggerVolumeInspectorPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Locale;
import javax.annotation.Nonnull;

public class TriggerVolumeBrowseCommand extends AbstractPlayerCommand {
   private final OptionalArg<String> volumeNameArg = this.withOptionalArg(
      "volumeName", "server.commands.triggervolume.browse.volumeName.desc", TriggerVolumeArgTypes.VOLUME_NAME
   );

   public TriggerVolumeBrowseCommand() {
      super("browse", "server.commands.triggervolume.browse.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         String selectedWorld = world.getName().toLowerCase(Locale.ROOT);
         String volumeName = this.volumeNameArg.get(context);
         if (volumeName == null) {
            TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
            TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
            if (manager != null) {
               volumeName = manager.getPlayerSelection(context.sender().getUuid());
            }
         }

         playerComponent.getPageManager()
            .openCustomPage(ref, store, new TriggerVolumeInspectorPage(playerRef, selectedWorld, volumeName, TriggerVolumeInspectorPage.InspectorTab.VOLUME));
      }
   }
}
