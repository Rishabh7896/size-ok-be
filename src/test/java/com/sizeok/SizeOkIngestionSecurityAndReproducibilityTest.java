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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SizeOkIngestionSecurityAndReproducibilityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void ingestionAdminSecurityAndReproducibilitySuite() throws Exception {
        MvcResult regA = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "priya@sizeok.in", "password": "Password123!", "name": "Priya" }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode userA = objectMapper.readTree(regA.getResponse().getContentAsString());
        String tokenA = userA.get("accessToken").asText();
        String profileA = userA.get("defaultProfileId").asText();

        // Save only CHEST in inches (40.5 IN = 102.87 CM), omitting SHOULDER so primary coverage is partial (Scenario 7)
        mockMvc.perform(post("/api/v1/profiles/" + profileA + "/measurement-sets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "unit": "IN",
                                  "source": "MANUAL",
                                  "measurements": [
                                    { "dimension": "CHEST", "value": 40.5 }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.measurementsCm.CHEST").value(102.87));

        // Scenario 7: Missing primary measurement (SHOULDER missing) reduces confidence from HIGH to MEDIUM
        MvcResult recPartial = mockMvc.perform(post("/api/v1/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "profileId": "%s", "productId": "prod_5001", "fitPreference": "REGULAR" }
                                """.formatted(profileA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendedSize").value("L"))
                .andExpect(jsonPath("$.confidence").value("MEDIUM"))
                .andExpect(jsonPath("$.chartVersion").value(1))
                .andReturn();
        String recId = objectMapper.readTree(recPartial.getResponse().getContentAsString()).get("recommendationId").asText();

        // Scenario 5 & 14: Supported URL ingestion + duplicate job idempotency
        MvcResult ing1 = mockMvc.perform(post("/api/v1/product-ingestions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileId": "%s",
                                  "productUrl": "https://www.zara.com/in/en/cotton-tee-p09999.html",
                                  "idempotencyKey": "idem_zara_9999"
                                }
                                """.formatted(profileA)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn();
        String ing1Id = objectMapper.readTree(ing1.getResponse().getContentAsString()).get("ingestionId").asText();

        mockMvc.perform(post("/api/v1/product-ingestions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileId": "%s",
                                  "productUrl": "https://www.zara.com/in/en/cotton-tee-p09999.html",
                                  "idempotencyKey": "idem_zara_9999"
                                }
                                """.formatted(profileA)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.ingestionId").value(ing1Id));

        // Scenario 6: Unsupported URL fallback + SSRF block
        mockMvc.perform(post("/api/v1/product-ingestions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "profileId": "%s", "productUrl": "https://unknown-store.example.org/item/1" }
                                """.formatted(profileA)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("NEEDS_USER_INPUT"))
                .andExpect(jsonPath("$.requiredAction").value("UPLOAD_SIZE_CHART"));

        mockMvc.perform(post("/api/v1/product-ingestions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "profileId": "%s", "productUrl": "http://169.254.169.254/latest/meta-data/" }
                                """.formatted(profileA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("SSRF_BLOCKED_HOST"));

        // Scenario 10, 12, 13: Admin chart update reproducibility, cross-user BOLA 403, and account deletion
        MvcResult regAdmin = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "admin2@sizeok.in", "password": "AdminPassword123!", "role": "CATALOG_ADMIN" }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String adminToken = objectMapper.readTree(regAdmin.getResponse().getContentAsString()).get("accessToken").asText();

        MvcResult regB = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "other2@sizeok.in", "password": "OtherPassword123!" }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String tokenB = objectMapper.readTree(regB.getResponse().getContentAsString()).get("accessToken").asText();

        mockMvc.perform(get("/api/v1/profiles/" + profileA)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        // Create a new brand via Admin API without app release (Use Case 9) and test v1 -> v2 chart reproducibility
        mockMvc.perform(post("/api/v1/admin/brands")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Uniqlo", "slug": "uniqlo", "country": "JP", "aliases": ["uniqlo india"] }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("brand_uniqlo"));

        mockMvc.perform(post("/api/v1/admin/size-charts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "brandId": "brand_uniqlo",
                                  "categoryId": "mens_tshirts",
                                  "region": "IN",
                                  "chartName": "Uniqlo Tops v1",
                                  "measurementBasis": "BODY",
                                  "unit": "CM",
                                  "markVerifiedAndActive": true,
                                  "sizeOptions": [
                                    { "sizeLabel": "L", "sortOrder": 1, "measurements": [ { "dimension": "CHEST", "minValue": 100, "maxValue": 106 } ] }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(1));

        MvcResult uniqloProd = mockMvc.perform(post("/api/v1/admin/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "brandId": "brand_uniqlo", "categoryId": "mens_tshirts", "retailer": "Uniqlo", "name": "Uniqlo Supima Tee", "region": "IN" }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String uniqloProdId = objectMapper.readTree(uniqloProd.getResponse().getContentAsString()).get("id").asText();

        MvcResult uniqloRec = mockMvc.perform(post("/api/v1/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "profileId": "%s", "productId": "%s", "fitPreference": "REGULAR" }
                                """.formatted(profileA, uniqloProdId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendedSize").value("L"))
                .andExpect(jsonPath("$.chartVersion").value(1))
                .andReturn();
        String uniqloRecId = objectMapper.readTree(uniqloRec.getResponse().getContentAsString()).get("recommendationId").asText();

        // Publish Uniqlo chart v2 (superseding v1)
        mockMvc.perform(post("/api/v1/admin/size-charts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "brandId": "brand_uniqlo",
                                  "categoryId": "mens_tshirts",
                                  "region": "IN",
                                  "chartName": "Uniqlo Tops v2",
                                  "measurementBasis": "BODY",
                                  "unit": "CM",
                                  "markVerifiedAndActive": true,
                                  "sizeOptions": [
                                    { "sizeLabel": "XL", "sortOrder": 1, "measurements": [ { "dimension": "CHEST", "minValue": 100, "maxValue": 106 } ] }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(2));

        // Previous recommendation uniqloRecId remains reproducible with chartVersion=1 and recommendedSize=L
        mockMvc.perform(get("/api/v1/recommendations/" + uniqloRecId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendedSize").value("L"))
                .andExpect(jsonPath("$.chartVersion").value(1));

        // Admin audit endpoint returns full snapshot
        mockMvc.perform(get("/api/v1/admin/recommendations/" + uniqloRecId + "/audit")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendation.recommendationId").value(uniqloRecId))
                .andExpect(jsonPath("$.chartSnapshot.version").value(1));

        mockMvc.perform(delete("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELETION_PENDING"));
    }
}


