package com.hypixel.hytale.server.npc.blackboard.view.interaction;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public interface ReservationProvider {
   @Nonnull
   ReservationStatus getReservationStatus(@Nonnull Ref<EntityStore> var1, @Nonnull Ref<EntityStore> var2, @Nonnull ComponentAccessor<EntityStore> var3);
}
