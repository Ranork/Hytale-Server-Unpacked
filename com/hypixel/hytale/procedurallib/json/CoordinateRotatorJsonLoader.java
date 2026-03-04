package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.random.CoordinateOriginRotator;
import com.hypixel.hytale.procedurallib.random.CoordinateRotator;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class CoordinateRotatorJsonLoader<K extends SeedResource> extends JsonLoader<K, CoordinateRotator> {
   public CoordinateRotatorJsonLoader(SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed, dataFolder, json);
   }

   @Nonnull
   public CoordinateRotator load() {
      double pitch = this.has("Pitch") ? this.get("Pitch").getAsDouble() * (float) (Math.PI / 180.0) : 0.0;
      double yaw = this.has("Yaw") ? this.get("Yaw").getAsDouble() * (float) (Math.PI / 180.0) : 0.0;
      if (pitch == 0.0 && yaw == 0.0) {
         return CoordinateRotator.NONE;
      } else {
         double originX = this.has("OriginX") ? this.get("OriginX").getAsDouble() : 0.0;
         double originY = this.has("OriginY") ? this.get("OriginY").getAsDouble() : 0.0;
         double originZ = this.has("OriginZ") ? this.get("OriginZ").getAsDouble() : 0.0;
         return (CoordinateRotator)(originX == 0.0 && originY == 0.0 && originZ == 0.0
            ? new CoordinateRotator(pitch, yaw)
            : new CoordinateOriginRotator(pitch, yaw, originX, originY, originZ));
      }
   }

   public interface Constants {
      String KEY_ROTATE = "Rotate";
      String KEY_PITCH = "Pitch";
      String KEY_YAW = "Yaw";
      String KEY_ORIGIN_X = "OriginX";
      String KEY_ORIGIN_Y = "OriginY";
      String KEY_ORIGIN_Z = "OriginZ";
   }
}
