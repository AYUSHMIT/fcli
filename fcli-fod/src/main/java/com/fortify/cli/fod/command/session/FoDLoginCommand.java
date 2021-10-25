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
package com.fortify.cli.fod.command.session;

import com.fortify.cli.common.command.session.login.AbstractSessionLoginCommand;
import com.fortify.cli.common.command.session.login.RootLoginCommand;
import com.fortify.cli.common.command.util.annotation.RequiresProduct;
import com.fortify.cli.common.command.util.annotation.SubcommandOf;
import com.fortify.cli.common.config.product.Product;
import com.fortify.cli.common.config.product.Product.ProductIdentifiers;
import com.fortify.cli.common.session.ILoginHandler;

import io.micronaut.core.annotation.ReflectiveAccess;
import jakarta.inject.Singleton;
import picocli.CommandLine.Command;

@Singleton @ReflectiveAccess
@SubcommandOf(RootLoginCommand.class)
@Command(name = ProductIdentifiers.FOD, description = "Login to FoD", sortOptions = false)
@RequiresProduct(Product.FOD)
public class FoDLoginCommand extends AbstractSessionLoginCommand<Object> {

	@Override
	protected String getLoginSessionType() {
		return ProductIdentifiers.FOD;
	}

	@Override
	protected Object getConnectionConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ILoginHandler<Object> getLoginHandler() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
