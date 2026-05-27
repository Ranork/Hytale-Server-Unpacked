package com.hypixel.hytale.server.core.asset.type.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BlockyModelBoundsParser {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final float BLOCK_SCALE = 0.03125F;
   private static final Vector3f[] BOX_CORNERS = new Vector3f[]{
      new Vector3f(-0.5F, 0.5F, -0.5F),
      new Vector3f(0.5F, 0.5F, -0.5F),
      new Vector3f(0.5F, -0.5F, -0.5F),
      new Vector3f(-0.5F, -0.5F, -0.5F),
      new Vector3f(0.5F, 0.5F, 0.5F),
      new Vector3f(-0.5F, 0.5F, 0.5F),
      new Vector3f(-0.5F, -0.5F, 0.5F),
      new Vector3f(0.5F, -0.5F, 0.5F)
   };

   @Nullable
   public static Box computeBounds(@Nonnull String modelPath) {
      CommonAsset asset = CommonAssetRegistry.getByName(modelPath);
      return asset == null ? null : computeBounds(asset);
   }

   @Nullable
   public static Box computeBounds(@Nonnull CommonAsset asset) {
      try {
         byte[] bytes = asset.getBlob().join();
         if (bytes == null) {
            return null;
         } else {
            JsonObject json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
            return computeBoundsFromJson(json);
         }
      } catch (Exception var3) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var3)).log("Failed to compute bounds for blockymodel: %s", asset.getName());
         return null;
      }
   }

   @Nullable
   private static Box computeBoundsFromJson(@Nonnull JsonObject root) {
      JsonArray nodesArray = root.getAsJsonArray("nodes");
      if (nodesArray != null && !nodesArray.isEmpty()) {
         float minX = Float.MAX_VALUE;
         float minY = Float.MAX_VALUE;
         float minZ = Float.MAX_VALUE;
         float maxX = -Float.MAX_VALUE;
         float maxY = -Float.MAX_VALUE;
         float maxZ = -Float.MAX_VALUE;
         boolean hasPoints = false;
         float[] minMax = new float[]{minX, minY, minZ, maxX, maxY, maxZ};

         for (JsonElement nodeElement : nodesArray) {
            if (nodeElement.isJsonObject()) {
               hasPoints |= accumulateNodeBounds(nodeElement.getAsJsonObject(), new Vector3f(0.0F, 0.0F, 0.0F), new Quaternionf(), minMax);
            }
         }

         return !hasPoints
            ? null
            : new Box(minMax[0] * 0.03125F, minMax[1] * 0.03125F, minMax[2] * 0.03125F, minMax[3] * 0.03125F, minMax[4] * 0.03125F, minMax[5] * 0.03125F);
      } else {
         return null;
      }
   }

   private static boolean accumulateNodeBounds(
      @Nonnull JsonObject node, @Nonnull Vector3f parentPosition, @Nonnull Quaternionf parentOrientation, @Nonnull float[] minMax
   ) {
      JsonObject shape = node.getAsJsonObject("shape");
      boolean visible = shape == null || !shape.has("visible") || shape.get("visible").getAsBoolean();
      if (!visible) {
         return false;
      } else {
         Vector3f position = readVec3(node.getAsJsonObject("position"), 0.0F, 0.0F, 0.0F);
         Quaternionf orientation = readQuat(node.getAsJsonObject("orientation"));
         Vector3f offset = shape != null ? readVec3(shape.getAsJsonObject("offset"), 0.0F, 0.0F, 0.0F) : new Vector3f();
         Vector3f localPosition = new Vector3f(offset);
         localPosition.rotate(orientation);
         localPosition.add(position);
         Vector3f worldPosition = new Vector3f(localPosition);
         worldPosition.rotate(parentOrientation);
         worldPosition.add(parentPosition);
         Quaternionf worldOrientation = new Quaternionf(parentOrientation);
         worldOrientation.mul(orientation);
         boolean hasPoints = false;
         if (shape != null) {
            String type = shape.has("type") ? shape.get("type").getAsString() : "none";
            if ("box".equals(type) || "quad".equals(type)) {
               JsonObject settings = shape.getAsJsonObject("settings");
               Vector3f size = settings != null ? readVec3(settings.getAsJsonObject("size"), 0.0F, 0.0F, 0.0F) : new Vector3f();
               Vector3f stretch = readVec3(shape.getAsJsonObject("stretch"), 1.0F, 1.0F, 1.0F);
               float sx = size.x * stretch.x;
               float sy = size.y * stretch.y;
               float sz = size.z * stretch.z;
               Vector3f[] corners = "box".equals(type) ? BOX_CORNERS : getQuadCorners(shape);

               for (Vector3f corner : corners) {
                  Vector3f scaled = new Vector3f(corner.x * sx, corner.y * sy, corner.z * sz);
                  scaled.rotate(worldOrientation);
                  scaled.add(worldPosition);
                  minMax[0] = Math.min(minMax[0], scaled.x);
                  minMax[1] = Math.min(minMax[1], scaled.y);
                  minMax[2] = Math.min(minMax[2], scaled.z);
                  minMax[3] = Math.max(minMax[3], scaled.x);
                  minMax[4] = Math.max(minMax[4], scaled.y);
                  minMax[5] = Math.max(minMax[5], scaled.z);
               }

               hasPoints = true;
            }
         }

         JsonArray children = node.getAsJsonArray("children");
         if (children != null) {
            for (JsonElement childElement : children) {
               if (childElement.isJsonObject()) {
                  hasPoints |= accumulateNodeBounds(childElement.getAsJsonObject(), worldPosition, worldOrientation, minMax);
               }
            }
         }

         return hasPoints;
      }
   }

   private static Vector3f[] getQuadCorners(@Nonnull JsonObject shape) {
      JsonObject settings = shape.getAsJsonObject("settings");
      String normal = settings != null && settings.has("normal") ? settings.get("normal").getAsString() : "+Z";

      return switch (normal) {
         case "+X", "-X" -> new Vector3f[]{
            new Vector3f(0.0F, -0.5F, -0.5F), new Vector3f(0.0F, 0.5F, -0.5F), new Vector3f(0.0F, 0.5F, 0.5F), new Vector3f(0.0F, -0.5F, 0.5F)
         };
         case "+Y", "-Y" -> new Vector3f[]{
            new Vector3f(-0.5F, 0.0F, -0.5F), new Vector3f(0.5F, 0.0F, -0.5F), new Vector3f(0.5F, 0.0F, 0.5F), new Vector3f(-0.5F, 0.0F, 0.5F)
         };
         default -> new Vector3f[]{
            new Vector3f(-0.5F, -0.5F, 0.0F), new Vector3f(0.5F, -0.5F, 0.0F), new Vector3f(0.5F, 0.5F, 0.0F), new Vector3f(-0.5F, 0.5F, 0.0F)
         };
      };
   }

   @Nonnull
   private static Vector3f readVec3(@Nullable JsonObject obj, float defX, float defY, float defZ) {
      if (obj == null) {
         return new Vector3f(defX, defY, defZ);
      } else {
         float x = obj.has("x") ? obj.get("x").getAsFloat() : defX;
         float y = obj.has("y") ? obj.get("y").getAsFloat() : defY;
         float z = obj.has("z") ? obj.get("z").getAsFloat() : defZ;
         return new Vector3f(x, y, z);
      }
   }

   @Nonnull
   private static Quaternionf readQuat(@Nullable JsonObject obj) {
      if (obj == null) {
         return new Quaternionf();
      } else {
         float x = obj.has("x") ? obj.get("x").getAsFloat() : 0.0F;
         float y = obj.has("y") ? obj.get("y").getAsFloat() : 0.0F;
         float z = obj.has("z") ? obj.get("z").getAsFloat() : 0.0F;
         float w = obj.has("w") ? obj.get("w").getAsFloat() : 1.0F;
         return new Quaternionf(x, y, z, w);
      }
   }
}
