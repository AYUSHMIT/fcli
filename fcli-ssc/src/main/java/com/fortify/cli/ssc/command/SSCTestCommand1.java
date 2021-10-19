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
package com.fortify.cli.ssc.command;

import com.fortify.cli.command.util.SubcommandOf;

import jakarta.inject.Singleton;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.UnirestInstance;
import lombok.SneakyThrows;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Singleton
@SubcommandOf(SSCCommand.class)
@Command(name = "test1", description = "SSC test 1")
public class SSCTestCommand1 implements Runnable {
	@Mixin SSCConnectionMixin sscConnectionMixin;

	@Override @SneakyThrows
	public void run() {
		/*
		 * System.out.println(SSCAuthenticatingRestConnection.builder()
		 * .baseUrl("http://localhost:2111/ssc").user("ssc").password("Fortify123!")
		 * .build() .api(SSCApplicationVersionAPI.class).
		 * queryApplicationVersions().build().getAll());
		 */
		/*
		Retrofit retrofit = new Retrofit.Builder()
			.baseUrl("http://localhost:2111/ssc/")
			.addConverterFactory(GsonConverterFactory.create())
			.build();
		String auth = Credentials.basic("ssc", "Fortify123!");
		System.out.println(auth);
		Call<SSCTokenResponse> createTokenCall = retrofit.create(SSCTokenOps.class).createToken(
				auth,
				SSCTokenRequest.builder().type("UnifiedLoginToken").build());
		Response<SSCTokenResponse> response = createTokenCall.execute();
		System.out.println(response);
		System.out.println(response.body());
		*/
		
		
		sscConnectionMixin.executeWithUnirest(this::executeRequest);
	}
	
	private Void executeRequest(UnirestInstance unirest) {
		unirest.get("/api/v1/events?limit=10")
		.accept("application/json")
		.header("Content-Type", "application/json")
		.asPaged(
				r->r.asJson(),
				r->getNextPageLink(r))
		.stream().map(HttpResponse::getBody).forEach(System.out::println);
		return null;
	}
	
	private String getNextPageLink(HttpResponse<Object> r) {
		return (String) ((JsonNode)r.getBody()).getObject().optQuery("/links/next/href");
	}
}