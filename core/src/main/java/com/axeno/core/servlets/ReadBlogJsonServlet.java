package com.axeno.core.servlets;

import com.axeno.core.models.BlogRoot;
import com.axeno.core.utils.GlobalObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.InputStream;

@Component(
        service = Servlet.class,
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/loadBlogJson",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=GET"
        }
)
public class ReadBlogJsonServlet extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response)
            throws IOException {

        response.setContentType("text/plain");

        try {


            if (GlobalObject.BlogRootObject != null) {
                response.setStatus(200);
                response.getWriter().write("JSON_ALREADY_LOADED");
                return;
            }

            Resource jsonResource =
                    request.getResourceResolver()
                            .getResource("/content/dam/blogging/blogData.json");

            if (jsonResource == null) {
                response.setStatus(404);
                response.getWriter().write("JSON_NOT_FOUND");
                return;
            }

            Resource original =
                    jsonResource.getChild("jcr:content/renditions/original");

            if (original == null) {
                response.setStatus(500);
                response.getWriter().write("ORIGINAL_RENDITION_MISSING");
                return;
            }

            try (InputStream is = original.adaptTo(InputStream.class)) {

                if (is == null) {
                    response.setStatus(500);
                    response.getWriter().write("STREAM_NULL");
                    return;
                }

                ObjectMapper mapper = new ObjectMapper();

                BlogRoot root =
                        mapper.readValue(is, BlogRoot.class);

                GlobalObject.BlogRootObject = root;
            }

            response.setStatus(200);
            response.getWriter().write("SUCCESS");

        } catch (Exception e) {

            response.setStatus(500);
            response.getWriter().write("FAILED");
        }
    }
}