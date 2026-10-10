package org.cloudburstmc.proxypass.network.bedrock.util;

import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleBlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;
import org.cloudburstmc.protocol.common.Definition;
import org.cloudburstmc.protocol.common.DefinitionRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;

// Resolves unregistered runtime ids to placeholders instead of null, like WaterdogPE
public class FakeDefinitionRegistry<D extends Definition> implements DefinitionRegistry<D> {
    protected static final IntFunction<BlockDefinition> UNKNOWN_BLOCK =
            runtimeId -> new SimpleBlockDefinition("unknown", runtimeId, NbtMap.EMPTY);
    protected static final IntFunction<ItemDefinition> UNKNOWN_ITEM =
            runtimeId -> new SimpleItemDefinition("unknown", runtimeId, false);

    // Shared by both sides of a session
    private final Map<Integer, D> definitions = new ConcurrentHashMap<>();
    private final IntFunction<? extends D> factory;

    public FakeDefinitionRegistry(IntFunction<? extends D> factory) {
        this.factory = factory;
    }

    public static FakeDefinitionRegistry<BlockDefinition> createBlockRegistry() {
        return new FakeDefinitionRegistry<>(UNKNOWN_BLOCK);
    }

    public static FakeDefinitionRegistry<ItemDefinition> createItemRegistry() {
        return new FakeDefinitionRegistry<>(UNKNOWN_ITEM).register(ItemDefinition.AIR);
    }

    public FakeDefinitionRegistry<D> register(D definition) {
        this.definitions.put(definition.getRuntimeId(), definition);
        return this;
    }

    @Override
    public D getDefinition(int runtimeId) {
        return this.definitions.computeIfAbsent(runtimeId, this.factory::apply);
    }

    @Override
    public boolean isRegistered(D definition) {
        return this.definitions.get(definition.getRuntimeId()) == definition;
    }
}
