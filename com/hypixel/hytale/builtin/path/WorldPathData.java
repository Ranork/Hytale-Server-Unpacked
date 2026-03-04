package com.hypixel.hytale.builtin.path;

import com.hypixel.fastutil.ints.Int2ObjectConcurrentHashMap;
import com.hypixel.hytale.builtin.path.path.IPrefabPath;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldPathData implements Resource<EntityStore> {
   private final Int2ObjectMap<PrefabPathCollection> prefabPaths = new Int2ObjectOpenHashMap();

   public static ResourceType<EntityStore, WorldPathData> getResourceType() {
      return PathPlugin.get().getWorldPathDataResourceType();
   }

   @Nullable
   public IPrefabPath getNearestPrefabPath(
      int worldgenId, int nameIndex, @Nonnull Vector3d position, Set<UUID> disallowedPaths, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      PrefabPathCollection entry = this.getPrefabPathCollection(worldgenId);
      return entry.getNearestPrefabPath(nameIndex, position, disallowedPaths, componentAccessor);
   }

   @Nullable
   public IPrefabPath getNearestPrefabPath(
      int worldgenId, @Nonnull Vector3d position, Set<UUID> disallowedPaths, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return this.getPrefabPathCollection(worldgenId).getNearestPrefabPath(position, disallowedPaths, componentAccessor);
   }

   public IPrefabPath getOrConstructPrefabPath(
      int worldgenId, @Nonnull UUID id, @Nonnull String name, @Nonnull Int2ObjectConcurrentHashMap.IntBiObjFunction<UUID, String, IPrefabPath> pathGenerator
   ) {
      PrefabPathCollection entry = this.getPrefabPathCollection(worldgenId);
      return entry.getOrConstructPath(id, name, pathGenerator);
   }

   public void removePrefabPathWaypoint(int worldgenId, UUID id, int index) {
      PrefabPathCollection entry = this.getPrefabPathCollection(worldgenId);
      entry.removePathWaypoint(id, index);
      if (entry.isEmpty()) {
         this.prefabPaths.remove(worldgenId);
      }
   }

   public void unloadPrefabPathWaypoint(int worldgenId, UUID id, int index) {
      PrefabPathCollection entry = this.getPrefabPathCollection(worldgenId);
      entry.unloadPathWaypoint(id, index);
      if (entry.isEmpty()) {
         this.prefabPaths.remove(worldgenId);
      }
   }

   public void removePrefabPath(int worldgenId, UUID id) {
      PrefabPathCollection entry = this.getPrefabPathCollection(worldgenId);
      entry.removePath(id);
      if (entry.isEmpty()) {
         this.prefabPaths.remove(worldgenId);
      }
   }

   @Nullable
   public IPrefabPath getPrefabPath(int worldgenId, UUID id, boolean ignoreLoadState) {
      PrefabPathCollection collection = this.getPrefabPathCollection(worldgenId);
      IPrefabPath path = collection.getPath(id);
      return ignoreLoadState || path != null && path.isFullyLoaded() ? path : null;
   }

   public void compactPrefabPath(int worldgenId, UUID id) {
      IPrefabPath path = this.getPrefabPath(worldgenId, id, true);
      if (path != null) {
         path.compact(worldgenId);
      }
   }

   @Nonnull
   public List<IPrefabPath> getAllPrefabPaths() {
      ObjectArrayList<IPrefabPath> list = new ObjectArrayList();
      this.prefabPaths.forEach((id, entry) -> entry.forEach((key, path) -> list.add(path)));
      return Collections.unmodifiableList(list);
   }

   @Nonnull
   public PrefabPathCollection getPrefabPathCollection(int worldgenId) {
      return (PrefabPathCollection)this.prefabPaths.computeIfAbsent(worldgenId, PrefabPathCollection::new);
   }

   @Override
   public Resource<EntityStore> clone() {
      throw new UnsupportedOperationException("Not implemented!");
   }
}
