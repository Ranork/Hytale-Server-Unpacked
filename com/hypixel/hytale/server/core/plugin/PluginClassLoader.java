package com.hypixel.hytale.server.core.plugin;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PluginClassLoader extends URLClassLoader {
   @Nonnull
   private final PluginManager pluginManager;
   private final boolean inServerClassPath;
   @Nullable
   private JavaPlugin plugin;

   public PluginClassLoader(@Nonnull PluginManager pluginManager, @Nullable PluginIdentifier identifier, boolean inServerClassPath, @Nonnull URL... urls) {
      super((inServerClassPath ? "BuiltinPlugin" : "ThirdParty") + (identifier != null ? "(" + identifier + ")" : ""), urls, null);
      this.inServerClassPath = inServerClassPath;
      this.pluginManager = pluginManager;
   }

   public boolean isInServerClassPath() {
      return this.inServerClassPath;
   }

   void setPlugin(@Nonnull JavaPlugin plugin) {
      this.plugin = plugin;
   }

   @Nonnull
   @Override
   protected Class<?> loadClass(@Nonnull String name, boolean resolve) throws ClassNotFoundException {
      return this.loadClass0(name, true);
   }

   @Nonnull
   private Class<?> loadClass0(@Nonnull String name, boolean useBridge) throws ClassNotFoundException {
      try {
         Class<?> loadClass = PluginManager.class.getClassLoader().loadClass(name);
         if (loadClass != null) {
            return loadClass;
         }
      } catch (ClassNotFoundException var7) {
      }

      try {
         Class<?> loadClass = super.loadClass(name, false);
         if (loadClass != null) {
            return loadClass;
         }
      } catch (ClassNotFoundException var6) {
      }

      if (useBridge) {
         if (this.plugin != null) {
            try {
               Class<?> loadClass = this.pluginManager.getBridgeClassLoader().loadClass0(name, this, this.plugin.getManifest());
               if (loadClass != null) {
                  return loadClass;
               }
            } catch (ClassNotFoundException var5) {
            }
         } else {
            try {
               Class<?> loadClass = this.pluginManager.getBridgeClassLoader().loadClass0(name, this);
               if (loadClass != null) {
                  return loadClass;
               }
            } catch (ClassNotFoundException var4) {
            }
         }
      }

      throw new ClassNotFoundException(name);
   }

   @Nonnull
   public Class<?> loadLocalClass(@Nonnull String name) throws ClassNotFoundException {
      synchronized (this.getClassLoadingLock(name)) {
         Class<?> loadedClass = this.findLoadedClass(name);
         if (loadedClass == null) {
            try {
               ClassLoader parent = this.getParent();
               if (parent != null) {
                  loadedClass = parent.loadClass(name);
               }
            } catch (ClassNotFoundException var6) {
            }

            if (loadedClass == null) {
               loadedClass = this.loadClass0(name, false);
            }
         }

         return loadedClass;
      }
   }

   @Nullable
   @Override
   public URL getResource(@Nonnull String name) {
      URL resource = PluginManager.class.getClassLoader().getResource(name);
      if (resource != null) {
         return resource;
      } else {
         resource = super.getResource(name);
         if (resource != null) {
            return resource;
         } else {
            PluginManager.PluginBridgeClassLoader bridge = this.pluginManager.getBridgeClassLoader();
            if (this.plugin != null) {
               resource = bridge.getResource0(name, this, this.plugin.getManifest());
            } else {
               resource = bridge.getResource0(name, this);
            }

            return resource;
         }
      }
   }

   @Nonnull
   @Override
   public Enumeration<URL> getResources(@Nonnull String name) throws IOException {
      ObjectArrayList<URL> results = new ObjectArrayList();
      Enumeration<URL> serverResources = PluginManager.class.getClassLoader().getResources(name);

      while (serverResources.hasMoreElements()) {
         results.add(serverResources.nextElement());
      }

      Enumeration<URL> pluginResources = super.getResources(name);

      while (pluginResources.hasMoreElements()) {
         results.add(pluginResources.nextElement());
      }

      PluginManager.PluginBridgeClassLoader bridge = this.pluginManager.getBridgeClassLoader();
      Enumeration<URL> bridgeResources;
      if (this.plugin != null) {
         bridgeResources = bridge.getResources0(name, this, this.plugin.getManifest());
      } else {
         bridgeResources = bridge.getResources0(name, this);
      }

      while (bridgeResources.hasMoreElements()) {
         results.add(bridgeResources.nextElement());
      }

      return Collections.enumeration(results);
   }

   public static boolean isFromThirdPartyPlugin(@Nullable Throwable throwable) {
      while (throwable != null) {
         for (StackTraceElement element : throwable.getStackTrace()) {
            if ("ThirdParty".equals(element.getClassLoaderName())) {
               return true;
            }
         }

         if (throwable.getCause() == throwable) {
            break;
         }

         throwable = throwable.getCause();
      }

      return false;
   }

   static {
      registerAsParallelCapable();
   }
}
