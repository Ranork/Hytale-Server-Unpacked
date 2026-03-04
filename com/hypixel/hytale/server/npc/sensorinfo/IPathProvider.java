package com.hypixel.hytale.server.npc.sensorinfo;

import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.path.IPathWaypoint;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IPathProvider extends ExtraInfoProvider {
   boolean hasPath();

   @Nullable
   IPath<? extends IPathWaypoint> getPath();

   void clear();

   @Nonnull
   @Override
   default Class<IPathProvider> getType() {
      return IPathProvider.class;
   }
}
