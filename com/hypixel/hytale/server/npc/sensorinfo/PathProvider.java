package com.hypixel.hytale.server.npc.sensorinfo;

import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.path.IPathWaypoint;
import javax.annotation.Nullable;

public class PathProvider implements IPathProvider {
   @Nullable
   private IPath<? extends IPathWaypoint> path;
   private boolean isValid;

   public void setPath(IPath<? extends IPathWaypoint> path) {
      this.path = path;
      this.isValid = true;
   }

   @Override
   public void clear() {
      this.path = null;
      this.isValid = false;
   }

   @Override
   public boolean hasPath() {
      return this.isValid;
   }

   @Nullable
   @Override
   public IPath<? extends IPathWaypoint> getPath() {
      return this.path;
   }
}
