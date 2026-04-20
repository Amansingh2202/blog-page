package com.axeno.core.servlets;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/blog/search",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class BlogSearchServlet extends SlingSafeMethodsServlet {

    private static final int PAGE_SIZE = 3;

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Read params from jQuery
        String pageParam  = request.getParameter("page");
        String searchText = request.getParameter("searchText");

        if (searchText == null) searchText = "";
        searchText = searchText.trim();


        // faltu ( ? good practise  )
        int pageNum = 1;
        try {
            pageNum = Integer.parseInt(pageParam);
        } catch (Exception e) {
            pageNum = 1;
        }

        int offset = (pageNum - 1) * PAGE_SIZE;

        // Build JCR Query
        Map<String, String> predicates = new HashMap<>();
        predicates.put("path", "/content/blogging/us/en");
        predicates.put("type", "cq:Page");

        if (!searchText.isEmpty()) {
            predicates.put("fulltext",searchText);
            predicates.put("fulltext.relPath",
                    "jcr:content");
        }
         // skipping first n results
        predicates.put("p.offset",
                String.valueOf(offset));
        // giving back next n result
        predicates.put("p.limit",
                String.valueOf(PAGE_SIZE));
        predicates.put("p.guessTotal", "100");
        predicates.put("orderby", "@jcr:content/cq:lastModified");
        predicates.put("orderby.sort", "dec");
        predicates.put("orderby.1", "@jcr:content/jcr:title");
        predicates.put("orderby.1.sort", "asc");

        //  Execute Query
        ResourceResolver resolver =
                request.getResourceResolver();
        Session session =
                resolver.adaptTo(Session.class);

        Query query = queryBuilder.createQuery(
                PredicateGroup.create(predicates), session);
        SearchResult result = query.getResult();

        //  Build JSON manually
        //  using StringBuilder
        StringBuilder blogsArray = new StringBuilder();
        blogsArray.append("[");

        boolean firstItem = true;

        for (Hit hit : result.getHits()) {
            try {
                Resource pageResource = hit.getResource();
                Resource jcrContent =
                        pageResource.getChild("jcr:content");

                if (jcrContent == null) continue;

                ValueMap pageProps =
                        jcrContent.getValueMap();

                String pageTitle  =
                        pageProps.get("jcr:title", "");
                String pageAuthor =
                        pageProps.get("authorName", "");
                String pagePath   =
                        pageResource.getPath();

                // Getting  blog_component children
                Resource blogComp =
                        jcrContent.getChild(
                                "root/blog_component");

                String compTitle   = "";
                String compContent = "";
                String compBio     = "";

                if (blogComp != null) {

                    Resource titleNode =
                            blogComp.getChild("title");
                    if (titleNode != null) {
                        compTitle = titleNode
                                .getValueMap()
                                .get("text", "");
                    }

                    Resource contentNode =
                            blogComp.getChild("content");
                    if (contentNode != null) {
                        compContent = contentNode
                                .getValueMap()
                                .get("text", "");
                    }

                    Resource bioNode =
                            blogComp.getChild("authorBio");
                    if (bioNode != null) {
                        compBio = bioNode
                                .getValueMap()
                                .get("text", "");
                    }
                }

                // Use compTitle if pageTitle is empty
                String finalTitle =
                        pageTitle.isEmpty()
                                ? compTitle : pageTitle;

                // Adding comma between items
                if (!firstItem) {
                    blogsArray.append(",");
                }
                firstItem = false;

                blogsArray.append("{")
                        .append("\"pagePath\":")
                        .append("\"")
                        .append(escape(pagePath))
                        .append("\",")
                        .append("\"pageTitle\":")
                        .append("\"")
                        .append(escape(finalTitle))
                        .append("\",")
                        .append("\"authorName\":")
                        .append("\"")
                        .append(escape(pageAuthor))
                        .append("\",")
                        .append("\"authorBio\":")
                        .append("\"")
                        .append(escape(compBio))
                        .append("\",")
                        .append("\"content\":")
                        .append("\"")
                        .append(escape(compContent))
                        .append("\",")
                        .append("\"pageUrl\":")
                        .append("\"")
                        .append(escape(pagePath + ".html"))
                        .append("\"")
                        .append("}");

            } catch (Exception e) {
                // skip bad hits
            }
        }

        blogsArray.append("]");

        // Calculate pagination values
        long totalMatches = result.getTotalMatches();
        int totalPages =
                (int) Math.ceil(
                        (double) totalMatches / PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;

        boolean hasPrev = pageNum > 1;
        boolean hasNext = pageNum < totalPages;

        // Build final JSON response
        String finalJson = "{"
                + "\"blogs\":"        + blogsArray.toString()  + ","
                + "\"totalResults\":" + totalMatches           + ","
                + "\"currentPage\":"  + pageNum                + ","
                + "\"totalPages\":"   + totalPages             + ","
                + "\"hasPrev\":"      + hasPrev                + ","
                + "\"hasNext\":"      + hasNext
                + "}";

        // Write response back to jQuery
        response.getWriter().write(finalJson);
    }


    private String escape(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}