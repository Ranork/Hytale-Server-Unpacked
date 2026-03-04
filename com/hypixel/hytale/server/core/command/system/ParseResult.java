package com.hypixel.hytale.server.core.command.system;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ParseResult {
   private boolean failed;
   @Nullable
   private List<Message> reasons;
   private final boolean throwExceptionWhenFailed;

   public ParseResult() {
      this(false);
   }

   public ParseResult(boolean throwExceptionWhenFailed) {
      this.throwExceptionWhenFailed = throwExceptionWhenFailed;
   }

   public void fail(@Nonnull Message reason, @Nullable Message... otherMessages) {
      this.failed = true;
      if (this.reasons == null) {
         this.reasons = new ObjectArrayList();
      }

      this.reasons.add(reason);
      if (otherMessages != null) {
         Collections.addAll(this.reasons, otherMessages);
      }

      if (this.throwExceptionWhenFailed) {
         StringBuilder builder = new StringBuilder(reason.getAnsiMessage());
         if (otherMessages != null) {
            for (Message otherMessage : otherMessages) {
               builder.append("\n").append(otherMessage.getAnsiMessage());
            }
         }

         throw new GeneralCommandException(Message.raw(builder.toString()));
      }
   }

   public void fail(@Nonnull Message reason) {
      this.fail(reason, (Message[])null);
   }

   public boolean failed() {
      return this.failed;
   }

   public void sendMessages(@Nonnull CommandSender sender) {
      if (this.reasons != null) {
         for (Message reason : this.reasons) {
            sender.sendMessage(reason);
         }
      }
   }
}
