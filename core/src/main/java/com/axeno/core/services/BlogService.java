package com.axeno.core.services;

import com.axeno.core.models.BlogPage;

import java.util.List;

public interface BlogService {
    List<BlogPage> getBlogPages(String resolver);
}