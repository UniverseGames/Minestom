package net.minestom.server.extensions;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minestom.server.utils.PlatformUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.of;

public class DependencyTest {

    @ParameterizedTest
    @MethodSource("dependencyTypesProvider")
    public void testDependencyTypes(String dependencyString, Class<?> expectedType) {
        JsonElement json = JsonParser.parseString(dependencyString);
        Dependency dependency = Dependency.fromJson(json);

        if (expectedType == null) {
            assertNull(dependency);
        } else {
            assertTrue(expectedType.isInstance(dependency));
        }
    }

    @ParameterizedTest
    @MethodSource("optionalValuesProvider")
    public void testOptionalValues(String dependencyString, boolean expectedOptional) {
        JsonElement json = JsonParser.parseString(dependencyString);
        Dependency dependency = Dependency.fromJson(json);

        assertNotNull(dependency);
        assertEquals(expectedOptional, dependency.isOptional());
    }

    @ParameterizedTest
    @ValueSource(strings = {"LuckPerms", "LuckPerms:5.0.0"})
    public void testIdParsing(String id) {
        JsonElement json = JsonParser.parseString("\"" + id + "\"");
        Dependency dependency = Dependency.fromJson(json);

        assertNotNull(dependency);
        assertEquals("LuckPerms", dependency.id());
    }

    @ParameterizedTest
    @MethodSource("platformValidationProvider")
    public void testPlatformValidation(String dependencyString, boolean expectedValid) {
        assumeTrue(PlatformUtils.OS.equals("macos"));
        assumeTrue(PlatformUtils.ARCH.equals("arm64"));

        JsonElement json = JsonParser.parseString(dependencyString);
        Dependency dependency = Dependency.fromJson(json);

        if (expectedValid) {
            assertNotNull(dependency);
        } else {
            assertNull(dependency);
        }
    }

    private static Stream<Arguments> dependencyTypesProvider() {
        return Stream.of(
                of("\"LuckPerms\"", Dependency.Extension.class),
                of("\"LuckPerms:5.0.0\"", Dependency.Extension.class),
                of("{\"id\"=\"LuckPerms\"}", Dependency.Extension.class),
                of("\"org.junit.jupiter:junit-jupiter-params:5.8.1\"", Dependency.Maven.class),
                of("\"org.lwjgl:lwjgl:3.2.2:natives-macos-arm64\"", Dependency.Maven.class)
        );
    }

    private static Stream<Arguments> optionalValuesProvider() {
        return Stream.of(
                of("\"LuckPerms\"", false),
                of("{\"id\"=\"LuckPerms\"}", false),
                of("{\"id\"=\"LuckPerms\",\"optional\":false}", false),
                of("{\"id\"=\"LuckPerms\",\"optional\":true}", true)
        );
    }

    private static Stream<Arguments> platformValidationProvider() {
        return Stream.of(
                of("""
                {
                    "id": "org.lwjgl:lwjgl:3.2.2:natives-macos-arm64",
                    "os": "macos",
                    "arch": "arm64"
                }
                """, true),
                of("""
                {
                    "id": "org.lwjgl:lwjgl:3.2.2:natives-macos-arm64",
                    "os": "macos"
                }
                """, true),
                of("""
                {
                    "id": "org.lwjgl:lwjgl:3.2.2:natives-macos-arm64",
                    "arch": "arm64"
                }
                """, true),
                of("""
                {
                    "id": "org.lwjgl:lwjgl:3.2.2:natives-macos-arm64",
                    "os": "windows",
                    "arch": "arm64"
                }
                """, false),
                of("""
                {
                    "id": "org.lwjgl:lwjgl:3.2.2:natives-macos-arm64",
                    "os": "macos",
                    "arch": "x64"
                }
                """, false),
                of("""
                {
                    "id": "org.lwjgl:lwjgl:3.2.2:natives-macos-arm64",
                    "os": "windows"
                }
                """, false),
                of("""
                {
                    "id": "org.lwjgl:lwjgl:3.2.2:natives-macos-arm64",
                    "arch": "x64"
                }
                """, false)
        );
    }
}
