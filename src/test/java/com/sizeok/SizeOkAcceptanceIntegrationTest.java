package com.sizeok;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SizeOkAcceptanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullEndToEndAcceptanceCriteriaSuite() throws Exception {
        // 1. Register User A & verify default profile creation
        MvcResult regAResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "arjun@sizeok.in",
                                  "password": "Password123!",
                                  "name": "Arjun",
                                  "preferredUnit": "CM",
                                  "region": "IN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.defaultProfileId").isNotEmpty())
                .andReturn();

        JsonNode userA = objectMapper.readTree(regAResult.getResponse().getContentAsString());
        String tokenA = userA.get("accessToken").asText();
        String profileA = userA.get("defaultProfileId").asText();

        // 2. Scenario 1: User enters measurements -> Correctly persisted, normalized to CM, and versioned (v1)
        MvcResult ms1Result = mockMvc.perform(post("/api/v1/profiles/" + profileA + "/measurement-sets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "unit": "CM",
                                  "source": "MEASURED",
                                  "measurements": [
                                    { "dimension": "CHEST", "value": 100 },
                                    { "dimension": "WAIST", "value": 85 },
                                    { "dimension": "HIP", "value": 99 },
                                    { "dimension": "SHOULDER", "value": 45 }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.measurementsCm.CHEST").value(100.0))
                .andReturn();

        String ms1Id = objectMapper.readTree(ms1Result.getResponse().getContentAsString()).get("measurementSetId").asText();

        // 3. Scenario 2: User changes chest measurement -> New immutable version (v2) created, v1 SUPERSEDED
        mockMvc.perform(patch("/api/v1/measurement-sets/" + ms1Id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "unit": "CM",
                                  "source": "MEASURED",
                                  "measurements": [
                                    { "dimension": "CHEST", "value": 103 },
                                    { "dimension": "SHOULDER", "value": 46.5 }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.measurementsCm.CHEST").value(103.0))
                .andExpect(jsonPath("$.measurementsCm.WAIST").value(85.0));

        // Verify v1 is still intact and marked SUPERSEDED
        mockMvc.perform(get("/api/v1/measurement-sets/" + ms1Id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.status").value("SUPERSEDED"))
                .andExpect(jsonPath("$.measurementsCm.CHEST").value(100.0));

        // 4. Scenario 3 & 4: User enters reference size M (Zara Men's T-Shirts) + conflicting XL (Nike Men's T-Shirts)
        mockMvc.perform(post("/api/v1/profiles/" + profileA + "/reference-sizes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "brandId": "brand_zara",
                                  "categoryId": "mens_tshirts",
                                  "sizeLabel": "M",
                                  "region": "EU",
                                  "fitPreference": "REGULAR"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inferredRanges.CHEST.minCm").value(96.0))
                .andExpect(jsonPath("$.inferredRanges.CHEST.maxCm").value(101.0))
                .andExpect(jsonPath("$.conflictDetected").value(false));

        MvcResult conflictRef = mockMvc.perform(post("/api/v1/profiles/" + profileA + "/reference-sizes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "brandId": "brand_nike",
                                  "categoryId": "mens_tshirts",
                                  "sizeLabel": "XL",
                                  "region": "IN",
                                  "fitPreference": "REGULAR"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conflictDetected").value(true))
                .andExpect(jsonPath("$.conflictWarnings").isNotEmpty())
                .andReturn();

        String conflictRefId = objectMapper.readTree(conflictRef.getResponse().getContentAsString()).get("id").asText();
        // Remove the conflicting reference size so subsequent recommendation has clean evidence
        mockMvc.perform(delete("/api/v1/reference-sizes/" + conflictRefId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        // 5. Scenario 9: User requests recommendation for Zara T-shirt (BODY chart, chest=103, shoulder=46.5 -> L)
        MvcResult rec1Result = mockMvc.perform(post("/api/v1/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileId": "%s",
                                  "productId": "prod_5001",
                                  "fitPreference": "REGULAR"
                                }
                                """.formatted(profileA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendedSize").value("L"))
                .andExpect(jsonPath("$.confidence").value("HIGH"))
                .andExpect(jsonPath("$.chartVersion").value(1))
                .andExpect(jsonPath("$.explanation").isNotEmpty())
                .andReturn();

        String rec1Id = objectMapper.readTree(rec1Result.getResponse().getContentAsString()).get("recommendationId").asText();

        // 6. Scenario 8: Chart is garment-based (H&M Oxford Shirt prod_5002) -> Garment ease considered
        mockMvc.perform(post("/api/v1/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileId": "%s",
                                  "productId": "prod_5002",
                                  "fitPreference": "REGULAR"
                                }
                                """.formatted(profileA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendedSize").value("L"))
                .andExpect(jsonPath("$.explanation").isArray());

        // 7. Scenario 11: User submits fit feedback -> Linked to recommendation without mutating body measurements
        mockMvc.perform(post("/api/v1/recommendations/" + rec1Id + "/feedback")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "purchased": true,
                                  "actualSize": "L",
                                  "fitResult": "TOO_TIGHT",
                                  "problemDimensions": ["CHEST"],
                                  "userComment": "Slightly snug around chest"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recommendationId").value(rec1Id))
                .andExpect(jsonPath("$.profileMeasurementVersion").value(2))
                .andExpect(jsonPath("$.chartVersion").value(1));
    }
}

