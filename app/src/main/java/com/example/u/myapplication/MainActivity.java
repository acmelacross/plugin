package com.example.u.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.duanqu.qupai.engine.session.MovieExportOptions;
import com.duanqu.qupai.engine.session.ProjectOptions;
import com.duanqu.qupai.engine.session.ThumbnailExportOptions;
import com.duanqu.qupai.engine.session.UISettings;
import com.duanqu.qupai.engine.session.VideoSessionCreateInfo;
import com.duanqu.qupai.sdk.android.QupaiManager;
import com.duanqu.qupai.sdk.android.QupaiService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends CheckPermissionsActivity {
    QupaiService qupaiService;
    String filePath="";
    String filePName="psjgngsasd.mp4";
    String buketName="jjtcsfb133";
    String imgPath="";
    public static final int ACTIVITY_RESULT_NUM_UPLOAD_VEDIO = 13;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (String str : new String[]{"gnustl_shared", "qupai-media-thirdparty", "qupai-media-jni"}) {
            System.loadLibrary(str);
        }
        qupaiService = QupaiManager.getQupaiService(MainActivity.this);

       initParam();
       // playVideo();
        initIntentParam();
    }

    private void initParam() {

        //UI设置参数
        UISettings _UISettings = new UISettings() {
            @Override
            public boolean hasEditor() {
                return false;//是否需要编辑功能
            }

            @Override
            public boolean hasImporter() {
                return false;//是否需要导入功能
            }

            @Override
            public boolean hasGuide() {
                return false;//是否启动引导功能，建议用户第一次使用时设置为true
            }

            @Override
            public boolean hasSkinBeautifer() {
                return true;//是否显示美颜图标
            }
        };
//压缩参数
        MovieExportOptions movie_options = new MovieExportOptions.Builder()
                .setVideoBitrate(2)
                .configureMuxer("movflags", "+faststart")
                .build();
//输出视频的参数
        ProjectOptions projectOptions = new ProjectOptions.Builder() //输出视频宽高目前只能设置1：1的宽高，建议设置480*480.
                .setVideoSize(480, 480) //帧率 .setVideoFrameRate(30)
//时长区间
                .setDurationRange(2,8) .get();
//缩略图参数,可设置取得缩略图的数量，默认10张
        ThumbnailExportOptions thumbnailExportOptions =new ThumbnailExportOptions.Builder().setCount(10).get();
        VideoSessionCreateInfo info =new VideoSessionCreateInfo.Builder()
//水印地址"assets://Qupai/watermark/q，如upai-logo.png"
                .setWaterMarkPath("assets://Qupai/watermark/qupai-logo.png")
//水印的位置
                .setWaterMarkPosition(1)
//摄像头方向,可配置前置或后置摄像头
                .setCameraFacing(Camera.CameraInfo.CAMERA_FACING_BACK)
//美颜百分比,设置之后内部会记住，多次设置无效
                .setBeautyProgress(80)
//默认是否开启
                .setBeautySkinOn(true)
                .setMovieExportOptions(movie_options)
                .setThumbnailExportOptions(thumbnailExportOptions)
                .build();
//初始化，建议在application里面做初始化，这里做是为了方便开发者认识参数的意义
        qupaiService.initRecord(info,projectOptions,_UISettings);


        /** * 调用拍摄 Activity *
         @param activity当前的activity *
         @param requestCode 为避免重复定义requestCode，请开发者自行定义requestCode *
         @param isFirstRecord 是否是第一次调用拍摄 */
        qupaiService.showRecordPage(MainActivity.this,100, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(requestCode+"aaaaaaaaaaaaaaaaa" +resultCode +"---"+data);
       // System.out.println(" data.getStringExtra() " +  data.getStringExtra("dat"));
        if (data==null){
            setResult(ACTIVITY_RESULT_NUM_UPLOAD_VEDIO);
            finish();
        }
        try {
            RecordResult result =new RecordResult(data);
            //得到视频地址，和缩略图地址的数组，返回十张缩略图
            String videoFile = result.getPath();
            String [] thum = result.getThumbnail();
            System.out.println("视频路径:" + videoFile + "图片路径:" + thum[0] + "Duration" +result.getDuration());
            filePath =  videoFile;
            imgPath =  thum[0];
            CopyFileUtil.copyFile(imgPath,Environment.getExternalStorageDirectory().toString() + "/Aichuxing/Bus/temp/videotemp/vImg.jpg",true);
//            Intent intent = getPackageManager().getLaunchIntentForPackage("com.tutuchuxing");
//        // 这里如果intent为空，就说名没有安装要跳转的应用嘛
//                // 这里跟Activity传递参数一样的嘛，不要担心怎么传递参数，还有接收参数也是跟Activity和Activity传参数一样
//             startActivity(intent);
   // setResult(100);

            initOSS();
        }catch (Exception e){
e.printStackTrace();
        }

    }

    private void initOSS(){
        String endpoint = "http://oss-cn-shanghai.aliyuncs.com";
// 明文设置secret的方式建议只在测试时使用，更多鉴权模式请参考后面的访问控制章节
        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider("LTAISMJwL6Ztwzwn", "Cby6lyA7d4GAVyyhqgGPoAvLRiiGTT");
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
        //System.out.println("filePath "+ filePath);
        // 异步上传时可以设置进度回调
         put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
              //      Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");
               // download(oss);
                Intent intent = new Intent();
                intent.putExtra("imgPath",imgPath);
                intent.putExtra("videoUrl","http://jjtcsfb133.oss-cn-shanghai.aliyuncs.com/"+filePName);
                System.out.println(imgPath+"传递之前   "+filePName);
                setResult(ACTIVITY_RESULT_NUM_UPLOAD_VEDIO,intent);
                finish();
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

                setResult(ACTIVITY_RESULT_NUM_UPLOAD_VEDIO);
                finish();
            }
        });
    }

    private void initIntentParam(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String name = bundle.getString("name");
            String birthday = bundle.getString("birthday");
            if (name != null || birthday != null) {
                Toast.makeText(getApplicationContext(), "name:" + name + "    birthday:" + birthday, Toast.LENGTH_SHORT).show();
            }
            filePName = name;
            //filePName = name+"_"+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".mp4";

            //new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        }
    }





    private void download(OSS oss){
        GetObjectRequest get = new GetObjectRequest(buketName, filePName);
        OSSAsyncTask task = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                // 请求成功
                InputStream inputStream = result.getObjectContent();
                OutputStream outputStream = null;
                try {
                    ;

//                    File saveFile=new File( Environment.getExternalStorageDirectory()+"/zhzhg.mp4");
//                    if (!saveFile.exists()){
//                        saveFile.createNewFile();
//                    }
                    outputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/zhzhg.mp4");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                byte[] buffer = new byte[2048];
                int len;
                int bytesWritten = 0;

                try {
                    while ((len = inputStream.read(buffer)) != -1) {
                        // 处理下载的数据
//                        outputStream.write(buffer, bytesWritten, len);
//                        bytesWritten += len;
                        outputStream.write(buffer);
                        System.out.println(bytesWritten+"   大小  ");
                    }
                    inputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
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
            }
        });
    }

private void playVideo(){
//    Intent intent = new Intent(Intent.ACTION_VIEW);
//    intent.setDataAndType(Uri.parse(Environment.getExternalStorageDirectory() + "/14.mp4"), "video/mp4");
//    startActivity(intent);

//    Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()+"/Test_Movie.m4v");
//    VideoView videoView = (VideoView)this.findViewById(R.id.video_view);
//    videoView.setMediaController(new MediaController(this));
//    videoView.setVideoURI(uri);
//    videoView.start();
//    videoView.requestFocus();
}

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // System.out.println("aaaaaaaaaaaaaa");
    }
}
