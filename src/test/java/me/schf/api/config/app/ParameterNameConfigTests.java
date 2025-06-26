package me.schf.api.config.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import me.schf.api.TestConfig;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class ParameterNameConfigTests {

    @Autowired
    private ParameterNamesConfig config;

    @Test
    void testPlaceholdersResolved() {
        assertEquals("/schfme/test/database/connection/cluster-name", config.getClusterName());
        assertEquals("/schfme/test/database/connection/host", config.getHost());
        assertEquals("/schfme/test/database/name", config.getDatabaseName());
        assertEquals("/schfme/test/database/service/name", config.getServiceName());
        assertEquals("/schfme/test/database/service/password", config.getServicePassword());
    }

}
