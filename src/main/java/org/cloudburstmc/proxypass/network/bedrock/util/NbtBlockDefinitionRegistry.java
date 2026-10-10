package org.cloudburstmc.proxypass.network.bedrock.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;

import java.util.List;

public class NbtBlockDefinitionRegistry extends FakeDefinitionRegistry<BlockDefinition> {

    public NbtBlockDefinitionRegistry(List<NbtMap> definitions, boolean hashed) {
        super(UNKNOWN_BLOCK);
        int counter = 0;
        for (NbtMap definition : definitions) {
            int runtimeId = hashed ? BlockPaletteUtils.createHash(definition) : counter++;
            this.register(new NbtBlockDefinition(runtimeId, definition));
        }
    }

    public record NbtBlockDefinition(@JsonIgnoreProperties int runtimeId, NbtMap tag) implements BlockDefinition {
        @Override
        @JsonIgnore
        public int getRuntimeId() {
            return runtimeId;
        }
    }
}
