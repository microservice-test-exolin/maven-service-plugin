package org.exolin.msp.web.ui.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.exolin.msp.web.ui.Constants;
import org.exolin.msp.web.ui.Main;
import org.exolin.msp.web.ui.servlet.service.ListServicesServlet;
import org.exolin.msp.web.ui.servlet.service.ServiceConfigServlet;
import org.exolin.msp.web.ui.servlet.service.ServiceLogServlet;
import org.exolin.msp.web.ui.servlet.task.ProcessHistoryServlet;
import org.exolin.msp.web.ui.servlet.task.ProcessServlet;
import org.exolin.msp.web.ui.servlet.task.TaskLogServlet;

/**
 *
 * @author tomgk
 */
public class Layout
{
    private static final String NAME = "Service Web UI";
    
    private static void writeNav(Writer w) throws IOException
    {
        w.append("<nav class=\"navbar navbar-dark fixed-top bg-dark flex-md-nowrap p-0 shadow\">");
            w.append("<a class=\"navbar-brand col-sm-3 col-md-2 mr-0\" href=\"/\">").append(NAME).append("</a>");
            w.append("<input class=\"form-control form-control-dark w-100\" type=\"text\" placeholder=\"Search\" aria-label=\"Search\">");
            w.append("<ul class=\"navbar-nav px-3\">");
                w.append("<li class=\"nav-item text-nowrap\">");
                w.append("<a class=\"nav-link\" href=\"/sign-out\">Sign out</a>");
                w.append("</li>");
            w.append("</ul>");
        w.append("</nav>");
    }
    
    private static void writeMenuItem(Writer out, MenuItem item, boolean cur) throws IOException
    {
         out.append("<li class=\"nav-item\">\n");
         out.append("<a class=\"nav-link");
         
         if(cur)
            out.append(" active");
         
         out.append("\" href=\""+item.link+"\">");
         item.icon.writeTo(out);
         out.append(item.title);
         
         if(cur)
            out.append("<span class=\"sr-only\">(current)</span>");
         
         out.append("</a>");
    }
    
    public static class Menu
    {
        private final String title;
        private final List<MenuItem> items;

        public Menu(String name, MenuItem...items)
        {
            this(name, Arrays.asList(items));
        }
        
        public Menu(String name, List<MenuItem> items)
        {
            this.title = name;
            this.items = items;
        }

        public List<MenuItem> getItems()
        {
            return Collections.unmodifiableList(items);
        }
    }
    
    public static class MenuItem
    {
        private final String title;
        private final String link;
        private final Icon icon;

        public MenuItem(String name, String url, Icon icon)
        {
            this.title = name;
            this.link = url;
            this.icon = icon;
        }

        public String getLink()
        {
            return link;
        }
    }
    
    public static final List<Menu> menus = Arrays.asList(
                new Menu("Serice",
                        new MenuItem("Services", ListServicesServlet.URL, Icon.SERVICE),
                        new MenuItem("Configs", ServiceConfigServlet.URL, Icon.SETTINGS),
                        new MenuItem("Logs", ServiceLogServlet.URL, Icon.LOG)),
                new Menu("Tasks", 
                        new MenuItem("Processes", ProcessServlet.URL, Icon.PROCESS),
                        new MenuItem("Process History", ProcessHistoryServlet.URL, Icon.PROCESS),
                        new MenuItem("Logs", TaskLogServlet.URL, Icon.LOG)
                ),
                new Menu("Server",
                        new MenuItem("Server Info", "/server-info", Icon.SERVER)
                )
        );
    
    private static void writeSidebar(Writer w, String current) throws IOException
    {
        w.write("<nav class=\"col-md-2 d-none d-md-block bg-light sidebar\">");
        w.append("<div class=\"sidebar-sticky\">");
        
        w.append("<ul class=\"nav flex-column\">");
        
        writeMenuItem(w, new MenuItem("Dashboard", "/", Icon.HOME), current.equals("/"));
        
        //find longest matching link
        String currentMenu = menus.stream()
                .flatMap(m -> m.items.stream())
                .map(mi -> mi.link)
                .filter(link -> current.startsWith(link))
                .sorted(Comparator.comparing(String::length).reversed())
                .findFirst()
                .orElse(null);
        
        for(Menu menu: menus)
        {
            w.append("<h6 class=\"sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 text-muted\">");
            w.append(menu.title);
            w.append("</h6>");
            
            for(MenuItem menuItem: menu.items)
                writeMenuItem(w, menuItem, menuItem.link.equals(currentMenu));
        }
        
        w.write("</ul>");
        w.write("<div class=\"footer\">Started at "+Main.startedAt+"<br>Version:"+Constants.VERSION+"</div>");
        w.write("</div>");
        w.write("</nav>");
    }

    public static void end(PrintWriter out)
    {
        out.append("</main>");
        
        out.append("</div>");
        out.append("</div>");

        out.append("<script src=\"https://code.jquery.com/jquery-3.3.1.slim.min.js\" integrity=\"sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo\" crossorigin=\"anonymous\"></script>");
        out.append("<script src=\"https://getbootstrap.com/docs/4.1/assets/js/vendor/popper.min.js\"></script>");
        out.append("<script src=\"https://getbootstrap.com/docs/4.1/dist/js/bootstrap.min.js\"></script>");

        Icon.script(out);

        out.append("</body>");
        out.append("</html>");
    }

    public static void start(String title, String curPath, Writer out) throws IOException
    {
        out.append("<html>");
        out.append("<head>");
        out.append("<title>").append(title).append("</title>");
        out.append("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\" integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">");
        out.append("<link rel=\"stylesheet\" href=\"/dashboard.css\">");
        out.append("<link rel=\"stylesheet\" href=\"/log.css\">");
        out.append("<link rel=\"icon\" type=\"image/png\" href=\"/favicon.png\"/>");
        out.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">");
        out.append("</head>");

        out.append("<body>");

        Layout.writeNav(out);

        out.append("<div class=\"container-fluid\">");
        out.append("<div class=\"row\">");

        Layout.writeSidebar(out, curPath);

        out.append("<main role=\"main\" class=\"col-md-9 ml-sm-auto col-lg-10 px-4\">");
    }
}
        /*w.write(
"              <li class=\"nav-item\">\n" +
"                <a class=\"nav-link active\" href=\"#\">\n" +
"                  <span data-feather=\"home\"></span>\n" +
"                  Dashboard <span class=\"sr-only\">(current)</span>\n" +
"                </a>\n" +
"              </li>\n" +
"              <li class=\"nav-item\">\n" +
"                <a class=\"nav-link\" href=\"/services\">\n" +
"                  <span data-feather=\"file\"></span>\n" +
"                  Services\n" +
"                </a>\n" +
"              </li>\n");*/
/*
"              <li class=\"nav-item\">\n" +
"                <a class=\"nav-link\" href=\"#\">\n" +
"                  <span data-feather=\"shopping-cart\"></span>\n" +
"                  Products\n" +
"                </a>\n" +
"              </li>\n" +
"              <li class=\"nav-item\">\n" +
"                <a class=\"nav-link\" href=\"#\">\n" +
"                  <span data-feather=\"users\"></span>\n" +
"                  Customers\n" +
"                </a>\n" +
"              </li>\n" +
"              <li class=\"nav-item\">\n" +
"                <a class=\"nav-link\" href=\"#\">\n" +
"                  <span data-feather=\"bar-chart-2\"></span>\n" +
"                  Reports\n" +
"                </a>\n" +
"              </li>\n" +
"              <li class=\"nav-item\">\n" +
"                <a class=\"nav-link\" href=\"#\">\n" +
"                  <span data-feather=\"layers\"></span>\n" +
"                  Integrations\n" +
"                </a>\n" +
"              </li>\n" +*/
/*+
"\n" +
"            <h6 class=\"sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 text-muted\">\n" +
"              <span>Saved reports</span>\n" +
"              <a class=\"d-flex align-items-center text-muted\" href=\"#\">\n" +
"                <span data-feather=\"plus-circle\"></span>\n" +
"              </a>\n" +
"            </h6>\n" +
"            <ul class=\"nav flex-column mb-2\">\n" +
"              <li class=\"nav-item\">\n" +
"                <a class=\"nav-link\" href=\"#\">\n" +
"                  <span data-feather=\"file-text\"></span>\n" +
"                  Current month\n" +
"                </a>\n" +
"              </li>\n" +
"              <li class=\"nav-item\">\n" +
"                <a class=\"nav-link\" href=\"#\">\n" +
"                  <span data-feather=\"file-text\"></span>\n" +
"                  Last quarter\n" +
"                </a>\n" +
"              </li>\n" +
"              <li class=\"nav-item\">\n" +
"                <a class=\"nav-link\" href=\"#\">\n" +
"                  <span data-feather=\"file-text\"></span>\n" +
"                  Social engagement\n" +
"                </a>\n" +
"              </li>\n" +
"              <li class=\"nav-item\">\n" +
"                <a class=\"nav-link\" href=\"#\">\n" +
"                  <span data-feather=\"file-text\"></span>\n" +
"                  Year-end sale\n" +
"                </a>\n" +
"              </li>\n" +
"            </ul>\n"*/