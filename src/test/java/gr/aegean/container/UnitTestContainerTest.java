package gr.aegean.container;

import org.junit.jupiter.api.Test;

import gr.aegean.AbstractUnitTest;

import static org.assertj.core.api.Assertions.assertThat;


class UnitTestContainerTest extends AbstractUnitTest {

    @Test
    void connectionEstablished() {
        assertThat(postgreSQLContainer.isRunning()).isTrue();
        assertThat(postgreSQLContainer.isCreated()).isTrue();
    }
}
