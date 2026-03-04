package com.hypixel.hytale.common.plugin;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PluginIdentifier {
   public static final String THIRD_PARTY_LOADER_NAME = "ThirdParty";
   @Nonnull
   private final String group;
   @Nonnull
   private final String name;

   public PluginIdentifier(@Nonnull String group, @Nonnull String name) {
      this.group = group;
      this.name = name;
   }

   public PluginIdentifier(@Nonnull PluginManifest manifest) {
      this(manifest.getGroup(), manifest.getName());
   }

   @Nullable
   public static PluginIdentifier identifyThirdPartyPlugin(Throwable t) {
      Throwable current = t;
      String prefix = "ThirdParty(";

      PluginIdentifier possibleFailureCause;
      for (possibleFailureCause = null; current != null; current = current.getCause()) {
         StackTraceElement[] stack = current.getStackTrace();

         for (StackTraceElement entry : stack) {
            String loader = entry.getClassLoaderName();
            if (loader != null && loader.startsWith(prefix)) {
               int end = loader.lastIndexOf(41);
               if (end != -1) {
                  possibleFailureCause = fromString(loader.substring(prefix.length(), end));
                  return possibleFailureCause;
               }
            }
         }
      }

      return possibleFailureCause;
   }

   @Nonnull
   public String getGroup() {
      return this.group;
   }

   @Nonnull
   public String getName() {
      return this.name;
   }

   @Override
   public int hashCode() {
      int result = this.group.hashCode();
      return 31 * result + this.name.hashCode();
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         PluginIdentifier that = (PluginIdentifier)o;
         return !Objects.equals(this.group, that.group) ? false : Objects.equals(this.name, that.name);
      } else {
         return false;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return this.group + ":" + this.name;
   }

   @Nonnull
   public static PluginIdentifier fromString(@Nonnull String str) {
      String[] split = str.split(":");
      if (split.length != 2) {
         throw new IllegalArgumentException("String does not match <group>:<name>");
      } else {
         return new PluginIdentifier(split[0], split[1]);
      }
   }
}
