/*
 * Copyright (C) 2023-2025 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.phase4.peppolstandalone.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unece.cefact.namespaces.sbdh.StandardBusinessDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.helger.commons.annotation.IsSPIImplementation;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.http.HttpHeaderMap;
import com.helger.config.IConfig;
import com.helger.peppol.reporting.api.PeppolReportingItem;
import com.helger.peppol.reporting.api.backend.PeppolReportingBackend;
import com.helger.peppol.reporting.api.backend.PeppolReportingBackendException;
import com.helger.peppol.sbdh.PeppolSBDHData;
import com.helger.phase4.config.AS4Configuration;
import com.helger.phase4.ebms3header.Ebms3Error;
import com.helger.phase4.ebms3header.Ebms3UserMessage;
import com.helger.phase4.incoming.IAS4IncomingMessageMetadata;
import com.helger.phase4.incoming.IAS4IncomingMessageState;
import com.helger.phase4.peppol.servlet.IPhase4PeppolIncomingSBDHandlerSPI;
import com.helger.phase4.peppol.servlet.Phase4PeppolServletMessageProcessorSPI;
import com.helger.phase4.peppolstandalone.APConfig;
import com.helger.sbdh.SBDMarshaller;
import com.helger.security.certificate.CertificateHelper;
import com.helger.xml.XMLHelper;
import com.helger.xml.serialize.read.DOMReader;

/**
 * This is a way of handling incoming Peppol messages
 *
 * @author Philip Helger
 */
@IsSPIImplementation
public class CustomPeppolIncomingSBDHandlerSPI implements IPhase4PeppolIncomingSBDHandlerSPI
{
    private class MyTrustManager extends X509ExtendedTrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
        
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            
        }
        
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            
        }
        
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                throws CertificateException {
            
        }
        
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
                throws CertificateException {
            
        }
        
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                throws CertificateException {
            
        }
        
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
                throws CertificateException {
            
        }
    }
    
  private static final Logger LOGGER = LoggerFactory.getLogger (CustomPeppolIncomingSBDHandlerSPI.class);

  public void handleIncomingSBD (@Nonnull final IAS4IncomingMessageMetadata aMessageMetadata,
                                 @Nonnull final HttpHeaderMap aHeaders,
                                 @Nonnull final Ebms3UserMessage aUserMessage,
                                 @Nonnull final byte [] aSBDBytes,
                                 @Nonnull final StandardBusinessDocument aSBD,
                                 @Nonnull final PeppolSBDHData aPeppolSBD,
                                 @Nonnull final IAS4IncomingMessageState aIncomingState,
                                 @Nonnull final ICommonsList <Ebms3Error> aProcessingErrorMessages) throws Exception
  {
    final String sMyPeppolSeatID = APConfig.getMyPeppolSeatID ();

    // Example code snippets how to get data
    LOGGER.info ("Received a new Peppol Message");
    LOGGER.info ("  C1 = " + aPeppolSBD.getSenderAsIdentifier ().getURIEncoded ());
    LOGGER.info ("  C2 = " + CertificateHelper.getSubjectCN (aIncomingState.getSigningCertificate ()));
    LOGGER.info ("  C3 = " + sMyPeppolSeatID);
    LOGGER.info ("  C4 = " + aPeppolSBD.getReceiverAsIdentifier ().getURIEncoded ());
    LOGGER.info ("  DocType = " + aPeppolSBD.getDocumentTypeAsIdentifier ().getURIEncoded ());
    LOGGER.info ("  Process = " + aPeppolSBD.getProcessAsIdentifier ().getURIEncoded ());
    LOGGER.info ("  CountryC1 = " + aPeppolSBD.getCountryC1 ());

    // T ODO add your code here
    // E.g. write to disk, write to S3, write to database, write to queue...
    // LOGGER.error ("You need to implement handleIncomingSBD to deal with
    // incoming messages");
    
    // In case there is an error, send an Exception
    
    IConfig config = AS4Configuration.getConfig();
    Document resDoc = performDataRequest(aHeaders, aSBD, aPeppolSBD, aUserMessage, config);
    Node pr = XMLHelper.getFirstChildElementOfName(resDoc, "ProcessResult");
    if (pr == null) {
        LOGGER.error(
                "No parseable result received from _data");
        throw new Exception("internal error"); // TODO better wording
    }
    String status = String.valueOf(XMLHelper.getFirstChildText(XMLHelper.getFirstChildElementOfName(pr, "Status")));
    String errorMessage = XMLHelper.getFirstChildText(XMLHelper.getFirstChildElementOfName(pr, "ErrorMessage"));
    String sC3ID = XMLHelper.getFirstChildText(XMLHelper.getFirstChildElementOfName(pr, "C3Id"));
    String sEndUserID = XMLHelper.getFirstChildText(XMLHelper.getFirstChildElementOfName(pr, "EndUserId"));
    String sC4CountryCode = XMLHelper.getFirstChildText(XMLHelper.getFirstChildElementOfName(pr, "C4CountryCode"));
    String jobNr = Objects.toString(XMLHelper.getFirstChildText(XMLHelper.getFirstChildElementOfName(pr, "JobNr")), "none");
    
    LOGGER.info("Processing of incoming request finished with status '" + status + "' and job number " + jobNr);
    
    if (!Objects.toString(status).equals("success")) {
        LOGGER.error("Processing of incoming request failed: " + status + "/" + errorMessage);
        throw new Exception("processing of request failed: " + errorMessage);
    }
    
    if (sEndUserID == null) {
        LOGGER.info("Processing of incoming request with job number " + jobNr + " provided no end user ID. Skipping reporting");
        return;
    }

    // Last action in this method
    new Thread ( () -> {
      // TODO If you have a way to determine the real end user of the message
      // here, this might be a good opportunity to store the data for Peppol
      // Reporting (do this asynchronously as the last activity)
      // Note: this is a separate thread so that it does not block the sending
      // of the positive receipt message

      // TODO Peppol Reporting - enable if possible to be done in here
      try {
          LOGGER.info ("Creating Peppol Reporting Item and storing it");

//          // TODO determine correct values for the next three fields
//          final String sC3ID = sMyPeppolSeatID;
//          final String sC4CountryCode = "AT";
//          final String sEndUserID = "EndUserID";

          // Create the reporting item
          final PeppolReportingItem aReportingItem = Phase4PeppolServletMessageProcessorSPI.createPeppolReportingItemForReceivedMessage (aUserMessage,
                                                                                                                                         aPeppolSBD,
                                                                                                                                         aIncomingState,
                                                                                                                                         sC3ID,
                                                                                                                                         sC4CountryCode,
                                                                                                                                         sEndUserID);
          PeppolReportingBackend.withBackendDo (APConfig.getConfig (),
                                                aBackend -> aBackend.storeReportingItem (aReportingItem));
        }
        catch (final PeppolReportingBackendException ex)
        {
          LOGGER.error ("Failed to store Peppol Reporting Item", ex);
          // TODO improve error handling
        }
    }).start ();
  }

    Document performDataRequest(final HttpHeaderMap aHeaders, final StandardBusinessDocument aSBD,
            final PeppolSBDHData aPeppolSBD, Ebms3UserMessage aUserMessage, IConfig config) throws Exception {
        if (aPeppolSBD.getBusinessMessageNoClone() == null) {
            throw new IllegalArgumentException("no business message in request");
        }
        
        Document doc = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
        Node root = doc.appendChild(doc.createElement("InboundPeppolRequest"));
        Node htHeaders = root.appendChild(doc.createElement("HttpRequestHeaders"));
        aHeaders.forEachSingleHeader((key, val) -> {
            Element req = (Element) htHeaders.appendChild(doc.createElement("HttpRequestHeader"));
            req.setAttribute("key", key);
            req.setTextContent(val);
        }, true);
        
        root.appendChild(doc.createElement("Sender")).setTextContent(aPeppolSBD.getSenderValue());
        root.appendChild(doc.createElement("Receiver")).setTextContent(aPeppolSBD.getReceiverValue());
        root.appendChild(doc.createElement("CountryC1")).setTextContent(aPeppolSBD.getCountryC1());
        root.appendChild(doc.createElement("InstanceIdentifier")).setTextContent(aPeppolSBD.getInstanceIdentifier());
        root.appendChild(doc.createElement("DocumentTypeInstanceIdentifier")).setTextContent(aPeppolSBD.getDocumentTypeValue());
        root.appendChild(doc.createElement("EBMSMessageID")).setTextContent(aUserMessage.getMessageInfo().getMessageId());
        Node recDoc = root.appendChild(doc.createElement("ReceivedBusinessDocument"));
        
        byte[] sbdBytes = new SBDMarshaller().getAsBytes(aSBD);
        String b64Code = new String(Base64.getEncoder().encode(sbdBytes), "8859_1");
//        System.out.println(b64Code);
        recDoc.setTextContent(b64Code);
        
        String url = config.getAsString("lobster.data.url");
        if (url == null) {
            LOGGER.error(
                    "Processing of incoming request failed: internal/no URL configured to be used to send request to _data");
            throw new IllegalArgumentException("internal error");
        }
        LOGGER.info("Trying to call _data at " + url);
//        writeXML(doc, System.out);
        
        InputStream is = performHTTPRequest(doc, url, false);
        
        Document resDoc = DOMReader.readXMLDOM(is);
        return resDoc;
    }

    InputStream performHTTPRequest(Document doc, String url, boolean isRedirect) throws Exception {
        HttpURLConnection huc = (HttpURLConnection) new URI(url).toURL().openConnection();
        huc.setInstanceFollowRedirects(true);
        if (huc instanceof HttpsURLConnection) {
            KeyManager[] km = null;
            X509TrustManager tm = new MyTrustManager();
            TrustManager[] tma = { tm };
            SSLContext sc = SSLContext.getInstance("ssl");
            sc.init(km, tma, new SecureRandom());
            sc.getClientSessionContext().setSessionTimeout(1);
            sc.getClientSessionContext().setSessionCacheSize(1);

            HttpsURLConnection hsuc = (HttpsURLConnection) huc;
            hsuc.setSSLSocketFactory(sc.getSocketFactory());
        }
        huc.setRequestMethod("POST");
        huc.setRequestProperty("Content-Type", "application/xml");
        huc.setDoOutput(true);
        huc.setChunkedStreamingMode(512);
        OutputStream os = huc.getOutputStream();
        
        writeXML(doc, os);
        int resCode = -1;
        Throwable cause = null;
        InputStream is = null;
        try {
            is = huc.getInputStream();
            resCode = huc.getResponseCode();
            if (resCode == 307) {
                String newLoc = huc.getHeaderField("Location");
                LOGGER.info("got a redirection to " + newLoc);
                if (isRedirect) {
                    LOGGER.info("Already got a redirection, treating as error");
                }
                else {
                    return performHTTPRequest(doc, newLoc, true);
                }
            }
        }
        catch(Exception e) {
            cause = e;
        }
        if (resCode / 100 != 2) {
            LOGGER.error("Processing of incoming request failed: " + resCode + "/" + huc.getResponseMessage());
            String errBody = getErrorBody(huc.getErrorStream());
            if (errBody != null) {
                LOGGER.error("Received error message from server: " + errBody);
            }
            throw new Exception("processing of request failed", cause);
        }
        return is;
    }
    
    String getErrorBody(InputStream is) {
        try {
            StringBuilder sb = new StringBuilder();
            byte[] data = new byte[1024];
            int read;
            while ((read = is.read(data)) != -1) {
                sb.append(new String(data, 0, read, "8859_1"));
            }
            return sb.toString();
        }
        catch(Exception e) {
            return null;
        }
    }

    void writeXML(Document doc, OutputStream os) throws TransformerFactoryConfigurationError,
            TransformerConfigurationException, TransformerException, IOException {
        DOMSource domSource = new DOMSource(doc);
        StreamResult result = new StreamResult(os);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        
        os.flush();
    }
}
