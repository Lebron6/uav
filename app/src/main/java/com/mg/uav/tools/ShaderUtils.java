package com.mg.uav.tools;

import android.opengl.GLES20;
import android.util.Log;

/*
*
*
* 着色器程序
*Create by lvyouhai
* */
public class ShaderUtils {
    private static final String TAG = "Shader";
    public static void checkGlError(String label) {
        int error;
        /**
         * 检查每一步的操作是否正确
         *
         * 使用GLES20.glGetError()方法可以获取错误代码, 如果错误代码为0, 那么就没有错误
         *
         * @param op 具体执行的方法名, 比如执行向着色程序中加入着色器,
         *      使glAttachShader()方法, 那么这个参数就是"glAttachShader"
         */
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    /**
     * 创建着色程序
     *
     * ① 加载顶点着色器
     * ② 加载片元着色器
     * ③ 创建着色程序
     * ④ 向着色程序中加入顶点着色器
     * ⑤ 向着色程序中加入片元着色器
     * ⑥ 链接程序
     * ⑦ 获取链接程序结果
     *
     * @param vertexSource      定点着色器脚本字符串
     * @param fragmentSource    片元着色器脚本字符串
     * @return
     */
    public  static int createProgram(String vertexSource, String fragmentSource) {
        //加载顶点着色器
        int vertexShaderIndex = LoadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShaderIndex == 0) {
            return GLES20.GL_FALSE;
        }
        //加载片元着色器
        int pixelShader = LoadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            Log.e(TAG,"pixelShader = 0");
            return GLES20.GL_FALSE;
        }

        //创建程序
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            //向程序中加入顶点着色器
            GLES20.glAttachShader(program, vertexShaderIndex);
          //  checkGlError("glAttachShader");
            //向程序中加入片元着色器
            GLES20.glAttachShader(program, pixelShader);
           // checkGlError("glAttachShader");
            //链接程序
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            //获取program的连接情况
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                //若连接失败则报错并删除程序
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        GLES20.glDeleteShader(vertexShaderIndex);
        GLES20.glDeleteShader(pixelShader);
        return program;
    }

    /**
     * 加载着色器方法
     *
     * 流程 :
     *
     * ① 创建着色器
     * ② 加载着色器脚本
     * ③ 编译着色器
     * ④ 获取着色器编译结果
     *
     * @param shaderType 着色器类型,顶点着色器(GLES20.GL_FRAGMENT_SHADER), 片元着色器(GLES20.GL_FRAGMENT_SHADER)
     * @param source 着色脚本字符串
     * @return 返回的是着色器的引用, 返回值可以代表加载的着色器
     */
    public  static int LoadShader(int shaderType, String source) {
        //1.创建一个着色器, 并记录所创建的着色器的id, 如果id==0, 那么创建失败
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            //2.如果着色器创建成功, 为创建的着色器加载脚本代码
            GLES20.glShaderSource(shader, source);
            //3.编译已经加载脚本代码的着色器
            GLES20.glCompileShader(shader);
            //存放shader的编译情况
            int[] compiled = new int[1];
            //4.获取着色器的编译情况, 如果结果为0, 说明编译失败
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                //若编译失败则显示错误日志并删除此shader
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }
}
