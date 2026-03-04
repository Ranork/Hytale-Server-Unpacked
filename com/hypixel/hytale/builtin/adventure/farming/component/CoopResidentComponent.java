package com.hypixel.hytale.builtin.adventure.farming.component;

import com.hypixel.hytale.builtin.adventure.farming.FarmingPlugin;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoopResidentComponent implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<CoopResidentComponent> CODEC = BuilderCodec.builder(CoopResidentComponent.class, CoopResidentComponent::new)
      .append(new KeyedCodec<>("CoopLocation", Vector3i.CODEC), (comp, ref) -> comp.coopLocation = ref, comp -> comp.coopLocation)
      .add()
      .append(
         new KeyedCodec<>("MarkedForDespawn", BuilderCodec.BOOLEAN),
         (comp, markedForDespawn) -> comp.markedForDespawn = markedForDespawn,
         comp -> comp.markedForDespawn
      )
      .add()
      .build();
   @Nonnull
   private Vector3i coopLocation = new Vector3i();
   private boolean markedForDespawn;

   public static ComponentType<EntityStore, CoopResidentComponent> getComponentType() {
      return FarmingPlugin.get().getCoopResidentComponentType();
   }

   public void setCoopLocation(@Nonnull Vector3i coopLocation) {
      this.coopLocation = coopLocation;
   }

   @Nonnull
   public Vector3i getCoopLocation() {
      return this.coopLocation;
   }

   public void setMarkedForDespawn(boolean markedForDespawn) {
      this.markedForDespawn = markedForDespawn;
   }

   public boolean getMarkedForDespawn() {
      return this.markedForDespawn;
   }

   @Nullable
   @Override
   public Component<EntityStore> clone() {
      CoopResidentComponent component = new CoopResidentComponent();
      component.coopLocation.assign(this.coopLocation);
      component.markedForDespawn = this.markedForDespawn;
      return component;
   }
}
