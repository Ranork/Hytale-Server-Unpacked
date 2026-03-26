package com.hypixel.hytale.builtin.worldgen.modifier.content;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class FileRef implements Content {
   public static final String TYPE_ID = "File";
   public static final BuilderCodec<FileRef> CODEC = BuilderCodec.builder(FileRef.class, FileRef::new)
      .documentation("File content to be loaded and added to the target content list")
      .append(new KeyedCodec<>("Path", BuilderCodec.STRING), FileRef::setFile, FileRef::getFilePath)
      .documentation("A dot-separated path to a content file within the target world-gen root folder")
      .add()
      .build();
   protected String path = "";
   protected transient JsonObject obj = new JsonObject();

   @Nonnull
   @Override
   public JsonElement get() {
      return this.obj;
   }

   @Override
   public String toString() {
      return "FileRef{Path=" + this.path + "}";
   }

   @Nonnull
   private String getFilePath() {
      return this.path;
   }

   private void setFile(@Nonnull String path) {
      this.path = path;
      this.obj = new JsonObject();
      this.obj.addProperty("File", path);
   }
}
