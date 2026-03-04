package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CraftingRecipe {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 10;
   public static final int VARIABLE_FIELD_COUNT = 5;
   public static final int VARIABLE_BLOCK_START = 30;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public MaterialQuantity[] inputs;
   @Nullable
   public MaterialQuantity[] outputs;
   @Nullable
   public MaterialQuantity primaryOutput;
   @Nullable
   public BenchRequirement[] benchRequirement;
   public boolean knowledgeRequired;
   public float timeSeconds;
   public int requiredMemoriesLevel;

   public CraftingRecipe() {
   }

   public CraftingRecipe(
      @Nullable String id,
      @Nullable MaterialQuantity[] inputs,
      @Nullable MaterialQuantity[] outputs,
      @Nullable MaterialQuantity primaryOutput,
      @Nullable BenchRequirement[] benchRequirement,
      boolean knowledgeRequired,
      float timeSeconds,
      int requiredMemoriesLevel
   ) {
      this.id = id;
      this.inputs = inputs;
      this.outputs = outputs;
      this.primaryOutput = primaryOutput;
      this.benchRequirement = benchRequirement;
      this.knowledgeRequired = knowledgeRequired;
      this.timeSeconds = timeSeconds;
      this.requiredMemoriesLevel = requiredMemoriesLevel;
   }

   public CraftingRecipe(@Nonnull CraftingRecipe other) {
      this.id = other.id;
      this.inputs = other.inputs;
      this.outputs = other.outputs;
      this.primaryOutput = other.primaryOutput;
      this.benchRequirement = other.benchRequirement;
      this.knowledgeRequired = other.knowledgeRequired;
      this.timeSeconds = other.timeSeconds;
      this.requiredMemoriesLevel = other.requiredMemoriesLevel;
   }

   @Nonnull
   public static CraftingRecipe deserialize(@Nonnull ByteBuf buf, int offset) {
      CraftingRecipe obj = new CraftingRecipe();
      byte nullBits = buf.getByte(offset);
      obj.knowledgeRequired = buf.getByte(offset + 1) != 0;
      obj.timeSeconds = buf.getFloatLE(offset + 2);
      obj.requiredMemoriesLevel = buf.getIntLE(offset + 6);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 30 + buf.getIntLE(offset + 10);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 30 + buf.getIntLE(offset + 14);
         int inputsCount = VarInt.peek(buf, varPos1);
         if (inputsCount < 0) {
            throw ProtocolException.negativeLength("Inputs", inputsCount);
         }

         if (inputsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Inputs", inputsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + inputsCount * 9L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Inputs", varPos1 + varIntLen + inputsCount * 9, buf.readableBytes());
         }

         obj.inputs = new MaterialQuantity[inputsCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < inputsCount; i++) {
            obj.inputs[i] = MaterialQuantity.deserialize(buf, elemPos);
            elemPos += MaterialQuantity.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 30 + buf.getIntLE(offset + 18);
         int outputsCount = VarInt.peek(buf, varPos2);
         if (outputsCount < 0) {
            throw ProtocolException.negativeLength("Outputs", outputsCount);
         }

         if (outputsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Outputs", outputsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
         if (varPos2 + varIntLen + outputsCount * 9L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Outputs", varPos2 + varIntLen + outputsCount * 9, buf.readableBytes());
         }

         obj.outputs = new MaterialQuantity[outputsCount];
         int elemPos = varPos2 + varIntLen;

         for (int i = 0; i < outputsCount; i++) {
            obj.outputs[i] = MaterialQuantity.deserialize(buf, elemPos);
            elemPos += MaterialQuantity.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits & 8) != 0) {
         int varPos3 = offset + 30 + buf.getIntLE(offset + 22);
         obj.primaryOutput = MaterialQuantity.deserialize(buf, varPos3);
      }

      if ((nullBits & 16) != 0) {
         int varPos4 = offset + 30 + buf.getIntLE(offset + 26);
         int benchRequirementCount = VarInt.peek(buf, varPos4);
         if (benchRequirementCount < 0) {
            throw ProtocolException.negativeLength("BenchRequirement", benchRequirementCount);
         }

         if (benchRequirementCount > 4096000) {
            throw ProtocolException.arrayTooLong("BenchRequirement", benchRequirementCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos4);
         if (varPos4 + varIntLen + benchRequirementCount * 6L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("BenchRequirement", varPos4 + varIntLen + benchRequirementCount * 6, buf.readableBytes());
         }

         obj.benchRequirement = new BenchRequirement[benchRequirementCount];
         int elemPos = varPos4 + varIntLen;

         for (int i = 0; i < benchRequirementCount; i++) {
            obj.benchRequirement[i] = BenchRequirement.deserialize(buf, elemPos);
            elemPos += BenchRequirement.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 30;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 10);
         int pos0 = offset + 30 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 14);
         int pos1 = offset + 30 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += MaterialQuantity.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 18);
         int pos2 = offset + 30 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2);

         for (int i = 0; i < arrLen; i++) {
            pos2 += MaterialQuantity.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 22);
         int pos3 = offset + 30 + fieldOffset3;
         pos3 += MaterialQuantity.computeBytesConsumed(buf, pos3);
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 26);
         int pos4 = offset + 30 + fieldOffset4;
         int arrLen = VarInt.peek(buf, pos4);
         pos4 += VarInt.length(buf, pos4);

         for (int i = 0; i < arrLen; i++) {
            pos4 += BenchRequirement.computeBytesConsumed(buf, pos4);
         }

         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.inputs != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.outputs != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.primaryOutput != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.benchRequirement != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.knowledgeRequired ? 1 : 0);
      buf.writeFloatLE(this.timeSeconds);
      buf.writeIntLE(this.requiredMemoriesLevel);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int inputsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int outputsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int primaryOutputOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int benchRequirementOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.inputs != null) {
         buf.setIntLE(inputsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.inputs.length > 4096000) {
            throw ProtocolException.arrayTooLong("Inputs", this.inputs.length, 4096000);
         }

         VarInt.write(buf, this.inputs.length);

         for (MaterialQuantity item : this.inputs) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(inputsOffsetSlot, -1);
      }

      if (this.outputs != null) {
         buf.setIntLE(outputsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.outputs.length > 4096000) {
            throw ProtocolException.arrayTooLong("Outputs", this.outputs.length, 4096000);
         }

         VarInt.write(buf, this.outputs.length);

         for (MaterialQuantity item : this.outputs) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(outputsOffsetSlot, -1);
      }

      if (this.primaryOutput != null) {
         buf.setIntLE(primaryOutputOffsetSlot, buf.writerIndex() - varBlockStart);
         this.primaryOutput.serialize(buf);
      } else {
         buf.setIntLE(primaryOutputOffsetSlot, -1);
      }

      if (this.benchRequirement != null) {
         buf.setIntLE(benchRequirementOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.benchRequirement.length > 4096000) {
            throw ProtocolException.arrayTooLong("BenchRequirement", this.benchRequirement.length, 4096000);
         }

         VarInt.write(buf, this.benchRequirement.length);

         for (BenchRequirement item : this.benchRequirement) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(benchRequirementOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 30;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.inputs != null) {
         int inputsSize = 0;

         for (MaterialQuantity elem : this.inputs) {
            inputsSize += elem.computeSize();
         }

         size += VarInt.size(this.inputs.length) + inputsSize;
      }

      if (this.outputs != null) {
         int outputsSize = 0;

         for (MaterialQuantity elem : this.outputs) {
            outputsSize += elem.computeSize();
         }

         size += VarInt.size(this.outputs.length) + outputsSize;
      }

      if (this.primaryOutput != null) {
         size += this.primaryOutput.computeSize();
      }

      if (this.benchRequirement != null) {
         int benchRequirementSize = 0;

         for (BenchRequirement elem : this.benchRequirement) {
            benchRequirementSize += elem.computeSize();
         }

         size += VarInt.size(this.benchRequirement.length) + benchRequirementSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 30) {
         return ValidationResult.error("Buffer too small: expected at least 30 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 10);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 30 + idOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Id");
            }

            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 2) != 0) {
            int inputsOffset = buffer.getIntLE(offset + 14);
            if (inputsOffset < 0) {
               return ValidationResult.error("Invalid offset for Inputs");
            }

            int posx = offset + 30 + inputsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Inputs");
            }

            int inputsCount = VarInt.peek(buffer, posx);
            if (inputsCount < 0) {
               return ValidationResult.error("Invalid array count for Inputs");
            }

            if (inputsCount > 4096000) {
               return ValidationResult.error("Inputs exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < inputsCount; i++) {
               ValidationResult structResult = MaterialQuantity.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid MaterialQuantity in Inputs[" + i + "]: " + structResult.error());
               }

               posx += MaterialQuantity.computeBytesConsumed(buffer, posx);
            }
         }

         if ((nullBits & 4) != 0) {
            int outputsOffset = buffer.getIntLE(offset + 18);
            if (outputsOffset < 0) {
               return ValidationResult.error("Invalid offset for Outputs");
            }

            int posxx = offset + 30 + outputsOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Outputs");
            }

            int outputsCount = VarInt.peek(buffer, posxx);
            if (outputsCount < 0) {
               return ValidationResult.error("Invalid array count for Outputs");
            }

            if (outputsCount > 4096000) {
               return ValidationResult.error("Outputs exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);

            for (int i = 0; i < outputsCount; i++) {
               ValidationResult structResult = MaterialQuantity.validateStructure(buffer, posxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid MaterialQuantity in Outputs[" + i + "]: " + structResult.error());
               }

               posxx += MaterialQuantity.computeBytesConsumed(buffer, posxx);
            }
         }

         if ((nullBits & 8) != 0) {
            int primaryOutputOffset = buffer.getIntLE(offset + 22);
            if (primaryOutputOffset < 0) {
               return ValidationResult.error("Invalid offset for PrimaryOutput");
            }

            int posxxx = offset + 30 + primaryOutputOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for PrimaryOutput");
            }

            ValidationResult primaryOutputResult = MaterialQuantity.validateStructure(buffer, posxxx);
            if (!primaryOutputResult.isValid()) {
               return ValidationResult.error("Invalid PrimaryOutput: " + primaryOutputResult.error());
            }

            posxxx += MaterialQuantity.computeBytesConsumed(buffer, posxxx);
         }

         if ((nullBits & 16) != 0) {
            int benchRequirementOffset = buffer.getIntLE(offset + 26);
            if (benchRequirementOffset < 0) {
               return ValidationResult.error("Invalid offset for BenchRequirement");
            }

            int posxxxx = offset + 30 + benchRequirementOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for BenchRequirement");
            }

            int benchRequirementCount = VarInt.peek(buffer, posxxxx);
            if (benchRequirementCount < 0) {
               return ValidationResult.error("Invalid array count for BenchRequirement");
            }

            if (benchRequirementCount > 4096000) {
               return ValidationResult.error("BenchRequirement exceeds max length 4096000");
            }

            posxxxx += VarInt.length(buffer, posxxxx);

            for (int i = 0; i < benchRequirementCount; i++) {
               ValidationResult structResult = BenchRequirement.validateStructure(buffer, posxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid BenchRequirement in BenchRequirement[" + i + "]: " + structResult.error());
               }

               posxxxx += BenchRequirement.computeBytesConsumed(buffer, posxxxx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public CraftingRecipe clone() {
      CraftingRecipe copy = new CraftingRecipe();
      copy.id = this.id;
      copy.inputs = this.inputs != null ? Arrays.stream(this.inputs).map(e -> e.clone()).toArray(MaterialQuantity[]::new) : null;
      copy.outputs = this.outputs != null ? Arrays.stream(this.outputs).map(e -> e.clone()).toArray(MaterialQuantity[]::new) : null;
      copy.primaryOutput = this.primaryOutput != null ? this.primaryOutput.clone() : null;
      copy.benchRequirement = this.benchRequirement != null ? Arrays.stream(this.benchRequirement).map(e -> e.clone()).toArray(BenchRequirement[]::new) : null;
      copy.knowledgeRequired = this.knowledgeRequired;
      copy.timeSeconds = this.timeSeconds;
      copy.requiredMemoriesLevel = this.requiredMemoriesLevel;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CraftingRecipe other)
            ? false
            : Objects.equals(this.id, other.id)
               && Arrays.equals((Object[])this.inputs, (Object[])other.inputs)
               && Arrays.equals((Object[])this.outputs, (Object[])other.outputs)
               && Objects.equals(this.primaryOutput, other.primaryOutput)
               && Arrays.equals((Object[])this.benchRequirement, (Object[])other.benchRequirement)
               && this.knowledgeRequired == other.knowledgeRequired
               && this.timeSeconds == other.timeSeconds
               && this.requiredMemoriesLevel == other.requiredMemoriesLevel;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Arrays.hashCode((Object[])this.inputs);
      result = 31 * result + Arrays.hashCode((Object[])this.outputs);
      result = 31 * result + Objects.hashCode(this.primaryOutput);
      result = 31 * result + Arrays.hashCode((Object[])this.benchRequirement);
      result = 31 * result + Boolean.hashCode(this.knowledgeRequired);
      result = 31 * result + Float.hashCode(this.timeSeconds);
      return 31 * result + Integer.hashCode(this.requiredMemoriesLevel);
   }
}
