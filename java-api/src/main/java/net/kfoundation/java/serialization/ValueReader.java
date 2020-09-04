package net.kfoundation.java.serialization;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public interface ValueReader<T> {
    T read(ObjectDeserializer deserializer);

    default T getDefaultValue() {
        throw new DeserializationError("Value is missing and no default is available");
    }

    default List<T> readList(ObjectDeserializer deserializer) {
        LinkedList<T> list = new LinkedList<>();
        deserializer.readCollectionBegin();
        while(!deserializer.tryReadCollectionEnd()) {
            list.add(ValueReader.this.read(deserializer));
        }
        return list;
    }

    default ValueReader<List<T>> toListReader() {
        return this::readList;
    }

    default ValueReader<Optional<T>> toOptionalReader() {
        return new ValueReader<>() {
            @Override
            public Optional<T> read(ObjectDeserializer deserializer) {
                return Optional.of(ValueReader.this.read(deserializer));
            }
            @Override
            public Optional<T> getDefaultValue() {
                return Optional.empty();
            }
        };
    }
}
