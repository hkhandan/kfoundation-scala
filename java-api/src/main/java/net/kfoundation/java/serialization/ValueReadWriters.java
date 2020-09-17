// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.java.serialization;

import net.kfoundation.java.UString;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * ValueReadWriters for basic types, and factories to create one for complex
 * types (i.e. objects).
 */
public class ValueReadWriters {

    private static class ObjectReader implements ValueReader<Map<UString, Object>> {
        private final UString typeName;
        private final Map<UString, ValueReader<?>> fieldMap;

        public ObjectReader(UString typeName, Map<UString, ValueReader<?>> fieldMap) {
            this.typeName = typeName;
            this.fieldMap = fieldMap;
        }

        @Override
        public Map<UString, Object> read(ObjectDeserializer deserializer) {
            deserializer.readObjectBegin().ifPresent(name -> {
                if(!name.equals(typeName)){
                    throw new DeserializationError(
                        "Trying to read object of type " + typeName + " but found: " + name);
                }
            });

            Map<UString, Object> result = new HashMap<>();

            for(Optional<UString> maybeProperty = deserializer.tryReadPropertyName();
                maybeProperty.isPresent();
                maybeProperty = deserializer.tryReadPropertyName())
            {
                UString pName = maybeProperty.get();
                ValueReader<?> reader = fieldMap.get(pName);
                if(reader == null) {
                    throw new DeserializationError(
                        "Found unexpected field in input while reading object of type "
                        + typeName + ": " + pName);
                }
                result.put(pName, reader.read(deserializer));
            }

            deserializer.readObjectEnd();

            return result;
        }
    }

    private static class AutoReaderByConstructor<T> implements ValueReader<T> {
        private final Constructor<T> constructor;
        private final List<UString> params;
        private final ObjectReader reader;

        public AutoReaderByConstructor(Constructor<T> constructor,
                                       Map<Parameter, ValueReader<?>> readers)
        {
            Map<UString, ValueReader<?>> fieldMap = readers.entrySet()
                .stream()
                .collect(Collectors.toMap(
                    e -> UString.of(getPropertyName(e.getKey())),
                    Map.Entry::getValue));

            reader = new ObjectReader(
                UString.of(getTypeName(constructor.getDeclaringClass())),
                fieldMap);

            params = Arrays.stream(constructor.getParameters())
                .map(p -> UString.of(p.getName()))
                .collect(Collectors.toList());

            this.constructor = constructor;
        }

        @Override
        public T read(ObjectDeserializer deserializer) {
            Map<UString, Object> raw = reader.read(deserializer);

            try {
                return constructor.newInstance(params.stream()
                    .map(raw::get)
                    .toArray());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new DeserializationError(
                    "Error instantiating deserialized object: " + e.getMessage());
            }
        }

        @Override
        public T getDefaultValue() {
            return null;
        }
    }


    private static class AutoReaderByFields<T> implements ValueReader<T> {
        private final Class<T> cls;
        private final ObjectReader reader;
        private final Map<UString, Field> fieldByName;

        public AutoReaderByFields(Class<T> cls, Map<Field, ValueReader<?>> fieldMap) {
            this.cls = cls;
            this.fieldByName = fieldMap.entrySet().stream().collect(Collectors.toMap(
                e -> UString.of(getPropertyName(e.getKey())),
                Map.Entry::getKey));

            Map<UString, ValueReader<?>> readerMap = fieldMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                    e -> UString.of(getPropertyName(e.getKey())),
                    Map.Entry::getValue));

            reader = new ObjectReader(UString.of(getTypeName(cls)), readerMap);
        }

        @Override
        public T read(ObjectDeserializer deserializer) {
            Map<UString, Object> raw = reader.read(deserializer);

            T obj;

            try {
                obj = cls.getConstructor().newInstance();
            } catch (InstantiationException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException e)
            {
                throw new DeserializationError("Error instantiating "
                    + cls.getCanonicalName() + " for deserialization: " + e);
            }

            raw.forEach((key, value) -> {
                Field f = fieldByName.get(key);
                try {
                    f.set(obj, value);
                } catch (IllegalAccessException exp) {
                    throw new DeserializationError("Error assigning field "
                        + cls.getCanonicalName() + "." + f.getName() + ": "
                        + exp);
                }
            });

            return obj;
        }
    }


    /**
     * Read-writer for boolean / Boolean.
     */
    public static ValueReadWriter<Boolean> BOOLEAN = new ValueReadWriter<>() {
        @Override
        public Boolean read(ObjectDeserializer deserializer) {
            return deserializer.readBooleanLiteral();
        }

        @Override
        public void write(ObjectSerializer serializer, Boolean value) {
            serializer.writeLiteral(value);
        }
    };


    /**
     * Read-writer for int / Integer.
     */
    public static ValueReadWriter<Integer> INT = new ValueReadWriter<>() {
        @Override
        public Integer read(ObjectDeserializer deserializer) {
            return (int) deserializer.readIntegerLiteral();
        }

        @Override
        public void write(ObjectSerializer serializer, Integer value) {
            serializer.writeLiteral(value);
        }
    };


    /**
     * Read-writer for long / Long.
     */
    public static ValueReadWriter<Long> LONG = new ValueReadWriter<>() {
        @Override
        public Long read(ObjectDeserializer deserializer) {
            return deserializer.readIntegerLiteral();
        }

        @Override
        public void write(ObjectSerializer serializer, Long value) {
            serializer.writeLiteral(value);
        }
    };


    /**
     * Read-writer for float / Float
     */
    public static ValueReadWriter<Float> FLOAT = new ValueReadWriter<>() {
        @Override
        public Float read(ObjectDeserializer deserializer) {
            return (float) deserializer.readDecimalLiteral();
        }

        @Override
        public void write(ObjectSerializer serializer, Float value) {
            serializer.writeLiteral(value);
        }
    };


    /**
     * Read-writer for double / Double.
     */
    public static ValueReadWriter<Double> DOUBLE = new ValueReadWriter<>() {
        @Override
        public Double read(ObjectDeserializer deserializer) {
            return deserializer.readDecimalLiteral();
        }

        @Override
        public void write(ObjectSerializer serializer, Double value) {
            serializer.writeLiteral(value);
        }
    };


    /**
     * Read-writer for UString.
     */
    public static ValueReadWriter<UString> USTRING = new ValueReadWriter<>() {
        @Override
        public UString read(ObjectDeserializer deserializer) {
            return deserializer.readStringLiteral();
        }

        @Override
        public void write(ObjectSerializer serializer, UString value) {
            serializer.writeLiteral(value);
        }
    };


    /**
     * Read-writer for String.
     */
    public static ValueReadWriter<String> STRING = new ValueReadWriter<>() {
        @Override
        public String read(ObjectDeserializer deserializer) {
            return deserializer.readStringLiteral().toString();
        }

        @Override
        public void write(ObjectSerializer serializer, String value) {
            serializer.writeLiteral(UString.of(value));
        }
    };

    private static final Map<Class<?>, ValueReader<?>> READERS = new HashMap<>();

    static {
        READERS.put(Boolean.class, BOOLEAN);
        READERS.put(boolean.class, BOOLEAN);
        READERS.put(Integer.class, INT);
        READERS.put(int.class, INT);
        READERS.put(Long.class, LONG);
        READERS.put(long.class, LONG);
        READERS.put(Float.class, FLOAT);
        READERS.put(float.class, FLOAT);
        READERS.put(Double.class, DOUBLE);
        READERS.put(double.class, DOUBLE);
        READERS.put(String.class, STRING);
        READERS.put(UString.class, USTRING);
    }


    @SuppressWarnings("unchecked")
    private static <T> Optional<Constructor<T>> getCreator(Class<T> cls) {
        for(Constructor<?> c : cls.getDeclaredConstructors()) {
            if(c.getDeclaredAnnotation(Creator.class) != null) {
                return Optional.of((Constructor<T>)c);
            }
        }
        return Optional.empty();
    }


    private static Map<Parameter, ValueReader<?>> getCreatorDefinedFields(Constructor<?> c) {
        return Stream.of(c.getParameters()).collect(Collectors.toMap(
            p -> p,
            p -> getReader(p.getType())));
    }


    private static Map<Field, ValueReader<?>> getObjectDefinedFields(Class<?> cls) {
        return Stream.of(cls.getFields()).collect(Collectors.toMap(
            p -> p,
            p -> getReader(p.getType())));
    }


    private static String getPropertyName(Parameter p) {
        SerializedName n = p.getAnnotation(SerializedName.class);
        if(n != null) {
            return n.value();
        }
        return p.getName();
    }


    private static String getPropertyName(Field f) {
        SerializedName n = f.getAnnotation(SerializedName.class);
        if(n != null) {
            return n.value();
        }
        return f.getName();
    }


    private static String getTypeName(Class<?> cls) {
        SerializedName n = cls.getAnnotation(SerializedName.class);
        if(n != null) {
            return n.value();
        }
        return cls.getSimpleName();
    }


    private static ValueReader<?> getReader(Class<?> cls) {
        if(READERS.containsKey(cls)) {
            return READERS.get(cls);
        }

        for(Field f: cls.getDeclaredFields()) {
            if(!Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            Class<?> fc = f.getType();
            if(!ValueReader.class.isAssignableFrom(fc)) {
                continue;
            }
            try {
                fc.getMethod("read", cls);
                ValueReader<?> reader = (ValueReader<?>)f.get(null);
                READERS.put(cls, reader);
                return reader;
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new DeserializationError("Error finding reader for "
                    + cls.getName() + ": " + e.getMessage(), e);
            }
        }

        throw new DeserializationError("Could not find reader for " + cls.getCanonicalName());
    }


    /**
     * Produces a read-writer for the given class using reflection.
     */
    public <T> ValueReader<T> readerOf(Class<T> cls) {
        Optional<Constructor<T>> creator =  getCreator(cls);
        if(creator.isPresent()) {
            Constructor<T> c = creator.get();
            return new AutoReaderByConstructor<>(c, getCreatorDefinedFields(c));
        }
        return new AutoReaderByFields<>(cls, getObjectDefinedFields(cls));
    }


    private ValueReadWriters() {}

}
