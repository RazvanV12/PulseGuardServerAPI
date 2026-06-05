package com.personal.pulseguardserverapi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires a running MySQL database — start docker-compose first")
class PulseGuardServerApiApplicationTests {

    @Test
    void contextLoads() {
    }
}
