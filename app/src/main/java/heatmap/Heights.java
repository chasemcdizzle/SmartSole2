package heatmap;

import android.opengl.GLES20;

import com.chase.smartsole2.HeatPoint;
import com.chase.smartsole2.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Heights {

	/** Width of the viewport */
	private int width;
	/** Height of the viewport */
	private int height;
	private Shader shader;
//	private Shader clampShader;
//	private Shader multiplyShader;
//	private Shader blurShader;
	/** A Node */
	private Node nodeBack;
	/** A node */
	Node nodeFront;
	/** Array which stores the generated buffer object names */
	private int[] vertexBuffer;
//	private int maxPointCount;
	private FloatBuffer vertexBufferData;
	/** Stores the current index position in the {@code vertexBufferData} */
	private int bufferIndex;
	/** Number of points added to the  {@code vertexBufferData} */
	private int pointCount;
	@SuppressWarnings("unused")
	private static final String LOG = "Heights";

    private int totalPoints;

    private int xLocations[] = {0, 48, 11, 90, 20, 99, 26, 53};
    private int yLocations[] = {20, 73, 118, 125, 208, 235, 323, 361};

    //vertical & horizontal padding for points
    private int verticalPad;
    private int horizontalPad;

    //scale for point positions
    private double xScale;
    private double yScale;

    //furthest points for scaling
    private final int furthestX = 99;
    private final int furthestY = 361; //with 20 offset (see yLocations array)


    //dimensions of image were 137x400


	/**
	 * Create a Height object.
	 *
	 * @param heatmap A heatmap
	 * @param width Width of the viewport
	 * @param height Height of the viewport
	 */
	public Heights(GLHeatmap heatmap, final int width, final int height) {
        totalPoints = 0;
		this.width = width;
		this.height = height;
		this.shader = new Shader(
			"attribute vec4 position, intensity;		\n" +
			"varying vec2 off, dim;						\n" +
			"varying float vIntensity;					\n" +
			"uniform vec2 viewport;						\n" +
			"											\n" +
			"void main(){								\n" +
			"    dim = abs(position.zw);				\n" +
			"    off = position.zw;						\n" +
			"    vec2 pos = position.xy + position.zw;	\n" +
			"    vIntensity = intensity.x;				\n" +
			"    gl_Position = vec4((pos/viewport)*2.0-1.0, 0.0, 1.0);\n" +
			"}",
			"#ifdef GL_FRAGMENT_PRECISION_HIGH			\n" +
			"    precision highp int;					\n" +
			"    precision highp float;					\n" +
			"#else										\n" +
			"    precision mediump int;					\n" +
			"    precision mediump float;				\n" +
			"#endif										\n" +
			"varying vec2 off, dim;						\n" +
			"varying float vIntensity;					\n" +
			"void main(){								\n" +
			"    float falloff = (1.0 - smoothstep(0.0, 1.0, length(off/dim)));\n" +
			"    float intensity = falloff*vIntensity;	\n" +
			"    gl_FragColor = vec4(intensity);		\n" +
			"}");
//		this.clampShader = new Shader(Main.vertexShaderBlit, Main.fragmentShaderBlit +
//				"uniform float low, high;					\n" +
//				"void main(){								\n" +
//				"    gl_FragColor = vec4(clamp(texture2D(source, texcoord).rgb, low, high), 1.0);\n" +
//				"}");
//		this.multiplyShader = new Shader(Main.vertexShaderBlit, Main.fragmentShaderBlit +
//				"uniform float value;						\n" +
//				"void main(){								\n" +
//				"    gl_FragColor = vec4(texture2D(source, texcoord).rgb*value, 1.0);\n" +
//				"}");
//		this.blurShader = new Shader(Main.vertexShaderBlit, Main.fragmentShaderBlit +
//				"uniform vec2 viewport;						\n" +
//				"void main(){								\n" +
//				"    vec4 result = vec4(0.0);				\n" +
//				"    for(int x=-1; x<=1; x++){				\n" +
//				"        for(int y=-1; y<=1; y++){			\n" +
//				"            vec2 off = vec2(x,y)/viewport;\n" +
//				"            //float factor = 1.0 - smoothstep(0.0, 1.5, length(off));\n" +
//				"            float factor = 1.0;			\n" +
//				"            result += vec4(texture2D(source, texcoord+off).rgb*factor, factor);\n" +
//				"        }									\n" +
//				"    }										\n" +
//				"    gl_FragColor = vec4(result.rgb/result.w, 1.0);\n" +
//				"}");
		this.nodeBack = new Node(this.width, this.height);
		this.nodeFront = new Node(this.width, this.height);
		this.vertexBuffer = new int[Main.NUM_BUFFER];
		GLES20.glGenBuffers(Main.NUM_BUFFER, this.vertexBuffer, Main.BUFFER_OFFSET);
        //i modified the first paremeter, now that we are loading 8 points at once, we multiply the buffer by xLocations.length
		this.vertexBufferData = ByteBuffer.allocateDirect(Main.VERTEX_SIZE * Main.NUM_INDICES_RENDER * Main.BYTES_PER_FLOAT * xLocations.length).order(ByteOrder.nativeOrder()).asFloatBuffer();
		this.bufferIndex = 0;
		this.pointCount = 0;

		//function of the screen width
		yScale = (this.height*(0.85)/this.furthestY);
		xScale = yScale;

		//use these!
        horizontalPad = (this.width/2) - (((int) (this.furthestX*xScale) )/2);
        verticalPad = (this.height/2) - (((int) ((this.furthestY+20/* + blur radius*/)*yScale) ) / 2);//20 offsets, see yPositions array

        GLES20.glClearColor(0f, 0f, 0f, 1f);
	}

	/**
	 * Set new size.
	 *
	 * @param width Width of the viewport
	 * @param height Height of the viewport
	 */
	void resize(final int width, final int height) {
		this.width = width;
		this.height = height;
		this.nodeBack.resize(this.width, this.height);
		this.nodeFront.resize(this.width, this.height);
	};

	/**
	 * Update the heatmap, i.e. draw all buffered points from the {code vertexBufferData}.
	 */
	public synchronized void update() {
		if (this.pointCount > 0) {
			GLES20.glEnable(GLES20.GL_BLEND);
			this.nodeFront.use();
                this.vertexBufferData.position(0);
                MyGLRenderer.checkGlError("glBufferData");
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.vertexBuffer[0]);
                //MyGLRenderer.checkGlError("glBindBuffer");
                this.vertexBufferData.position(0);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, this.vertexBufferData.capacity() * Main.BYTES_PER_FLOAT, this.vertexBufferData, GLES20.GL_STATIC_DRAW);
                //GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, this.vertexBufferData.capacity() * Main.BYTES_PER_FLOAT, this.vertexBufferData);
                MyGLRenderer.checkGlError("glBufferData");
//			int positionLoc = this.shader.attribLocation(Main.VARIABLE_ATTRIBUTE_POSITION);
//			int intensityLoc = this.shader.attribLocation(Main.VARIABLE_ATTRIBUTE_INTENSITY);
                //Log.i(LOG, positionLoc + "_" + intensityLoc);
                GLES20.glEnableVertexAttribArray(1);
                //MyGLRenderer.checkGlError("glEnableVertexAttribArray");
                GLES20.glVertexAttribPointer(0, Main.POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, Main.STRIDE_BYTES, 0 * Main.POSITION_DATA_SIZE);
                //MyGLRenderer.checkGlError("glVertexAttribPointer");
                GLES20.glVertexAttribPointer(1, Main.POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, Main.STRIDE_BYTES, Main.BYTES_PER_FLOAT * Main.POSITION_DATA_SIZE);
                //MyGLRenderer.checkGlError("glVertexAttribPointer");
                this.shader.use().vec2(Main.VARIABLE_UNIFORM_VIEWPORT, this.width, this.height);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, this.pointCount * Main.NUM_INDICES_RENDER);
                //MyGLRenderer.checkGlError("glDrawArrays");
                GLES20.glDisableVertexAttribArray(1);
                //MyGLRenderer.checkGlError("glDisableVertexAttribArray");
                this.pointCount = 0;
                //I added this line
                this.bufferIndex = 0;
                this.nodeFront.end();
                GLES20.glDisable(GLES20.GL_BLEND);
            //Log.d(MainActivity.class.getSimpleName(), String.valueOf(totalPoints));
		}
	}

	/**
	 * Clear color and buffer.
	 */
	public void clear() {
        /*
        Log.d(MainActivity.class.getSimpleName(), "something called clear method");
        GLES20.glEnable(GLES20.GL_BLEND);
        this.nodeFront.use();
        GLES20.glClearColor(1f, 0, 0, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        this.nodeFront.end();
        GLES20.glDisable(GLES20.GL_BLEND);
        */
        /*
        this.nodeFront.use();
        GLES20.glDeleteBuffers(vertexBuffer.length, vertexBuffer, 0);
        this.nodeFront.end();
        Log.d(MainActivity.class.getSimpleName(), "cleared");
        */
        //this.nodeFront = new Node(this.width, this.height);
        //this.nodeFront.clear();
        /*
        //removeVertexes();
        //nodeFront.resize(1,1);
		this.nodeFront.use();
        //GLES20.glColorMask(true, true, true, true);
        //GLES20.glDepthMask(true);
		GLES20.glClearColor(0, 0, 0, .5f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		this.nodeFront.end();
        //nodeFront.resize(this.width, this.height);
        //i put this here, doesn't seem to do anything
        //vertexBufferData.clear();
        Log.d(MainActivity.class.getSimpleName(), "cleared");
        */
	}

//	public void clamp(int min, int max) {
//		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.heatmap.quad.get(0));
//		GLES20.glVertexAttribPointer(0, Main.POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);
//		this.nodeFront.bind(0);
//		this.nodeBack.use();
//		this.clampShader.use()._int("source", 0)._float("low", min)._float("high", max);
//		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, Main.UNKOWN_SIX);
//		this.nodeBack.end();
//		this.swap();
//	}
//
//	public void multiply(float value) {
//		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.heatmap.quad.get(0));
//		GLES20.glVertexAttribPointer(0, Main.POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);
//		this.nodeFront.bind(0);
//		this.nodeBack.use();
//		this.multiplyShader.use()._int("source", 0)._float("value", value);
//		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, Main.UNKOWN_SIX);
//		this.nodeBack.end();
//		this.swap();
//	}
//
//	public void blur() {
//		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.heatmap.quad.get(0));
//		GLES20.glVertexAttribPointer(0, Main.POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, 0);
//		this.nodeFront.bind(0);
//		this.nodeBack.use();
//		this.blurShader.use()._int("source", 0).vec2("viewport", this.width, this.height);
//		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, Main.UNKOWN_SIX);
//		this.nodeBack.end();
//		this.swap();
//	}

//	private void swap() {
//		Node tmp = this.nodeFront;
//		this.nodeFront = this.nodeBack;
//		this.nodeBack = tmp;
//	}

	/**
	 * Add a point to the vertex buffer.
	 *
	 * @param x x-coordinate of the point
	 * @param y y-coordinate of the point
	 * @param xs
	 * @param ys
	 * @param intensity intensity (>= 0 and <=1) of the point
	 */
	private synchronized void addVertex(float x, float y, float xs, float ys, float intensity) {
		//Log.i("addVertex", bufferIndex + "_" + x + "_" + y + "_" + xs + "_" + ys + "_" + intensity);
		this.vertexBufferData.put(this.bufferIndex++, x);
		this.vertexBufferData.put(this.bufferIndex++, y);
		this.vertexBufferData.put(this.bufferIndex++, xs);
		this.vertexBufferData.put(this.bufferIndex++, ys);
		this.vertexBufferData.put(this.bufferIndex++, intensity);
		this.vertexBufferData.put(this.bufferIndex++, intensity);
		this.vertexBufferData.put(this.bufferIndex++, intensity);
		this.vertexBufferData.put(this.bufferIndex++, intensity);
	}

    private void removeVertexes(){
        this.vertexBufferData.clear();
        this.vertexBuffer = new int[Main.NUM_BUFFER];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.vertexBuffer[0]);
        GLES20.glGenBuffers(Main.NUM_BUFFER, this.vertexBuffer, Main.BUFFER_OFFSET);
        this.vertexBufferData = ByteBuffer.allocateDirect(Main.VERTEX_SIZE * Main.NUM_INDICES_RENDER * Main.BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, this.vertexBufferData.capacity() * Main.BYTES_PER_FLOAT, this.vertexBufferData, GLES20.GL_STREAM_DRAW);
        this.pointCount = 0;
        this.bufferIndex = 0;
    }

    private void addVertexes(){

    }

	/**
	 * Add a point to the heatmap.
	 *
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param size Size (diameter)
	 * @param intensity Intensity (>= 0 and <= 1)
	 */
	//not used anymore
	public synchronized void addPoint(float x, float y, float size, float intensity) {
        if (this.pointCount >= 1) {
            this.update();
        }
        y = this.height - y;
        float newY = this.height - (int)(yLocations[totalPoints]*yScale);
        float s = size / 2;
        //Log.d(MainActivity.class.getSimpleName(), "y: " + newY + " x: " + xLocations[totalPoints]*4 + "totalPoints: " + totalPoints);
        this.addVertex((int)(xLocations[totalPoints]*xScale), newY, -s, -s, intensity);
        this.addVertex((int)(xLocations[totalPoints]*xScale), newY, +s, -s, intensity);
        this.addVertex((int)(xLocations[totalPoints]*xScale), newY, -s, +s, intensity);
        this.addVertex((int)(xLocations[totalPoints]*xScale), newY, -s, +s, intensity);
        this.addVertex((int)(xLocations[totalPoints]*xScale), newY, +s, -s, intensity);
        this.addVertex((int)(xLocations[totalPoints]*xScale), newY, +s, +s, intensity);
        this.pointCount += 1;
        totalPoints++;
		/*
		//original code
        if (this.pointCount >= 1) {
			this.update();
		}
		y = this.height - y;
		float s = size / 2;
		this.addVertex(x, y, -s, -s, intensity);
		this.addVertex(x, y, +s, -s, intensity);
		this.addVertex(x, y, -s, +s, intensity);
		this.addVertex(x, y, -s, +s, intensity);
		this.addVertex(x, y, +s, -s, intensity);
		this.addVertex(x, y, +s, +s, intensity);
		this.pointCount += 1;
        totalPoints++;
        */
}

	//I believe the scaling of the heatmap points is here
    public synchronized void addPoints(HeatPoint points[]) {
        if (this.pointCount >= 1) {
            this.update();
        }
        for(int i = 0; i < points.length; i++) {
            //Log.d(MainActivity.class.getSimpleName(), i + " " + points[i].x + " " + points[i].y);
            //float y = this.height - points[i].y;
            int y = yLocations[i];
            y = this.height - y;
            float newY = this.height - ( (int)(yLocations[i]*yScale) + verticalPad );
            float s = points[i].size*((float)xScale) / 2;
            float newX = (int)(xLocations[i]*xScale) + horizontalPad;
            //Log.d(MainActivity.class.getSimpleName(), "y: " + y + " = height: " + this.height + " - yloc: " + yLocations[i]);
            this.addVertex(newX, newY, -s, -s, points[i].intensity);
            this.addVertex(newX, newY, +s, -s, points[i].intensity);
            this.addVertex(newX, newY, -s, +s, points[i].intensity);
            this.addVertex(newX, newY, -s, +s, points[i].intensity);
            this.addVertex(newX, newY, +s, -s, points[i].intensity);
            this.addVertex(newX, newY, +s, +s, points[i].intensity);
            //this.addVertex(points[i].x, y, +s, +s, points[i].intensity);
            this.pointCount += 1;
            totalPoints++;
        }
    }

}
