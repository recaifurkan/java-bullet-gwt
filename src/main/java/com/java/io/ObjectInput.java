package com.java.io;

import com.java.lang.ClassNotFoundException;

public interface ObjectInput {
  int readInt() throws IOException;
  Object readObject() throws IOException, ClassNotFoundException;
}