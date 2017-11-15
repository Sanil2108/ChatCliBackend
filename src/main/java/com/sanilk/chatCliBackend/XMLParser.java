package com.sanilk.chatCliBackend;

import com.sanilk.chatCliBackend.Requests.MyRequest;
import com.sanilk.chatCliBackend.Requests.authenticate.AuthenticateRequest;
import com.sanilk.chatCliBackend.Requests.send.SendRequest;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;

/**
 * Created by root on 15/11/17.
 */
public class XMLParser {
    private final static boolean DEBUG=true;

    public static MyRequest parseXML(String xmlText){
        try {
            StringReader reader = new StringReader(xmlText);
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(reader);

            XMLEvent e;
            while(xmlEventReader.hasNext()){
                e=xmlEventReader.nextEvent();

                while(!(e.isStartElement() && e.asStartElement().getName().toString().equals(MyRequest.TYPE))) {
                    e=xmlEventReader.nextEvent();
                }
                e=xmlEventReader.nextEvent();
                if(e.isCharacters()) {
                    String temp=e.asCharacters().getData();
                    switch (e.asCharacters().getData()) {
                        case SendRequest.TYPE:
                            return parseSendRequest(xmlEventReader);
                        case AuthenticateRequest.TYPE:
                            return parseAuthenticateRequest(xmlEventReader);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static SendRequest parseSendRequest(XMLEventReader eventReader) throws Exception{
        XMLEvent e=eventReader.nextEvent();
        String senderNick="";
        String senderPassword="";
        String receiverNick="";
        String message="";
        while(!(e.isStartElement() && e.asStartElement().getName().toString().equals(SendRequest.SENDER_NICK))){
            e=eventReader.nextEvent();
        }
        e=eventReader.nextEvent();
        if(e.isCharacters()) {
            senderNick = e.asCharacters().getData();
            if(DEBUG){
                System.out.println("SenderNick - "+senderNick);
            }
        }
        while(!(e.isStartElement() && e.asStartElement().getName().toString().equals(SendRequest.RECEIVER_NICK))){
            e=eventReader.nextEvent();
        }
        e=eventReader.nextEvent();
        if(e.isCharacters()) {
            receiverNick = e.asCharacters().getData();
            if(DEBUG){
                System.out.println("ReceiverNick - "+receiverNick);
            }
        }
        while(!(e.isStartElement() && e.asStartElement().getName().toString().equals(SendRequest.SENDER_PASSWORD))){
            e=eventReader.nextEvent();
        }
        e=eventReader.nextEvent();
        if(e.isCharacters()) {
            senderPassword = e.asCharacters().getData();
            if(DEBUG){
                System.out.println("SenderPass - "+senderPassword);
            }
        }
        while(!(e.isStartElement() && e.asStartElement().getName().toString().equals(SendRequest.MESSAGE))){
            e=eventReader.nextEvent();
        }
        e=eventReader.nextEvent();
        if(e.isCharacters()) {
            message = e.asCharacters().getData();
            if(DEBUG){
                System.out.println("Message - "+message);
            }
        }

        return new SendRequest(senderNick, receiverNick, senderPassword, message);
    }

    static AuthenticateRequest parseAuthenticateRequest(XMLEventReader eventReader) throws Exception{
        XMLEvent e;
        String senderNick="";
        String senderPassword="";
        e=eventReader.nextEvent();
        while(!(e.isStartElement() && e.asStartElement().getName().toString().equals(AuthenticateRequest.SENDER_NICK))){
            e=eventReader.nextEvent();
        }
        e=eventReader.nextEvent();
        if(e.isCharacters()) {
            senderNick = e.asCharacters().getData();
            if(DEBUG){
                System.out.println("SenderNick - "+senderNick);
            }
        }
        while(!(e.isStartElement() && e.asStartElement().getName().toString().equals(AuthenticateRequest.SENDER_PASSWORD))){
            e=eventReader.nextEvent();
        }
        e=eventReader.nextEvent();
        if(e.isCharacters()) {
            senderPassword = e.asCharacters().getData();
            if(DEBUG){
                System.out.println("SenderPass - "+senderPassword);
            }
        }

        return new AuthenticateRequest(senderNick, senderPassword);
    }
}
