package com.hypixel.hytale.builtin.path.waypoint;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.path.IPathWaypoint;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public interface IPrefabPathWaypoint extends IPathWaypoint {
   void onReplaced();

   void initialise(@Nonnull UUID var1, @Nonnull String var2, int var3, double var4, float var6, int var7, @Nonnull ComponentAccessor<EntityStore> var8);

   IPath<IPrefabPathWaypoint> getParentPath();
}
