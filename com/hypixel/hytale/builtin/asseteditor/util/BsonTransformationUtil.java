package com.hypixel.hytale.builtin.asseteditor.util;

import com.hypixel.hytale.common.util.StringUtil;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonNull;
import org.bson.BsonValue;

public class BsonTransformationUtil {
   private static void actionOnProperty(BsonDocument entity, @Nonnull String[] propertyPath, @Nonnull BiConsumer<BsonValue, String> biConsumer, boolean create) {
      BsonValue current = entity;

      for (int i = 0; i < propertyPath.length - 1; i++) {
         BsonValue jsonElement;
         if (current instanceof BsonDocument) {
            jsonElement = ((BsonDocument)current).get(propertyPath[i]);
            if (jsonElement == null || jsonElement instanceof BsonNull) {
               if (!create) {
                  return;
               }

               if (StringUtil.isNumericString(propertyPath[i + 1])) {
                  jsonElement = new BsonArray();
               } else {
                  jsonElement = new BsonDocument();
               }

               ((BsonDocument)current).put(propertyPath[i], jsonElement);
            }
         } else {
            if (!(current instanceof BsonArray) || !StringUtil.isNumericString(propertyPath[i])) {
               throw new IllegalArgumentException(
                  "Element is not Object or (Array or invalid index)! " + String.join(".", propertyPath) + ", " + propertyPath[i] + ", " + current
               );
            }

            int index = Integer.parseInt(propertyPath[i]);
            jsonElement = ((BsonArray)current).get(index);
            if (jsonElement == null || jsonElement instanceof BsonNull) {
               if (!create) {
                  return;
               }

               if (StringUtil.isNumericString(propertyPath[i + 1])) {
                  jsonElement = new BsonArray();
               } else {
                  jsonElement = new BsonDocument();
               }

               ((BsonArray)current).set(index, jsonElement);
            }
         }

         current = jsonElement;
      }

      biConsumer.accept(current, propertyPath[propertyPath.length - 1]);
   }

   public static void removeProperty(BsonDocument entity, @Nonnull String[] propertyPath) {
      actionOnProperty(entity, propertyPath, (parent, key) -> {
         if (parent instanceof BsonDocument) {
            ((BsonDocument)parent).remove(key);
         } else {
            if (!(parent instanceof BsonArray) || !StringUtil.isNumericString(key)) {
               throw new IllegalArgumentException("Element is not Object or (Array or invalid index)! " + key + ", " + key + ", " + parent);
            }

            ((BsonArray)parent).remove(Integer.parseInt(key));
         }
      }, false);
   }

   public static void setProperty(BsonDocument entity, @Nonnull String[] pathElements, BsonValue value) {
      actionOnProperty(entity, pathElements, (parent, key) -> {
         if (parent instanceof BsonDocument) {
            ((BsonDocument)parent).put(key, value);
         } else {
            if (!(parent instanceof BsonArray) || !StringUtil.isNumericString(key)) {
               throw new IllegalArgumentException("Element is not Object or (Array or invalid index)! " + key + ", " + key + ", " + parent);
            }

            ((BsonArray)parent).set(Integer.parseInt(key), value);
         }
      }, true);
   }

   public static void insertProperty(BsonDocument entity, @Nonnull String[] pathElements, BsonValue value) {
      actionOnProperty(entity, pathElements, (parent, key) -> {
         if (parent instanceof BsonDocument) {
            ((BsonDocument)parent).put(key, value);
         } else {
            if (!(parent instanceof BsonArray) || !StringUtil.isNumericString(key)) {
               throw new IllegalArgumentException("Element is not Object or (Array or invalid index)! " + key + ", " + key + ", " + parent);
            }

            ((BsonArray)parent).add(Integer.parseInt(key), value);
         }
      }, true);
   }
}
