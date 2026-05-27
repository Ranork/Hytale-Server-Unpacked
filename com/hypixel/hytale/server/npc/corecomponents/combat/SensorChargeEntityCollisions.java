package com.hypixel.hytale.server.npc.corecomponents.combat;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.builders.BuilderSensorChargeEntityCollisions;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Instruction;
import com.hypixel.hytale.server.npc.movement.controllers.EntityHit;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.EntityCollisionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SensorChargeEntityCollisions extends SensorBase {
   private final EntityCollisionProvider entityCollisionProvider = new EntityCollisionProvider();
   private final boolean getPlayers;
   private final boolean getNPCs;
   private final boolean excludeOwnType;
   private final List<EntityHit> filteredHits = new ObjectArrayList();
   @Nullable
   private BodyMotionCharge matchingChargeBodyMotion;

   public SensorChargeEntityCollisions(@Nonnull BuilderSensorChargeEntityCollisions builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.getPlayers = builder.isGetPlayers(support);
      this.getNPCs = builder.isGetNPCs(support);
      this.excludeOwnType = builder.isExcludeOwnType(support);
   }

   @Override
   public void loaded(Role role) {
      if (this.parent instanceof Instruction instruction) {
         this.matchingChargeBodyMotion = instruction.findNearestPrecedingBodyMotion(BodyMotionCharge.class);
      }
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      this.filteredHits.clear();
      this.entityCollisionProvider.clear();
      if (super.matches(ref, role, dt, store) && this.matchingChargeBodyMotion != null && this.matchingChargeBodyMotion.getEntityHitCount() != 0) {
         int ownRoleIndex = role.getRoleIndex();
         int hitCount = this.matchingChargeBodyMotion.getEntityHitCount();

         for (int i = 0; i < hitCount; i++) {
            EntityHit hit = this.matchingChargeBodyMotion.getEntityHit(i);
            if (this.shouldIncludeHit(hit, ownRoleIndex, store)) {
               this.filteredHits.add(hit);
            }
         }

         return this.entityCollisionProvider.populate(this.filteredHits, this.filteredHits.size());
      } else {
         return false;
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.entityCollisionProvider;
   }

   @Override
   public void done() {
      super.done();
      this.entityCollisionProvider.clear();
      this.filteredHits.clear();
   }

   private boolean shouldIncludeHit(@Nonnull EntityHit hit, int ownRoleIndex, @Nonnull Store<EntityStore> store) {
      Ref<EntityStore> targetRef = hit.entity;
      if (targetRef == null || !targetRef.isValid()) {
         return false;
      } else if (hit.isPlayer) {
         return this.getPlayers;
      } else if (!this.getNPCs) {
         return false;
      } else {
         NPCEntity npcComponent = store.getComponent(targetRef, NPCEntity.getComponentType());
         if (npcComponent == null) {
            return false;
         } else {
            return !this.excludeOwnType ? true : npcComponent.getRoleIndex() != ownRoleIndex;
         }
      }
   }
}
