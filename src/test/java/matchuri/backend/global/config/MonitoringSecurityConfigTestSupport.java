package matchuri.backend.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

abstract class MonitoringSecurityConfigTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment environment;

    @Test
    void applicationServerBindsToLoopbackOnly() {
        assertThat(environment.getProperty("server.address")).isEqualTo("127.0.0.1");
        assertThat(environment.getProperty("server.port", Integer.class)).isEqualTo(8080);
    }

    @Test
    void prometheusEndpointIsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jvm_")));
    }

    @Test
    void prometheusEndpointDoesNotAcceptWrites() throws Exception {
        mockMvc.perform(post("/api/v1/prometheus"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void monitoringSecurityChainDoesNotPermitChildPaths() throws Exception {
        mockMvc.perform(get("/api/v1/prometheus/extra"))
                .andExpect(status().isUnauthorized());
    }
}
