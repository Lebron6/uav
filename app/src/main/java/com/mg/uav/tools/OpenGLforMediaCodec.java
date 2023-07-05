package com.mg.uav.tools;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class OpenGLforMediaCodec {

    private static final String TAG = "OpenGLforMediaCodec";
    private EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;
    private EGLContext eglCtx = EGL14.EGL_NO_CONTEXT;
    private EGLDisplay eglDis = EGL14.EGL_NO_DISPLAY;
    private EGLConfig eglConfig = null;



    /*片段着色器脚本 fragment shader
     *gl_FragColor:
     * Fragment Shader的输出，它是一个四维变量（或称为 vec4）。
     * 表示在经过着色器代码处理后，正在呈现的像素的 R、G、B、A 值。
     * */
    private String fragmentShader =
            "precision mediump float;\n" +
                    "varying vec2 coordinate;\n" +
                    "uniform sampler2D tex_y;\n" +
                    "uniform sampler2D tex_u;\n" +
                    "uniform sampler2D tex_v;\n" +
                    "void main()\n" +
                    "{\n" +
                    "    vec3 yuv;\n" +
                    "    vec3 mrgb;    \n" +
                    "    yuv.x = texture2D(tex_y, coordinate).r;        \n" +//得到yuv数据坐标矩阵
                    "    yuv.y = texture2D(tex_u, coordinate).r - 0.5; \n" +
                    "    yuv.z = texture2D(tex_v, coordinate).r - 0.5; \n" +
                    "    mrgb = mat3( 1,       1,         1,\n" +
                    "                0,       -0.39173,  2.017,\n" +
                    "                1.5958,  -0.81290,  0) * yuv;  " +
                    "    gl_FragColor = vec4(mrgb.rgb, 1.0);\n" +
                    "}\n";
    /*顶点着色器脚本 vertex shader
     * gl_Position:原始的顶点数据在Vertex Shader中经过平移、旋转、缩放等数学变换后，
     * 生成新的顶点位置（一个四维 (vec4) 变量，包含顶点的 x、y、z 和 w 值）。
     * 新的顶点位置通过在Vertex Shader中写入gl_Position传递到渲染管线的后继阶段继续处理。
     * */
    private String vertexShader =
            "attribute vec4 position;\n" +
                    "attribute  vec2 textureCoordinate;\n" +//要获取的纹理坐标
                    "varying  vec2 coordinate;\n" + //传递给fragm shader的纹理坐标，会自动插值
                    "void main() { \n" +
                    "    gl_Position = position; \n" +
                    "    coordinate = textureCoordinate;\n" +
                    "}\n";

    private SurfaceHolder surfaceHolder;

    private  int programId = 3;
    public OpenGLforMediaCodec(){
    }

    //初始化EGL程序
    public void initShader(){
        programId = ShaderUtils.createProgram(vertexShader, fragmentShader);
        vertexBuffer = floatBufferUtil(vertexData);
        textureVertexBuffer = floatBufferUtil(textureVertexData);
    }
    public void initEGL(SurfaceHolder mSurfaceHolder) {
        surfaceHolder = mSurfaceHolder;
        if(eglDis == EGL14.EGL_NO_DISPLAY) {
            eglDis = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (eglDis == EGL14.EGL_NO_DISPLAY){
                Log.e(TAG,"eglGetDisplay error :" + EGL14.eglGetError());
            }
        }
        boolean success;
        int[] majorVersion = new int[1];
        int[] minorVersion = new int[1];
        success = EGL14.eglInitialize(eglDis, majorVersion, 0, minorVersion, 0);
        if(!success){
            Log.e(TAG, "Unable to initialize EGL");
        }
        int confAttr[] = {
//                EGL14.EGL_BUFFER_SIZE,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8, //EGL_RED_SIZE，EGL_GREEN_SIZE，EGL_BLUE_SIZE 表示我们最终渲染的图形是RGB格式
//                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_NONE   //常量结束符
        };//获取framebuffer格式和能力
        EGLConfig[] configs = new EGLConfig[1];
        int [] numConfigs = new int[1];//存放获取的config数据
        success = EGL14.eglChooseConfig(eglDis, confAttr, 0, configs, 0, configs.length, numConfigs, 0);
        if (!success){
            Log.e(TAG,"some config is wrong :" + EGL14.eglGetError());
        }
        eglConfig = configs[0];
        //创建OpenGL上下文
        int ctxAttr[] = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,// openGL 2.0
                EGL14.EGL_NONE
        };
        eglCtx = EGL14.eglCreateContext(eglDis, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttr, 0);
        if(eglCtx == EGL14.EGL_NO_CONTEXT){
            Log.e(TAG,"context failed:" + EGL14.eglGetError());
        }
        int[] surfaceAttr = {
                EGL14.EGL_NONE
        };
        //OpenGL显示层和本地窗口ANativeWindow的绑定
        if(surfaceHolder.getSurface().isValid()) {//fix bug,create window surface will fail when surface is invalid
            eglSurface = EGL14.eglCreateWindowSurface(eglDis, configs[0], surfaceHolder, surfaceAttr, 0);
        }
        if(eglSurface ==  EGL14.EGL_NO_SURFACE) {
            switch (EGL14.eglGetError()) {
                case EGL14.EGL_BAD_ALLOC:
                    // Not enough resources available. Handle and recover
                    Log.e(TAG, "Not enough resources available");
                    break;
                case EGL14.EGL_BAD_CONFIG:
                    // Verify that provided EGLConfig is valid
                    Log.e(TAG, "provided EGLConfig is invalid");
                    break;
                case EGL14.EGL_BAD_PARAMETER:
                    // Verify that the EGL_WIDTH and EGL_HEIGHT are
                    // non-negative values
                    Log.e(TAG, "provided EGL_WIDTH and EGL_HEIGHT is invalid");
                    break;
                case EGL14.EGL_BAD_MATCH:
                    // Check window and EGLConfig attributes to determine
                    // compatibility and pbuffer-texture parameters
                    Log.e(TAG, "Check window and EGLConfig attributes");
                    break;
            }
        }else
            Log.e(TAG,"create native window success eglSurface : " + eglSurface);
        EGL14.eglMakeCurrent(eglDis, eglSurface, eglSurface, eglCtx);
        EGL14.eglBindAPI(EGL14.EGL_OPENGL_ES_API);

    }


    private final float[] vertexData = { //渲染顶点坐标数据
            -1.0f,  1.0f,    //左上角
            -1.0f,  -1.0f,   //左下角
            1.0f,   1.0f,   //右下角
            1.0f,   -1.0f     //右上角
    };
    final float[] textureVertexData = { //渲染纹理坐标数据
            0.0f,   0.0f,
            0.0f,   1.0f,
            1.0f,   0.0f,
            1.0f,   1.0f,
    };

    private float[] mSTMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];

    private int aPositionHandle;
    private int aTextureCoordHandle;

    private int tex_y;
    private int tex_u;
    private int tex_v;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureVertexBuffer;

    /*用于将YUV数据写入文件调试*/
    private void createFileWithByte(byte[] bytes) {
        /**
         * 创建File对象，其中包含文件所在的目录以及文件的命名
         */
        File file = new File(Environment.getExternalStorageDirectory(),
                "byte_to_file.yuv");
        // 创建FileOutputStream对象
        FileOutputStream outputStream = null;
        // 创建BufferedOutputStream对象
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // 如果文件存在则删除
            if (file.exists()) {
                file.delete();
            }
            // 在文件系统中根据路径创建一个新的空文件
            file.createNewFile();
            // 获取FileOutputStream对象
            outputStream = new FileOutputStream(file);
            // 获取BufferedOutputStream对象
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            // 往文件所在的缓冲输出流中写byte数据
            bufferedOutputStream.write(bytes);
            // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
            bufferedOutputStream.flush();
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        } finally {
            // 关闭创建的流对象
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
    public void render(byte[] bytes, int screenWidth, int screenHeight, int decodeWidth, int decodeHeight, int pixWidth, int pixHeight){

//        createFileWithByte(bytes);//仅用于调试，无其它功能，暂时保留

        GLES20.glViewport(0, 0, screenWidth, screenHeight);

        /*
         * 获取着色器的属性引用id法(传入的字符串时着色器脚本中的属性名)
         * */
        if(programId == 0){
            Log.e(TAG, "create shaderUtils failed");
            return;
        }
        aPositionHandle = GLES20.glGetAttribLocation(programId, "position");//检索着色器程序的属性位置
        aTextureCoordHandle = GLES20.glGetAttribLocation(programId, "textureCoordinate");//检索着色器程序的统一位置

        tex_y = GLES20.glGetUniformLocation(programId, "tex_y");
        tex_u = GLES20.glGetUniformLocation(programId, "tex_u");
        tex_v = GLES20.glGetUniformLocation(programId, "tex_v");
        GLES20.glUseProgram(programId); //绘制时使用着色程序
        //矩阵变化
        drawFrameRender(bytes,decodeWidth, decodeHeight, pixWidth, pixHeight);

        GLES20.glVertexAttribPointer(aPositionHandle,
                2,
                GLES20.GL_FLOAT,
                false,
                2 * 4,
                vertexBuffer);
        GLES20.glEnableVertexAttribArray(aPositionHandle); //在用VertexAttribArray前必须先激活它
//        //取两个数U,V
        GLES20.glVertexAttribPointer(aTextureCoordHandle,
                2,
                GLES20.GL_FLOAT,
                false,
                2 * 4,
                textureVertexBuffer);
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle);
        // 交换显存(将surface显存和显示器的显存交换)
//        EGL14.eglMakeCurrent(eglDis, eglSurface, eglSurface, eglCtx);
        EGL14.eglSwapBuffers(eglDis,eglSurface);
        releaseBuffer();
    }
    private byte[] yuvCopy(byte[] src, int offset, int inWidth, int inHeight, byte[] dest, int outWidth, int outHeight) {
        for (int h = 0; h < inHeight; h++) {
            if (h < outHeight) {
                System.arraycopy(src, offset + h * inWidth, dest, h * outWidth, outWidth);
            }
        }
        return dest;
    }
    private void drawFrameRender(byte[] bytes,int decodeWidth, int decodeHeight, int width, int height){
        //YUV数据分离，YUV420SP（NV12和NV21） two-plane模式，即Y和UV分为两个plane UV交错存储
        //Y占byte[]的前 width*height  后面的都是UV数据，其中UV交错存储
        int [] textureIdY = new int[1];
        int [] textureIdU = new int[1];
        int [] textureIdV = new int[1];
        GLES20.glGenTextures(1, textureIdY, 0);
        GLES20.glGenTextures(1, textureIdU, 0);
        GLES20.glGenTextures(1, textureIdV, 0);
        int yBufferSize = decodeWidth * decodeHeight;
        int uBufferSize = decodeWidth / 2 * decodeHeight / 2;
        int vBufferSize = decodeWidth / 2 * decodeHeight / 2;
        byte[] dstY = new byte[yBufferSize];
        byte[] dstU = new byte[uBufferSize];
        byte[] dstV = new byte[vBufferSize];

        System.arraycopy(bytes, 0, dstY, 0,yBufferSize);
        //交叉存储的uv数据分离
        int sizeU = 0,sizeV = 0;
        for (int i = yBufferSize; i < decodeWidth * decodeHeight * 3 / 2; i++) {
            dstU[sizeU] = bytes[i];
            if (i == (decodeWidth * decodeHeight * 3 / 2 - 1)) break;
            dstV[sizeV] = bytes[i + 1];
            sizeU++;
            sizeV++;
            i++;
        }

        //Y纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIdY[0]);
        bindTexture(dstY, width, height);
        GLES20.glUniform1i(tex_y, 0);
        //U纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIdU[0]);
        bindTexture(dstU, width/2, height/2);
        GLES20.glUniform1i(tex_u, 1);
        //V纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIdV[0]);
        bindTexture(dstV, width/2, height/2);
        GLES20.glUniform1i(tex_v, 2);



        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT );
        //纹理坐标转换

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //删除纹理数据，如果不delete，则大量的纹理数据会导致程序crash
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDeleteTextures(1, textureIdY,0);
        GLES20.glDeleteTextures(1, textureIdU,0);
        GLES20.glDeleteTextures(1, textureIdV,0);
    }

    //float[]数组转化成FloatBuffer
    private FloatBuffer floatBufferUtil(float[] arr) {
        FloatBuffer mbuffer;
        // 初始化ByteBuffer，长度为arr.length * 4,因为float占4个字节
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        // 数组排列用nativeOrder
        qbb.order(ByteOrder.nativeOrder());

        mbuffer = qbb.asFloatBuffer();
        mbuffer.put(arr);
        mbuffer.position(0);
        return mbuffer;
    }

    private void bindTexture(byte[] buffer, int width, int height)
    {
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf (GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                0,//GLint level
                GLES20.GL_LUMINANCE,//GLint internalformat
                width,//GLsizei width
                height,// GLsizei height,
                0,//GLint border,
                GLES20.GL_LUMINANCE,//GLenum format,
                GLES20.GL_UNSIGNED_BYTE,//GLenum type,
                ByteBuffer.wrap(buffer)//const void * pixels
        );
    }

    public EGLContext getContext() {
        return eglCtx;
    }

    public void release() {
        Log.i(TAG, "release surface and display");
        surfaceHolder = null;
        EGL14.eglMakeCurrent(eglDis, eglSurface, eglSurface, eglCtx);
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(eglDis, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(eglDis, eglSurface);
            eglSurface = EGL14.EGL_NO_SURFACE;
        }
        if (eglCtx != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglDestroyContext(eglDis, eglCtx);
            eglCtx = EGL14.EGL_NO_CONTEXT;
        }
        if (eglDis != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglTerminate(eglDis);
            eglDis = EGL14.EGL_NO_DISPLAY;
        }
        eglDis = EGL14.EGL_NO_DISPLAY;
        eglSurface = EGL14.EGL_NO_SURFACE;
        eglCtx = EGL14.EGL_NO_CONTEXT;
    }
    private void releaseBuffer(){
        vertexBuffer.clear();
        textureVertexBuffer.clear();
    }
}
