package com.litesoftwares.tweetsapp.model;

public class WAMessage {
    private String from;
    private String to;
    private String body;
    private int numMedia;
    private String smsId;
    private String mediaUrl;
    private String mediaType;
   // private List<Media> mediaList;

    public WAMessage(){

    }

    public WAMessage(String from, String to, String body, int numMedia) {
        this.from = from;
        this.to = to;
        this.body = body;
        this.numMedia = numMedia;
    }

    public WAMessage(String from, String to, String body, int numMedia, String smsId) {
        this.from = from;
        this.to = to;
        this.body = body;
        this.numMedia = numMedia;
        this.smsId = smsId;
    }

    public WAMessage(String from, String to, String body, int numMedia, String smsId, String mediaUrl, String mediaType) {
        this.from = from;
        this.to = to;
        this.body = body;
        this.numMedia = numMedia;
        this.smsId = smsId;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getbody() {
        return body;
    }

    public void setbody(String body) {
        body = body;
    }

    public int getNumMedia() {
        return numMedia;
    }

    public void setNumMedia(int numMedia) {
        this.numMedia = numMedia;
    }

    public String getSmsId() {
        return smsId;
    }

    public void setSmsId(String smsId) {
        this.smsId = smsId;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String toString() {
        return "WAMessage{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", body='" + body + '\'' +
                ", numMedia=" + numMedia +
                ", smsId='" + smsId + '\'' +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", mediaType='" + mediaType + '\'' +
                '}';
    }

}
