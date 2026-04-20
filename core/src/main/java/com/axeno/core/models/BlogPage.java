package com.axeno.core.models;



public class BlogPage {

    private String pageName;
    private String title;
    private String content;
    private String authorName;
    private String authorBio;

    public String getPageName() {
        return pageName;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorBio() {
        return authorBio;
    }
    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setAuthorBio(String authorBio) {
        this.authorBio = authorBio;
    }
}