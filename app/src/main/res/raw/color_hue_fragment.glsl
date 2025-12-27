#version 300 es
precision mediump float;

in vec2 vTextureCo;
uniform sampler2D uTextureSampler;
uniform float hue;
out vec4 fragColor;

vec3 rgb2yiq(vec3 color) {
    float Y = dot(color, vec3(0.299, 0.587, 0.114));
    float I = dot(color, vec3(0.596, -0.274, -0.322));
    float Q = dot(color, vec3(0.211, -0.523, 0.312));
    return vec3(Y, I, Q);
}

vec3 yiq2rgb(vec3 yiq) {
    float Y = yiq.x;
    float I = yiq.y;
    float Q = yiq.z;
    float R = Y + 0.956 * I + 0.621 * Q;
    float G = Y - 0.272 * I - 0.647 * Q;
    float B = Y - 1.106 * I + 1.703 * Q;
    return vec3(R, G, B);
}

void main() {
    vec4 color = texture(uTextureSampler, vTextureCo);
    vec3 yiq = rgb2yiq(color.rgb);

    float angle = hue * 3.1415926; // hue [-1,1] → [-π, π]
    float cosA = cos(angle);
    float sinA = sin(angle);

    float I = yiq.y * cosA - yiq.z * sinA;
    float Q = yiq.y * sinA + yiq.z * cosA;

    vec3 newColor = yiq2rgb(vec3(yiq.x, I, Q));
    fragColor = vec4(newColor, color.a);
}
