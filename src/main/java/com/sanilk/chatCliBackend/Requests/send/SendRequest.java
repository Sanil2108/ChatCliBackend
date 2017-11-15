package com.sanilk.chatCliBackend.Requests.send;

import com.sanilk.chatCliBackend.Requests.MyRequest;

/**
 * Created by root on 15/11/17.
 */
public class SendRequest extends MyRequest{
    //constants
    public final static String TYPE ="SEND";
    public final static String SENDER_NICK="sender_nick";
    public final static String RECEIVER_NICK="receiver_nick";
    public final static String MESSAGE="message";
    public final static String SENDER_PASSWORD="sender_password";

    public String senderNick;
    public String receiverNick;
    public String senderPass;
    public String message;

    public SendRequest(String senderNick, String receiverNick, String senderPass, String message){
        this.senderNick=senderNick;
        this.receiverNick=receiverNick;
        this.senderPass=senderPass;
        this.message=message;
    }
}
