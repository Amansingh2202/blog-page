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

        response.setContentType("application/json");

        try {

            if (GlobalObject.BlogRootObject != null) {

                ObjectMapper mapper = new ObjectMapper();

                response.getWriter().write(
                        mapper.writeValueAsString(
                                GlobalObject.BlogRootObject.getPages()
                        )
                );
                return;
            }


            Resource jsonResource =
                    request.getResourceResolver()
                            .getResource("/content/dam/blogging/blogData.json");

            if (jsonResource == null) {
                response.getWriter().write("{\"error\":\"JSON not found\"}");
                return;
            }

            Resource original =
                    jsonResource.getChild("jcr:content/renditions/original");

            if (original == null) {
                response.getWriter().write("{\"error\":\"Original rendition missing\"}");
                return;
            }


            try (InputStream is = original.adaptTo(InputStream.class)) {

                if (is == null) {
                    response.getWriter().write("{\"error\":\"InputStream null\"}");
                    return;
                }

                ObjectMapper mapper = new ObjectMapper();

                BlogRoot root =
                        mapper.readValue(is, BlogRoot.class);


                GlobalObject.BlogRootObject = root;

                response.getWriter().write(
                        mapper.writeValueAsString(root.getPages())
                );
            }

        } catch (Exception e) {
            response.getWriter().write(
                    "{\"error\":\"" + e.getMessage() + "\"}"
            );
        }
    }
}