package com.hypixel.hytale.server.core.plugin.pending;

import com.hypixel.hytale.common.plugin.Mod;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class PendingLoadPlugin implements Mod {
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
   @Override
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

   @Override
   public abstract boolean isCoreMod();

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
}
