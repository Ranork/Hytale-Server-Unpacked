package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.EntitySnapshot;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class SnapshotBuffer implements Component<EntityStore> {
   private EntitySnapshot[] snapshots;
   private int currentTickIndex = Integer.MIN_VALUE;
   private int oldestTickIndex = Integer.MIN_VALUE;
   private int currentIndex = -1;
   private int storedCount = 0;

   public static ComponentType<EntityStore, SnapshotBuffer> getComponentType() {
      return EntityModule.get().getSnapshotBufferComponentType();
   }

   @Nonnull
   public EntitySnapshot getSnapshotClamped(int tickIndex) {
      if (this.currentIndex == -1) {
         throw new IllegalStateException("Snapshots not initialized");
      } else {
         int relIndex = tickIndex - this.currentTickIndex;
         int maxRel = this.oldestTickIndex - this.currentTickIndex;
         if (relIndex > 0) {
            throw new IllegalArgumentException("Tick index is in the future");
         } else {
            if (relIndex < maxRel) {
               relIndex = maxRel;
            }

            return this.getSnapshotRelative(relIndex);
         }
      }
   }

   @Nullable
   public EntitySnapshot getSnapshot(int tickIndex) {
      if (this.currentIndex == -1) {
         return null;
      } else {
         int relIndex = tickIndex - this.currentTickIndex;
         int maxRel = this.oldestTickIndex - this.currentTickIndex;
         if (relIndex > 0) {
            throw new IllegalArgumentException("Tick index is in the future");
         } else {
            return relIndex < maxRel ? null : this.getSnapshotRelative(relIndex);
         }
      }
   }

   private EntitySnapshot getSnapshotRelative(int relIndex) {
      int index = this.currentIndex + relIndex;
      index = (this.snapshots.length + index) % this.snapshots.length;
      return this.snapshots[index];
   }

   public void storeSnapshot(int tickIndex, @Nonnull Vector3d position, @Nonnull Rotation3f bodyRotation) {
      if (this.currentIndex != -1 && this.currentTickIndex == tickIndex - 1) {
         this.currentTickIndex = tickIndex;
         if (++this.currentIndex == this.snapshots.length) {
            this.currentIndex = 0;
         }

         if (this.storedCount < this.snapshots.length) {
            this.storedCount++;
         } else {
            this.oldestTickIndex++;
         }

         this.snapshots[this.currentIndex].init(position, bodyRotation);
      } else {
         if (this.currentIndex != -1) {
            this.currentIndex = -1;
            this.currentTickIndex = Integer.MIN_VALUE;
            this.oldestTickIndex = Integer.MIN_VALUE;
            this.storedCount = 0;
         }

         this.oldestTickIndex = tickIndex;
         this.currentTickIndex = tickIndex;
         this.currentIndex = 0;
         this.storedCount = 1;
         this.snapshots[0].init(position, bodyRotation);
      }
   }

   public void resize(int newLength) {
      if (newLength <= 0) {
         throw new IllegalArgumentException("New size is too small: " + newLength);
      } else if (this.snapshots == null || newLength != this.snapshots.length) {
         if (this.snapshots == null) {
            this.snapshots = new EntitySnapshot[newLength];

            for (int i = 0; i < this.snapshots.length; i++) {
               this.snapshots[i] = new EntitySnapshot();
            }
         } else {
            int oldLength = this.snapshots.length;
            this.snapshots = Arrays.copyOf(this.snapshots, newLength);

            for (int i = oldLength; i < newLength; i++) {
               this.snapshots[i] = new EntitySnapshot();
            }
         }

         this.currentIndex = -1;
         this.currentTickIndex = Integer.MIN_VALUE;
         this.oldestTickIndex = Integer.MIN_VALUE;
         this.storedCount = 0;
      }
   }

   public boolean isInitialized() {
      return this.currentIndex != -1;
   }

   public int getCurrentTickIndex() {
      return this.currentTickIndex;
   }

   public int getOldestTickIndex() {
      return this.oldestTickIndex;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      SnapshotBuffer buffer = new SnapshotBuffer();
      if (this.snapshots != null && this.currentIndex != -1) {
         buffer.resize(this.snapshots.length);

         for (int i = this.oldestTickIndex; i <= this.currentTickIndex; i++) {
            EntitySnapshot snap = this.getSnapshot(i);
            buffer.storeSnapshot(i, snap.getPosition(), snap.getBodyRotation());
         }

         return buffer;
      } else {
         return buffer;
      }
   }
}
