package com.github.llyb120.namilite.init;

import com.github.llyb120.namilite.config.NamiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.regex.Pattern;

@Order(-200)
@Configuration
public class NamiSpringFilter extends UrlRewriteFilter {

    public static Route[] routes;

    @Autowired
    NamiConfig namiConfig;

    @Override
    protected void loadUrlRewriter(FilterConfig filterConfig) throws ServletException {
        StringBuilder sb = new StringBuilder();
        sb.append("<urlrewrite>");
        Map<String, String> pks = namiConfig.controlPackages();
        routes = new Route[pks.size()];
        int i =0;
        for (Map.Entry<String, String> entry : pks.entrySet()) {
            String url = entry.getKey().replaceAll("\\:c|\\:a","([^\\/\\\\?]+)");
            Pattern p = Pattern.compile(url);
            sb.append("<rule><from>^");
            sb.append(url);
            sb.append("$</from>");
            sb.append("<run class=\"com.github.llyb120.namilite.rewrite.UrlRewriteHolder\" />\n");
            sb.append("<to qsappend=\"true\">/nami/$1/$2");
            sb.append("</to></rule>");
            routes[i++] = new Route(p, entry.getValue());
        }
        sb.append("</urlrewrite>");
            try{
                //Create a UrlRewrite Conf object with the injected resource
                Conf conf = new Conf(filterConfig.getServletContext(), new ByteArrayInputStream(sb.toString().getBytes()), "urlrewrite.xml", "@@traceability@@");
                checkConf(conf);
            } catch (Exception ex) {
                throw new ServletException("Unable to load URL rewrite configuration file from ", ex);
            }
    }

    public static class Route{
        public Pattern p;
        public String packageName;

        public Route(Pattern p, String packageName) {
            this.p = p;
            this.packageName = packageName;
        }

        public boolean matches(String url){
            return p.matcher(url).find();
        }
    }


}
