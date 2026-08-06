package com.ohgiraffer.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserSheetConnectionApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "MANAGER")
    void 시트_연결_확인_API_성공() throws Exception {
        String body = """
                {
                  "spreadsheetUrl": "https://docs.google.com/spreadsheets/d/1_0N5mcWykajc7bagHi4NoNVqiYwvp97di_QxiWGOtRQ/edit?gid=0#gid=0"
                }
                """;

        mockMvc.perform(post("/user/sheet/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spreadsheetTitle").value("user 테스트"))
                .andExpect(jsonPath("$.sheetNames[0]").value("시트1"))
                .andExpect(jsonPath("$.columns").isArray());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void 훈련생은_403() throws Exception {
        String body = """
                { "spreadsheetUrl": "https://docs.google.com/spreadsheets/d/abc/edit" }
                """;

        mockMvc.perform(post("/user/sheet/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }
}