package com.hypixel.hytale.server.core.ui.builder;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommandType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Area;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.ValueCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonNull;
import org.bson.BsonString;
import org.bson.BsonValue;

public class UICommandBuilder {
   private static final Map<Class, Codec> CODEC_MAP = new Object2ObjectOpenHashMap();
   public static final CustomUICommand[] EMPTY_COMMAND_ARRAY = new CustomUICommand[0];
   @Nonnull
   private final List<CustomUICommand> commands = new ObjectArrayList();

   @Nonnull
   public UICommandBuilder clear(String selector) {
      this.commands.add(new CustomUICommand(CustomUICommandType.Clear, selector, null, null));
      return this;
   }

   @Nonnull
   public UICommandBuilder remove(String selector) {
      this.commands.add(new CustomUICommand(CustomUICommandType.Remove, selector, null, null));
      return this;
   }

   @Nonnull
   public UICommandBuilder append(String documentPath) {
      this.commands.add(new CustomUICommand(CustomUICommandType.Append, null, null, documentPath));
      return this;
   }

   @Nonnull
   public UICommandBuilder append(String selector, String documentPath) {
      this.commands.add(new CustomUICommand(CustomUICommandType.Append, selector, null, documentPath));
      return this;
   }

   @Nonnull
   public UICommandBuilder appendInline(String selector, String document) {
      this.commands.add(new CustomUICommand(CustomUICommandType.AppendInline, selector, null, document));
      return this;
   }

   @Nonnull
   public UICommandBuilder insertBefore(String selector, String documentPath) {
      this.commands.add(new CustomUICommand(CustomUICommandType.InsertBefore, selector, null, documentPath));
      return this;
   }

   @Nonnull
   public UICommandBuilder insertBeforeInline(String selector, String document) {
      this.commands.add(new CustomUICommand(CustomUICommandType.InsertBeforeInline, selector, null, document));
      return this;
   }

   @Nonnull
   @Deprecated
   private UICommandBuilder setBsonValue(String selector, BsonValue bsonValue) {
      BsonDocument valueWrapper = new BsonDocument();
      valueWrapper.put("0", bsonValue);
      this.commands.add(new CustomUICommand(CustomUICommandType.Set, selector, valueWrapper.toJson(), null));
      return this;
   }

   @Nonnull
   public <T> UICommandBuilder set(String selector, @Nonnull Value<T> ref) {
      if (ref.getValue() != null) {
         throw new IllegalArgumentException("Method only accepts references without a direct value");
      } else {
         return this.setBsonValue(selector, ValueCodec.REFERENCE_ONLY.encode(ref));
      }
   }

   @Nonnull
   public UICommandBuilder setNull(String selector) {
      return this.setBsonValue(selector, BsonNull.VALUE);
   }

   @Nonnull
   public UICommandBuilder set(String selector, @Nonnull String str) {
      return this.setBsonValue(selector, new BsonString(str));
   }

   @Nonnull
   public UICommandBuilder set(String selector, @Nonnull Message message) {
      return this.setBsonValue(selector, Message.CODEC.encode(message, EmptyExtraInfo.EMPTY));
   }

   @Nonnull
   public UICommandBuilder set(String selector, boolean b) {
      return this.setBsonValue(selector, new BsonBoolean(b));
   }

   @Nonnull
   public UICommandBuilder set(String selector, float n) {
      return this.setBsonValue(selector, new BsonDouble(n));
   }

   @Nonnull
   public UICommandBuilder set(String selector, int n) {
      return this.setBsonValue(selector, new BsonInt32(n));
   }

   @Nonnull
   public UICommandBuilder set(String selector, double n) {
      return this.setBsonValue(selector, new BsonDouble(n));
   }

   @Nonnull
   public UICommandBuilder setObject(String selector, @Nonnull Object data) {
      Codec codec = CODEC_MAP.get(data.getClass());
      if (codec == null) {
         throw new IllegalArgumentException(data.getClass().getName() + " is not a compatible class");
      } else {
         return this.setBsonValue(selector, codec.encode(data));
      }
   }

   @Nonnull
   public <T> UICommandBuilder set(String selector, @Nonnull T[] data) {
      Codec codec = CODEC_MAP.get(data.getClass().getComponentType());
      if (codec == null) {
         throw new IllegalArgumentException(data.getClass().getName() + " is not a compatible class");
      } else {
         BsonArray arr = new BsonArray();

         for (T d : data) {
            arr.add(codec.encode(d));
         }

         return this.setBsonValue(selector, arr);
      }
   }

   @Nonnull
   public <T> UICommandBuilder set(String selector, @Nonnull List<T> data) {
      Codec<T> codec = null;
      BsonArray arr = new BsonArray();

      for (T d : data) {
         if (codec == null) {
            codec = CODEC_MAP.get(d.getClass());
            if (codec == null) {
               throw new IllegalArgumentException(data.getClass().getName() + " is not a compatible class");
            }
         }

         arr.add(codec.encode(d));
      }

      return this.setBsonValue(selector, arr);
   }

   @Nonnull
   public CustomUICommand[] getCommands() {
      return this.commands.toArray(CustomUICommand[]::new);
   }

   static {
      CODEC_MAP.put(Area.class, Area.CODEC);
      CODEC_MAP.put(ItemGridSlot.class, ItemGridSlot.CODEC);
      CODEC_MAP.put(ItemStack.class, ItemStack.CODEC);
      CODEC_MAP.put(LocalizableString.class, LocalizableString.CODEC);
      CODEC_MAP.put(PatchStyle.class, PatchStyle.CODEC);
      CODEC_MAP.put(DropdownEntryInfo.class, DropdownEntryInfo.CODEC);
      CODEC_MAP.put(Anchor.class, Anchor.CODEC);
   }
}
