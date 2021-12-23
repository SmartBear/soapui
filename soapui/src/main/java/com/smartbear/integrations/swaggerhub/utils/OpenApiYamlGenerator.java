package com.smartbear.integrations.swaggerhub.utils;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.io.Writer;

public class OpenApiYamlGenerator extends YAMLGenerator {

    public OpenApiYamlGenerator(IOContext ctxt, int jsonFeatures, int yamlFeatures, ObjectCodec codec, Writer out, DumperOptions.Version version) throws IOException {
        super(ctxt, jsonFeatures, yamlFeatures, codec, out, version);
    }

    public void writeResponseCode(String value) throws IOException {
        if (this._writeContext.writeFieldName(value) == 4) {
            this._reportError("Can not write a field name, expecting a value");
        }
        _writeScalar(value, "string", DumperOptions.ScalarStyle.DOUBLE_QUOTED);
    }
}