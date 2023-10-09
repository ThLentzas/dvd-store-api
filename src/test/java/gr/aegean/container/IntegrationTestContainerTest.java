package gr.aegean.container;

import org.junit.jupiter.api.Test;

import gr.aegean.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;


class IntegrationTestContainerTest extends AbstractIntegrationTest {

    @Test
    void connectionEstablished() {
        assertThat(postgreSQLContainer.isRunning()).isTrue();
        assertThat(postgreSQLContainer.isCreated()).isTrue();
        assertThat(redisContainer.isRunning()).isTrue();
        assertThat(redisContainer.isCreated()).isTrue();
    }
}
