package com.hypixel.hytale.server.npc.entities;

import com.hypixel.hytale.builtin.path.WorldPathData;
import com.hypixel.hytale.builtin.path.path.IPrefabPath;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.component.WorldGenId;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PathManager {
   @Nonnull
   public static final BuilderCodec<PathManager> CODEC = BuilderCodec.builder(PathManager.class, PathManager::new)
      .append(new KeyedCodec<>("CurrentPath", Codec.UUID_BINARY), (npcEntity, uuid) -> npcEntity.currentPathHint = uuid, npcEntity -> npcEntity.currentPathHint)
      .setVersionRange(5, 5)
      .add()
      .build();
   @Nullable
   private UUID currentPathHint;
   @Nullable
   private IPath<?> currentPath;

   public void setPrefabPath(@Nonnull UUID currentPath, @Nonnull IPrefabPath path) {
      this.currentPathHint = currentPath;
      this.currentPath = path;
   }

   public void setTransientPath(@Nonnull IPath<?> path) {
      this.currentPathHint = null;
      this.currentPath = path;
   }

   public boolean isFollowingPath() {
      return this.currentPathHint != null || this.currentPath != null;
   }

   @Nullable
   public UUID getCurrentPathHint() {
      return this.currentPathHint;
   }

   @Nullable
   public IPath<?> getPath(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.currentPath == null) {
         if (this.currentPathHint == null) {
            return null;
         }

         WorldPathData worldPathData = componentAccessor.getResource(WorldPathData.getResourceType());
         WorldGenId worldGenIdComponent = componentAccessor.getComponent(ref, WorldGenId.getComponentType());
         int worldGenId = worldGenIdComponent != null ? worldGenIdComponent.getWorldGenId() : 0;
         this.currentPath = worldPathData.getPrefabPath(worldGenId, this.currentPathHint, false);
      }

      return this.currentPath;
   }
}
