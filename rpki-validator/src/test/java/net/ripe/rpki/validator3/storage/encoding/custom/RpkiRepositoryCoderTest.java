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
package net.ripe.rpki.validator3.storage.encoding.custom;

import net.ripe.rpki.validator3.api.util.Dates;
import net.ripe.rpki.validator3.storage.data.Key;
import net.ripe.rpki.validator3.storage.data.Ref;
import net.ripe.rpki.validator3.storage.data.RpkiRepository;
import net.ripe.rpki.validator3.storage.data.TrustAnchor;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class RpkiRepositoryCoderTest {

    @Test
    public void testSaveRead() {
        Ref<TrustAnchor> trustAnchorRef = Ref.unsafe("bla", Key.of(123L));
        RpkiRepository rpkiRepository = new RpkiRepository(trustAnchorRef, "some-uri", RpkiRepository.Type.RRDP);
        Ref<RpkiRepository> parentRepoRef = Ref.unsafe("foo", Key.of(987654321L));
        rpkiRepository.setParentRepository(parentRepoRef);
        rpkiRepository.setLastDownloadedAt(Dates.nowTruncatedMillis());
        rpkiRepository.setRrdpSerial(new BigInteger("2133553334897396402696204629648763485348763845"));
        rpkiRepository.setRrdpSessionId("sfjbkskbsfkbjsfkjbs");

        RpkiRepositoryCoder coder = new RpkiRepositoryCoder();
        RpkiRepository rpkiRepository1 = coder.fromBytes(coder.toBytes(rpkiRepository));

        assertEquals(rpkiRepository, rpkiRepository1);
    }
}