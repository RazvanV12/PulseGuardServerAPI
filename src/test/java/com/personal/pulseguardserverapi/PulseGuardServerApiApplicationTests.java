package com.personal.pulseguardserverapi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// This test starts the full application context including JPA, which requires
// a running MySQL database. Start docker-compose first, then run this test.
// Run:  docker-compose up -d
//       mvn test
@SpringBootTest
@Disabled("Requires a running MySQL database — start docker-compose first")
class PulseGuardServerApiApplicationTests {

    @Test
    void contextLoads() {
    }
}
