package com.hypixel.hytale.server.core.receiver;

import com.hypixel.hytale.server.core.Message;
import javax.annotation.Nonnull;

public interface IMessageReceiver {
   void sendMessage(@Nonnull Message var1);
}
