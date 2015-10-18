package com.example.how_old;

import java.io.ByteArrayOutputStream;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.widget.ImageView;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

public class FaceppDetect {
	public static void dectet(final Bitmap bitmap, final CallBack callBack) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					HttpRequests requests = new HttpRequests(Constant.KEY,
							Constant.SECRET,true,false);
					Bitmap imgSmall = Bitmap.createBitmap(bitmap, 0, 0,
							bitmap.getWidth(), bitmap.getHeight());
				
					
					
//					float scale = Math.min(1, Math.min(600f / bitmap.getWidth(), 600f / bitmap.getHeight()));
//		    		Matrix matrix = new Matrix();
//		    		matrix.postScale(scale, scale);
//
//		    		Bitmap imgSmall = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
					
		    		Log.e("photoSize", imgSmall.getWidth()+"     "+imgSmall.getHeight()+"");
					
		    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    		imgSmall.compress(Bitmap.CompressFormat.JPEG, 80, bos);
					
					
					
					byte[] bytes = bos.toByteArray();
					PostParameters params = new PostParameters();
					params.setImg(bytes);
//					params.setUrl("http://www.faceplusplus.com.cn/wp-content/themes/faceplusplus/assets/img/demo/1.jpg?v=4");
					JSONObject json = requests.detectionDetect(params);
					
					if(callBack!=null){
						Log.e("TAG", json+"");
						callBack.success(json);
					}
				} catch (FaceppParseException e) {
					e.printStackTrace();
					if(callBack!=null){
						callBack.error(e);
					}
				}
			}
		}).start();
	}

}
