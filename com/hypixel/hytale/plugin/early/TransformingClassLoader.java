package com.hypixel.hytale.plugin.early;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public final class TransformingClassLoader extends URLClassLoader {
   private static final Set<String> SECURE_PACKAGE_PREFIXES = Set.of(
      "java.",
      "javax.",
      "jdk.",
      "sun.",
      "com.sun.",
      "org.bouncycastle.",
      "server.io.netty.",
      "org.objectweb.asm.",
      "com.google.gson.",
      "org.slf4j.",
      "org.apache.logging.",
      "ch.qos.logback.",
      "com.google.flogger.",
      "server.io.sentry.",
      "com.hypixel.protoplus.",
      "com.hypixel.fastutil.",
      "com.hypixel.hytale.plugin.early."
   );
   private final List<ClassTransformer> transformers;
   private final ClassLoader appClassLoader;

   public TransformingClassLoader(@Nonnull URL[] urls, @Nonnull List<ClassTransformer> transformers, ClassLoader parent, ClassLoader appClassLoader) {
      super(urls, parent);
      this.transformers = transformers;
      this.appClassLoader = appClassLoader;
   }

   @Override
   protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      synchronized (this.getClassLoadingLock(name)) {
         Class<?> loaded = this.findLoadedClass(name);
         if (loaded != null) {
            if (resolve) {
               this.resolveClass(loaded);
            }

            return loaded;
         } else if (isPreloadedClass(name)) {
            Class<?> clazz = this.appClassLoader.loadClass(name);
            if (resolve) {
               this.resolveClass(clazz);
            }

            return clazz;
         } else {
            String internalName = name.replace('.', '/');
            URL resource = this.findResource(internalName + ".class");
            if (resource != null) {
               Class var9;
               try (InputStream is = resource.openStream()) {
                  Class<?> clazz = this.transformAndDefine(name, internalName, is.readAllBytes(), resource);
                  if (resolve) {
                     this.resolveClass(clazz);
                  }

                  var9 = clazz;
               } catch (IOException var13) {
                  return super.loadClass(name, resolve);
               }

               return var9;
            } else {
               return super.loadClass(name, resolve);
            }
         }
      }
   }

   private Class<?> transformAndDefine(String name, String internalName, byte[] classBytes, URL resource) {
      if (!isSecureClass(name)) {
         for (ClassTransformer transformer : this.transformers) {
            try {
               byte[] transformed = transformer.transform(name, internalName, classBytes);
               if (transformed != null) {
                  classBytes = transformed;
               }
            } catch (Exception var8) {
               System.err.println("[EarlyPlugin] Transformer " + transformer.getClass().getName() + " failed on " + name + ": " + var8.getMessage());
               var8.printStackTrace();
            }
         }
      }

      URL codeSourceUrl = getCodeSourceUrl(resource, internalName);
      CodeSource codeSource = new CodeSource(codeSourceUrl, (Certificate[])null);
      ProtectionDomain protectionDomain = new ProtectionDomain(codeSource, null, this, null);
      return this.defineClass(name, classBytes, 0, classBytes.length, protectionDomain);
   }

   private static URL getCodeSourceUrl(URL resource, String internalName) {
      String urlStr = resource.toString();
      String classPath = internalName + ".class";
      if (urlStr.startsWith("jar:")) {
         int bangIndex = urlStr.indexOf("!/");
         if (bangIndex > 0) {
            try {
               return new URL(urlStr.substring(4, bangIndex));
            } catch (Exception var6) {
               return resource;
            }
         }
      } else if (urlStr.endsWith(classPath)) {
         try {
            return new URL(urlStr.substring(0, urlStr.length() - classPath.length()));
         } catch (Exception var7) {
            return resource;
         }
      }

      return resource;
   }

   private static boolean isPreloadedClass(@Nonnull String name) {
      return name.equals("com.hypixel.hytale.Main") || name.startsWith("com.hypixel.hytale.plugin.early.");
   }

   private static boolean isSecureClass(@Nonnull String name) {
      for (String prefix : SECURE_PACKAGE_PREFIXES) {
         if (name.startsWith(prefix)) {
            return true;
         }
      }

      return false;
   }
}
