package com.hypixel.hytale.server.npc.sensorinfo;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public interface IPositionProvider {
   boolean hasPosition();

   boolean providePosition(Vector3d var1);

   double getX();

   double getY();

   double getZ();

   @Nullable
   Ref<EntityStore> getTarget();

   void clear();
}
