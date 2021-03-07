// --------------------------------------------------------------------------
//   ██╗  ██╗███████╗
//   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
//   █████╔╝ █████╗     KFoundation for Scala Library
//   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
//   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
//   ╚═╝  ╚═╝╚═╝
// --------------------------------------------------------------------------

package net.kfoundation.java.io;

import net.kfoundation.scala.UString;
import scala.jdk.CollectionConverters;
import scala.jdk.javaapi.OptionConverters;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Unified abstraction and interface to work with file system. This class is
 * used throughout the KFoundation in place of Java's File and Path classes.
 */
public class Path {

    private final net.kfoundation.scala.io.Path impl;


    private Path(net.kfoundation.scala.io.Path impl) {
        this.impl = impl;
    }


    /**
     * Coverts Scala implementation to Java.
     */
    public static Path of(net.kfoundation.scala.io.Path scalaPath) {
        return new Path(scalaPath);
    }


    /**
     * Converts Java implementation to Scala.
     */
    public net.kfoundation.scala.io.Path toScala() {
        return impl;
    }



    // --- Delegated Methods --- //

    /**
     * Tests if this path is relative or absolute.
     */
    public boolean isRelative() {
        return impl.isRelative();
    }


    /**
     * Returns individual segments of this path.
     */
    public List<String> segments() {
        return CollectionConverters.SeqHasAsJava(impl.segments())
            .asJava()
            .stream()
            .map(UString::toString)
            .collect(Collectors.toList());
    }


    /**
     * Returns the last segment of this path if any.
     */
    public Optional<String> getFileName() {
        return OptionConverters.toJava(impl.fileName())
            .map(UString::toString);
    }


    /**
     * Returns the last portion of this path after a period '.' if any.
     */
    public Optional<String> getExtension() {
        return OptionConverters.toJava(impl.extension())
                .map(UString::toString);
    }


    /**
     * Converts this to a java.nio.file.Path object.
     */
    public java.nio.file.Path toJavaPath() {
        return impl.toJavaPath();
    }


    /**
     * Converts this to a java.io.File object.
     */
    public File toJavaFile() {
        return impl.toJavaFile();
    }


    /**
     * Gets the parent of this path, i.e. all segments minus the last one.
     */
    public net.kfoundation.scala.io.Path getParent() {
        return impl.parent();
    }


    /**
     * Opens an InputStream to read from the file pointed to by this path.
     */
    public FileInputStream getInputStream() {
        return impl.newInputStream();
    }


    /**
     * Opens an OutputStream to write to the file pointed to by this path.
     */
    public FileOutputStream getOutputStream() {
        return impl.newOutputStream();
    }


    /**
     * Creates a FileReader to read from the file pointed to by this path.
     */
    public FileReader getReader() {
        return impl.newReader();
    }


    /**
     * Creates a FileWriter to write to the file pointed to by this path.
     */
    public FileWriter getWriter() {
        return impl.newWriter();
    }


    /**
     * Creates a subpath by adding a segment to this path.
     * @throws IllegalArgumentException if the parameter contains a path
     * separator character.
     */
    public Path add(String segment) {
        return of(impl.append(UString.of(segment)));
    }


    @Override
    public String toString() {
        return impl.toString();
    }

}
