package com.hypixel.hytale.server.core.universe.world.chunk;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractCachedAccessor {
   protected ComponentAccessor<ChunkStore> commandBuffer;
   private int minX;
   private int minY;
   private int minZ;
   private int length;
   private Ref<ChunkStore>[] chunks;
   private Ref<ChunkStore>[] sections;
   private Component<ChunkStore>[][] sectionComponents;

   protected AbstractCachedAccessor(int numComponents) {
      this.sectionComponents = new Component[numComponents][];
   }

   protected void init(ComponentAccessor<ChunkStore> commandBuffer, int cx, int cy, int cz, int radius) {
      this.commandBuffer = commandBuffer;
      radius = ChunkUtil.chunkCoordinate(radius) + 1;
      this.minX = cx - radius;
      this.minY = cy - radius;
      this.minZ = cz - radius;
      this.length = radius * 2 + 1;
      int size2d = this.length * this.length;
      int size3d = this.length * this.length * this.length;
      if (this.chunks != null && this.chunks.length >= size2d) {
         Arrays.fill(this.chunks, null);
      } else {
         this.chunks = new Ref[size2d];
      }

      for (int i = 0; i < this.sectionComponents.length; i++) {
         Component<ChunkStore>[] sectionComps = this.sectionComponents[i];
         if (sectionComps != null && sectionComps.length >= size3d) {
            Arrays.fill(sectionComps, null);
         } else {
            this.sectionComponents[i] = new Component[size3d];
         }
      }

      if (this.sections != null && this.sections.length >= size3d) {
         Arrays.fill(this.sections, null);
      } else {
         this.sections = new Ref[size3d];
      }
   }

   protected void insertSection(Ref<ChunkStore> section, int cx, int cy, int cz) {
      int x = cx - this.minX;
      int y = cy - this.minY;
      int z = cz - this.minZ;
      int index3d = x + z * this.length + y * this.length * this.length;
      if (index3d >= 0 && index3d < this.sections.length) {
         this.sections[index3d] = section;
      }
   }

   protected void insertSectionComponent(int index, Component<ChunkStore> component, int cx, int cy, int cz) {
      int x = cx - this.minX;
      int y = cy - this.minY;
      int z = cz - this.minZ;
      int index3d = x + z * this.length + y * this.length * this.length;
      if (index3d >= 0 && index3d < this.sectionComponents[index].length) {
         this.sectionComponents[index][index3d] = component;
      }
   }

   @Nullable
   public Ref<ChunkStore> getChunk(int cx, int cz) {
      int x = cx - this.minX;
      int z = cz - this.minZ;
      int index = x + z * this.length;
      if (index >= 0 && index < this.chunks.length) {
         Ref<ChunkStore> chunk = this.chunks[index];
         if (chunk == null) {
            this.chunks[index] = chunk = this.commandBuffer.getExternalData().getChunkReference(ChunkUtil.indexChunk(cx, cz));
         }

         return chunk;
      } else {
         return this.commandBuffer.getExternalData().getChunkReference(ChunkUtil.indexChunk(cx, cz));
      }
   }

   @Nullable
   public Ref<ChunkStore> getSection(int cx, int cy, int cz) {
      if (cy >= 0 && cy < 10) {
         int x = cx - this.minX;
         int y = cy - this.minY;
         int z = cz - this.minZ;
         int index = x + z * this.length + y * this.length * this.length;
         if (index >= 0 && index < this.sections.length) {
            Ref<ChunkStore> section = this.sections[index];
            if (section == null) {
               this.sections[index] = section = this.commandBuffer.getExternalData().getChunkSectionReference(this.commandBuffer, cx, cy, cz);
            }

            return section;
         } else {
            return this.commandBuffer.getExternalData().getChunkSectionReference(this.commandBuffer, cx, cy, cz);
         }
      } else {
         return null;
      }
   }

   @Nullable
   protected <T extends Component<ChunkStore>> T getComponentSection(int cx, int cy, int cz, int typeIndex, @Nonnull ComponentType<ChunkStore, T> componentType) {
      int x = cx - this.minX;
      int y = cy - this.minY;
      int z = cz - this.minZ;
      int index = x + z * this.length + y * this.length * this.length;
      if (index >= 0 && index < this.sections.length) {
         T comp = (T)this.sectionComponents[typeIndex][index];
         if (comp == null) {
            Ref<ChunkStore> section = this.getSection(cx, cy, cz);
            if (section == null) {
               return null;
            }

            this.sectionComponents[typeIndex][index] = comp = this.commandBuffer.getComponent(section, componentType);
         }

         return comp;
      } else {
         Ref<ChunkStore> section = this.getSection(cx, cy, cz);
         return section == null ? null : this.commandBuffer.getComponent(section, componentType);
      }
   }
}
