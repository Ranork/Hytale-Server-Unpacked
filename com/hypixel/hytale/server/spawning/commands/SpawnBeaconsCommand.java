package com.hypixel.hytale.server.spawning.commands;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetEntityCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawns.config.BeaconNPCSpawn;
import com.hypixel.hytale.server.spawning.beacons.LegacySpawnBeaconEntity;
import com.hypixel.hytale.server.spawning.beacons.SpawnBeacon;
import com.hypixel.hytale.server.spawning.util.FloodFillPositionSelector;
import com.hypixel.hytale.server.spawning.wrappers.BeaconSpawnWrapper;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import javax.annotation.Nonnull;

public class SpawnBeaconsCommand extends AbstractCommandCollection {
   private static final AssetArgumentType<BeaconNPCSpawn, ?> BEACON_SPAWN_ASSET_TYPE = new AssetArgumentType(
      "server.commands.spawning.beacons.arg.beacon.name", BeaconNPCSpawn.class, "server.commands.spawning.beacons.arg.beacon.usage"
   );

   public SpawnBeaconsCommand() {
      super("beacons", "server.commands.spawning.beacons.desc");
      this.addSubCommand(new SpawnBeaconsCommand.Add());
      this.addSubCommand(new SpawnBeaconsCommand.ManualTrigger());
   }

   private static class Add extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<BeaconNPCSpawn> beaconArg = this.withRequiredArg(
         "beacon", "server.commands.spawning.beacons.add.arg.beacon.desc", SpawnBeaconsCommand.BEACON_SPAWN_ASSET_TYPE
      );
      @Nonnull
      private final FlagArg manualArg = this.withFlagArg("manual", "server.commands.spawning.beacons.add.manual.desc");

      public Add() {
         super("add", "server.commands.spawning.beacons.add.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3f rotation = transformComponent.getRotation();
         Vector3d position = transformComponent.getPosition();
         BeaconNPCSpawn beacon = this.beaconArg.get(context);
         BeaconSpawnWrapper wrapper = SpawningPlugin.get().getBeaconSpawnWrapper(BeaconNPCSpawn.getAssetMap().getIndex(beacon.getId()));
         if (this.manualArg.get(context)) {
            SpawnBeacon entity = new SpawnBeacon();
            entity.setSpawnWrapper(wrapper);
            BeaconNPCSpawn spawn = wrapper.getSpawn();
            String modelName = spawn.getModel();
            ModelAsset modelAsset = null;
            if (modelName != null && !modelName.isEmpty()) {
               modelAsset = ModelAsset.getAssetMap().getAsset(modelName);
            }

            Model model;
            if (modelAsset == null) {
               model = SpawningPlugin.get().getSpawnMarkerModel();
            } else {
               model = Model.createUnitScaleModel(modelAsset);
            }

            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
            holder.addComponent(SpawnBeacon.getComponentType(), entity);
            holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position, rotation));
            holder.ensureComponent(UUIDComponent.getComponentType());
            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
            holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
            Message displayNameMessage = Message.raw(spawn.getId());
            holder.addComponent(DisplayNameComponent.getComponentType(), new DisplayNameComponent(displayNameMessage));
            holder.addComponent(Nameplate.getComponentType(), new Nameplate(spawn.getId()));
            store.addEntity(holder, AddReason.SPAWN);
         } else {
            LegacySpawnBeaconEntity.create(wrapper, position, rotation, store);
         }

         context.sendMessage(Message.translation("server.commands.spawning.beacons.add.added").param("beaconId", beacon.getId()));
      }
   }

   private static class ManualTrigger extends AbstractTargetEntityCommand {
      private static final Message MESSAGE_COMMANDS_SPAWNING_BEACONS_TRIGGER_NO_BEACONS = Message.translation(
         "server.commands.spawning.beacons.trigger.no_beacons"
      );

      public ManualTrigger() {
         super("trigger", "server.commands.spawning.beacons.trigger.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull ObjectList<Ref<EntityStore>> entities, @Nonnull World world, @Nonnull Store<EntityStore> store
      ) {
         if (entities.isEmpty()) {
            context.sendMessage(MESSAGE_COMMANDS_SPAWNING_BEACONS_TRIGGER_NO_BEACONS);
         } else {
            int count = 0;
            ObjectListIterator var6 = entities.iterator();

            while (var6.hasNext()) {
               Ref<EntityStore> ref = (Ref<EntityStore>)var6.next();
               if (ref != null && ref.isValid()) {
                  UUIDComponent uuid = store.getComponent(ref, UUIDComponent.getComponentType());
                  if (uuid != null) {
                     FloodFillPositionSelector positionSelectorComponent = store.getComponent(ref, FloodFillPositionSelector.getComponentType());
                     if (positionSelectorComponent != null) {
                        SpawnBeacon spawnBeaconComponent = store.getComponent(ref, SpawnBeacon.getComponentType());
                        if (spawnBeaconComponent != null) {
                           if (!spawnBeaconComponent.manualTrigger(ref, positionSelectorComponent, ref, store)) {
                              Message message = Message.translation("server.commands.spawning.beacons.trigger.no_spots");
                              message.param("id", uuid.getUuid().toString());
                              context.sendMessage(message);
                           } else {
                              Message message = Message.translation("server.commands.spawning.beacons.trigger.success");
                              message.param("id", uuid.getUuid().toString());
                              context.sendMessage(message);
                              count++;
                           }
                        }
                     }
                  }
               }
            }

            if (count == 0) {
               context.sendMessage(MESSAGE_COMMANDS_SPAWNING_BEACONS_TRIGGER_NO_BEACONS);
            }
         }
      }
   }
}
