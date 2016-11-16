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

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.support.definition.support.InvalidDefinitionException;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.impl.wsdl.support.UrlSchemaLoader;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.tools.PropertyExpansionRemover;
import net.java.dev.wadl.x2009.x02.ApplicationDocument;
import net.java.dev.wadl.x2009.x02.ApplicationDocument.Application;
import net.java.dev.wadl.x2009.x02.DocDocument.Doc;
import net.java.dev.wadl.x2009.x02.MethodDocument.Method;
import net.java.dev.wadl.x2009.x02.ParamDocument.Param;
import net.java.dev.wadl.x2009.x02.ParamStyle;
import net.java.dev.wadl.x2009.x02.RepresentationDocument;
import net.java.dev.wadl.x2009.x02.RepresentationDocument.Representation;
import net.java.dev.wadl.x2009.x02.ResourceDocument.Resource;
import net.java.dev.wadl.x2009.x02.ResourceTypeDocument;
import net.java.dev.wadl.x2009.x02.ResourcesDocument.Resources;
import net.java.dev.wadl.x2009.x02.ResponseDocument.Response;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WadlImporter {
    private RestService service;
    private Application application;
    private List<Resources> resourcesList;
    private Map<String, ApplicationDocument> refCache = new HashMap<String, ApplicationDocument>();
    private boolean isWADL11 = true;

    public WadlImporter(RestService service) {
        this.service = service;
    }

    public void initFromWadl(String wadlUrl) {
        try {
            // XmlObject xmlObject = XmlObject.Factory.parse( new URL( wadlUrl ) );
            XmlObject xmlObject = XmlUtils.createXmlObject(new URL(wadlUrl));

            String content = xmlObject.xmlText();
            Element element = ((Document) xmlObject.getDomNode()).getDocumentElement();

            // try to allow older namespaces
            if (element.getLocalName().equals("application")
                    && element.getNamespaceURI().startsWith("http://research.sun.com/wadl")) {
                isWADL11 = false;
                content = content.replaceAll("\"" + element.getNamespaceURI() + "\"", "\"" + Constants.WADL11_NS + "\"");
            } else if (!element.getLocalName().equals("application")
                    || !element.getNamespaceURI().equals(Constants.WADL11_NS)) {
                throw new Exception("Document is not a WADL application with " + Constants.WADL11_NS + " namespace");
            }
            content = PropertyExpansionRemover.removeExpansions(content);
            ApplicationDocument applicationDocument = ApplicationDocument.Factory.parse(content);
            application = applicationDocument.getApplication();

            resourcesList = application.getResourcesList();

            service.setName(getFirstTitle(application.getDocList(), service.getName()));

            String base = resourcesList.size() == 1 ? resourcesList.get(0).getBase() : "";

            try {
                URL baseUrl = new URL(base);
                service.setBasePath(baseUrl.getPath());

                service.addEndpoint(Tools.getEndpointFromUrl(baseUrl));
            } catch (Exception e) {
                service.setBasePath(base);
            }

            service.setWadlUrl(wadlUrl);
            service.getConfig().setWadlVersion(isWADL11 ? Constants.WADL11_NS : Constants.WADL10_NS);

            for (Resources resources : resourcesList) {
                RestResource baseResource = null;
                if (resourcesList.size() > 1) {
                    String path = resources.getBase();
                    baseResource = service.addNewResource(path, path);
                }
                for (Resource resource : resources.getResourceList()) {
                    String name = getFirstTitle(resource.getDocList(), resource.getPath());
                    String path = resource.getPath();

                    RestResource newResource = null;

                    if (baseResource != null && path != null) {
                        for (RestResource res : baseResource.getChildResourceList()) {
                            if (path.equals(res.getPath())) {
                                newResource = res;
                                break;
                            }
                        }

                        if (newResource == null) {
                            newResource = baseResource.addNewChildResource(name, path);
                        }
                    } else if (path != null) {
                        for (RestResource res : service.getResourceList()) {
                            if (path.equals(res.getPath())) {
                                newResource = res;
                                break;
                            }
                        }

                        if (newResource == null) {
                            newResource = service.addNewResource(name, path);
                        }
                    } else {
                        newResource = service.addNewResource(name, "");
                    }

                    initResourceFromWadlResource(newResource, resource);
                    addSubResources(newResource, resource);
                }
            }
        } catch (InvalidDefinitionException ex) {
            ex.show();
        } catch (Exception e) {
            UISupport.showErrorMessage(e);
        }
    }

    private void addSubResources(RestResource newResource, Resource resource) {
        for (Resource res : resource.getResourceList()) {
            String path = res.getPath();
            if (path == null) {
                path = "";
            }

            String name = getFirstTitle(res.getDocList(), path);

            RestResource newRes = null;

            for (RestResource child : newResource.getChildResourceList()) {
                if (path.equals(child.getPath())) {
                    newRes = child;
                    break;
                }
            }

            if (newRes == null) {
                newRes = newResource.addNewChildResource(name, path);
            }

            initResourceFromWadlResource(newRes, res);

            addSubResources(newRes, res);
        }
    }

    private String getFirstTitle(List<Doc> list, String defaultTitle) {
        for (Doc doc : list) {
            if (StringUtils.hasContent(doc.getTitle())) {
                return doc.getTitle();
            }
        }
        return defaultTitle;
    }

    private void initResourceFromWadlResource(RestResource newResource, Resource resource) {
        for (Param param : resource.getParamList()) {
            param = resolveParameter(param);
            if (param != null) {
                String nm = param.getName();
                RestParamProperty prop = newResource.hasProperty(nm) ? newResource.getProperty(nm) : newResource
                        .addProperty(nm);

                initParam(param, prop);
            }
        }

        for (Method method : resource.getMethodList()) {
            method = resolveMethod(method);
            initMethod(newResource, method);
        }

        List<?> types = resource.getType();
        if (types != null && types.size() > 0) {
            for (Object obj : types) {
                ResourceTypeDocument.ResourceType type = resolveResource(obj.toString());
                if (type != null) {
                    for (Method method : type.getMethodList()) {
                        method = resolveMethod(method);
                        RestMethod restMethod = initMethod(newResource, method);

                        for (Param param : type.getParamList()) {
                            param = resolveParameter(param);
                            if (param != null) {
                                String nm = param.getName();
                                RestParamProperty prop = restMethod.hasProperty(nm) ? restMethod.getProperty(nm)
                                        : restMethod.addProperty(nm);

                                initParam(param, prop);
                            }
                        }
                    }
                }
            }
        }
    }

    private RestMethod initMethod(RestResource newResource, Method method) {
        // build name
        String name = getFirstTitle(method.getDocList(), method.getName());
        String id = method.getId();
        if (StringUtils.hasContent(id) && !id.trim().equals(name.trim())) {
            name += " - " + method.getId();
        }

        // ensure unique name
        if (newResource.getRestMethodByName(name) != null) {
            int cnt = 0;
            String orgName = name;

            while (newResource.getRestMethodByName(name) != null) {
                cnt++;
                name = orgName + "-" + cnt;
            }
        }

        // add to resource
        RestMethod restMethod = newResource.addNewMethod(name);
        restMethod.setMethod(RestRequestInterface.HttpMethod.valueOf(method.getName()));

        if (method.getRequest() != null) {
            for (Param param : method.getRequest().getParamList()) {
                param = resolveParameter(param);
                if (param != null) {
                    RestParamProperty p = restMethod.addProperty(param.getName());
                    initParam(param, p);
                }
            }

            for (Representation representation : method.getRequest().getRepresentationList()) {
                representation = resolveRepresentation(representation);
                addRepresentationFromConfig(restMethod, representation, RestRepresentation.Type.REQUEST, null);
            }
        }

        for (Response response : method.getResponseList()) {
            for (Representation representation : response.getRepresentationList()) {
                addRepresentation(response, restMethod, representation);
            }
            if (!isWADL11) {
                NodeList children = response.getDomNode().getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node n = children.item(i);
                    if ("fault".equals(n.getNodeName())) {
                        String content = XmlUtils.serialize(n, false);
                        try {
                            Map<Object, Object> map = new HashMap<Object, Object>();
                            XmlCursor cursor = response.newCursor();
                            cursor.getAllNamespaces(map);
                            cursor.dispose();
                            XmlOptions options = new XmlOptions();
                            options.setLoadAdditionalNamespaces(map);
                            // XmlObject obj = XmlObject.Factory.parse(
                            // content.replaceFirst( "<(([a-z]+:)?)fault ",
                            // "<$1representation " ), options );
                            XmlObject obj = XmlUtils.createXmlObject(
                                    content.replaceFirst("<(([a-z]+:)?)fault ", "<$1representation "), options);
                            RepresentationDocument representation = (RepresentationDocument) obj
                                    .changeType(RepresentationDocument.type);
                            addRepresentation(response, restMethod, representation.getRepresentation());
                        } catch (XmlException e) {
                        }
                    }
                }
            }
        }

        restMethod.addNewRequest("Request 1");
        return restMethod;
    }

    private void addRepresentation(Response response, RestMethod restMethod, Representation representation) {
        representation = resolveRepresentation(representation);
        List<Long> status = null;
        if (isWADL11) {
            status = response.getStatus();
        } else {
            Node n = representation.getDomNode().getAttributes().getNamedItem("status");
            if (n != null) {
                status = new ArrayList<Long>();
                for (String s : n.getNodeValue().split(" ")) {
                    status.add(Long.parseLong(s));
                }
            }
        }
        boolean fault = false;
        if (status != null && status.size() > 0) {
            fault = true;
            for (Long s : status) {
                if (s < 400) {
                    fault = false;
                    break;
                }
            }
        }
        RestRepresentation.Type type = fault ? RestRepresentation.Type.FAULT : RestRepresentation.Type.RESPONSE;
        addRepresentationFromConfig(restMethod, representation, type, status);
    }

    private void addRepresentationFromConfig(RestMethod restMethod, Representation representation,
                                             RestRepresentation.Type type, List<?> status) {
        RestRepresentation restRepresentation = restMethod.addNewRepresentation(type);
        restRepresentation.setMediaType(representation.getMediaType());
        restRepresentation.setElement(representation.getElement());
        if (status != null) {
            restRepresentation.setStatus(status);
        }
        restRepresentation.setId(representation.getId());
        restRepresentation.setDescription(getFirstTitle(representation.getDocList(), null));
    }

    private void initParam(Param param, RestParamProperty prop) {
        prop.setDefaultValue(param.getDefault());
        prop.setValue(param.getDefault());
        ParamStyle.Enum paramStyle = param.getStyle();
        if (paramStyle == null) {
            paramStyle = ParamStyle.QUERY;
        }

        prop.setStyle(ParameterStyle.valueOf(paramStyle.toString().toUpperCase()));
        prop.setRequired(param.getRequired());
        QName paramType = param.getType();
        prop.setType(paramType);

        String[] options = new String[param.sizeOfOptionArray()];
        for (int c = 0; c < options.length; c++) {
            options[c] = param.getOptionArray(c).getValue();
        }

        if (options.length > 0) {
            prop.setOptions(options);
        }
    }

    private Method resolveMethod(Method method) {
        String href = method.getHref();
        if (!StringUtils.hasContent(href)) {
            return method;
        }

        for (Method m : application.getMethodList()) {
            if (m.getId().equals(href.substring(1))) {
                return m;
            }
        }

        try {
            ApplicationDocument applicationDocument = loadReferencedWadl(href);
            if (applicationDocument != null) {
                int ix = href.lastIndexOf('#');
                if (ix > 0) {
                    href = href.substring(ix + 1);
                }

                for (Method m : application.getMethodList()) {
                    if (m.getId().equals(href)) {
                        return m;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return method;
    }

    private Representation resolveRepresentation(Representation representation) {
        String href = representation.getHref();
        if (!StringUtils.hasContent(href)) {
            return representation;
        }

        try {
            Application app = application;

            if (!href.startsWith("#")) {
                ApplicationDocument applicationDocument = loadReferencedWadl(href);
                app = applicationDocument.getApplication();
            }

            if (app != null) {
                int ix = href.lastIndexOf('#');
                if (ix >= 0) {
                    href = href.substring(ix + 1);
                }

                for (Representation m : application.getRepresentationList()) {
                    if (m.getId().equals(href)) {
                        return m;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return representation;
    }

    private Param resolveParameter(Param param) {
        String href = param.getHref();
        if (!StringUtils.hasContent(href)) {
            return param;
        }

        try {
            Application app = application;

            if (!href.startsWith("#")) {
                ApplicationDocument applicationDocument = loadReferencedWadl(href);
                app = applicationDocument.getApplication();
            }

            if (app != null) {
                int ix = href.lastIndexOf('#');
                if (ix >= 0) {
                    href = href.substring(ix + 1);
                }

                for (Param p : application.getParamList()) {
                    if (p.getId().equals(href)) {
                        return p;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

	/*
     * private Representation resolveFault( Representation representation ) {
	 * String href = representation.getHref(); if( !StringUtils.hasContent( href
	 * ) ) return representation;
	 * 
	 * try { ApplicationDocument applicationDocument = loadReferencedWadl( href
	 * ); if( applicationDocument != null ) { int ix = href.lastIndexOf( '#' );
	 * if( ix > 0 ) href = href.substring( ix + 1 );
	 * 
	 * for( Representation m : application.getFaultList() ) { if(
	 * m.getId().equals( href ) ) return m; } } } catch( Exception e ) {
	 * e.printStackTrace(); }
	 * 
	 * return representation; }
	 */

    private ResourceTypeDocument.ResourceType resolveResource(String id) {
        for (ResourceTypeDocument.ResourceType resourceType : application.getResourceTypeList()) {
            if (resourceType.getId().equals(id)) {
                return resourceType;
            }
        }

        try {
            ApplicationDocument applicationDocument = loadReferencedWadl(id);
            if (applicationDocument != null) {
                int ix = id.lastIndexOf('#');
                if (ix > 0) {
                    id = id.substring(ix + 1);
                }

                for (ResourceTypeDocument.ResourceType resourceType : applicationDocument.getApplication()
                        .getResourceTypeList()) {
                    if (resourceType.getId().equals(id)) {
                        return resourceType;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private ApplicationDocument loadReferencedWadl(String id) throws URISyntaxException, XmlException, IOException {
        int ix = id.indexOf('#');
        if (ix != -1) {
            id = id.substring(0, ix);
        }
        ApplicationDocument applicationDocument = refCache.get(id);

        if (applicationDocument == null) {
            URI uri = new URI(id);
            applicationDocument = ApplicationDocument.Factory.parse(uri.toURL());
            refCache.put(id, applicationDocument);
        }

        return applicationDocument;
    }

    public static Map<String, XmlObject> getDefinitionParts(String wadlUrl) {
        Map<String, XmlObject> result = new HashMap<String, XmlObject>();

        try {
            return SchemaUtils.getSchemas(wadlUrl, new UrlSchemaLoader(wadlUrl));

            // URL url = new URL(wadlUrl);
            // ApplicationDocument applicationDocument =
            // ApplicationDocument.Factory.parse(url);
            // result.put(url.getPath(), applicationDocument);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String extractParams(URL param, RestParamsPropertyHolder params) {
        String path = param.getPath();
        String[] items = path.split("/");

        int templateParamCount = 0;
        StringBuffer resultPath = new StringBuffer();

        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            try {
                String[] matrixParams = item.split(";");
                if (matrixParams.length > 0) {
                    item = matrixParams[0];
                    for (int c = 1; c < matrixParams.length; c++) {
                        String matrixParam = matrixParams[c];

                        int ix = matrixParam.indexOf('=');
                        if (ix == -1) {
                            params.addProperty(URLDecoder.decode(matrixParam, "Utf-8")).setStyle(ParameterStyle.MATRIX);
                        } else {
                            String name = matrixParam.substring(0, ix);
                            RestParamProperty property = params.addProperty(URLDecoder.decode(name, "Utf-8"));
                            property.setStyle(ParameterStyle.MATRIX);
                            property.setValue(URLDecoder.decode(matrixParam.substring(ix + 1), "Utf-8"));
                        }
                    }
                }

                Integer.parseInt(item);
                RestParamProperty prop = params.addProperty("param" + templateParamCount++);
                prop.setStyle(ParameterStyle.TEMPLATE);
                prop.setValue(item);

                item = "{" + prop.getName() + "}";
            } catch (Exception e) {
            }

            if (StringUtils.hasContent(item)) {
                resultPath.append('/').append(item);
            }
        }

        String query = ((URL) param).getQuery();
        if (StringUtils.hasContent(query)) {
            items = query.split("&");
            for (String item : items) {
                try {
                    int ix = item.indexOf('=');
                    if (ix == -1) {
                        params.addProperty(URLDecoder.decode(item, "Utf-8")).setStyle(ParameterStyle.QUERY);
                    } else {
                        String name = item.substring(0, ix);
                        RestParamProperty property = params.addProperty(URLDecoder.decode(name, "Utf-8"));
                        property.setStyle(ParameterStyle.QUERY);
                        property.setValue(URLDecoder.decode(item.substring(ix + 1), "Utf-8"));
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        return resultPath.toString();
    }
}
