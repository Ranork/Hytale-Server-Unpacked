package com.hypixel.hytale.server.core.util.message;

import com.hypixel.hytale.server.core.Message;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MessageFormat {
   private static final int LIST_MAX_INLINE_VALUES = 4;

   @Nonnull
   public static Message list(@Nullable Message header, @Nonnull Collection<Message> values) {
      Message msg = Message.empty();
      if (header != null) {
         msg.insert(Message.translation("server.formatting.list.header").param("header", header).param("count", values.size()));
         if (values.size() <= 4) {
            msg.insert(Message.translation("server.formatting.list.inlineHeaderSuffix"));
         }
      }

      if (values.isEmpty()) {
         msg.insert(Message.translation("server.formatting.list.empty"));
         return msg;
      } else {
         if (values.size() <= 4) {
            Message separator = Message.translation("server.formatting.list.itemSeparator");
            Message[] array = values.toArray(Message[]::new);

            for (int i = 0; i < array.length; i++) {
               msg.insert(array[i]);
               if (i < array.length - 1) {
                  msg.insert(separator);
               }
            }
         } else {
            Message delim = Message.raw("\n");

            for (Message value : values) {
               msg.insert(delim);
               msg.insert(Message.translation("server.formatting.list.row").param("value", value));
            }
         }

         return msg;
      }
   }
}
