package com.hypixel.hytale.builtin.worldgen.modifier.content;

import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class FileContent implements Content {
   public static final String ID = "File";
   public static final BuilderCodec<FileContent> CODEC = BuilderCodec.builder(FileContent.class, FileContent::new)
      .documentation("File content to be loaded and added to the target content list")
      .<Content.Type>append(new KeyedCodec<>("ContentType", Content.Type.CODEC), (instance, type) -> instance.type = type, instance -> instance.type)
      .documentation("The type of the content to load")
      .add()
      .<String>append(new KeyedCodec<>("Path", BuilderCodec.STRING), (instance, path) -> instance.path = path, instance -> instance.path)
      .documentation("A dot-separated path to a content file within the target world-gen root folder")
      .add()
      .build();
   protected Content.Type type = Content.Type.NONE;
   protected String path = "";

   @Override
   public Content.Type type() {
      return this.type;
   }

   @Override
   public <T> void applyTo(ModifyEvent<T> event) throws Exception {
      T value = event.loader().load(this.path);
      if (value != null) {
         event.entries().add(value);
      }
   }

   @Override
   public String toString() {
      return "FileContent{Path=" + this.path + "}";
   }
}
