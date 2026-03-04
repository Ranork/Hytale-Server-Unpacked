package com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.common.util.ExceptionUtil;
import com.hypixel.hytale.server.core.prefab.selection.buffer.BsonPrefabBufferDeserializer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.util.BsonUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class PrefabLoader {
   public static void loadAllPrefabBuffersUnder(@Nonnull Path dirPath, @Nonnull List<PrefabBuffer> pathPrefabs) {
      if (!Files.isDirectory(dirPath)) {
         PrefabBuffer prefab = loadPrefabBufferAt(dirPath);
         if (prefab != null) {
            pathPrefabs.add(prefab);
         }
      } else {
         try {
            Files.walkFileTree(dirPath, new PrefabFileVisitor(pathPrefabs));
         } catch (IOException var4) {
            String msg = "Exception thrown by HytaleGenerator while loading a Prefab:\n";
            msg = msg + ExceptionUtil.toStringWithStack(var4);
            LoggerUtil.getLogger().severe(msg);
         }
      }
   }

   @Nullable
   public static PrefabBuffer loadPrefabBufferAt(@Nonnull Path filePath) {
      if (!hasJsonExtension(filePath)) {
         return null;
      } else {
         try {
            BsonDocument prefabAsBson = BsonUtil.readDocumentNow(filePath);
            return prefabAsBson == null ? null : BsonPrefabBufferDeserializer.INSTANCE.deserialize(filePath, prefabAsBson);
         } catch (Exception var3) {
            String msg = "Exception thrown by HytaleGenerator while loading a PrefabBuffer for " + filePath + ":\n";
            msg = msg + ExceptionUtil.toStringWithStack(var3);
            LoggerUtil.getLogger().severe(msg);
            return null;
         }
      }
   }

   public static boolean hasJsonExtension(@Nonnull Path path) {
      String pathString = path.toString();
      return pathString.toLowerCase().endsWith(".json");
   }
}
