package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterAttitude;
import com.hypixel.hytale.server.npc.role.Role;
import java.util.Arrays;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class EntityFilterAttitude extends EntityFilterBase {
   public static final String TYPE = "Attitude";
   public static final int COST = 0;
   protected final EnumSet<Attitude> attitudes;

   public EntityFilterAttitude(@Nonnull BuilderEntityFilterAttitude builder, @Nonnull BuilderSupport support) {
      this.attitudes = builder.getAttitudes(support);
   }

   public EntityFilterAttitude(Attitude[] attitudes) {
      this.attitudes = EnumSet.noneOf(Attitude.class);
      this.attitudes.addAll(Arrays.asList(attitudes));
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      Attitude attitude = role.getWorldSupport().getAttitude(ref, targetRef, store);
      return attitude != null && this.attitudes.contains(attitude);
   }

   @Override
   public int cost() {
      return 0;
   }

   @Override
   public void registerWithSupport(@Nonnull Role role) {
      role.getWorldSupport().requireAttitudeCache();
   }
}
