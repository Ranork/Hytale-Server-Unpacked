package com.hypixel.hytale.server.core.plugin.pending;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class PendingLoadPlugin {
   @Nonnull
   private final PluginIdentifier identifier;
   @Nonnull
   private final PluginManifest manifest;
   @Nullable
   private final Path path;

   PendingLoadPlugin(@Nullable Path path, @Nonnull PluginManifest manifest) {
      this.path = path;
      this.identifier = new PluginIdentifier(manifest);
      this.manifest = manifest;
   }

   @Nonnull
   public PluginIdentifier getIdentifier() {
      return this.identifier;
   }

   @Nonnull
   public PluginManifest getManifest() {
      return this.manifest;
   }

   @Nullable
   public Path getPath() {
      return this.path;
   }

   public abstract PendingLoadPlugin createSubPendingLoadPlugin(PluginManifest var1);

   @Nonnull
   public abstract PluginBase load() throws Exception;

   @Nonnull
   public List<PendingLoadPlugin> createSubPendingLoadPlugins() {
      List<PluginManifest> subPlugins = this.manifest.getSubPlugins();
      if (subPlugins.isEmpty()) {
         return Collections.emptyList();
      } else {
         ObjectArrayList<PendingLoadPlugin> plugins = new ObjectArrayList(subPlugins.size());

         for (PluginManifest subManifest : subPlugins) {
            subManifest.inherit(this.manifest);
            plugins.add(this.createSubPendingLoadPlugin(subManifest));
         }

         return plugins;
      }
   }

   public boolean dependsOn(PluginIdentifier identifier) {
      return this.manifest.getDependencies().containsKey(identifier) || this.manifest.getOptionalDependencies().containsKey(identifier);
   }

   public abstract boolean isInServerClassPath();

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         PendingLoadPlugin that = (PendingLoadPlugin)o;
         if (!this.identifier.equals(that.identifier)) {
            return false;
         } else {
            return !this.manifest.equals(that.manifest) ? false : Objects.equals(this.path, that.path);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.identifier.hashCode();
      result = 31 * result + this.manifest.hashCode();
      return 31 * result + (this.path != null ? this.path.hashCode() : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "PendingLoadPlugin{identifier=" + this.identifier + ", manifest=" + this.manifest + ", path=" + this.path + "}";
   }

   @Nonnull
   public static List<PendingLoadPlugin> calculateLoadOrder(@Nonnull Map<PluginIdentifier, PendingLoadPlugin> pending) {
      HashMap<PluginIdentifier, PendingLoadPlugin.EntryNode> nodes = new HashMap<>(pending.size());

      for (Entry<PluginIdentifier, PendingLoadPlugin> entry : pending.entrySet()) {
         nodes.put(entry.getKey(), new PendingLoadPlugin.EntryNode(entry.getValue()));
      }

      HashSet<PluginIdentifier> classpathPlugins = new HashSet<>();

      for (Entry<PluginIdentifier, PendingLoadPlugin> entry : pending.entrySet()) {
         if (entry.getValue().isInServerClassPath() && "Hytale".equals(entry.getKey().getGroup())) {
            classpathPlugins.add(entry.getKey());
         }
      }

      HashMap<PluginIdentifier, Set<PluginIdentifier>> missingDependencies = new HashMap<>();

      for (PendingLoadPlugin.EntryNode node : nodes.values()) {
         PluginManifest manifest = node.plugin.manifest;

         for (PluginIdentifier depId : manifest.getDependencies().keySet()) {
            if (nodes.containsKey(depId)) {
               node.edge.add(depId);
            } else {
               missingDependencies.computeIfAbsent(node.plugin.identifier, k -> new HashSet<>()).add(depId);
            }
         }

         for (PluginIdentifier identifier : manifest.getOptionalDependencies().keySet()) {
            PendingLoadPlugin.EntryNode dep = nodes.get(identifier);
            if (dep != null) {
               node.edge.add(identifier);
            }
         }

         if (!node.plugin.isInServerClassPath()) {
            node.edge.addAll(classpathPlugins);
         }
      }

      HashMap<PluginIdentifier, Set<PluginIdentifier>> missingLoadBefore = new HashMap<>();

      for (Entry<PluginIdentifier, PendingLoadPlugin> entryx : pending.entrySet()) {
         PluginManifest manifest = entryx.getValue().manifest;

         for (PluginIdentifier targetId : manifest.getLoadBefore().keySet()) {
            PendingLoadPlugin.EntryNode targetNode = nodes.get(targetId);
            if (targetNode != null) {
               targetNode.edge.add(entryx.getKey());
            } else {
               missingLoadBefore.computeIfAbsent(entryx.getKey(), k -> new HashSet<>()).add(targetId);
            }
         }
      }

      if (missingDependencies.isEmpty() && missingLoadBefore.isEmpty()) {
         ObjectArrayList<PendingLoadPlugin> loadOrder = new ObjectArrayList(nodes.size());

         while (!nodes.isEmpty()) {
            boolean didWork = false;
            Iterator<Entry<PluginIdentifier, PendingLoadPlugin.EntryNode>> iterator = nodes.entrySet().iterator();

            while (iterator.hasNext()) {
               Entry<PluginIdentifier, PendingLoadPlugin.EntryNode> entryx = iterator.next();
               PendingLoadPlugin.EntryNode node = entryx.getValue();
               if (node.edge.isEmpty()) {
                  didWork = true;
                  iterator.remove();
                  loadOrder.add(node.plugin);
                  PluginIdentifier identifierx = entryx.getKey();

                  for (PendingLoadPlugin.EntryNode otherNode : nodes.values()) {
                     otherNode.edge.remove(identifierx);
                  }
               }
            }

            if (!didWork) {
               StringBuilder sb = new StringBuilder("Found cyclic dependency between plugins:\n");

               for (Entry<PluginIdentifier, PendingLoadPlugin.EntryNode> entryx : nodes.entrySet()) {
                  sb.append("  ").append(entryx.getKey()).append(" waiting on: ").append(entryx.getValue().edge).append("\n");
               }

               throw new IllegalArgumentException(sb.toString());
            }
         }

         return loadOrder;
      } else {
         StringBuilder sb = new StringBuilder();
         if (!missingDependencies.isEmpty()) {
            sb.append("Missing required dependencies:\n");

            for (Entry<PluginIdentifier, Set<PluginIdentifier>> entryx : missingDependencies.entrySet()) {
               sb.append("  ").append(entryx.getKey()).append(" requires: ").append(entryx.getValue()).append("\n");
            }
         }

         if (!missingLoadBefore.isEmpty()) {
            sb.append("Missing loadBefore targets:\n");

            for (Entry<PluginIdentifier, Set<PluginIdentifier>> entryx : missingLoadBefore.entrySet()) {
               sb.append("  ").append(entryx.getKey()).append(" loadBefore: ").append(entryx.getValue()).append("\n");
            }
         }

         throw new IllegalArgumentException(sb.toString());
      }
   }

   private static final class EntryNode {
      private final Set<PluginIdentifier> edge = new HashSet<>();
      private final PendingLoadPlugin plugin;

      private EntryNode(PendingLoadPlugin plugin) {
         this.plugin = plugin;
      }

      @Nonnull
      @Override
      public String toString() {
         return "EntryNode{plugin=" + this.plugin + ", dependencies=" + this.edge + "}";
      }
   }
}
