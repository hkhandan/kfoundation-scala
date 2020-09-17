// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.java.serialization;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;



/**
 * Enables reading of specific type of value using any deserializer.
 */
public interface ValueReader<T> {

    /**
     * Invokes the given deserializer in proper fashion to read a value of
     * type T.
     */
    T read(ObjectDeserializer deserializer);


    /**
     * If an object in the stream being read is expected to have a property of
     * type T, but such property is missing, the value returned by this method
     * will be used instead. Default behavior of this method is not to allow
     * such properties to be missing by throwing an exception.
     */
    default T getDefaultValue() {
        throw new DeserializationError("Value is missing and no default is available");
    }


    /**
     * Reads a list of items of type T.
     */
    default List<T> readList(ObjectDeserializer deserializer) {
        LinkedList<T> list = new LinkedList<>();
        deserializer.readCollectionBegin();
        while(!deserializer.tryReadCollectionEnd()) {
            list.add(ValueReader.this.read(deserializer));
        }
        return list;
    }


    /**
     * Produces a ValueReader that can read a list of values of type T.
     */
    default ValueReader<List<T>> toListReader() {
        return this::readList;
    }


    /**
     * Produces a reader to read a value of type T, with default for missing
     * value being None (rather than throwing an error).
     */
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
