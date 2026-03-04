package com.hypixel.hytale.server.core.receiver;

import com.hypixel.hytale.server.core.Message;

public interface IEventTitleReceiver {
   float DEFAULT_DURATION = 4.0F;
   float DEFAULT_FADE_DURATION = 1.5F;

   default void showEventTitle(Message primaryTitle, Message secondaryTitle, boolean isMajor, String icon) {
      this.showEventTitle(primaryTitle, secondaryTitle, isMajor, icon, 4.0F);
   }

   default void showEventTitle(Message primaryTitle, Message secondaryTitle, boolean isMajor, String icon, float duration) {
      this.showEventTitle(primaryTitle, secondaryTitle, isMajor, icon, duration, 1.5F, 1.5F);
   }

   void showEventTitle(Message var1, Message var2, boolean var3, String var4, float var5, float var6, float var7);

   default void hideEventTitle() {
      this.hideEventTitle(1.5F);
   }

   void hideEventTitle(float var1);
}
