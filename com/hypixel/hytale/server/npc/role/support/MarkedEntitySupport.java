package com.hypixel.hytale.server.npc.role.support;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MarkedEntitySupport {
   public static final String DEFAULT_TARGET_SLOT = "LockedTarget";
   @Nullable
   protected static final ComponentType<EntityStore, NPCEntity> NPC_COMPONENT_TYPE = NPCEntity.getComponentType();
   @Nonnull
   private static final Object2IntMap<String> EMPTY_TARGET_SLOT_MAP = new Object2IntOpenHashMap(0);
   protected final NPCEntity parent;
   protected Object2IntMap<String> targetSlotMappings;
   @Nullable
   protected Int2ObjectMap<String> slotToNameMap;
   protected Ref<EntityStore>[] entityTargets;
   @Nullable
   protected Vector3d[] storedPositions;
   protected int defaultTargetSlot;
   protected int targetSlotToIgnoreForAvoidance = Integer.MIN_VALUE;

   public MarkedEntitySupport(NPCEntity parent) {
      this.parent = parent;
   }

   public Ref<EntityStore>[] getEntityTargets() {
      return this.entityTargets;
   }

   public void postRoleBuilder(@Nonnull BuilderSupport support) {
      Object2IntMap<String> slotMappings = support.getTargetSlotMappings();
      if (slotMappings != null) {
         this.targetSlotMappings = slotMappings;
         this.slotToNameMap = support.getTargetSlotToNameMap();
         this.entityTargets = new Ref[this.targetSlotMappings.size()];
         this.defaultTargetSlot = this.targetSlotMappings.getInt("LockedTarget");
      } else {
         this.targetSlotMappings = EMPTY_TARGET_SLOT_MAP;
         this.slotToNameMap = null;
         this.entityTargets = (Ref<EntityStore>[])Ref.EMPTY_ARRAY;
         this.defaultTargetSlot = Integer.MIN_VALUE;
      }

      this.storedPositions = support.allocatePositionSlots();
   }

   public void clearMarkedEntity(int targetSlot) {
      this.entityTargets[targetSlot] = null;
   }

   public void setMarkedEntity(String targetSlot, Ref<EntityStore> target) {
      int slot = this.targetSlotMappings.getInt(targetSlot);
      if (slot >= 0) {
         this.setMarkedEntity(slot, target);
      }
   }

   public void setMarkedEntity(int targetSlot, @Nullable Ref<EntityStore> target) {
      if (target != null && target.isValid()) {
         this.entityTargets[targetSlot] = target;
      } else {
         this.clearMarkedEntity(targetSlot);
      }
   }

   @Nullable
   public Ref<EntityStore> getMarkedEntityRef(String targetSlot) {
      int slot = this.targetSlotMappings.getInt(targetSlot);
      return slot >= 0 ? this.getMarkedEntityRef(slot) : null;
   }

   @Nullable
   public Ref<EntityStore> getMarkedEntityRef(int targetSlot) {
      Ref<EntityStore> ref = this.entityTargets[targetSlot];
      return ref != null && ref.isValid() ? ref : null;
   }

   public int getMarkedEntitySlotCount() {
      return this.entityTargets.length;
   }

   public Vector3d getStoredPosition(int slot) {
      return this.storedPositions[slot];
   }

   public boolean hasMarkedEntity(@Nonnull Ref<EntityStore> entityReference, int targetSlot) {
      return entityReference.equals(this.getMarkedEntityRef(targetSlot));
   }

   public boolean hasMarkedEntityInSlot(String targetSlot) {
      int slot = this.targetSlotMappings.getInt(targetSlot);
      return slot < 0 ? false : this.hasMarkedEntityInSlot(slot);
   }

   public boolean hasMarkedEntityInSlot(int targetSlot) {
      return this.getMarkedEntityRef(targetSlot) != null;
   }

   public void flockSetTarget(@Nonnull String targetSlot, @Nullable Ref<EntityStore> targetRef, @Nonnull Store<EntityStore> store) {
      Ref<EntityStore> parentRef = this.parent.getReference();
      Ref<EntityStore> flockReference = FlockPlugin.getFlockReference(parentRef, store);
      if (flockReference != null) {
         store.getComponent(flockReference, EntityGroup.getComponentType()).forEachMember((member, sender, _target, _targetSlot) -> {
            NPCEntity npcComponent = member.getStore().getComponent(member, NPC_COMPONENT_TYPE);
            if (npcComponent != null) {
               npcComponent.onFlockSetTarget(_targetSlot, _target);
            }
         }, parentRef, targetRef, targetSlot);
      }
   }

   public void setTargetSlotToIgnoreForAvoidance(int targetSlotToIgnoreForAvoidance) {
      this.targetSlotToIgnoreForAvoidance = targetSlotToIgnoreForAvoidance;
   }

   @Nullable
   public Ref<EntityStore> getTargetReferenceToIgnoreForAvoidance() {
      int slot = this.targetSlotToIgnoreForAvoidance >= 0 ? this.targetSlotToIgnoreForAvoidance : this.defaultTargetSlot;
      return slot < 0 ? null : this.getMarkedEntityRef(slot);
   }

   public String getSlotName(int slot) {
      return (String)this.slotToNameMap.get(slot);
   }

   public void unloaded() {
      for (int i = 0; i < this.entityTargets.length; i++) {
         this.clearMarkedEntity(i);
      }
   }

   static {
      EMPTY_TARGET_SLOT_MAP.defaultReturnValue(Integer.MIN_VALUE);
   }
}
