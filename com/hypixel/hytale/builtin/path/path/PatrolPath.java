package com.hypixel.hytale.builtin.path.path;

import com.hypixel.fastutil.ints.Int2ObjectConcurrentHashMap;
import com.hypixel.hytale.builtin.path.PathPlugin;
import com.hypixel.hytale.builtin.path.entities.PatrolPathMarkerEntity;
import com.hypixel.hytale.builtin.path.waypoint.IPrefabPathWaypoint;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class PatrolPath implements IPrefabPath {
   private final UUID id;
   private final String name;
   private final int worldgenId;
   private final Int2ObjectConcurrentHashMap<IPrefabPathWaypoint> waypoints = new Int2ObjectConcurrentHashMap<>();
   private final AtomicInteger length = new AtomicInteger(0);
   private final AtomicInteger loadedCount = new AtomicInteger(0);
   private final AtomicBoolean pathChanged = new AtomicBoolean(false);
   private final ReentrantReadWriteLock listLock = new ReentrantReadWriteLock();
   private List<IPrefabPathWaypoint> waypointList;

   public PatrolPath(int worldgenId, UUID id, String name) {
      this.id = id;
      this.worldgenId = worldgenId;
      this.name = name;
   }

   @Override
   public UUID getId() {
      return this.id;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Nonnull
   @Override
   public List<IPrefabPathWaypoint> getPathWaypoints() {
      if (this.pathChanged.get()) {
         this.listLock.writeLock().lock();

         try {
            this.waypointList = new ObjectArrayList();
            int size = this.length.get();

            for (int i = 0; i < size; i++) {
               this.waypointList.add(this.waypoints.get(i));
            }

            this.pathChanged.set(false);
         } finally {
            this.listLock.writeLock().unlock();
         }
      }

      this.listLock.readLock().lock();

      List var11;
      try {
         var11 = Collections.unmodifiableList(this.waypointList);
      } finally {
         this.listLock.readLock().unlock();
      }

      return var11;
   }

   @Override
   public short registerNewWaypoint(@Nonnull IPrefabPathWaypoint waypoint, int worldGenId) {
      short index = (short)this.length.getAndIncrement();
      PathPlugin.get().getLogger().at(Level.FINER).log("Adding waypoint %s to path %s.%s", index, worldGenId, this.name);

      for (int i = 0; i < index; i++) {
         PatrolPathMarkerEntity wp = (PatrolPathMarkerEntity)this.waypoints.get(i);
         if (wp != null) {
            wp.markNeedsSave();
         }
      }

      this.pathChanged.set(true);
      return index;
   }

   @Override
   public void registerNewWaypointAt(int index, @Nonnull IPrefabPathWaypoint waypoint, int worldGenId) {
      for (int i = 0; i < index; i++) {
         PatrolPathMarkerEntity wp = (PatrolPathMarkerEntity)this.waypoints.get(i);
         if (wp != null) {
            wp.markNeedsSave();
         }
      }

      for (int ix = this.waypoints.size() - 1; ix >= index; ix--) {
         PatrolPathMarkerEntity wp = (PatrolPathMarkerEntity)this.waypoints.remove(ix);
         if (wp == null) {
            this.waypoints.remove(ix + 1);
         } else {
            wp.setOrder((short)(ix + 1));
            this.waypoints.put(ix + 1, wp);
         }
      }

      this.length.getAndIncrement();
      this.pathChanged.set(true);
   }

   @Override
   public void addLoadedWaypoint(@Nonnull IPrefabPathWaypoint waypoint, int pathLength, int index, int worldGenId) {
      PathPlugin.get().getLogger().at(Level.FINER).log("Loading waypoint %s to path %s.%s", index, worldGenId, this.name);
      IPrefabPathWaypoint old = this.waypoints.put(index, waypoint);
      if (old != null) {
         old.onReplaced();
         PathPlugin.get().getLogger().at(Level.WARNING).log("Waypoint %s replaced in path %s.%s", index, worldGenId, this.name);
      } else {
         this.loadedCount.getAndIncrement();
      }

      this.length.set(pathLength);
      this.pathChanged.set(true);
   }

   @Override
   public void removeWaypoint(int index, int worldGenId) {
      this.waypoints.remove(index);
      this.length.getAndDecrement();
      this.loadedCount.getAndDecrement();

      for (int i = 0; i < index; i++) {
         PatrolPathMarkerEntity wp = (PatrolPathMarkerEntity)this.waypoints.get(i);
         if (wp != null) {
            wp.markNeedsSave();
         }
      }

      for (int ix = index; ix < this.waypoints.size(); ix++) {
         PatrolPathMarkerEntity wp = (PatrolPathMarkerEntity)this.waypoints.remove(ix + 1);
         if (wp == null) {
            this.waypoints.remove(ix);
         } else {
            wp.setOrder(ix);
            this.waypoints.put(ix, wp);
         }
      }

      this.pathChanged.set(true);
   }

   @Override
   public void unloadWaypoint(int index) {
      this.waypoints.remove(index);
      this.loadedCount.getAndDecrement();
   }

   @Override
   public boolean hasLoadedWaypoints() {
      return this.loadedCount.get() > 0;
   }

   @Override
   public boolean isFullyLoaded() {
      return this.loadedCount.get() == this.length.get();
   }

   @Override
   public int loadedWaypointCount() {
      return this.loadedCount.get();
   }

   @Override
   public int getWorldGenId() {
      return this.worldgenId;
   }

   @Override
   public Vector3d getNearestWaypointPosition(@Nonnull Vector3d origin, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Vector3d nearest = Vector3d.MAX;
      double minDist2 = Double.MAX_VALUE;

      for (int i = 0; i < this.length.get(); i++) {
         IPrefabPathWaypoint wp = this.waypoints.get(i);
         if (wp != null) {
            double dist2 = origin.distanceSquaredTo(wp.getWaypointPosition(componentAccessor));
            if (dist2 < minDist2) {
               nearest = wp.getWaypointPosition(componentAccessor);
               minDist2 = dist2;
            }
         }
      }

      return nearest;
   }

   @Override
   public void mergeInto(@Nonnull IPrefabPath target, int worldGenId, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      for (int i = 0; i < this.length.get(); i++) {
         IPrefabPathWaypoint waypoint = this.waypoints.get(i);
         waypoint.initialise(target.getId(), target.getName(), -1, waypoint.getPauseTime(), waypoint.getObservationAngle(), worldGenId, componentAccessor);
         target.addLoadedWaypoint(waypoint, target.length(), waypoint.getOrder(), worldGenId);
      }

      this.waypoints.clear();
      this.loadedCount.set(0);
      this.length.set(0);
      this.pathChanged.set(true);
   }

   @Override
   public void compact(int worldGenId) {
      short length = 0;

      for (int i = 0; i < this.length.get(); i++) {
         PatrolPathMarkerEntity wp = (PatrolPathMarkerEntity)this.waypoints.remove(i);
         if (wp != null) {
            wp.setOrder(length);
            this.waypoints.put(length++, wp);
         }
      }

      PathPlugin.get().getLogger().at(Level.WARNING).log("Compacted path %s.%s from length %s to %s", worldGenId, this.name, this.length.get(), length);
      this.loadedCount.set(length);
      this.length.set(length);
      this.pathChanged.set(true);
   }

   @Override
   public int length() {
      return this.length.get();
   }

   public IPrefabPathWaypoint get(int index) {
      if (index >= 0 && index < this.length.get()) {
         return this.waypoints.get(index);
      } else {
         throw new IndexOutOfBoundsException();
      }
   }
}
