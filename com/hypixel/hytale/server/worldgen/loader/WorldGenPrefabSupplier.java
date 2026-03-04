package com.hypixel.hytale.server.worldgen.loader;

import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabLoader;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabSupplier;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.util.bounds.ChunkBounds;
import com.hypixel.hytale.server.worldgen.util.bounds.IChunkBounds;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldGenPrefabSupplier implements PrefabSupplier {
   public static final WorldGenPrefabSupplier[] EMPTY_ARRAY = new WorldGenPrefabSupplier[0];
   private final WorldGenPrefabLoader loader;
   private final String prefabKey;
   private final Path path;
   private String prefabName;
   @Nullable
   private ChunkBounds bounds;

   public WorldGenPrefabSupplier(WorldGenPrefabLoader loader, String prefabKey, Path path) {
      this.loader = loader;
      this.path = path;
      this.bounds = null;
      this.prefabKey = prefabKey;
   }

   public WorldGenPrefabLoader getLoader() {
      return this.loader;
   }

   @Nonnull
   public String getName() {
      return this.path.toString();
   }

   @Nonnull
   public String getPrefabName() {
      if (this.prefabName == null) {
         this.prefabName = PrefabLoader.resolveRelativeJsonPath(this.prefabKey, this.path, this.loader.getRootFolder());
      }

      return this.prefabName;
   }

   public Path getPath() {
      return this.path;
   }

   @Nullable
   public IPrefabBuffer get() {
      return ChunkGenerator.getResource().prefabs.get(this);
   }

   @Nonnull
   public IChunkBounds getBounds(@Nonnull IPrefabBuffer buffer) {
      if (this.bounds == null) {
         this.bounds = this.getBounds(0, 0, 0, buffer, PrefabRotation.ROTATION_0, new ChunkBounds());
      }

      return this.bounds;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         WorldGenPrefabSupplier that = (WorldGenPrefabSupplier)o;
         return this.path.equals(that.path);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.path.hashCode();
   }

   @Nonnull
   @Override
   public String toString() {
      return "WorldGenPrefabSupplier{path=" + this.path + "}";
   }

   @Nonnull
   private ChunkBounds getBounds(int depth, int x, int z, @Nonnull IPrefabBuffer prefab, @Nonnull PrefabRotation rotation, @Nonnull ChunkBounds bounds) {
      if (depth >= 10) {
         return bounds;
      } else {
         int minX = x + prefab.getMinX(rotation);
         int minZ = z + prefab.getMinZ(rotation);
         int maxX = x + prefab.getMaxX(rotation);
         int maxZ = z + prefab.getMaxZ(rotation);
         bounds.include(minX, minZ, maxX, maxZ);

         for (PrefabBuffer.ChildPrefab child : prefab.getChildPrefabs()) {
            int childX = x + rotation.getX(child.getX(), child.getZ());
            int childZ = z + rotation.getZ(child.getX(), child.getZ());

            for (WorldGenPrefabSupplier supplier : this.loader.get(child.getPath())) {
               IPrefabBuffer childPrefab = supplier.get();
               PrefabRotation childRotation = rotation.add(child.getRotation());
               this.getBounds(depth + 1, childX, childZ, childPrefab, childRotation, bounds);
            }
         }

         return bounds;
      }
   }
}
