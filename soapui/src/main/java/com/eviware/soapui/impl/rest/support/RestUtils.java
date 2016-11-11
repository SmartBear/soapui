/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.impl.rest.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.types.StringList;
import org.apache.xmlbeans.XmlBoolean;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.eviware.soapui.impl.support.HttpUtils.urlEncodeWithUtf8;

public class RestUtils {

    public static enum TemplateExtractionOption {
        EXTRACT_TEMPLATE_PARAMETERS, IGNORE_TEMPLATE_PARAMETERS
    }

    public static String[] extractTemplateParams(String path) {
        if (StringUtils.isNullOrEmpty(path)) {
            return new String[0];
        }

        StringList result = new StringList();

        int ix = path.indexOf('{');
        while (ix != -1) {
            int endIx = path.indexOf('}', ix);
            if (endIx == -1) {
                break;
            }

            if (endIx > ix + 1 && (ix > 0 && path.charAt(ix - 1) != '$')) {
                result.add(path.substring(ix + 1, endIx));
            }

            ix = path.indexOf('{', ix + 1);
        }

        return result.toStringArray();

    }

    public static String extractParams(String pathOrEndpoint, RestParamsPropertyHolder params, boolean keepHost) {
        return extractParams(pathOrEndpoint, params, keepHost, TemplateExtractionOption.EXTRACT_TEMPLATE_PARAMETERS);
    }

    public static String extractParams(String pathOrEndpoint, RestParamsPropertyHolder params, boolean keepHost, TemplateExtractionOption templateExtractionOptions) {
        if (StringUtils.isNullOrEmpty(pathOrEndpoint)) {
            return "";
        }

        String path = pathOrEndpoint;
        String queryString = "";
        URL url = null;

        try {
            url = new URL(pathOrEndpoint);
            path = url.getPath();
            queryString = url.getQuery();
        } catch (MalformedURLException e) {
            int ix = path.indexOf('?');
            if (ix >= 0) {
                queryString = path.substring(ix + 1);
                path = path.substring(0, ix);
            }
        }

        String[] items = path.split("/");

        StringBuilder resultPath = new StringBuilder();

        for (String item : items) {
            try {
                if (templateExtractionOptions == TemplateExtractionOption.EXTRACT_TEMPLATE_PARAMETERS) {
                    int openCurlyIndex = item.indexOf("{");
                    int closingCurlyIndex = item.indexOf("}");
                    if (openCurlyIndex != -1 && closingCurlyIndex > openCurlyIndex) {
                        String name = item.substring(openCurlyIndex + 1, closingCurlyIndex);
                        RestParamProperty property = params.getProperty(name);
                        if (!params.hasProperty(name)) {
                            property = params.addProperty(name);
                        }

                        property.setStyle(ParameterStyle.TEMPLATE);
                        property.setValue(name);
                        property.setDefaultValue(name);
                    }
                }
                String[] matrixParams = item.split(";");
                if (matrixParams.length > 0) {
                    item = matrixParams[0];
                    for (int c = 1; c < matrixParams.length; c++) {
                        String matrixParam = matrixParams[c];

                        int ix = matrixParam.indexOf('=');
                        if (ix == -1) {
                            String name = URLDecoder.decode(matrixParam, "Utf-8");
                            if (!params.hasProperty(name)) {
                                params.addProperty(name).setStyle(ParameterStyle.MATRIX);
                            }
                        } else {

                            String name = URLDecoder.decode(matrixParam.substring(0, ix), "Utf-8");
                            RestParamProperty property = params.getProperty(name);
                            if (property == null) {
                                property = params.addProperty(name);
                            }

                            property.setStyle(ParameterStyle.MATRIX);
                            property.setValue(URLDecoder.decode(matrixParam.substring(ix + 1), "Utf-8"));
                            property.setDefaultValue(URLDecoder.decode(matrixParam.substring(ix + 1), "Utf-8"));
                        }
                    }
                }
            } catch (Exception e) {
                SoapUI.logError(e, "Couldn't parse the template/matrix parameters from URI");
            }

            if (StringUtils.hasContent(item)) {
                resultPath.append('/').append(item);
            }
        }

        if (StringUtils.hasContent(queryString)) {
            extractParamsFromQueryString(params, queryString);
        }

        if (path.endsWith("/")) {
            resultPath.append('/');
        }

        if (keepHost && url != null) {
            return Tools.getEndpointFromUrl(url) + resultPath.toString();
        }

        return resultPath.toString();
    }

    public static void extractParamsFromQueryString(RestParamsPropertyHolder params, String queryString) {
        String[] items;
        items = queryString.split("&");
        for (String item : items) {
            try {
                int ix = item.indexOf('=');
                if (ix == -1) {
                    String name = URLDecoder.decode(item, "Utf-8");

                    if (!params.hasProperty(name)) {
                        params.addProperty(name).setStyle(ParameterStyle.QUERY);
                    }
                } else {
                    String name = URLDecoder.decode(item.substring(0, ix), "Utf-8");
                    RestParamProperty property = params.getProperty(name);
                    if (property == null) {
                        property = params.addProperty(name);
                    }

                    property.setStyle(ParameterStyle.QUERY);
                    property.setValue(URLDecoder.decode(item.substring(ix + 1), "Utf-8"));
                    property.setDefaultValue(URLDecoder.decode(item.substring(ix + 1), "Utf-8"));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public static String expandPath(String path, RestParamsPropertyHolder params, RestRequestInterface request) {
        DefaultPropertyExpansionContext context = new DefaultPropertyExpansionContext(request);
        for (int c = 0; c < params.getPropertyCount(); c++) {
            RestParamProperty param = params.getPropertyAt(c);

            String value = PropertyExpander.expandProperties(context, param.getValue());
            if (!StringUtils.hasContent(value) && !param.getRequired()) {
                continue;
            }


            if (value != null && !param.isDisableUrlEncoding()) {
                try {
                    String encoding = System.getProperty("soapui.request.encoding", request.getEncoding());
                    encoding = StringUtils.hasContent(encoding) ? encoding : Charset.defaultCharset().toString();
                    value = URLEncoder.encode(value, encoding);
                } catch (UnsupportedEncodingException e1) {
                    SoapUI.logError(e1);
                    value = urlEncodeWithUtf8(value);
                }
            }

            if (param.getStyle() == ParameterStyle.TEMPLATE) {
                path = path.replaceAll("\\{" + param.getName() + "\\}", value);
            }
        }

        return path + makeSuffixParameterString(request);
    }

    /**
     * Build the parameter string to be appended to the path of an REST request
     *
     * @param request an object representing the REST request
     * @return the full parameter string, including matrix style parameters and query string.
     */
    public static String makeSuffixParameterString(RestRequestInterface request) {
        return makeMatrixParameterString(request.getParams()) + getQueryParamsString(request);
    }

    private static String makeMatrixParameterString(RestParamsPropertyHolder params) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < params.getPropertyCount(); i++) {
            RestParamProperty param = params.getPropertyAt(i);
            String value = param.getValue();
            if (param.getStyle() == ParameterStyle.MATRIX) {
                if (param.getType().equals(XmlBoolean.type.getName())) {
                    if (value.toUpperCase().equals("TRUE") || value.equals("1")) {
                        buffer.append(";").append(param.getName());
                    }
                } else {
                    buffer.append(";").append(param.getName());
                    if (StringUtils.hasContent(value)) {
                        buffer.append("=").append(value);
                    }
                }

            }
        }
        return buffer.toString();
    }


    public static String getQueryParamsString(RestRequestInterface request) {
        if (isRequestWithoutQueryString(request)) {
            return "";
        }
        RestParamsPropertyHolder params = request.getParams();
        StringBuilder query = new StringBuilder();

        for (int c = 0; c < params.getPropertyCount(); c++) {
            RestParamProperty param = params.getPropertyAt(c);
            String value = param.getValue();
            List<String> valueParts = splitMultipleParameters(value, request.getMultiValueDelimiter());

            if (param.getStyle() != ParameterStyle.QUERY || (valueParts.isEmpty() && !param.getRequired())) {
                continue;
            }

            for (String valuePart : valueParts) {
                if (query.length() > 0) {
                    query.append('&');
                }
                query.append(param.getName()).append('=');
                if (StringUtils.hasContent(valuePart)) {
                    query.append(valuePart);
                }
            }
        }

        return (query.length() > 0 ? "?" : "") + query.toString();
    }

    private static boolean isRequestWithoutQueryString(RestRequestInterface request) {
        return request.isPostQueryString() || "multipart/form-data".equals(request.getMediaType())
                || "multipart/mixed".equals(request.getMediaType());
    }


    public static List<String> splitMultipleParameters(String paramStr, String delimiter) {
        StringList result = new StringList();

        if (StringUtils.hasContent(paramStr)) {
            if (!StringUtils.hasContent(delimiter)) {
                result.add(paramStr);
            } else {
                result.addAll(paramStr.split(delimiter));
            }
        }

        return result;

    }

    /**
     * specificaly used for adding empty parameters also in the list when
     * "send empty parameters" are checked in HTTP TestRequest Properties
     *
     * @param paramStr
     * @param delimiter
     * @return
     */
    public static List<String> splitMultipleParametersEmptyIncluded(String paramStr, String delimiter) {
        StringList result = new StringList();

        if (!StringUtils.hasContent(delimiter)) {
            result.add(paramStr);
        } else {
            result.addAll(paramStr.split(delimiter));
        }

        return result;
    }

    public static List<RestResource> extractAncestorsParentFirst(RestResource childResource) {
        final List<RestResource> resources = new ArrayList<RestResource>();
        for (RestResource r = childResource; r != null; r = r.getParentResource()) {
            resources.add(r);
        }
        Collections.reverse(resources);
        return resources;
    }

    public static String getExpandedPath(String path, RestParamsPropertyHolder params, ModelItem context) {
        String expandedPath = path;
        expandedPath = PropertyExpander.expandProperties(context, expandedPath);
        for (String pathParam : RestUtils.extractTemplateParams(expandedPath)) {
            String pathParamValue = params.getPropertyValue(pathParam);
            pathParamValue = PropertyExpander.expandProperties(context, pathParamValue);
            expandedPath = expandedPath.replaceAll("\\{" + pathParam + "\\}", pathParamValue == null ? "" : pathParamValue);
        }

        return expandedPath;
    }
}
