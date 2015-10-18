package com.example.how_old;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import com.facepp.error.FaceppParseException;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener {
	private static final int PHOTO_REQUEST_GALLERY = 0X11;
	private Button button_getImage, button_detect;
	private TextView tv_tip;
	private ImageView imageView;
	private FrameLayout frameLayout;
	private String CurrentImageURL;
	private Bitmap imagePhoto;
	public static final int MSG_SUCCESS = 0x12;
	public static final int MSG_ERROR = 0x13;
	private float x;
	private float y;
	private float height;
	private float width;
	private String gender;
	private int age;
	private String race;
	private float smile;
	private Paint paint = new Paint();
	private Canvas canvas;
	private Bitmap bitmap;
	private TextView tv_age;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();

	}

	public void init() {
		button_detect = (Button) findViewById(R.id.button_detect);
		button_getImage = (Button) findViewById(R.id.button_getImage);
		tv_tip = (TextView) findViewById(R.id.tv_tip);
		imageView = (ImageView) findViewById(R.id.image);
		frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
		button_detect.setOnClickListener(this);
		button_getImage.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_getImage:
			// 激活系统图库，选取一张图片
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, PHOTO_REQUEST_GALLERY);

			break;
		case R.id.button_detect:
			bitmap = Bitmap.createBitmap(imagePhoto.getWidth(),
					imagePhoto.getHeight(), imagePhoto.getConfig().ARGB_8888);
			canvas = new Canvas(bitmap);
			frameLayout.setVisibility(View.VISIBLE);
			FaceppDetect.dectet(imagePhoto, new CallBack() {

				@Override
				public void success(JSONObject json) {
					Message msg = Message.obtain();
					msg.what = MSG_SUCCESS;
					msg.obj = json;
					handleImage.sendMessage(msg);
				}

				@Override
				public void error(FaceppParseException e) {
					Message msg = Message.obtain();
					msg.what = MSG_ERROR;
					msg.obj = e.getErrorMessage();
					handleImage.sendMessage(msg);
				}
			});
			break;
		default:
			break;
		}
	}

	Handler handleImage = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MainActivity.MSG_SUCCESS:
				frameLayout.setVisibility(View.GONE);
				prepJsonBitmap(msg);
				drawFaceRectangle();
				Bitmap ageBitmap=buildAgeBitmap(age,"male".equals(gender));
				
				//根据图片比例进行年龄框的缩放
				if(bitmap.getWidth()<imagePhoto.getWidth()&&bitmap.getHeight()<imagePhoto.getHeight()){
					double ratio=Math.max(bitmap.getWidth()*1.0d/imagePhoto.getWidth(), bitmap.getHeight()*1.0d/imagePhoto.getHeight());
					
					ageBitmap=Bitmap.createScaledBitmap(ageBitmap, (int)(ageBitmap.getWidth()*ratio), (int)(ageBitmap.getHeight()*ratio), false);
					canvas.drawBitmap(ageBitmap, x-ageBitmap.getWidth()/2, y-height/2-ageBitmap.getHeight(), paint);

				}
				canvas.drawBitmap(ageBitmap, x-ageBitmap.getWidth()/2, y-height/2-ageBitmap.getHeight(), paint);
				
				imageView.setImageBitmap(bitmap);
				break;
			case MainActivity.MSG_ERROR:
				frameLayout.setVisibility(View.GONE);
				Toast.makeText(MainActivity.this, "出错啦", Toast.LENGTH_SHORT)
						.show();
				break;

			default:
				break;
			}

		}
		/**
		 * 在人脸框上绘制年龄框和性别图案
		 * @param age
		 * @param isMale
		 * @return bitmap
		 */
		private Bitmap buildAgeBitmap(int age, boolean isMale) {
			tv_age=(TextView) frameLayout.findViewById(R.id.tv_age);
			tv_age.setText(MainActivity.this.age+"");
			if(isMale){
				tv_age.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.male), null, null, null);
			}else{
				tv_age.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.female), null, null, null);
			}
			tv_age.setDrawingCacheEnabled(true);
			Bitmap bitmap=Bitmap.createBitmap(tv_age.getDrawingCache());
			return bitmap;
		}

	};

	private void prepJsonBitmap(Message msg) {
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
				x = x / 100 * imagePhoto.getWidth();
				y = y / 100 * imagePhoto.getHeight();

				// 获取脸部框的宽高
				height = (float) position.getDouble("height");
				width = (float) position.getDouble("width");
				width = width / 100 * imagePhoto.getWidth();
				height = height / 100 * imagePhoto.getHeight();
				JSONObject attribute = face.getJSONObject("attribute");
				// 获取性别
				gender = attribute.getJSONObject("gender").getString("value");
				// 获取年龄
				age = attribute.getJSONObject("age").getInt("value");
				// 获取种族
				race = attribute.getJSONObject("race").getString("value");
				// 微笑指数
				smile = (float) attribute.getJSONObject("smiling").getDouble(
						"value");
				Log.e("gender", gender);
				Log.e("age", age + "");
			}

			tv_tip.setText("total" + faces.length() + "faces");
			Log.e("face number", faces.length() + "");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// 绘制脸部的框
	private void drawFaceRectangle() {
		paint.setColor(Color.RED);
		paint.setStrokeWidth(3);
		canvas.drawBitmap(imagePhoto, 0, 0, paint);
		//绘制人脸框
		canvas.drawLine(x - width / 2, y - height / 2, x - width / 2, y
				+ height / 2, paint);
		canvas.drawLine(x - width / 2, y - height / 2, x + width / 2, y
				- height / 2, paint);
		canvas.drawLine(x - width / 2, y + height / 2, x + width / 2, y
				+ height / 2, paint);
		canvas.drawLine(x + width / 2, y - height / 2, x + width / 2, y
				+ height / 2, paint);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PHOTO_REQUEST_GALLERY) {
			if (data != null) {
				// 获取图片的路径
				Uri uri = data.getData();
				Cursor cursor = getContentResolver().query(uri, null, null,
						null, null);
				cursor.moveToFirst();

				int idx = cursor
						.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
				CurrentImageURL = cursor.getString(idx);
				cursor.close();
				resizePhoto();
				imageView.setImageBitmap(imagePhoto);
			}
		}
	}

	// 压缩图片
	public void resizePhoto() {

		BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inJustDecodeBounds = true;
//		BitmapFactory.decodeFile(CurrentImageURL, options);
//		double ratio = Math.max(options.outWidth * 1.0d / 1024f,
//				options.outHeight * 1.0d / 1024f);
//		// 向上取整
//		options.inSampleSize = (int) Math.ceil(ratio);
		options.inJustDecodeBounds = false;
		imagePhoto = BitmapFactory.decodeFile(CurrentImageURL, options);

	}

	public FrameLayout getFrameLayout() {
		return frameLayout;
	}

	public TextView gettv_tip() {
		return tv_tip;
	}

	public Bitmap getBitmap() {
		return imagePhoto;
	}

	public ImageView getImageView() {
		return imageView;
	}
}
