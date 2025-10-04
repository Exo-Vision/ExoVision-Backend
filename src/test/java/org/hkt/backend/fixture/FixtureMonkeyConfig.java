package org.hkt.backend.fixture;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;

/**
 * Fixture Monkey configuration for test data generation
 */
public class FixtureMonkeyConfig {

    private static final FixtureMonkey INSTANCE = FixtureMonkey.builder()
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .plugin(new JakartaValidationPlugin())
            .defaultNotNull(true)
            .build();

    public static FixtureMonkey getInstance() {
        return INSTANCE;
    }
}
