package com.hypixel.hytale.server.flock.corecomponents;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.flock.FlockMembershipSystems;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderEntityFilterFlock;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.movement.FlockMembershipType;
import com.hypixel.hytale.server.npc.movement.FlockPlayerMembership;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;

public class EntityFilterFlock extends EntityFilterBase {
   public static final int COST = 100;
   protected static final ComponentType<EntityStore, FlockMembership> FLOCK_MEMBERSHIP_COMPONENT_TYPE = FlockMembership.getComponentType();
   protected static final ComponentType<EntityStore, Player> PLAYER_COMPONENT_TYPE = Player.getComponentType();
   protected static final ComponentType<EntityStore, EntityGroup> ENTITY_GROUP_COMPONENT_TYPE = EntityGroup.getComponentType();
   protected final FlockMembershipType flockMembership;
   protected final FlockPlayerMembership flockPlayerMembership;
   protected final int[] size;
   protected final boolean checkCanJoin;

   public EntityFilterFlock(@Nonnull BuilderEntityFilterFlock builder) {
      this.flockMembership = builder.getFlockMembership();
      this.flockPlayerMembership = builder.getFlockPlayerMembership();
      this.size = builder.getSize();
      this.checkCanJoin = builder.isCheckCanJoin();
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      FlockMembership membership = store.getComponent(targetRef, FLOCK_MEMBERSHIP_COMPONENT_TYPE);
      switch (this.flockMembership) {
         case Leader:
            if (membership == null || !membership.getMembershipType().isActingAsLeader()) {
               return false;
            }
            break;
         case Follower:
            if (membership == null || membership.getMembershipType().isActingAsLeader()) {
               return false;
            }
            break;
         case Member:
            if (membership == null) {
               return false;
            }
            break;
         case NotMember:
            if (membership != null) {
               return false;
            }
         case Any:
            break;
         default:
            throw new MatchException(null, null);
      }

      EntityGroup group = null;
      if (membership != null) {
         Ref<EntityStore> flockReference = membership.getFlockRef();
         if (flockReference != null && flockReference.isValid()) {
            group = store.getComponent(flockReference, ENTITY_GROUP_COMPONENT_TYPE);
         }
      }

      if (this.size != null && group != null && (group.size() < this.size[0] || group.size() > this.size[1])) {
         return false;
      } else if (!this.checkCanJoin || membership != null && FlockMembershipSystems.canJoinFlock(targetRef, membership.getFlockRef(), store)) {
         Ref<EntityStore> leaderRef = group != null ? group.getLeaderRef() : null;
         boolean leaderIsPlayer = leaderRef != null && leaderRef.isValid() && store.getArchetype(leaderRef).contains(PLAYER_COMPONENT_TYPE);

         return switch (this.flockPlayerMembership) {
            case Member -> leaderIsPlayer;
            case NotMember -> !leaderIsPlayer;
            case Any -> true;
         };
      } else {
         return false;
      }
   }

   @Override
   public int cost() {
      return 100;
   }
}
