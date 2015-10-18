package com.example.how_old;

import org.json.JSONObject;

import com.facepp.error.FaceppParseException;

public interface CallBack {
	public void success(JSONObject json);
	public void error(FaceppParseException e);
}
