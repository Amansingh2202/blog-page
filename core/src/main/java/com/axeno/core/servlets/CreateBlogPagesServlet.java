package com.axeno.core.servlets;

import com.axeno.core.models.BlogPage;
import com.axeno.core.utils.GlobalObject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.servlets.ServletResolverConstants;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Page;
import org.osgi.service.component.annotations.Component;

import javax.jcr.Node;
import javax.servlet.Servlet;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/createBlogPages",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=GET"
        }
)
public class CreateBlogPagesServlet extends SlingSafeMethodsServlet {

    private static final String PARENT_PATH = "/content/blogging/us/en";
    private static final String TEMPLATE_PATH = "/conf/blogging/settings/wcm/templates/blog-detail-template-";

    @Override
    protected void doGet(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response)
            throws IOException {

        response.setContentType("text/plain");

        try {


            if (GlobalObject.BlogRootObject == null) {
                response.setStatus(400);
                response.getWriter().write("JSON_NOT_LOADED");
                return;
            }

            ResourceResolver resolver = request.getResourceResolver();

            PageManager pageManager =
                    resolver.adaptTo(PageManager.class);

            if (pageManager == null) {
                response.setStatus(500);
                response.getWriter().write("PAGE_MANAGER_NULL");
                return;
            }


            for (BlogPage blog : GlobalObject.BlogRootObject.getPages()) {

                String pageName = blog.getPageName();

                String pagePath = PARENT_PATH + "/" + pageName;


                Resource existingPage =
                        resolver.getResource(pagePath);

                if (existingPage != null) {
                    continue;
                }


                Page page =
                        pageManager.create(
                                PARENT_PATH,
                                pageName,
                                TEMPLATE_PATH,
                                blog.getTitle()
                        );

                if (page == null) {
                    continue;
                }

                String componentPath =
                        page.getPath() +
                                "/jcr:content/root/blog_component";

                Resource componentResource =
                        resolver.getResource(componentPath);

                if (componentResource == null) {
                    continue;
                }


                Node blogNode = componentResource.adaptTo(Node.class);

// -------- TITLE ----------
                Node titleNode;
                if (blogNode.hasNode("title")) {
                    titleNode = blogNode.getNode("title");
                } else {
                    titleNode = blogNode.addNode("title");
                    titleNode.setProperty("sling:resourceType", "blogging/components/title");
                }
                titleNode.setProperty("text", blog.getTitle());


// -------- CONTENT ----------
                Node contentNode;
                if (blogNode.hasNode("content")) {
                    contentNode = blogNode.getNode("content");
                } else {
                    contentNode = blogNode.addNode("content");
                    contentNode.setProperty("sling:resourceType", "blogging/components/text");
                }
                contentNode.setProperty("text", blog.getContent());


// -------- AUTHOR NAME ----------
                Node authorNode;
                if (blogNode.hasNode("authorName")) {
                    authorNode = blogNode.getNode("authorName");
                } else {
                    authorNode = blogNode.addNode("authorName");
                    authorNode.setProperty("sling:resourceType", "blogging/components/text");
                }
                authorNode.setProperty("text", blog.getAuthorName());


// --------------- AUTHOR BIO ----------
                Node bioNode;
                if (blogNode.hasNode("authorBio")) {
                    bioNode = blogNode.getNode("authorBio");
                } else {
                    bioNode = blogNode.addNode("authorBio");
                    bioNode.setProperty("sling:resourceType", "blogging/components/text");
                }
                bioNode.setProperty("text", blog.getAuthorBio());

            }

            resolver.commit();

            response.setStatus(200);
            response.getWriter().write("PAGES_CREATED_SUCCESSFULLY");

        } catch (Exception e) {

            response.setStatus(500);
            response.getWriter().write("PAGE_CREATION_FAILED");
        }
    }
}