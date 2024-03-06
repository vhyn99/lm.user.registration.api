package com.legalmatch.api.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legalmatch.api.filter.wrapper.CustomHttpServletRequestWrapper;
import graphql.parser.GraphqlAntlrToLanguage;
import graphql.parser.antlr.GraphqlLexer;
import graphql.parser.antlr.GraphqlParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.StringUtils;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Simple Filter for /graphql url. This handles the checking of the x api key of
 * the request headers if the same with our environment variable apiKey.
 * The Bean was defined in {@link com.legalmatch.api.AppConfig}
 * 
 */
@Slf4j
public class GraphqlFilter implements Filter {
	private static final String HTTP_X_API_KEY = "X-API-KEY";
	private final String apiKey;

	public GraphqlFilter(String apiKey) {
		this.apiKey = apiKey;
	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		log.info("Initializing filter :{}", this);
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest httpReq = (HttpServletRequest) request;
		final HttpServletResponse httpRes = (HttpServletResponse) response;
		final ServletRequest requestWrapper = new CustomHttpServletRequestWrapper((HttpServletRequest) request);

		if (isOptionsRequest(httpReq)) {
			chain.doFilter(requestWrapper, response);
			return;
		}
		
		if (isConfigPropertiesQuery(((CustomHttpServletRequestWrapper) requestWrapper).getBody())) { // allow config properties query even without api key.
		    chain.doFilter(requestWrapper, response);
		    return;
		}

		if (!isValidApiKey(httpReq)) {
			httpRes.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The api key is not valid or not specified.");
			return;
		}

		chain.doFilter(requestWrapper, response);		
	}

	private boolean isOptionsRequest(HttpServletRequest httpReq) {
		return httpReq.getMethod().equalsIgnoreCase("options");
	}

	/**
	 *
	 * @param body
	 * @return Boolean
	 * @throws IOException
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	private boolean isConfigPropertiesQuery(final String body)
			throws IOException, JsonProcessingException, JsonMappingException {
		final String gqlString = body;
		final ObjectMapper objectMapper = new ObjectMapper();
		final JsonNode gqlJson = objectMapper.readTree(gqlString);
		
		if(gqlJson.has("query")) {
			final String queryString = gqlJson.get("query").asText();
			final GraphqlLexer lexer = new GraphqlLexer(new ANTLRInputStream(queryString));
		    final CommonTokenStream tokens = new CommonTokenStream(lexer);
		    final GraphqlParser parser = new GraphqlParser(tokens);
		    final GraphqlParser.DocumentContext document = parser.document();
		    final GraphqlAntlrToLanguage antlrToLanguage = new GraphqlAntlrToLanguage(tokens);
		    antlrToLanguage.visitDocument(document);
		    if(null != antlrToLanguage.getResult()) {
			    return antlrToLanguage.getResult().toString().contains("Field{name='configProperties'");
		    }
		}
		return false;
	}

	@Override
	public void destroy() {
		log.warn("Destructing filter :{}", this);
	}

	private boolean isValidApiKey(HttpServletRequest httpReq) throws IOException {
		final String xApiKey = httpReq.getHeader(HTTP_X_API_KEY);

		return StringUtils.isEmpty(apiKey) || (StringUtils.isNotEmpty(xApiKey) && apiKey.equals(xApiKey));
	}

}
