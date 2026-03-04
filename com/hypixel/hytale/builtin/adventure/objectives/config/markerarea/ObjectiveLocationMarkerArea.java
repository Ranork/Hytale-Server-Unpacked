package com.hypixel.hytale.builtin.adventure.objectives.config.markerarea;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class ObjectiveLocationMarkerArea {
   @Nonnull
   public static final CodecMapCodec<ObjectiveLocationMarkerArea> CODEC = new CodecMapCodec<>("Type");
   protected Box entryAreaBox;
   protected Box exitAreaBox;

   public abstract void getPlayersInEntryArea(
      @Nonnull SpatialResource<Ref<EntityStore>, EntityStore> var1, @Nonnull List<Ref<EntityStore>> var2, @Nonnull Vector3d var3
   );

   public abstract void getPlayersInExitArea(
      @Nonnull SpatialResource<Ref<EntityStore>, EntityStore> var1, @Nonnull List<Ref<EntityStore>> var2, @Nonnull Vector3d var3
   );

   public abstract boolean hasPlayerInExitArea(
      @Nonnull SpatialResource<Ref<EntityStore>, EntityStore> var1,
      @Nonnull ComponentType<EntityStore, PlayerRef> var2,
      @Nonnull Vector3d var3,
      @Nonnull CommandBuffer<EntityStore> var4
   );

   public abstract boolean isPlayerInEntryArea(@Nonnull Vector3d var1, @Nonnull Vector3d var2);

   public Box getBoxForEntryArea() {
      return this.entryAreaBox;
   }

   public Box getBoxForExitArea() {
      return this.exitAreaBox;
   }

   @Nonnull
   public ObjectiveLocationMarkerArea getRotatedArea(float yaw, float pitch) {
      return this;
   }

   protected abstract void computeAreaBoxes();

   @Nonnull
   @Override
   public String toString() {
      return "ObjectiveLocationMarkerArea{, entryAreaBox=" + this.entryAreaBox + ", exitAreaBox=" + this.exitAreaBox + "}";
   }

   static {
      CODEC.register("Box", ObjectiveLocationAreaBox.class, ObjectiveLocationAreaBox.CODEC);
      CODEC.register("Radius", ObjectiveLocationAreaRadius.class, ObjectiveLocationAreaRadius.CODEC);
   }
}
