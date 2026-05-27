package com.hypixel.hytale.builtin.worldgen.modifier.op;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Content;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.worldgen.util.LogUtil;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class LogOp implements Op {
   public static final String ID = "Log";
   public static final BuilderCodec<LogOp> CODEC = BuilderCodec.builder(LogOp.class, LogOp::new)
      .documentation("Log the filepath of the content container for a given content-type")
      .<Content.Type>append(new KeyedCodec<>("ContentType", Content.Type.CODEC), (t, v) -> t.type = v, t -> t.type)
      .documentation("The content-type to log the container of")
      .add()
      .build();
   @Nonnull
   protected Content.Type type = Content.Type.NONE;

   @Override
   public Content.Type type() {
      return this.type;
   }

   @Override
   public <T> void apply(@Nonnull ModifyEvent<T> event) throws Error {
      if (event.type() == this.type) {
         LogUtil.getLogger().at(Level.INFO).log("[%s] %s", event.type(), event.file().getContentPath());
      }
   }
}
