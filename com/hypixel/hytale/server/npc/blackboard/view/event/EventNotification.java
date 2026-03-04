package com.hypixel.hytale.server.npc.blackboard.view.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EventNotification {
   private final Vector3d position = new Vector3d();
   private Ref<EntityStore> initiator;
   private int set;

   @Nonnull
   public Vector3d getPosition() {
      return this.position;
   }

   public void setPosition(double x, double y, double z) {
      this.position.assign(x, y, z);
   }

   public Ref<EntityStore> getInitiator() {
      return this.initiator;
   }

   public void setInitiator(Ref<EntityStore> initiator) {
      this.initiator = initiator;
   }

   public int getSet() {
      return this.set;
   }

   public void setSet(int set) {
      this.set = set;
   }
}
