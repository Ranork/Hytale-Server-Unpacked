package com.hypixel.hytale.builtin.triggervolumes.command;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.asset.TriggerEffectAsset;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class TriggerVolumeAssignEffectCommand extends AbstractWorldCommand {
   private final OptionalArg<String> volumeNameArg = this.withOptionalArg(
      "volumeName", "server.commands.triggervolume.assigneffect.volumeName.desc", TriggerVolumeArgTypes.VOLUME_NAME
   );
   private final RequiredArg<String> effectAssetIdArg = this.withRequiredArg(
      "effectAssetId", "server.commands.triggervolume.assigneffect.effectId.desc", ArgTypes.STRING
   );

   public TriggerVolumeAssignEffectCommand() {
      super("assigneffect", "server.commands.triggervolume.assigneffect.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      String effectId = this.effectAssetIdArg.get(context);
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         String volumeName = this.volumeNameArg.get(context);
         if (volumeName == null) {
            volumeName = manager.getPlayerSelection(context.sender().getUuid());
         }

         if (volumeName == null) {
            context.sendMessage(Message.translation("server.commands.triggervolume.assigneffect.noSelection"));
         } else {
            VolumeEntry entry = manager.getVolume(volumeName);
            if (entry == null) {
               context.sendMessage(Message.translation("server.commands.triggervolume.notFound").param("name", volumeName));
            } else {
               AssetStore<String, TriggerEffectAsset, DefaultAssetMap<String, TriggerEffectAsset>> effectAssetStore = AssetRegistry.getAssetStore(
                  TriggerEffectAsset.class
               );
               if (effectAssetStore != null) {
                  TriggerEffectAsset effectAsset = (TriggerEffectAsset)((DefaultAssetMap)effectAssetStore.getAssetMap()).getAsset(effectId);
                  if (effectAsset == null) {
                     context.sendMessage(Message.translation("server.commands.triggervolume.assigneffect.effectNotFound").param("effectId", effectId));
                  } else {
                     entry.getConditions().clear();
                     entry.getConditions().addAll(Arrays.asList(effectAsset.getConditions()));
                     entry.getEffects().clear();
                     entry.getEffects().addAll(Arrays.asList(effectAsset.getEffects()));
                     entry.getRejectionEffects().clear();
                     entry.getRejectionEffects().addAll(Arrays.asList(effectAsset.getRejectionEffects()));
                     entry.setConditionTiming(effectAsset.getConditionTiming());
                     entry.setEffectAssetRef(effectId);
                     manager.notifyViewersAdd(entry);
                     context.sendMessage(
                        Message.translation("server.commands.triggervolume.assigneffect.success").param("volumeName", volumeName).param("effectId", effectId)
                     );
                  }
               }
            }
         }
      }
   }
}
