package com.smartbear.ready.recipe;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class RecipeJsonSchemaValidatorTest {

    private final File file;

    @Parameters(name = "{1}")
    public static Collection<Object[]> data() throws IOException, ProcessingException {
        File recipeDir = getTestRecipesDirectory();
        File[] listFiles = recipeDir.listFiles();
        assertThat("Recipe directory not found", listFiles, is(notNullValue()));
        List<Object[]> recipeFiles = new ArrayList<>();
        for (File file : listFiles) {
            recipeFiles.add(new Object[]{file, file.getName()});
        }
        return recipeFiles;
    }

    public RecipeJsonSchemaValidatorTest(File file, String name) {
        this.file = file;
    }

    @Test
    public void validateSchema() throws Exception {
        JsonSchema schema = getTestRecipeSchema();
        String recipe = IOUtils.toString(new FileInputStream(file));
        JsonNode data = JsonLoader.fromString(recipe);
        ProcessingReport report = schema.validate(data);
        if (report != null) {
            assertThat(StringUtils.join(report, "\n"), report.isSuccess(), is(true));
        }
    }

    private static File getTestRecipesDirectory() {
        String filePath = RecipeJsonSchemaValidatorTest.class.getResource("/test-recipes").getFile();
        return new File(filePath);
    }

    private JsonSchema getTestRecipeSchema() throws IOException, ProcessingException {
        String jsonSchema = loadJsonSchema();
        JsonNode schemaNode = JsonLoader.fromString(jsonSchema);
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        return factory.getJsonSchema(schemaNode);
    }

    private String loadJsonSchema() throws IOException {
        InputStream recipeAsStream = RecipeJsonSchemaValidatorTest.class.getResourceAsStream("/test-recipe-schema.json");
        return IOUtils.toString(recipeAsStream);
    }
}
