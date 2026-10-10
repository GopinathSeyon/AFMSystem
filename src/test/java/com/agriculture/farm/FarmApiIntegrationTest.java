package com.agriculture.farm;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FarmApiIntegrationTest {
    @Autowired MockMvc mvc;

    record Session(String token, String refresh, long userId, Long farmerId) {}

    // ---------- helpers ----------
    private MvcResult call(MockHttpServletRequestBuilder b, String token, String json, int expected) throws Exception {
        if (token != null) b.header("Authorization", "Bearer " + token);
        if (json != null) b.contentType(MediaType.APPLICATION_JSON).content(json);
        return mvc.perform(b).andExpect(status().is(expected)).andReturn();
    }

    private static String body(MvcResult r) throws Exception { return r.getResponse().getContentAsString(); }

    private static Session session(MvcResult r) throws Exception {
        var d = JsonPath.parse(body(r));
        Number farmerId = d.read("$.farmerId");
        return new Session(d.read("$.token"), d.read("$.refreshToken"),
                ((Number) d.read("$.userId")).longValue(), farmerId == null ? null : farmerId.longValue());
    }

    private Session register() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        String json = "{\"name\":\"Test User\",\"email\":\"" + email + "\",\"password\":\"StrongPass123\"}";
        return session(call(post("/api/auth/register"), null, json, 201));
    }

    private Session adminLogin() throws Exception {
        return session(call(post("/api/auth/login"), null,
                "{\"email\":\"admin@test.com\",\"password\":\"Admin12345\"}", 200));
    }

    private long create(String url, String token, String json) throws Exception {
        return ((Number) JsonPath.read(body(call(post(url), token, json, 201)), "$.id")).longValue();
    }

    private static BigDecimal num(MvcResult r, String path) throws Exception {
        Number n = JsonPath.read(body(r), path);
        return new BigDecimal(n.toString());
    }

    private static void assertNum(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }

    private long newFarm(Session s) throws Exception {
        return create("/api/farms", s.token(), "{\"name\":\"Green Valley\",\"totalArea\":12.5,\"farmerId\":" + s.farmerId() + "}");
    }

    // ---------- tests ----------
    @Test
    void requestWithoutTokenIs401() throws Exception {
        call(get("/api/farms"), null, null, 401);
    }

    @Test
    void registrationCreatesFarmerProfile() throws Exception {
        Session s = register();
        assertNotNull(s.farmerId());
        MvcResult me = call(get("/api/farmers/me"), s.token(), null, 200);
        assertEquals(s.farmerId().longValue(), ((Number) JsonPath.read(body(me), "$.id")).longValue());
    }

    @Test
    void duplicateRegistrationIs409() throws Exception {
        String json = "{\"name\":\"Dup\",\"email\":\"dup-" + UUID.randomUUID() + "@example.com\",\"password\":\"StrongPass123\"}";
        call(post("/api/auth/register"), null, json, 201);
        call(post("/api/auth/register"), null, json, 409);
    }

    @Test
    void farmerCannotUseAnotherFarmersData() throws Exception {
        Session a = register(), b = register();
        long farmId = newFarm(a);

        call(get("/api/farms/" + farmId), b.token(), null, 403);
        call(put("/api/farms/" + farmId), b.token(),
                "{\"name\":\"Hacked\",\"totalArea\":1,\"farmerId\":" + b.farmerId() + "}", 403);
        call(post("/api/fields"), b.token(),
                "{\"name\":\"F\",\"area\":2,\"farmId\":" + farmId + "}", 403);
        // B cannot create a farm under A's farmer profile either
        call(post("/api/farms"), b.token(),
                "{\"name\":\"X\",\"totalArea\":1,\"farmerId\":" + a.farmerId() + "}", 403);

        MvcResult list = call(get("/api/farms"), b.token(), null, 200);
        assertEquals(0, ((java.util.List<?>) JsonPath.read(body(list), "$.content")).size());
        call(get("/api/farmers"), a.token(), null, 403);     // listing all farmers is ADMIN only
    }

    @Test
    void onlyAdminCanDelete() throws Exception {
        Session a = register();
        long farmId = newFarm(a);
        call(delete("/api/farms/" + farmId), a.token(), null, 403);
        call(delete("/api/farms/" + farmId), adminLogin().token(), null, 204);
        call(get("/api/farms/" + farmId), a.token(), null, 404);
    }

    @Test
    void harvestRevenueAndProfitReport() throws Exception {
        Session a = register();
        long farmId = newFarm(a);
        long fieldId = create("/api/fields", a.token(), "{\"name\":\"North\",\"area\":5,\"farmId\":" + farmId + "}");
        long cropId = create("/api/crops", a.token(), "{\"name\":\"Wheat\",\"fieldId\":" + fieldId + "}");
        create("/api/expenses", a.token(),
                "{\"category\":\"SEED\",\"amount\":1000,\"expenseDate\":\"2026-03-01\",\"farmId\":" + farmId + "}");

        MvcResult h = call(post("/api/harvests"), a.token(),
                "{\"harvestDate\":\"2026-10-01\",\"quantity\":500,\"pricePerUnit\":22,\"unit\":\"kg\",\"cropId\":" + cropId + "}", 201);
        assertNum("11000", num(h, "$.totalRevenue"));
        assertEquals("HARVESTED", JsonPath.read(body(call(get("/api/crops/" + cropId), a.token(), null, 200)), "$.status"));

        MvcResult profit = call(get("/api/reports/profit?from=2026-01-01&to=2026-12-31"), a.token(), null, 200);
        assertNum("11000", num(profit, "$.totalRevenue"));
        assertNum("1000", num(profit, "$.totalExpense"));
        assertNum("10000", num(profit, "$.totalProfit"));

        MvcResult exp = call(get("/api/expenses/report?from=2026-01-01&to=2026-12-31&farmId=" + farmId), a.token(), null, 200);
        assertNum("1000", num(exp, "$.totalAmount"));
        call(get("/api/reports/profit?from=2026-12-31&to=2026-01-01"), a.token(), null, 400);
        call(get("/api/crops/search?name=whe"), a.token(), null, 200);
    }

    @Test
    void validationErrorsReturn400() throws Exception {
        Session a = register();
        call(post("/api/farms"), a.token(), "{\"name\":\"\",\"totalArea\":-5,\"farmerId\":" + a.farmerId() + "}", 400);
        call(post("/api/auth/register"), null, "{\"name\":\"X\",\"email\":\"bad\",\"password\":\"short\"}", 400);
    }

    @Test
    void refreshTokenRotationAndReuseDetection() throws Exception {
        Session s = register();
        MvcResult r1 = call(post("/api/auth/refresh"), null, "{\"refreshToken\":\"" + s.refresh() + "\"}", 200);
        Session s2 = session(r1);
        assertNotEquals(s.refresh(), s2.refresh());

        call(post("/api/auth/refresh"), null, "{\"refreshToken\":\"" + s.refresh() + "\"}", 401);   // reuse
        call(post("/api/auth/refresh"), null, "{\"refreshToken\":\"" + s2.refresh() + "\"}", 401);  // all revoked
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        Session s = register();
        call(post("/api/auth/logout"), null, "{\"refreshToken\":\"" + s.refresh() + "\"}", 200);
        call(post("/api/auth/refresh"), null, "{\"refreshToken\":\"" + s.refresh() + "\"}", 401);
    }

    @Test
    void adminCanPromoteAndDeactivateUsers() throws Exception {
        Session admin = adminLogin();
        Session u = register();
        call(get("/api/admin/users"), u.token(), null, 403);

        call(patch("/api/admin/users/" + u.userId() + "/role"), admin.token(), "{\"role\":\"ADMIN\"}", 200);
        call(get("/api/admin/users"), u.token(), null, 200);            // role change is immediate

        call(patch("/api/admin/users/" + u.userId() + "/active"), admin.token(), "{\"active\":false}", 200);
        call(get("/api/farmers/me"), u.token(), null, 401);              // old access token stops working
        call(post("/api/auth/refresh"), null, "{\"refreshToken\":\"" + u.refresh() + "\"}", 401);
        call(post("/api/auth/login"), null,
                "{\"email\":\"" + JsonPath.read(body(call(get("/api/admin/users/" + u.userId()), admin.token(), null, 200)), "$.email")
                        + "\",\"password\":\"StrongPass123\"}", 401);
    }

    @Test
    void adminCannotDemoteOrDeactivateSelf() throws Exception {
        Session admin = adminLogin();
        call(patch("/api/admin/users/" + admin.userId() + "/role"), admin.token(), "{\"role\":\"FARMER\"}", 400);
        call(patch("/api/admin/users/" + admin.userId() + "/active"), admin.token(), "{\"active\":false}", 400);
    }
}
