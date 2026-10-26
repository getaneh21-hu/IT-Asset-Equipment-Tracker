
package com.It_Asset.it_asset_equipment_tracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5433/it_asset_tracker",
        "spring.datasource.username=asset_user",
        "spring.datasource.password=${DB_PASSWORD:}",
        "app.jwt.secret=${JWT_SECRET:}"
})
class ItAssetEquipmentTrackerApplicationTests {

    @Test
    void contextLoads() {
    }
}
