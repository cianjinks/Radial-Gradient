import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Application {

    private long window;
    public static int WINDOW_WIDTH = 720;
    public static int WINDOW_HEIGHT = 720;
    public static String WINDOW_TITLE = "Bloom";

    public void run() {

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();

    }

    private void init() {

        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_TITLE, NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop() {
        GL.createCapabilities();
        GLUtil.setupDebugMessageCallback();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // IBO (Index Buffer Object)
        // 768 for 16kb of vertex memory
        int numQuads = 1;
        // BLUE
        Vertex v0 = new Vertex(); v0.setXYZW(0.0f, 0.0f, 0.0f, 1.0f); v0.setRGBA(0.0f, 0.0f, 1.0f, 1.0f);
        Vertex v1 = new Vertex(); v1.setXYZW(0.0f, WINDOW_HEIGHT, 0.0f, 1.0f); v1.setRGBA(0.0f, 0.0f, 1.0f, 1.0f);
        Vertex v2 = new Vertex(); v2.setXYZW(WINDOW_WIDTH, WINDOW_HEIGHT, 0.0f, 1.0f); v2.setRGBA(0.0f, 0.0f, 1.0f, 1.0f);
        Vertex v3 = new Vertex(); v3.setXYZW(WINDOW_WIDTH, 0.0f, 0.0f, 1.0f); v3.setRGBA(0.0f, 0.0f, 1.0f, 1.0f);
        // RED
//        Vertex v0 = new Vertex(); v0.setXYZW(0.0f, 0.0f, 0.0f, 1.0f); v0.setRGBA(1.0f, 0.0f, 0.0f, 1.0f);
//        Vertex v1 = new Vertex(); v1.setXYZW(0.0f, WINDOW_HEIGHT, 0.0f, 1.0f); v1.setRGBA(1.0f, 0.0f, 0.0f, 1.0f);
//        Vertex v2 = new Vertex(); v2.setXYZW(WINDOW_WIDTH, WINDOW_HEIGHT, 0.0f, 1.0f); v2.setRGBA(1.0f, 0.0f, 0.0f, 1.0f);
//        Vertex v3 = new Vertex(); v3.setXYZW(WINDOW_WIDTH, 0.0f, 0.0f, 1.0f); v3.setRGBA(1.0f, 0.0f, 0.0f, 1.0f);
        // COLOR MIX
//        Vertex v0 = new Vertex(); v0.setXYZW(0.0f, 0.0f, 0.0f, 1.0f); v0.setRGBA(1.0f, 1.0f, 0.0f, 1.0f);
//        Vertex v1 = new Vertex(); v1.setXYZW(0.0f, WINDOW_HEIGHT, 0.0f, 1.0f); v1.setRGBA(1.0f, 0.0f, 1.0f, 1.0f);
//        Vertex v2 = new Vertex(); v2.setXYZW(WINDOW_WIDTH, WINDOW_HEIGHT, 0.0f, 1.0f); v2.setRGBA(0.0f, 1.0f, 1.0f, 1.0f);
//        Vertex v3 = new Vertex(); v3.setXYZW(WINDOW_WIDTH, 0.0f, 0.0f, 1.0f); v3.setRGBA(0.0f, 0.0f, 1.0f, 1.0f);
        Vertex[] vertices = new Vertex[] {v0, v1, v2, v3};

        int[] indices = new int[numQuads * 6];
        int offset = 0;
        for(int i = 0; i < indices.length; i += 6) {
            indices[i + 0] = 0 + offset;
            indices[i + 1] = 1 + offset;
            indices[i + 2] = 2 + offset;

            indices[i + 3] = 2 + offset;
            indices[i + 4] = 3 + offset;
            indices[i + 5] = 0 + offset;

            offset += 4;
        }
        IntBuffer iboBuffer = BufferUtils.createIntBuffer(indices.length);
        iboBuffer.put(indices);
        iboBuffer.flip();

        // VAO (Vertex Array Object)
        int vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);

        // Bind Buffer Data
        int vboID = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
        // Call is now dynamic and so we allocate memory (16kB) or 512 vertices (8 floats per vertex) (768 indices)
        FloatBuffer vboBuffer = MemoryUtil.memAllocFloat(2 * 4 * 512);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vboBuffer.capacity() * Float.BYTES, GL30.GL_DYNAMIC_DRAW);
        GL30.glVertexAttribPointer(0, Vertex.positionElementCount, Vertex.type, false, Vertex.stride, Vertex.positionOffset);
        GL30.glVertexAttribPointer(1, Vertex.colorElementCount, Vertex.type, false, Vertex.stride, Vertex.colorOffset);

        GL30.glBindVertexArray(0);

        int iboID = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, iboID);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, iboBuffer, GL30.GL_STATIC_DRAW);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);

        Shader shaderHandler = new Shader();
        shaderHandler.addShader("/shaders/vert.shader", GL30.GL_VERTEX_SHADER);
        shaderHandler.addShader("/shaders/frag.shader", GL30.GL_FRAGMENT_SHADER);
        shaderHandler.validateProgram();
        shaderHandler.bindProgram();

        Matrix4f mvp = new Matrix4f().ortho(0.0f, WINDOW_WIDTH, 0.0f, WINDOW_HEIGHT, -1.0f, 1.0f);

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            for(Vertex vertex : vertices) {
                vboBuffer.put(vertex.getXYZW());
                vboBuffer.put(vertex.getRGBA());
            }
            vboBuffer.flip();

            shaderHandler.bindProgram();
            shaderHandler.setUniMat4f("u_MVP", mvp);

            // Bind VBO and dynamically fill it with data
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
            GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, vboBuffer);

            // Bind VAO
            GL30.glBindVertexArray(vaoID);
            GL30.glEnableVertexAttribArray(0);
            GL30.glEnableVertexAttribArray(1);
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, iboID);

            // Draw the vertices
            GL30.glDrawElements(GL30.GL_TRIANGLES, numQuads * 6, GL_UNSIGNED_INT, 0);

            // Unbind VAO
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);
            GL30.glDisableVertexAttribArray(0);
            GL30.glDisableVertexAttribArray(1);
            GL30.glBindVertexArray(0);

            // Clear the VBO
            vboBuffer.clear();

            shaderHandler.unBindProgram();

            glfwSwapBuffers(window); // swap the color buffers
            glfwPollEvents();

        }
    }

    public static void main(String[] args) {
        new Application().run();
    }

}
