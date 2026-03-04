package com.hypixel.hytale.server.core.asset.type.buildertool.config.args;

import com.hypixel.hytale.server.core.Message;
import javax.annotation.Nonnull;

public class ToolArgException extends Exception {
   @Nonnull
   private final Message translationMessage;

   public ToolArgException(@Nonnull Message translationMessage) {
      super(translationMessage.toString());
      this.translationMessage = translationMessage;
   }

   public ToolArgException(@Nonnull Message translationMessage, Throwable cause) {
      super(translationMessage.toString(), cause);
      this.translationMessage = translationMessage;
   }

   @Nonnull
   public Message getTranslationMessage() {
      return this.translationMessage;
   }
}
