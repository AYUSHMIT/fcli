package com.fortify.cli;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.apache.commons.logging.impl.SimpleLog;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import com.fortify.cli.command.RootCommand;
import com.fortify.cli.command.ssc.SSCTestCommand1.SSCTokenRequest;
import com.fortify.cli.command.ssc.SSCTestCommand1.SSCTokenResponse;
import com.fortify.cli.command.util.SubcommandOf;
import com.oracle.svm.core.annotate.AutomaticFeature;

import io.micronaut.configuration.picocli.MicronautFactory;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import picocli.CommandLine;

public class FortifyCLI {
	private static int execute(Class<?> clazz, String[] args) {
		try (ApplicationContext context = ApplicationContext.builder(FortifyCLI.class, Environment.CLI).start()) {
			RootCommand rootCommand = context.getBean(RootCommand.class);
			MicronautFactory factory = new MicronautFactory(context);
			CommandLine commandLine = new CommandLine(rootCommand, factory).setCaseInsensitiveEnumValuesAllowed(true)
					.setUsageHelpAutoWidth(true);
			addSubcommands(context, factory, commandLine, rootCommand);
			return commandLine.execute(args);
		}
	}

	private static void addSubcommands(ApplicationContext context, MicronautFactory factory, CommandLine commandLine,
			RootCommand rootCommand) {
		Map<Class<?>, List<Object>> parentToSubcommandsMap = getParentToSubcommandsMap(context);
		addSubcommands(parentToSubcommandsMap, factory, commandLine, rootCommand);
	}

	private static final void addSubcommands(Map<Class<?>, List<Object>> parentToSubcommandsMap,
			MicronautFactory factory, CommandLine commandLine, Object command) {
		List<Object> subcommands = parentToSubcommandsMap.get(command.getClass());
		if (subcommands != null) {
			for (Object subcommand : subcommands) {
				CommandLine subCommandLine = new CommandLine(subcommand, factory);
				addSubcommands(parentToSubcommandsMap, factory, subCommandLine, subcommand);
				commandLine.addSubcommand(subCommandLine);
			}
		}
	}

	private static final Map<Class<?>, List<Object>> getParentToSubcommandsMap(ApplicationContext context) {
		Collection<BeanDefinition<?>> beanDefinitions = context.getBeanDefinitions(Qualifiers.byStereotype(SubcommandOf.class));
		return beanDefinitions.stream().collect(
			Collectors.groupingBy(bd -> bd.getAnnotation(SubcommandOf.class).classValue().get(),
			                      Collectors.mapping(context::getBean, Collectors.toList())));
	}

	public static void main(String[] args) {
		int exitCode = execute(RootCommand.class, args);
		System.exit(exitCode);
	}
	
	@AutomaticFeature
	public static final class RuntimeReflectionRegistrationFeature implements Feature {
		public void beforeAnalysis(BeforeAnalysisAccess access) {
			RuntimeReflection.register(String.class);
			RuntimeReflection.register(LogFactoryImpl.class);
			RuntimeReflection.register(LogFactoryImpl.class.getDeclaredConstructors());
			RuntimeReflection.register(LogFactory.class);
			RuntimeReflection.register(LogFactory.class.getDeclaredConstructors());
			RuntimeReflection.register(SimpleLog.class);
			RuntimeReflection.register(SimpleLog.class.getDeclaredConstructors());
			/*
			RuntimeReflection.register(SSCTokenRequest.class);
			RuntimeReflection.register(SSCTokenRequest.class.getDeclaredConstructors());
			RuntimeReflection.register(SSCTokenResponse.class.getDeclaredMethods());
			RuntimeReflection.register(SSCTokenRequest.class.getDeclaredFields());
			RuntimeReflection.register(SSCTokenResponse.class);
			RuntimeReflection.register(SSCTokenResponse.class.getDeclaredConstructors());
			RuntimeReflection.register(SSCTokenResponse.class.getDeclaredMethods());
			RuntimeReflection.register(SSCTokenResponse.class.getDeclaredFields());
			*/
			
		}
	}
}
