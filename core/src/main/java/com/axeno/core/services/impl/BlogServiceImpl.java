package com.axeno.core.services.impl;

import com.axeno.core.models.BlogPage;
import com.axeno.core.services.BlogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component(service = BlogService.class)
public class BlogServiceImpl implements BlogService {

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public List<BlogPage> getBlogPages(String DAM_PATH) {

        Map<String, Object> param = new HashMap<>();
        param.put(ResourceResolverFactory.SUBSERVICE, "blogServiceUser");

        try (ResourceResolver resolver =
                     resolverFactory.getServiceResourceResolver(param)) {

            if (resolver == null) {
                return Collections.emptyList();
            }

            Resource resource = resolver.getResource(DAM_PATH);

            if (resource == null) {
                return Collections.emptyList();
            }

            Resource original = resource.getChild("jcr:content/renditions/original");

            if (original == null) {
                return Collections.emptyList();
            }

            Resource content = original.getChild("jcr:content");

            if (content == null) {
                return Collections.emptyList();
            }

            try (InputStream is = content.adaptTo(InputStream.class)) {

                if (is == null) {
                    return Collections.emptyList();
                }

                ObjectMapper mapper = new ObjectMapper();

                String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                if (json.trim().isEmpty()) {
                    return Collections.emptyList();
                }

                json = json.trim();

                // ✅ CASE 1 → JSON ARRAY
                if (json.startsWith("[")) {

                    BlogPage[] pages =
                            mapper.readValue(json, BlogPage[].class);

                    return Arrays.asList(pages);
                }

                // ✅ CASE 2 → JSON OBJECT
                else if (json.startsWith("{")) {

                    Map<String, Object> map =
                            mapper.readValue(json, Map.class);

                    Object pagesObj = map.get("pages");

                    if (pagesObj == null) {
                        return Collections.emptyList();
                    }

                    List<Map<String, Object>> pages =
                            mapper.convertValue(
                                    pagesObj,
                                    new TypeReference<List<Map<String, Object>>>() {}
                            );

                    List<BlogPage> blogPages = new ArrayList<>();

                    for (Map<String, Object> p : pages) {

                        BlogPage blog = new BlogPage();

                        blog.setPageName((String) p.get("pageName"));
                        blog.setTitle((String) p.get("title"));
                        blog.setContent((String) p.get("content"));
                        blog.setAuthorName((String) p.get("authorName"));
                        blog.setAuthorBio((String) p.get("authorBio"));

                        blogPages.add(blog);
                    }

                    return blogPages;
                }

                return Collections.emptyList();
            }

        } catch (Exception e) {
            // Ideally use logger instead of print
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}