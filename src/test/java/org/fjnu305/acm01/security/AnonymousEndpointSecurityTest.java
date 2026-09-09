package org.fjnu305.acm01.security;

import org.fjnu305.acm01.module.contest.controller.ContestController;
import org.fjnu305.acm01.module.contest.query.service.ContestQueryService;
import org.fjnu305.acm01.module.search.controller.SearchController;
import org.fjnu305.acm01.module.search.service.SearchQueryService;
import org.fjnu305.acm01.module.search.vo.SearchResultVO;
import org.fjnu305.acm01.module.social.controller.PostController;
import org.fjnu305.acm01.module.social.dto.CreatePostRequest;
import org.fjnu305.acm01.module.social.service.PostService;
import org.fjnu305.acm01.module.subscription.controller.SubscriptionController;
import org.fjnu305.acm01.module.subscription.dto.SubscribeRequest;
import org.fjnu305.acm01.module.subscription.service.SubscriptionService;
import org.fjnu305.acm01.module.sync.controller.OjAccountController;
import org.fjnu305.acm01.module.sync.service.OjAccountService;
import org.fjnu305.acm01.module.team.controller.TeamController;
import org.fjnu305.acm01.module.team.dto.TeamPublishRequest;
import org.fjnu305.acm01.module.team.service.TeamService;
import org.fjnu305.acm01.module.user.config.UploadProperties;
import org.fjnu305.acm01.module.user.controller.UserController;
import org.fjnu305.acm01.module.user.service.UserService;
import org.fjnu305.acm01.support.AbstractSecurityFilterTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Test helper. */
@WebMvcTest(controllers = {
        SubscriptionController.class,
        PostController.class,
        TeamController.class,
        UserController.class,
        OjAccountController.class,
        ContestController.class,
        SearchController.class
})
@EnableConfigurationProperties(UploadProperties.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnonymousEndpointSecurityTest extends AbstractSecurityFilterTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private OjAccountService ojAccountService;

    @MockitoBean
    private ContestQueryService contestQueryService;

    @MockitoBean
    private SearchQueryService searchQueryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void subscribe_anonymous_returnsUnauthorized() throws Exception {
        SubscribeRequest body = new SubscribeRequest();
        body.setContestId(1L);
        body.setRemindBeforeMinutes(List.of(60));

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void createPost_anonymous_returnsUnauthorized() throws Exception {
        CreatePostRequest body = new CreatePostRequest();
        body.setContent("hello");

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publishTeam_anonymous_returnsUnauthorized() throws Exception {
        TeamPublishRequest body = new TeamPublishRequest();
        body.setTitle("Need teammate");

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void inviteTeam_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/teams/1/invite/2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userMe_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listOjAccounts_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/oj/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listContests_anonymous_returnsOk() throws Exception {
        when(contestQueryService.listContests(any(), any(), anyInt(), anyInt()))
                .thenReturn(org.fjnu305.acm01.Common.result.PageResult.of(List.of(), 0, 1, 20));

        mockMvc.perform(get("/api/contests"))
                .andExpect(status().isOk());
    }

    @Test
    void search_anonymous_returnsOk() throws Exception {
        when(searchQueryService.search(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new SearchResultVO());

        mockMvc.perform(get("/api/search").param("q", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void teamMine_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/teams/mine"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void teamList_anonymous_returnsOk() throws Exception {
        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk());
    }

    @Test
    void listTeams_anonymous_returnsOk() throws Exception {
        when(teamService.list(any(), any(), anyInt(), anyInt()))
                .thenReturn(org.fjnu305.acm01.Common.result.PageResult.of(List.of(), 0, 1, 20));

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk());
    }
}
