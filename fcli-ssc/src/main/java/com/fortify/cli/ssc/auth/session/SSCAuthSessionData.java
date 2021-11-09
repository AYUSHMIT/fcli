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
package com.fortify.cli.ssc.auth.session;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fortify.cli.common.auth.session.AbstractAuthSessionData;
import com.fortify.cli.common.auth.session.summary.AuthSessionSummary;
import com.fortify.cli.ssc.auth.login.SSCLoginConfig;
import com.fortify.cli.ssc.auth.login.rest.SSCTokenResponse;
import com.fortify.cli.ssc.auth.login.rest.SSCTokenResponse.SSCTokenData;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true)  @Introspected @JsonIgnoreProperties(ignoreUnknown = true)
public class SSCAuthSessionData extends AbstractAuthSessionData {
	private char[] predefinedToken;
	private SSCTokenResponse cachedTokenResponse;
	
	public SSCAuthSessionData() {}
	
	public SSCAuthSessionData(SSCLoginConfig config) {
		super(config.getConnectionConfig());
		this.predefinedToken = config.getToken();
	}
	
	public SSCAuthSessionData(SSCLoginConfig config, SSCTokenResponse cachedTokenResponse) {
		this(config);
		this.cachedTokenResponse = cachedTokenResponse;
	}
	
	@JsonIgnore 
	public final char[] getActiveToken() {
		if ( hasActiveCachedTokenResponse() ) {
			return getCachedTokenResponseData().getToken();
		} else {
			return predefinedToken;
		}
	}
	
	@JsonIgnore
	public final boolean hasActiveCachedTokenResponse() {
		return getCachedTokenResponseData()!=null && getCachedTokenResponseData().getTerminalDate().after(new Date()); 
	}
	
	@JsonIgnore
	private SSCTokenData getCachedTokenResponseData() {
		return cachedTokenResponse==null || cachedTokenResponse.getData()==null 
				? null
				: cachedTokenResponse.getData();
	}
	
	@JsonIgnore
	private Date getCachedTokenTerminalDate() {
		return getCachedTokenResponseData()==null ? null : getCachedTokenResponseData().getTerminalDate();
	}
	
	@JsonIgnore
	protected Date getSessionExpiryDate() {
		Date sessionExpiryDate = AuthSessionSummary.EXPIRES_UNKNOWN;
		if ( getCachedTokenTerminalDate()!=null ) {
			sessionExpiryDate = getCachedTokenTerminalDate();
		}
		return sessionExpiryDate;
	}
}