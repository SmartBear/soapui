package com.smartbear.integrations.swagger.utils;

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
import io.swagger.models.ArrayModel;
import io.swagger.models.ComposedModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Xml;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BaseIntegerProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.EmailProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.properties.UUIDProperty;
import io.swagger.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//This class now never used, but do not delete it. It should be kept for safety purposes if some backward compatibility issue will be raised.
//Now instead of this class the original ExampleBuilder class from swagger-inflector is used.
public class SwaggerExampleBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerExampleBuilder.class);

    public static final String SAMPLE_EMAIL_PROPERTY_VALUE = "apiteam@swagger.io";
    public static final String SAMPLE_UUID_PROPERTY_VALUE = "3fa85f64-5717-4562-b3fc-2c963f66afa6";
    public static final String SAMPLE_STRING_PROPERTY_VALUE = "string";
    public static final int SAMPLE_INT_PROPERTY_VALUE = 0;
    public static final int SAMPLE_LONG_PROPERTY_VALUE = 0;
    public static final int SAMPLE_BASE_INTEGER_PROPERTY_VALUE = 0;
    public static final float SAMPLE_FLOAT_PROPERTY_VALUE = 1.1f;
    public static final double SAMPLE_DOUBLE_PROPERTY_VALUE = 1.1f;
    public static final boolean SAMPLE_BOOLEAN_PROPERTY_VALUE = true;
    public static final String SAMPLE_DATE_PROPERTY_VALUE = "2015-07-20";
    public static final String SAMPLE_DATETIME_PROPERTY_VALUE = "2015-07-20T15:49:04-07:00";
    public static final double SAMPLE_DECIMAL_PROPERTY_VALUE = 1.5;

    public static Example fromProperty(Property property, Map<String, Model> definitions) {
        return fromProperty(property, definitions, new HashMap<>());
    }

    public static Example fromProperty(Property property, Map<String, Model> definitions, Map<String, Example> processedModels) {
        if (property == null) {
            return null;
        }

        String name = null;
        String namespace = null;
        String prefix = null;
        Boolean attribute = false;
        Boolean wrapped = false;

        if (property.getXml() != null) {
            Xml xml = property.getXml();
            name = xml.getName();
            namespace = xml.getNamespace();
            prefix = xml.getPrefix();
            attribute = xml.getAttribute();
            wrapped = xml.getWrapped() != null ? xml.getWrapped() : false;
        }

        Example output = null;

        Object example = property.getExample();
        if (property instanceof RefProperty) {
            RefProperty ref = (RefProperty) property;
            if (processedModels.containsKey(ref.getSimpleRef())) {
                // return some sort of example
                return alreadyProcessedRefExample(ref.getSimpleRef(), definitions, processedModels);
            }
            processedModels.put(ref.getSimpleRef(), null);
            if (definitions != null) {
                Model model = definitions.get(ref.getSimpleRef());
                if (model != null) {
                    output = fromModel(ref.getSimpleRef(), model, definitions, processedModels);
                    processedModels.put(ref.getSimpleRef(), output);
                }
            }
        } else if (property instanceof EmailProperty) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            else {
                String defaultValue = ((EmailProperty)property).getDefault();

                if( defaultValue == null ){
                    List<String> enums = ((EmailProperty) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new StringExample( defaultValue == null ? SAMPLE_EMAIL_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof UUIDProperty) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            else {
                String defaultValue = ((UUIDProperty)property).getDefault();

                if( defaultValue == null ){
                    List<String> enums = ((UUIDProperty) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new StringExample( defaultValue == null ? SAMPLE_UUID_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof StringProperty) {
            if (example != null) {
                output = new StringExample(example.toString());
            } else {
                String defaultValue = ((StringProperty)property).getDefault();

                if( defaultValue == null ){
                    List<String> enums = ((StringProperty) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new StringExample( defaultValue == null ? SAMPLE_STRING_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof IntegerProperty) {
            if (example != null) {
                try {
                    output = new IntegerExample(Integer.parseInt(example.toString()));
                }
                catch( NumberFormatException e ){}
            }

            if( output == null )  {
                Integer defaultValue = ((IntegerProperty) property).getDefault();

                if( defaultValue == null ){
                    List<Integer> enums = ((IntegerProperty) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new IntegerExample( defaultValue == null ? SAMPLE_INT_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof LongProperty) {
            if (example != null) {
                try {
                    output = new LongExample(Long.parseLong(example.toString()));
                }
                catch( NumberFormatException e ) {}
            }

            if( output == null ) {
                Long defaultValue = ((LongProperty) property).getDefault();

                if( defaultValue == null ){
                    List<Long> enums = ((LongProperty) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new LongExample( defaultValue == null ? SAMPLE_LONG_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof BaseIntegerProperty) {
            if (example != null) {
                try {
                    output = new IntegerExample(Integer.parseInt(example.toString()));
                }
                catch( NumberFormatException e ){}
            }

            if( output == null ) {
                output = new IntegerExample(SAMPLE_BASE_INTEGER_PROPERTY_VALUE);
            }
        } else if (property instanceof FloatProperty) {
            if (example != null) {
                try {
                    output = new FloatExample(Float.parseFloat(example.toString()));
                }
                catch( NumberFormatException e ){}
            }

            if( output == null ) {
                Float defaultValue = ((FloatProperty) property).getDefault();

                if( defaultValue == null ){
                    List<Float> enums = ((FloatProperty) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new FloatExample( defaultValue == null ? SAMPLE_FLOAT_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof DoubleProperty) {
            if (example != null) {
                try {
                    output = new DoubleExample(Double.parseDouble(example.toString()));
                }
                catch( NumberFormatException e ){}
            }

            if( output == null ){
                Double defaultValue = ((DoubleProperty) property).getDefault();

                if( defaultValue == null ){
                    List<Double> enums = ((DoubleProperty) property).getEnum();
                    if( enums != null && !enums.isEmpty()) {
                        defaultValue = enums.get(0);
                    }
                }

                output = new DoubleExample( defaultValue == null ? SAMPLE_DOUBLE_PROPERTY_VALUE : defaultValue );
            }
        } else if (property instanceof BooleanProperty) {
            if (example != null) {
                output = new BooleanExample(Boolean.valueOf(example.toString()));
            }
            else {
                Boolean defaultValue = ((BooleanProperty)property).getDefault();
                output = new BooleanExample( defaultValue == null ? SAMPLE_BOOLEAN_PROPERTY_VALUE : defaultValue.booleanValue());
            }
        } else if (property instanceof DateProperty) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            else {

                List<String> enums = ((DateProperty) property).getEnum();
                if( enums != null && !enums.isEmpty()) {
                    output = new StringExample(enums.get(0));
                }
                else {
                    output = new StringExample(SAMPLE_DATE_PROPERTY_VALUE);
                }
            }
        } else if (property instanceof DateTimeProperty) {
            if (example != null) {
                output = new StringExample(example.toString());
            }
            else {
                List<String> enums = ((DateTimeProperty) property).getEnum();
                if( enums != null && !enums.isEmpty()) {
                    output = new StringExample(enums.get(0));
                }
                else {
                    output = new StringExample(SAMPLE_DATETIME_PROPERTY_VALUE);
                }
            }
        } else if (property instanceof DecimalProperty) {
            if (example != null) {
                try {
                    output = new DecimalExample(new BigDecimal(example.toString()));
                }
                catch( NumberFormatException e ){}
            }

            if( output == null ){
                output = new DecimalExample(new BigDecimal(SAMPLE_DECIMAL_PROPERTY_VALUE));
            }
        } else if (property instanceof ObjectProperty) {
            if (example != null) {
                try {
                    output = Json.mapper().readValue(example.toString(), ObjectExample.class);
                } catch (IOException e) {
                    LOGGER.error("unable to convert `" + example + "` to JsonNode");
                    output = new ObjectExample();
                }
            }
            else {
                ObjectExample outputExample = new ObjectExample();
                outputExample.setName( property.getName() );
                ObjectProperty op = (ObjectProperty) property;
                if(op.getProperties() != null) {
                    for(String propertyname : op.getProperties().keySet()) {
                        Property inner = op.getProperties().get(propertyname);
                        Example innerExample = fromProperty(inner, definitions, processedModels);
                        outputExample.put(propertyname, innerExample);
                    }
                }
                output = outputExample;
            }
        } else if (property instanceof ArrayProperty) {
            if (example != null) {
                output = new ArrayExample();
            }
            else {
                ArrayProperty ap = (ArrayProperty) property;
                Property inner = ap.getItems();
                if (inner != null) {
                    Object innerExample = fromProperty(inner, definitions, processedModels);
                    if (innerExample != null) {
                        if (innerExample instanceof Example) {
                            ArrayExample an = new ArrayExample();
                            an.add((Example) innerExample);
                            an.setName(property.getName());
                            output = an;
                        }
                    }
                }
            }
        } else if (property instanceof MapProperty) {
            MapProperty mp = (MapProperty) property;
            Property inner = mp.getAdditionalProperties();
            if (inner != null) {
                Object innerExample = fromProperty(inner, definitions, processedModels);
                if (innerExample != null) {
                    ObjectExample on = new ObjectExample();

                    if (innerExample instanceof Example) {
                        StringExample key = new StringExample("key");
                        key.setName("key");
                        Example in = (Example) innerExample;
                        on.put("key", in);
                        output = on;
                    } else {
                        ObjectExample outputMap = new ObjectExample();
                        outputMap.put("key", new ObjectExample());
                        output = outputMap;
                    }
                }
            }
        }

        // TODO: File
        if (property instanceof RefProperty && output == null) {
            if( definitions != null ) {
                RefProperty ref = (RefProperty) property;
                Model model = definitions.get(ref.getSimpleRef());
                if (model != null) {
                    if (model instanceof ModelImpl) {
                        ModelImpl i = (ModelImpl) model;
                        if (i.getXml() != null) {
                            Xml xml = i.getXml();
                            name = xml.getName();
                            attribute = xml.getAttribute();
                            namespace = xml.getNamespace();
                            prefix = xml.getPrefix();
                            wrapped = xml.getWrapped();
                        }
                    }
                    if (model.getExample() != null) {
                        try {
                            Example n = Json.mapper().readValue(model.getExample().toString(), Example.class);
                            output = n;
                        } catch (IOException e) {
                            LOGGER.error("unable to convert value", e);
                        }
                    } else {
                        ObjectExample values = new ObjectExample();

                        Map<String, Property> properties = model.getProperties();
                        if (properties != null) {
                            for (String key : properties.keySet()) {
                                Property innerProp = properties.get(key);
                                Example p = (Example) fromProperty(innerProp, definitions, processedModels);
                                if (p != null) {
                                    if (p.getName() == null) {
                                        p.setName(key);
                                    }
                                    values.put(key, p);
                                    processedModels.put(key, p);
                                }
                            }
                        }
                        output = values;
                    }
                }
                if (output != null) {
                    output.setName(ref.getSimpleRef());
                }
            }
        }
        if (output != null) {
            if (attribute != null) {
                output.setAttribute(attribute);
            }
            if (wrapped != null && wrapped) {
                if (name != null) {
                    output.setWrappedName(name);
                }
            } else if (name != null) {
                output.setName(name);
            }
            output.setNamespace(namespace);
            output.setPrefix(prefix);
            output.setWrapped(wrapped);
        }
        return output;
    }

    public static Example alreadyProcessedRefExample(String name, Map<String, Model> definitions, Map<String, Example> processedModels) {
        Model model = definitions.get(name);
        if (model == null) {
            return null;
        }
        Example output = null;

        if (model instanceof ModelImpl) {
            // look at type
            ModelImpl impl = (ModelImpl) model;
            if (impl.getType() != null) {
                if ("object".equals(impl.getType())) {
                    output = processedModels.get(name);
                    if (output == null) {
                        output = new ObjectExample();
                    }
                } else if ("string".equals(impl.getType())) {
                    output = new StringExample("");
                } else if ("integer".equals(impl.getType())) {
                    output = new IntegerExample(0);
                } else if ("long".equals(impl.getType())) {
                    output = new LongExample(0);
                } else if ("float".equals(impl.getType())) {
                    output = new FloatExample(0);
                } else if ("double".equals(impl.getType())) {
                    output = new DoubleExample(0);
                }
            }
        }

        return output;
    }

    public static Example fromModel(String name, Model model, Map<String, Model> definitions, Map<String, Example> processedModels) {
        String namespace = null;
        String prefix = null;
        Boolean attribute = false;
        Boolean wrapped = false;

        Example output = null;
        if (model.getExample() != null) {
            try {
                String str = model.getExample().toString();
                output = Json.mapper().readValue(str, ObjectExample.class);
            } catch (IOException e) {
                return null;
            }
        }
        else if(model instanceof ModelImpl) {
            ModelImpl impl = (ModelImpl) model;
            if (impl.getXml() != null) {
                Xml xml = impl.getXml();
                name = xml.getName();
                namespace = xml.getNamespace();
                prefix = xml.getPrefix();
                attribute = xml.getAttribute();
                wrapped = xml.getWrapped() != null ? xml.getWrapped() : false;
            }

            ObjectExample ex = new ObjectExample();

            if(impl.getProperties() != null) {
                for(String key : impl.getProperties().keySet()) {
                    Property property = impl.getProperties().get(key);
                    Example propExample = fromProperty(property, definitions, processedModels);
                    ex.put(key, propExample);
                }
            }
            output = ex;
        }
        else if(model instanceof ComposedModel) {
            ComposedModel cm = (ComposedModel) model;
            List<Model> models = cm.getAllOf();
            ObjectExample ex = new ObjectExample();

            List<Example> innerExamples = new ArrayList<>();
            if(models != null) {
                for (Model im : models) {
                    Example innerExample = fromModel(null, im, definitions, processedModels);
                    if(innerExample != null) {
                        innerExamples.add(innerExample);
                    }
                }
            }
            mergeTo(ex, innerExamples);
            output = ex;
        }
        else if(model instanceof ArrayModel) {
            ArrayModel am = (ArrayModel) model;
            ObjectExample ex = new ObjectExample();

            Property inner = am.getItems();
            if (inner != null) {
                Example innerExample = fromProperty(inner, definitions, processedModels);
                if (innerExample != null) {
                    ArrayExample an = new ArrayExample();
                    an.add(innerExample);
                    output = an;
                }
            }
        }
        if (output != null) {
            if (attribute != null) {
                output.setAttribute(attribute);
            }
            if (wrapped != null && wrapped) {
                if (name != null) {
                    output.setWrappedName(name);
                }
            } else if (name != null) {
                output.setName(name);
            }
            output.setNamespace(namespace);
            output.setPrefix(prefix);
            output.setWrapped(wrapped);
        }
        return output;
    }

    public static void mergeTo(ObjectExample output, List<Example> examples) {
        for(Example ex : examples) {
            if(ex instanceof ObjectExample) {
                ObjectExample objectExample = (ObjectExample) ex;
                Map<String, Example> values = objectExample.getValues();
                if( values != null ) {
                    output.putAll(values);
                }
            }
        }
    }
}