package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import it.unimi.dsi.fastutil.Pair;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class NPCAllCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_NPC_ALL_NO_ROLES_TO_SPAWN = Message.translation("server.commands.npc.all.noRolesToSpawn");
   @Nonnull
   private final OptionalArg<Double> distanceArg = this.withOptionalArg("distance", "server.commands.npc.all.distance", ArgTypes.DOUBLE)
      .addValidator(Validators.greaterThan(0.0));

   public NPCAllCommand() {
      super("all", "server.commands.npc.all.desc", true);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      double distance = this.distanceArg.provided(context) ? this.distanceArg.get(context) : 4.0;
      NPCPlugin npcModule = NPCPlugin.get();
      List<String> roles = npcModule.getRoleTemplateNames(true);
      if (roles.isEmpty()) {
         playerRef.sendMessage(MESSAGE_COMMANDS_NPC_ALL_NO_ROLES_TO_SPAWN);
      } else {
         roles.sort(String::compareToIgnoreCase);
         int columns = MathUtil.ceil(Math.sqrt(roles.size()));
         double squareSideLength = (columns - 1) * distance;
         double squareSideLengthHalf = squareSideLength / 2.0;
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         double px = position.getX() - squareSideLengthHalf;
         double pz = position.getZ() - squareSideLengthHalf;
         Vector3d pos = new Vector3d();

         for (int index = 0; index < roles.size(); index++) {
            String name = roles.get(index);
            if (name != null && !name.isEmpty()) {
               try {
                  double x = px + distance * (index % columns);
                  double z = pz + distance * (index / columns);
                  double y = NPCPhysicsMath.heightOverGround(world, x, z);
                  if (!(y < 0.0)) {
                     pos.assign(x, y, z);
                     int roleIndex = npcModule.getIndex(name);
                     if (roleIndex < 0) {
                        throw new IllegalStateException("No such valid role: " + name);
                     }

                     Pair<Ref<EntityStore>, NPCEntity> npcPair = npcModule.spawnEntity(store, roleIndex, pos, null, null, null);
                     Ref<EntityStore> npcRef = (Ref<EntityStore>)npcPair.first();

                     assert npcRef != null;

                     store.putComponent(npcRef, Nameplate.getComponentType(), new Nameplate(name));
                     store.ensureComponent(npcRef, Frozen.getComponentType());
                  }
               } catch (Throwable var33) {
                  playerRef.sendMessage(Message.translation("server.commands.npc.all.failedToSpawn").param("role", name));
                  npcModule.getLogger().at(Level.WARNING).log("Error spawning NPC with role: %s", name, var33);
               }
            }
         }
      }
   }
}
