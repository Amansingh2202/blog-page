package com.axeno.core.servlets;

import com.axeno.core.models.BlogRoot;
import com.axeno.core.utils.GlobalObject;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component(
        service = Servlet.class,
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/readBlogJson",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=GET"
        }
)
public class ReadBlogJsonServlet extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response)
            throws IOException {

        try {


            if (GlobalObject.BlogRootObject != null) {
                response.getWriter().write("JSON already loaded");
                return;
            }

            Resource jsonResource =
                    request.getResourceResolver()
                            .getResource("/content/dam/blogging/blogData.json");

            if (jsonResource == null) {
                response.getWriter().write("JSON not found");
                return;
            }

            Resource original =
                    jsonResource.getChild("jcr:content/renditions/original");

            assert original != null;
            InputStream is =
                    original.adaptTo(InputStream.class);
            Gson gson=new Gson();

            assert is != null;
            String json =
                    IOUtils.toString(is, StandardCharsets.UTF_8);


            GlobalObject.BlogRootObject = gson.fromJson(json, BlogRoot.class);

            response.getWriter().write("JSON loaded successfully");

        } catch (Exception e) {
            response.getWriter().write("Error : " + e.getMessage());
        }
    }
}