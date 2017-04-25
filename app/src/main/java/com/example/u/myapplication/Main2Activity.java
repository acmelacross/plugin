package com.example.u.myapplication;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Main2Activity extends Activity  implements View.OnClickListener{

    String filePath= Environment.getExternalStorageDirectory().toString() + "/a.mp4";
    String filePName="bacdef";
    String buketName="jjtcsfb133";
  // String endpoint = "http://jjtcsfb133.oss-cn-shanghai.aliyuncs.com/asda?OSSAccessKeyId=LTAISMJwL6Ztwzwn&Expires=1490795403&Signature=WtUPz946Pj%2FrTtx3lUxXfyqWeGw%3D";
    private int progress = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initOSS();
    }

    @Override
    public void onClick(View v) {

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (progress < 100) {
                progress += 1;
                handler.sendMessageDelayed(Message.obtain(), 20);
            }
            super.handleMessage(msg);
        }
    };

    private void initOSS(){
// 明文设置secret的方式建议只在测试时使用，更多鉴权模式请参考后面的访问控制章节
        String endpoint = "http://oss-cn-shanghai.aliyuncs.com";
        OSSCustomSignerCredentialProvider    credentialProvider = new OSSCustomSignerCredentialProvider() {
            @Override
            public String signContent(String content) {
                // 您需要在这里依照OSS规定的签名算法，实现加签一串字符内容，并把得到的签名传拼接上AccessKeyId后返回
                // 一般实现是，将字符内容post到您的业务服务器，然后返回签名
                // 如果因为某种原因加签失败，描述error信息后，返回nil
                // 以下是用本地算法进行的演示
                //return "OSS " + AccessKeyId + ":" + base64(hlmac-sha1(AccessKeySecret, content));
                //return "OSS AccessKeyId=LTAISMJwL6Ztwzwn&Expires=1490794634&Signature=Rqem1zeAyKTXZ1Oj%2Blo8kjzwjQc%3D";
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("_userId","9a5289e0-b515-11e6-9fac-b9baaea4901b");
                map.put("_userKey","d5a94130-234e-11e7-b216-dd24a8cd1bd7_android");
                map.put("content",content);

//                String jsonStr = new Gson().toJson(map);
//                Log.d("jsonStr    ","jsonStr  "+jsonStr);
//                OkHttpClient client = new OkHttpClient();
//                MediaType MEDIA_TYPE_TEXT = MediaType.parse("application/json");
//                String postBody = jsonStr;

//oss/signature/sign
                String str = HttpUtils.submitPostData("http://bus.tutuchuxing.com/oss/signature/sign",map,"utf-8");
              try{
                  Log.d("jsonStrfanhui    ","jsonStr  "+str);
                  JSONObject jsonObject = new JSONObject(str);
                  content=((JSONObject)jsonObject.get("data")).get("content").toString();
              }catch (Exception e){
                  e.printStackTrace();
                  Log.d("content fan hui  ","Exception  ");
              }
                return "OSS LTAISMJwL6Ztwzwn:"+content;
//                Request request = new Request.Builder()
//                        .url("http://bus.tutuchuxing.com/oss/signature/put/"+filePName)
//                        .post(RequestBody.create(MEDIA_TYPE_TEXT, postBody))
//                        .build();
//                Response response= null;
//try{
//    response = client.newCall(request).execute();
//    if (!response.isSuccessful()) {
//        Log.d("fuwiqicuowu ","OSS LTAISMJwL6Ztwzwn:"+response);
//        throw new IOException("服务器端错误: " + response);
//    }
//   // System.out.println(response.body().string());
//  //  Log.d("content fan hui  ","OSS LTAISMJwL6Ztwzwn:"+response.body().string());
//    return "OSS LTAISMJwL6Ztwzwn:"+response.body().string();
//}catch (Exception e){
//    e.printStackTrace();
//    Log.d("content fan hui  ","Exception  ");
//    return "";
//}




//                System.out.println("content   " +content);
//                Log.d("content", "content  "+ content);
//                System.out.println("content--   " +content);
//                String ss = "";

//              return "OSS LTAISMJwL6Ztwzwn:"+ss;
                //OSSAccessKeyId=LTAISMJwL6Ztwzwn&Expires=1492450973&Signature=11mACHc6crl0lGRZHtCioW%2FCqwc%3D"
            }
        };
        //"http://jjtcsfb133.oss-cn-shanghai.aliyuncs.com/bacdef?OSSAccessKeyId=LTAISMJwL6Ztwzwn&Expires=1492513255&Signature=lZtrlQrEIoIFGsTtN7oBVK4VX78%3D"
      //  credentialProvider.signContent("OSS AccessKeyId=LTAISMJwL6Ztwzwn&Expires=1490795403&Signature=WtUPz946Pj%2FrTtx3lUxXfyqWeGw%3D");
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSS oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider, conf);
        uploadfile(oss);
    }
    private void uploadfile(final OSS oss){
        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(buketName, filePName, filePath);
        System.out.println("filePath "+ filePath);
        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
            //    progress = ((int)currentSize/(int)totalSize)*100;
             //   Log.d("PutObject", progress+"currentSize: " + currentSize + " totalSize: " + totalSize);
            //    zyDownloading.setProgress(progress);
                //      Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });
        //   zyDownloading.startDownload();
        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");
                // download(oss);
                System.out.println("传递之前   "+filePName);
               // finish();
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
              //  finish();
            }
        });

    }
}
