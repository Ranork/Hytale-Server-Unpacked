package com.hypixel.hytale.builtin.mounts;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class MountedByComponent implements Component<EntityStore> {
   @Nonnull
   private final List<Ref<EntityStore>> passengers = new ObjectArrayList();

   public static ComponentType<EntityStore, MountedByComponent> getComponentType() {
      return MountPlugin.getInstance().getMountedByComponentType();
   }

   public void removeInvalid() {
      this.passengers.removeIf(v -> !v.isValid());
   }

   @Nonnull
   public List<Ref<EntityStore>> getPassengers() {
      this.removeInvalid();
      return this.passengers;
   }

   public void addPassenger(Ref<EntityStore> passenger) {
      this.passengers.add(passenger);
   }

   public void removePassenger(Ref<EntityStore> ref) {
      this.passengers.remove(ref);
   }

   @Nonnull
   public MountedByComponent withPassenger(Ref<EntityStore> passenger) {
      this.passengers.add(passenger);
      return this;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new MountedByComponent();
   }
}
