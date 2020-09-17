package net.kfoundation.java.serialization;

import java.util.List;
import java.util.Optional;


/**
 * Enables serialization of values of type T using any ObjectSerializer.
 */
public interface ValueWriter<T> {

    /**
     * Invokes the given serializer in proper fashion to write a value of
     * type T to the output.
     */
    void write(ObjectSerializer serializer, T value);


    /**
     * Writes a list of values of type T to the output of the given
     * deserializer.
     */
    default void writeList(ObjectSerializer s, List<T> values) {
        s.writeCollectionBegin();
        for (T v: values) {
            ValueWriter.this.write(s, v);
        }
        s.writeCollectionEnd();
    }


    /**
     * Writes a value of type T if present, otherwise does nothing.
     */
    default void writeOptional(ObjectSerializer s, Optional<T> v) {
        v.ifPresent(t -> ValueWriter.this.write(s, t));
    }


    /**
     * Produces a ValueWriter that can write a list of values of type T.
     */
    default ValueWriter<List<T>> toListWriter() {
        return this::writeList;
    }


    /**
     * Produces a ValueWriter that can optionally writer a value of type T.
     */
    default ValueWriter<Optional<T>> toOptionalWriter() {
        return this::writeOptional;
    }

}
