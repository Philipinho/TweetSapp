package com.litesoftwares.tweetsapp.service;

import com.litesoftwares.tweetsapp.model.WAMessage;

import com.twilio.Twilio;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import twitter4j.*;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

@Service
public class WebhookService {

    private final Twitter twitter = TwitterFactory.getSingleton();

    @Value("${twilio.number}")
    private String TWILIO_NUMBER;
    @Value("${twilio.account_sid}")
    private String ACCOUNT_SID;
    @Value("${twilio.auth_token}")
    private String AUTH_TOKEN;

    
    public void processIncoming(HttpServletRequest httpRequest) throws Exception{

        String from = httpRequest.getParameter("From");
        String to = httpRequest.getParameter("To");
        String body = httpRequest.getParameter("Body");
        int numMedia = 0;
        numMedia = Integer.parseInt(httpRequest.getParameter("NumMedia"));
        String smsId = httpRequest.getParameter("SmsSid");
        String mediaType = httpRequest.getParameter("MediaContentType0");
        String mediaUrl = httpRequest.getParameter("MediaUrl0");

        String tweetResult = null;

        WAMessage waMessage = new WAMessage(from, to, body, numMedia, smsId, mediaUrl, mediaType);

        switch (waMessage.getbody().toLowerCase()){
            case "*help": tweetResult = getHelp();
                break;
            case "*mentions": tweetResult = getMentions();
                break;
            case "*timeline": tweetResult = getTimeline();
                break;
            case "*delete": tweetResult = deleteLastTweet(getLastTweetId());
                break;
            default:
                if (body.length() > 280){
                    tweetResult = "You have passed the 280 character limit. Your tweet contains " + body.length() + " characters.";
                } else if (waMessage.getNumMedia() == 1){
                    tweetResult = sendTweetWithMedia(waMessage.getbody(), waMessage.getMediaUrl(), mediaType);
                } else {
                    tweetResult = sendTweet(waMessage.getbody());
                }
        }

       whatsappResponse(tweetResult, waMessage.getFrom(), waMessage.getTo());
    }

    private String getHelp(){
        StringBuilder sb =new StringBuilder();
        sb.append("*PS:* Anything you send without a command will be Tweeted automatically." +
                " Text, Photos and Videos are supported.\n\n");
        sb.append("*Available commands ( e.g *mentions):*\n");
        sb.append("**help* -  For help\n");
        sb.append("**mentions* -  This will display your recent Twitter mentions.\n");
        sb.append("**timeline* -  This will display your recent Timeline Tweets.\n");
        sb.append("**delete* - This will delete your must recent Tweet.\n");

        return sb.toString();
    }

    private String getMentions(){
        StringBuilder sb = new StringBuilder();
        Paging paging = new Paging();
        paging.setCount(10);

        try {
            for (Status status : twitter.getMentionsTimeline(paging)){
                sb.append("@").append(status.getUser().getScreenName())
                        .append(": ").append(status.getText())
                        .append("\n\n");
            }

        } catch (TwitterException e){
            sb.append(exceptionMessage(e.getMessage()));
            e.printStackTrace();
        }

        return sb.toString();
    }

    private String getTimeline(){
        StringBuilder sb =new StringBuilder();
        Paging paging = new Paging();
        paging.setCount(15);

        try {
            for (Status status : twitter.getHomeTimeline(paging)){
                sb.append("@").append(status.getUser().getScreenName())
                        .append(": ").append(status.getText())
                        .append("\n\n");
            }

        } catch (TwitterException e){
            sb.append(exceptionMessage(e.getMessage()));
            e.printStackTrace();
        }

        return sb.toString();
    }

    private long getLastTweetId() {
        long recentId;

        try {
            recentId = twitter.getHomeTimeline().get(0).getId();
        } catch (Exception e){
            recentId = 1L;
            e.printStackTrace();
        }

        return recentId;
    }

    private String deleteLastTweet(long tweetId){
        String response = "";
        try {
            twitter.destroyStatus(tweetId);
            response= "Your last tweet was deleted successfully.";
        } catch (TwitterException e) {
            response = exceptionMessage(e.getMessage());
            System.out.println(e.getMessage());
        }
        return response;
    }


    private String sendTweet(String tweetMessage){
        String response = "";
        try {
            Status status = twitter.updateStatus(tweetMessage);

            String tweetLink = "https://twitter.com/" +
                    status.getUser().getScreenName() + "/status/" + status.getId();
            response = "Tweet posted successfully.\n\nLink: " + tweetLink;

        } catch (TwitterException e) {
            response = exceptionMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    private String sendTweetWithMedia(String tweetMessage, String mediaUrl, String mediaType){

        String response = "";

        try {

            StatusUpdate status = new StatusUpdate(tweetMessage);
            UploadedMedia uploadedMedia = null;

            switch (mediaType){
                case "image/jpeg": uploadedMedia = twitter.uploadMedia("photo", getMediaStream(mediaUrl));
                break;
                case "video/mp4":  uploadedMedia = twitter.uploadMediaChunked("video", getMediaStream(mediaUrl));
                break;
            }

            status.setMediaIds(uploadedMedia.getMediaId());
            Status statusUpdate = twitter.updateStatus(status);

            String tweetLink = "https://twitter.com/" +
                    statusUpdate.getUser().getScreenName() + "/status/" + statusUpdate.getId();
            response = "Tweet posted successfully.\nLink: " + tweetLink;

        } catch (Exception e) {
            response = exceptionMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    private void whatsappResponse(String text, String userNumber, String twilioNumber){
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message.creator(new PhoneNumber(userNumber), new PhoneNumber(twilioNumber), text).create();
    }

    public void deleteMedia(String smsId){
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        try {
            com.twilio.rest.api.v2010.account.Message.deleter(smsId).delete();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String exceptionMessage(String message){
        if (message.contains("message - ")) {
            return message.substring(message.lastIndexOf("message - ")
                    , message.lastIndexOf("code - "));
        }
        return message;
    }

    private InputStream getMediaStream(String mediaUrl){
        InputStream inputStream = null;
        try {
            URL url = new URL(mediaUrl);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", "tweetsapp.xyz");
            inputStream = new BufferedInputStream(conn.getInputStream());

        } catch (Exception e){
            e.printStackTrace();
        }
        return inputStream;
    }

}
