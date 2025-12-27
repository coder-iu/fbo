#version 300 es
precision mediump float;

in vec2 vTextureCo;
uniform sampler2D uTextureSampler;
uniform float brightness;
out vec4 fragColor;

void main() {
    vec4 color = texture(uTextureSampler, vTextureCo);
    vec3 outRGB = color.rgb * (1.0 - brightness);
    fragColor = vec4(outRGB, color.a);
}
