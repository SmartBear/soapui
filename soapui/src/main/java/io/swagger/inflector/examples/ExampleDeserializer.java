package io.swagger.inflector.examples;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.swagger.inflector.examples.models.ArrayExample;
import io.swagger.inflector.examples.models.BooleanExample;
import io.swagger.inflector.examples.models.DecimalExample;
import io.swagger.inflector.examples.models.DoubleExample;
import io.swagger.inflector.examples.models.Example;
import io.swagger.inflector.examples.models.FloatExample;
import io.swagger.inflector.examples.models.IntegerExample;
import io.swagger.inflector.examples.models.LongExample;
import io.swagger.inflector.examples.models.ObjectExample;
import io.swagger.inflector.examples.models.StringExample;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExampleDeserializer extends StdDeserializer<Example> {
    public ExampleDeserializer() {
        super(Example.class);
    }

    public Example deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = (JsonNode)jp.getCodec().readTree(jp);
        return this.fromNode(node);
    }

    private Example fromNode(JsonNode node) {
        if (node.isObject()) {
            ObjectExample obj = new ObjectExample();
            Iterator names = node.fieldNames();

            while(names.hasNext()) {
                String name = (String)names.next();
                JsonNode child = node.get(name);
                obj.put(name, this.fromNode(child));
            }

            return obj;
        } else if (node.isArray()) {
            ArrayExample arr = new ArrayExample();
            Iterator var9 = node.iterator();

            while(var9.hasNext()) {
                JsonNode element = (JsonNode)var9.next();
                arr.add(this.fromNode(element));
            }

            return arr;
        } else if (node.isBoolean()) {
            BooleanExample bool = new BooleanExample();
            bool.setValue(node.asBoolean());
            return bool;
        } else if (node.isDouble()) {
            DoubleExample d = new DoubleExample();
            d.setValue(node.asDouble());
            return d;
        } else if (node.isFloat()) {
            FloatExample f = new FloatExample();
            f.setValue((float)node.asDouble());
            return f;
        } else if (node.isLong()) {
            LongExample l = new LongExample();
            l.setValue(node.asLong());
            return l;
        } else if (node.isInt()) {
            IntegerExample i = new IntegerExample();
            i.setValue(node.asInt());
            return i;
        } else if (node.isBigDecimal()) {
            DecimalExample d = new DecimalExample();
            d.setValue(new BigDecimal(node.asText()));
            return d;
        } else if (node.isTextual()) {
            StringExample str = new StringExample();
            str.setValue(node.asText());
            return str;
        } else {
            return null;
        }
    }
}
