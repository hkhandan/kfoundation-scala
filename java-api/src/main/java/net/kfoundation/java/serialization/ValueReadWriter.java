// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.java.serialization;

import java.util.List;
import java.util.Optional;



/**
 * A ValueReadWriter for type T can act as both ValueReader and ValueReader
 * for that type.
 */
public interface ValueReadWriter<T> extends ValueReader<T>, ValueWriter<T> {


    /**
     * Produces a read-writer for list of values of type T.
     */
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


    /**
     * Produces a read-writer for where value of type T is optional.
     */
    default ValueReadWriter<Optional<T>> toOptionalReadWriter() {
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
