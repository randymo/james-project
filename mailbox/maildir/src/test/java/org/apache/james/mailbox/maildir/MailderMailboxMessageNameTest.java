/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.mailbox.maildir;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Flags;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class MailderMailboxMessageNameTest {

    private String validName;
    private Parts parts;

    public MailderMailboxMessageNameTest(Parts parts) {
        this.validName = parts.fullName;
        this.parts = parts;
    }

    @Parameterized.Parameters
    public static List<Object[]> testData() {
        List<Object[]> args = new ArrayList<>();
        // no size, two flags
        Parts parts = Parts.fullName("1328026049.19146_0.km1111:2,RS").timeSeconds(1328026049)
                .baseName("1328026049.19146_0.km1111").flagAnswered().flagSeen();
        args.add(toObjectArray(parts));

        // size and flag
        parts = Parts.fullName("1328613172.M569643P1862V0000000000000902I00EE42CE_0.km1111,S=13103:2,S")
                .timeSeconds(1328613172).size(13103L)
                .baseName("1328613172.M569643P1862V0000000000000902I00EE42CE_0.km1111").flagSeen();
        args.add(toObjectArray(parts));

        // size, no flags
        parts = Parts.fullName("1340124194.M723289P3184V0000000000000902I006780E9_6.km1111,S=1344:2,")
                .baseName("1340124194.M723289P3184V0000000000000902I006780E9_6.km1111").timeSeconds(1340124194)
                .size(1344L);
        args.add(toObjectArray(parts));

        // three flags, no size
        parts = Parts.fullName("1106685752.12132_0.km1111:2,FRS").baseName("1106685752.12132_0.km1111")
                .timeSeconds(1106685752).flagFlagged().flagAnswered().flagSeen();
        args.add(toObjectArray(parts));

        // with dovecot attributes
        parts = Parts.fullName("1035478339.27041_118.foo.org,S=1000,W=1030:2,S")
                .baseName("1035478339.27041_118.foo.org").timeSeconds(1035478339).size(1000L).flagSeen();
        args.add(toObjectArray(parts));

        parts = parts.copy();
        parts.fullName = "1035478339.27041_118.foo.org,W=1030,S=1000:2,S";
        args.add(toObjectArray(parts));

        // new mail, no info part at all. found in courier maildirs
        parts = Parts.fullName("1355543030.15049_0.foo.org").baseName("1355543030.15049_0.foo.org")
                .timeSeconds(1355543030).noFlags();
        args.add(toObjectArray(parts));

        // new mail, generated by james
        parts = Parts.fullName("1356001301.e563087e30181513.foohost,S=629:2,")
                .baseName("1356001301.e563087e30181513.foohost").timeSeconds(1356001301).size(629L);
        args.add(toObjectArray(parts));

        parts = Parts.fullName("1355675588.5c7e107958851103.foohost,S=654:2,S").timeSeconds(1355675588)
                .baseName("1355675588.5c7e107958851103.foohost").size(654L).flagSeen();
        args.add(toObjectArray(parts));

        parts = Parts.fullName("1355675651.f3dd564265174501.foohost,S=661:2,")
                .baseName("1355675651.f3dd564265174501.foohost").timeSeconds(1355675651).size(661L);
        args.add(toObjectArray(parts));

        return args;
    }
    private static Object[] toObjectArray(Parts validName) {
        return new Object[] { validName };
    }

    @Test
    public void testParsing() throws Exception {
        try {
            parseValidName(validName, parts);
        } catch (Throwable e) {
            throw new Exception("Valid name '" + validName + "' failed.", e);
        }
    }

    private void parseValidName(String name, Parts parts) throws Exception {
        MaildirMessageName mn = new MaildirMessageName(null, name);
        mn.setMessageNameStrictParse(false);
        if (parts.time == null) {
            assertThat(mn.getInternalDate()).describedAs("date").isNull();
        } else {
            assertThat(mn.getInternalDate()).describedAs("date").hasTime(parts.time);
        }
        assertThat(mn.getFullName()).describedAs("fullName").isEqualTo(parts.fullName);
        assertThat(mn.getFlags()).describedAs("flags").isEqualTo(parts.flags);
        assertThat(mn.getSize()).describedAs("size").isEqualTo(parts.size);
        assertThat(mn.getBaseName()).describedAs("baseName").isEqualTo(parts.baseName);
    }

    static class Parts {
        public Long time;
        public String fullName;
        public String baseName;
        public Long size;
        public Flags flags = new Flags();

        private Parts(String fullName) {
            this.fullName = fullName;
        }

        public static Parts fullName(String fullName) {
            return new Parts(fullName);
        }

        public Parts noFlags() {
            this.flags = null;
            return this;
        }

        public Parts flagSeen() {
            this.flags.add(Flags.Flag.SEEN);
            return this;
        }

        public Parts flagRecent() {
            this.flags.add(Flags.Flag.RECENT);
            return this;
        }

        public Parts flagAnswered() {
            this.flags.add(Flags.Flag.ANSWERED);
            return this;
        }

        public Parts flagFlagged() {
            this.flags.add(Flags.Flag.FLAGGED);
            return this;
        }

        public Parts flagDeleted() {
            this.flags.add(Flags.Flag.DELETED);
            return this;
        }

        public Parts flagDraft() {
            this.flags.add(Flags.Flag.DRAFT);
            return this;
        }

        public Parts baseName(String baseName) {
            this.baseName = baseName;
            return this;
        }

        public Parts size(Long size) {
            this.size = size;
            return this;
        }

        public Parts timeSeconds(Long time) {
            if (time != null) {
                this.time = time * 1000;
            } else {
                this.time = null;
            }
            return this;
        }

        public Parts timeMillis(Long time) {
            this.time = time;
            return this;
        }

        public Parts timeSeconds(Integer time) {
            return timeSeconds(time != null ? time.longValue() : null);
        }

        public Parts copy() {
            Parts p = Parts.fullName(fullName).baseName(baseName).size(size).timeMillis(time);
            p.flags = (Flags) this.flags.clone();
            return p;
        }
    }

}
