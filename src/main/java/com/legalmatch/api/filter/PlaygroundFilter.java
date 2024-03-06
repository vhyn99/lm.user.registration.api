package com.legalmatch.api.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PlaygroundFilter implements Filter {
    private final boolean isPlaygroundEnabled;

    public PlaygroundFilter(boolean isPlaygroundEnabled) {
        this.isPlaygroundEnabled = isPlaygroundEnabled;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (!isPlaygroundEnabled) {
            ((HttpServletResponse) res).setStatus(404);
            return;
        }

        chain.doFilter(req, res);
    }
}
