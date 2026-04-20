package com.axeno.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/submitForm",
                "sling.servlet.methods=POST"
        }
)
public class FormServlet extends SlingAllMethodsServlet {

    @Override
    protected void doPost(SlingHttpServletRequest req,
                          SlingHttpServletResponse res)
            throws IOException {

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String dob = req.getParameter("DOB");

        // Print (for testing)
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("DOB: " + dob);

        res.getWriter().write("your name is = " + name);
        res.getWriter().write("your email is = " + email);
        res.getWriter().write("your name is = " + dob);

        res.getWriter().write("Form Submitted Successfully!");
    }
}