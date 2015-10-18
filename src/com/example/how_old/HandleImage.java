package com.example.how_old;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class HandleImage extends Handler {
	private MainActivity mainActivity;
	private Bitmap bitmap;
	private Canvas canvas = new Canvas(bitmap);
	private Paint paint = new Paint();
	private float x, y;
	private float height, width, smile;
	private int age;
	private String gender, race;

	public HandleImage(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
		bitmap=Bitmap.createBitmap(mainActivity.getBitmap().getWidth(), mainActivity.getBitmap().getHeight(), mainActivity.getBitmap().getConfig());
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		case MainActivity.MSG_SUCCESS:
			mainActivity.getFrameLayout().setVisibility(View.GONE);

			prepJSON(msg);
			drawFaceRectangle();
			break;
		case MainActivity.MSG_ERROR:
			mainActivity.getFrameLayout().setVisibility(View.GONE);
			Toast.makeText(mainActivity, "出错啦", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
	}

	// 绘制脸部的框
	private void drawFaceRectangle() {
		paint.setColor(Color.RED);
		canvas.drawLine(x-width/2, y-height/2, x-width/2, y+height/2, paint);
		canvas.drawLine(x-width/2, y-height/2, x+width/2, y-height/2, paint);
		canvas.drawLine(x-width/2, y+height/2, x+width/2, y+height/2, paint);
		canvas.drawLine(x+width/2, y-height/2, x+width/2, y+height/2, paint);
		mainActivity.getImageView().setImageBitmap(bitmap);
	}

	private void prepJSON(Message msg) {
		JSONObject faceoObject = (JSONObject) msg.obj;
		try {
			JSONArray faces = faceoObject.getJSONArray("face");
			for (int i = 0; i < faces.length(); i++) {
				JSONObject face = faces.getJSONObject(i);
				JSONObject position = face.getJSONObject("position");
				// 获取脸部中心比例
				x = (float) position.getJSONObject("center").getDouble("x");
				y = (float) position.getJSONObject("center").getDouble("y");

				// 根据照片实际大小计算脸部中心坐标
				x = x / 100 * mainActivity.getBitmap().getWidth();
				y = y / 100 * mainActivity.getBitmap().getHeight();

				// 获取脸部框的宽高
				height = (float) position.getDouble("height");
				width = (float) position.getDouble("width");
				width = width / 100 * mainActivity.getBitmap().getWidth();
				height = height / 100 * mainActivity.getBitmap().getHeight();
				JSONObject attribute = face.getJSONObject("attribute");
				// 获取性别
				gender = attribute.getJSONObject("gender").getString("value");
				// 获取年龄
				age = attribute.getJSONObject("age").getInt("value");
				// 获取种族
				race = attribute.getJSONObject("race").getString("value");
				// 微笑指数
				smile = (float) attribute.getJSONObject("smiling").getDouble("value");
				Log.e("gender", gender);
				Log.e("age", age + "");
			}

			mainActivity.gettv_tip()
					.setText("total" + faces.length() + "faces");
			Log.e("face number", faces.length() + "");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
