package es.us.isa.restest.mutation;

import static es.us.isa.restest.util.SchemaManager.generateFullyResolvedSchema;

import java.security.SecureRandom;
import java.util.Random;

import es.us.isa.restest.mutation.pipelines.DropSelectTypePipeline;
import es.us.isa.restest.mutation.rules.DuplicateRule;
import es.us.isa.restest.mutation.rules.FormatRule;
import es.us.isa.restest.mutation.rules.RequiredRule;
import es.us.isa.restest.mutation.rules.TypeRule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

public class SchemaMutation {

    private final Random random = new SecureRandom();

    private Schema schema;
    private OpenAPI spec;

    public SchemaMutation(Schema schema, OpenAPI spec) {
        this.schema = schema;
        this.spec = spec;
    }

    public Schema mutate(MutationPipeline mutation) {
        mutation = mutation == null ? MutationPipeline.values()[random.nextInt(MutationPipeline.values().length)] : mutation;

        Schema mutatedSchema = generateFullyResolvedSchema(schema, spec);
        switch (mutation) {
            case DUPLICATE:
                DuplicateRule.getInstance().apply(mutatedSchema, true, spec);
                break;
            case DROP_SELECT_TYPE:
                DropSelectTypePipeline.getInstance().apply(mutatedSchema, spec);
                break;
            case REMOVE_REQUIRED_FROM_BODY:
                RequiredRule.getInstance().apply(mutatedSchema, spec);
                break;
            case CHANGE_BODY_PROPERTY_TYPE:
                TypeRule.getInstance().apply(mutatedSchema, false, spec);
                break;
            case CHANGE_BODY_PROPERTY_FORMAT:
                FormatRule.getInstance().apply(mutatedSchema, false, spec);
                break;
            default:
        }

        return mutatedSchema;
    }

    public enum MutationPipeline {
        DROP_SELECT_TYPE, DUPLICATE, REMOVE_REQUIRED_FROM_BODY, CHANGE_BODY_PROPERTY_TYPE, CHANGE_BODY_PROPERTY_FORMAT
    }
}


