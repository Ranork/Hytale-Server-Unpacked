package com.hypixel.hytale.component.dependency;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.system.ISystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DependencyGraph<ECS_TYPE> {
   @Nonnull
   private final ISystem<ECS_TYPE>[] systems;
   @Nonnull
   private final Map<ISystem<ECS_TYPE>, List<DependencyGraph.Edge<ECS_TYPE>>> beforeSystemEdges = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Map<ISystem<ECS_TYPE>, List<DependencyGraph.Edge<ECS_TYPE>>> afterSystemEdges = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Map<ISystem<ECS_TYPE>, Set<DependencyGraph.Edge<ECS_TYPE>>> afterSystemUnfulfilledEdges = new Object2ObjectOpenHashMap();
   private DependencyGraph.Edge<ECS_TYPE>[] edges = DependencyGraph.Edge.emptyArray();

   public DependencyGraph(@Nonnull ISystem<ECS_TYPE>[] systems) {
      this.systems = systems;

      for (int i = 0; i < systems.length; i++) {
         ISystem<ECS_TYPE> system = systems[i];
         this.beforeSystemEdges.put(system, new ObjectArrayList());
         this.afterSystemEdges.put(system, new ObjectArrayList());
         this.afterSystemUnfulfilledEdges.put(system, new HashSet<>());
      }
   }

   @Nonnull
   public ISystem<ECS_TYPE>[] getSystems() {
      return this.systems;
   }

   public void resolveEdges(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      for (ISystem<ECS_TYPE> system : this.systems) {
         for (Dependency<ECS_TYPE> dependency : system.getDependencies()) {
            dependency.resolveGraphEdge(registry, system, this);
         }

         if (system.getGroup() != null) {
            for (Dependency<ECS_TYPE> dependency : system.getGroup().getDependencies()) {
               dependency.resolveGraphEdge(registry, system, this);
            }
         }
      }

      for (ISystem<ECS_TYPE> system : this.systems) {
         if (this.afterSystemEdges.get(system).isEmpty()) {
            int priority = 0;
            List<DependencyGraph.Edge<ECS_TYPE>> edges = this.beforeSystemEdges.get(system);

            for (DependencyGraph.Edge<ECS_TYPE> edge : edges) {
               priority += edge.priority / edges.size();
            }

            this.addEdgeFromRoot(system, priority);
         }
      }
   }

   public void addEdgeFromRoot(@Nonnull ISystem<ECS_TYPE> afterSystem, int priority) {
      this.addEdge(new DependencyGraph.Edge<>(null, afterSystem, priority));
   }

   public void addEdge(@Nonnull ISystem<ECS_TYPE> beforeSystem, @Nonnull ISystem<ECS_TYPE> afterSystem, int priority) {
      this.addEdge(new DependencyGraph.Edge<>(beforeSystem, afterSystem, priority));
   }

   public void addEdge(@Nonnull DependencyGraph.Edge<ECS_TYPE> edge) {
      int index = Arrays.binarySearch(this.edges, edge);
      int insertionPoint;
      if (index >= 0) {
         insertionPoint = index;

         while (insertionPoint < this.edges.length && this.edges[insertionPoint].priority == edge.priority) {
            insertionPoint++;
         }
      } else {
         insertionPoint = -(index + 1);
      }

      int oldLength = this.edges.length;
      int newLength = oldLength + 1;
      if (oldLength < newLength) {
         this.edges = Arrays.copyOf(this.edges, newLength);
      }

      System.arraycopy(this.edges, insertionPoint, this.edges, insertionPoint + 1, oldLength - insertionPoint);
      this.edges[insertionPoint] = edge;
      if (edge.beforeSystem != null) {
         this.beforeSystemEdges.get(edge.beforeSystem).add(edge);
      }

      this.afterSystemEdges.get(edge.afterSystem).add(edge);
      if (!edge.fulfilled) {
         this.afterSystemUnfulfilledEdges.get(edge.afterSystem).add(edge);
      }
   }

   public void sort(@Nonnull ISystem<ECS_TYPE>[] sortedSystems) {
      int index = 0;

      label52:
      while (index < this.systems.length) {
         for (DependencyGraph.Edge<ECS_TYPE> edge : this.edges) {
            if (!edge.resolved && edge.fulfilled) {
               ISystem<ECS_TYPE> system = edge.afterSystem;
               if (this.afterSystemUnfulfilledEdges.get(system).isEmpty() && !this.hasEdgeOfLaterPriority(system, edge.priority)) {
                  sortedSystems[index++] = system;
                  this.resolveEdgesFor(system);
                  this.fulfillEdgesFor(system);
                  continue label52;
               }
            }
         }

         for (DependencyGraph.Edge<ECS_TYPE> edgex : this.edges) {
            if (!edgex.resolved && edgex.fulfilled) {
               ISystem<ECS_TYPE> system = edgex.afterSystem;
               if (this.afterSystemUnfulfilledEdges.get(system).isEmpty()) {
                  sortedSystems[index++] = system;
                  this.resolveEdgesFor(system);
                  this.fulfillEdgesFor(system);
                  continue label52;
               }
            }
         }

         throw new IllegalArgumentException("Found a cyclic dependency!" + this);
      }
   }

   private boolean hasEdgeOfLaterPriority(@Nonnull ISystem<ECS_TYPE> system, int priority) {
      for (DependencyGraph.Edge<ECS_TYPE> edge : this.afterSystemEdges.get(system)) {
         if (!edge.resolved && edge.priority > priority) {
            return true;
         }
      }

      return false;
   }

   private void resolveEdgesFor(@Nonnull ISystem<ECS_TYPE> system) {
      for (DependencyGraph.Edge<ECS_TYPE> edge : this.afterSystemEdges.get(system)) {
         edge.resolved = true;
      }
   }

   private void fulfillEdgesFor(@Nonnull ISystem<ECS_TYPE> system) {
      for (DependencyGraph.Edge<ECS_TYPE> edge : this.beforeSystemEdges.get(system)) {
         edge.fulfilled = true;
         this.afterSystemUnfulfilledEdges.get(edge.afterSystem).remove(edge);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "DependencyGraph{systems=" + Arrays.toString((Object[])this.systems) + ", edges=" + Arrays.toString((Object[])this.edges) + "}";
   }

   private static class Edge<ECS_TYPE> implements Comparable<DependencyGraph.Edge<ECS_TYPE>> {
      @Nonnull
      private static final DependencyGraph.Edge<?>[] EMPTY_ARRAY = new DependencyGraph.Edge[0];
      @Nullable
      private final ISystem<ECS_TYPE> beforeSystem;
      private final ISystem<ECS_TYPE> afterSystem;
      private final int priority;
      private boolean fulfilled;
      private boolean resolved;

      public static <ECS_TYPE> DependencyGraph.Edge<ECS_TYPE>[] emptyArray() {
         return (DependencyGraph.Edge<ECS_TYPE>[])EMPTY_ARRAY;
      }

      public Edge(@Nullable ISystem<ECS_TYPE> beforeSystem, @Nonnull ISystem<ECS_TYPE> afterSystem, int priority) {
         this.beforeSystem = beforeSystem;
         this.afterSystem = afterSystem;
         this.priority = priority;
         this.fulfilled = beforeSystem == null;
      }

      public int compareTo(@Nonnull DependencyGraph.Edge<ECS_TYPE> o) {
         return Integer.compare(this.priority, o.priority);
      }

      @Nonnull
      @Override
      public String toString() {
         return "Edge{beforeSystem="
            + this.beforeSystem
            + ", afterSystem="
            + this.afterSystem
            + ", priority="
            + this.priority
            + ", fulfilled="
            + this.fulfilled
            + ", resolved="
            + this.resolved
            + "}";
      }
   }
}
