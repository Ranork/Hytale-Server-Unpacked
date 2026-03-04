package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.RoleStats;
import com.hypixel.hytale.server.npc.systems.PositionCacheSystems;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class NPCSensorStatsCommand extends AbstractPlayerCommand {
   public NPCSensorStatsCommand() {
      super("sensorstats", "server.commands.npc.sensorstats.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      NPCPlugin npcPlugin = NPCPlugin.get();
      List<String> roles = npcPlugin.getRoleTemplateNames(true);
      if (roles.isEmpty()) {
         context.sendMessage(Message.translation("server.commands.npc.sensorstats.noroles"));
      } else {
         roles.sort(String::compareToIgnoreCase);
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d pos = new Vector3d(transformComponent.getPosition());
         String name = roles.getFirst();
         int roleIndex = NPCPlugin.get().getIndex(name);
         if (roleIndex < 0) {
            throw new IllegalStateException("No such valid role: " + name);
         } else {
            Pair<Ref<EntityStore>, NPCEntity> npcPair = npcPlugin.spawnEntity(store, roleIndex, pos, null, null, null);
            NPCEntity npcComponent = (NPCEntity)npcPair.second();
            StringBuilder out = new StringBuilder();
            RoleStats roleStats = new RoleStats();

            for (int i = 0; i < roles.size(); i++) {
               String roleName = roles.get(i);

               try {
                  roleStats.clear();
                  BuilderInfo builderInfo = NPCPlugin.get().prepareRoleBuilderInfo(NPCPlugin.get().getIndex(roleName));
                  Builder<Role> roleBuilder = (Builder<Role>)builderInfo.getBuilder();
                  BuilderSupport builderSupport = new BuilderSupport(
                     NPCPlugin.get().getBuilderManager(), npcComponent, EntityStore.REGISTRY.newHolder(), new ExecutionContext(), roleBuilder, roleStats
                  );
                  Role role = NPCPlugin.buildRole(roleBuilder, builderInfo, builderSupport, roleIndex);
                  PositionCacheSystems.initialisePositionCache(role, builderSupport.getStateEvaluator(), 0.0);
               } catch (Throwable var22) {
                  context.sendMessage(Message.translation("server.commands.npc.spawn.templateNotFound").param("template", roleName));
                  npcPlugin.getLogger().at(Level.WARNING).log("Error spawning role " + roleName + ": " + var22.getMessage());
                  continue;
               }

               if (!isRangesEmpty(roleStats, true)) {
                  out.append('\n').append("PLY ");
                  formatRanges(out, roleStats, "S=", true, RoleStats.RangeType.SORTED, 25);
                  formatRanges(out, roleStats, "U=", true, RoleStats.RangeType.UNSORTED, 9);
                  formatRanges(out, roleStats, "A=", true, RoleStats.RangeType.AVOIDANCE, 9);
                  formatBuckets(out, roleStats, "B=", true, 20);
                  out.append(roleName);
               }

               if (!isRangesEmpty(roleStats, false)) {
                  out.append('\n').append("ENT ");
                  formatRanges(out, roleStats, "S=", false, RoleStats.RangeType.SORTED, 25);
                  formatRanges(out, roleStats, "U=", false, RoleStats.RangeType.UNSORTED, 9);
                  formatRanges(out, roleStats, "A=", false, RoleStats.RangeType.AVOIDANCE, 9);
                  formatBuckets(out, roleStats, "B=", false, 20);
                  out.append(roleName);
               }
            }

            npcPlugin.getLogger().at(Level.INFO).log(out.toString());
            npcComponent.remove();
         }
      }
   }

   private static boolean isRangesEmpty(@Nonnull RoleStats roleStats, boolean isPlayer) {
      return roleStats.getRanges(isPlayer, RoleStats.RangeType.SORTED) == null
         && roleStats.getRanges(isPlayer, RoleStats.RangeType.UNSORTED) == null
         && roleStats.getRanges(isPlayer, RoleStats.RangeType.AVOIDANCE) == null;
   }

   private static void formatBuckets(@Nonnull StringBuilder builder, @Nonnull RoleStats roleStats, @Nonnull String label, boolean isPlayer, int width) {
      builder.append(label);
      int length = builder.length();
      IntArrayList buckets = roleStats.getBuckets(isPlayer);

      for (int i = 0; i < buckets.size(); i++) {
         builder.append(buckets.getInt(i)).append(" ");
      }

      length = width + length - builder.length();
      if (length > 0) {
         builder.append(" ".repeat(length));
      }
   }

   private static void formatRanges(
      @Nonnull StringBuilder builder, @Nonnull RoleStats roleStats, @Nonnull String label, boolean isPlayer, @Nonnull RoleStats.RangeType rangeType, int width
   ) {
      builder.append(label);
      int length = builder.length();
      int[] ranges = roleStats.getRangesSorted(isPlayer, rangeType);
      if (ranges != null && ranges.length != 0) {
         for (int range : ranges) {
            builder.append(range).append(" ");
         }
      } else {
         builder.append("- ");
      }

      length = width + length - builder.length();
      if (length > 0) {
         builder.append(" ".repeat(length));
      }
   }
}
