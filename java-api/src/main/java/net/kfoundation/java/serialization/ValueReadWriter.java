package net.kfoundation.java.serialization;

import java.util.List;
import java.util.Optional;

public interface ValueReadWriter<T> extends ValueReader<T>, ValueWriter<T> {

    default ValueReadWriter<List<T>> toListReadWriter() {
        return new ValueReadWriter<>() {
            @Override
            public List<T> read(ObjectDeserializer deserializer) {
                return ValueReadWriter.this.readList(deserializer);
            }
            @Override
            public void write(ObjectSerializer serializer, List<T> value) {
                ValueReadWriter.this.writeList(serializer, value);
            }
        };
    }

    default ValueReadWriter<Optional<T>> toOptionalWriter() {
        return new ValueReadWriter<>() {
            @Override
            public Optional<T> read(ObjectDeserializer deserializer) {
                return Optional.of(ValueReadWriter.this.read(deserializer));
            }

            @Override
            public void write(ObjectSerializer serializer, Optional<T> value) {
                value.ifPresent(v -> ValueReadWriter.this.write(serializer, v));
            }
        };
    }

}
