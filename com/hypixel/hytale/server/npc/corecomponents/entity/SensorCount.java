package com.hypixel.hytale.server.npc.corecomponents.entity;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderManager;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorCount;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class SensorCount extends SensorBase {
   protected final int minCount;
   protected final int maxCount;
   protected final double minRange;
   protected final double maxRange;
   protected final int[] includeGroups;
   protected final int[] excludeGroups;
   protected boolean findPlayers;
   protected final boolean haveIncludeGroups;
   protected final boolean haveExcludeGroups;

   public SensorCount(@Nonnull BuilderSensorCount builderSensorCount, @Nonnull BuilderSupport support) {
      super(builderSensorCount);
      int[] count = builderSensorCount.getCount(support);
      this.minCount = count[0];
      this.maxCount = count[1];
      double[] range = builderSensorCount.getRange(support);
      this.minRange = range[0];
      this.maxRange = range[1];
      this.includeGroups = builderSensorCount.getIncludeGroups();
      this.excludeGroups = builderSensorCount.getExcludeGroups();
      this.haveIncludeGroups = this.includeGroups != null && this.includeGroups.length > 0;
      this.haveExcludeGroups = this.excludeGroups != null && this.excludeGroups.length > 0;
   }

   @Override
   public void registerWithSupport(@Nonnull Role role) {
      if (this.haveIncludeGroups) {
         this.findPlayers = groupListHasPlayer(this.includeGroups);
      } else {
         this.findPlayers = !this.haveExcludeGroups || !groupListHasPlayer(this.excludeGroups);
      }

      role.getPositionCache().requireEntityDistanceSorted(this.maxRange);
      if (this.findPlayers) {
         role.getPositionCache().requirePlayerDistanceSorted(this.maxRange);
      }
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      return super.matches(ref, role, dt, store)
         && role.getPositionCache()
            .isEntityCountInRange(this.minRange, this.maxRange, this.minCount, this.maxCount, this.findPlayers, role, SensorCount::filterNPC, this, store);
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }

   protected static boolean groupListHasPlayer(@Nonnull int[] groups) {
      for (int group : groups) {
         if (WorldSupport.hasTagInGroup(group, BuilderManager.getPlayerGroupID())) {
            return true;
         }
      }

      return false;
   }

   protected boolean filterNPC(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      int roleIndex = role.getRoleIndex();
      return (!this.haveIncludeGroups || WorldSupport.isGroupMember(roleIndex, ref, this.includeGroups, componentAccessor))
         && (!this.haveExcludeGroups || !WorldSupport.isGroupMember(roleIndex, ref, this.excludeGroups, componentAccessor));
   }
}
