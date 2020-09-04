package net.kfoundation.java.io;

import scala.collection.Seq;
import scala.jdk.CollectionConverters;
import scala.jdk.javaapi.OptionConverters;

import java.io.*;
import java.util.List;
import java.util.Optional;


public class Path {

    private final net.kfoundation.scala.io.Path impl;

    private Path(net.kfoundation.scala.io.Path impl) {
        this.impl = impl;
    }

    public static Path of(net.kfoundation.scala.io.Path scalaPath) {
        return new Path(scalaPath);
    }

    public net.kfoundation.scala.io.Path toScala() {
        return impl;
    }


    // --- Delegated Methods --- //

    public boolean isRelative() {
        return impl.isRelative();
    }

    public List<String> segments() {
        return CollectionConverters.<String>SeqHasAsJava(
                (Seq<String>)impl.segments())
            .asJava();
    }

    public Optional<String> getFileName() {
        return OptionConverters.<String>toJava(impl.getFileName());
    }

    public Optional<String> getExtension() {
        return OptionConverters.<String>toJava(impl.getExtension());
    }

    public java.nio.file.Path toJavaPath() {
        return impl.toJavaPath();
    }

    public File toJavaFile() {
        return impl.toJavaFile();
    }

    public net.kfoundation.scala.io.Path getParent() {
        return impl.getParent();
    }

    public FileInputStream getInputStream() {
        return impl.getInputStream();
    }

    public FileOutputStream getOutputStream() {
        return impl.getOutputStream();
    }

    public FileReader getReader() {
        return impl.getReader();
    }

    public FileWriter getWriter() {
        return impl.getWriter();
    }

    public Path add(String segment) {
        return of(impl.add(segment));
    }

    @Override
    public String toString() {
        return impl.toString();
    }
}
