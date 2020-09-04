package net.kfoundation.java.serialization;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface ValueWriter<T> {
    void write(ObjectSerializer serializer, T value);

    default void writeList(ObjectSerializer s, List<T> values) {
        s.writeCollectionBegin();
        for (T v: values) {
            ValueWriter.this.write(s, v);
        }
        s.writeCollectionEnd();
    }

    default void writeOptional(ObjectSerializer s, Optional<T> v) {
        v.ifPresent(t -> ValueWriter.this.write(s, t));
    }

    default ValueWriter<List<T>> toListWriter() {
        return this::writeList;
    }

    default ValueWriter<Optional<T>> toOptionalWriter() {
        return this::writeOptional;
    }
}
