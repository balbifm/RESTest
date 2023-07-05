package es.us.isa.restest.mutation.rules;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static es.us.isa.restest.util.SchemaManager.resolveSchema;

public abstract class SingleRule {

    protected final Random random = new SecureRandom();

    protected SingleRule() {
    }

    public void apply(Schema<?> schema, boolean internalNode, OpenAPI spec) {
        if ("array".equals(schema.getType())) {
            apply(((ArraySchema) schema).getItems(), internalNode, spec);
        } else if (schema.getProperties() != null) {
            List<Schema> objectNodes = getAllObjectNodes(schema, internalNode, spec);

            if (!objectNodes.isEmpty()) {
                Schema s = objectNodes.get(random.nextInt(objectNodes.size()));

                applyNodeFuzzingRule(s, spec);
            }
        }
    }

    private List<Schema> getAllObjectNodes(Schema<?> schema, boolean internalNode, OpenAPI spec) {
        List<Schema> objectNodes = new ArrayList<>();

//        objectNodes.add(schema);

        if (schema.getProperties() != null) {
            schema.getProperties().forEach((key, value) -> {
                if ("array".equals(value.getType())) {
                    apply(((ArraySchema) value).getItems(), internalNode, spec);
                } else if ("object".equals(value.getType())) {
                    objectNodes.addAll(getAllObjectNodes(value, internalNode, spec));
                } else if (!internalNode) {
                    objectNodes.add(value);
                }
            });
        }

        return objectNodes;
    }

    protected abstract void applyNodeFuzzingRule(Schema<?> schema, OpenAPI spec);
}
