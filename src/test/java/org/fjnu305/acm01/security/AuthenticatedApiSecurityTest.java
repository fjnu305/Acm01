package org.fjnu305.acm01.security;

import org.fjnu305.acm01.module.social.controller.PostController;
import org.fjnu305.acm01.module.social.service.PostService;
import org.fjnu305.acm01.module.subscription.controller.SubscriptionController;
import org.fjnu305.acm01.module.subscription.dto.SubscribeRequest;
import org.fjnu305.acm01.module.subscription.service.SubscriptionService;
import org.fjnu305.acm01.module.user.config.UploadProperties;
import org.fjnu305.acm01.support.AbstractWebMvcTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Test helper. */
@WebMvcTest(controllers = {SubscriptionController.class, PostController.class})
@EnableConfigurationProperties(UploadProperties.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthenticatedApiSecurityTest extends AbstractWebMvcTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @MockitoBean
    private PostService postService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(username = "user_a", roles = "USER")
    void subscribe_authenticated_ok() throws Exception {
        SubscribeRequest body = new SubscribeRequest();
        body.setContestId(1L);
        body.setRemindBeforeMinutes(List.of(60));

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user_a", roles = "USER")
    void hotFeed_authenticated_ok() throws Exception {
        mockMvc.perform(get("/api/posts/feed/hot"))
                .andExpect(status().isOk());
    }
}
