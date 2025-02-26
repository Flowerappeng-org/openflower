package org.lowcoder.api.framework.plugin.endpoint;

import static org.lowcoder.sdk.exception.BizError.NOT_AUTHORIZED;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.OPTIONS;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lowcoder.api.framework.plugin.data.PluginServerRequest;
import org.lowcoder.api.framework.plugin.security.PluginAuthorizationManager;
import org.lowcoder.api.framework.plugin.security.SecuredEndpoint;
import org.lowcoder.plugin.api.EndpointExtension;
import org.lowcoder.plugin.api.PluginEndpoint;
import org.lowcoder.plugin.api.data.EndpointRequest;
import org.lowcoder.plugin.api.data.EndpointResponse;
import org.lowcoder.sdk.exception.BaseException;
import org.lowcoder.sdk.exception.BizException;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.target.SimpleBeanTargetSource;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.ServerResponse.BodyBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class PluginEndpointHandlerImpl implements PluginEndpointHandler
{
	private List<RouterFunction<ServerResponse>> routes = new ArrayList<>();
	
	private final ApplicationContext applicationContext;
	private final DefaultListableBeanFactory beanFactory;
	private final PluginAuthorizationManager pluginAuthorizationManager;
	
	@Override
	public void registerEndpoints(String pluginUrlPrefix, List<PluginEndpoint> endpoints) 
	{
		String urlPrefix = PLUGINS_BASE_URL + pluginUrlPrefix;
		
		if (CollectionUtils.isNotEmpty(endpoints))
		{
			for (PluginEndpoint endpoint : endpoints)
			{
				Method[] handlers = endpoint.getClass().getDeclaredMethods();
				if (handlers != null && handlers.length > 0)
				{
					for (Method handler : handlers)
					{
						registerEndpointHandler(urlPrefix, endpoint, handler);
					}
				}
			}
			
			((ReloadableRouterFunctionMapping)beanFactory.getBean("routerFunctionMapping")).reloadFunctionMappings();
		}
	}
	
	@Override
	public List<RouterFunction<ServerResponse>> registeredEndpoints() 
	{
		return routes;
	}

	private void registerEndpointHandler(String urlPrefix, PluginEndpoint endpoint, Method handler)
	{
		if (!handler.isAnnotationPresent(EndpointExtension.class) || !checkHandlerMethod(handler))
		{
			if (handler.isAnnotationPresent(EndpointExtension.class))
			{
				log.debug("Not registering plugin endpoint method: {} -> {}! Handler method must be defined as: public EndpointResponse methodName(EndpointRequest request)", endpoint.getClass().getSimpleName(), handler.getName(), handler.getName());
			}
			return;
		}

		EndpointExtension endpointMeta = handler.getAnnotation(EndpointExtension.class);			
		String endpointName = endpoint.getClass().getSimpleName() + "_" + handler.getName();		
		RouterFunction<ServerResponse> routerFunction = route(createRequestPredicate(urlPrefix, endpointMeta), req -> runPluginEndpointMethod(endpoint, endpointMeta, handler, req));				
		routes.add(routerFunction);				
		registerRouterFunctionMapping(endpointName, routerFunction);

		log.info("Registered endpoint: {} -> {}: {}", endpoint.getClass().getSimpleName(), endpointMeta.method(), urlPrefix + endpointMeta.uri());
	}

	public Mono<ServerResponse> runPluginEndpointMethod(PluginEndpoint endpoint, EndpointExtension endpointMeta, Method handler, ServerRequest request)
	{
		log.info("Running plugin endpoint method {}\nRequest: {}", handler.getName(), request);

		Mono<Authentication> monoAuthentication = ReactiveSecurityContextHolder.getContext().map(SecurityContext::getAuthentication).cache();
		Mono<AuthorizationDecision> decisionMono = monoAuthentication.flatMap(authentication -> {
			MethodInvocation methodInvocation = null;
			try {
				methodInvocation = getMethodInvocation(endpointMeta, authentication);
			} catch (NoSuchMethodException e) {
				return Mono.error(new RuntimeException(e));
			}
			return pluginAuthorizationManager.check(monoAuthentication, methodInvocation);
		});

		return decisionMono.<EndpointResponse>handle((authorizationDecision, sink) -> {
			if(!authorizationDecision.isGranted()) {
				sink.error(new BizException(NOT_AUTHORIZED, "NOT_AUTHORIZED"));
				return;
			}
			try {
				sink.next((EndpointResponse) handler.invoke(endpoint, PluginServerRequest.fromServerRequest(request)));
			} catch (IllegalAccessException | InvocationTargetException e) {
				sink.error(new RuntimeException(e));
			}
		}).flatMap(this::createServerResponse);
	}

	private static @NotNull MethodInvocation getMethodInvocation(EndpointExtension endpointMeta, Authentication authentication) throws NoSuchMethodException {
		Method method = Authentication.class.getMethod("isAuthenticated");
		Object[] arguments = new Object[]{"someString", endpointMeta};
        return new MethodInvocation() {
			@NotNull
			@Override
			public Method getMethod() {
				return method;
			}

			@NotNull
			@Override
			public Object[] getArguments() {
				return arguments;
			}

			@Nullable
			@Override
			public Object proceed() throws Throwable {
				return null;
			}

			@Nullable
			@Override
			public Object getThis() {
				return authentication;
			}

			@NotNull
			@Override
			public AccessibleObject getStaticPart() {
				return null;
			}
		};
	}


	private void registerRouterFunctionMapping(String endpointName, RouterFunction<ServerResponse> routerFunction)
	{
		String beanName = "pluginEndpoint_" + endpointName + "_" + System.currentTimeMillis();
		((GenericApplicationContext)applicationContext).registerBean(beanName, RouterFunction.class, () -> routerFunction );
		log.debug("Registering RouterFunction bean definition: {}", beanName);
	}
	
	
	private Mono<ServerResponse> createServerResponse(EndpointResponse pluginResponse)
	{
		/** Create response with given status **/
		BodyBuilder builder = ServerResponse.status(pluginResponse.statusCode());

		/** Set response headers **/
		if (pluginResponse.headers() != null && !pluginResponse.headers().isEmpty())
		{
			pluginResponse.headers().entrySet()
				.forEach(entry ->  builder.header(entry.getKey(), entry.getValue().toArray(new String[] {})));
		}
		
		/** Set cookies if available **/
		if (pluginResponse.cookies() != null && !pluginResponse.cookies().isEmpty())
		{
			pluginResponse.cookies().values()
				.forEach(cookies -> cookies
						.forEach(cookie -> builder
								.cookie(ResponseCookie.from(cookie.getKey(), cookie.getValue()).build())));
		}
		
		/** Set response body if available **/
		if (pluginResponse.body() != null)
		{
			return builder.bodyValue(pluginResponse.body());
		}

		return builder.build();
	}
	
	private boolean checkHandlerMethod(Method method)
	{
		ResolvableType returnType = ResolvableType.forMethodReturnType(method);

		return (returnType.getRawClass().isAssignableFrom(EndpointResponse.class)
					&& method.getParameterCount() == 1
					&& method.getParameterTypes()[0].isAssignableFrom(EndpointRequest.class)
		);
	}
	
	private RequestPredicate createRequestPredicate(String basePath, EndpointExtension endpoint)
	{
		switch(endpoint.method())
		{
			case GET:
				return GET(pluginEndpointUri(basePath, endpoint.uri()));
			case POST:
				return POST(pluginEndpointUri(basePath, endpoint.uri()));
			case PUT:
				return PUT(pluginEndpointUri(basePath, endpoint.uri()));
			case PATCH:
				return PATCH(pluginEndpointUri(basePath, endpoint.uri()));
			case DELETE:
				return DELETE(pluginEndpointUri(basePath, endpoint.uri()));
			case OPTIONS:
				return OPTIONS(pluginEndpointUri(basePath, endpoint.uri()));
		}
		return null;
	}

	private String pluginEndpointUri(String basePath, String uri)
	{
		return StringUtils.join(basePath, StringUtils.prependIfMissing(uri, "/"));
	}
	

}
