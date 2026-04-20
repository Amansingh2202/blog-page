package com.axeno.core.jobs;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.*;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.event.jobs.Job;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Node;
import java.util.HashMap;
import java.util.Map;

@Component(
        service = JobConsumer.class,
        property = {
                JobConsumer.PROPERTY_TOPICS + "=blog/page/create"
        }
)
public class BlogJobConsumer implements JobConsumer {

    @Reference
    private ResourceResolverFactory resolverFactory;

//    private static final String PARENT_PATH = "/content/blogging/us/en";
    private static final String TEMPLATE_PATH = "/conf/blogging/settings/wcm/templates/blog-detail-template-";

    @Override
    public JobResult process(Job job) {

        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put(ResourceResolverFactory.SUBSERVICE, "blogServiceUser");

        try (ResourceResolver resolver =
                     resolverFactory.getServiceResourceResolver(authInfo)) {

            PageManager pageManager = resolver.adaptTo(PageManager.class);
            if (pageManager == null) {
                return JobResult.FAILED;
            }

            String pageName = (String) job.getProperty("pageName");
            String title = (String) job.getProperty("title");
            String content = (String) job.getProperty("content");
            String authorName = (String) job.getProperty("authorName");
            String authorBio = (String) job.getProperty("authorBio");
            String PARENT_PATH = (String) job.getProperty("parentPath");
            if (PARENT_PATH == null) {
                return JobResult.FAILED;
            }

            String pagePath = PARENT_PATH + "/" + pageName;

            if (resolver.getResource(pagePath) != null) {
                return JobResult.OK;
            }

            Page page = pageManager.create(
                    PARENT_PATH,
                    pageName,
                    TEMPLATE_PATH,
                    title
            );

            if (page == null) {
                return JobResult.FAILED;
            }

            Resource contentRes = resolver.getResource(page.getPath() + "/jcr:content");
            ModifiableValueMap props = contentRes.adaptTo(ModifiableValueMap.class);
            props.put("sling:resourceType", "blogging/components/page");
            props.put("authorName", authorName);

            Resource compRes = resolver.getResource(
                    page.getPath() + "/jcr:content/root/blog_component");

            if (compRes == null) {
                return JobResult.FAILED;
            }

            Node blogNode = compRes.adaptTo(Node.class);
            if (blogNode == null) {
                return JobResult.FAILED;
            }

            Node titleNode = blogNode.hasNode("title")
                    ? blogNode.getNode("title")
                    : blogNode.addNode("title");
            titleNode.setProperty("sling:resourceType", "blogging/components/title");
            titleNode.setProperty("text", title);

            Node contentNode = blogNode.hasNode("content")
                    ? blogNode.getNode("content")
                    : blogNode.addNode("content");
            contentNode.setProperty("sling:resourceType", "blogging/components/text");
            contentNode.setProperty("text", content);

            Node authorNode = blogNode.hasNode("authorName")
                    ? blogNode.getNode("authorName")
                    : blogNode.addNode("authorName");
            authorNode.setProperty("sling:resourceType", "blogging/components/text");
            authorNode.setProperty("text", authorName);

            Node bioNode = blogNode.hasNode("authorBio")
                    ? blogNode.getNode("authorBio")
                    : blogNode.addNode("authorBio");
            bioNode.setProperty("sling:resourceType", "blogging/components/text");
            bioNode.setProperty("text", authorBio);

            resolver.commit();

            return JobResult.OK;

        } catch (Exception e) {
            return JobResult.FAILED;
        }
    }
}