package com.hypixel.hytale.builtin.triggervolumes.command;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.asset.TriggerEffectAsset;
import com.hypixel.hytale.builtin.triggervolumes.manager.GroupEntry;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class TriggerVolumeAssignGroupEffectCommand extends AbstractWorldCommand {
   private final RequiredArg<String> groupNameArg = this.withRequiredArg(
      "groupName", "server.commands.triggervolume.assigngroupeffect.groupName.desc", TriggerVolumeArgTypes.GROUP_NAME
   );
   private final RequiredArg<String> effectAssetIdArg = this.withRequiredArg(
      "effectAssetId", "server.commands.triggervolume.assigngroupeffect.effectId.desc", ArgTypes.STRING
   );

   public TriggerVolumeAssignGroupEffectCommand() {
      super("assigngroupeffect", "server.commands.triggervolume.assigngroupeffect.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      String groupName = this.groupNameArg.get(context);
      String effectId = this.effectAssetIdArg.get(context);
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         GroupEntry group = manager.getGroup(groupName);
         if (group == null) {
            context.sendMessage(Message.translation("server.commands.triggervolume.groupNotFound").param("name", groupName));
         } else {
            AssetStore<String, TriggerEffectAsset, DefaultAssetMap<String, TriggerEffectAsset>> effectAssetStore = AssetRegistry.getAssetStore(
               TriggerEffectAsset.class
            );
            if (effectAssetStore != null) {
               TriggerEffectAsset effectAsset = (TriggerEffectAsset)((DefaultAssetMap)effectAssetStore.getAssetMap()).getAsset(effectId);
               if (effectAsset == null) {
                  context.sendMessage(Message.translation("server.commands.triggervolume.assigngroupeffect.effectNotFound").param("effectId", effectId));
               } else {
                  group.getEffects().clear();
                  group.getEffects().addAll(Arrays.asList(effectAsset.getEffects()));
                  context.sendMessage(
                     Message.translation("server.commands.triggervolume.assigngroupeffect.success").param("groupName", groupName).param("effectId", effectId)
                  );
               }
            }
         }
      }
   }
}
