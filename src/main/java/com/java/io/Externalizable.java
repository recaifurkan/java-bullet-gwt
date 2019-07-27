package com.java.io;


import com.java.lang.ClassNotFoundException;

public interface Externalizable {
  void readExternal(ObjectInput in) throws IOException, ClassNotFoundException;
  void writeExternal(ObjectOutput out) throws IOException;
}