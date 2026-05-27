package com.hypixel.hytale.builtin.triggervolumes.command;

import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.builtin.triggervolumes.shape.BoxShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.CylinderShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.SphereShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Locale;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class TriggerVolumeCreateCommand extends AbstractWorldCommand {
   private final RequiredArg<String> nameArg = this.withRequiredArg("name", "server.commands.triggervolume.create.name.desc", ArgTypes.STRING);
   private final RequiredArg<String> shapeArg = this.withRequiredArg("shape", "server.commands.triggervolume.create.shape.desc", ArgTypes.STRING);
   private final RequiredArg<Double> dimension1Arg = this.withRequiredArg("dimension1", "server.commands.triggervolume.create.dimension1.desc", ArgTypes.DOUBLE);
   private final RequiredArg<Double> dimension2Arg = this.withRequiredArg("dimension2", "server.commands.triggervolume.create.dimension2.desc", ArgTypes.DOUBLE);
   private final RequiredArg<Double> dimension3Arg = this.withRequiredArg("dimension3", "server.commands.triggervolume.create.dimension3.desc", ArgTypes.DOUBLE);

   public TriggerVolumeCreateCommand() {
      super("create", "server.commands.triggervolume.create.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      String name = this.nameArg.get(context);
      String shapeType = this.shapeArg.get(context).toLowerCase(Locale.ROOT);
      Double dimension1 = this.dimension1Arg.get(context);
      Double dimension2 = this.dimension2Arg.get(context);
      Double dimension3 = this.dimension3Arg.get(context);
      String worldName = world.getName().toLowerCase(Locale.ROOT);
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         if (manager.hasVolume(name)) {
            context.sendMessage(Message.translation("server.commands.triggervolume.create.alreadyExists").param("name", name));
         } else {
            Ref<EntityStore> playerRef = context.senderAsPlayerRef();
            if (playerRef != null && playerRef.isValid()) {
               TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());
               if (transform != null) {
                  Vector3d position = new Vector3d(transform.getPosition());
                  TriggerVolumeShape shape;
                  switch (shapeType) {
                     case "box":
                        shape = new BoxShape(new Vector3d(-dimension1, 0.0, -dimension3), new Vector3d(dimension1, dimension2, dimension3));
                        break;
                     case "sphere":
                        shape = new SphereShape(new Vector3d(0.0, dimension2, 0.0), dimension1);
                        break;
                     case "cylinder":
                        shape = new CylinderShape(new Vector3d(0.0, 0.0, 0.0), dimension1, dimension2);
                        break;
                     default:
                        context.sendMessage(Message.translation("server.commands.triggervolume.create.unknownShape").param("shape", shapeType));
                        return;
                  }

                  VolumeEntry entry = new VolumeEntry(name, worldName, position, shape, new ArrayList<>(), EnumSet.of(EntityTargetType.PLAYER), true);
                  manager.register(name, entry);
                  manager.notifyViewersAdd(entry);
                  context.sendMessage(Message.translation("server.commands.triggervolume.create.success").param("name", name));
               }
            }
         }
      }
   }
}
