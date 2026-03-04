package com.hypixel.hytale.server.core.modules.accesscontrol.ban;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

@FunctionalInterface
public interface BanParser {
   Ban parse(JsonObject var1) throws JsonParseException;
}
