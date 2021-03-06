/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.hadoop.ozone.common;

import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

/** {@link ChunkBuffer} implementation using a single {@link ByteBuffer}. */
public final class ChunkBufferImplWithByteBuffer implements ChunkBuffer {
  private final ByteBuffer buffer;

  ChunkBufferImplWithByteBuffer(ByteBuffer buffer) {
    this.buffer = Objects.requireNonNull(buffer, "buffer == null");
  }

  @Override
  public int position() {
    return buffer.position();
  }

  @Override
  public int remaining() {
    return buffer.remaining();
  }

  @Override
  public Iterable<ByteBuffer> iterate(int bytesPerChecksum) {
    return () -> new Iterator<ByteBuffer>() {
      @Override
      public boolean hasNext() {
        return buffer.hasRemaining();
      }

      @Override
      public ByteBuffer next() {
        final ByteBuffer duplicated = buffer.duplicate();
        final int min = Math.min(
            buffer.position() + bytesPerChecksum, buffer.limit());
        duplicated.limit(min);
        buffer.position(min);
        return duplicated;
      }
    };
  }

  @Override
  public ChunkBuffer duplicate(int newPosition, int newLimit) {
    final ByteBuffer duplicated = buffer.duplicate();
    duplicated.position(newPosition).limit(newLimit);
    return new ChunkBufferImplWithByteBuffer(duplicated);
  }

  @Override
  public void put(ByteBuffer b) {
    buffer.put(b);
  }

  @Override
  public void clear() {
    buffer.clear();
  }

  @Override
  public ByteString toByteString(Function<ByteBuffer, ByteString> function) {
    return function.apply(buffer);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof ChunkBufferImplWithByteBuffer)) {
      return false;
    }
    final ChunkBufferImplWithByteBuffer that
        = (ChunkBufferImplWithByteBuffer)obj;
    return this.buffer.equals(that.buffer);
  }

  @Override
  public int hashCode() {
    return buffer.hashCode();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ":limit=" + buffer.limit();
  }
}
