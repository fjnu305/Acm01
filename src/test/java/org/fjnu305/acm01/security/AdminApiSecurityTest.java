package org.fjnu305.acm01.security;

import org.fjnu305.acm01.module.contest.controller.ContestAdminController;
import org.fjnu305.acm01.module.contest.controller.CrawlLogAdminController;
import org.fjnu305.acm01.module.contest.crawl.service.ContestCrawlService;
import org.fjnu305.acm01.module.contest.log.query.CrawlLogQueryService;
import org.fjnu305.acm01.module.user.controller.AdminController;
import org.fjnu305.acm01.module.user.controller.AdminUserController;
import org.fjnu305.acm01.module.user.service.AdminService;
import org.fjnu305.acm01.module.user.service.AdminUserService;
import org.fjnu305.acm01.module.user.config.UploadProperties;
import org.fjnu305.acm01.support.AbstractWebMvcTestSupport;
import org.fjnu305.acm01.support.TestMethodSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AdminController.class,
        AdminUserController.class,
        ContestAdminController.class,
        CrawlLogAdminController.class
})
@Import(TestMethodSecurityConfig.class)
@EnableConfigurationProperties(UploadProperties.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdminApiSecurityTest extends AbstractWebMvcTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private ContestCrawlService contestCrawlService;

    @MockitoBean
    private CrawlLogQueryService crawlLogQueryService;

    @Test
    @WithAnonymousUser
    void dashboard_anonymous_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void dashboard_user_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void dashboard_admin_returnsOk() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void listUsers_user_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listUsers_admin_returnsOk() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void manualCrawl_user_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/admin/contests/crawl/codeforces"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void manualCrawl_admin_returnsOk() throws Exception {
        mockMvc.perform(post("/api/admin/contests/crawl/codeforces"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void crawlLogs_user_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/crawl/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crawlLogs_admin_returnsOk() throws Exception {
        mockMvc.perform(get("/api/admin/crawl/logs"))
                .andExpect(status().isOk());
    }
}
