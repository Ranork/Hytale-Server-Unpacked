package com.hypixel.hytale.server.worldgen.cave;

import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.util.bounds.WorldBounds;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Cave {
   private static final Comparator<CaveNode> ORDER = Comparator.comparingInt(o -> o.getCaveNodeType().getPriority());
   @Nullable
   private Long2ObjectMap<List<CaveNode>> rawChunkNodeMap = new Long2ObjectOpenHashMap();
   private Long2ObjectMap<CaveNode[]> chunkNodeMap;
   private final CaveType caveType;
   @Nonnull
   private final WorldBounds bounds;
   private int nodeCount;

   public Cave(CaveType caveType) {
      this.caveType = caveType;
      this.bounds = new WorldBounds();
      this.nodeCount = 0;
   }

   public long getNodeCount() {
      return this.nodeCount;
   }

   @Nonnull
   public CaveType getCaveType() {
      return this.caveType;
   }

   @Nonnull
   public WorldBounds getBounds() {
      return this.bounds;
   }

   public void addNode(@Nonnull CaveNode element) {
      element.compile();
      this.bounds.include(element.getBounds());
      element.forEachChunk(chunk -> {
         List<CaveNode> nodes = (List<CaveNode>)this.rawChunkNodeMap.get(chunk);
         if (nodes == null) {
            this.rawChunkNodeMap.put(chunk, nodes = new ArrayList<>());
         }

         nodes.add(element);
      });
      this.nodeCount++;
   }

   public boolean contains(long chunkIndex) {
      return this.chunkNodeMap.containsKey(chunkIndex);
   }

   public CaveNode[] getCaveNodes(long chunkIndex) {
      return (CaveNode[])this.chunkNodeMap.get(chunkIndex);
   }

   public void compile() {
      this.compileNodeMap();
   }

   private void compileNodeMap() {
      this.chunkNodeMap = new Long2ObjectOpenHashMap();
      ObjectIterator var1 = this.rawChunkNodeMap.long2ObjectEntrySet().iterator();

      while (var1.hasNext()) {
         Entry<List<CaveNode>> entry = (Entry<List<CaveNode>>)var1.next();
         CaveNode[] array = ((List)entry.getValue()).toArray(CaveNode[]::new);
         ObjectArrays.mergeSort(array, ORDER);
         this.chunkNodeMap.put(entry.getLongKey(), array);
      }

      this.rawChunkNodeMap = null;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Cave{rawChunkNodeMap="
         + this.rawChunkNodeMap
         + ", chunkNodeMap="
         + this.chunkNodeMap
         + ", caveType="
         + this.caveType
         + ", bounds="
         + this.bounds
         + ", nodeCount="
         + this.nodeCount
         + "}";
   }
}
