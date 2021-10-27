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
package com.fortify.cli.common.output.xml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fortify.cli.common.output.IOutputWriter;
import com.fortify.cli.common.output.OutputWriterConfig;

import lombok.SneakyThrows;

public class XmlOutputWriter implements IOutputWriter {
	private final boolean pretty = true;

	public XmlOutputWriter(OutputWriterConfig config) {
		// TODO Auto-generated constructor stub
	}

	@Override @SneakyThrows
	public void write(JsonNode jsonNode) {
		XmlMapper xmlMapper = new XmlMapper();

        if(! (jsonNode instanceof ObjectNode)){
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode root = objectMapper.createObjectNode();
            root.set("item", jsonNode);

            jsonNode = root;
        }

        if (pretty){
            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        String xmlString = xmlMapper.writeValueAsString(jsonNode).replace("ObjectNode", "content");

        System.out.println(xmlString);
	}

}