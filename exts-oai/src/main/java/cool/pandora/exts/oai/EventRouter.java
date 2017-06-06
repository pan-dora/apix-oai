/*
 * Copyright 2016 Amherst College
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cool.pandora.exts.oai;

import static org.apache.camel.Exchange.*;
import static java.net.URLEncoder.encode;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.util.ExchangeHelper.getMandatoryHeader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.NoSuchHeaderException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.jena.util.URIref;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_IDENTIFIER;
import static org.slf4j.LoggerFactory.getLogger;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;

/**
 * @author Christopher Johnson
 */
public class EventRouter extends RouteBuilder {
    private static final String FCREPO_URI = "http://localhost:8080/fcrepo/rest";
    private static final String OAI_VERB = "verb";
    private static final String OAI_SET = "set";
    private static final String HTTP_ACCEPT = "Accept";
    private static final Logger LOGGER = getLogger(EventRouter.class);

    /**
     * Configure the message route workflow.
     */
    public void configure() throws Exception {

        from("jetty:http://{{rest.host}}:{{rest.port}}{{rest.prefix}}?" +
                "optionsEnabled=true&matchOnUriPrefix=true&sendServerVersion=false&httpMethodRestrict=GET,OPTIONS")
                .routeId("SPARQLRouter")
                .process(e -> e.getIn().setHeader(FCREPO_IDENTIFIER,
                        e.getIn().getHeader("Apix-Ldp-Resource-Path",
                                e.getIn().getHeader(HTTP_PATH))))
                .removeHeaders(HTTP_ACCEPT)
                .choice()
                    .when(header(HTTP_METHOD).isEqualTo("GET"))
                        .to("direct:sparql")
                .when(header(HTTP_METHOD).isEqualTo("OPTIONS"))
                .to("direct:options");
        from("direct:options")
                .routeId("XmlOptions")
                .setHeader(CONTENT_TYPE).constant("text/turtle")
                .setHeader("Allow").constant("GET,OPTIONS")
                .to("language:simple:resource:classpath:options.ttl");
        from("direct:sparql")
                .routeId("SparqlGet")
                .choice()
                    .when(header(OAI_VERB).isEqualTo("ListRecords"))
                        .setHeader(HTTP_METHOD).constant("POST")
                        .setHeader(CONTENT_TYPE).constant("application/x-www-form-urlencoded; charset=utf-8")
                        .setHeader(HTTP_ACCEPT).constant("application/rdf+xml")
                        .process(e -> e.getIn().setBody(sparqlSelect(ConstructList(getSet(e)))))
                        .to("{{triplestore.baseUrl}}?useSystemProperties=true&bridgeEndpoint=true")
                        .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                        .setHeader(CONTENT_TYPE).constant("application/xml")
                        .convertBodyTo(String.class)
                        .log(LoggingLevel.INFO, LOGGER,"Transforming RDF to OAI_DC/XML")
                        .to("xslt:{{oai_dc.xslt}}?saxon=true");
     }

    private static String sparqlSelect(final String command) {
        try {
            return "query=" + encode(command, "UTF-8");
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    private static String ConstructList(final String setName) {
        return "CONSTRUCT {?s ?p ?o} WHERE {?s <http://www.openarchives.org/OAI/2.0/setSpec> \"" + setName + "\";?p ?o} ";
    }

    private static String graph(final Exchange ex) throws NoSuchHeaderException {
        return URIref.encode(getMandatoryHeader(ex, FCREPO_URI, String.class));
    }

    private static String getSet(final Exchange e) {
        final Object optHdr = e.getIn().getHeader(OAI_SET);

        if (optHdr instanceof Collection) {
            return String.join(" ",  new HashSet<>((Collection<String>) optHdr));
        } else if (optHdr != null) {
            return (String) optHdr;
        }
        return "";
    }
}
