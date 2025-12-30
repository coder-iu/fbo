package com.example.fbostudy.rendering

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import androidx.annotation.RawRes
import com.example.fbostudy.bean.VaoBean
import com.example.fbostudy.bean.Window
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 *@author Y³
 *@date 2025/5/9
 */
object OpenGLHelper {

    private val TAG = OpenGLHelper.javaClass.name

    /**
     * 从res/raw读取着色器代码
     * @param context 上下文对象
     * @param resId 资源ID (R.raw.xxx)
     * @return 着色器代码字符串，失败返回空字符串
     */
    @JvmStatic
    fun readShaderFromRaw(context: Context, @RawRes resId: Int): String {
        return try {
            context.resources.openRawResource(resId).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val sb = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line).append('\n')
                    }
                    sb.toString()
                }
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Shader resource not found (ID: $resId)")

            ""
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read shader (ID: $resId)", e)
            ""
        }
    }

    /**
     * 从assets读取着色器代码
     * @param context 上下文对象
     * @param assetPath 资源路径 (如 "shaders/vertex.glsl")
     * @return 着色器代码字符串，失败返回空字符串
     */
    @JvmStatic
    fun readShaderFromAssets(context: Context, assetPath: String): String {
        return try {
            context.assets.open(assetPath).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read shader from assets: $assetPath", e)
            ""
        }
    }

    fun createOESTexture(): Int {
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        val texture = textures[0]
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture)
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR.toFloat()
        )
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR.toFloat()
        )
        GLES30.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE
        )
        GLES30.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE
        )
        return texture
    }


    /**
     * 创建GLSL程序
     * @param vertexShaderCode 顶点着色器代码
     * @param fragmentShaderCode 片段着色器代码
     * @return 程序ID，0表示失败
     */
    @JvmStatic
    fun createShaderProgram(vertexShaderCode: String, fragmentShaderCode: String): Int {
        if (vertexShaderCode.isEmpty() || fragmentShaderCode.isEmpty()) {
            Log.e(TAG, "Empty shader code")
            return 0
        }

        val vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)
        if (vertexShader == 0 || fragmentShader == 0) return 0

        return linkProgram(vertexShader, fragmentShader)
    }

    // ========== 私有方法 ==========

    private fun compileShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val infoLog = GLES30.glGetShaderInfoLog(shader)
            Log.e(
                TAG,
                "Shader compilation error:\n$infoLog\nCode:\n${
                    shaderCode.lines().take(10).joinToString("\n")
                }..."
            )
            GLES30.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    private fun linkProgram(vertexShader: Int, fragmentShader: Int): Int {
        val program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        GLES30.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val infoLog = GLES30.glGetProgramInfoLog(program)
            Log.e(TAG, "Program linking error:\n$infoLog")
            GLES30.glDeleteProgram(program)
            return 0
        }

        // 安全删除已附加的着色器
        GLES30.glDetachShader(program, vertexShader)
        GLES30.glDetachShader(program, fragmentShader)
        GLES30.glDeleteShader(vertexShader)
        GLES30.glDeleteShader(fragmentShader)

        return program
    }


    fun draw(
        mode: Int = GLES30.GL_TRIANGLES,
        count: Int,
        hasEbo: Boolean = false,
        offset: Int = 0,
        instances: Int = 1
    ) {
        if (hasEbo) {
            if (instances > 1) {
                GLES30.glDrawElementsInstanced(
                    mode, count, GLES30.GL_UNSIGNED_INT, offset, instances
                )
            } else {
                GLES30.glDrawElements(mode, count, GLES30.GL_UNSIGNED_INT, offset)
            }
        } else {
            if (instances > 1) {
                GLES30.glDrawArraysInstanced(mode, offset, count, instances)
            } else {
                GLES30.glDrawArrays(mode, offset, count)
            }
        }
    }

    fun drawControlPoint(programId: Int, aPositionLoc: Int, x: Float, y: Float) {

        val vertices = floatArrayOf(x, y)

        val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
        vertexBuffer.position(0)

        // 使用program
        GLES30.glUseProgram(programId)
        // 传顶点坐标
        GLES30.glEnableVertexAttribArray(aPositionLoc)
        GLES30.glVertexAttribPointer(
            aPositionLoc,
            2, GLES30.GL_FLOAT,
            false,
            2 * 4,
            vertexBuffer
        )
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1)
        GLES30.glDisableVertexAttribArray(aPositionLoc)

    }

    fun loadTextureFromAssets(context: Context, fileName: String): Int {
        val inputStream = context.assets.open(fileName)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val textureIds = IntArray(1)
        GLES30.glGenTextures(1, textureIds, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0])
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE
        )
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        return textureIds[0]
    }

    private fun logError(message: String, e: Exception? = null) {
        Log.e(TAG, message, e)
    }

    fun createFBO(width: Int, height: Int): Pair<Int, Int> {
        // 生成纹理
        val texIds = IntArray(1)
        GLES30.glGenTextures(1, texIds, 0)
        val texId = texIds[0]
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texId)
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RGBA,
            width,
            height,
            0,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            null
        )
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE
        )

        // 生成 FBO
        val fboIds = IntArray(1)
        GLES30.glGenFramebuffers(1, fboIds, 0)
        val fboId = fboIds[0]
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboId)
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER,
            GLES30.GL_COLOR_ATTACHMENT0,
            GLES30.GL_TEXTURE_2D,
            texId,
            0
        )

        // 检查 FBO 状态
        val status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)
        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("FBO 创建失败, status=$status")
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)

        return Pair(fboId, texId)
    }


    fun releaseFBO(fboId: Int, fboTextureId: Int) {
        // 解绑 FBO（安全起见）
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)

        // 删除 FBO
        if (fboId != 0) {
            val fboIds = intArrayOf(fboId)
            GLES30.glDeleteFramebuffers(1, fboIds, 0)
        }

        // 删除纹理
        if (fboTextureId != 0) {
            val texIds = intArrayOf(fboTextureId)
            GLES30.glDeleteTextures(1, texIds, 0)
        }
    }


    fun createFullScreenVao(): Int {
        // GL_TRIANGLE_STRIP 顺序：左上, 左下, 右上, 右下
        val vertexData = floatArrayOf(
            -1f,  1f,  // 左上
            -1f, -1f,  // 左下
            1f,  1f,  // 右上
            1f, -1f   // 右下
        )

        val texCoordData = floatArrayOf(
            0f, 0f,  // 左上
            0f, 1f,  // 左下
            1f, 0f,  // 右上
            1f, 1f   // 右下
        )

        val vaoId = createVAO()
        val verVboId = createVBO(
            data = vertexData,
            attributeLocation = 0,
            componentSize = 2
        )
        val texVboId = createVBO(
            data = texCoordData,
            attributeLocation = 1,
            componentSize = 2
        )
        // VAO 内绑定完毕，解绑
        GLES30.glBindVertexArray(0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)

        return vaoId
    }

    private fun createVAO(): Int {
        val vaoIds = IntArray(1)
        GLES30.glGenVertexArrays(1, vaoIds, 0)
        GLES30.glBindVertexArray(vaoIds[0])
        return vaoIds[0]
    }

    private fun createVBO(
        data: FloatArray,
        attributeLocation: Int,
        componentSize: Int,
        normalized: Boolean = false,
        stride: Int = 0,
        offset: Int = 0,
        usage: Int = GLES30.GL_STATIC_DRAW
    ): Int {
        val buffer = createFloatBuffer(data)

        val vboIds = IntArray(1)
        GLES30.glGenBuffers(1, vboIds, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboIds[0])
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            buffer.capacity() * Float.SIZE_BYTES,
            buffer,
            usage
        )

        GLES30.glEnableVertexAttribArray(attributeLocation)
        GLES30.glVertexAttribPointer(
            attributeLocation,
            componentSize,
            GLES30.GL_FLOAT,
            normalized,
            stride,
            offset
        )
        return vboIds[0]
    }

    fun generateWinVao(
        mScreenW: Int, mScreenH: Int,
        aPositionLocation: Int, aTextureCoLocation: Int,
        win: Window
    ): VaoBean {
        val vertex: FloatArray =
            win.generateRotatedVertexData(mScreenW.toFloat(), mScreenH.toFloat())
        val texture: FloatArray = win.calculateRotatedTextureCoordinates(
            mScreenW.toFloat(), mScreenH.toFloat()
        )

        val vaoId = createVAO()
        val verVboId = createVBO(
            data = vertex,
            attributeLocation = aPositionLocation,
            componentSize = 2,
        )
        val texVboId = createVBO(
            data = texture,
            attributeLocation = aTextureCoLocation,
            componentSize = 2,
        )

        return VaoBean(win.winType, vaoId, verVboId, texVboId)
    }

    fun releaseVAO(vaoId: Int) {
        GLES30.glDeleteVertexArrays(1, intArrayOf(vaoId), 0)
    }

    fun releaseVBO(vboId: Int) {
        GLES30.glDeleteBuffers(1, intArrayOf(vboId), 0)
    }

    private fun createFloatBuffer(data: FloatArray): FloatBuffer {
        val bb = ByteBuffer.allocateDirect(data.size * Float.SIZE_BYTES)
        bb.order(ByteOrder.nativeOrder())
        val buffer = bb.asFloatBuffer()
        buffer.put(data)
        buffer.position(0)
        return buffer
    }


    fun createControlPointVao(
        aPositionLocation: Int,
        points: FloatArray // 8 个点，共 16 个 float（x, y）
    ): VaoBean {
        val vaoIds = IntArray(1)
        GLES30.glGenVertexArrays(1, vaoIds, 0)
        GLES30.glBindVertexArray(vaoIds[0])
        val vboIds = IntArray(1)
        GLES30.glGenBuffers(1, vboIds, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboIds[0])
        val buffer = ByteBuffer.allocateDirect(points.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(points)
        buffer.position(0)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            points.size * 4,
            buffer,
            GLES30.GL_STATIC_DRAW
        )

        // 绑定属性 aPosition
        GLES30.glEnableVertexAttribArray(aPositionLocation)
        GLES30.glVertexAttribPointer(
            aPositionLocation,
            2, GLES30.GL_FLOAT, false,
            2 * 4, 0
        )
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindVertexArray(0)

        return VaoBean(vaoType = 999, vaoId = vaoIds[0], verVboId = vboIds[0], texVboId = 0)
    }


}