package com.hypixel.hytale.server.core.ui.builder;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBinding;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UIEventBuilder {
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final CustomUIEventBinding[] EMPTY_EVENT_BINDING_ARRAY = new CustomUIEventBinding[0];
   @Nonnull
   private final List<CustomUIEventBinding> events = new ObjectArrayList();

   @Nonnull
   public UIEventBuilder addEventBinding(CustomUIEventBindingType type, String selector) {
      return this.addEventBinding(type, selector, null);
   }

   @Nonnull
   public UIEventBuilder addEventBinding(CustomUIEventBindingType type, String selector, boolean locksInterface) {
      return this.addEventBinding(type, selector, null, locksInterface);
   }

   @Nonnull
   public UIEventBuilder addEventBinding(CustomUIEventBindingType type, String selector, EventData data) {
      return this.addEventBinding(type, selector, data, true);
   }

   @Nonnull
   public UIEventBuilder addEventBinding(CustomUIEventBindingType type, String selector, @Nullable EventData data, boolean locksInterface) {
      String dataString = null;
      if (data != null) {
         ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
         dataString = MapCodec.STRING_HASH_MAP_CODEC.encode(data.events(), extraInfo).asDocument().toJson();
         extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
      }

      this.events.add(new CustomUIEventBinding(type, selector, dataString, locksInterface));
      return this;
   }

   @Nonnull
   public CustomUIEventBinding[] getEvents() {
      return this.events.toArray(CustomUIEventBinding[]::new);
   }
}
