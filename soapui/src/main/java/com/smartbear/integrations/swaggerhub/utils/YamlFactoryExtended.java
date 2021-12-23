package com.smartbear.swagger.utils;

import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.IOException;
import java.io.Writer;

public class YamlFactoryExtended extends YAMLFactory {
    @Override
    protected YAMLGenerator _createGenerator(Writer out, IOContext ctxt) throws IOException {
        int feats = this._yamlGeneratorFeatures;
        return new OpenApiYamlGenerator(ctxt, this._generatorFeatures, feats, this._objectCodec, out, this._version);
    }
}