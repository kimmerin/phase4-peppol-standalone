package com.helger.phase4.peppolstandalone.spi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.unece.cefact.namespaces.sbdh.StandardBusinessDocument;
import org.w3c.dom.Document;

import com.helger.commons.http.HttpHeaderMap;
import com.helger.peppol.sbdh.PeppolSBDHData;
import com.helger.peppol.sbdh.PeppolSBDHDataReader;
import com.helger.peppolid.factory.SimpleIdentifierFactory;
import com.helger.phase4.config.AS4Configuration;
import com.helger.phase4.ebms3header.Ebms3MessageInfo;
import com.helger.phase4.ebms3header.Ebms3UserMessage;

class __Run_ProcessDocument {
    
    static String invXML;
    
    static {
        try {
            InputStream invIS = __Run_ProcessDocument.class.getClassLoader().getResourceAsStream("external/example-invoice.xml");
            assertNotNull(invIS, "check existence of invoice stream");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read;
            while ((read = invIS.read()) != -1) {
                baos.write(read);
            }
            invXML = baos.toString("utf8")
                    .replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "")
                    .trim();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    void testXml() throws Exception {
        String xml = sbdExampleStart + invXML + sbdExampleEnd;
        final PeppolSBDHDataReader aReader = new PeppolSBDHDataReader (SimpleIdentifierFactory.INSTANCE)
                .setPerformValueChecks (false)
                .setCheckForCountryC1 (false);
        
        PeppolSBDHData psbd = aReader.extractData(new ByteArrayInputStream(xml.getBytes("UTF8")));
        StandardBusinessDocument sbd = psbd.getAsStandardBusinessDocument();
        HttpHeaderMap map = new HttpHeaderMap();
        map.addHeader("X-SomeHeader", "SomeValue");
        
        Ebms3UserMessage eb3um = new Ebms3UserMessage();
        Ebms3MessageInfo eb3mi = new Ebms3MessageInfo();
        eb3mi.setMessageId("EB3MessageIDValue");
        eb3um.setMessageInfo(eb3mi);
        
        CustomPeppolIncomingSBDHandlerSPI cpis = new CustomPeppolIncomingSBDHandlerSPI();
        Document resDoc = cpis.performDataRequest(map, sbd, psbd, eb3um, AS4Configuration.getConfig());
        
        cpis.writeXML(resDoc, System.out);
    }
    
    @Test
    void testTextContent() throws Exception {
        String xml = sbdExampleStart + edifactExampleXml + sbdExampleEnd;
        final PeppolSBDHDataReader aReader = new PeppolSBDHDataReader (SimpleIdentifierFactory.INSTANCE)
                .setPerformValueChecks (false)
                .setCheckForCountryC1 (false);
        
        PeppolSBDHData psbd = aReader.extractData(new ByteArrayInputStream(xml.getBytes("UTF8")));
        StandardBusinessDocument sbd = psbd.getAsStandardBusinessDocument();
        HttpHeaderMap map = new HttpHeaderMap();
        map.addHeader("X-SomeHeader", "SomeValue");
        
        Ebms3UserMessage eb3um = new Ebms3UserMessage();
        Ebms3MessageInfo eb3mi = new Ebms3MessageInfo();
        eb3mi.setMessageId("EB3MessageIDValue");
        eb3um.setMessageInfo(eb3mi);
        
        CustomPeppolIncomingSBDHandlerSPI cpis = new CustomPeppolIncomingSBDHandlerSPI();
        Document resDoc = cpis.performDataRequest(map, sbd, psbd, eb3um, AS4Configuration.getConfig());
        
        cpis.writeXML(resDoc, System.out);
    }
    
    static String sbdExampleStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
            + "<StandardBusinessDocument xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n"
            + "xmlns=\"http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader\">\r\n"
            + "<StandardBusinessDocumentHeader>\r\n"
            + "<HeaderVersion>1.0</HeaderVersion>\r\n"
            + "<Sender>\r\n"
            + "<Identifier Authority=\"iso6523-actorid-upis\">0088:7315458756324</Identifier>\r\n"
            + "</Sender>\r\n"
            + "<Receiver>\r\n"
            + "<Identifier Authority=\"iso6523-actorid-upis\">0088:4562458856624</Identifier>\r\n"
            + "</Receiver>\r\n"
            + "<DocumentIdentification>\r\n"
            + "<Standard>urn:oasis:names:specification:ubl:schema:xsd:Invoice-2</Standard>\r\n"
            + "<TypeVersion>2.1</TypeVersion>\r\n"
            + "<InstanceIdentifier>123123</InstanceIdentifier>\r\n"
            + "<Type>Invoice</Type>\r\n"
            + "<CreationDateAndTime>2019-02-01T15:42:10Z</CreationDateAndTime>\r\n"
            + "</DocumentIdentification>\r\n"
            + "<BusinessScope>\r\n"
            + "<Scope>\r\n"
            + "<Type>DOCUMENTID</Type>\r\n"
            + "<InstanceIdentifier>urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0::2.1</InstanceIdentifier>\r\n"
            + "<Identifier>busdox-docid-qns</Identifier>\r\n"
            + "</Scope>\r\n"
            + "<Scope>\r\n"
            + "<Type>PROCESSID</Type>\r\n"
            + "<InstanceIdentifier>urn:fdc:peppol.eu:2017:poacc:billing:01:1.0</InstanceIdentifier>\r\n"
            + "<Identifier>cenbii-procid-ubl</Identifier>\r\n"
            + "</Scope>\r\n"
            + "<Scope>\r\n"
            + "<Type>COUNTRY_C1</Type>\r\n"
            + "<InstanceIdentifier>BE</InstanceIdentifier>\r\n"
            + "</Scope>\r\n"
            + "</BusinessScope>\r\n"
            + "</StandardBusinessDocumentHeader>\r\n";
    
    static String sbdExampleEnd = "\r\n</StandardBusinessDocument>";
    
    static String edifactExampleXml = "<TextContent xmlns=\"http://peppol.eu/xsd/ticc/envelope/1.0\"\r\n"
            + "mimeType=\"Application/EDIFACT\">\r\n"
            + "UNB+UNOA:2+9930711378399:14+7798032711116:14+160927:2252+EW861380947'UNH+186453437+CONTRL\r\n"
            + ":D:96A:UN:EAN002'UCI+F6GVY+7658032710006:14+9930711378111:14+8'UCM+3HHL0+ORDERS:D:96A:UN:\r\n"
            + "EAN008+7'UNT+4+186453437'UNZ+1+EW861380947'\r\n"
            + "</TextContent>";
}
