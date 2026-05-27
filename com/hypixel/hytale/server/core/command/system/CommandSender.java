package com.hypixel.hytale.server.core.command.system;

import com.hypixel.hytale.server.core.permissions.PermissionHolder;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import java.util.UUID;

public interface CommandSender extends IMessageReceiver, PermissionHolder {
   String getUsername();

   UUID getUuid();
}
