package com.hypixel.hytale.builtin.worldgen.modifier.op;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Content;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.worldgen.util.LogUtil;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AddOp implements Op {
   public static final String ID = "Add";
   public static final BuilderCodec<AddOp> CODEC = BuilderCodec.builder(AddOp.class, AddOp::new)
      .documentation("Adds content to the target content list")
      .<Content>append(new KeyedCodec<>("Content", Content.TYPE_CODEC), (instance, array) -> instance.content = array, instance -> instance.content)
      .documentation("Content to add to the target content list")
      .add()
      .build();
   @Nullable
   protected Content content = null;

   @Override
   public <T> void apply(@Nonnull ModifyEvent<T> event) throws Error {
      if (this.content != null) {
         try {
            T content = event.loader().load(this.content.get());
            if (content == null) {
               throw new NullPointerException("Failed to load content " + this.content);
            } else {
               event.entries().add(content);
               LogUtil.getLogger().at(Level.FINE).log("[%s] Added content %s to %s", event.type(), this.content, event.file().getContentPath());
            }
         } catch (Throwable var3) {
            throw new Error("Failed to load content " + this.content, var3);
         }
      }
   }
}
