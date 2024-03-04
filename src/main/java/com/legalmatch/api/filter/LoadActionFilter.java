package com.legalmatch.api.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoadActionFilter implements Filter {
    private final boolean isLoadActionEnabled;

    public LoadActionFilter(boolean isLoadActionEnabled) {
        this.isLoadActionEnabled = isLoadActionEnabled;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (!isLoadActionEnabled) {
            ((HttpServletResponse) res).setStatus(404);
            return;
        }

        chain.doFilter(req, res);
    }
}
