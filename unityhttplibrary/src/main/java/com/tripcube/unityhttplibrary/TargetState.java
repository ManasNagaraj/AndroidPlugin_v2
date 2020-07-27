package com.tripcube.unityhttplibrary;

import org.json.JSONObject;
import org.json.JSONException;

class TargetState {
    private String 	m_status;
    private String 	m_targetId;
    private Boolean m_activeFlag;
    private String 	m_name;
    private Float  	m_width;
    private Integer m_trackingRating;
    private String 	m_recoRating;

    public boolean hasState = false;

    public String getStatus() {
        return m_status;
    }

    public void setStatus(String status) {
        m_status = status;
    }

    public String getTargetId() {
        return m_targetId;
    }

    public void setTargetId(String tid) {
        m_targetId = tid;
    }

    public Boolean getActiveFlag() {
        return m_activeFlag;
    }

    public void setActiveFlag(Boolean active) {
        m_activeFlag = active;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public Float getWidth() {
        return m_width;
    }

    public void setWidth(Float width) {
        m_width = width;
    }

    public Integer getTrackingRating() {
        return m_trackingRating;
    }

    public void setTrackingRating(Integer rating) {
        m_trackingRating = rating;
    }

    public String getRecoRating() {
        return m_recoRating;
    }

    public void setRecoRating(String rating) {
        m_recoRating = rating;
    }

    public static TargetState createFromJSON(JSONObject jobj) {
        if (jobj == null) {
            throw new IllegalArgumentException("Failed to create TargetState from JSON object: JSON object may not be null!");
        }

        TargetState result = new TargetState();
        result.hasState = true;

        try {
            result.setStatus( jobj.getString("status") );

            JSONObject targetRecord = jobj.getJSONObject("target_record");

            result.setTargetId( targetRecord.getString("target_id") );

            if( targetRecord.has("active_flag" ) )// not mandatory
                result.setActiveFlag( targetRecord.getBoolean("active_flag"));

            result.setName( targetRecord.getString("name"));
            result.setWidth( (float) targetRecord.getDouble("width") );
            result.setTrackingRating( targetRecord.getInt("tracking_rating") );

            if( targetRecord.has("reco_rating") )// not mandatory
                result.setRecoRating( targetRecord.getString("reco_rating") );

            return result;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
