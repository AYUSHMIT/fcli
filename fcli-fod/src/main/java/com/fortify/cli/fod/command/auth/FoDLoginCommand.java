/*******************************************************************************
 * (c) Copyright 2021 Micro Focus or one of its affiliates
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
package com.fortify.cli.fod.command.auth;

import java.util.Optional;

import com.fortify.cli.common.auth.login.ILoginHandler;
import com.fortify.cli.common.config.product.ProductOrGroup;
import com.fortify.cli.common.config.product.ProductOrGroup.ProductIdentifiers;
import com.fortify.cli.common.picocli.annotation.RequiresProduct;
import com.fortify.cli.common.picocli.annotation.SubcommandOf;
import com.fortify.cli.common.picocli.command.auth.login.AbstractAuthLoginCommand;
import com.fortify.cli.common.picocli.command.auth.login.AuthLoginCommand;
import com.fortify.cli.common.picocli.command.auth.login.LoginConnectionOptions;
import com.fortify.cli.common.picocli.command.auth.login.LoginUserCredentialOptions;
import com.fortify.cli.fod.auth.login.FoDLoginConfig;
import com.fortify.cli.fod.auth.login.FoDLoginHandler;
import com.fortify.cli.fod.auth.login.IFoDClientCredentialsConfig;
import com.fortify.cli.fod.auth.login.IFoDUserCredentialsConfig;

import io.micronaut.core.annotation.ReflectiveAccess;
import jakarta.inject.Inject;
import lombok.Getter;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@ReflectiveAccess
@SubcommandOf(AuthLoginCommand.class) 
@Command(name = ProductIdentifiers.FOD, description = "Login to FoD", sortOptions = false)
@RequiresProduct(ProductOrGroup.FOD)
public class FoDLoginCommand extends AbstractAuthLoginCommand<FoDLoginConfig> {
	@Getter @Inject private FoDLoginHandler sscLoginHandler;
	
	@ArgGroup(exclusive = false, multiplicity = "1", heading = "FoD connection options:%n", order = 1)
	@Getter private LoginConnectionOptions connectionOptions;
	
	@ArgGroup(exclusive = false, multiplicity = "1", heading = "FoD authentication options:%n", order = 2)
    @Getter private FoDAuthOptions authOptions;
	
	static class FoDAuthOptions {
		@ArgGroup(exclusive = true, multiplicity = "1", order = 3)
	    @Getter private FoDCredentialOptions credentialOptions;
	}
	
    static class FoDCredentialOptions {
    	@ArgGroup(exclusive = false, multiplicity = "1", order = 1) 
    	@Getter private FoDUserCredentialOptions userCredentialOptions = new FoDUserCredentialOptions();
    	@ArgGroup(exclusive = false, multiplicity = "1", order = 2) 
    	@Getter private FoDClientCredentialOptions clientCredentialOptions = new FoDClientCredentialOptions();
    }
    
    static class FoDUserCredentialOptions extends LoginUserCredentialOptions implements IFoDUserCredentialsConfig {
    	@Option(names = {"--tenant"}, required = true) 
    	@Getter private String tenant;
    }
    
    static class FoDClientCredentialOptions implements IFoDClientCredentialsConfig {
    	@Option(names = {"--client-id"}, required = true) 
    	@Getter private String clientId;
    	@Option(names = {"--client-secret"}, required = true, interactive = true, arity = "0..1", echo = false) 
    	@Getter private String clientSecret;
    }
	
	@Override
	protected String getAuthSessionType() {
		return ProductIdentifiers.FOD;
	}
	
	@Override
	protected final FoDLoginConfig getLoginConfig() {
		FoDLoginConfig config = new FoDLoginConfig();
		config.setConnectionConfig(getConnectionOptions());
		Optional.ofNullable(authOptions).map(FoDAuthOptions::getCredentialOptions).map(FoDCredentialOptions::getUserCredentialOptions).ifPresent(config::setFodUserCredentialsConfig);
		Optional.ofNullable(authOptions).map(FoDAuthOptions::getCredentialOptions).map(FoDCredentialOptions::getClientCredentialOptions).ifPresent(config::setFodClientCredentialsConfig);
		return config;
	}

	@Override
	protected ILoginHandler<FoDLoginConfig> getLoginHandler() {
		return sscLoginHandler;
	}
}
