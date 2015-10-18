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
			Toast.makeText(mainActivity, "������", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
	}

	// ���������Ŀ�
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
				// ��ȡ�������ı���
				x = (float) position.getJSONObject("center").getDouble("x");
				y = (float) position.getJSONObject("center").getDouble("y");

				// ������Ƭʵ�ʴ�С����������������
				x = x / 100 * mainActivity.getBitmap().getWidth();
				y = y / 100 * mainActivity.getBitmap().getHeight();

				// ��ȡ������Ŀ��
				height = (float) position.getDouble("height");
				width = (float) position.getDouble("width");
				width = width / 100 * mainActivity.getBitmap().getWidth();
				height = height / 100 * mainActivity.getBitmap().getHeight();
				JSONObject attribute = face.getJSONObject("attribute");
				// ��ȡ�Ա�
				gender = attribute.getJSONObject("gender").getString("value");
				// ��ȡ����
				age = attribute.getJSONObject("age").getInt("value");
				// ��ȡ����
				race = attribute.getJSONObject("race").getString("value");
				// ΢Цָ��
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
