#version 300 es
#extension GL_OES_EGL_image_external : require
precision mediump float;

in vec2 vTextureCo;
uniform samplerExternalOES uTextureSampler;
uniform mat4 uSTMatrix;
out vec4 fragColor;

void main() {
    vec2 uv = vTextureCo;

    float gridWidth = 1.0 / 4.0;
    float gridHeight = 1.0 / 2.0;

    int col = int(uv.x / gridWidth);
    int row = int(uv.y / gridHeight);

    float xInGrid = (uv.x - float(col) * gridWidth) * 4.0;
    float yInGrid = (uv.y - float(row) * gridHeight) * 2.0;

    if ((row % 2 == 0) && (col % 2 == 0)) {
        xInGrid = 1.0 - xInGrid;
        yInGrid = 1.0 - yInGrid;
    } else if (row % 2 == 0) {
        yInGrid = 1.0 - yInGrid;
    } else if (col % 2 == 0) {
        xInGrid = 1.0 - xInGrid;
    }

    vec4 texCoord = uSTMatrix * vec4(xInGrid, yInGrid, 0.0, 1.0);

    fragColor = texture(uTextureSampler, texCoord.xy);
}