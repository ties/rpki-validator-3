/**
 * The BSD License
 *
 * Copyright (c) 2010-2018 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.ripe.rpki.validator3.storage.lmdb;

import net.ripe.rpki.validator3.storage.Coder;
import net.ripe.rpki.validator3.storage.Key;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.DbiFlags.MDB_DUPFIXED;
import static org.lmdbjava.DbiFlags.MDB_DUPSORT;

/**
 * Special optimised case when primary keys are of the same size: i.e. Longs or SHA256.
 */
public class SameSizeKeyIxMap<T extends Serializable> extends IxMap<T> {

    private final int keySizeInBytes;

    public SameSizeKeyIxMap(int keySizeInBytes,
                            Env<ByteBuffer> env,
                            String name,
                            Coder<T> coder,
                            Map<String, Function<T, Set<Key>>> indexFunctions) {
        super(env, name, coder, indexFunctions);
        this.keySizeInBytes = keySizeInBytes;
    }

    @Override
    protected DbiFlags[] getIndexDbiFlags() {
        return new DbiFlags[] { MDB_CREATE, MDB_DUPSORT, MDB_DUPFIXED };
    }

    @Override
    protected void verifyKey(Key k) {
        super.verifyKey(k);
        if (k.size() != keySizeInBytes) {
            throw new IllegalArgumentException("Key size has to be " + keySizeInBytes + " but was " + k.size());
        }
    }
}
