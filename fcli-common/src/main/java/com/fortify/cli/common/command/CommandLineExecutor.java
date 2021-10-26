/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.cli.common.command;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fortify.cli.common.command.util.DefaultValueProvider;
import com.fortify.cli.common.command.util.annotation.AlphaFeature;
import com.fortify.cli.common.command.util.annotation.RequiresProduct;
import com.fortify.cli.common.command.util.annotation.SubcommandOf;
import com.fortify.cli.common.config.alpha.AlphaFeaturesHelper;
import com.fortify.cli.common.config.product.EnabledProductsHelper;
import com.fortify.cli.common.config.product.Product;

import io.micronaut.configuration.picocli.MicronautFactory;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import picocli.CommandLine;

@Singleton
public class CommandLineExecutor {
	@Getter private final FCLIRootCommand rootCommand;
	@Getter private final ApplicationContext applicationContext;
	@Getter private final AlphaFeaturesHelper alphaFeaturesHelper;
	@Getter private final EnabledProductsHelper enabledProductsHelper;
	@Getter private final MicronautFactory factory;
	@Getter private CommandLine commandLine;
	@Inject
	public CommandLineExecutor(
			FCLIRootCommand rootCommand,  
			ApplicationContext applicationContext,
			AlphaFeaturesHelper alphaFeaturesHelper,
			EnabledProductsHelper enabledProductsHelper) {
		this.rootCommand = rootCommand;
		this.applicationContext = applicationContext;
		this.alphaFeaturesHelper = alphaFeaturesHelper;
		this.enabledProductsHelper = enabledProductsHelper;
		this.factory = new MicronautFactory(applicationContext);
	}
	
	@PreDestroy
	public void closeFactory() {
		this.factory.close();
	}
	
	@PostConstruct
	public void createCommandLine() {
		this.commandLine = new CommandLine(rootCommand, factory)
				.setCaseInsensitiveEnumValuesAllowed(true)
				.setDefaultValueProvider(new DefaultValueProvider())
				.setUsageHelpAutoWidth(true); // TODO Add ExceptionHandler?
		addSubcommands(commandLine, getRootCommand());
	}
	
	public final int execute(String[] args) {
		return getCommandLine().execute(args);
	}
	
	private void addSubcommands(CommandLine commandLine, FCLIRootCommand rootCommand) {
		Map<Class<?>, List<Object>> parentToSubcommandsMap = getParentToSubcommandsMap();
		addSubcommands(parentToSubcommandsMap, commandLine, rootCommand);
	}

	private final void addSubcommands(Map<Class<?>, List<Object>> parentToSubcommandsMap, CommandLine commandLine, Object command) {
		List<Object> subcommands = parentToSubcommandsMap.get(command.getClass());
		if (subcommands != null) {
			for (Object subcommand : subcommands) {
				CommandLine subCommandLine = new CommandLine(subcommand, getFactory());
				try {
					commandLine.addSubcommand(subCommandLine);
				} catch ( RuntimeException e ) {
					throw new RuntimeException("Error while adding command class "+subcommand.getClass().getName(), e);
				}
				addSubcommands(parentToSubcommandsMap, subCommandLine, subcommand);
			}
		}
	}

	private final Map<Class<?>, List<Object>> getParentToSubcommandsMap() {
		final var beanDefinitions = getApplicationContext().getBeanDefinitions(Qualifiers.byStereotype(SubcommandOf.class));
		
		var parentToSubcommandsMap = new LinkedHashMap<Class<?>, List<Object>>();
		beanDefinitions.stream()
		// TODO Instead of filtering disabled commands, it would be better to replace these with a generic, hidden command
		//      that tells the user that the command has been disabled (and for what reason). Requires some more research 
		//      though on how to properly do this.
			.filter(this::isEnabled) 
			.sorted(this::compare)
			.forEach(bd ->
				addMultiValueEntry(
						parentToSubcommandsMap, 
						getParentCommandClazz(bd),
						getApplicationContext().getBean(bd)));
		// TODO Remove commands that do not have any runnable or callable children (because those have been filtered based on product 
		return parentToSubcommandsMap;
	}

	private boolean isEnabled(BeanDefinition<?> bd) {
		return isRequiredProductEnabled(bd) && isNotAlphaOrAllowed(bd);
	}

	private boolean isNotAlphaOrAllowed(BeanDefinition<?> bd) {
		return !bd.hasAnnotation(AlphaFeature.class) || alphaFeaturesHelper.isAlphaFeaturesEnabled();	
	}
	
	private boolean isRequiredProductEnabled(BeanDefinition<?> bd) {
		boolean result = true;
		AnnotationValue<RequiresProduct> annotation = bd.getAnnotation(RequiresProduct.class);
		if ( annotation!=null ) {
			Optional<Product> product = annotation.enumValue(Product.class);
			result = enabledProductsHelper.isProductEnabled(product);
		}
		return result;
	}

	private static final Class<?> getParentCommandClazz(BeanDefinition<?> bd) {
		Optional<Class<?>> optClazz = bd.getAnnotation(SubcommandOf.class).classValue();
		if ( !optClazz.isPresent() ) {
			throw new IllegalStateException("No parent command found for class "+bd.getBeanType().getName());
		}
		return optClazz.get();
	}
	
	private int compare(BeanDefinition<?> bd1, BeanDefinition<?> bd2) {
		return Integer.compare(OrderUtil.getOrder(bd1.getAnnotationMetadata()), OrderUtil.getOrder(bd2.getAnnotationMetadata()));
	}

	private static final <K, V> void addMultiValueEntry(LinkedHashMap<K, List<V>> map, K key, V value) {
		map.computeIfAbsent(key, k->new LinkedList<V>()).add(value);
	}
}