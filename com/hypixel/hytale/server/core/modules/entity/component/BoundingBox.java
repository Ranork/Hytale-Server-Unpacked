package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.server.core.asset.type.model.config.DetailBox;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BoundingBox implements Component<EntityStore> {
   private final Box boundingBox = new Box();
   @Nullable
   private Box baseModelBox;
   protected Map<String, DetailBox[]> detailBoxes;

   public static ComponentType<EntityStore, BoundingBox> getComponentType() {
      return EntityModule.get().getBoundingBoxComponentType();
   }

   public BoundingBox() {
   }

   public BoundingBox(@Nonnull Box boundingBox) {
      this.setBoundingBox(boundingBox);
   }

   @Nonnull
   public Box getBoundingBox() {
      return this.boundingBox;
   }

   public void setBoundingBox(@Nonnull Box boundingBox) {
      this.boundingBox.assign(boundingBox);
   }

   public void setBaseModelBox(@Nullable Box baseModelBox) {
      this.baseModelBox = baseModelBox != null ? baseModelBox.clone() : null;
   }

   public void applyRotation(float pitch, float yaw, float roll) {
      if (this.baseModelBox != null) {
         this.boundingBox.assign(this.baseModelBox.enclosingRotatedAABB(pitch, yaw, roll));
      }
   }

   public Map<String, DetailBox[]> getDetailBoxes() {
      return this.detailBoxes;
   }

   public void setDetailBoxes(Map<String, DetailBox[]> detailBoxes) {
      this.detailBoxes = detailBoxes;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      BoundingBox copy = new BoundingBox(this.boundingBox);
      copy.baseModelBox = this.baseModelBox != null ? this.baseModelBox.clone() : null;
      return copy;
   }
}
