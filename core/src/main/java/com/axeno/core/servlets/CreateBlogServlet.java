package com.axeno.core.servlets;

import com.axeno.core.models.BlogPage;
import com.axeno.core.services.BlogService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(
        service = Servlet.class,
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/blog/create",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=GET"
        }
)
public class CreateBlogServlet extends SlingSafeMethodsServlet {

    @Reference
    private BlogService blogService;

    @Reference
    private JobManager jobManager;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response)
            throws IOException {
        String damPath    = request.getParameter("damPath");
        String parentPath = request.getParameter("parentPath");

        if (damPath == null || parentPath == null) {
            response.getWriter().write("MISSING_PARAMS");
            return;
        }

        List<BlogPage> blogPages =
                blogService.getBlogPages(damPath);

        for (int i = 0; i < blogPages.size(); i++) {

            BlogPage blog = blogPages.get(i);

            Map<String, Object> payload = new HashMap<>();
            payload.put("pageName", blog.getPageName());
            payload.put("title", blog.getTitle());
            payload.put("content", blog.getContent());
            payload.put("authorName", blog.getAuthorName());
            payload.put("authorBio", blog.getAuthorBio());
            payload.put("parentPath",  parentPath);

            jobManager.addJob("blog/page/create", payload);
        }

        response.getWriter().write("JOBS_CREATED");
    }
}