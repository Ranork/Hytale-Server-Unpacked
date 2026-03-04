package com.hypixel.hytale.common.util.java;

import com.hypixel.hytale.function.supplier.CachedSupplier;
import com.hypixel.hytale.function.supplier.SupplierUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import javax.annotation.Nullable;

public class ManifestUtil {
   public static final String VENDOR_ID_PROPERTY = "Implementation-Vendor-Id";
   public static final String VERSION_PROPERTY = "Implementation-Version";
   public static final String REVISION_ID_PROPERTY = "Implementation-Revision-Id";
   public static final String PATCHLINE_PROPERTY = "Implementation-Patchline";
   private static final CachedSupplier<Manifest> MANIFEST = SupplierUtil.cache(() -> {
      try {
         ClassLoader cl = ManifestUtil.class.getClassLoader();
         Enumeration<URL> enumeration = cl.getResources("META-INF/MANIFEST.MF");
         Manifest theManifest = null;

         while (enumeration.hasMoreElements()) {
            URL url = enumeration.nextElement();

            Manifest possible;
            try (InputStream is = url.openStream()) {
               possible = new Manifest(is);
            }

            Attributes mainAttributes = possible.getMainAttributes();
            String vendorId = mainAttributes.getValue("Implementation-Vendor-Id");
            if (vendorId != null && vendorId.equals("com.hypixel.hytale")) {
               theManifest = possible;
               break;
            }
         }

         return theManifest;
      } catch (Throwable var10) {
         HytaleLogger.getLogger().at(Level.WARNING).log("Exception was thrown getting manifest!", var10);
         return null;
      }
   });
   private static final CachedSupplier<String> IMPLEMENTATION_VERSION = SupplierUtil.cache(
      () -> {
         try {
            Manifest localManifest = MANIFEST.get();
            return localManifest == null
               ? "NoJar"
               : Objects.requireNonNull(localManifest.getMainAttributes().getValue("Implementation-Version"), "Null implementation version!");
         } catch (Throwable var1) {
            HytaleLogger.getLogger().at(Level.WARNING).log("Exception was thrown getting implementation version!", var1);
            return "UNKNOWN";
         }
      }
   );
   private static final CachedSupplier<String> IMPLEMENTATION_REVISION_ID = SupplierUtil.cache(
      () -> {
         try {
            Manifest localManifest = MANIFEST.get();
            return localManifest == null
               ? "NoJar"
               : Objects.requireNonNull(localManifest.getMainAttributes().getValue("Implementation-Revision-Id"), "Null implementation revision id!");
         } catch (Throwable var1) {
            HytaleLogger.getLogger().at(Level.WARNING).log("Exception was thrown getting implementation revision id!", var1);
            return "UNKNOWN";
         }
      }
   );
   private static final CachedSupplier<String> IMPLEMENTATION_PATCHLINE = SupplierUtil.cache(() -> {
      try {
         Manifest localManifest = MANIFEST.get();
         if (localManifest == null) {
            return "dev";
         } else {
            String value = localManifest.getMainAttributes().getValue("Implementation-Patchline");
            return value != null && !value.isEmpty() ? value : "dev";
         }
      } catch (Throwable var2) {
         HytaleLogger.getLogger().at(Level.WARNING).log("Exception was thrown getting implementation patchline!", var2);
         return "dev";
      }
   });
   private static final CachedSupplier<String> VERSION = SupplierUtil.cache(() -> {
      String version = IMPLEMENTATION_VERSION.get();
      return "NoJar".equals(version) ? null : version;
   });

   public static boolean isJar() {
      return MANIFEST.get() != null;
   }

   @Nullable
   public static Manifest getManifest() {
      return MANIFEST.get();
   }

   @Nullable
   public static String getImplementationVersion() {
      return IMPLEMENTATION_VERSION.get();
   }

   @Nullable
   public static String getVersion() {
      return VERSION.get();
   }

   @Nullable
   public static String getImplementationRevisionId() {
      return IMPLEMENTATION_REVISION_ID.get();
   }

   @Nullable
   public static String getPatchline() {
      return IMPLEMENTATION_PATCHLINE.get();
   }
}
