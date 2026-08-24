package org.fjnu305.acm01.security;

import org.fjnu305.acm01.module.contest.controller.ContestController;
import org.fjnu305.acm01.module.contest.query.service.ContestQueryService;
import org.fjnu305.acm01.module.solution.controller.SolutionController;
import org.fjnu305.acm01.module.solution.service.SolutionService;
import org.fjnu305.acm01.module.user.config.UploadProperties;
import org.fjnu305.acm01.support.AbstractWebMvcTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Test helper. */
@WebMvcTest(controllers = {ContestController.class, SolutionController.class})
@EnableConfigurationProperties(UploadProperties.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PublicApiSecurityTest extends AbstractWebMvcTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContestQueryService contestQueryService;

    @MockitoBean
    private SolutionService solutionService;

    @Test
    @WithAnonymousUser
    void listContests_anonymous_ok() throws Exception {
        mockMvc.perform(get("/api/contests"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void listSolutions_anonymous_ok() throws Exception {
        mockMvc.perform(get("/api/solutions"))
                .andExpect(status().isOk());
    }
}
