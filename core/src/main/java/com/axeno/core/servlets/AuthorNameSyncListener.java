package com.axeno.core.servlets;

import org.apache.sling.api.resource.*;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(
        service = ResourceChangeListener.class,
        property = {
                // only watch jcr:content nodes under your blog pages
                ResourceChangeListener.PATHS + "=/content/blogging/us/en",
                ResourceChangeListener.CHANGES + "=CHANGED"
        }
)
public class AuthorNameSyncListener implements ResourceChangeListener {

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public void onChange(List<ResourceChange> changes) {

        for (ResourceChange change : changes) {
            String changedPath = change.getPath();

            //  only react when jcr:content itself is changed (page properties save)
            if (!changedPath.endsWith("/jcr:content")) {
                continue;
            }

            syncAuthorName(changedPath);
        }
    }

    private void syncAuthorName(String jcrContentPath) {

        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put(ResourceResolverFactory.SUBSERVICE, "blogServiceUser");

        try (ResourceResolver resolver =
                     resolverFactory.getServiceResourceResolver(authInfo)) {

            Resource jcrContent = resolver.getResource(jcrContentPath);
            if (jcrContent == null) return;

            // read authorName from page properties
            String authorName = jcrContent.getValueMap()
                    .get("authorName", String.class);
            if (authorName == null) return;

            // navigate to blog_component/authorName node
            Resource authorNameRes = resolver.getResource(
                    jcrContentPath + "/root/blog_component/authorName"
            );
            if (authorNameRes == null) return;

            ModifiableValueMap authorProps =
                    authorNameRes.adaptTo(ModifiableValueMap.class);
            if (authorProps == null) return;

            //  only write if value actually changed — avoids unnecessary commits
            String existingValue = authorProps.get("text", String.class);
            if (authorName.equals(existingValue)) return;

            authorProps.put("text", authorName);
            resolver.commit();

        } catch (Exception e) {
            // log the error
        }
    }
}