/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.kfoundation

object MurmurHash3 {

    // Constants for 32-bit variant
    private val C1_32: Int = 0xcc9e2d51
    private val C2_32: Int = 0x1b873593
    private val R1_32: Int = 15
    private val R2_32: Int = 13
    private val M_32 : Int = 5
    private val N_32 : Int = 0xe6546b64

    // Constants for 128-bit variant
    private val C1: Long = 0x87c37b91114253d5L
    private val C2: Long = 0x4cf5ad432745937fL
    private val R1: Int = 31
    private val R2: Int = 27
    private val R3: Int = 33
    private val M : Int = 5
    private val N1: Int = 0x52dce729
    private val N2: Int = 0x38495ab5

    private def rotateLeft(i: Long, distance: Int): Long =
        (i << distance) | (i >>> -distance)

    private def rotateLeft(i: Int, distance: Int): Int =
        (i << distance) | (i >>> -distance)

    private def getLittleEndianInt(data: Array[Byte], index: Int) =
        (data(index     ) & 0xff       ) |
        ((data(index + 1) & 0xff) <<  8) |
        ((data(index + 2) & 0xff) << 16) |
        ((data(index + 3) & 0xff) << 24)

    private def mix32(k: Int, hash: Int): Int = {
        val h = hash ^ rotateLeft(k * C1_32, R1_32) * C2_32
        rotateLeft(h, R2_32) * M_32 + N_32
    }

    private def getLittleEndianLong(data: Seq[Byte], index: Int): Long =
        (data(index).toLong & 0xff) |
        ((data(index + 1).toLong & 0xff) << 8) |
        ((data(index + 2).toLong & 0xff) << 16) |
        ((data(index + 3).toLong & 0xff) << 24) |
        ((data(index + 4).toLong & 0xff) << 32) |
        ((data(index + 5).toLong & 0xff) << 40) |
        ((data(index + 6).toLong & 0xff) << 48) |
        ((data(index + 7).toLong & 0xff) << 56)

    private def fMix64(h: Long): Long = {
        var hash = h
        hash ^= (hash >>> 33)
        hash *= 0xff51afd7ed558ccdL
        hash ^= (hash >>> 33)
        hash *= 0xc4ceb9fe1a85ec53L
        hash ^ (hash >>> 33)
    }

    def hash32x86(data: Array[Byte]): Int = hash32x86(data, 0, data.length, 0)

    def hash32x86(data: Array[Byte], offset: Int, length: Int, seed: Int): Int = {
        var hash: Int = seed
        val nBlocks: Int = length >> 2

        // body
        Range(0, nBlocks).foreach(i => {
            val index: Int = offset + (i << 2)
            val k: Int = getLittleEndianInt(data, index)
            hash = mix32(k, hash)
        })

        // tail
        val index: Int = offset + (nBlocks << 2)
        var k1: Int = 0
        val pos = offset + length - index
        if(pos >= 3) k1 ^= (data(index + 2) & 0xff) << 16
        if(pos >= 2) k1 ^= (data(index + 1) & 0xff) << 8
        if(pos >= 1) k1 ^= (data(index) & 0xff)

        // mix functions
        k1 *= C1_32
        k1 = Integer.rotateLeft(k1, R1_32)
        k1 *= C2_32
        hash ^= k1
        hash ^ length
    }


    private def hash128x64Internal(data: Seq[Byte], offset: Int, length: Int,
        seed: Long): Array[Long] =
    {
        var h1 = seed
        var h2 = seed
        val nBlocks = length >> 4

        // body
        Range(0, nBlocks).foreach({i =>
            val index = offset + (i << 4)
            var k1 = getLittleEndianLong(data, index)
            var k2 = getLittleEndianLong(data, index + 8)

            // mix functions for k1
            k1 *= C1
            k1 = rotateLeft(k1, R1)
            k1 *= C2
            h1 ^= k1
            h1 = rotateLeft(h1, R2)
            h1 += h2
            h1 = h1 * M + N1

            // mix functions for k2
            k2 *= C2
            k2 = rotateLeft(k2, R3)
            k2 *= C1
            h2 ^= k2
            h2 = rotateLeft(h2, R1)
            h2 += h1
            h2 = h2 * M + N2
        })

        // tail
        var k1: Long = 0
        var k2: Long = 0
        val index = offset + (nBlocks << 4)
        val pos = offset + length - index

        if(pos >= 15) k2 ^= (data(index + 14).toLong & 0xff) << 48
        if(pos >= 14) k2 ^= (data(index + 13).toLong & 0xff) << 40
        if(pos >= 13) k2 ^= (data(index + 12).toLong & 0xff) << 32
        if(pos >= 12) k2 ^= (data(index + 11).toLong & 0xff) << 24
        if(pos >= 11) k2 ^= (data(index + 10).toLong & 0xff) << 16
        if(pos >= 10) k2 ^= (data(index + 9).toLong & 0xff) << 8
        if(pos >= 9) k2 ^= (data(index + 8).toLong & 0xff)

        k2 *= C2
        k2 = rotateLeft(k2, R3)
        k2 *= C1
        h2 ^= k2

        if(pos >= 8) k1 ^= (data(index + 7).toLong & 0xff) << 56
        if(pos >= 7) k1 ^= (data(index + 6).toLong & 0xff) << 48
        if(pos >= 6) k1 ^= (data(index + 5).toLong & 0xff) << 40
        if(pos >= 5) k1 ^= (data(index + 4).toLong & 0xff) << 32
        if(pos >= 4) k1 ^= (data(index + 3).toLong & 0xff) << 24
        if(pos >= 3) k1 ^= (data(index + 2).toLong & 0xff) << 16
        if(pos >= 2) k1 ^= (data(index + 1).toLong & 0xff) << 8
        if(pos >= 1) k1 ^= data(index) & 0xff

        k1 *= C1
        k1 = rotateLeft(k1, R1)
        k1 *= C2
        h1 ^= k1

        // finalization
        h1 ^= length
        h2 ^= length

        h1 += h2
        h2 += h1

        h1 = fMix64(h1)
        h2 = fMix64(h2)

        h1 += h2
        h2 += h1

        Array(h1, h2)
    }

    def hash128x64(data: Seq[Byte]): Array[Long] = hash128x64(data, 0, data.length, 0)

    def hash128x64(data: Seq[Byte], offset: Int, length: Int, seed: Int): Array[Long] =
        hash128x64Internal(data, offset, length, seed & 0xffffffffL)
}